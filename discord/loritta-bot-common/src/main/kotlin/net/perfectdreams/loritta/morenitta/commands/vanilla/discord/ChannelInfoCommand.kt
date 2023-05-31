package net.perfectdreams.loritta.morenitta.commands.vanilla.discord

import net.perfectdreams.loritta.morenitta.utils.Constants
import net.perfectdreams.loritta.morenitta.utils.DateUtils
import net.dv8tion.jda.api.EmbedBuilder
import net.perfectdreams.loritta.common.commands.ArgumentType
import net.perfectdreams.loritta.common.commands.arguments
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.platform.discord.legacy.commands.DiscordAbstractCommandBase
import net.perfectdreams.loritta.morenitta.utils.OutdatedCommandUtils
import net.perfectdreams.loritta.morenitta.utils.extensions.getGuildMessageChannelById

class ChannelInfoCommand(loritta: LorittaBot) : DiscordAbstractCommandBase(loritta, listOf("channelinfo", "channel"), net.perfectdreams.loritta.common.commands.CommandCategory.DISCORD) {
	companion object {
		private const val LOCALE_PREFIX = "commands.command"
	}

	override fun command() = create {
		localizedDescription("$LOCALE_PREFIX.channelinfo.description")

		arguments {
			argument(ArgumentType.TEXT) {
				optional = true
			}
		}

		// TODO: Fix examples
		/* examples {
			listOf(
					"",
					"297732013006389252"
			)
		} */

		canUseInPrivateChannel = false

		executesDiscord {
			OutdatedCommandUtils.sendOutdatedCommandMessage(this, locale, "server channel info")

			val context = this
			val channelId = args.getOrNull(0)
					?.replace("<#", "")
					?.replace(">", "")
					?: context.discordMessage.channel.id
			val channel = context.guild.getTextChannelById(channelId)!!

			val builder = EmbedBuilder()

			val channelTopic = if (channel.topic == null) {
				"Tópico não definido!"
			} else {
				"```\n${channel.topic}```"
			}

			builder.setColor(Constants.DISCORD_BLURPLE)
			builder.setTitle("\uD83D\uDC81 ${context.locale["$LOCALE_PREFIX.channelinfo.channelInfo", "#${channel.name}"]}")
			builder.setDescription(channelTopic)
			builder.addField("\uD83D\uDD39 ${context.locale["$LOCALE_PREFIX.channelinfo.channelMention"]}", "`${channel.asMention}`", true)
			builder.addField("\uD83D\uDCBB ${context.locale["$LOCALE_PREFIX.userinfo.discordId"]}", "`${channel.id}`", true)
			builder.addField("\uD83D\uDD1E NSFW", if (channel.isNSFW) context.locale["loritta.fancyBoolean.true"] else context.locale["loritta.fancyBoolean.false"], true)
			builder.addField("\uD83D\uDCC5 ${context.locale["$LOCALE_PREFIX.channelinfo.channelCreated"]}", DateUtils.formatDateWithRelativeFromNowAndAbsoluteDifferenceWithDiscordMarkdown(channel.timeCreated), true)
			builder.addField("\uD83D\uDD39 Guild", "`${channel.guild.name}`", true)
			context.sendMessage(context.user.asMention, builder.build())
		}
	}
}