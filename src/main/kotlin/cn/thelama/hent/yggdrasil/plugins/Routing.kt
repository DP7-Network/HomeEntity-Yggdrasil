package cn.thelama.hent.yggdrasil.plugins

import cn.thelama.hent.yggdrasil.*
import cn.thelama.hent.yggdrasil.protocol.*
import cn.thelama.hent.yggdrasil.protocol.admin.*
import cn.thelama.hent.yggdrasil.protocol.client.*
import cn.thelama.hent.yggdrasil.protocol.server.SAuthenticatePayload
import cn.thelama.hent.yggdrasil.protocol.server.SRequestFailedResponse
import com.amazonaws.services.s3.model.ObjectMetadata
import com.mongodb.BasicDBObject
import com.mongodb.client.model.Filters
import io.ktor.routing.*
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.utils.io.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.bson.Document
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.nio.ByteBuffer
import java.security.MessageDigest
import java.util.*
import kotlin.math.abs

private val ALLOWED_CONTENT_TYPE = ContentType.parse("application/json")
private val RANDOM = Random()
private val userContentRoot = File("user-content")
private val skinBaseDir = File(userContentRoot, "skins")
private val capeBaseDir = File(userContentRoot, "capes")

fun Application.configureRouting() {
    if(!userContentRoot.exists()) {
        userContentRoot.mkdir()
    }
    if(!skinBaseDir.exists()) {
        skinBaseDir.mkdir()
    }
    if(!capeBaseDir.exists()) {
        capeBaseDir.mkdir()
    }

    routing {
        get("/") {
            call.response.header("X-Authlib-Injector-API-Location", "/meta")
            call.respondText("Welcome to HomeEntity Yggdrasil! Main page is still under developing")
        }
        
        get("/meta") { }

        /**
         * 在Yggdrasil 协议中定义 | 适配 Authlib injector
         * Autenticate API
         */
        post("/authserver/authenticate") {
            if(call.request.contentType() == ALLOWED_CONTENT_TYPE) {
                kotlin.runCatching {
                    val req = call.receive(CAuthenticatePayload::class)
                    if(req.agent.name.lowercase() == "minecraft") {
                        val lookup = users.find(Filters.eq("username", req.username)).filter(Filters.eq("password", sha256(req.password))).iterator().tryNext()
                        if(lookup != null) {
                            val uuid = lookup["uuid"] as String
                            val name = lookup["playerName"] as String
                            val token = sha256((System.currentTimeMillis() + RANDOM.nextLong()).toString() + name)
                            val clientToken = req.clientToken ?: sha256((Math.random() + System.currentTimeMillis()).toString() + uuid)
                            sessions.deleteMany(Filters.eq("playerName", name))
                            sessions.insertOne(Document.parse(Json.encodeToString(SessionStorage(name, token, clientToken, System.currentTimeMillis() + 86400000 * 3)))) // 86400000 is 1 day
                            call.respond(HttpStatusCode.OK, SAuthenticatePayload(token, listOf(AvailableProfile(uuid, name)), clientToken, SelectedProfile(uuid.replace("-", ""), name), User(lookup.getString("uid"), listOf(), name)))
                        } else {
                            call.respond(HttpStatusCode.Forbidden, Json.encodeToString(SRequestFailedResponse("ForbiddenOperationException", "Invalid credentials. Invalid username or password.", "Wrong username/password")))
                        }
                    } else {
                        call.respond(HttpStatusCode.NotAcceptable, Json.encodeToString(SRequestFailedResponse("Game not supported", "This yggdrasil server is for Minecraft not other game!", "Game not supported")))
                    }
                }.onFailure {
                    it.printStackTrace()
                    call.respond(HttpStatusCode.BadRequest, Json.encodeToString(SRequestFailedResponse("Unsupported Media", "The server is refusing to service the request because the entity of the request is in a format not supported by the requested resource for the requested method. Please check your request body. Make sure they are criterion json format!", "Illegal Content")))
                }
            } else {
                call.respond(HttpStatusCode.BadRequest, Json.encodeToString(SRequestFailedResponse("Unsupported Media Type", "The server is refusing to service the request because the entity of the request is in a format not supported by the requested resource for the requested method. Please try change the header field: Content-Type to application/json", "Illegal Content-Type Header Field")))
            }
        }
        post("/authserver/refresh") {
            if(call.request.contentType() == ALLOWED_CONTENT_TYPE) {
                val req = call.receive(CRefreshSessionPayload::class)
                val d = sessions.findOneAndUpdate(Filters.eq("accessToken", req.accessToken), Document("\$set", Document("expireTime", System.currentTimeMillis() + 86400000 * 3)))
                if(d == null) {
                    call.respond(HttpStatusCode.Forbidden)
                } else {
                    //                                                                                                                        不确定此处User的uuid是否需要-还是说不需要- 出BUG就来这里看就对了
                    call.respond(HttpStatusCode.OK, SAuthenticatePayload(req.accessToken, listOf(), req.clientToken, req.selectedProfile, User(req.selectedProfile.id, listOf(), req.selectedProfile.name)))
                }
            } else {
                call.respond(HttpStatusCode.BadRequest, Json.encodeToString(SRequestFailedResponse("Unsupported Media Type", "The server is refusing to service the request because the entity of the request is in a format not supported by the requested resource for the requested method. Please try change the header field: Content-Type to application/json", "Illegal Content-Type Header Field")))
            }
        }
        post("/authserver/validate") {
            if(call.request.contentType() == ALLOWED_CONTENT_TYPE) {
                val req = call.receive(CValidate::class)
                val d = sessions.find(Filters.eq("accessToken", req.accessToken)).iterator().tryNext()
                if(d == null) {
                    call.respond(HttpStatusCode.Forbidden)
                } else {
                    if(d.getLong("expire") > System.currentTimeMillis()) {
                        call.respond(HttpStatusCode.NoContent)
                    } else {
                        call.respond(HttpStatusCode.Forbidden)
                    }
                }
            }
        }
        post("/authserver/signout") {
            if (call.request.contentType() == ALLOWED_CONTENT_TYPE) {
                val req = call.receive(CSignout::class)
                val lookup = users.find(Filters.eq("username", req.username)).filter(Filters.eq("password", sha256(req.password))).iterator().tryNext()
                if(lookup != null) {
                    sessions.deleteMany(Filters.eq("playerName", lookup.getString("playerName")))
                    call.respond(HttpStatusCode.OK, "")
                } else {
                    call.respond(HttpStatusCode.Forbidden, Json.encodeToString(SRequestFailedResponse("ForbiddenOperationException", "Invalid credentials. Invalid username or password.", "Wrong username/password")))
                }
            }
        }
        post("/authserver/invalidate") {
            if(call.request.contentType() == ALLOWED_CONTENT_TYPE) {
                val req = call.receive(CInvalidateToken::class)
                val d = sessions.deleteMany(Filters.eq("accessToken", req.accessToken)).deletedCount
                if(d <= 0) {
                    call.respond(HttpStatusCode.Forbidden)
                } else {
                    call.respond(HttpStatusCode.NoContent)
                }
            }
        }

        /**
         * 在Yggdrasil 协议中定义 | 适配 Authlib injector
         * Autenticate API
         */

        post("/sessionserver/session/minecraft/join") {}
        get("/sessionserver/session/minecraft/hasJoined?username={username}&serverId={serverId}&ip={ip}") {}
        get("/sessionserver/session/minecraft/profile/{uuid}?unsigned={unsigned}") {}
        post("/api/profiles/minecraft") {}

        /**
         *                   用户名               密码
         * 登录 参数: Json { username -> String, password -> String }
         * 返回:
         *   204 -> 成功，并携带网页验证用Cookie
         *   403 -> 失败，账号/密码不正确
         */
        post("/webClient/login") {
            if(call.request.contentType() == ALLOWED_CONTENT_TYPE) {
                val req = call.receive(CLoginPayload::class)
                val lookup = users.find(Filters.eq("username", req.username)).filter(Filters.eq("password", sha256(req.password))).iterator().tryNext()
                if(lookup != null) {
                    val token = sha256((System.currentTimeMillis() + RANDOM.nextLong()).toString() + req.username)
                    webSessions.deleteMany(Filters.eq("username", req.username))
                    webSessions.insertOne(Document.parse(Json.encodeToString(WebSessionStorage(token, req.username, System.currentTimeMillis() + 86400000))))
                    call.response.cookies.append(Cookie("Token", token))
                    call.respond(HttpStatusCode.NoContent)
                } else {
                    call.respond(HttpStatusCode.Forbidden)
                }
            }
        }

        /**
         *                    老密码          新密码
         * 修改密码 参数: Json { old -> String, new -> String }
         * 返回:
         *   204 -> 成功
         *   403 -> 失败，老密码不正确
         *   401 -> 未携带Token Cookie或Cookie值不正确
         */
        post("/webClient/changePassword") {
            if(call.request.contentType() == ALLOWED_CONTENT_TYPE) {
                val cookie = call.request.cookies["Token"]
                if(cookie == null) {
                    call.respond(HttpStatusCode.Forbidden)
                    return@post
                }
                val lookup = webSessions.find(Filters.eq("token", cookie)).iterator().tryNext()
                if(lookup != null) {
                    if(lookup.getLong("expire") > System.currentTimeMillis()) {
                        val req = call.receive(CChangePassword::class)
                        val username = lookup["username"] as String
                        if(users.updateOne(BasicDBObject().also {
                            it["username"] = username
                            it["password"] = sha256(req.old)
                        }, BasicDBObject().also { root ->
                            root["\$set"] = BasicDBObject().also {
                                it["password"] = sha256(req.new)
                            }
                        }).modifiedCount > 0) {
                            call.respond(HttpStatusCode.NoContent)
                        } else {
                            call.respond(HttpStatusCode.Forbidden)
                        }
                    } else {
                        call.respond(HttpStatusCode.Unauthorized)
                    }
                } else {
                    call.respond(HttpStatusCode.Unauthorized)
                }
            }
        }

        /**
         *                   皮肤链接
         * 设置皮肤 参数: Json { url -> String }
         * 返回:
         *   204 -> 成功
         *   500 -> 失败，内部错误，联系开发者解决
         *   401 -> 失败，未携带Token Cookie或Cookie值不正确
         */
        post("/webClient/setSkin") {
            if(call.request.contentType() == ALLOWED_CONTENT_TYPE) {
                val cookie = call.request.cookies["Token"]
                if(cookie == null) {
                    call.respond(HttpStatusCode.Forbidden)
                    return@post
                }
                val lookup = webSessions.find(Filters.eq("token", cookie)).iterator().tryNext()
                if(lookup != null) {
                    if(lookup.getLong("expire") > System.currentTimeMillis()) {
                        val req = call.receive(CSetResource::class)
                        val username = lookup["username"] as String
                        if(users.updateOne(BasicDBObject().also {
                                it["username"] = username
                            }, BasicDBObject().also { root ->
                                root["\$set"] = BasicDBObject().also {
                                    it["skinLink"] = req.url
                                }
                            }).modifiedCount > 0) {
                            call.respond(HttpStatusCode.NoContent)
                        } else {
                            call.respond(HttpStatusCode.InternalServerError)
                        }
                    } else {
                        call.respond(HttpStatusCode.Unauthorized)
                    }
                } else {
                    call.respond(HttpStatusCode.Unauthorized)
                }
            }
        }

        /**
         *                   披风链接
         * 设置披风 参数: Json { url -> String }
         * 返回:
         *   204 -> 成功
         *   500 -> 失败，内部错误，联系开发者解决
         *   401 -> 失败，未携带Token Cookie或Cookie值不正确
         */
        post("/webClient/setCape") {
            if(call.request.contentType() == ALLOWED_CONTENT_TYPE) {
                val cookie = call.request.cookies["Token"]
                if(cookie == null) {
                    call.respond(HttpStatusCode.Forbidden)
                    return@post
                }
                val lookup = webSessions.find(Filters.eq("token", cookie)).iterator().tryNext()
                if(lookup != null) {
                    if(lookup.getLong("expire") > System.currentTimeMillis()) {
                        val req = call.receive(CSetResource::class)
                        val username = lookup["username"] as String
                        if(users.updateOne(BasicDBObject().also {
                                it["username"] = username
                            }, BasicDBObject().also { root ->
                                root["\$set"] = BasicDBObject().also {
                                    it["capeLink"] = req.url
                                }
                            }).modifiedCount > 0) {
                            call.respond(HttpStatusCode.NoContent)
                        } else {
                            call.respond(HttpStatusCode.Forbidden)
                        }
                    } else {
                        call.respond(HttpStatusCode.Unauthorized)
                    }
                } else {
                    call.respond(HttpStatusCode.Unauthorized)
                }
            }
        }

        /**
         *                   用户名               密码
         * 注册 参数: Json { username -> String, password -> String }
         * 返回:
         *   204 -> 成功
         *   200 -> 失败，账号已被注册
         *   400 -> 失败，请求格式不正确
         */
        post("/webClient/register") {
            if(call.request.contentType() == ALLOWED_CONTENT_TYPE) {
                kotlin.runCatching {
                    val req = call.receive(CRegisterPayload::class)
                    if(users.find(Filters.eq("username", req.username)).iterator().hasNext()) {
                        call.respond(HttpStatusCode.OK, SRequestFailedResponse("Username already exist", "Username already exist", ""))
                    }
                    users.insertOne(Document.parse(Json.encodeToString(UserStorage(sha256(RANDOM.nextInt().toString() + req.username), req.username, sha256(req.password), req.username, UUID.nameUUIDFromBytes("Offline:${req.username}".toByteArray()).toString(),"", ""))))
                }.onFailure {
                    it.printStackTrace()
                    call.respond(HttpStatusCode.BadRequest)
                }.onSuccess {
                    call.respond(HttpStatusCode.NoContent)
                }
            }
        }

        /**
         *
         * 列出在线人数 参数: 无
         * 返回:
         *          在线人数                 名称列表
         *   Json { count -> Int, names -> List<String> }
         */
        get("/webClient/online") {
            val result = sessions.find()
            val names = mutableListOf<String>()
            result.forEach {
                names += it.getString("playerName")
            }
            call.respond(HttpStatusCode.OK, Json.encodeToString(SOnlines(result.count(), names)))
        }

        /**
         *
         * 上传用户资源(皮肤/披风)
         * 请求参数:
         *   type -> 上传类型 <skin|cape>
         * 返回:
         *   200 -> 成功
         *   401 -> 失败，未携带Token Cookie或Cookie值不正确
         */
        put("/webClient/uploadUserContent") {
            val cookie = call.request.cookies["Token"]
            if(cookie == null) {
                call.respond(HttpStatusCode.Forbidden)
                return@put
            }

            val lookup = webSessions.find(Filters.eq("token", cookie)).iterator().tryNext()
            if(lookup == null || lookup.getLong("expire") < System.currentTimeMillis()) {
                call.respond(HttpStatusCode.Unauthorized)
            }

            val t = context.request.queryParameters["type"]
            val file = when(t) {
                "skin" -> File(skinBaseDir, "${lookup["username"] as String}.tmp").apply {
                    if (exists()) {
                        delete()
                    }
                    createNewFile()
                }
                "cape" -> File(capeBaseDir, "${lookup["username"] as String}.tmp").apply {
                    if(exists()) {
                        delete()
                    }
                    createNewFile()
                }
                else -> {
                    call.respond(HttpStatusCode.BadRequest)
                    return@put
                }
            }

            val lenRaw = context.request.headers["Content-Length"]
            if(lenRaw != null) {
                val len = lenRaw.toLongOrNull()
                if(len != null) {
                    if(len < 10_000_000) {
                        val digest = MessageDigest.getInstance("SHA-256")
                        val buffer = ByteBuffer.allocate(len.toInt())
                        val channel = context.receiveChannel()
                        channel.readFully(buffer)
                        println("Buffer len: ${buffer.capacity()}")
                        buffer.position(0)
                        val os = FileOutputStream(file)
                        val osChannel = os.channel
                        var writeLen = osChannel.write(buffer)
                        println("Write len: $writeLen")
                        while(writeLen != 0) {
                            writeLen = osChannel.write(buffer)
                            println("Write len: $writeLen")
                        }
                        os.close()
                        digest.update(buffer)
                        file.renameTo(File(if(t == "skin") skinBaseDir else capeBaseDir, digest.digest().joinToString(separator = "") { byte -> "%02x".format(byte) }))
                        context.respond(HttpStatusCode.OK)
                    } else {
                        context.respond(HttpStatusCode.BadRequest)
                    }
                } else {
                    context.respond(HttpStatusCode.BadRequest)
                }
            } else {
                context.respond(HttpStatusCode.BadRequest)
            }
        }
    }
}

fun sha256(str: String): String {
    return MessageDigest.getInstance("SHA-256").digest(str.toByteArray(charset("UTF-8"))).joinToString("") { abs(it.toInt()).toString(16).padStart(2, '0') }
}