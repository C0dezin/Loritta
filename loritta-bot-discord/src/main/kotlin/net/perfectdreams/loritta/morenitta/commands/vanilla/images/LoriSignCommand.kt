package net.perfectdreams.loritta.morenitta.commands.vanilla.images

import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.commands.vanilla.images.base.GabrielaImageServerCommandBase

class LoriSignCommand(m: LorittaBot) : GabrielaImageServerCommandBase(
	m,
	listOf("lorisign", "lorittasign", "loriplaca", "lorittaplaca"),
	1,
	"commands.command.lorisign.description",
	"/api/v1/images/lori-sign",
	"lori_sign.png",
	slashCommandName = "lorisign"
)