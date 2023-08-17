package net.perfectdreams.loritta.morenitta.website.routes.dashboard

import net.perfectdreams.loritta.cinnamon.pudding.tables.GuildProfiles
import net.perfectdreams.loritta.morenitta.website.LorittaWebsite
import net.perfectdreams.loritta.morenitta.website.evaluate
import io.ktor.server.application.*
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.website.routes.RequiresDiscordLoginLocalizedRoute
import net.perfectdreams.loritta.morenitta.website.session.LorittaJsonWebSession
import net.perfectdreams.loritta.morenitta.website.utils.extensions.legacyVariables
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondHtml
import net.perfectdreams.loritta.morenitta.website.views.LegacyPebbleProfileDashboardRawHtmlView
import net.perfectdreams.loritta.morenitta.website.views.LegacyPebbleRawHtmlView
import net.perfectdreams.loritta.morenitta.website.views.SelectGuildProfileDashboardView
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.update
import kotlin.collections.set

class DashboardRoute(loritta: LorittaBot) : RequiresDiscordLoginLocalizedRoute(loritta, "/dashboard") {
	override suspend fun onAuthenticatedRequest(call: ApplicationCall, locale: BaseLocale, i18nContext: I18nContext, discordAuth: TemmieDiscordAuth, userIdentification: LorittaJsonWebSession.UserIdentification) {
		val variables = call.legacyVariables(loritta, locale)

		val lorittaProfile = loritta.getOrCreateLorittaProfile(userIdentification.id.toLong())
		val settings = loritta.newSuspendedTransaction { lorittaProfile.settings }
		variables["lorittaProfile"] = lorittaProfile
		variables["settings"] = settings

		val userGuilds = discordAuth.getUserGuilds()
		val userGuildsIds = userGuilds.map { it.id.toLong() }

		// Update if the user is in a guild or not based on the retrieved guilds
		loritta.newSuspendedTransaction {
			GuildProfiles.update({ (GuildProfiles.userId eq lorittaProfile.id.value) and (GuildProfiles.guildId inList userGuildsIds) }) {
				it[GuildProfiles.isInGuild] = true
			}

			GuildProfiles.update({ (GuildProfiles.userId eq lorittaProfile.id.value) and (GuildProfiles.guildId notInList userGuildsIds) }) {
				it[GuildProfiles.isInGuild] = false
			}
		}

		val guilds = userGuilds.filter { LorittaWebsite.canManageGuild(it) }

		variables["userGuilds"] = guilds
		val userPermissionLevels = mutableMapOf<TemmieDiscordAuth.Guild, LorittaWebsite.UserPermissionLevel>()
		val joinedServers = mutableMapOf<TemmieDiscordAuth.Guild, Boolean>()
		for (guild in guilds) {
			userPermissionLevels[guild] = LorittaWebsite.getUserPermissionLevel(guild)
			joinedServers[guild] = loritta.lorittaShards.getGuildById(guild.id) != null
		}
		variables["userPermissionLevels"] = userPermissionLevels
		variables["joinedServers"] = joinedServers
		variables["saveType"] = "main"

		call.respondHtml(
			SelectGuildProfileDashboardView(
				loritta,
				i18nContext,
				locale,
				getPathWithoutLocale(call),
				loritta.getLegacyLocaleById(locale.id),
				guilds,
			).generateHtml()
		)
	}
}