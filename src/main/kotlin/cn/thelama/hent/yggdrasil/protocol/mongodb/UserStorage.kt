package cn.thelama.hent.yggdrasil.protocol.mongodb

import kotlinx.serialization.Serializable

@Serializable
data class UserStorage(val uid: String, val username: String, val password: String, val playerName: String, val uuid: String, val skinLink: String, val capeLink: String)