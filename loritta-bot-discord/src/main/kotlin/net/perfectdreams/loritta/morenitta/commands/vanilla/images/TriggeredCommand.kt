package net.perfectdreams.loritta.morenitta.commands.vanilla.images

import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.commands.AbstractCommand
import net.perfectdreams.loritta.morenitta.commands.CommandContext
import net.perfectdreams.loritta.morenitta.gifs.GifSequenceWriter
import net.perfectdreams.loritta.morenitta.utils.Constants
import net.perfectdreams.loritta.morenitta.utils.MiscUtils
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import net.perfectdreams.loritta.morenitta.api.commands.Command
import net.perfectdreams.loritta.morenitta.utils.extensions.readImage
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.stream.FileImageOutputStream

class TriggeredCommand(loritta: LorittaBot) : AbstractCommand(loritta, "triggered", category = net.perfectdreams.loritta.common.commands.CommandCategory.IMAGES) {
	override fun getDescriptionKey() = LocaleKeyData("commands.command.triggered.description")
	override fun getExamplesKey() = Command.SINGLE_IMAGE_EXAMPLES_KEY

	// TODO: Fix Usage

	override fun needsToUploadFiles(): Boolean {
		return true
	}

	override suspend fun run(context: CommandContext,locale: BaseLocale) {
		val contextImage = context.getImageAt(0) ?: run { Constants.INVALID_IMAGE_REPLY.invoke(context); return; }

		val input = contextImage

		val triggeredLabel = readImage(File(LorittaBot.ASSETS, "triggered.png"))
		// scale

		val subtractW = input.width / 16
		val subtractH = input.height / 16
		val inputWidth = input.width - subtractW
		val inputHeight = input.height - subtractH

		// ogWidth --- input.width
		// ogHeight --- x
		val a1 = triggeredLabel.height * inputWidth
		val labelHeight = a1 / triggeredLabel.width

		val scaledTriggeredLabel = triggeredLabel.getScaledInstance(inputWidth, labelHeight, BufferedImage.SCALE_SMOOTH)

		val base = BufferedImage(inputWidth, inputHeight + scaledTriggeredLabel.getHeight(null), BufferedImage.TYPE_INT_ARGB)
		val tint = BufferedImage(base.width, inputHeight, BufferedImage.TYPE_INT_ARGB)

		val color = Color(255, 0, 0, 60)
		val graphics = base.graphics
		val tintGraphics = tint.graphics
		tintGraphics.color = color
		tintGraphics.fillRect(0, 0, tint.width, tint.height)

		var fileName = LorittaBot.TEMP + "triggered-" + System.currentTimeMillis() + ".gif"
		val outputFile = File(fileName)
		var output = FileImageOutputStream(outputFile)

		val writer = GifSequenceWriter(output, BufferedImage.TYPE_INT_ARGB, 4, true)

		for (i in 0..5) {
			var offsetX = LorittaBot.RANDOM.nextInt(0, subtractW)
			var offsetY = LorittaBot.RANDOM.nextInt(0, subtractH)

			val subimage = input.getSubimage(offsetX, offsetY, inputWidth, inputHeight)

			graphics.drawImage(subimage, 0, 0, null)

			graphics.drawImage(tint, 0, 0, null)
			graphics.drawImage(scaledTriggeredLabel, 0, inputHeight, null)
			writer.writeToSequence(base)
		}

		writer.close()
		output.close()

		loritta.gifsicle.optimizeGIF(outputFile)
		context.sendFile(outputFile, "triggered.gif", context.getAsMention(true))
		outputFile.delete()
	}
}