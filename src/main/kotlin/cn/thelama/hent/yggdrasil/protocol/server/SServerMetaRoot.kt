package cn.thelama.hent.yggdrasil.protocol.server

import kotlinx.serialization.Serializable

@Serializable
data class SServerMetaRoot(val meta: ServerMeta, val skinDomains: List<String>)
