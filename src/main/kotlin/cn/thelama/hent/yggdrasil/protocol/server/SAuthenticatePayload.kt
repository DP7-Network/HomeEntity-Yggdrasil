package cn.thelama.hent.yggdrasil.protocol.server

import cn.thelama.hent.yggdrasil.protocol.mojang.types.AvailableProfile
import cn.thelama.hent.yggdrasil.protocol.mojang.types.SelectedProfile
import cn.thelama.hent.yggdrasil.protocol.mojang.types.User
import kotlinx.serialization.Serializable

@Serializable
data class SAuthenticatePayload(
    val accessToken: String,
    val availableProfiles: List<AvailableProfile>,
    val clientToken: String,
    val selectedProfile: SelectedProfile,
    val user: User
)