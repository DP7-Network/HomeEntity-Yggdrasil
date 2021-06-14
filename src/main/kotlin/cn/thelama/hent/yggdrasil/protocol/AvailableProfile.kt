package cn.thelama.hent.yggdrasil.protocol

import kotlinx.serialization.Serializable

@Serializable
data class AvailableProfile(
    val id: String,
    val name: String
)