package cn.thelama.hent.yggdrasil.protocol.mojang.types

import kotlinx.serialization.Serializable

@Serializable
data class Property(
    val name: String,
    val value: String
)