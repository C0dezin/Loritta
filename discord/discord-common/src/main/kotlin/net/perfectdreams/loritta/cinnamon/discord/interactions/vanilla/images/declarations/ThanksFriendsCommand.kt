package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.images.declarations

import net.perfectdreams.gabrielaimageserver.client.GabrielaImageServerClient
import net.perfectdreams.loritta.cinnamon.locale.LanguageManager
import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.discord.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.CommandCategory
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.CinnamonSlashCommandDeclarationWrapper
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.images.EveryGroupHasExecutor
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.images.SadRealityExecutor
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.images.ThanksFriendsExecutor
import net.perfectdreams.loritta.cinnamon.utils.TodoFixThisData

class ThanksFriendsCommand(languageManager: LanguageManager) : CinnamonSlashCommandDeclarationWrapper(languageManager) {
    companion object {
        val I18N_PREFIX = I18nKeysData.Commands.Command.Thanksfriends
    }

    override fun declaration() = slashCommand(I18N_PREFIX.Label, CommandCategory.IMAGES, TodoFixThisData) {
        executor = { ThanksFriendsExecutor(it) }
    }
}