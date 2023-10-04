package net.perfectdreams.loritta.serializable.internal.requests

import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.serializable.dashboard.requests.DashGuildScopedRequest

@Serializable
sealed class LorittaInternalRPCRequest {
    @Serializable
    class ExecuteDashGuildScopedRPCRequest(
        val guildId: Long,
        val memberIdToBePermissionCheckedAgainst: Long,
        val dashRequest: DashGuildScopedRequest
    ) : LorittaInternalRPCRequest()

    @Serializable
    data object GetLorittaInfoRequest : LorittaInternalRPCRequest()

    @Serializable
    data object UpdateTwitchSubscriptionsRequest : LorittaInternalRPCRequest()

    @Serializable
    data class TwitchStreamOnlineEventRequest(
        val twitchUserId: Long,
        val twitchUserLogin: String,
        val title: String?,
        val gameName: String?,
    ) : LorittaInternalRPCRequest()
}