package com.nobody.campick.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream
import kotlin.math.sqrt

object ImageCompressor {

    fun compressImage(imageData: ByteArray, maxSizeInMB: Double = 1.0): ByteArray? {
        val maxBytes = (maxSizeInMB * 1024 * 1024).toLong()

        val bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.size) ?: return null

        var compression = 100
        var compressedData = compressBitmap(bitmap, compression)

        while (compressedData.size > maxBytes && compression > 10) {
            compression -= 10
            compressedData = compressBitmap(bitmap, compression)
        }

        if (compressedData.size > maxBytes) {
            val ratio = sqrt(maxBytes.toDouble() / compressedData.size.toDouble())
            val newWidth = (bitmap.width * ratio).toInt()
            val newHeight = (bitmap.height * ratio).toInt()

            val resizedBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
            compressedData = compressBitmap(resizedBitmap, 80)

            if (resizedBitmap != bitmap) {
                resizedBitmap.recycle()
            }
        }

        bitmap.recycle()
        return compressedData
    }

    private fun compressBitmap(bitmap: Bitmap, quality: Int): ByteArray {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
        return outputStream.toByteArray()
    }
}