package net.perfectdreams.loritta.morenitta.commands.vanilla.minecraft

import net.perfectdreams.loritta.morenitta.commands.AbstractCommand
import net.perfectdreams.loritta.morenitta.commands.CommandContext
import net.perfectdreams.loritta.morenitta.utils.Constants
import net.perfectdreams.loritta.morenitta.utils.LorittaUtils
import net.perfectdreams.loritta.morenitta.utils.minecraft.MCUtils
import net.perfectdreams.loritta.morenitta.messages.LorittaReply
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import net.perfectdreams.loritta.morenitta.utils.OutdatedCommandUtils
import net.perfectdreams.loritta.morenitta.LorittaBot

class McBodyCommand(loritta: LorittaBot) : AbstractCommand(loritta, "mcbody", listOf("mcstatue"), net.perfectdreams.loritta.common.commands.CommandCategory.MINECRAFT) {
	override fun getDescriptionKey() = LocaleKeyData("commands.command.mcbody.description")
	override fun getExamplesKey() = LocaleKeyData("commands.category.minecraft.skinPlayerNameExamples")

	// TODO: Fix Usage

	override fun needsToUploadFiles(): Boolean {
		return true
	}

	override suspend fun run(context: CommandContext,locale: BaseLocale) {
		OutdatedCommandUtils.sendOutdatedCommandMessage(context, locale, "minecraft player body")

		if (context.args.isNotEmpty()) {
			val nickname = context.args[0]

			val uuid = MCUtils.getUniqueId(nickname)

			if (uuid == null) {
				context.reply(
                        LorittaReply(
                                locale["commands.category.minecraft.unknownPlayer", context.args.getOrNull(0)],
                                Constants.ERROR
                        )
				)
				return
			}

			val bufferedImage = LorittaUtils.downloadImage(loritta, "https://crafatar.com/renders/body/$uuid?size=128&overlay")
			context.sendFile(bufferedImage!!, "avatar.png", context.getAsMention(true))
		} else {
			context.explain()
		}
	}

}