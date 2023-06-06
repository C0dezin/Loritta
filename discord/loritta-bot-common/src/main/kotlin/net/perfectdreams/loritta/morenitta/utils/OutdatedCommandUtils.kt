package net.perfectdreams.loritta.morenitta.utils

import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.commands.CommandContext
import net.dv8tion.jda.api.EmbedBuilder
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.morenitta.platform.discord.legacy.commands.DiscordCommandContext
import java.time.Instant
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

/**
 * Utilites to ask users to use Loritta's slash commands instead of using old commands
 */
object OutdatedCommandUtils {
    private val ZONE_UTC = ZoneOffset.UTC
    private val MESSAGE_INTENT_DEADLINE = ZonedDateTime.of(2022, 8, 31, 0, 0, 0, 0, ZONE_UTC)
    private val OUTDATED_COMMAND_WARNING_DEADLINE = MESSAGE_INTENT_DEADLINE.minusDays(7L)

    suspend fun sendOutdatedCommandMessage(
        context: CommandContext,
        locale: BaseLocale,
        slashCommandName: String,
        alwaysSendOutdatedCommandWarning: Boolean = false
    ) {
        if (alwaysSendOutdatedCommandWarning || shouldSendOutdatedCommandWarning())
            context.sendMessageEmbeds(buildEmbed(locale, slashCommandName))
    }

    suspend fun sendOutdatedCommandMessage(
        context: net.perfectdreams.loritta.morenitta.api.commands.CommandContext,
        locale: BaseLocale,
        slashCommandName: String,
        alwaysSendOutdatedCommandWarning: Boolean = false
    ) {
        context as DiscordCommandContext
        if (alwaysSendOutdatedCommandWarning || shouldSendOutdatedCommandWarning())
            context.sendMessageEmbeds(buildEmbed(locale, slashCommandName))
    }

    /**
     * This checks if the outdated command warning should be sent or not, based on the amount of days between now and the deadline date
     */
    // For now, it should NEVER send a warning, because Discord is taking their sweeeeet time to fix the countless issues present in interactions
    // So, for now, let the users use message commands without repercussions: They are in a better state than Discord's slash commands
    private fun shouldSendOutdatedCommandWarning() = false

    private fun buildEmbed(locale: BaseLocale, slashCommandName: String) = EmbedBuilder()
        .setTitle("${locale["commands.outdatedCommand.title"]} <:lori_zap:956404868417990776>")
        .setDescription(
            locale.getList(
                "commands.outdatedCommand.description",
                slashCommandName,
                "https://discord.gg/lori",
                "<:lori_yay:1014022521739280454>",
                "<:lori_lurk:1012854272817381487>",
            ).joinToString("\n")
        )
        .setColor(Constants.ROBLOX_RED) // heh
        .build()
}