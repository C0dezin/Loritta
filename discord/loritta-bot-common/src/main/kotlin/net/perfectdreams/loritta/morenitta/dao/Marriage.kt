package net.perfectdreams.loritta.morenitta.dao

import net.perfectdreams.loritta.cinnamon.pudding.tables.Marriages
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class Marriage(id: EntityID<Long>) : LongEntity(id) {
	companion object : LongEntityClass<Marriage>(Marriages)

	var user1 by Marriages.user1
	var user2 by Marriages.user2
	var marriedSince by Marriages.marriedSince
}