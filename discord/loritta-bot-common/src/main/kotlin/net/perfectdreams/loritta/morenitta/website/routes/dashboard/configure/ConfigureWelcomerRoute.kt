package net.perfectdreams.loritta.morenitta.website.routes.dashboard.configure

import net.perfectdreams.loritta.morenitta.dao.ServerConfig
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.morenitta.website.evaluate
import io.ktor.server.application.*
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.website.routes.dashboard.RequiresGuildAuthLocalizedRoute
import net.perfectdreams.loritta.morenitta.website.session.LorittaJsonWebSession
import net.perfectdreams.loritta.morenitta.website.utils.extensions.legacyVariables
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondHtml
import net.perfectdreams.loritta.morenitta.website.views.LegacyPebbleGuildDashboardRawHtmlView
import net.perfectdreams.loritta.morenitta.website.views.LegacyPebbleRawHtmlView
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth
import kotlin.collections.set

class ConfigureWelcomerRoute(loritta: LorittaBot) : RequiresGuildAuthLocalizedRoute(loritta, "/configure/welcomer") {
	override suspend fun onGuildAuthenticatedRequest(call: ApplicationCall, locale: BaseLocale, i18nContext: I18nContext, discordAuth: TemmieDiscordAuth, userIdentification: LorittaJsonWebSession.UserIdentification, guild: Guild, serverConfig: ServerConfig) {
		val welcomerConfig = loritta.newSuspendedTransaction {
			serverConfig.welcomerConfig
		}

		val variables = call.legacyVariables(loritta, locale)

		variables["saveType"] = "welcomer"
		variables["serverConfig"] = FakeServerConfig(
				FakeServerConfig.FakeWelcomerConfig(
						welcomerConfig != null,
						welcomerConfig?.tellOnJoin ?: false,
						welcomerConfig?.tellOnRemove ?: false,
						welcomerConfig?.tellOnBan ?: false,
						welcomerConfig?.tellOnPrivateJoin ?: false,
						welcomerConfig?.joinMessage ?: "\uD83D\uDC49 {@user} entrou no servidor!",
						welcomerConfig?.removeMessage ?: "\uD83D\uDC48 {user.name} saiu do servidor!",
						welcomerConfig?.joinPrivateMessage ?: "Obrigado por entrar na {guild} {@user}! Espero que você curta o nosso servidor!",
						welcomerConfig?.bannedMessage ?: "{user} foi banido do servidor!",
						welcomerConfig?.deleteJoinMessagesAfter ?: 0L,
						welcomerConfig?.deleteRemoveMessagesAfter ?: 0L
				)
		)

		call.respondHtml(
			LegacyPebbleGuildDashboardRawHtmlView(
				loritta,
				i18nContext,
				locale,
				getPathWithoutLocale(call),
				loritta.getLegacyLocaleById(locale.id),
				guild,
				"Painel de Controle",
				evaluate("welcomer.html", variables),
				"welcomer"
			).generateHtml()
		)
	}

	/**
	 * Fake Server Config for Pebble, in the future this will be removed
	 */
	private class FakeServerConfig(val joinLeaveConfig: FakeWelcomerConfig) {
		class FakeWelcomerConfig(
				val isEnabled: Boolean,
				val tellOnJoin: Boolean,
				val tellOnLeave: Boolean,
				val tellOnBan: Boolean,
				val tellOnPrivate: Boolean,
				val joinMessage: String,
				val removeMessage: String,
				val joinPrivateMessage: String,
				val bannedMessage: String,
				val deleteJoinMessagesAfter: Long,
				val deleteRemoveMessagesAfter: Long
		)
	}
}