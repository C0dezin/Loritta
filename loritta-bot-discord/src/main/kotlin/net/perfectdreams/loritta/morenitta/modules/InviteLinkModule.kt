package net.perfectdreams.loritta.morenitta.modules

import com.github.benmanes.caffeine.cache.Caffeine
import dev.minn.jda.ktx.messages.MessageCreate
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.utils.LorittaPermission
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.dao.Profile
import net.perfectdreams.loritta.morenitta.dao.ServerConfig
import net.perfectdreams.loritta.morenitta.dao.servers.moduleconfigs.InviteBlockerConfig
import net.perfectdreams.loritta.morenitta.events.LorittaMessageEvent
import net.perfectdreams.loritta.morenitta.utils.*
import net.perfectdreams.loritta.morenitta.utils.extensions.await
import net.perfectdreams.loritta.morenitta.utils.extensions.sendMessageAsync
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import java.util.concurrent.TimeUnit
import java.util.regex.Matcher

class InviteLinkModule(val loritta: LorittaBot) : MessageReceivedModule {
    companion object {
        val cachedInviteLinks = Caffeine.newBuilder().expireAfterAccess(30L, TimeUnit.MINUTES).build<Long, List<String>>().asMap()
        
        val unicodeLookalikes = mapOf(
            "a" to listOf("\u0430", "\u00e0", "\u00e1", "\u1ea1", "\u0105"),
            "c" to listOf("\u0441", "\u0188", "\u010b"),
            "d" to listOf("\u0501", "\u0257"),
            "e" to listOf("\u0435", "\u1eb9", "\u0117", "\u0117", "\u00e9", "\u00e8"),
            "g" to listOf("\u0121"),
            "h" to listOf("\u04bb"),
            "i" to listOf("\u0456", "\u00ed", "\u00ec", "\u00ef"),
            "j" to listOf("\u0458", "\u029d"),
            "k" to listOf("\u03ba"),
            "l" to listOf("\u04cf", "\u1e37"),
            "n" to listOf("\u0578"),
            "o" to listOf("\u043e", "\u03bf", "\u0585", "\u022f", "\u1ecd", "\u1ecf", "\u01a1", "\u00f6", "\u00f3", "\u00f2"),
            "p" to listOf("\u0440"),
            "q" to listOf("\u0566"),
            "s" to listOf("\u0282"),
            "u" to listOf("\u03c5", "\u057d", "\u00fc", "\u00fa", "\u00f9"),
            "v" to listOf("\u03bd", "\u0475"),
            "x" to listOf("\u0445", "\u04b3"),
            "y" to listOf("\u0443", "\u00fd"),
            "z" to listOf("\u0290", "\u017c")
        )
    }

    override suspend fun matches(
        event: LorittaMessageEvent,
        loritta:User  LorittaUser ,
        lorittaProfile: Profile?,
        serverConfig: ServerConfig,
        locale: BaseLocale,
        i18nContext: I18nContext
    ): Boolean {
        val inviteBlockerConfig = serverConfig.getCachedOrRetreiveFromDatabase<InviteBlockerConfig?>(lor itta, ServerConfig::inviteBlockerConfig)
            ?: return false

        if (!inviteBlockerConfig.enabled)
            return false

        if (inviteBlockerConfig.whitelistedChannels.contains(event.channel.idLong))
            return false

        if (lorittaUser .hasPermission(LorittaPermission.ALLOW_INVITES))
            return false

        return true
    }

    override suspend fun handle(
        event: LorittaMessageEvent,
        loritta:User  LorittaUser ,
        lorittaProfile: Profile?,
        serverConfig: ServerConfig,
        locale: BaseLocale,
        i18nContext: I18nContext
    ): Boolean {
        val message = event.message
        val guild = message.guild
        val inviteBlockerConfig = serverConfig.getCachedOrRetreiveFromDatabase<InviteBlockerConfig?>(loritta, ServerConfig::inviteBlockerConfig)
            ?: return false

        val validMatchers = mutableListOf<Matcher>()

        validMatchers.addAll(checkMessage(MessageWrapper.Message(message)))

        for (snapshot in message.messageSnapshots) {
            validMatchers.addAll(checkMessage(MessageWrapper.MessageSnapshot(snapshot)))
        }

        // Se existe algum link na mensagem...
        if (validMatchers.isEmpty())
            return false

        // Para evitar que use a API do Discord para pegar os invites do servidor toda hora, nós iremos *apenas* pegar caso seja realmente
        // necessário, e, ao pegar, vamos guardar no cache de invites
        val whitelisted = mutableListOf<String>()
        if (inviteBlockerConfig.whitelistServerInvites) {
            guild.vanityCode?.let { whitelisted.add(it) }

            if (!cachedInviteLinks.containsKey(guild.idLong) && guild.selfMember.hasPermission(Permission.MANAGE_SERVER)) {
                val invites = guild.retrieveInvites().await()
                val codes = invites.map { it.code }
                cachedInviteLinks[guild.idLong] = codes
            }

            cachedInviteLinks[guild.idLong]?.forEach {
                whitelisted.add(it)
            }
        }

        for (matcher in validMatchers) {
            val urls = mutableSetOf<String>()
            while (matcher.find()) {
                var url = matcher.group()
                if (url.startsWith("discord.gg", true)) {
                    url = "discord.gg" + matcher.group(1).replace(".", "")
                }
                urls.add(url)
            }

            for (url in urls) {
                val inviteId = MiscUtils.getInviteId(url) ?: continue

                if (whitelisted.contains(inviteId))
                    continue

                if (inviteBlockerConfig.deleteMessage && guild.selfMember.hasPermission(message.guildChannel, Permission.MESSAGE_MANAGE))
                    message.delete().queue()

                val warnMessage = inviteBlockerConfig.warnMessage

                if (inviteBlockerConfig.tellUser  && !warnMessage.isNullOrEmpty() && message.guildChannel.canTalk()) {
                    if (event.member != null && event.member.hasPermission(Permission.MANAGE_SERVER)) {
                        // Se a pessoa tiver permissão para ativar a permissão de convites, faça que a Loritta recomende que ative a permissão
                        val topRole = event.member.roles.sortedByDescending { it.position }.firstOrNull { !it.isPublicRole }

                        if (topRole != null) {
                            val button = loritta.interactivityManager.buttonForUser (
                                message.author,
                                ButtonStyle.PRIMARY,
                                i18nContext.get(I18nKeysData.Modules.InviteBlocker.AllowSendingInvites),
                                {
                                    loriEmoji = net.perfectdreams.loritta.cinnamon.emotes.Emotes.LoriPat
                                }
                            ) { context ->
                                val deferredEdit = context.deferEdit()

                                val success = loritta.pudding.transaction {
                                    // Check if it already exists
                                    if (net.perfectdreams.loritta.cinnamon.pudding.tables.servers.ServerRolePermissions.select {
                                            net.perfectdreams.loritta.cinnamon.pudding.tables.servers.ServerRolePermissions.guild eq guild.idLong and
                                                    (net.perfectdreams.loritta.cinnamon.pudding.tables.servers.ServerRolePermissions.roleId eq topRole.idLong) and
                                                    (net.perfectdreams.loritta.cinnamon.pudding.tables.servers.ServerRolePermissions.permission eq net.perfectdreams.loritta.common.utils.LorittaPermission.ALLOW_INVITES)
                                        }.count() == 1L
                                    ) {
                                        return@transaction false
                                    }

                                    net.perfectdreams.loritta.cinnamon.pudding.tables.servers.ServerRolePermissions.insert {
                                        it[net.perfectdreams.loritta.cinnamon.pudding.tables.servers.ServerRolePermissions.guild] = guild.idLong
                                        it[net.perfectdreams.loritta.cinnamon.pudding.tables.servers.ServerRolePermissions.roleId] = topRole.idLong
                                        it[net.perfectdreams.loritta.cinnamon.pudding.tables.servers.ServerRolePermissions.permission] = net.perfectdreams.loritta.common.utils.LorittaPermission.ALLOW_INVITES
                                    }
                                    return@transaction true
                                }

                                // Update message updates the original interaction message, in this case, where the button is
                                deferredEdit.editOriginalComponents(ActionRow.of(context.event.component.asDisabled())).await()

                                context.reply(true) {
                                    if (success) {
                                        styled(
                                            context.i18nContext.get(I18nKeysData.Modules.InviteBlocker.BypassEnabled("<@&${topRole.idLong}>")),
                                            net.perfectdreams.loritta.cinnamon.emotes.Emotes.LoriHappy
                                        )
                                    } else {
                                        styled(
                                            context.i18nContext.get(I18nKeysData.Modules.InviteBlocker.RoleAlreadyHasInviteBlockerBypass("<@&${topRole.idLong}>")),
                                            net.perfectdreams.loritta.cinnamon.emotes.Emotes.Error
                                        )
                                    }
                                }
                            }

                            message.guildChannel.sendMessageAsync(
                                MessageCreate {
                                    mentions {}

                                    styled(
                                        i18nContext.get(I18nKeysData.Modules.InviteBlocker.ActivateInviteBlockerBypass("<@&${topRole.id}>")),
                                        net.perfectdreams.loritta.cinnamon.emotes.Emotes.LoriSmile
                                    )

                                    styled(
                                        i18nContext.get(I18nKeysData.Modules.InviteBlocker.HowToReEnableLater("<${loritta.config.loritta.website.url}guild/${guild.idLong}/configure/permissions>")),
                                        net.perfectdreams.loritta.cinnamon.emotes.Emotes.LoriHi
                                    )

                                    actionRow(button)
                                }
                            )
                        }
                    }

                    val toBeSent = MessageUtils.generateMessageOrFallbackIfInvalid(
                        i18nContext,
                        warnMessage,
                        listOf(message.author, guild, message.channel),
                        guild,
                        generationErrorMessageI18nKey = I18nKeysData.InvalidMessages.InviteBlocked
                    )

                    message.guildChannel.sendMessage(toBeSent).queue()
                }

                return true
            }
        }
        return false
    }

    private fun stripAnythingThatCouldCauseIssues(contentToBeStripped: String): String {
        var strippedContent = contentToBeStripped
            .stripCodeMarks()
            .replace("\u200B", "")
            .replace("\\", "/")
            .replace(Regex("/+"), "/")
            .replace("../", "")

        // Replace any lookalike with the original character
        for ((original, lookalikes) in unicodeLookalikes) {
            for (lookalike in lookalikes) {
                strippedContent = strippedContent.replace(lookalike, original)
            }
        }
        return strippedContent
    }

    private fun checkMessage(message: MessageWrapper): List<Matcher> {
        val content = stripAnythingThatCouldCauseIssues(message.contentRaw)

        val validMatchers = mutableListOf<Matcher>()
        val contentMatcher = getMatcherIfHasInviteLink(content)
        if (contentMatcher != null)
            validMatchers.add(contentMatcher)

        val embeds = message.embeds
        if (!isYouTubeLink(content)) {
            for (embed in embeds) {
                val descriptionMatcher = getMatcherIfHasInviteLink(embed.description)
                if (descriptionMatcher != null)
                    validMatchers.add(descriptionMatcher)

                val titleMatcher = getMatcherIfHasInviteLink(embed.title)
                if (titleMatcher != null)
                    validMatchers.add(titleMatcher)

                val urlMatcher = getMatcherIfHasInviteLink(embed.url)
                if (urlMatcher != null)
                    validMatchers.add(urlMatcher)

                val footerMatcher = getMatcherIfHasInviteLink(embed.footer?.text)
                if (footerMatcher != null)
                    validMatchers.add(footerMatcher)

                val authorNameMatcher = getMatcherIfHasInviteLink(embed.author?.name)
                if (authorNameMatcher != null)
                    validMatchers.add(authorNameMatcher)

                val authorUrlMatcher = getMatcherIfHasInviteLink(embed.author?.url)
                if (authorUrlMatcher != null)
                    validMatchers.add(authorUrlMatcher)

                for (field in embed.fields) {
                    val fieldMatcher = getMatcherIfHasInviteLink(field.value)
                    if (fieldMatcher != null)
                        validMatchers.add(fieldMatcher)
                }
            }
        }

        return validMatchers
    }

    sealed class MessageWrapper {
        abstract val contentRaw: String
        abstract val embeds: List<MessageEmbed>

        class Message(val message: net.dv8tion.jda.api.entities.Message) : MessageWrapper() {
            override val contentRaw
                get() = message.contentRaw
            override val embeds
                get() ```kotlin
                get() = message.embeds
        }

        class MessageSnapshot(val messageSnapshot: net.dv8tion.jda.api.entities.messages.MessageSnapshot) : MessageWrapper() {
            override val contentRaw
                get() = messageSnapshot.contentRaw
            override val embeds
                get() = messageSnapshot.embeds
        }
    }
}
``` ```kotlin
                get() = messageSnapshot.embeds
        }
    }
}
