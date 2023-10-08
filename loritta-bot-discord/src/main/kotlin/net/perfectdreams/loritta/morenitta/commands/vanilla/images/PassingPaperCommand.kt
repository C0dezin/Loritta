package net.perfectdreams.loritta.morenitta.commands.vanilla.images

import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.commands.vanilla.images.base.GabrielaImageServerCommandBase

class PassingPaperCommand(m: LorittaBot) : GabrielaImageServerCommandBase(
	m,
	listOf("passingpaper", "bilhete", "quizkid"),
	1,
	"commands.command.passingpaper.description",
	"/api/v1/images/passing-paper",
	"passing_paper.png",
	slashCommandName = "passingpaper"
)