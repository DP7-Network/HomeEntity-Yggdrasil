package cn.thelama.hent.yggdrasil.protocol.mojang.types

import kotlinx.serialization.Serializable

@Serializable
data class Agent(
    val name: String,
    val version: Int
)