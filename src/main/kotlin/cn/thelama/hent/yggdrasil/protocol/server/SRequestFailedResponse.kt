package cn.thelama.hent.yggdrasil.protocol.server

import kotlinx.serialization.Serializable

@Serializable
data class SRequestFailedResponse(val error: String, val errorMessage: String, val cause: String)
