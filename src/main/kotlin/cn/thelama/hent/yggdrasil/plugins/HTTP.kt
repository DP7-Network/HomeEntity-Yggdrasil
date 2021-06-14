package cn.thelama.hent.yggdrasil.plugins

import cn.thelama.hent.yggdrasil.configuration
import io.ktor.features.*
import io.ktor.application.*

fun Application.configureHTTP() {
    install(HttpsRedirect) {
        sslPort = configuration.getInt("server.ssl.port")
        permanentRedirect = configuration.getBoolean("server.ssl.forceRedirect")
    }


}
