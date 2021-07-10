package cn.thelama.hent.yggdrasil.protocol.mojang.types

import kotlinx.serialization.Serializable

@Serializable
data class SelectedProfile(
    val id: String,
    val name: String
)