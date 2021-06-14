package cn.thelama.hent.yggdrasil.protocol.admin

import kotlinx.serialization.Serializable

@Serializable
data class CRegisterPayload(val username: String, val password: String)
