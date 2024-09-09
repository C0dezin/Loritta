package net.perfectdreams.loritta.morenitta.interactions.vanilla.economy

import dev.kord.common.entity.Snowflake
import dev.minn.jda.ktx.messages.InlineMessage
import kotlinx.datetime.Clock
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.`fun`.declarations.CoinFlipCommand
import net.perfectdreams.loritta.cinnamon.discord.utils.NumberUtils
import net.perfectdreams.loritta.cinnamon.discord.utils.SonhosUtils.appendUserHaventGotDailyTodayOrUpsellSonhosBundles
import net.perfectdreams.loritta.cinnamon.discord.utils.UserId
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.pudding.services.BetsService
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.utils.GACampaigns
import net.perfectdreams.loritta.common.utils.TodoFixThisData
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.interactions.BarebonesInteractionContext
import net.perfectdreams.loritta.morenitta.interactions.UnleashedContext
import net.perfectdreams.loritta.morenitta.interactions.commands.*
import net.perfectdreams.loritta.morenitta.interactions.commands.options.ApplicationCommandOptions
import net.perfectdreams.loritta.serializable.CachedUserInfo
import net.perfectdreams.loritta.serializable.UserId
import java.util.*
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

class BetCommand : SlashCommandDeclarationWrapper {
    companion object {
        val I18N_PREFIX = I18nKeysData.Commands.Command.Bet
        val COINFLIP_GLOBAL_I18N_PREFIX = I18nKeysData.Commands.Command.Betcoinflipglobal

        val QUANTITIES = listOf<Long>(
            0,
            100,
            1_000,
            2_500,
            5_000,
            10_000,
            25_000,
            50_000,
            100_000,
            250_000,
            500_000,
            1_000_000
        )

        suspend fun addToMatchmakingQueue(
            context: UnleashedContext,
            quantity: Long
        ) {
            if (quantity !in QUANTITIES)
                context.fail(true) {
                    styled(
                        context.i18nContext.get(
                            COINFLIP_GLOBAL_I18N_PREFIX.InvalidQuantity(
                                QUANTITIES.joinToString(", ")
                            )
                        ),
                        Emotes.Error
                    )
                }

            val results = context.loritta.pudding.bets.addToCoinFlipBetGlobalMatchmakingQueue(
                UserId(Snowflake(context.user.idLong)),
                context.discordInteraction.token,
                context.loritta.languageManager.getIdByI18nContext(context.i18nContext),
                quantity
            )

            for (result in results) {
                when (result) {
                    is BetsService.AddedToQueueResult -> context.reply(true) {
                        styled(
                            context.i18nContext.get(
                                COINFLIP_GLOBAL_I18N_PREFIX.AddedToMatchmakingQueue(context.loritta.commandMentions.betCoinflipGlobal)
                            ),
                            Emotes.LoriRage
                        )
                    }

                    is BetsService.AlreadyInQueueResult -> context.reply(true) {
                        styled(
                            context.i18nContext.get(
                                COINFLIP_GLOBAL_I18N_PREFIX.YouAreAlreadyInTheMatchmakingQueue
                            ),
                            Emotes.LoriRage
                        )
                    }

                    is BetsService.CoinFlipResult -> {
                        val winnerCachedUserInfo = context.loritta.getCachedUserInfo(result.winner)
                        val loserCachedUserInfo = context.loritta.getCachedUserInfo(result.loser)
                        val now24HoursAgo = Clock.System.now()
                            .minus(24.hours)
                        val winnerBetStats = context.loritta.pudding.bets.getCoinFlipBetGlobalUserBetsStats(
                            result.winner,
                            now24HoursAgo
                        )
                        val loserBetStats = context.loritta.pudding.bets.getCoinFlipBetGlobalUserBetsStats(
                            result.loser,
                            now24HoursAgo
                        )

                        val isSelfUserTheWinner = result.winner == UserId(Snowflake(context.user.idLong))

                        context.reply(true) {
                            apply(
                                createCoinFlipResultMessage(
                                    context.loritta,
                                    context.i18nContext,
                                    UserId(Snowflake(context.user.idLong)),
                                    result,
                                    quantity,
                                    winnerCachedUserInfo,
                                    loserCachedUserInfo,
                                    if (isSelfUserTheWinner)
                                        winnerBetStats
                                    else
                                        loserBetStats,
                                    if(isSelfUserTheWinner)
                                        result.winnerStreakCount
                                    else
                                        result.loserStreakCount
                                )
                            )
                        }

                        val otherUserMessage = createCoinFlipResultMessage(
                            context.loritta,
                            context.loritta.languageManager.getI18nContextById(result.otherUserLanguage),
                            result.otherUser,
                            result,
                            quantity,
                            winnerCachedUserInfo,
                            loserCachedUserInfo,
                            if (!isSelfUserTheWinner)
                                winnerBetStats
                            else
                                loserBetStats,
                            if (!isSelfUserTheWinner)
                                result.winnerStreakCount
                            else
                                result.loserStreakCount
                        )

                        val otherUserContext = BarebonesInteractionContext(
                            context.jda,
                            result.userInteractionToken
                        )

                        otherUserContext.reply(true, otherUserMessage)
                    }

                    is BetsService.AnotherUserRemovedFromMatchmakingQueueResult -> {
                        val otherUserContext = BarebonesInteractionContext(
                            context.jda,
                            result.userInteractionToken
                        )

                        val otherUserI18nContext = context.loritta.languageManager.getI18nContextById(result.language)

                        otherUserContext.reply(true) {
                            styled(
                                "<@${result.user.value}> ${
                                    otherUserI18nContext.get(
                                        COINFLIP_GLOBAL_I18N_PREFIX.LeftMatchmakingQueueDueToNotEnoughSonhos
                                    )
                                }",
                                Emotes.LoriSob
                            )

                            appendUserHaventGotDailyTodayOrUpsellSonhosBundles(
                                context.loritta,
                                context.i18nContext,
                                UserId(Snowflake(context.user.idLong)),
                                "bet-coinflip-global",
                                "removed-from-mm"
                            )
                        }
                    }

                    is BetsService.YouDontHaveEnoughSonhosToBetResult -> {
                        context.reply(true) {
                            styled(
                                "${context.user.asMention} ${
                                    context.i18nContext.get(
                                        COINFLIP_GLOBAL_I18N_PREFIX.NotEnoughSonhosToBet
                                    )
                                }",
                                Emotes.LoriSob
                            )

                            appendUserHaventGotDailyTodayOrUpsellSonhosBundles(
                                context.loritta,
                                context.i18nContext,
                                UserId(Snowflake(context.user.idLong)),
                                "bet-coinflip-global",
                                "mm-check"
                            )
                        }
                    }

                    is BetsService.OtherUserAchievementResult -> {
                        val otherUserContext = BarebonesInteractionContext(
                            context.jda,
                            result.userInteractionToken
                        )

                        val otherUserI18nContext = context.loritta.languageManager.getI18nContextById(result.language)

                        otherUserContext.giveAchievementToUser(
                            context.loritta,
                            result.user,
                            otherUserI18nContext,
                            result.achievementType
                        )
                    }

                    is BetsService.SelfUserAchievementResult -> {
                        context.giveAchievementAndNotify(result.achievementType, true)
                    }
                }
            }
        }

        fun createCoinFlipResultMessage(
            loritta: LorittaBot,
            i18nContext: I18nContext,
            selfUser: UserId,
            result: BetsService.CoinFlipResult,
            quantity: Long,
            winnerCachedUserInfo: CachedUserInfo?,
            loserCachedUserInfo: CachedUserInfo?,
            selfStats: BetsService.UserCoinFlipBetGlobalStats,
            selfStreak: Int
        ): InlineMessage<*>.() -> (Unit) = {
            val isSelfUserTheWinner = result.winner == selfUser
            val isJustForFun = quantity == 0L

            styled(
                if (result.isTails)
                    "**${i18nContext.get(CoinFlipCommand.I18N_PREFIX.Tails)}!**"
                else
                    "**${i18nContext.get(CoinFlipCommand.I18N_PREFIX.Heads)}!**",
                if (result.isTails)
                    Emotes.CoinTails
                else
                    Emotes.CoinHeads
            )

            if (isJustForFun) {
                if (isSelfUserTheWinner) {
                    styled(
                        i18nContext.get(
                            COINFLIP_GLOBAL_I18N_PREFIX.CongratulationsJustForFun(
                                user = "<@${selfUser.value}>",
                                loserTag = "${loserCachedUserInfo?.name}#${loserCachedUserInfo?.discriminator}",
                                loserId = loserCachedUserInfo?.id?.value.toString()
                            )
                        ),
                        Emotes.LoriRich
                    )
                } else {
                    styled(
                        i18nContext.get(
                            BetCommand.COINFLIP_GLOBAL_I18N_PREFIX.LostJustForFun(
                                user = "<@${selfUser.value}>",
                                winnerTag = "${winnerCachedUserInfo?.name}#${winnerCachedUserInfo?.discriminator}",
                                winnerId = winnerCachedUserInfo?.id?.value.toString()
                            )
                        ),
                        Emotes.LoriSob
                    )
                }
            } else if (result.tax != null && result.taxPercentage != null) {
                // Taxed
                if (isSelfUserTheWinner) {
                    styled(
                        i18nContext.get(
                            BetCommand.COINFLIP_GLOBAL_I18N_PREFIX.CongratulationsTaxed(
                                user = "<@${selfUser.value}>",
                                sonhosCount = result.quantityAfterTax,
                                sonhosCountWithoutTax = result.quantity,
                                loserTag = "${loserCachedUserInfo?.name}#${loserCachedUserInfo?.discriminator}",
                                loserId = loserCachedUserInfo?.id?.value.toString()
                            )
                        ),
                        Emotes.LoriRich
                    )
                } else {
                    styled(
                        i18nContext.get(
                            COINFLIP_GLOBAL_I18N_PREFIX.LostTaxed(
                                user = "<@${selfUser.value}>",
                                sonhosCount = result.quantityAfterTax,
                                sonhosCountWithoutTax = result.quantity,
                                winnerTag = "${winnerCachedUserInfo?.name}#${winnerCachedUserInfo?.discriminator}",
                                winnerId = winnerCachedUserInfo?.id?.value.toString()
                            )
                        ),
                        Emotes.LoriSob
                    )
                }
            } else {
                if (isSelfUserTheWinner) {
                    styled(
                        i18nContext.get(
                            COINFLIP_GLOBAL_I18N_PREFIX.Congratulations(
                                user = "<@${selfUser.value}>",
                                sonhosCount = result.quantityAfterTax,
                                loserTag = "${loserCachedUserInfo?.name}#${loserCachedUserInfo?.discriminator}",
                                loserId = loserCachedUserInfo?.id?.value.toString()
                            )
                        ),
                        Emotes.LoriRich
                    )

                    // Upsell if the user does not have premium but the loser has
                    if (result.winner !in result.premiumUsers && result.loser in result.premiumUsers) {
                        styled(
                            i18nContext.get(
                                COINFLIP_GLOBAL_I18N_PREFIX.DontWantTaxAnymorePremiumPlanUpsellOtherUserHasPremium(
                                    GACampaigns.premiumUpsellDiscordMessageUrl(
                                        loritta.config.loritta.website.url,
                                        "bet-coinflip-global",
                                        "victory-against-premium-users"
                                    )
                                )
                            ),
                            Emotes.CreditCard
                        )
                    }
                } else {
                    styled(
                        i18nContext.get(
                            COINFLIP_GLOBAL_I18N_PREFIX.Lost(
                                user = "<@${selfUser.value}>",
                                sonhosCount = result.quantityAfterTax,
                                winnerTag = "${winnerCachedUserInfo?.name}#${winnerCachedUserInfo?.discriminator}",
                                winnerId = winnerCachedUserInfo?.id?.value.toString()
                            )
                        ),
                        Emotes.LoriSob
                    )
                }
            }

            styled(
                i18nContext.get(
                    BetCommand.COINFLIP_GLOBAL_I18N_PREFIX.RecentBetsStats(
                        selfStats.winCount + selfStats.lostCount,
                        selfStats.winCount,
                        selfStats.lostCount,
                        selfStats.winSum - selfStats.lostSum
                    )
                ),
                Emotes.LoriReading
            )

            // If the user won, then the selfStreak is their winning streak
            // (After all, if they won... the losing streak would be 0)
            // emojis = the three stages of happiness/grief idk i never watched it
            if (isSelfUserTheWinner) {
                styled(
                    i18nContext.get(BetCommand.COINFLIP_GLOBAL_I18N_PREFIX.YouHaveConsecutiveWins(selfStreak)),
                    when {
                        selfStreak >= 10 -> Emotes.LoriHappy
                        selfStreak >= 5 -> Emotes.LoriUwU
                        else -> Emotes.LoriWow
                    }
                )
            } else {
                styled(
                    i18nContext.get(BetCommand.COINFLIP_GLOBAL_I18N_PREFIX.YouHaveConsecutiveLosses(selfStreak)),
                    when {
                        selfStreak >= 10 -> Emotes.LoriSob
                        selfStreak >= 5 -> Emotes.LoriRage
                        else -> Emotes.LoriHmpf
                    }
                )
            }

            actionRow(
                loritta.interactivityManager.buttonForUser(
                    selfUser.value.toLong(),
                    ButtonStyle.PRIMARY,
                    if (isJustForFun)
                        i18nContext.get(
                            COINFLIP_GLOBAL_I18N_PREFIX.JoinMatchmakingQueueJustForFunButton
                        )
                    else
                        i18nContext.get(
                            COINFLIP_GLOBAL_I18N_PREFIX.JoinMatchmakingQueueButton(quantity)
                        ),
                    {
                        loriEmoji = Emotes.LoriRich
                    }
                ) {
                    it.deferEdit()

                    addToMatchmakingQueue(
                        it,
                        quantity
                    )
                }
            )
        }
    }

    override fun command() = slashCommand(I18N_PREFIX.Label, TodoFixThisData, CommandCategory.ECONOMY, UUID.fromString("0b70972d-31b1-3b6d-a379-8f0af60ece64")) {
        subcommandGroup(I18nKeysData.Commands.Command.Coinflip.Label, TodoFixThisData) {
            subcommand(COINFLIP_GLOBAL_I18N_PREFIX.Label, COINFLIP_GLOBAL_I18N_PREFIX.Description, UUID.fromString("23c45ce7-d802-36e3-ad47-056a2bd7ab46")) {
                executor = CoinFlipBetGlobalExecutor()
            }
        }

        subcommand(COINFLIP_GLOBAL_I18N_PREFIX.DiscordOldDiscordAppWorkaroundLabel, COINFLIP_GLOBAL_I18N_PREFIX.Description, UUID.fromString("4faff499-92b7-3304-b84c-495a12ca9be5")) {
            executor = CoinFlipBetGlobalExecutor()
        }
    }

    inner class CoinFlipBetGlobalExecutor : LorittaSlashCommandExecutor() {
        inner class Options : ApplicationCommandOptions() {
            val quantity = string("quantity", COINFLIP_GLOBAL_I18N_PREFIX.Options.Quantity.Text) {
                autocomplete { context ->
                    val currentInput = context.event.focusedOption.value

                    val trueNumber = NumberUtils.convertShortenedNumberToLong(
                        context.i18nContext,
                        currentInput
                    )

                    val trueNumberAsString = trueNumber.toString()
                    val matchedChoices = mutableSetOf<Long>()
                    val discordChoices = mutableMapOf<String, String>()

                    for (quantity in QUANTITIES) {
                        if (context.event.focusedOption.value.isEmpty() || quantity.toString().startsWith(trueNumberAsString)) {
                            matchedChoices.add(quantity)
                        }
                    }

                    val matchmakingStats = context.loritta.pudding.bets.getUserCoinFlipBetGlobalMatchmakingStats(
                        UserId(Snowflake(context.event.user.idLong)),
                        matchedChoices.toList(),
                        Clock.System.now().minus(5.minutes)
                    )

                    for (choice in matchedChoices) {
                        val mmStat = matchmakingStats[choice]

                        discordChoices[
                                buildString {
                                    if (mmStat == null) {
                                        if (choice == 0L) {
                                            append(
                                                context.i18nContext.get(
                                                    COINFLIP_GLOBAL_I18N_PREFIX.Options.Quantity.Choice.JustForFun
                                                )
                                            )
                                        } else {
                                            append(
                                                context.i18nContext.get(
                                                    COINFLIP_GLOBAL_I18N_PREFIX.Options.Quantity.Choice.MatchmakingSonhos(
                                                        choice
                                                    )
                                                )
                                            )
                                        }
                                    } else {
                                        if (mmStat.userPresentInMatchmakingQueue) {
                                            append(
                                                context.i18nContext.get(
                                                    COINFLIP_GLOBAL_I18N_PREFIX.Options.Quantity.Choice.QuitMatchmakingQueue(
                                                        choice
                                                    )
                                                )
                                            )
                                        } else {
                                            if (choice == 0L) {
                                                append(
                                                    context.i18nContext.get(
                                                        COINFLIP_GLOBAL_I18N_PREFIX.Options.Quantity.Choice.JustForFun
                                                    )
                                                )
                                            } else {
                                                append(
                                                    context.i18nContext.get(
                                                        COINFLIP_GLOBAL_I18N_PREFIX.Options.Quantity.Choice.MatchmakingSonhos(
                                                            choice
                                                        )
                                                    )
                                                )
                                            }

                                            val averageTimeOnQueue = mmStat.averageTimeOnQueue

                                            if (averageTimeOnQueue != null) {
                                                append(" (${
                                                    context.i18nContext.get(
                                                        COINFLIP_GLOBAL_I18N_PREFIX.Options.Quantity.Choice.AverageTimeInSeconds(
                                                            averageTimeOnQueue.toMillis().toDouble() / 1_000
                                                        )
                                                    )
                                                })")
                                            }
                                            append(" ")
                                            append("[")

                                            if (mmStat.playersPresentInMatchmakingQueue) {
                                                append(
                                                    context.i18nContext.get(COINFLIP_GLOBAL_I18N_PREFIX.Options.Quantity.Choice.PlayersInMatchmakingQueue)
                                                )
                                                append(" | ")
                                            }
                                            append(
                                                context.i18nContext.get(
                                                    COINFLIP_GLOBAL_I18N_PREFIX.Options.Quantity.Choice.RecentMatches(
                                                        mmStat.recentMatches
                                                    )
                                                )
                                            )
                                            append("]")
                                        }
                                    }
                                }
                        ] = if (mmStat?.userPresentInMatchmakingQueue == true)
                            "q$choice"
                        else
                            choice.toString()
                    }

                    return@autocomplete discordChoices
                }
            }
        }

        override val options = Options()

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            context.deferChannelMessage(true)

            val quantityAsString = args[options.quantity]
            val isRemoveFromQueueRequest = quantityAsString.startsWith("q")

            val quantity = NumberUtils.convertShortenedNumberToLong(
                context.i18nContext,
                quantityAsString
                    .removePrefix("q")
            ) ?: context.fail(true) {
                styled(
                    context.i18nContext.get(
                        COINFLIP_GLOBAL_I18N_PREFIX.InvalidQuantity(
                            QUANTITIES.joinToString(", ")
                        )
                    ),
                    Emotes.Error
                )
            }

            if (isRemoveFromQueueRequest) {
                val leftQueue = context.loritta.pudding.bets.removeFromCoinFlipBetGlobalMatchmakingQueue(
                    UserId(Snowflake(context.user.idLong)),
                    quantity
                )

                if (leftQueue) {
                    context.reply(true) {
                        styled(
                            context.i18nContext.get(
                                COINFLIP_GLOBAL_I18N_PREFIX.QuittedMatchmakingQueue
                            ),
                            Emotes.LoriSmile
                        )
                    }
                } else {
                    context.reply(true) {
                        styled(
                            context.i18nContext.get(
                                COINFLIP_GLOBAL_I18N_PREFIX.YouArentInTheMatchmakingQueueToLeaveIt
                            ),
                            Emotes.Error
                        )
                    }
                }
            } else {
                addToMatchmakingQueue(context, quantity)
            }
        }
    }
}