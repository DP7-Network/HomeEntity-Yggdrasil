package cn.thelama.hent.yggdrasil.protocol

import kotlinx.serialization.Serializable

@Serializable
data class SelectedProfile(
    val id: String,
    val name: String
)