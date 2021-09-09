package net.perfectdreams.loritta.cinnamon.platform.commands.`fun`.declarations

import net.perfectdreams.loritta.cinnamon.platform.commands.`fun`.VieirinhaExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandCategory
import net.perfectdreams.loritta.cinnamon.platform.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData

object VieirinhaCommand : CommandDeclaration {
    val I18N_PREFIX = I18nKeysData.Commands.Command.Vieirinha
    val PUNCTUATIONS = listOf(
        "exclamation",
        "dot"
    )

    override fun declaration() = command(listOf("vieirinha", "8ball", "magicball", "magic8ball"), CommandCategory.FUN, I18N_PREFIX.Description) {
        executor = VieirinhaExecutor
    }
}