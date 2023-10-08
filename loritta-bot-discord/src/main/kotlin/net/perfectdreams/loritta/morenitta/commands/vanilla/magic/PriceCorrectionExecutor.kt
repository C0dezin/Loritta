package net.perfectdreams.loritta.morenitta.commands.vanilla.magic

import net.perfectdreams.loritta.cinnamon.pudding.tables.Profiles
import net.perfectdreams.loritta.morenitta.api.commands.CommandContext
import net.perfectdreams.loritta.morenitta.messages.LorittaReply
import net.perfectdreams.loritta.cinnamon.pudding.tables.BackgroundPayments
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update

object PriceCorrectionExecutor : LoriToolsCommand.LoriToolsExecutor {
	override val args = "background_price_correction"

	override fun executes(): suspend CommandContext.() -> Boolean = task@{
		if (args.getOrNull(0) != "background_price_correction")
			return@task false

		loritta.pudding.transaction {
			BackgroundPayments.selectAll().forEach {
				if (it[BackgroundPayments.cost] == 20_000L) {
					BackgroundPayments.update({ BackgroundPayments.id eq it[BackgroundPayments.id] }) {
						it[cost] = 10_000L
					}

					Profiles.update({ Profiles.id eq it[BackgroundPayments.userId] }) {
						with(SqlExpressionBuilder) {
							// 20_000 - 10_000 = 10_000
							it.update(money, money + 10_000L)
						}
					}
				}
				if (it[BackgroundPayments.cost] == 40_000L) {
					BackgroundPayments.update({ BackgroundPayments.id eq it[BackgroundPayments.id] }) {
						it[cost] = 25_000L
					}

					Profiles.update({ Profiles.id eq it[BackgroundPayments.userId] }) {
						with(SqlExpressionBuilder) {
							// 40_000 - 25_000 = 15_000
							it.update(money, money + 15_000L)
						}
					}
				}
			}
		}

		reply(
				LorittaReply(
						"Preços corrigidos!"
				)
		)
		return@task true
	}
}