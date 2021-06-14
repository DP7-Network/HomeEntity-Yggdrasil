package cn.thelama.hent.yggdrasil.protocol.admin

import kotlinx.serialization.Serializable

@Serializable
data class CChangePassword(val old: String, val new: String) {
}