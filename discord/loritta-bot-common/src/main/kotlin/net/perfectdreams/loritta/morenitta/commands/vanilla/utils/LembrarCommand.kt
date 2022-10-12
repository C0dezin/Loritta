package net.perfectdreams.loritta.morenitta.commands.vanilla.utils

import net.perfectdreams.loritta.morenitta.commands.AbstractCommand
import net.perfectdreams.loritta.morenitta.commands.CommandContext
import net.perfectdreams.loritta.morenitta.dao.Reminder
import net.perfectdreams.loritta.morenitta.tables.Reminders
import net.perfectdreams.loritta.morenitta.utils.Constants
import net.perfectdreams.loritta.morenitta.utils.TimeUtils
import net.perfectdreams.loritta.morenitta.utils.extensions.humanize
import net.perfectdreams.loritta.morenitta.utils.onReactionAddByAuthor
import net.perfectdreams.loritta.morenitta.utils.onResponseByAuthor
import net.perfectdreams.loritta.morenitta.utils.substringIfNeeded
import net.perfectdreams.loritta.deviousfun.EmbedBuilder
import dev.kord.common.entity.Permission
import net.perfectdreams.loritta.deviousfun.entities.Message
import net.perfectdreams.loritta.deviousfun.DeviousEmbed
import net.perfectdreams.loritta.morenitta.messages.LorittaReply
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import org.jetbrains.exposed.sql.deleteWhere
import java.awt.Color
import java.time.Instant
import java.time.ZonedDateTime
import net.perfectdreams.loritta.morenitta.LorittaBot

class LembrarCommand(loritta: LorittaBot) : AbstractCommand(loritta, "remindme", listOf("lembre", "remind", "lembrar", "lembrete", "reminder"), net.perfectdreams.loritta.common.commands.CommandCategory.UTILS) {
	override fun getBotPermissions() = listOf(Permission.ManageMessages)

	override fun getDescriptionKey() = LocaleKeyData("${LOCALE_PREFIX}.description")
	override fun getExamplesKey() = LocaleKeyData("${LOCALE_PREFIX}.examples")
	// TODO: Fix Usage

	override suspend fun run(context: CommandContext,locale: BaseLocale) {
		if (thereIsCommandToProcess(context)) {
			val message = getMessage(context)
			if ( message.isAValidListCommand() ) {
				handleReminderList(context, 0, locale)
				return
			}

			val reply = createReply(context, locale)
			createResponseByAuthor(reply, context, message, locale)
			createReactionAddByAuthor(reply, context, locale)
			runCatching { reply.addReaction("\uD83D\uDE45") }
		} else {
			this.explain(context)
		}
	}

	private fun createReactionAddByAuthor(reply: Message, context: CommandContext, locale: BaseLocale) {
		reply.onReactionAddByAuthor(context) {
			loritta.messageInteractionCache.remove(reply.idLong)
			runCatching { reply.delete() }
			context.reply(
					LorittaReply(
							message = locale["$LOCALE_PREFIX.cancel"],
							prefix = "\uD83D\uDDD1"
					)
			)
		}
	}

	private fun createResponseByAuthor(reply: Message, context: CommandContext, message: String, locale: BaseLocale) {
		reply.onResponseByAuthor(context) {
			loritta.messageInteractionCache.remove(reply.idLong)
			runCatching { reply.delete() }
			val inMillis = TimeUtils.convertToMillisRelativeToNow(it.message.contentDisplay)
			val instant = Instant.ofEpochMilli(inMillis)
			val localDateTime = ZonedDateTime.ofInstant(instant, Constants.LORITTA_TIMEZONE)

			val messageContent = message.trim()
			logger.trace { "userId = ${context.userHandle.idLong}" }
			logger.trace { "channelId = ${context.message.channel.idLong}" }
			logger.trace { "remindAt = $inMillis" }
			logger.trace { "content = $messageContent" }

			createReminder(context, localDateTime, messageContent)

			val dayOfMonth = String.format("%02d", localDateTime.dayOfMonth)
			val month = String.format("%02d", localDateTime.monthValue)
			val hours = String.format("%02d", localDateTime.hour)
			val minutes = String.format("%02d", localDateTime.minute)
			context.sendMessage(context.getAsMention(true) + locale["${LOCALE_PREFIX}.success", dayOfMonth, month, localDateTime.year, hours, minutes])
		}
	}

	private suspend fun createReminder(context: CommandContext, zonedDateTime: ZonedDateTime, messageContent: String) {
		loritta.newSuspendedTransaction {
			Reminder.new {
				userId = context.userHandle.idLong
				channelId = context.message.textChannel.idLong
				remindAt = (zonedDateTime.toEpochSecond() * 1000)
				content = messageContent
			}
		}
	}

	private suspend fun createReply(context: CommandContext, locale: BaseLocale): Message {
		return context.reply(
				LorittaReply(
						message = locale["${LOCALE_PREFIX}.setHour"],
						prefix = "⏰"
				)
		)
	}

	private fun getMessage(context: CommandContext) =
			context.strippedArgs.joinToString(separator = " ")

	private fun thereIsCommandToProcess(context: CommandContext) =
			context.args.isNotEmpty()

	private suspend fun handleReminderList(context: CommandContext, page: Int, locale: BaseLocale) {
		val reminders = loritta.newSuspendedTransaction {
			Reminder.find { Reminders.userId eq context.userHandle.idLong }.toMutableList()
		}

		val visReminders = reminders.subList(page * 9, Math.min((page * 9) + 9, reminders.size))
		val embed = EmbedBuilder()
		embed.setTitle("<a:lori_notification:394165039227207710> ${locale["${LOCALE_PREFIX}.yourReminders"]} (${reminders.size})")
		embed.setColor(Color(255, 179, 43))

		for ((idx, reminder) in visReminders.withIndex()) {
			embed.appendDescription(Constants.INDEXES[idx] + " ${reminder.content.substringIfNeeded(0..100)}\n")
		}

		val message = context.sendMessage(context.getAsMention(true), embed.build())

		message.onReactionAddByAuthor(context) {
			if (it.reactionEmote.isEmote("➡")) {
				runCatching { message.delete() }
				handleReminderList(context, page + 1, locale)
				return@onReactionAddByAuthor
			}
			if (it.reactionEmote.isEmote("⬅")) {
				runCatching { message.delete() }
				handleReminderList(context, page - 1, locale)
				return@onReactionAddByAuthor
			}

			val idx = Constants.INDEXES.indexOf(it.reactionEmote.name)

			if (idx == -1) // derp
				return@onReactionAddByAuthor

			val reminder = visReminders.getOrNull(idx) ?: return@onReactionAddByAuthor

			val textChannel = loritta.lorittaShards.getTextChannelById(reminder.channelId.toString())

			val guild = textChannel?.guild

			val embedBuilder = EmbedBuilder()
			if (guild != null) {
				embedBuilder.setThumbnail(guild.iconUrl)
			}

			embedBuilder.setTitle("<a:lori_notification:394165039227207710> ${reminder.content}".substringIfNeeded(0 until DeviousEmbed.TITLE_MAX_LENGTH))
			embedBuilder.appendDescription("**${locale["${LOCALE_PREFIX}.remindAt"]} ** ${reminder.remindAt.humanize(locale)}\n")
			embedBuilder.appendDescription("**${locale["${LOCALE_PREFIX}.createdInGuild"]}** `${guild?.name ?: "Servidor não existe mais..."}`\n")
			embedBuilder.appendDescription("**${locale["${LOCALE_PREFIX}.remindInTextChannel"]}** ${textChannel?.asMention ?: "Canal de texto não existe mais..."}")
			embedBuilder.setColor(Color(255, 179, 43))

			runCatching { message.clearReactions() }
			runCatching { message.editMessage(embedBuilder.build()) }
			runCatching { message.addReaction("⬅️") }

			message.onReactionAddByAuthor(context) {

				if (it.reactionEmote.isEmote("⬅️")) {

					runCatching { message.delete() }
					handleReminderList(context, page, locale)
					return@onReactionAddByAuthor

				}

				runCatching { message.delete() }
				reminders.remove(reminder)
				loritta.newSuspendedTransaction {
					Reminders.deleteWhere { Reminders.id eq reminder.id }
				}

				val successMessage = context.sendMessage(locale["${LOCALE_PREFIX}.reminderRemoved"])
				successMessage.onReactionAddByAuthor(context) {
					runCatching { successMessage.delete() }
					handleReminderList(context, page, locale)
				}
				runCatching { successMessage.addReaction("⬅️") }
				return@onReactionAddByAuthor
			}

			runCatching { message.addReaction("\uD83D\uDDD1") }
			return@onReactionAddByAuthor
		}

		if (page != 0)
			runCatching { message.addReaction("⬅") }

		for ((idx, _) in visReminders.withIndex()) {
			runCatching { message.addReaction(Constants.INDEXES[idx]) }
		}

		if (((page + 1) * 9) in 0..reminders.size) {
			runCatching { message.addReaction("➡") }
		}
	}

	private companion object {
		private const val LOCALE_PREFIX = "commands.command.remindme"
	}
}

private fun String.isAValidListCommand(): Boolean {
	val validListCommands = listOf("lista", "list")
	return 	validListCommands.contains(this)
}