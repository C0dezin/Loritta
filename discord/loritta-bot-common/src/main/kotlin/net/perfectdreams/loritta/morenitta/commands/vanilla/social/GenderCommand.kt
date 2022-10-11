package net.perfectdreams.loritta.morenitta.commands.vanilla.social

import net.perfectdreams.loritta.morenitta.commands.AbstractCommand
import net.perfectdreams.loritta.morenitta.commands.CommandContext
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.morenitta.utils.locale.Gender
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import net.perfectdreams.loritta.morenitta.utils.onReactionAddByAuthor
import net.perfectdreams.loritta.deviousfun.EmbedBuilder
import net.perfectdreams.loritta.morenitta.messages.LorittaReply
import net.perfectdreams.loritta.morenitta.utils.OutdatedCommandUtils
import net.perfectdreams.loritta.morenitta.LorittaBot

class GenderCommand(loritta: LorittaBot) : AbstractCommand(loritta, "gender", listOf("gênero", "genero"), net.perfectdreams.loritta.common.commands.CommandCategory.SOCIAL) {
    override fun getDescriptionKey() = LocaleKeyData("commands.command.gender.description")

    override suspend fun run(context: CommandContext, locale: BaseLocale) {
        OutdatedCommandUtils.sendOutdatedCommandMessage(context, locale, "gender")

        val embed = EmbedBuilder()
                .setTitle(locale["commands.command.gender.whatAreYou"])
                .setDescription(locale["commands.command.gender.whyShouldYouSelect"])
                .build()


        val message = context.sendMessage(embed)

        runCatching { message.addReaction("male:384048518853296128") }
        runCatching { message.addReaction("female:384048518337265665") }
        runCatching { message.addReaction("❓") }

        message.onReactionAddByAuthor(context) {
            runCatching { message.delete() }

            if (it.reactionEmote.id == "384048518853296128") {
                loritta.newSuspendedTransaction {
                    context.lorittaUser.profile.settings.gender = Gender.MALE
                }

                context.reply(
						LorittaReply(
								locale["commands.command.gender.successfullyChanged"],
								"\uD83C\uDF89"
						)
				)
            }


            if (it.reactionEmote.id == "384048518337265665") {
                loritta.newSuspendedTransaction {
                    context.lorittaUser.profile.settings.gender = Gender.FEMALE
                }

                context.reply(
						LorittaReply(
								locale["commands.command.gender.successfullyChanged"],
								"\uD83C\uDF89"
						)
				)
            }

            if (it.reactionEmote.isEmote("❓")) {
                loritta.newSuspendedTransaction {
                    context.lorittaUser.profile.settings.gender = Gender.UNKNOWN
                }

                context.reply(
						LorittaReply(
								locale["commands.command.gender.successfullyChanged"],
								"\uD83C\uDF89"
						)
				)
            }
        }
    }
}