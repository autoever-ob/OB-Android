package com.nobody.campick.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import java.io.ByteArrayOutputStream
import kotlin.math.sqrt

object ImageUtils {

    /**
     * 이미지를 지정된 크기(MB) 이하로 압축
     * Swift ImageUploadService와 동일한 로직
     */
    fun compressImage(bitmap: Bitmap, maxSizeInMB: Double = 1.0): ByteArray? {
        val maxBytes = (maxSizeInMB * 1024 * 1024).toLong()

        var compression = 100
        var imageData = bitmapToByteArray(bitmap, compression)

        // 이미지가 maxBytes보다 클 경우 압축률을 점차 낮춤
        while (imageData.size > maxBytes && compression > 10) {
            compression -= 10
            imageData = bitmapToByteArray(bitmap, compression)
        }

        // 그래도 크면 이미지 크기를 줄임
        if (imageData.size > maxBytes) {
            val ratio = sqrt(maxBytes.toDouble() / imageData.size)
            val newWidth = (bitmap.width * ratio).toInt()
            val newHeight = (bitmap.height * ratio).toInt()

            val resizedBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
            imageData = bitmapToByteArray(resizedBitmap, 80)

            if (resizedBitmap != bitmap) {
                resizedBitmap.recycle()
            }
        }

        return imageData
    }

    /**
     * URI에서 압축된 Bitmap 로드
     */
    fun loadCompressedBitmapFromUri(context: Context, uri: Uri, maxSizeInMB: Double = 1.0): Bitmap? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            bitmap?.let {
                // EXIF 회전 정보 적용
                val rotatedBitmap = handleImageRotation(context, uri, it)
                rotatedBitmap
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 메인 이미지를 4:3 비율로 크롭
     * Swift AspectRatioCropView와 동일한 출력
     */
    fun cropToAspectRatio(bitmap: Bitmap, aspectRatio: Float = 4f / 3f): Bitmap {
        val originalWidth = bitmap.width
        val originalHeight = bitmap.height
        val originalAspectRatio = originalWidth.toFloat() / originalHeight.toFloat()

        val newWidth: Int
        val newHeight: Int

        if (originalAspectRatio > aspectRatio) {
            // 원본이 더 넓은 경우, 높이를 기준으로 계산
            newHeight = originalHeight
            newWidth = (newHeight * aspectRatio).toInt()
        } else {
            // 원본이 더 높은 경우, 너비를 기준으로 계산
            newWidth = originalWidth
            newHeight = (newWidth / aspectRatio).toInt()
        }

        val x = (originalWidth - newWidth) / 2
        val y = (originalHeight - newHeight) / 2

        return Bitmap.createBitmap(bitmap, x, y, newWidth, newHeight)
    }

    /**
     * 메인 이미지를 Swift와 동일한 400x300 크기로 리사이즈
     */
    fun resizeToMainImageSize(bitmap: Bitmap): Bitmap {
        val targetWidth = 400
        val targetHeight = 300

        return Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true)
    }

    /**
     * 메인 이미지 전체 처리: 4:3 크롭 + 400x300 리사이즈
     */
    fun processMainImage(bitmap: Bitmap): Bitmap {
        val croppedBitmap = cropToAspectRatio(bitmap, 4f / 3f)
        val resizedBitmap = resizeToMainImageSize(croppedBitmap)

        if (croppedBitmap != bitmap) {
            croppedBitmap.recycle()
        }

        return resizedBitmap
    }

    /**
     * EXIF 데이터를 확인하여 이미지 회전 처리
     */
    private fun handleImageRotation(context: Context, uri: Uri, bitmap: Bitmap): Bitmap {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val exif = ExifInterface(inputStream!!)
            val orientation = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
            inputStream.close()

            val matrix = Matrix()
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
                else -> return bitmap
            }

            val rotatedBitmap = Bitmap.createBitmap(
                bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true
            )

            if (rotatedBitmap != bitmap) {
                bitmap.recycle()
            }

            rotatedBitmap
        } catch (e: Exception) {
            e.printStackTrace()
            bitmap
        }
    }

    /**
     * Bitmap을 JPEG ByteArray로 변환
     */
    private fun bitmapToByteArray(bitmap: Bitmap, quality: Int): ByteArray {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
        return outputStream.toByteArray()
    }

    /**
     * ByteArray 크기를 MB 단위로 반환
     */
    fun getImageSizeInMB(imageData: ByteArray): Double {
        return imageData.size.toDouble() / (1024 * 1024)
    }

    /**
     * 이미지 크기 정보를 문자열로 반환
     */
    fun getImageSizeString(imageData: ByteArray): String {
        val sizeInMB = getImageSizeInMB(imageData)
        return if (sizeInMB >= 1.0) {
            String.format("%.1f MB", sizeInMB)
        } else {
            String.format("%.0f KB", sizeInMB * 1024)
        }
    }
}