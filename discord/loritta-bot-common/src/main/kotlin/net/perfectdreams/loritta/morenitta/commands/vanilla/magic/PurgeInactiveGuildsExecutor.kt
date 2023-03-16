package net.perfectdreams.loritta.morenitta.commands.vanilla.magic

import net.perfectdreams.loritta.morenitta.tables.GuildProfiles
import net.perfectdreams.loritta.morenitta.tables.Mutes
import net.perfectdreams.loritta.morenitta.tables.ServerConfigs
import net.perfectdreams.loritta.morenitta.api.commands.CommandContext
import net.perfectdreams.loritta.morenitta.messages.LorittaReply
import net.perfectdreams.loritta.morenitta.tables.servers.Giveaways
import net.perfectdreams.loritta.morenitta.utils.DiscordUtils
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.selectAll

object PurgeInactiveGuildsExecutor : LoriToolsCommand.LoriToolsExecutor {
	override val args = "purge inactive guilds <now>"

	override fun executes(): suspend CommandContext.() -> Boolean = task@{
		if (args.getOrNull(0) != "purge")
			return@task false
		if (args.getOrNull(1) != "inactive")
			return@task false
		if (args.getOrNull(2) != "guilds")
			return@task false

		val badGuildData = mutableListOf<Long>()

		loritta.pudding.transaction {
			ServerConfigs.selectAll().forEach {
				if (it[ServerConfigs.id].value != -1L) { // -1 = coisas na DM
					val shardId = DiscordUtils.getLorittaClusterForGuildId(loritta, it[ServerConfigs.id].value)

					if (shardId.id == loritta.lorittaCluster.id) {
						val guild = loritta.lorittaShards.getGuildById(it[ServerConfigs.id].value)

						if (guild == null)
							badGuildData.add(it[ServerConfigs.id].value)
					}
				}
			}
		}

		if (args.getOrNull(3) == "now") {
			reply(
					LorittaReply(
							"Deletando guilds inativas..."
					)
			)

			val idList = badGuildData.chunked(30_000)

			for (ids in idList) {
				loritta.pudding.transaction {
					GuildProfiles.deleteWhere {
						GuildProfiles.guildId inList ids
					}

					ServerConfigs.deleteWhere { ServerConfigs.id inList ids }

					Giveaways.deleteWhere {
						Giveaways.guildId inList ids
					}

					Mutes.deleteWhere {
						Mutes.guildId inList ids
					}
				}
			}

			reply(
					LorittaReply(
							"Feito! Todas as guilds inativas foram deletadas!"
					)
			)
		} else {
			reply(
					LorittaReply(
							"Existem **${badGuildData.size} guilds** inativas (Deveriam existir no cluster atual... mas cadê?)"
					)
			)
		}

		return@task true
	}
}
