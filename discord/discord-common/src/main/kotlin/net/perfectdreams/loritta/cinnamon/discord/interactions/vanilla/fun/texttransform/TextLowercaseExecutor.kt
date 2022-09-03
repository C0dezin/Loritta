package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.`fun`.texttransform

import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.CinnamonSlashCommandExecutor
import net.perfectdreams.loritta.cinnamon.discord.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.`fun`.declarations.TextTransformCommand
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.options.LocalizedApplicationCommandOptions
import net.perfectdreams.discordinteraktions.common.commands.options.SlashCommandArguments

class TextLowercaseExecutor(loritta: LorittaCinnamon) : TextExecutor(loritta) {
    inner class Options : LocalizedApplicationCommandOptions(loritta) {
        val text = string("text", TextTransformCommand.I18N_PREFIX.Lowercase.Description)
    }

    override val options = Options()

    override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
        val text = args[options.text]

        sendPublicOrEphemeralReplyIfTheMessageHasInvite(
            context,
            content = text.lowercase(),
            prefix = "✍"
        )
    }
}