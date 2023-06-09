package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.images

import dev.kord.core.entity.User
import dev.kord.rest.Image
import net.perfectdreams.discordinteraktions.common.commands.options.SlashCommandArguments
import net.perfectdreams.gabrielaimageserver.client.GabrielaImageServerClient
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.i18nhelper.core.keydata.StringI18nData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.CinnamonSlashCommandExecutor
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.GuildApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.options.LocalizedApplicationCommandOptions
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.images.declarations.EveryGroupHasCommand
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.images.declarations.ThanksFriendsCommand
import net.perfectdreams.loritta.cinnamon.discord.utils.UserId
import net.perfectdreams.loritta.cinnamon.discord.utils.UserUtils
import net.perfectdreams.loritta.cinnamon.discord.utils.effectiveAvatar
import net.perfectdreams.loritta.cinnamon.discord.utils.images.*
import net.perfectdreams.loritta.cinnamon.discord.utils.images.ImageUtils.toByteArray
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.common.utils.Gender
import net.perfectdreams.loritta.common.utils.TodoFixThisData
import java.awt.Color
import java.awt.Graphics
import java.awt.Rectangle
import java.awt.image.BufferedImage
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

class ThanksFriendsExecutor(loritta: LorittaBot) : CinnamonSlashCommandExecutor(loritta) {
    inner class Options : LocalizedApplicationCommandOptions(loritta) {
        val user1 = optionalUser("user1", ThanksFriendsCommand.I18N_PREFIX.Options.User1.Text(ThanksFriendsCommand.I18N_PREFIX.Slot.Thanks))
        val user2 = optionalUser("user2", ThanksFriendsCommand.I18N_PREFIX.Options.User2.Text(ThanksFriendsCommand.I18N_PREFIX.Slot.For))
        val user3 = optionalUser("user3", ThanksFriendsCommand.I18N_PREFIX.Options.User3.Text(ThanksFriendsCommand.I18N_PREFIX.Slot.Being))
        val user4 = optionalUser("user4", ThanksFriendsCommand.I18N_PREFIX.Options.User4.Text(ThanksFriendsCommand.I18N_PREFIX.Slot.The))
        val user5 = optionalUser("user5", ThanksFriendsCommand.I18N_PREFIX.Options.User5.Text(ThanksFriendsCommand.I18N_PREFIX.Slot.NotYou))
        val user6 = optionalUser("user6", ThanksFriendsCommand.I18N_PREFIX.Options.User6.Text(ThanksFriendsCommand.I18N_PREFIX.Slot.Best))
        val user7 = optionalUser("user7", ThanksFriendsCommand.I18N_PREFIX.Options.User7.Text(ThanksFriendsCommand.I18N_PREFIX.Slot.Friends))
        val user8 = optionalUser("user8", ThanksFriendsCommand.I18N_PREFIX.Options.User8.Text(ThanksFriendsCommand.I18N_PREFIX.Slot.Of))
        val user9 = optionalUser("user9", ThanksFriendsCommand.I18N_PREFIX.Options.User9.Text(ThanksFriendsCommand.I18N_PREFIX.Slot.All))
    }

    override val options = Options()

    override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
        context.deferChannelMessage()

        val user1FromArguments = args[options.user1]
        val user2FromArguments = args[options.user2]
        val user3FromArguments = args[options.user3]
        val user4FromArguments = args[options.user4]
        val user5FromArguments = args[options.user5]
        val user6FromArguments = args[options.user6]
        val user7FromArguments = args[options.user7]
        val user8FromArguments = args[options.user8]
        val user9FromArguments = args[options.user9]

        val (listOfUsers, successfullyFilled, noPermissionToQuery) = UserUtils.fillUsersFromRecentMessages(
            context,
            listOf(
                user1FromArguments,
                user2FromArguments,
                user3FromArguments,
                user4FromArguments,
                user5FromArguments,
                user6FromArguments,
                user7FromArguments,
                user8FromArguments,
                user9FromArguments
            )
        )

        // Not enough users!
        if (!successfullyFilled) {
            context.fail {
                styled(context.i18nContext.get(ThanksFriendsCommand.I18N_PREFIX.NotEnoughUsers), Emotes.LoriSob)

                if (noPermissionToQuery) {
                    styled(context.i18nContext.get(I18nKeysData.Commands.UsersFill.NotEnoughUsersPermissionsTip), Emotes.LoriReading)
                } else if (context !is GuildApplicationCommandContext) {
                    styled(context.i18nContext.get(I18nKeysData.Commands.UsersFill.NotEnoughUsersGuildTip), Emotes.LoriReading)
                }
            }
        }

        val result = userAvatarCollage(3, 3) {
            localizedSlot(context.i18nContext, listOfUsers[0], Color.WHITE, ThanksFriendsCommand.I18N_PREFIX.Slot.Thanks)
            localizedSlot(context.i18nContext, listOfUsers[1], Color.WHITE, ThanksFriendsCommand.I18N_PREFIX.Slot.For)
            localizedSlot(context.i18nContext, listOfUsers[2], Color.WHITE, ThanksFriendsCommand.I18N_PREFIX.Slot.Being)
            localizedSlot(context.i18nContext, listOfUsers[3], Color.WHITE, ThanksFriendsCommand.I18N_PREFIX.Slot.The)
            localizedSlot(context.i18nContext, listOfUsers[4], Color.RED,   ThanksFriendsCommand.I18N_PREFIX.Slot.NotYou)
            localizedSlot(context.i18nContext, listOfUsers[5], Color.WHITE, ThanksFriendsCommand.I18N_PREFIX.Slot.Best)
            localizedSlot(context.i18nContext, listOfUsers[6], Color.WHITE, ThanksFriendsCommand.I18N_PREFIX.Slot.Friends)
            localizedSlot(context.i18nContext, listOfUsers[7], Color.WHITE, ThanksFriendsCommand.I18N_PREFIX.Slot.Of)
            localizedSlot(context.i18nContext, listOfUsers[8], Color.WHITE, ThanksFriendsCommand.I18N_PREFIX.Slot.All)
        }.generate(loritta)

        context.sendMessage {
            addFile("thanks_friends.png", result.toByteArray(ImageFormatType.PNG).inputStream())
        }
    }
}