package cn.thelama.hent.yggdrasil.protocol.admin

import kotlinx.serialization.Serializable

@Serializable
data class SOnlines(val count: Int, val names: List<String>)
