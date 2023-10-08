package net.perfectdreams.loritta.morenitta.commands.vanilla.administration

import net.perfectdreams.loritta.morenitta.commands.AbstractCommand
import net.perfectdreams.loritta.morenitta.commands.CommandContext
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji
import net.perfectdreams.loritta.morenitta.messages.LorittaReply
import net.perfectdreams.loritta.morenitta.LorittaBot

class RemoveEmojiCommand(loritta: LorittaBot) : AbstractCommand(loritta, "removeemoji", listOf("deleteemoji", "deletaremoji", "removeremoji", "delemoji"), net.perfectdreams.loritta.common.commands.CommandCategory.MODERATION) {
	// TODO: Fix Usage

	override fun getDescriptionKey() = LocaleKeyData("commands.command.removeemoji.description")

	override fun getDiscordPermissions(): List<Permission> {
		return listOf(Permission.MANAGE_EMOJIS_AND_STICKERS)
	}

	override fun getBotPermissions(): List<Permission> {
		return listOf(Permission.MANAGE_EMOJIS_AND_STICKERS)
	}

	override suspend fun run(context: CommandContext,locale: BaseLocale) {
		var deletedEmotes = 0

		context.message.mentions.customEmojis.filterIsInstance<RichCustomEmoji>().forEach {
			if (it.guild == context.guild) {
				it.delete().queue()
				deletedEmotes++
			}
		}

		if (deletedEmotes != 0) {
			context.reply(
				LorittaReply(
					locale["commands.command.removeemoji.success", deletedEmotes, if (deletedEmotes == 1) "emoji" else "emojis"],
					"\uD83D\uDDD1"
				)
			)
		} else {
			context.explain()
		}
	}
}