package org.example.service

import org.springframework.stereotype.Component
import java.awt.Graphics2D
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.util.Base64
import javax.imageio.ImageIO

@Component
class KeypadImageComposer {

    fun compose(shuffledItems: List<Any>): String {
        val imagePaths = shuffledItems.map { item ->
            when (item) {
                is Int -> "_${item}.png"
                "EMPTY" -> "_blank.png"
                else -> throw IllegalArgumentException("Invalid item in shuffledItems")
            }
        }

        val images = imagePaths.map { path -> loadImageFromClasspath(path) }
        val combinedImage = combineImagesInGrid(images, GRID_COLUMNS, GRID_ROWS)
        return encodeImageToBase64(combinedImage)
    }

    private fun loadImageFromClasspath(path: String): BufferedImage {
        val resourceAsStream: InputStream = javaClass.classLoader.getResourceAsStream("keypad_images/$path")
            ?: throw IllegalArgumentException("Image file not found: $path")
        return ImageIO.read(resourceAsStream)
    }

    private fun combineImagesInGrid(images: List<BufferedImage>, columns: Int, rows: Int): BufferedImage {
        require(images.size == columns * rows) { "The number of images does not match the grid size." }

        val imageWidth = images[0].width
        val imageHeight = images[0].height
        val combinedWidth = imageWidth * columns
        val combinedHeight = imageHeight * rows

        val combinedImage = BufferedImage(combinedWidth, combinedHeight, BufferedImage.TYPE_INT_ARGB)
        val g2d: Graphics2D = combinedImage.createGraphics()

        for (i in 0 until rows) {
            for (j in 0 until columns) {
                val image = images[i * columns + j]
                g2d.drawImage(image, j * imageWidth, i * imageHeight, null)
            }
        }
        g2d.dispose()

        return combinedImage
    }

    private fun encodeImageToBase64(image: BufferedImage): String {
        val baos = ByteArrayOutputStream()
        ImageIO.write(image, "png", baos)
        val imageBytes = baos.toByteArray()
        return Base64.getEncoder().encodeToString(imageBytes)
    }

    companion object {
        private const val GRID_COLUMNS = 3
        private const val GRID_ROWS = 4
    }
}
