package net.perfectdreams.loritta.morenitta.utils.extensions

import net.perfectdreams.loritta.morenitta.utils.MiscUtils
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageEmbed

fun String?.isValidUrl(): Boolean {
	if (this == null)
		return false
	else if (this.length > MessageEmbed.URL_MAX_LENGTH)
		return false
	else if (!EmbedBuilder.URL_PATTERN.matcher(this).matches())
		return false
	return true
}

fun String.stripLinks() = MiscUtils.stripLinks(this)