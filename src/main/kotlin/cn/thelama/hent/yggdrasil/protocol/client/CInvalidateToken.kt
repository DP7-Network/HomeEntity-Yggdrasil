package cn.thelama.hent.yggdrasil.protocol.client

import kotlinx.serialization.Serializable

@Serializable
data class CInvalidateToken(
    val accessToken: String,
    val clientToken: String
)