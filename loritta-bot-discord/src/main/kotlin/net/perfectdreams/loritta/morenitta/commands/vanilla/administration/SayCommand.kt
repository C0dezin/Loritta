package net.perfectdreams.loritta.morenitta.commands.vanilla.administration

import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel
import net.dv8tion.jda.api.utils.messages.MessageEditData
import net.perfectdreams.loritta.common.commands.ArgumentType
import net.perfectdreams.loritta.common.commands.arguments
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import net.perfectdreams.loritta.common.utils.LorittaPermission
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.commands.AbstractCommand
import net.perfectdreams.loritta.morenitta.commands.CommandContext
import net.perfectdreams.loritta.morenitta.messages.LorittaReply
import net.perfectdreams.loritta.morenitta.utils.*
import net.perfectdreams.loritta.morenitta.utils.extensions.await
import net.perfectdreams.loritta.morenitta.utils.extensions.getGuildMessageChannelById

class SayCommand(loritta: LorittaBot) : AbstractCommand(loritta, "say", listOf("falar"), net.perfectdreams.loritta.common.commands.CommandCategory.MODERATION) {
	override fun getDescriptionKey() = LocaleKeyData("commands.command.say.description")
	override fun getExamplesKey()  = LocaleKeyData("commands.command.say.examples")
	override fun getUsage() = arguments {
		argument(ArgumentType.TEXT) {}
	}

	override fun getDiscordPermissions(): List<Permission> {
		return listOf(Permission.MANAGE_SERVER)
	}

	override suspend fun run(context: CommandContext, locale: BaseLocale) {
		if (context.rawArgs.isNotEmpty()) {
			OutdatedCommandUtils.sendOutdatedCommandMessage(context, locale, "webhook send")

			var args = context.rawArgs
			var currentIdx = 0

			val arg0 = context.rawArgs[0]
			var isEditMode = false
			var editMessage: Message? = null

			if (arg0 == "edit" || arg0 == "editar") {
				isEditMode = true
				currentIdx++
			}

			val channelIdOrMessageLink = context.rawArgs[currentIdx]

			if (isEditMode) {
				val split = channelIdOrMessageLink.split("/")

				if (split.size >= 2) {
					val messageId = split.last()
					val channelId = split.dropLast(1).last()

					editMessage = context.guild.getGuildMessageChannelById(channelId)!!
							.retrieveMessageById(messageId)
							.await()
					args = args.remove(0) // Removes the "edit"
					args = args.remove(0) // Removes the message URL
				} else { return } // TODO: Good message
			}

			// Pegando canal de texto, via menções, ID ou nada
			val channel = if (isEditMode) editMessage!!.channel else if (args.size >= 2) {
				if (channelIdOrMessageLink.startsWith("<#") && channelIdOrMessageLink.endsWith(">")) {
					try {
						val ch = context.guild.getGuildMessageChannelById(channelIdOrMessageLink.substring(2, channelIdOrMessageLink.length - 1))
						args = args.remove(0)
						ch
					} catch (e: Exception) {
						null
					}
				} else {
					try {
						val ch = context.guild.getGuildMessageChannelById(channelIdOrMessageLink)
						args = args.remove(0)
						ch
					} catch (e: Exception) {
						null
					}
				}
			} else { null } ?: context.event.channel

			if (channel is TextChannel) { // Caso seja text channel...
				if (!channel.canTalk()) {
					context.reply(
							LorittaReply(
									context.locale["commands.command.say.iDontHavePermissionToTalkIn", channel.asMention],
									Constants.ERROR
							)
					)
					return
				}
				if (!channel.canTalk(context.handle)) {
					context.reply(
							LorittaReply(
									context.locale["commands.command.say.youDontHavePermissionToTalkIn", channel.asMention],
									Constants.ERROR
							)
					)
					return
				}
				if (context.config.blacklistedChannels.contains(channel.idLong) && !context.lorittaUser.hasPermission(LorittaPermission.BYPASS_COMMAND_BLACKLIST)) {
					context.reply(
							LorittaReply(
									context.locale["commands.command.say.cannotBeUsedIn", channel.asMention],
									Constants.ERROR
							)
					)
					return
				}
			}

			var message = args.joinToString(" ")

			if (!context.isPrivateChannel && !context.handle.hasPermission(channel as GuildChannel, Permission.MESSAGE_MENTION_EVERYONE))
				message = message.escapeMentions()

			// Watermarks the message to "deanonymise" the message, to avoid users reporting Loritta for ToS breaking stuff, even tho it was
			// a malicious user sending the messages.
			val watermarkedMessage = MessageUtils.watermarkSayMessage(
					message,
					context.userHandle,
					context.locale["commands.command.say.messageSentBy"]
			)

			val discordMessage = try {
				MessageUtils.generateMessage(
						watermarkedMessage,
						mutableListOf<Any>(context.userHandle)
								.apply {
									// If the guild is not null, we add them to the context.
									// This is needed because "context.event.guild" is null inside a private channel.
									val guild = context.event.guild
									if (guild != null)
										add(guild)
								},
						context.event.guild
				)
			} catch (e: Exception) {
				null
			}

			if (discordMessage != null)
				(
						if (isEditMode)
							editMessage!!.editMessage(MessageEditData.fromCreateData(discordMessage))
						else
							channel.sendMessage(discordMessage)
						).queue()
			else
				(
						if (isEditMode)
							editMessage!!.editMessage(message)
						else
							channel.sendMessage(message)
						).queue()

			if (context.event.channel != channel && channel is TextChannel)
				context.reply(
						LorittaReply(
								context.locale["commands.command.say.messageSuccessfullySent", channel.asMention],
								"\uD83C\uDF89"
						)
				)

		} else {
			this.explain(context)
		}
	}
}