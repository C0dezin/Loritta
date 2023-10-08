package net.perfectdreams.loritta.morenitta.commands.vanilla.images

import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.commands.vanilla.images.base.GabrielaImageServerCommandBase

class QuadroCommand(m: LorittaBot) : GabrielaImageServerCommandBase(
	m,
	listOf("quadro", "frame", "picture", "wolverine"),
	1,
	"commands.command.wolverine.description",
	"/api/v1/images/wolverine-frame",
	"wolverine_frame.png",
	slashCommandName = "wolverineframe"
)