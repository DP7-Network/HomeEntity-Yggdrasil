package cn.thelama.hent.yggdrasil.protocol.server

import cn.thelama.hent.yggdrasil.protocol.AvailableProfile
import cn.thelama.hent.yggdrasil.protocol.SelectedProfile
import cn.thelama.hent.yggdrasil.protocol.User
import kotlinx.serialization.Serializable

@Serializable
data class SAuthenticatePayload(
    val accessToken: String,
    val availableProfiles: List<AvailableProfile>,
    val clientToken: String,
    val selectedProfile: SelectedProfile,
    val user: User
)