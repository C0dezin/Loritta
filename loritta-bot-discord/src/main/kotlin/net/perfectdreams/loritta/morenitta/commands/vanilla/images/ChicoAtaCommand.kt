package net.perfectdreams.loritta.morenitta.commands.vanilla.images

import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.commands.vanilla.images.base.GabrielaImageServerCommandBase

class ChicoAtaCommand(m: LorittaBot) : GabrielaImageServerCommandBase(
	m,
	listOf("chicoata"),
	1,
	"commands.command.chicoata.description",
	"/api/v1/images/chico-ata",
	"chico_ata.png",
	slashCommandName = "brmemes ata chico"
)