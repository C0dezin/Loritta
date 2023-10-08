package net.perfectdreams.loritta.morenitta.commands.vanilla.`fun`

import net.perfectdreams.loritta.morenitta.LorittaLauncher
import net.perfectdreams.loritta.morenitta.utils.Constants
import net.perfectdreams.loritta.morenitta.utils.extensions.*
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.morenitta.utils.onReactionAddByAuthor
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Message
import net.perfectdreams.loritta.common.commands.ArgumentType
import net.perfectdreams.loritta.common.commands.arguments
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.platform.discord.legacy.commands.DiscordAbstractCommandBase
import net.perfectdreams.loritta.morenitta.platform.discord.legacy.commands.DiscordCommandContext
import net.perfectdreams.loritta.morenitta.utils.config.FanArt
import net.perfectdreams.loritta.morenitta.utils.config.FanArtArtist

class FanArtsCommand(loritta: LorittaBot) : DiscordAbstractCommandBase(loritta, listOf("fanarts", "fanart"), net.perfectdreams.loritta.common.commands.CommandCategory.MISC) {
    override fun command() = create {
        localizedDescription("commands.command.fanarts.description", "<a:lori_blobheartseyes:393914347706908683>", "<a:lori_blobheartseyes:393914347706908683>")

        arguments {
            argument(ArgumentType.NUMBER) {
                optional = true
            }
        }

        botRequiredPermissions = listOf(Permission.MESSAGE_MANAGE)

        executesDiscord {
            val index = args.getOrNull(0)

            val fanArtsByDate = loritta.fanArts.sortedBy {
                it.createdAt
            }

            var fanArtIndex = (index?.toIntOrNull() ?: LorittaBot.RANDOM.nextInt(fanArtsByDate.size) + 1) - 1
            if (fanArtIndex !in fanArtsByDate.indices) {
                fanArtIndex = 0
            }

            sendFanArtEmbed(this, locale, fanArtsByDate, fanArtIndex, null)
        }
    }

    suspend fun sendFanArtEmbed(context: DiscordCommandContext, locale: BaseLocale, list: List<FanArt>, item: Int, currentMessage: Message?) {
        val fanArt = list[item]
        val index = list.indexOf(fanArt) + 1

        val embed = EmbedBuilder().apply {
            setTitle("\uD83D\uDDBC<:loritta:331179879582269451> Fan Art")

            val fanArtArtist = loritta.getFanArtArtistByFanArt(fanArt)
            val discordId = fanArtArtist?.socialNetworks
                ?.filterIsInstance<FanArtArtist.SocialNetwork.DiscordSocialNetwork>()
                ?.firstOrNull()
                ?.id

            val user = loritta.lorittaShards.retrieveUserInfoById(discordId?.toLong())

            val displayName = fanArtArtist?.info?.override?.name ?: user?.name ?: fanArtArtist?.info?.name

            setDescription("**" + locale["commands.command.fanarts.madeBy", displayName] + "**")

            // TODO: Corrigir
            /* if (artist != null) {
                for (socialNetwork in artist.socialNetworks) {
                    var root = socialNetwork.display
                    if (socialNetwork.link != null) {
                        root = "[$root](${socialNetwork.link})"
                    }
                    appendDescription("\n**${socialNetwork.socialNetwork.fancyName}:** $root")
                }
            } */

            appendDescription("\n\n${locale["commands.command.fanarts.thankYouAll", displayName]}")

            var footer = "Fan Art ${locale["loritta.xOfX", index, loritta.fanArts.size]}"

            if (user != null) {
                footer = "${user.name + "#" + user.discriminator} • $footer"
            }

            setFooter(footer, user?.effectiveAvatarUrl)
            setImage("https://loritta.website/assets/img/fanarts/${fanArt.fileName}")
            setColor(Constants.LORITTA_AQUA)
        }

        var message = currentMessage?.edit(context.getUserMention(true), embed.build(), clearReactions = false) ?: context.sendMessage(context.getUserMention(true), embed.build())

        val allowForward = list.size > item + 1
        val allowBack = item != 0

        if ((!allowForward && message.reactions.any { it.emoji.isEmote("⏩") }) || (!allowBack && message.reactions.any { it.emoji.isEmote("⏪") })) { // Remover todas as reações caso seja necessário
            message.clearReactions().await()
            message = message.refresh().await() // Precisamos "refrescar", já que o JDA não limpa a lista de reações
        }

        message.onReactionAddByAuthor(context) {
            if (allowForward && it.emoji.isEmote("⏩")) {
                sendFanArtEmbed(context, locale, list, item + 1, message)
            }
            if (allowBack && it.emoji.isEmote("⏪")) {
                sendFanArtEmbed(context, locale, list, item - 1, message)
            }
        }

        val emotes = mutableListOf<String>()

        if (allowBack)
            emotes.add("⏪")
        if (allowForward)
            emotes.add("⏩")

        message.doReactions(*emotes.toTypedArray())
    }
}