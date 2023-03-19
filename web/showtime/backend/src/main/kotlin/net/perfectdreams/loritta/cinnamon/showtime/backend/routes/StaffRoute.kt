package net.perfectdreams.loritta.cinnamon.showtime.backend.routes

import net.perfectdreams.loritta.common.locale.BaseLocale
import io.ktor.server.application.*
import io.ktor.server.html.*
import net.perfectdreams.dokyo.RoutePath
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.pudding.data.UserId
import net.perfectdreams.loritta.cinnamon.showtime.backend.ShowtimeBackend
import net.perfectdreams.loritta.cinnamon.showtime.backend.utils.userTheme
import net.perfectdreams.loritta.cinnamon.showtime.backend.views.StaffView

class StaffRoute(val showtime: ShowtimeBackend) : LocalizedRoute(showtime, RoutePath.TEAM) {
    override suspend fun onLocalizedRequest(call: ApplicationCall, locale: BaseLocale, i18nContext: I18nContext) {
        val discordAvatars = StaffView.staffList.groups.flatMap { it.users }
            .mapNotNull {
                val discordSocialNetwork = it.socialNetworks.filterIsInstance<StaffView.Companion.DiscordSocialNetwork>()
                    .firstOrNull()

                if (discordSocialNetwork != null) {
                    val cachedUserInfo = showtime.pudding.users.getCachedUserInfoById(
                        UserId(discordSocialNetwork.userId)
                    )

                    val avatarId = cachedUserInfo?.avatarId
                    if (avatarId != null) {
                        // https://cdn.discordapp.com/avatars/123170274651668480/8bd2b747f135c65fd2da873c34ba485c.png?size=2048
                        discordSocialNetwork.userId to "https://cdn.discordapp.com/avatars/${cachedUserInfo.id.value}/${avatarId}.${if (avatarId.startsWith("a_")) "gif" else "webp"}?size=256"
                    } else null
                } else null
            }.toMap()

        val aboutMe = StaffView.staffList.groups.flatMap { it.users }
            .mapNotNull {
                val discordSocialNetwork = it.socialNetworks.filterIsInstance<StaffView.Companion.DiscordSocialNetwork>()
                    .firstOrNull()

                if (discordSocialNetwork != null) {
                    val profileSettings = showtime.pudding.users.getUserProfile(UserId(discordSocialNetwork.userId))
                        ?.getProfileSettings()

                    if (profileSettings != null)
                        discordSocialNetwork.userId to profileSettings.aboutMe
                    else null
                } else null
            }.toMap()

        call.respondHtml(
            block = StaffView(
                showtime,
                call.request.userTheme,
                locale,
                i18nContext,
                "/staff",
                discordAvatars,
                aboutMe
            ).generateHtml()
        )
    }
}