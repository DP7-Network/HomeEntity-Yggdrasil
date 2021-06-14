package cn.thelama.hent.yggdrasil.protocol.client

import cn.thelama.hent.yggdrasil.protocol.Agent
import kotlinx.serialization.Serializable

@Serializable
data class CAuthenticatePayload(
    val agent: Agent,
    val clientToken: String?,
    val password: String,
    val requestUser: Boolean,
    val username: String
)