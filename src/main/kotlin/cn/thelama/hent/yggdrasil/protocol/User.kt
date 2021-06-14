package cn.thelama.hent.yggdrasil.protocol

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String,
    val properties: List<Property>,
    val username: String
)