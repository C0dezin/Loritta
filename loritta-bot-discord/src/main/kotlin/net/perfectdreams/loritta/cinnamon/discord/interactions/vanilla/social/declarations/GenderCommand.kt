package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.social.declarations

import net.perfectdreams.loritta.common.locale.LanguageManager
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.CinnamonSlashCommandDeclarationWrapper

import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.social.GenderExecutor

class GenderCommand(languageManager: LanguageManager) : CinnamonSlashCommandDeclarationWrapper(languageManager) {
    companion object {
        val I18N_PREFIX = I18nKeysData.Commands.Command.Gender
    }

    override fun declaration() = slashCommand(I18N_PREFIX.Label, CommandCategory.SOCIAL, I18N_PREFIX.Description) {
        executor = { GenderExecutor(it) }
    }
}