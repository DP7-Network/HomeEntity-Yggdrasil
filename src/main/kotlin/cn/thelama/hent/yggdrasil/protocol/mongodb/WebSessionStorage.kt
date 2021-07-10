package cn.thelama.hent.yggdrasil.protocol.mongodb

import kotlinx.serialization.Serializable

@Serializable
data class WebSessionStorage(val token: String, val username: String, val expire: Long)
