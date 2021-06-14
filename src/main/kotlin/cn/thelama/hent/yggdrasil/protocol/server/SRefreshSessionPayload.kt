package cn.thelama.hent.yggdrasil.protocol.server

import cn.thelama.hent.yggdrasil.protocol.SelectedProfile
import cn.thelama.hent.yggdrasil.protocol.User
import kotlinx.serialization.Serializable

@Serializable
data class SRefreshSessionPayload(
    val accessToken: String,
    val clientToken: String,
    val selectedProfile: SelectedProfile,
    val user: User
)