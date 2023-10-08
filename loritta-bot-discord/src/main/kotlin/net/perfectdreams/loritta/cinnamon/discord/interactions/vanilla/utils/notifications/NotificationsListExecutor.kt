package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.utils.notifications

import net.perfectdreams.discordinteraktions.common.builder.message.embed
import net.perfectdreams.discordinteraktions.common.utils.field
import net.perfectdreams.loritta.common.utils.LorittaColors
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.CinnamonSlashCommandExecutor
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.discordinteraktions.common.commands.options.SlashCommandArguments
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.utils.declarations.NotificationsCommand
import net.perfectdreams.loritta.cinnamon.discord.utils.UserId
import net.perfectdreams.loritta.cinnamon.discord.utils.toKordColor
import net.perfectdreams.loritta.serializable.CorreiosPackageUpdateUserNotification
import net.perfectdreams.loritta.serializable.DailyTaxTaxedUserNotification
import net.perfectdreams.loritta.serializable.DailyTaxWarnUserNotification
import net.perfectdreams.loritta.serializable.UnknownUserNotification

class NotificationsListExecutor(loritta: LorittaBot) : CinnamonSlashCommandExecutor(loritta) {


    override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
        context.deferChannelMessageEphemerally()

        val notifications = context.loritta.pudding.notifications.getUserNotifications(UserId(context.user.id), 10, 0)

        context.sendEphemeralMessage {
            embed {
                title = context.i18nContext.get(NotificationsCommand.I18N_PREFIX.List.Title)

                for (notification in notifications) {
                    field(
                        "[${notification.id}] ${
                        when (notification) {
                            is DailyTaxTaxedUserNotification -> context.i18nContext.get(NotificationsCommand.I18N_PREFIX.DailyTaxTaxedUserNotification)
                            is DailyTaxWarnUserNotification -> context.i18nContext.get(NotificationsCommand.I18N_PREFIX.DailyTaxWarnUserNotification)
                            is CorreiosPackageUpdateUserNotification -> context.i18nContext.get(NotificationsCommand.I18N_PREFIX.CorreiosPackageUpdate)
                            is UnknownUserNotification -> context.i18nContext.get(NotificationsCommand.I18N_PREFIX.UnknownNotification)
                        }}",
                        "<t:${notification.timestamp.epochSeconds}:d> <t:${notification.timestamp.epochSeconds}:t> | <t:${notification.timestamp.epochSeconds}:R>",
                        false
                    )
                }

                color = LorittaColors.LorittaAqua.toKordColor()
            }
        }
    }
}