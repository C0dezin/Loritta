package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.utils.packtracker

import dev.kord.common.entity.ButtonStyle
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import net.perfectdreams.discordinteraktions.common.builder.message.actionRow
import net.perfectdreams.discordinteraktions.common.builder.message.embed
import dev.kord.core.entity.User
import net.perfectdreams.discordinteraktions.common.utils.field
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.common.utils.LorittaColors
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.BarebonesSingleUserComponentData
import net.perfectdreams.loritta.cinnamon.discord.utils.ComponentExecutorIds
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.utils.declarations.PackageCommand
import net.perfectdreams.loritta.cinnamon.discord.interactions.components.*
import net.perfectdreams.loritta.cinnamon.discord.utils.ComponentDataUtils
import net.perfectdreams.loritta.cinnamon.discord.utils.CorreiosUtils
import net.perfectdreams.loritta.cinnamon.discord.utils.correios.entities.CorreiosEvento
import net.perfectdreams.loritta.cinnamon.discord.utils.correios.entities.eventTypeWithStatus
import net.perfectdreams.loritta.cinnamon.discord.utils.toKordColor
import net.perfectdreams.loritta.serializable.UserId

class SelectPackageSelectMenuExecutor(
    loritta: LorittaBot
) : CinnamonSelectMenuExecutor(loritta) {
    companion object : SelectMenuExecutorDeclaration(ComponentExecutorIds.SELECT_PACKAGE_SELECT_MENU_EXECUTOR)

    override suspend fun onSelect(
        user: User,
        context: ComponentContext,
        values: List<String>
    ) {
        context.deferUpdateMessage()

        val viewingTrackingId = values.first()

        val packageEvents = context.loritta.pudding.packagesTracking.getCorreiosPackageEvents(viewingTrackingId)
            .map { Json.decodeFromString<CorreiosEvento>(it) }

        val trackingIdsTrackedByUser = context.loritta.pudding.packagesTracking.getTrackedCorreiosPackagesByUser(UserId(context.user.id.value))

        val lastEvent = packageEvents.maxByOrNull { it.dtHrCriado }

        context.updateMessage {
            embed {
                title = "`${viewingTrackingId}`"
                color = LorittaColors.CorreiosYellow.toKordColor()

                if (lastEvent != null) {
                    val eventTypeWithStatus = lastEvent.eventTypeWithStatus

                    field(
                        "${CorreiosUtils.getEmoji(eventTypeWithStatus)} ${lastEvent.descricao}",
                        CorreiosUtils.formatEvent(lastEvent),
                        false
                    )

                    image = CorreiosUtils.getImage(eventTypeWithStatus)
                }
            }

            actionRow {
                interactiveButton(
                    ButtonStyle.Primary,
                    GoBackToPackageListButtonClickExecutor,
                    ComponentDataUtils.encode(
                        BarebonesSingleUserComponentData(context.user.id)
                    )
                ) {
                    this.loriEmoji = Emotes.ChevronLeft
                }

                interactiveButton(
                    ButtonStyle.Primary,
                    TrackPackageButtonClickExecutor,
                    ComponentDataUtils.encode(
                        TrackPackageData(context.user.id, viewingTrackingId)
                    )
                ) {
                    this.label = context.i18nContext.get(PackageCommand.I18N_PREFIX.List.TrackPackage)
                    this.loriEmoji = Emotes.LoriReading
                }
            }

            actionRow {
                selectMenu(SelectPackageSelectMenuExecutor) {
                    for (trackingId in trackingIdsTrackedByUser) {
                        option(trackingId, trackingId) {
                            loriEmoji = Emotes.Correios
                            default = trackingId == viewingTrackingId
                        }
                    }
                }
            }
        }
    }
}