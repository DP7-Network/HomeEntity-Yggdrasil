package cn.thelama.hent.yggdrasil.plugins

import io.ktor.sessions.*
import io.ktor.application.*
fun Application.configureSecurity() {
    install(Sessions) {

    }
}