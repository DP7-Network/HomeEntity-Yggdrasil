package cn.thelama.hent.yggdrasil

import io.ktor.server.engine.*
import io.ktor.server.netty.*
import cn.thelama.hent.yggdrasil.plugins.*
import com.mongodb.*
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import org.bson.Document
import java.io.File
import java.io.FileOutputStream
import kotlin.system.exitProcess
import cn.thelama.hent.yggdrasil.plugins.configureRouting
import io.ktor.application.*
import java.security.KeyStore

const val CONFIG_SUPPORT_VERSION = 1.0
lateinit var mongo: MongoClient
lateinit var yggdrasil: MongoDatabase
lateinit var users: MongoCollection<Document>
lateinit var sessions: MongoCollection<Document>
lateinit var webSessions: MongoCollection<Document>
lateinit var configuration: Config

fun main(args: Array<String>) {
    if("-no-clear" !in args) {
        print("\u001B[2J")
        print("\u001B[1;1H")
    }
    println("\n\n\n           Welcome to HomeEntity Yggdrasil!           ")
    println("HomeEntity Yggdrasil 1.0 | Ktor with Netty Engine | Starting...")
    runCatching {
        val conf = File("config.conf")
        if(!conf.exists()) {
            conf.createNewFile()
            val ips = Thread.currentThread().contextClassLoader.getResourceAsStream("defaultConfig.conf")!!
            val ops = FileOutputStream(conf)
            var d = ips.read()
            while(d != -1) {
                ops.write(d)
                d = ips.read()
            }
            ops.close()
            ips.close()
            print("\n\n")
            println("  !! FAILED TO START YGGDRASIL SERVICE !!  ")
            println("Default config not found! A new config file generated at ./config.conf. Please edit this file and restart!")
            exitProcess(1)
        }

        configuration = ConfigFactory.parseFile(conf)

        if(configuration.getDouble("configFormat") != CONFIG_SUPPORT_VERSION) {
            println("  !! FAILED TO START YGGDRASIL SERVICE !!  ")
            println("Configuration file version don't match with current supported version! Current version supports: $CONFIG_SUPPORT_VERSION but we get ${configuration.getDouble("configFormat")}")
            exitProcess(1)
        }
    }.onFailure {
        print("\n\n")
        it.printStackTrace()
        println("  !! FAILED TO START YGGDRASIL SERVICE !!  ")
        println("Error while loading config file! Please check your config settings")
        exitProcess(1)
    }

    runCatching {
        mongo = if(configuration.getBoolean("mongo.useUrl")) {
            MongoClient(MongoClientURI(configuration.getString("mongo.url")))
        } else {
            MongoClient(
                ServerAddress(configuration.getString("mongo.address"), configuration.getInt("mongo.port")),
                MongoCredential.createPlainCredential(
                    configuration.getString("mongo.username"),
                    configuration.getString("mongo.database"),
                    configuration.getString("mongo.password").toCharArray()),
                MongoClientOptions.builder().build())
        }

        yggdrasil = mongo.getDatabase(configuration.getString("mongo.database"))
        users = yggdrasil.getCollection(configuration.getString("mongo.collections.usersName"))
        sessions = yggdrasil.getCollection(configuration.getString("mongo.collections.sessionsName"))
        webSessions = yggdrasil.getCollection(configuration.getString("mongo.collections.webSessions"))
    }.onFailure {
        print("\n\n")
        it.printStackTrace()
        println("  !! FAILED TO START YGGDRASIL SERVICE !!  ")
        println("Error while setting up database! Please check your database settings")
        exitProcess(1)
    }

    embeddedServer(Netty, environment = applicationEngineEnvironment {
        module(Application::configureRouting)
        module(Application::configureSecurity)
        if(configuration.getBoolean("server.enable-ssl")) {
            sslConnector(
                KeyStore.getInstance(File(configuration.getString("server.ssl.keyStore")), configuration.getString("server.ssl.keyStorePassword").toCharArray()),
                configuration.getString("server.ssl.keyAlias"),
                { configuration.getString("server.ssl.keyStorePassword").toCharArray() },
                { configuration.getString("server.ssl.privateKeyPassword").toCharArray() },
            ) {
                this.port = configuration.getInt("server.ssl.port")
            }
            module(Application::configureHTTP)
        }
        module(Application::configureSerialization)
        module(Application::configureMonitoring)

        connector {
            this.port = configuration.getInt("server.port")
            this.host = configuration.getString("server.host")
        }
    }, configure = {

    }).start(wait = true)
}