package net.perfectdreams.loritta.morenitta.commands.vanilla.images

import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.commands.vanilla.images.base.GabrielaImageServerCommandBase

class AttackOnHeartCommand(m: LorittaBot) : GabrielaImageServerCommandBase(
	m,
	listOf("attackonheart"),
	1,
	"commands.command.attackonheart.description",
	"/api/v1/videos/attack-on-heart",
	"attack_on_heart.mp4",
	category = net.perfectdreams.loritta.common.commands.CommandCategory.VIDEOS,
	slashCommandName = "attackonheart"
)