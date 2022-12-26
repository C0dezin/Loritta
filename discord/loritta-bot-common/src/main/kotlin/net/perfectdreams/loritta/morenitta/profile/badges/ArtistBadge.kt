package net.perfectdreams.loritta.morenitta.profile.badges

import net.perfectdreams.loritta.morenitta.dao.Profile
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.profile.Badge
import net.perfectdreams.loritta.morenitta.profile.ProfileDesignManager
import net.perfectdreams.loritta.morenitta.profile.ProfileUserInfoData
import net.perfectdreams.loritta.morenitta.utils.config.FanArtArtist
import java.util.*

class ArtistBadge(val loritta: LorittaBot) : Badge.LorittaBadge(
	UUID.fromString("81788d4a-7e6c-415f-8832-d55573f8c40b"),
	ProfileDesignManager.I18N_BADGES_PREFIX.Artist.Title,
	ProfileDesignManager.I18N_BADGES_PREFIX.Artist.Description,
	"artist_badge.png",
	25
) {
	override suspend fun checkIfUserDeservesBadge(user: ProfileUserInfoData, profile: Profile, mutualGuilds: Set<Long>): Boolean {
		return loritta.fanArtArtists.any { it.socialNetworks?.filterIsInstance<FanArtArtist.SocialNetwork.DiscordSocialNetwork>()?.firstOrNull()?.id == user.id.toString() }
	}
}