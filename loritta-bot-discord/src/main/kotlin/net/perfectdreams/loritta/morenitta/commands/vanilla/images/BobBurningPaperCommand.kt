package net.perfectdreams.loritta.morenitta.commands.vanilla.images

import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.commands.vanilla.images.base.GabrielaImageServerCommandBase

class BobBurningPaperCommand(m: LorittaBot) : GabrielaImageServerCommandBase(
	m,
	listOf("bobburningpaper", "bobpaperfire", "bobfire", "bobpapelfogo", "bobfogo"),
	1,
	"commands.command.bobfire.description",
	"/api/v1/images/bob-burning-paper",
	"bobfire.png",
	slashCommandName = "bobburningpaper"
)