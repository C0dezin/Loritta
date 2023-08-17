package net.perfectdreams.loritta.morenitta.dao

import net.perfectdreams.loritta.cinnamon.pudding.tables.Mutes
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class Mute(id: EntityID<Long>) : LongEntity(id) {
	companion object : LongEntityClass<Mute>(Mutes)

	var guildId by Mutes.guildId
	var userId by Mutes.userId
	var punishedById by Mutes.punishedById
	var content by Mutes.content
	var receivedAt by Mutes.receivedAt
	var isTemporary by Mutes.isTemporary
	var expiresAt by Mutes.expiresAt
	var userTimedOutUntil by Mutes.userTimedOutUntil
}