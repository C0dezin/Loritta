package net.perfectdreams.loritta.cinnamon.discord.interactions.commands.customoptions

import dev.kord.common.entity.CommandArgument
import dev.kord.common.entity.DiscordAttachment
import dev.kord.common.entity.DiscordInteraction
import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.rest.Image
import dev.kord.rest.builder.interaction.BaseInputChatBuilder
import dev.kord.rest.builder.interaction.attachment
import dev.kord.rest.builder.interaction.string
import net.perfectdreams.discordinteraktions.common.commands.options.CommandOptionBuilder
import net.perfectdreams.discordinteraktions.common.commands.options.InteraKTionsCommandOption
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.discord.utils.ContentTypeUtils
import net.perfectdreams.loritta.cinnamon.discord.utils.ContextStringToUserInfoConverter
import net.perfectdreams.loritta.cinnamon.discord.utils.UserUtils
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.common.images.URLImageReference
import net.perfectdreams.loritta.i18n.I18nKeysData
import kotlin.streams.toList

// ===[ OPTION ]===
data class ImageReferenceOrAttachmentIntermediaryData(
    val dataValue: String?,
    val attachmentValue: DiscordAttachment?,
    val required: Boolean
) {
    suspend fun get(context: ApplicationCommandContext): URLImageReference? {
        // Attachments take priority
        if (attachmentValue != null) {
            if (attachmentValue.contentType.value in ContentTypeUtils.COMMON_IMAGE_CONTENT_TYPES)
                return URLImageReference(attachmentValue.url)

            // This ain't an image dawg! Because the user explicitly provided the image, then let's fail
            context.fail(
                context.i18nContext.get(I18nKeysData.Commands.NoValidImageFound),
                Emotes.LoriSob
            )
        } else if (dataValue != null) {
            // Now check if it is a valid thing!
            // First, we will try matching via user mentions or user IDs
            val cachedUserInfo = ContextStringToUserInfoConverter.convert(
                context,
                dataValue
            )

            if (cachedUserInfo != null) {
                val icon = UserUtils.createUserAvatarOrDefaultUserAvatar(
                    context.loritta.interaKTions.kord,
                    Snowflake(cachedUserInfo.id.value),
                    cachedUserInfo.avatarId,
                    cachedUserInfo.discriminator
                )

                return URLImageReference(icon.cdnUrl.toUrl {
                    this.format = Image.Format.PNG
                    this.size = Image.Size.Size128
                })
            }

            if (dataValue.startsWith("http")) {
                // It is a URL!
                // TODO: Use a RegEx to check if it is a valid URL
                return URLImageReference(dataValue)
            }

            // It is a emote!
            // Discord emotes always starts with "<" and ends with ">"
            return if (dataValue.startsWith("<") && dataValue.endsWith(">")) {
                val emoteId = dataValue.substringAfterLast(":").substringBefore(">")
                URLImageReference("https://cdn.discordapp.com/emojis/${emoteId}.png?v=1")
            } else {
                // If not, we are going to handle it as if it were a Unicode emoji
                val emoteId = dataValue.codePoints().toList()
                    .joinToString(separator = "-") { String.format("\\u%04x", it).substring(2) }
                URLImageReference("https://abs.twimg.com/emoji/v2/72x72/$emoteId.png")
            }
        }

        // If no image was found, we will try to find the first recent message in this chat
        val channelId = context.channelId
        try {
            val messages = context.loritta.rest.channel.getMessages(
                channelId,
                null,
                100
            )

            // Sort from the newest message to the oldest message
            val attachmentUrl = messages.sortedByDescending { it.id.timestamp }
                .flatMap { it.attachments }
                .firstOrNull {
                    // Only get filenames ending with "image" extensions
                    it.contentType.value in ContentTypeUtils.COMMON_IMAGE_CONTENT_TYPES
                }?.url

            if (attachmentUrl != null) {
                // Found a valid URL, let's go!
                return URLImageReference(attachmentUrl)
            }
        } catch (e: Exception) {
            // TODO: Catch the "permission required" exception and show a nice message
            e.printStackTrace()
        }

        if (required) {
            context.fail(
                context.i18nContext.get(I18nKeysData.Commands.NoValidImageFound),
                Emotes.LoriSob
            )
        }

        return null
    }
}

class ImageReferenceOrAttachmentOption(
    override val name: String,
    val required: Boolean
) : InteraKTionsCommandOption<ImageReferenceOrAttachmentIntermediaryData> {
    override fun register(builder: BaseInputChatBuilder) {
        builder.string(name + "_data", "Image, URL or Emoji") {
            this.required = false
        }

        builder.attachment(name + "_attachment", "Image Attachment") {
            this.required = false
        }
    }

    override fun parse(
        kord: Kord,
        args: List<CommandArgument<*>>,
        interaction: DiscordInteraction
    ): ImageReferenceOrAttachmentIntermediaryData {
        val dataValue = args.firstOrNull { it.name == name + "_data" }?.value as String?
        val attachmentValue = args.firstOrNull { it.name == name + "_attachment" }?.value as Snowflake?

        return ImageReferenceOrAttachmentIntermediaryData(
            dataValue,
            attachmentValue.let { interaction.data.resolved.value?.attachments?.value?.get(it) },
            required
        )
    }
}

// ===[ BUILDER ]===
class ImageReferenceOrAttachmentOptionBuilder(
    override val name: String,
    override val required: Boolean
) : CommandOptionBuilder<ImageReferenceOrAttachmentIntermediaryData, ImageReferenceOrAttachmentIntermediaryData>() {
    override fun build() = ImageReferenceOrAttachmentOption(
        name,
        required
    )
}