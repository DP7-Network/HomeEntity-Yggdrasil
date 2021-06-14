package cn.thelama.hent.yggdrasil.plugins

import io.ktor.serialization.*
import io.ktor.features.*
import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*

fun Application.configureSerialization() {
    install(ContentNegotiation) {
        json()
    }
}
