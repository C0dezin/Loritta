package net.perfectdreams.loritta.morenitta.interactions.vanilla.discord

import net.dv8tion.jda.api.entities.User
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.morenitta.interactions.CommandContextCompat
import net.perfectdreams.loritta.morenitta.interactions.commands.ApplicationCommandContext
import net.perfectdreams.loritta.morenitta.interactions.commands.LorittaUserCommandExecutor
import net.perfectdreams.loritta.morenitta.interactions.commands.UserCommandDeclarationWrapper
import net.perfectdreams.loritta.morenitta.interactions.commands.options.UserAndMember
import net.perfectdreams.loritta.morenitta.interactions.commands.userCommand

class UserInfoUserCommand : UserCommandDeclarationWrapper {
    override fun command() = userCommand(UserCommand.I18N_PREFIX.Info.ViewUserInfo, CommandCategory.DISCORD, UserInfoUserExecutor())

    class UserInfoUserExecutor : LorittaUserCommandExecutor() {
        override suspend fun execute(context: ApplicationCommandContext, user: User) {
            val member = context.guildOrNull?.getMember(user)
            context.reply(true) {
                apply(
                    UserInfoExecutor.createUserInfoMessage(
                        context,
                        UserAndMember(user, member)
                    )
                )
            }
        }
    }
}