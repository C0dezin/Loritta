package net.perfectdreams.loritta.morenitta.commands.nashorn

import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.commands.AbstractCommand
import net.perfectdreams.loritta.morenitta.commands.CommandContext
import net.perfectdreams.loritta.morenitta.utils.ExperienceUtils
import net.perfectdreams.loritta.morenitta.utils.MessageUtils
import net.perfectdreams.loritta.serializable.CustomCommandCodeType

/**
 * Comandos usando a Nashorn Engine
 */
class NashornCommand(loritta: LorittaBot, label: String, val javaScriptCode: String, val codeType: CustomCommandCodeType) : AbstractCommand(loritta, label, category = net.perfectdreams.loritta.common.commands.CommandCategory.MISC) {
	override suspend fun run(context: CommandContext, locale: BaseLocale) {
		when (codeType) {
			CustomCommandCodeType.SIMPLE_TEXT -> {
				val customTokens = mutableMapOf<String, String>()

				if (javaScriptCode.contains("{experience") || javaScriptCode.contains("{level") || javaScriptCode.contains("{xp")) {
					customTokens.putAll(
						ExperienceUtils.getExperienceCustomTokens(
							loritta,
							context.config,
							context.handle
						)
					)
				}

				val message = MessageUtils.generateMessageOrFallbackIfInvalid(
					context.i18nContext,
					javaScriptCode,
					listOf(
						context.handle,
						context.guild,
						context.message.channel
					),
					context.guild,
					customTokens = customTokens,
					i18nKey = I18nKeysData.InvalidMessages.CustomCommand
				)

				context.sendMessage(message)
			}
			else -> throw RuntimeException("Unsupported code type $codeType")
		}
	}
}