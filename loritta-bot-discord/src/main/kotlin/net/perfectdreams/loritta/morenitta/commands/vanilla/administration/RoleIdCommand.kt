package net.perfectdreams.loritta.morenitta.commands.vanilla.administration

import net.perfectdreams.loritta.morenitta.commands.AbstractCommand
import net.perfectdreams.loritta.morenitta.commands.CommandContext
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import net.dv8tion.jda.api.Permission
import net.perfectdreams.loritta.morenitta.messages.LorittaReply
import java.util.*
import net.perfectdreams.loritta.morenitta.LorittaBot

class RoleIdCommand(loritta: LorittaBot) : AbstractCommand(loritta, "roleid", listOf("cargoid", "iddocargo"), net.perfectdreams.loritta.common.commands.CommandCategory.MODERATION) {
	override fun getDescriptionKey() = LocaleKeyData("commands.command.roleid.description")

	// TODO: Fix getUsage

	override fun getExamples(): List<String> {
		return Arrays.asList("Moderadores")
	}

	override fun getDiscordPermissions(): List<Permission> {
		return listOf(Permission.MANAGE_ROLES)
	}

	override fun canUseInPrivateChannel(): Boolean {
		return false
	}

	override suspend fun run(context: CommandContext,locale: BaseLocale) {
		if (context.rawArgs.isNotEmpty()) {
			var argument = context.rawArgs.joinToString(" ")

			val mentionedRoles = context.message.mentions.roles // Se o usuário mencionar o cargo, vamos mostrar o ID dos cargos mencionados

			val list = mutableListOf<LorittaReply>()

			if (mentionedRoles.isNotEmpty()) {

				list.add(
                    LorittaReply(
                        message = locale["commands.command.roleid.identifiers", argument],
                        prefix = "\uD83D\uDCBC"
                )
                )

				mentionedRoles.mapTo(list) {
                    LorittaReply(
                            message = "*${it.name}* - `${it.id}`",
                            mentionUser = false
                    )
				}
			} else {
				val roles = context.guild.roles.filter { it.name.contains(argument, true) }

				list.add(
                    LorittaReply(
                        message = locale["commands.command.roleid.rolesThatContains", argument],
                        prefix = "\uD83D\uDCBC"
                )
                )

				if (roles.isEmpty()) {
					list.add(
                            LorittaReply(
                                    message = "*${locale["commands.command.roleid.emptyRoles"]}*",
                                    mentionUser = false,
                                    prefix = "\uD83D\uDE22"
                            )
					)
				} else {
					roles.mapTo(list) {
                        LorittaReply(
                                message = "*${it.name}* - `${it.id}`",
                                mentionUser = false
                        )
					}
				}

			}
			context.reply(*list.toTypedArray())
		} else {
			context.explain()
		}
	}
}