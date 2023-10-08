package net.perfectdreams.loritta.morenitta.gifs

import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.utils.ImageUtils
import net.perfectdreams.loritta.morenitta.utils.extensions.readImage
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.stream.FileImageOutputStream

object CepoDeMadeiraGIF {
	suspend fun getGIF(toUse: BufferedImage): File {
		var ogTeste = ImageUtils.toBufferedImage(toUse.getScaledInstance(45, 45, BufferedImage.SCALE_SMOOTH))

		var fileName = LorittaBot.TEMP + "cepo-" + System.currentTimeMillis() + ".gif"
		var output = FileImageOutputStream(File(fileName))
		val writer = GifSequenceWriter(output, BufferedImage.TYPE_INT_ARGB, 10, true)

		var fogoFx = 0
		for (i in 0..112) {
			val file = File(LorittaBot.ASSETS + "cepo/cepo_${i.toString().padStart(6, '0')}.png")
			if (file.exists()) {
				var ogImage = readImage(File(LorittaBot.ASSETS + "cepo/cepo_${i.toString().padStart(6, '0')}.png"))
				var image = BufferedImage(ogImage.width, ogImage.height, BufferedImage.TYPE_INT_ARGB)
				image.graphics.drawImage(ogImage, 0, 0, null)
				if (i in 0..16) {
					image.graphics.drawImage(ogTeste, 65, 151, null)
				}
				if (i in 17..26) {
					val fogo = readImage(File(LorittaBot.ASSETS + "fogo/fogo_${fogoFx.toString().padStart(6, '0')}.png"))
					image.graphics.drawImage(fogo, 55, 141, null)
					fogoFx++
				}
				writer.writeToSequence(image)
			}
		}
		writer.close()
		output.close()
		return File(fileName)
	}

}