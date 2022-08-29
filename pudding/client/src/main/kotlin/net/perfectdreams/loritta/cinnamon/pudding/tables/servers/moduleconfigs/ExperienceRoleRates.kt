package net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs

import org.jetbrains.exposed.dao.id.LongIdTable

object ExperienceRoleRates : LongIdTable() {
    val guildId = long("guild").index()
    val role = long("role").index()
    val rate = double("rate")
}