package net.perfectdreams.loritta.cinnamon.discord.interactions

import mu.KotlinLogging
import net.perfectdreams.discordinteraktions.common.DiscordInteraKTions
import net.perfectdreams.discordinteraktions.common.components.ButtonExecutor
import net.perfectdreams.discordinteraktions.common.components.SelectMenuExecutor
import net.perfectdreams.discordinteraktions.common.modals.ModalExecutor
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.CinnamonMessageCommandDeclarationWrapper
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.CinnamonSlashCommandDeclarationWrapper
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.CinnamonUserCommandDeclarationWrapper
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.economy.bet.coinflipglobal.StartCoinFlipGlobalBetMatchmakingButtonClickExecutor
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.economy.broker.BrokerInfoCategorySelectMenuExecutor
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.economy.pay.CancelSonhosTransferButtonExecutor
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.economy.pay.TransferSonhosButtonExecutor
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.economy.sonhosrank.ChangeSonhosRankPageButtonExecutor
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.economy.transactions.ChangeTransactionFilterSelectMenuExecutor
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.economy.transactions.ChangeTransactionPageButtonClickExecutor
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.`fun`.soundbox.PlayAudioClipButtonExecutor
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.moderation.ban.ConfirmBanButtonExecutor
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.social.AchievementsExecutor
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.social.xprank.ChangeXpRankPageButtonExecutor
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.undertale.textbox.*
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.utils.declarations.OCRMessageCommand
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.utils.ocr.OCRTranslateButtonExecutor
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.utils.packtracker.*

class InteractionsManager(
    private val loritta: LorittaBot,
    val interaKTions: DiscordInteraKTions
) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    private val discordConfig = loritta.config.loritta.discord

    val languageManager = loritta.languageManager

    fun register() {
        val publicLorittaCommands = PublicLorittaCommands(languageManager)
        publicLorittaCommands.commands().forEach {
            register(it)
        }

        // ===[ MODERATION ]===
        register(ConfirmBanButtonExecutor(loritta))

        // ===[ FUN ]===
        register(PlayAudioClipButtonExecutor(loritta))

        /* commandManager.register(
            BomDiaECiaCommand,
            BomDiaECiaExecutor()
        ) */

        // ===[ IMAGES ]===

        // ===[ VIDEOS ]===

        // ===[ UTILS ]===
        register(FollowPackageButtonClickExecutor(loritta, loritta.correiosClient))
        register(UnfollowPackageButtonClickExecutor(loritta, loritta.correiosClient))
        register(SelectPackageSelectMenuExecutor(loritta))

        register(GoBackToPackageListButtonClickExecutor(loritta, loritta.correiosClient))
        register(TrackPackageButtonClickExecutor(loritta, loritta.correiosClient))

        register(OCRMessageCommand(languageManager))
        register(OCRTranslateButtonExecutor(loritta))

        // ===[ ECONOMY ]===
        register(ChangeTransactionPageButtonClickExecutor(loritta))
        register(ChangeTransactionFilterSelectMenuExecutor(loritta))

        register(StartCoinFlipGlobalBetMatchmakingButtonClickExecutor(loritta))

        register(TransferSonhosButtonExecutor(loritta))
        register(CancelSonhosTransferButtonExecutor(loritta))

        register(ChangeSonhosRankPageButtonExecutor(loritta))

        register(BrokerInfoCategorySelectMenuExecutor(loritta))

        // ===[ SOCIAL ]===
        register(AchievementsExecutor.ChangeCategoryMenuExecutor(loritta))
        register(ChangeXpRankPageButtonExecutor(loritta))

        // ===[ UNDERTALE ]===
        register(PortraitSelectMenuExecutor(loritta, loritta.gabrielaImageServerClient))
        register(ChangeUniverseSelectMenuExecutor(loritta, loritta.gabrielaImageServerClient))
        register(ChangeCharacterSelectMenuExecutor(loritta, loritta.gabrielaImageServerClient))

        register(ChangeDialogBoxTypeButtonClickExecutor(loritta, loritta.gabrielaImageServerClient))
        register(ConfirmDialogBoxButtonClickExecutor(loritta, loritta.gabrielaImageServerClient))
        register(ChangeColorPortraitTypeButtonClickExecutor(loritta, loritta.gabrielaImageServerClient))

        // ===[ ROBLOX ]===
    }

    private fun register(declarationWrapper: CinnamonSlashCommandDeclarationWrapper) {
        interaKTions.manager.register(declarationWrapper.declaration().build(loritta))
    }

    private fun register(declarationWrapper: CinnamonUserCommandDeclarationWrapper) {
        interaKTions.manager.register(declarationWrapper.declaration().build(loritta))
    }

    private fun register(declarationWrapper: CinnamonMessageCommandDeclarationWrapper) {
        interaKTions.manager.register(declarationWrapper.declaration().build(loritta))
    }

    private fun register(executor: ButtonExecutor) {
        interaKTions.manager.register(executor)
    }

    private fun register(executor: SelectMenuExecutor) {
        interaKTions.manager.register(executor)
    }

    private fun register(executor: ModalExecutor) {
        interaKTions.manager.register(executor)
    }
}