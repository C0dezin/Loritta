package net.perfectdreams.loritta.morenitta.interactions.vanilla.economy

import dev.minn.jda.ktx.messages.MessageEdit
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.discord.utils.SonhosUtils
import net.perfectdreams.loritta.cinnamon.discord.utils.toLong
import net.perfectdreams.loritta.cinnamon.pudding.tables.CoinFlipBetMatchmakingResults
import net.perfectdreams.loritta.cinnamon.pudding.tables.SonhosTransactionsLog
import net.perfectdreams.loritta.cinnamon.pudding.tables.transactions.CoinFlipBetSonhosTransactionsLog
import net.perfectdreams.loritta.cinnamon.pudding.utils.SimpleSonhosTransactionsLogUtils
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.utils.Emotes
import net.perfectdreams.loritta.common.utils.GACampaigns
import net.perfectdreams.loritta.common.utils.TransactionType
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.interactions.UnleashedContext
import net.perfectdreams.loritta.morenitta.interactions.commands.*
import net.perfectdreams.loritta.morenitta.interactions.commands.options.ApplicationCommandOptions
import net.perfectdreams.loritta.morenitta.interactions.commands.options.OptionReference
import net.perfectdreams.loritta.morenitta.utils.AccountUtils
import net.perfectdreams.loritta.morenitta.utils.Constants
import net.perfectdreams.loritta.morenitta.utils.NumberUtils
import net.perfectdreams.loritta.morenitta.utils.PaymentUtils
import net.perfectdreams.loritta.morenitta.utils.extensions.refreshInDeferredTransaction
import net.perfectdreams.loritta.serializable.SonhosPaymentReason
import net.perfectdreams.loritta.serializable.StoredCoinFlipBetTransaction
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.insertAndGetId
import java.time.Instant

class CoinFlipBetCommand(val loritta: LorittaBot) : SlashCommandDeclarationWrapper {
    override fun command() = slashCommand(I18nKeysData.Commands.Command.Coinflipbet.Label, I18nKeysData.Commands.Command.Coinflipbet.Description, CommandCategory.ECONOMY) {
        enableLegacyMessageSupport = true
        isGuildOnly = true
        alternativeLegacyAbsoluteCommandPaths.apply {
            listOf("coinflip", "flipcoin", "girarmoeda", "caracoroa")
                .flatMap { listOf("$it bet", "$it apostar") }
                .forEach {
                    add(it)
                }
        }

        executor = CoinFlipBetExecutor()
    }

    inner class CoinFlipBetExecutor : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        inner class Options : ApplicationCommandOptions() {
            val user = user("user", I18nKeysData.Commands.Command.Coinflipbet.Options.User.Text)
            val sonhos = string("sonhos", I18nKeysData.Commands.Command.Coinflipbet.Options.Sonhos.Text)
        }

        override val options = Options()

        val mutex = Mutex()

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            context.deferChannelMessage(false)

            if (SonhosUtils.checkIfEconomyIsDisabled(context))
                return

            val invitedUser = args[options.user].user

            if (invitedUser == context.user) {
                context.reply(false) {
                    styled(
                        context.locale["commands.command.flipcoinbet.cantBetSelf"],
                        Constants.ERROR
                    )
                }
                return
            }

            val selfActiveDonations = loritta.getActiveMoneyFromDonations(context.user.idLong)
            val otherActiveDonations = loritta.getActiveMoneyFromDonations(invitedUser.idLong)

            val selfPlan = UserPremiumPlans.getPlanFromValue(selfActiveDonations)
            val otherPlan = UserPremiumPlans.getPlanFromValue(otherActiveDonations)

            var tax: Long? = null
            val totalRewardPercentage: Double?
            val money: Long
            val taxResult: CoinFlipTaxResult

            val number = NumberUtils.convertShortenedNumberToLong(args[options.sonhos])
                ?: context.fail(
                    false,
                    context.i18nContext.get(
                        I18nKeysData.Commands.InvalidNumber(args[options.sonhos])
                    )
                )

            if (selfPlan.totalCoinFlipReward == 1.0) {
                taxResult = CoinFlipTaxResult.PremiumUser(
                    context.user,
                    1.0
                )
            } else if (otherPlan.totalCoinFlipReward == 1.0) {
                taxResult = CoinFlipTaxResult.PremiumUser(
                    invitedUser,
                    1.0
                )
            } else {
                val specialTotalRewardChange = SonhosUtils.getSpecialTotalCoinFlipReward(context.guild, UserPremiumPlans.Free.totalCoinFlipReward)

                taxResult = when (specialTotalRewardChange) {
                    is SonhosUtils.SpecialTotalCoinFlipReward.LorittaCommunity -> {
                        CoinFlipTaxResult.LorittaCommunity(
                            specialTotalRewardChange.isWeekend,
                            specialTotalRewardChange.value
                        )
                    }
                    is SonhosUtils.SpecialTotalCoinFlipReward.NoChange -> {
                        CoinFlipTaxResult.Default(UserPremiumPlans.Free.totalCoinFlipReward)
                    }
                }
            }

            val hasNoTax = taxResult.totalRewardPercentage == 1.0
            totalRewardPercentage = taxResult.totalRewardPercentage

            if (hasNoTax) {
                money = number
                tax = null
            } else {
                val taxPercentage = (1.0.toBigDecimal() - totalRewardPercentage.toBigDecimal()).toDouble() // Avoid rounding errors
                tax = (number * taxPercentage).toLong()
                money = number - tax
            }

            if (!hasNoTax && tax == 0L) {
                context.reply(false) {
                    styled(
                        context.locale["commands.command.flipcoinbet.youNeedToBetMore"],
                        Constants.ERROR
                    )
                }
                return
            }

            if (0 >= number) {
                context.reply(false) {
                    styled(
                        context.locale["commands.command.flipcoinbet.zeroMoney"],
                        Constants.ERROR
                    )
                }
                return
            }

            val selfUserProfile = context.lorittaUser.profile

            if (number > selfUserProfile.money) {
                context.reply(false) {
                    styled(
                        context.locale["commands.command.flipcoinbet.notEnoughMoneySelf"],
                        Constants.ERROR
                    )

                    styled(
                        context.i18nContext.get(
                            GACampaigns.sonhosBundlesUpsellDiscordMessage(
                                "https://loritta.website/", // Hardcoded, woo
                                "bet-coinflip-legacy",
                                "bet-not-enough-sonhos"
                            )
                        ),
                        Emotes.LORI_RICH.asMention
                    )
                }
                return
            }

            val invitedUserProfile = loritta.getOrCreateLorittaProfile(invitedUser.id)
            val bannedState = invitedUserProfile.getBannedState(loritta)

            if (number > invitedUserProfile.money || bannedState != null) {
                context.reply(false) {
                    styled(
                        context.locale["commands.command.flipcoinbet.notEnoughMoneyInvited", invitedUser.asMention],
                        Constants.ERROR
                    )
                }
                return
            }

            // Self user check
            run {
                val epochMillis = context.user.timeCreated.toEpochSecond() * 1000

                // Don't allow users to bet if they are recent accounts
                if (epochMillis + (Constants.ONE_WEEK_IN_MILLISECONDS * 2) > System.currentTimeMillis()) { // 14 dias
                    context.reply(false) {
                        styled(
                            context.locale["commands.command.pay.selfAccountIsTooNew", 14] + " ${Emotes.LORI_CRYING}",
                            Constants.ERROR
                        )
                    }
                    return
                }
            }

            // Invited user check
            run {
                val epochMillis = invitedUser.timeCreated.toEpochSecond() * 1000

                // Don't allow users to bet if they are recent accounts
                if (epochMillis + (Constants.ONE_WEEK_IN_MILLISECONDS * 2) > System.currentTimeMillis()) { // 14 dias
                    context.reply(false) {
                        styled(
                            context.locale["commands.command.pay.otherAccountIsTooNew", 14] + " ${Emotes.LORI_CRYING}",
                            Constants.ERROR
                        )
                    }
                    return
                }
            }

            // Only allow users to participate in a coin flip bet if the user got their daily reward today
            val todayDailyReward = AccountUtils.getUserTodayDailyReward(loritta, context.lorittaUser.profile)

            if (todayDailyReward == null) {
                context.reply(false) {
                    styled(
                        context.locale["commands.youNeedToGetDailyRewardBeforeDoingThisAction", context.config.commandPrefix],
                        Constants.ERROR
                    )
                }
                return
            }

            // oh my gahhhh
            val usersThatAcceptedTheBet = mutableSetOf<User>()
            var isFinished = false
            if (invitedUser.idLong == loritta.config.loritta.discord.applicationId.toLong())
                usersThatAcceptedTheBet.add(invitedUser)

            context.reply(false) {
                when (taxResult) {
                    is CoinFlipTaxResult.LorittaCommunity -> {
                        if (taxResult.isWeekend && hasNoTax) {
                            styled(
                                context.i18nContext
                                    .get(
                                        I18nKeysData.Commands.Command.Coinflipbet.StartBetNoTax(
                                            invitedUser.asMention,
                                            context.user.asMention,
                                            SonhosUtils.getSonhosEmojiOfQuantity(money),
                                            number,
                                            money,
                                        )
                                    ),
                                Emotes.LORI_RICH,
                            )

                            styled(
                                context.i18nContext
                                    .get(
                                        I18nKeysData.Commands.Command.Coinflipbet.StartBetLorittaCommunityWeekend
                                    ),
                                net.perfectdreams.loritta.cinnamon.emotes.Emotes.LoriKiss
                            )
                        } else {
                            styled(
                                context.i18nContext
                                    .get(
                                        I18nKeysData.Commands.Command.Coinflipbet.StartBet(
                                            invitedUser.asMention,
                                            context.user.asMention,
                                            SonhosUtils.getSonhosEmojiOfQuantity(money),
                                            number,
                                            tax ?: 0L,
                                            money
                                        )
                                    ),
                                Emotes.LORI_RICH,
                            )

                            styled(
                                context.i18nContext
                                    .get(
                                        I18nKeysData.Commands.Command.Coinflipbet.StartBetLorittaCommunity(
                                            1.0 - UserPremiumPlans.Free.totalCoinFlipReward,
                                            1.0 - taxResult.totalRewardPercentage
                                        )
                                    ),
                                net.perfectdreams.loritta.cinnamon.emotes.Emotes.LoriKiss
                            )
                        }
                    }
                    is CoinFlipTaxResult.PremiumUser -> {
                        styled(
                            context.i18nContext
                                .get(
                                    I18nKeysData.Commands.Command.Coinflipbet.StartBetNoTax(
                                        invitedUser.asMention,
                                        context.user.asMention,
                                        SonhosUtils.getSonhosEmojiOfQuantity(money),
                                        number,
                                        money,
                                    )
                                ),
                            Emotes.LORI_RICH
                        )

                        styled(
                            context.i18nContext
                                .get(
                                    I18nKeysData.Commands.Command.Coinflipbet.StartBetPremiumUser(taxResult.premiumUser.asMention)
                                )
                        )
                    }
                    is CoinFlipTaxResult.Default -> {
                        styled(
                            context.i18nContext
                                .get(
                                    I18nKeysData.Commands.Command.Coinflipbet.StartBet(
                                        invitedUser.asMention,
                                        context.user.asMention,
                                        SonhosUtils.getSonhosEmojiOfQuantity(money),
                                        number,
                                        tax ?: 0L,
                                        money
                                    )
                                ),
                            Emotes.LORI_RICH,
                        )
                    }
                }

                styled(
                    context.locale[
                        "commands.command.flipcoinbet.clickToAcceptTheBet",
                        invitedUser.asMention,
                        "✅"
                    ],
                    "🤝"
                )

                actionRow(
                    loritta.interactivityManager.button(
                        ButtonStyle.PRIMARY,
                        context.i18nContext.get(I18nKeysData.Commands.Command.Coinflipbet.Participate(usersThatAcceptedTheBet.size)),
                        {
                            emoji = Emoji.fromUnicode("✅")
                        }
                    ) { componentContext ->
                        if (componentContext.user != invitedUser && componentContext.user != context.user) {
                            componentContext.reply(true) {
                                styled(componentContext.i18nContext.get(I18nKeysData.Commands.Command.Coinflipbet.YouAreNotParticipatingOnThisBet))
                            }
                            return@button
                        }

                        mutex.withLock {
                            // This should NEVER happen since the button AND the component are invalidated!
                            if (isFinished)
                                return@button

                            if (componentContext.user !in usersThatAcceptedTheBet) {
                                usersThatAcceptedTheBet.add(componentContext.user)

                                if (invitedUser in usersThatAcceptedTheBet && context.user in usersThatAcceptedTheBet) {
                                    isFinished = true
                                    componentContext.invalidateComponentCallback()

                                    listOf(
                                        selfUserProfile.refreshInDeferredTransaction(loritta),
                                        invitedUserProfile.refreshInDeferredTransaction(loritta)
                                    ).awaitAll()

                                    if (number > selfUserProfile.money)
                                        return@withLock

                                    if (number > invitedUserProfile.money)
                                        return@withLock

                                    val isTails = LorittaBot.RANDOM.nextBoolean()
                                    val prefix: String
                                    val message: String

                                    if (isTails) {
                                        prefix = "<:coroa:412586257114464259>"
                                        message = context.locale["commands.command.flipcoin.tails"]
                                    } else {
                                        prefix = "<:cara:412586256409559041>"
                                        message = context.locale["commands.command.flipcoin.heads"]
                                    }

                                    val winner: User
                                    val loser: User
                                    val now = Instant.now()

                                    if (isTails) {
                                        winner = context.user
                                        loser = invitedUser
                                        loritta.newSuspendedTransaction {
                                            selfUserProfile.addSonhosNested(money)
                                            invitedUserProfile.takeSonhosNested(number)

                                            PaymentUtils.addToTransactionLogNested(
                                                number,
                                                SonhosPaymentReason.COIN_FLIP_BET,
                                                givenBy = invitedUserProfile.id.value,
                                                receivedBy = selfUserProfile.id.value
                                            )

                                            // Cinnamon transaction system
                                            val mmResult = CoinFlipBetMatchmakingResults.insertAndGetId {
                                                it[CoinFlipBetMatchmakingResults.timestamp] = now
                                                it[CoinFlipBetMatchmakingResults.winner] = selfUserProfile.id.value
                                                it[CoinFlipBetMatchmakingResults.loser] = invitedUserProfile.id.value
                                                it[CoinFlipBetMatchmakingResults.quantity] = number
                                                it[CoinFlipBetMatchmakingResults.quantityAfterTax] = money
                                                it[CoinFlipBetMatchmakingResults.tax] = tax
                                                it[CoinFlipBetMatchmakingResults.taxPercentage] = totalRewardPercentage
                                            }

                                            // Cinnamon transaction log
                                            SimpleSonhosTransactionsLogUtils.insert(
                                                selfUserProfile.id.value,
                                                now,
                                                TransactionType.COINFLIP_BET,
                                                money,
                                                StoredCoinFlipBetTransaction(mmResult.value)
                                            )

                                            SimpleSonhosTransactionsLogUtils.insert(
                                                invitedUserProfile.id.value,
                                                now,
                                                TransactionType.COINFLIP_BET,
                                                number,
                                                StoredCoinFlipBetTransaction(mmResult.value)
                                            )
                                        }
                                    } else {
                                        winner = invitedUser
                                        loser = context.user
                                        loritta.newSuspendedTransaction {
                                            invitedUserProfile.addSonhosNested(money)
                                            selfUserProfile.takeSonhosNested(number)

                                            PaymentUtils.addToTransactionLogNested(
                                                number,
                                                SonhosPaymentReason.COIN_FLIP_BET,
                                                givenBy = selfUserProfile.id.value,
                                                receivedBy = invitedUserProfile.id.value
                                            )

                                            val mmResult = CoinFlipBetMatchmakingResults.insertAndGetId {
                                                it[CoinFlipBetMatchmakingResults.timestamp] = Instant.now()
                                                it[CoinFlipBetMatchmakingResults.winner] = invitedUserProfile.id.value
                                                it[CoinFlipBetMatchmakingResults.loser] = selfUserProfile.id.value
                                                it[CoinFlipBetMatchmakingResults.quantity] = number
                                                it[CoinFlipBetMatchmakingResults.quantityAfterTax] = money
                                                it[CoinFlipBetMatchmakingResults.tax] = tax
                                                it[CoinFlipBetMatchmakingResults.taxPercentage] = totalRewardPercentage
                                            }

                                            // Cinnamon transaction log
                                            SimpleSonhosTransactionsLogUtils.insert(
                                                invitedUserProfile.id.value,
                                                now,
                                                TransactionType.COINFLIP_BET,
                                                money,
                                                StoredCoinFlipBetTransaction(mmResult.value)
                                            )

                                            SimpleSonhosTransactionsLogUtils.insert(
                                                selfUserProfile.id.value,
                                                now,
                                                TransactionType.COINFLIP_BET,
                                                number,
                                                StoredCoinFlipBetTransaction(mmResult.value)
                                            )
                                        }
                                    }

                                    componentContext.deferAndEditOriginal {
                                        MessageEdit {
                                            actionRow(
                                                Button.of(
                                                    ButtonStyle.PRIMARY,
                                                    componentContext.event.componentId, // Reuse the same component
                                                    context.i18nContext.get(I18nKeysData.Commands.Command.Coinflipbet.Participate(usersThatAcceptedTheBet.size)),
                                                ).withEmoji(Emoji.fromUnicode("✅")).withDisabled(true)
                                            )
                                        }
                                    }

                                    context.reply(false) {
                                        styled(
                                            "**$message!**",
                                            prefix
                                        )

                                        styled(
                                            context.locale["commands.command.flipcoinbet.congratulations", winner.asMention, money, loser.asMention],
                                            Emotes.LORI_RICH
                                        )
                                    }
                                } else {
                                    componentContext.deferAndEditOriginal {
                                        MessageEdit {
                                            actionRow(
                                                Button.of(
                                                    ButtonStyle.PRIMARY,
                                                    componentContext.event.componentId, // Reuse the same component
                                                    context.i18nContext.get(I18nKeysData.Commands.Command.Coinflipbet.Participate(usersThatAcceptedTheBet.size)),
                                                ).withEmoji(Emoji.fromUnicode("✅"))
                                            )
                                        }
                                    }
                                }
                            } else {
                                usersThatAcceptedTheBet.remove(componentContext.user)

                                componentContext.deferAndEditOriginal {
                                    MessageEdit {
                                        actionRow(
                                            Button.of(
                                                ButtonStyle.PRIMARY,
                                                componentContext.event.componentId,
                                                context.i18nContext.get(I18nKeysData.Commands.Command.Coinflipbet.Participate(usersThatAcceptedTheBet.size)),
                                            ).withEmoji(Emoji.fromUnicode("✅"))
                                        )
                                    }
                                }
                            }
                        }
                    }
                )
            }.retrieveOriginal()
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?>? {
            val userAndMember = context.getUserAndMember(0)
            if (userAndMember == null) {
                context.explain()
                return null
            }

            return mapOf(
                options.user to userAndMember,
                options.sonhos to args[1]
            )
        }
    }

    sealed class CoinFlipTaxResult(val totalRewardPercentage: Double) {
        class LorittaCommunity(val isWeekend: Boolean, totalRewardPercentage: Double) : CoinFlipTaxResult(totalRewardPercentage)
        class PremiumUser(val premiumUser: User, totalRewardPercentage: Double) : CoinFlipTaxResult(totalRewardPercentage)
        class Default(totalRewardPercentage: Double) : CoinFlipTaxResult(totalRewardPercentage)
    }
}