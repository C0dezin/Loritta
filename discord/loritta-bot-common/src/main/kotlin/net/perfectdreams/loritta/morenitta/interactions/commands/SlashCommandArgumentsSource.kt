package net.perfectdreams.loritta.morenitta.interactions.commands

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.perfectdreams.loritta.morenitta.interactions.commands.options.DiscordOptionReference
import net.perfectdreams.loritta.morenitta.interactions.commands.options.ImageReference
import net.perfectdreams.loritta.morenitta.interactions.commands.options.ImageReferenceOrAttachmentDiscordOptionReference
import net.perfectdreams.loritta.morenitta.interactions.commands.options.OptionReference

abstract class SlashCommandArgumentsSource {
    abstract operator fun <T> get(argument: OptionReference<T>): T

    class SlashCommandArgumentsEventSource(val event: SlashCommandInteractionEvent) : SlashCommandArgumentsSource() {
        override fun <T> get(argument: OptionReference<T>): T {
            when (argument) {
                is DiscordOptionReference -> {
                    val option = event.getOption(argument.name)

                    if (option == null) {
                        if (argument.required)
                            throw RuntimeException("Missing argument ${argument.name}!")

                        return null as T
                    }

                    return argument.get(option)
                }

                is ImageReferenceOrAttachmentDiscordOptionReference -> {
                    val imageRefOption = event.getOption(argument.name + "_data")
                    val attachmentOption = event.getOption(argument.name + "_attachment")

                    val imageRef = imageRefOption?.asString
                    val attachment = attachmentOption?.asAttachment

                    return ImageReference(imageRef, attachment) as T
                }
            }
        }
    }

    class SlashCommandArgumentsMapSource(val event: Map<OptionReference<*>, Any?>) : SlashCommandArgumentsSource() {
        override fun <T> get(argument: OptionReference<T>): T {
            when (argument) {
                is DiscordOptionReference -> {
                    return event[argument] as T
                }

                is ImageReferenceOrAttachmentDiscordOptionReference -> {
                    return event[argument] as T
                }
            }
        }
    }
}