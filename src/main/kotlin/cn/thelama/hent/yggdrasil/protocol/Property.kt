package cn.thelama.hent.yggdrasil.protocol

import kotlinx.serialization.Serializable

@Serializable
data class Property(
    val name: String,
    val value: String
)