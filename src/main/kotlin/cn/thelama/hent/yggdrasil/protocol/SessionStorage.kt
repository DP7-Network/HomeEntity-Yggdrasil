package cn.thelama.hent.yggdrasil.protocol

import kotlinx.serialization.Serializable

@Serializable
data class SessionStorage(val playerName: String, val accessToken: String, val clientToken: String, val expireTime: Long)