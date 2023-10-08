package net.perfectdreams.loritta.morenitta.website.routes.api.v1.loritta

import com.github.salomonbrys.kotson.*
import com.google.gson.JsonParser
import net.perfectdreams.loritta.morenitta.website.utils.WebsiteUtils
import net.perfectdreams.loritta.morenitta.website.LoriWebCode
import net.perfectdreams.loritta.morenitta.website.WebsiteAPIException
import io.ktor.server.application.ApplicationCall
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receiveText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.cinnamon.pudding.tables.SpicyStacktraces
import net.perfectdreams.sequins.ktor.BaseRoute
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondJson
import org.jetbrains.exposed.sql.insertAndGetId

class PostErrorRoute(val loritta: LorittaBot) : BaseRoute("/api/v1/loritta/error/{type}") {
	override suspend fun onRequest(call: ApplicationCall) {
		val body = withContext(Dispatchers.IO) { call.receiveText() }
		val type = call.parameters["type"]

		val json = JsonParser.parseString(body).obj

		when (type) {
			"spicy" -> {
				val errorCodeId = loritta.newSuspendedTransaction {
					SpicyStacktraces.insertAndGetId {
						it[message] = json["message"].string
						it[spicyHash] = json["spicyHash"].nullString
						it[file] = json["file"].string
						it[line] = json["line"].int
						it[column] = json["column"].int
						it[userAgent] = json["userAgent"].nullString
						it[url] = json["url"].string
						it[spicyPath] = json["spicyPath"].nullString
						it[localeId] = json["localeId"].string
						it[isLocaleInitialized] = json["isLocaleInitialized"].bool
						it[userId] = json["userId"].nullLong
						it[currentRoute] = json["currentRoute"].nullString
						it[stack] = json["stack"].nullString
						it[receivedAt] = System.currentTimeMillis()
					}
				}

				call.respondJson(
						jsonObject(
								"errorCodeId" to errorCodeId.value
						)
				)
			}
			else -> throw WebsiteAPIException(
					HttpStatusCode.NotImplemented,
					WebsiteUtils.createErrorPayload(
							loritta,
							LoriWebCode.MISSING_PAYLOAD_HANDLER,
							"Type $type is not implemented yet!"
					)
			)
		}

		call.respondJson(jsonObject())
	}
}