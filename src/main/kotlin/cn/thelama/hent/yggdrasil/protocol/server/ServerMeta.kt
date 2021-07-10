package cn.thelama.hent.yggdrasil.protocol.server

import kotlinx.serialization.Serializable

@Serializable
data class ServerMeta(val serverName: String, val implementationName: String)
