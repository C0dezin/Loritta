package net.perfectdreams.loritta.cinnamon.pudding.tables

import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Column

object SentYouTubeVideoIds : IdTable<String>() {
	val channelId = text("channel").index()
	val videoId = text("video")
	override val id: Column<EntityID<String>> = videoId.entityId()

	val receivedAt = long("received_at")
}