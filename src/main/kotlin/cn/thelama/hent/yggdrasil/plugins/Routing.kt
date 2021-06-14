package cn.thelama.hent.yggdrasil.plugins

import cn.thelama.hent.yggdrasil.protocol.*
import cn.thelama.hent.yggdrasil.protocol.admin.*
import cn.thelama.hent.yggdrasil.protocol.client.*
import cn.thelama.hent.yggdrasil.protocol.server.SAuthenticatePayload
import cn.thelama.hent.yggdrasil.protocol.server.SRequestFailedResponse
import cn.thelama.hent.yggdrasil.sessions
import cn.thelama.hent.yggdrasil.users
import cn.thelama.hent.yggdrasil.webSessions
import com.mongodb.BasicDBObject
import com.mongodb.client.model.Filters
import io.ktor.routing.*
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.bson.Document
import java.security.MessageDigest
import java.util.*
import kotlin.math.abs

private val ALLOWED_CONTENT_TYPE = ContentType.parse("application/json")
private val RANDOM = Random()

fun Application.configureRouting() {
    routing {
        get("/") {
            call.respondText("Welcome to HomeEntity Yggdrasil! Main page is still under developing")
        }

        /**
         * 在Yggdrasil 协议中定义
         */
        post("/authenticate") {
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
        post("/refresh") {
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
        post("/validate") {
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
        post("/signout") {
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
        post("/invalidate") {
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
         *                   用户名               密码
         * 登录 参数: Json { username -> String, password -> String }
         * 返回:
         *   204 -> 成功，并携带网页验证用Cookie
         *   403 -> 失败，账号/密码不正确
         */
        post("/management/login") {
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
        post("/management/changePassword") {
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
        post("/management/setSkin") {
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
        post("/management/setCape") {
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
        post("/management/register") {
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
        get("/management/online") {
            val result = sessions.find()
            val names = mutableListOf<String>()
            result.forEach {
                names += it.getString("playerName")
            }
            call.respond(HttpStatusCode.OK, Json.encodeToString(SOnlines(result.count(), names)))
        }
    }
}

fun sha256(str: String): String {
    return MessageDigest.getInstance("SHA-256").digest(str.toByteArray(charset("UTF-8"))).joinToString("") { abs(it.toInt()).toString(16).padStart(2, '0') }
}