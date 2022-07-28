package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.economy.broker

import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.utils.LorittaBovespaBrokerUtils
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.CinnamonSlashCommandExecutor
import net.perfectdreams.loritta.cinnamon.discord.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.economy.broker.BrokerExecutorUtils.brokerBaseEmbed
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.economy.declarations.BrokerCommand
import net.perfectdreams.discordinteraktions.common.commands.options.SlashCommandArguments
import net.perfectdreams.loritta.cinnamon.pudding.data.BrokerTickerInformation

class BrokerInfoExecutor(loritta: LorittaCinnamon) : CinnamonSlashCommandExecutor(loritta) {
    override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
        context.deferChannelMessage() // Defer because this sometimes takes too long

        val stockInformations = context.loritta.services.bovespaBroker.getAllTickers()

        context.sendMessage {
            brokerBaseEmbed(context) {
                title = "${Emotes.LoriStonks} ${context.i18nContext.get(BrokerCommand.I18N_PREFIX.Info.Embed.Title)}"
                description = context.i18nContext.get(
                    BrokerCommand.I18N_PREFIX.Info.Embed.Explanation(
                        loriSob = Emotes.LoriSob,
                        tickerOutOfMarket = Emotes.DoNotDisturb,
                        openTime = LorittaBovespaBrokerUtils.TIME_OPEN_DISCORD_TIMESTAMP,
                        closingTime = LorittaBovespaBrokerUtils.TIME_CLOSING_DISCORD_TIMESTAMP
                    )
                ).joinToString("\n")

                for (stockInformation in stockInformations.sortedBy(BrokerTickerInformation::ticker)) {
                    val tickerId = stockInformation.ticker
                    val tickerName = LorittaBovespaBrokerUtils.trackedTickerCodes[tickerId]
                    val currentPrice = LorittaBovespaBrokerUtils.convertReaisToSonhos(stockInformation.value)
                    val buyingPrice = LorittaBovespaBrokerUtils.convertToBuyingPrice(currentPrice) // Buying price
                    val sellingPrice = LorittaBovespaBrokerUtils.convertToSellingPrice(currentPrice) // Selling price
                    val changePercentage = stockInformation.dailyPriceVariation

                    val fieldTitle = "`$tickerId` ($tickerName) | ${"%.2f".format(changePercentage)}%"
                    val emojiStatus = BrokerExecutorUtils.getEmojiStatusForTicker(stockInformation)

                    if (stockInformation.status != LorittaBovespaBrokerUtils.MARKET) {
                        field {
                            name = "$emojiStatus $fieldTitle"
                            value = context.i18nContext.get(BrokerCommand.I18N_PREFIX.Info.Embed.PriceBeforeMarketClose(currentPrice))
                            inline = true
                        }
                    } else if (LorittaBovespaBrokerUtils.checkIfTickerDataIsStale(stockInformation.lastUpdatedAt)) {
                        field {
                            name = "$emojiStatus $fieldTitle"
                            value = """${context.i18nContext.get(BrokerCommand.I18N_PREFIX.Info.Embed.BuyPrice(buyingPrice))}
                                |${context.i18nContext.get(BrokerCommand.I18N_PREFIX.Info.Embed.SellPrice(sellingPrice))}
                            """.trimMargin()
                            inline = true
                        }
                    } else {
                        field {
                            name = "$emojiStatus $fieldTitle"
                            value = """${context.i18nContext.get(BrokerCommand.I18N_PREFIX.Info.Embed.BuyPrice(buyingPrice))}
                                |${context.i18nContext.get(BrokerCommand.I18N_PREFIX.Info.Embed.SellPrice(sellingPrice))}
                            """.trimMargin()
                            inline = true
                        }
                    }
                }
            }
        }
    }
}