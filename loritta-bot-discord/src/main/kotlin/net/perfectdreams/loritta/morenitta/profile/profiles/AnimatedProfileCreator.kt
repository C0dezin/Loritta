package net.perfectdreams.loritta.morenitta.profile.profiles

import dev.kord.common.entity.Snowflake
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.dao.Profile
import net.perfectdreams.loritta.morenitta.profile.ProfileGuildInfoData
import net.perfectdreams.loritta.morenitta.profile.ProfileUserInfoData
import java.awt.image.BufferedImage

abstract class AnimatedProfileCreator(loritta: LorittaBot, internalName: String) : ProfileCreator(loritta, internalName) {
    abstract suspend fun create(
        sender: ProfileUserInfoData,
        user: ProfileUserInfoData,
        userProfile: Profile,
        guild: ProfileGuildInfoData?,
        badges: List<BufferedImage>,
        locale: BaseLocale,
        i18nContext: I18nContext,
        background: BufferedImage,
        aboutMe: String,
        allowedDiscordEmojis: List<Snowflake>?
    ): List<BufferedImage>
}