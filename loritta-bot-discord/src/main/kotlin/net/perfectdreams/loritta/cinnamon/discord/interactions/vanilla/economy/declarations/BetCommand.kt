package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.economy.declarations

import net.perfectdreams.loritta.common.locale.LanguageManager
import net.perfectdreams.loritta.common.utils.TodoFixThisData
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.CinnamonSlashCommandDeclarationWrapper
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.economy.bet.coinflipglobal.CoinFlipBetGlobalExecutor

class BetCommand(languageManager: LanguageManager) : CinnamonSlashCommandDeclarationWrapper(languageManager) {
    companion object {
        val I18N_PREFIX = I18nKeysData.Commands.Command.Bet
        val COINFLIP_GLOBAL_I18N_PREFIX = I18nKeysData.Commands.Command.Betcoinflipglobal
    }

    override fun declaration() = slashCommand(I18N_PREFIX.Label, CommandCategory.ECONOMY, TodoFixThisData) {
        subcommandGroup(I18nKeysData.Commands.Command.Coinflip.Label, TodoFixThisData) {
            /* subcommand(COINFLIP_FRIEND_I18N_PREFIX.Label, COINFLIP_GLOBAL_I18N_PREFIX.Description) {
                executor = { CoinFlipBetFriendExecutor(it) }
            } */

            subcommand(COINFLIP_GLOBAL_I18N_PREFIX.Label, COINFLIP_GLOBAL_I18N_PREFIX.Description) {
                executor = { CoinFlipBetGlobalExecutor(it) }
            }
        }

        subcommand(COINFLIP_GLOBAL_I18N_PREFIX.DiscordOldDiscordAppWorkaroundLabel, COINFLIP_GLOBAL_I18N_PREFIX.Description) {
            executor = { CoinFlipBetGlobalExecutor(it) }
        }
    }
}