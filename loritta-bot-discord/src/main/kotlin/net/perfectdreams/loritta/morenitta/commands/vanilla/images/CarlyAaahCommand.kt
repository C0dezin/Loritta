package net.perfectdreams.loritta.morenitta.commands.vanilla.images

import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.commands.vanilla.images.base.GabrielaImageServerCommandBase

class CarlyAaahCommand(m: LorittaBot) : GabrielaImageServerCommandBase(
	m,
	listOf("carlyaaah"),
	1,
	"commands.command.carlyaaah.description",
	"/api/v1/videos/carly-aaah",
	"carly_aaah.mp4",
	category = net.perfectdreams.loritta.common.commands.CommandCategory.VIDEOS,
	slashCommandName = "carlyaaah"
)