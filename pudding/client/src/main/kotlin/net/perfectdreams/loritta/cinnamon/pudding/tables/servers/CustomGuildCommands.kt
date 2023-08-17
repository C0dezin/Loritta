package net.perfectdreams.loritta.cinnamon.pudding.tables.servers

import net.perfectdreams.loritta.serializable.CustomCommandCodeType
import org.jetbrains.exposed.dao.id.LongIdTable

object CustomGuildCommands : LongIdTable() {
	val guild = reference("guild", ServerConfigs).index()
	val label = text("label")
	val enabled = bool("enabled")
	val codeType = enumeration("code_type", CustomCommandCodeType::class)
	val code = text("code")
}