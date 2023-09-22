package net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils

import net.perfectdreams.loritta.common.utils.placeholders.MessagePlaceholder

/**
 * A Loritta's renderable message placeholder
 */
data class RenderableMessagePlaceholder(
    val placeholder: MessagePlaceholder,
    val replaceWith: String
)