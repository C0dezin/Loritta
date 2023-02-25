package net.perfectdreams.loritta.morenitta.commands.vanilla.`fun`

import dev.minn.jda.ktx.messages.InlineMessage
import dev.minn.jda.ktx.messages.MessageCreate
import mu.KotlinLogging
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.Role
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.common.entities.LorittaEmote
import net.perfectdreams.loritta.common.entities.UnicodeEmote
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.utils.ColorUtils
import net.perfectdreams.loritta.common.utils.Emotes
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.interactions.modals.options.modalString
import net.perfectdreams.loritta.morenitta.interactions.modals.options.optionalModalString
import net.perfectdreams.loritta.morenitta.messages.LorittaReply
import net.perfectdreams.loritta.morenitta.platform.discord.legacy.commands.DiscordAbstractCommandBase
import net.perfectdreams.loritta.morenitta.platform.discord.legacy.commands.DiscordCommandContext
import net.perfectdreams.loritta.morenitta.platform.discord.legacy.entities.DiscordEmote
import net.perfectdreams.loritta.morenitta.utils.*
import net.perfectdreams.loritta.morenitta.utils.extensions.await
import net.perfectdreams.loritta.morenitta.utils.extensions.getGuildMessageChannelById
import net.perfectdreams.loritta.morenitta.utils.extensions.getGuildMessageChannelByName
import net.perfectdreams.loritta.morenitta.utils.giveaway.GiveawayManager
import net.perfectdreams.loritta.serializable.GiveawayRoles
import java.awt.Color
import java.util.*

class GiveawaySetupCommand(loritta: LorittaBot): DiscordAbstractCommandBase(loritta, listOf("giveaway setup", "sorteio setup", "giveaway criar", "sorteio criar", "giveaway create", "sorteio create"), net.perfectdreams.loritta.common.commands.CommandCategory.FUN) {
    companion object {
        private const val LOCALE_PREFIX = "commands.command"
        private val I18N_PREFIX = I18nKeysData.Commands.Command.Giveaway.Setup
        val logger = KotlinLogging.logger { }
    }

    override fun command() = create {
        userRequiredPermissions = listOf(Permission.MESSAGE_MANAGE)

        canUseInPrivateChannel = false

        localizedDescription("$LOCALE_PREFIX.giveaway.description")

        executesDiscord {
            val context = this

            var customGiveawayMessage: String? = null

            if (args.isNotEmpty()) {
                val customMessage = args.joinToString(" ")

                val watermarkedMessage = MessageUtils.watermarkSayMessage(
                    customMessage,
                    context.user,
                    context.locale["$LOCALE_PREFIX.giveaway.giveawayCreatedBy"]
                )

                val message = MessageUtils.generateMessage(watermarkedMessage, listOf(), context.guild, mapOf(), true)

                if (message != null) {
                    context.reply(
                        LorittaReply(
                            message = locale["commands.command.giveaway.giveawayValidCustomMessage"],
                            prefix = Emotes.LORI_TEMMIE
                        )
                    )

                    val giveawayMessage = loritta.giveawayManager.createGiveawayMessage(
                        context.i18nContext,
                        "Exemplo de Giveaway",
                        "Apenas um exemplo!",
                        "\uD83C\uDF89",
                        null,
                        null,
                        null,
                        System.currentTimeMillis() + 120_000,
                        context.guild,
                        watermarkedMessage,
                        null,
                        0,
                        null,
                        null
                    )

                    context.sendMessage(giveawayMessage)
                    customGiveawayMessage = watermarkedMessage
                }
            }

            getGiveawayDecorations(context, locale, GiveawayBuilder().apply { this.customGiveawayMessage = customGiveawayMessage })
        }
    }

    private suspend fun getGiveawayDecorations(context: DiscordCommandContext, locale: BaseLocale, builder: GiveawayBuilder) {
        context.discordMessage.channel.sendMessage(
            MessageCreate {
                styled(
                    context.i18nContext.get(I18N_PREFIX.LetsSetupYourGiveawayStep.LetsSetupYourGiveaway),
                    net.perfectdreams.loritta.cinnamon.emotes.Emotes.LoriHi
                )

                actionRow(
                    loritta.interactivityManager.buttonForUser(context.user, ButtonStyle.PRIMARY, context.i18nContext.get(I18N_PREFIX.LetsSetupYourGiveawayStep.StartSetup)) {
                        val giveawayNameOption = modalString(
                            context.i18nContext.get(I18N_PREFIX.LetsSetupYourGiveawayStep.GiveawayName),
                            TextInputStyle.SHORT,
                            value = context.i18nContext.get(I18N_PREFIX.LetsSetupYourGiveawayStep.GiveawayNamePlaceholder)
                        )
                        val giveawayDescriptionOption = modalString(
                            context.i18nContext.get(I18N_PREFIX.LetsSetupYourGiveawayStep.GiveawayDescription),
                            TextInputStyle.PARAGRAPH,
                            value = context.i18nContext.get(I18N_PREFIX.LetsSetupYourGiveawayStep.GiveawayDescriptionPlaceholder)
                        )
                        val giveawayImageOption = optionalModalString(
                            context.i18nContext.get(I18N_PREFIX.LetsSetupYourGiveawayStep.GiveawayEmbedImage),
                            TextInputStyle.SHORT
                        )
                        val giveawayThumbnailOption = optionalModalString(
                            context.i18nContext.get(I18N_PREFIX.LetsSetupYourGiveawayStep.GiveawayEmbedThumbnail),
                            TextInputStyle.SHORT
                        )
                        val giveawayEmbedColorOption = optionalModalString(
                            context.i18nContext.get(I18N_PREFIX.LetsSetupYourGiveawayStep.GiveawayEmbedColor),
                            TextInputStyle.SHORT,
                            placeholder = "#29a6fe"
                        )

                        it.sendModal(
                            context.i18nContext.get(
                                I18N_PREFIX.LetsSetupYourGiveawayStep.GiveawayInfo),
                            listOf(
                                ActionRow.of(giveawayNameOption.toJDA()),
                                ActionRow.of(giveawayDescriptionOption.toJDA()),
                                ActionRow.of(giveawayImageOption.toJDA()),
                                ActionRow.of(giveawayThumbnailOption.toJDA()),
                                ActionRow.of(giveawayEmbedColorOption.toJDA())
                            )
                        ) { modalContext, args ->
                            modalContext.deferEdit()
                            it.event.message.delete().await()

                            builder.name = args[giveawayNameOption]
                            builder.description = args[giveawayDescriptionOption]
                            builder.imageUrl = args[giveawayImageOption]?.ifBlank { null }
                            builder.thumbnailUrl = args[giveawayThumbnailOption]?.ifBlank { null }
                            builder.color = args[giveawayEmbedColorOption]?.let { ColorUtils.getColorFromString(it) }

                            getGiveawayDuration(context, locale, builder)
                        }
                    }
                )

                addCancelOption(context)
            }
        ).await()
    }

    private suspend fun getGiveawayDuration(context: DiscordCommandContext, locale: BaseLocale, builder: GiveawayBuilder) {
        val message = context.discordMessage.channel.sendMessage(
            MessageCreate {
                styled(
                    locale["commands.command.giveaway.giveawayDuration"],
                    "\uD83E\uDD14"
                )

                addCancelOption(context)
            }
        ).await()

        message.onResponseByAuthor(context) {
            builder.duration = it.message.contentRaw
            message.delete().await()
            getGiveawayReaction(context, locale, builder)
        }
    }

    private suspend fun getGiveawayReaction(context: DiscordCommandContext, locale: BaseLocale, builder: GiveawayBuilder) {
        val message = context.discordMessage.channel.sendMessage(
            MessageCreate {
                styled(
                    locale["$LOCALE_PREFIX.giveaway.giveawayReaction"],
                    "\uD83E\uDD14"
                )

                addCancelOption(context)
            }
        ).await()

        message.onResponseByAuthor(context) {
            // If the message contains a emote, we are going to use it on the giveaway
            // This way we can use any emote as long as the user has Nitro and Loritta shares a server.
            //
            // Before we were extracting using a RegEx pattern,
            val emoteInTheMessage = it.message.mentions.customEmojis.firstOrNull()

            builder.reaction = if (emoteInTheMessage != null)
                DiscordEmote.DiscordEmoteBackedByJdaEmote(emoteInTheMessage)
            else
                UnicodeEmote(it.message.contentRaw)

            message.delete().await()
            getGiveawayChannel(context, locale, builder)
        }
    }

    private suspend fun getGiveawayChannel(context: DiscordCommandContext, locale: BaseLocale, builder: GiveawayBuilder) {
        val message = context.discordMessage.channel.sendMessage(
            MessageCreate {
                styled(
                    locale["$LOCALE_PREFIX.giveaway.giveawayChannel"],
                    "\uD83E\uDD14"
                )

                addCancelOption(context)
            }
        ).await()

        message.onResponseByAuthor(context) {
            val pop = it.message.contentRaw
            var channel: GuildMessageChannel? = null

            val queriedChannel = context.guild.getGuildMessageChannelByName(pop, true)

            if (queriedChannel != null) {
                channel = queriedChannel
            } else {
                val id = pop
                    .replace("<", "")
                    .replace("#", "")
                    .replace(">", "")

                if (id.isValidSnowflake()) {
                    channel = context.guild.getGuildMessageChannelById(id)
                }
            }

            if (channel == null) {
                context.reply(
                    LorittaReply(
                        "Canal inválido!",
                        Constants.ERROR
                    )
                )
                return@onResponseByAuthor
            }

            if (!channel.canTalk()) {
                context.reply(
                    LorittaReply(
                        "Eu não posso falar no canal de texto!",
                        Constants.ERROR
                    )
                )
                return@onResponseByAuthor
            }

            if (!channel.canTalk(context.member!!)) {
                context.reply(
                    LorittaReply(
                        "Você não pode falar no canal de texto!",
                        Constants.ERROR
                    )
                )
                return@onResponseByAuthor
            }

            val lorittaAsMember = context.guild.selfMember

            if (!lorittaAsMember.hasPermission(channel, Permission.MESSAGE_ADD_REACTION)) {
                context.reply(
                    LorittaReply(
                        "Não tenho permissão para reagir nesse canal!",
                        Constants.ERROR
                    )
                )
                return@onResponseByAuthor
            }

            if (!lorittaAsMember.hasPermission(channel, Permission.MESSAGE_EMBED_LINKS)) {
                context.reply(
                    LorittaReply(
                        "Não tenho permissão para enviar embeds nesse canal!",
                        Constants.ERROR
                    )
                )
                return@onResponseByAuthor
            }

            if (!lorittaAsMember.hasPermission(channel, Permission.MESSAGE_HISTORY)) {
                context.reply(
                    LorittaReply(
                        "Não tenho permissão para ver o histórico desse canal!",
                        Constants.ERROR
                    )
                )
                return@onResponseByAuthor
            }

            builder.channel = channel

            message.delete().await()
            getGiveawayAllowedToParticipateFilteringRoles(context, locale, builder)
        }
    }

    private suspend fun getGiveawayAllowedToParticipateFilteringRoles(context: DiscordCommandContext, locale: BaseLocale, builder: GiveawayBuilder) {
        context.discordMessage.channel.sendMessage(
            MessageCreate {
                styled(
                    context.i18nContext.get(I18N_PREFIX.DoYouWantToAllowSpecificRolesToParticipateStep.DoYouWantToAllowSpecificRolesToParticipate),
                    "\uD83E\uDD14"
                )
                val allowedRoles = builder.allowedRoles
                val deniedRoles = builder.deniedRoles
                if (allowedRoles != null || deniedRoles != null) {
                    if (allowedRoles != null) {
                        if (allowedRoles.isAndCondition) {
                            styled(context.i18nContext.get(GiveawayManager.I18N_PREFIX.NeedsToHaveAllRoles(allowedRoles.roleIds.joinToString { "<@&$it>" })))
                        } else {
                            styled(context.i18nContext.get(GiveawayManager.I18N_PREFIX.NeedsToHaveAnyRoles(allowedRoles.roleIds.joinToString { "<@&$it>" })))
                        }
                    }

                    if (deniedRoles != null) {
                        if (deniedRoles.isAndCondition) {
                            styled(context.i18nContext.get(GiveawayManager.I18N_PREFIX.CantHaveAllRoles(deniedRoles.roleIds.joinToString { "<@&$it>" })))
                        } else {
                            styled(context.i18nContext.get(GiveawayManager.I18N_PREFIX.CantHaveAnyRoles(deniedRoles.roleIds.joinToString { "<@&$it>" })))
                        }
                    }
                } else {
                    styled(
                        context.i18nContext.get(I18N_PREFIX.DoYouWantToAllowSpecificRolesToParticipateStep.CurrentlyAllMembersCanParticipate)
                    )
                }

                allowedMentionTypes = EnumSet.of(Message.MentionType.EMOJI)

                actionRow(
                    loritta.interactivityManager.buttonForUser(context.user, ButtonStyle.PRIMARY, label = context.i18nContext.get(I18N_PREFIX.DoYouWantToAllowSpecificRolesToParticipateStep.ChooseWhatRolesCanParticipate)) {
                        it.event.message.delete().await()

                        val message = context.discordMessage.channel.sendMessage(
                            MessageCreate {
                                styled(
                                    context.i18nContext.get(I18N_PREFIX.DoYouWantToAllowSpecificRolesToParticipateStep.WhatRolesCanParticipate),
                                    "\uD83E\uDD14"
                                )

                                addCancelOption(context)
                            }
                        ).await()

                        message.onResponseByAuthor(context) {
                            val roles = mutableSetOf<Role>()

                            it.message.mentions.roles.forEach {
                                roles.add(it) // Vamos adicionar os cargos marcados, quick and easy
                            }

                            // Tentar pegar pelo nome
                            var roleName = it.message.contentRaw
                            if (it.message.contentRaw.startsWith("@")) {
                                roleName = it.message.contentRaw.substring(1)
                            }

                            val role = context.guild.getRolesByName(roleName, true).firstOrNull()

                            if (role != null)
                                roles.add(role)

                            // Tentar pegar pelos IDs
                            val roleIds = it.message.contentRaw.replace(",", " ").split(" ").mapNotNull { it.toLongOrNull() }
                            val rolesFromIds = roleIds.mapNotNull { context.guild.getRoleById(it) }
                            roles.addAll(rolesFromIds)

                            if (roles.isEmpty()) {
                                context.reply(
                                    LorittaReply(
                                        locale["commands.command.giveaway.giveawayNoValidRoles"],
                                        Constants.ERROR
                                    )
                                )
                                return@onResponseByAuthor
                            }

                            message.delete().await()

                            // If it is only one role, skip the question
                            if (roles.size >= 2) {
                                context.discordMessage.channel.sendMessage(
                                    MessageCreate {
                                        styled(
                                            context.i18nContext.get(I18N_PREFIX.DoYouWantToAllowSpecificRolesToParticipateStep.OkayTheFollowingRolesWillBeUsed(roles.joinToString { it.asMention })),
                                            "\uD83E\uDD14"
                                        )

                                        styled(context.i18nContext.get(I18N_PREFIX.DoYouWantToAllowSpecificRolesToParticipateStep.ToEnterTheGiveawayShouldItHaveOneOrAllRoles))

                                        allowedMentionTypes = EnumSet.of(Message.MentionType.EMOJI)

                                        actionRow(
                                            loritta.interactivityManager.buttonForUser(context.user, ButtonStyle.PRIMARY, context.i18nContext.get(I18N_PREFIX.DoYouWantToAllowSpecificRolesToParticipateStep.ShouldHaveAllRoles)) {
                                                it.event.message.delete().await()

                                                builder.allowedRoles = GiveawayRoles(roles.map { it.idLong }, true)

                                                getGiveawayAllowedToParticipateFilteringRoles(context, locale, builder)
                                            },
                                            loritta.interactivityManager.buttonForUser(context.user, ButtonStyle.PRIMARY, context.i18nContext.get(I18N_PREFIX.DoYouWantToAllowSpecificRolesToParticipateStep.ShouldHaveAnyRole)) {
                                                it.event.message.delete().await()

                                                builder.allowedRoles = GiveawayRoles(roles.map { it.idLong }, false)

                                                getGiveawayAllowedToParticipateFilteringRoles(context, locale, builder)
                                            }
                                        )
                                    }
                                ).await()
                            } else {
                                builder.allowedRoles = GiveawayRoles(roles.map { it.idLong }, true)

                                getGiveawayAllowedToParticipateFilteringRoles(context, locale, builder)
                            }
                        }
                    },
                    loritta.interactivityManager.buttonForUser(context.user, ButtonStyle.PRIMARY, label = context.i18nContext.get(I18N_PREFIX.DoYouWantToAllowSpecificRolesToParticipateStep.ChooseWhatRolesCannotParticipate)) {
                        it.event.message.delete().await()

                        val message = context.discordMessage.channel.sendMessage(
                            MessageCreate {
                                styled(
                                    context.i18nContext.get(I18N_PREFIX.DoYouWantToAllowSpecificRolesToParticipateStep.WhatRolesCannotParticipate),
                                    "\uD83E\uDD14"
                                )

                                addCancelOption(context)
                            }
                        ).await()

                        message.onResponseByAuthor(context) {
                            val roles = mutableSetOf<Role>()

                            it.message.mentions.roles.forEach {
                                roles.add(it) // Vamos adicionar os cargos marcados, quick and easy
                            }

                            // Tentar pegar pelo nome
                            var roleName = it.message.contentRaw
                            if (it.message.contentRaw.startsWith("@")) {
                                roleName = it.message.contentRaw.substring(1)
                            }

                            val role = context.guild.getRolesByName(roleName, true).firstOrNull()

                            if (role != null)
                                roles.add(role)

                            // Tentar pegar pelos IDs
                            val roleIds = it.message.contentRaw.replace(",", " ").split(" ").mapNotNull { it.toLongOrNull() }
                            val rolesFromIds = roleIds.mapNotNull { context.guild.getRoleById(it) }
                            roles.addAll(rolesFromIds)

                            if (roles.isEmpty()) {
                                context.reply(
                                    LorittaReply(
                                        locale["commands.command.giveaway.giveawayNoValidRoles"],
                                        Constants.ERROR
                                    )
                                )
                                return@onResponseByAuthor
                            }

                            message.delete().await()

                            // If it is only one role, skip the question
                            if (roles.size >= 2) {
                                context.discordMessage.channel.sendMessage(
                                    MessageCreate {
                                        styled(
                                            context.i18nContext.get(I18N_PREFIX.DoYouWantToAllowSpecificRolesToParticipateStep.OkayTheFollowingRolesWillBeUsed(roles.joinToString { it.asMention })),
                                            "\uD83E\uDD14"
                                        )

                                        styled(context.i18nContext.get(I18N_PREFIX.DoYouWantToAllowSpecificRolesToParticipateStep.ToEnterTheGiveawayShouldItNotHaveOneOrAllRoles))

                                        allowedMentionTypes = EnumSet.of(Message.MentionType.EMOJI)

                                        actionRow(
                                            loritta.interactivityManager.buttonForUser(context.user, ButtonStyle.PRIMARY, context.i18nContext.get(I18N_PREFIX.DoYouWantToAllowSpecificRolesToParticipateStep.CantHaveAllRoles)) {
                                                it.event.message.delete().await()

                                                builder.deniedRoles = GiveawayRoles(roles.map { it.idLong }, true)

                                                getGiveawayAllowedToParticipateFilteringRoles(context, locale, builder)
                                            },
                                            loritta.interactivityManager.buttonForUser(context.user, ButtonStyle.PRIMARY, context.i18nContext.get(I18N_PREFIX.DoYouWantToAllowSpecificRolesToParticipateStep.CantHaveAnyRole)) {
                                                it.event.message.delete().await()

                                                builder.deniedRoles = GiveawayRoles(roles.map { it.idLong }, false)

                                                getGiveawayAllowedToParticipateFilteringRoles(context, locale, builder)
                                            }
                                        )
                                    }
                                ).await()
                            } else {
                                builder.deniedRoles = GiveawayRoles(roles.map { it.idLong }, false)

                                getGiveawayAllowedToParticipateFilteringRoles(context, locale, builder)
                            }
                        }
                    },
                    loritta.interactivityManager.buttonForUser(context.user, ButtonStyle.SECONDARY, label = "Continuar") {
                        it.event.message.delete().await()

                        getGiveawayWinningRoles(context, locale, builder)
                    }
                )
            }
        ).await()
    }

    private suspend fun getGiveawayWinningRoles(context: DiscordCommandContext, locale: BaseLocale, builder: GiveawayBuilder) {
        context.discordMessage.channel.sendMessage(
            MessageCreate {
                styled(
                    locale["$LOCALE_PREFIX.giveaway.giveawayDoYouWantAutomaticRole"],
                    "\uD83E\uDD14"
                )

                actionRow(
                    loritta.interactivityManager.buttonForUser(context.user, ButtonStyle.PRIMARY, context.i18nContext.get(I18N_PREFIX.GiveawayWinningRoles.GiveRolesToWinner), { emoji = Emoji.fromUnicode("✅") }) {
                        it.event.message.delete().await()

                        val message = context.discordMessage.channel.sendMessage(
                            MessageCreate {
                                styled(
                                    locale["$LOCALE_PREFIX.giveaway.giveawayMentionRoles"],
                                    "\uD83E\uDD14"
                                )

                                addCancelOption(context)
                            }
                        ).await()

                        message.onResponseByAuthor(context) {
                            val roles = mutableSetOf<Role>()

                            it.message.mentions.roles.forEach {
                                roles.add(it) // Vamos adicionar os cargos marcados, quick and easy
                            }

                            // Tentar pegar pelo nome
                            var roleName = it.message.contentRaw
                            if (it.message.contentRaw.startsWith("@")) {
                                roleName = it.message.contentRaw.substring(1)
                            }

                            val role = context.guild.getRolesByName(roleName, true).firstOrNull()

                            if (role != null)
                                roles.add(role)

                            // Tentar pegar pelos IDs
                            val roleIds = it.message.contentRaw.replace(",", " ").split(" ").mapNotNull { it.toLongOrNull() }
                            val rolesFromIds = roleIds.mapNotNull { context.guild.getRoleById(it) }
                            roles.addAll(rolesFromIds)

                            if (roles.isEmpty()) {
                                context.reply(
                                    LorittaReply(
                                        locale["commands.command.giveaway.giveawayNoValidRoles"],
                                        Constants.ERROR
                                    )
                                )
                                return@onResponseByAuthor
                            }

                            for (role in roles) {
                                if (!context.guild.selfMember.canInteract(role) || role.isManaged) {
                                    context.reply(
                                        LorittaReply(
                                            locale["commands.command.giveaway.giveawayCantInteractWithRole", "`${role.name}`"],
                                            Constants.ERROR
                                        )
                                    )
                                    return@onResponseByAuthor
                                }

                                if (context.discordMessage.member?.hasPermission(Permission.MANAGE_ROLES) == false || context.discordMessage.member?.canInteract(role) == false) {
                                    context.reply(
                                        LorittaReply(
                                            locale["commands.command.giveaway.giveawayCantYouInteractWithRole", "`${role.name}`"],
                                            Constants.ERROR
                                        )
                                    )
                                    return@onResponseByAuthor
                                }
                            }

                            message.delete().await()

                            builder.roleIds = roles.map { it.id }

                            getGiveawayWinnerCount(context, locale, builder)
                        }
                    },
                    loritta.interactivityManager.buttonForUser(context.user, ButtonStyle.PRIMARY,  context.i18nContext.get(I18N_PREFIX.GiveawayWinningRoles.DontGiveRolesToWinner), { emoji = Emoji.fromUnicode("\uD83D\uDE45") }) {
                        it.event.message.delete().await()

                        getGiveawayWinnerCount(context, locale, builder)
                    }
                )
            }
        ).await()
    }

    private suspend fun getGiveawayWinnerCount(context: DiscordCommandContext, locale: BaseLocale, builder: GiveawayBuilder) {
        val message = context.discordMessage.channel.sendMessage(
            MessageCreate {
                styled(
                    locale["commands.command.giveaway.giveawayWinnerCount"],
                    "\uD83E\uDD14"
                )

                addCancelOption(context)
            }
        ).await()

        message.onResponseByAuthor(context) {
            val numberOfWinners = it.message.contentRaw.toIntOrNull()

            if (numberOfWinners == null) {
                context.reply(
                    LorittaReply(
                        "Eu não sei o que você colocou aí, mas tenho certeza que não é um número.",
                        Constants.ERROR
                    )
                )
                return@onResponseByAuthor
            }

            if (numberOfWinners !in 1..100) {
                context.reply(
                    LorittaReply(
                        locale["commands.command.giveaway.giveawayWinnerCountNotInRange"],
                        Constants.ERROR
                    )
                )
                return@onResponseByAuthor
            }

            message.delete().await()

            builder.numberOfWinners = numberOfWinners

            buildGiveaway(it.message, context, locale, builder)
        }
    }

    private suspend fun buildGiveaway(message: Message, context: DiscordCommandContext, locale: BaseLocale, builder: GiveawayBuilder) {
        val epoch = TimeUtils.convertToMillisRelativeToNow(builder.duration!!)

        message.delete().await()

        loritta.giveawayManager.spawnGiveaway(
            context.locale,
            context.i18nContext,
            builder.channel!!,
            builder.name!!,
            builder.description!!,
            builder.imageUrl,
            builder.thumbnailUrl,
            builder.color,
            builder.reaction!!.code,
            epoch,
            builder.numberOfWinners!!,
            builder.customGiveawayMessage,
            builder.roleIds,
            builder.allowedRoles,
            builder.deniedRoles
        )
    }

    private fun InlineMessage<*>.addCancelOption(context: DiscordCommandContext) {
        actionRow(
            loritta.interactivityManager.buttonForUser(context.user, ButtonStyle.DANGER, context.i18nContext.get(I18N_PREFIX.CancelConfiguration), { emoji = Emoji.fromCustom("error", 412585701054611458, false) }) {
                it.event.message.delete().await()

                context.reply(
                    LorittaReply(
                        context.locale["$LOCALE_PREFIX.giveaway.giveawaySetupCancelled"]
                    )
                )
            }
        )
    }

    class GiveawayBuilder {
        var customGiveawayMessage: String? = null
        var name: String? = null
        var description: String? = null
        var imageUrl: String? = null
        var thumbnailUrl: String? = null
        var color: Color? = null
        var duration: String? = null
        var reaction: LorittaEmote? = null
        var channel: GuildMessageChannel? = null
        var numberOfWinners: Int? = null
        var roleIds: List<String>? = null
        var allowedRoles: GiveawayRoles? = null
        var deniedRoles: GiveawayRoles? = null
    }
}
