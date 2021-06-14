package cn.thelama.hent.yggdrasil.protocol.admin

import kotlinx.serialization.Serializable

@Serializable
data class CLoginPayload(val username: String, val password: String)
