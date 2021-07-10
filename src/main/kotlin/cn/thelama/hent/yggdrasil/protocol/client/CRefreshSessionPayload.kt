package cn.thelama.hent.yggdrasil.protocol.client

import cn.thelama.hent.yggdrasil.protocol.mojang.types.SelectedProfile
import kotlinx.serialization.Serializable

@Serializable
data class CRefreshSessionPayload(
    val accessToken: String,
    val clientToken: String,
    val requestUser: Boolean,
    val selectedProfile: SelectedProfile
)