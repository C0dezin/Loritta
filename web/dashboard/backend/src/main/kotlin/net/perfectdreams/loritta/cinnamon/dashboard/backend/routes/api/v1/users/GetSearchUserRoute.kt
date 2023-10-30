package net.perfectdreams.loritta.cinnamon.dashboard.backend.routes.api.v1.users

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import net.perfectdreams.loritta.cinnamon.dashboard.backend.LorittaDashboardBackend
import net.perfectdreams.loritta.cinnamon.dashboard.backend.routes.api.v1.RequiresAPIDiscordLoginRoute
import net.perfectdreams.loritta.cinnamon.dashboard.backend.utils.respondJson
import net.perfectdreams.loritta.serializable.UserId
import net.perfectdreams.loritta.temmiewebsession.LorittaJsonWebSession
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth

class GetSearchUserRoute(m: LorittaDashboardBackend) : RequiresAPIDiscordLoginRoute(m, "/api/v1/users/search") {
    override suspend fun onAuthenticatedRequest(
        call: ApplicationCall,
        discordAuth: TemmieDiscordAuth,
        userIdentification: LorittaJsonWebSession.UserIdentification
    ) {
        val id = call.parameters["id"]?.toLong()
        val tag = call.parameters["tag"]
        val pomelo = call.parameters["pomelo"]

        if (id != null) {
            val cachedUserInfo = m.pudding.users.getCachedUserInfoById(UserId(id))

            if (cachedUserInfo != null) {
                call.respondJson(cachedUserInfo)
            } else {
                // TODO: Query via Discord's API too
                call.respondText("", status = HttpStatusCode.NotFound)
            }
        } else if (tag != null) {
            val (name, discriminator) = tag.split("#")

            val cachedUserInfo = m.pudding.users.getCachedUserInfoByNameAndDiscriminator(name, discriminator)

            if (cachedUserInfo != null) {
                call.respondJson(cachedUserInfo)
            } else {
                // TODO: Query via Discord's API too
                call.respondText("", status = HttpStatusCode.NotFound)
            }
        } else if (pomelo != null) {
            val cachedUserInfo = m.pudding.users.getCachedUserInfoByPomeloName(pomelo)

            if (cachedUserInfo != null) {
                call.respondJson(cachedUserInfo)
            } else {
                // TODO: Query via Discord's API too
                call.respondText("", status = HttpStatusCode.NotFound)
            }
        } else  error("Tried to search users, but I couldn't find any parameters to do it!")
    }
}