package net.perfectdreams.loritta.morenitta.profile.badges

import dev.kord.common.entity.UserFlag
import net.perfectdreams.i18nhelper.core.keydata.StringI18nData
import net.perfectdreams.loritta.morenitta.dao.Profile
import net.perfectdreams.loritta.morenitta.profile.Badge
import net.perfectdreams.loritta.morenitta.profile.ProfileDesignManager
import net.perfectdreams.loritta.morenitta.profile.ProfileUserInfoData
import java.util.*

open class DiscordUserFlagBadge(
	val flag: UserFlag,
	id: UUID,
	title: StringI18nData,
	description: StringI18nData,
	badgeName: String
) : Badge.LorittaBadge(id, title, description, badgeName, 50) {
	class DiscordBraveryHouseBadge : DiscordUserFlagBadge(
		UserFlag.HouseBravery,
		UUID.fromString("4415250e-0a5f-40b9-bd0f-caa8d97b55e3"),
		ProfileDesignManager.I18N_BADGES_PREFIX.DiscordBravery.Title,
		ProfileDesignManager.I18N_BADGES_PREFIX.DiscordBravery.Description,
		"discord_bravery.png"
	)
	class DiscordBrillianceHouseBadge : DiscordUserFlagBadge(
		UserFlag.HouseBrilliance,
		UUID.fromString("1812c988-0eec-405e-8670-d5419ccb1fe8"),
		ProfileDesignManager.I18N_BADGES_PREFIX.DiscordBrilliance.Title,
		ProfileDesignManager.I18N_BADGES_PREFIX.DiscordBrilliance.Description,
		"discord_brilliance.png"
	)
	class DiscordBalanceHouseBadge : DiscordUserFlagBadge(
		UserFlag.HouseBalance,
		UUID.fromString("7e8973ff-65be-4941-afb9-8df6b8febfc9"),
		ProfileDesignManager.I18N_BADGES_PREFIX.DiscordBalance.Title,
		ProfileDesignManager.I18N_BADGES_PREFIX.DiscordBalance.Description,
		"discord_balance.png"
	)
	class DiscordEarlySupporterBadge : DiscordUserFlagBadge(
		UserFlag.EarlySupporter,
		UUID.fromString("3349d275-390f-40c2-83dc-8245bd530f0e"),
		ProfileDesignManager.I18N_BADGES_PREFIX.DiscordEarlySupporter.Title,
		ProfileDesignManager.I18N_BADGES_PREFIX.DiscordEarlySupporter.Description,
		"discord_early_supporter.png"
	)
	class DiscordPartnerBadge : DiscordUserFlagBadge(
		UserFlag.DiscordPartner,
		UUID.fromString("97004586-188f-4d4a-bba8-53b7fd5e0a9a"),
		ProfileDesignManager.I18N_BADGES_PREFIX.DiscordPartner.Title,
		ProfileDesignManager.I18N_BADGES_PREFIX.DiscordPartner.Description,
		"discord_partner.png"
	)
	class DiscordHypesquadEventsBadge : DiscordUserFlagBadge(
		UserFlag.HypeSquad,
		UUID.fromString("f5665d18-ff6d-4660-be07-ac0aa1188447"),
		ProfileDesignManager.I18N_BADGES_PREFIX.DiscordHypesquadEvents.Title,
		ProfileDesignManager.I18N_BADGES_PREFIX.DiscordHypesquadEvents.Description,
		"hypesquad_events.png"
	)
	class DiscordVerifiedDeveloperBadge : DiscordUserFlagBadge(
		UserFlag.VerifiedBotDeveloper,
		UUID.fromString("3e8fc05e-490f-4533-bd39-af7f81a81867"),
		ProfileDesignManager.I18N_BADGES_PREFIX.DiscordVerifiedDeveloper.Title,
		ProfileDesignManager.I18N_BADGES_PREFIX.DiscordVerifiedDeveloper.Description,
		"verified_developer.png"
	)

	override suspend fun checkIfUserDeservesBadge(user: ProfileUserInfoData, profile: Profile, mutualGuilds: Set<Long>) = user.flags.contains(flag)
}