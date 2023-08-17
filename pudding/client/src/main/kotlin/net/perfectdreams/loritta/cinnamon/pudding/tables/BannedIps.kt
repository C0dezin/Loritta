package net.perfectdreams.loritta.cinnamon.pudding.tables

import org.jetbrains.exposed.dao.id.LongIdTable

object BannedIps : LongIdTable() {
	val ip = text("ip").index()
	val bannedAt = long("banned_at")
	val reason = text("reason").nullable()
}