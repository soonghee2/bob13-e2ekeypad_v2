package org.example.service

import java.awt.Graphics2D
import java.awt.Image
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.util.Base64
import javax.imageio.ImageIO

class Images_patcher (private val shuffledItems: List<Any>) {

    fun combineImages(): String {
        // 이미지 파일 경로 설정
        val imagePaths = shuffledItems.map { item ->
            when (item) {
                is Int -> "_${item}.png"
                "EMPTY" -> "_blank.png"
                else -> throw IllegalArgumentException("Invalid item in shuffledItems")
            }
        }

        // 이미지 로딩
        val images = imagePaths.map { path -> loadImageFromClasspath(path) }

        // 그리드 크기 및 개수 설정
        val columns = 4
        val rows = 3

        // 이미지 결합
        val combinedImage = combineImagesInGrid(images, columns, rows)

        // 이미지 인코딩
        return encodeImageToBase64(combinedImage)

    }

    private fun loadImageFromClasspath(path: String): BufferedImage {
        val resourceAsStream: InputStream = javaClass.classLoader.getResourceAsStream("keypad_images/$path")
            ?: throw IllegalArgumentException("Image file not found: $path")
        return ImageIO.read(resourceAsStream)
    }

    private fun combineImagesInGrid(images: List<BufferedImage>, columns: Int, rows: Int): BufferedImage {
        // 이미지의 폭과 높이 계산
        if (images.size != columns * rows) {
            throw IllegalArgumentException("The number of images does not match the grid size.")
        }

        val imageWidth = images[0].width
        val imageHeight = images[0].height
        val combinedWidth = imageWidth * columns
        val combinedHeight = imageHeight * rows

        // 새로운 이미지 생성
        val combinedImage = BufferedImage(combinedWidth, combinedHeight, BufferedImage.TYPE_INT_ARGB)
        val g2d: Graphics2D = combinedImage.createGraphics()

        // 이미지를 그리드 형태로 결합
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
}