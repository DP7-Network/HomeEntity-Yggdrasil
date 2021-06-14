package cn.thelama.hent.yggdrasil.protocol.client

import kotlinx.serialization.Serializable

@Serializable
data class CSignout(
    val password: String,
    val username: String
)