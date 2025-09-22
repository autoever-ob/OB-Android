package com.nobody.campick.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.nobody.campick.resources.theme.AppColors
import com.nobody.campick.utils.ImageUtils
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

class ImageCropActivity : ComponentActivity() {

    private lateinit var imageUri: Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        imageUri = intent.getParcelableExtra<Uri>(EXTRA_IMAGE_URI) ?: run {
            setResult(Activity.RESULT_CANCELED)
            finish()
            return
        }

        setContent {
            ImageCropScreen(
                imageUri = imageUri,
                onCropComplete = { croppedImageUri ->
                    val intent = Intent().apply {
                        putExtra(EXTRA_CROPPED_IMAGE_URI, croppedImageUri)
                    }
                    setResult(Activity.RESULT_OK, intent)
                    finish()
                },
                onCancel = {
                    setResult(Activity.RESULT_CANCELED)
                    finish()
                }
            )
        }
    }

    companion object {
        const val EXTRA_IMAGE_URI = "extra_image_uri"
        const val EXTRA_CROPPED_IMAGE_URI = "extra_cropped_image_uri"

        fun newIntent(context: Context, imageUri: Uri): Intent {
            return Intent(context, ImageCropActivity::class.java).apply {
                putExtra(EXTRA_IMAGE_URI, imageUri)
            }
        }
    }
}

@Composable
private fun ImageCropScreen(
    imageUri: Uri,
    onCropComplete: (Uri) -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }

    // 이미지 로드
    LaunchedEffect(imageUri) {
        bitmap = ImageUtils.loadCompressedBitmapFromUri(context, imageUri)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.background)
    ) {
        // 상단 제목
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "이미지를 4:3 비율로 조절하세요",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }

        // 크롭 영역
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            bitmap?.let { bmp ->
                ImageCropView(
                    bitmap = bmp,
                    scale = scale,
                    offset = offset,
                    onScaleChange = { scale = it },
                    onOffsetChange = { offset = it },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // 하단 버튼들
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = onCancel,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Gray.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "취소",
                    color = Color.White,
                    fontSize = 16.sp
                )
            }

            Button(
                onClick = {
                    bitmap?.let { bmp ->
                        val croppedBitmap = cropBitmapWithTransform(bmp, scale, offset)
                        val croppedUri = saveBitmapToTempFile(context, croppedBitmap)
                        croppedUri?.let(onCropComplete)
                    }
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColors.brandOrange
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "완료",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun ImageCropView(
    bitmap: Bitmap,
    scale: Float,
    offset: Offset,
    onScaleChange: (Float) -> Unit,
    onOffsetChange: (Offset) -> Unit,
    modifier: Modifier = Modifier
) {
    val imageBitmap = bitmap.asImageBitmap()

    BoxWithConstraints(modifier = modifier) {
        val containerWidth = maxWidth.value
        val containerHeight = maxHeight.value

        // 4:3 비율 크롭 영역 계산
        val aspectRatio = 4f / 3f
        val cropWidth: Float
        val cropHeight: Float

        if (containerWidth / containerHeight > aspectRatio) {
            cropHeight = containerHeight - 40f
            cropWidth = cropHeight * aspectRatio
        } else {
            cropWidth = containerWidth - 40f
            cropHeight = cropWidth / aspectRatio
        }

        val cropSize = Size(cropWidth, cropHeight)
        val cropCenter = Offset(containerWidth / 2f, containerHeight / 2f)

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        onScaleChange((scale * zoom).coerceIn(1f, 3f))
                        onOffsetChange(offset + pan)
                    }
                }
                .pointerInput(Unit) {
                    detectDragGestures { _, dragAmount ->
                        onOffsetChange(offset + dragAmount)
                    }
                }
        ) {
            // 이미지 그리기
            val imageWidth = bitmap.width * scale
            val imageHeight = bitmap.height * scale

            val imageOffset = Offset(
                cropCenter.x - imageWidth / 2f + offset.x,
                cropCenter.y - imageHeight / 2f + offset.y
            )

            drawImage(
                image = imageBitmap,
                dstOffset = androidx.compose.ui.unit.IntOffset(
                    imageOffset.x.toInt(),
                    imageOffset.y.toInt()
                ),
                dstSize = androidx.compose.ui.unit.IntSize(
                    imageWidth.toInt(),
                    imageHeight.toInt()
                )
            )

            // 어두운 오버레이
            drawRect(
                color = Color.Black.copy(alpha = 0.6f),
                size = size
            )

            // 크롭 영역 (투명)
            drawRect(
                color = Color.Transparent,
                topLeft = Offset(
                    cropCenter.x - cropWidth / 2f,
                    cropCenter.y - cropHeight / 2f
                ),
                size = cropSize,
                blendMode = BlendMode.Clear
            )

            // 크롭 영역 테두리
            drawRect(
                color = AppColors.brandOrange,
                topLeft = Offset(
                    cropCenter.x - cropWidth / 2f,
                    cropCenter.y - cropHeight / 2f
                ),
                size = cropSize,
                style = Stroke(width = 2.dp.toPx())
            )

            // "4:3" 비율 표시
            drawContext.canvas.nativeCanvas.apply {
                val paint = android.graphics.Paint().apply {
                    color = AppColors.brandOrange.toArgb()
                    textSize = 12.sp.toPx()
                    textAlign = android.graphics.Paint.Align.CENTER
                }

                drawText(
                    "4:3",
                    cropCenter.x,
                    cropCenter.y - cropHeight / 2f - 30f,
                    paint
                )
            }

            // 모서리 표시
            drawCornerIndicators(cropCenter, cropSize)
        }
    }
}

private fun DrawScope.drawCornerIndicators(center: Offset, cropSize: Size) {
    val cornerLength = 20f
    val cornerThickness = 3f
    val halfWidth = cropSize.width / 2f
    val halfHeight = cropSize.height / 2f

    val corners = listOf(
        Offset(center.x - halfWidth, center.y - halfHeight), // 좌상
        Offset(center.x + halfWidth, center.y - halfHeight), // 우상
        Offset(center.x - halfWidth, center.y + halfHeight), // 좌하
        Offset(center.x + halfWidth, center.y + halfHeight)  // 우하
    )

    corners.forEach { corner ->
        drawRect(
            color = AppColors.brandOrange,
            topLeft = Offset(corner.x - cornerLength / 2f, corner.y - cornerThickness / 2f),
            size = Size(cornerLength, cornerThickness)
        )
    }
}

private fun cropBitmapWithTransform(bitmap: Bitmap, scale: Float, offset: Offset): Bitmap {
    // 4:3 비율로 크롭 후 400x300으로 리사이즈
    val croppedBitmap = ImageUtils.cropToAspectRatio(bitmap, 4f / 3f)
    return ImageUtils.resizeToMainImageSize(croppedBitmap)
}

private fun saveBitmapToTempFile(context: Context, bitmap: Bitmap): Uri? {
    return try {
        val tempFile = File(context.cacheDir, "cropped_image_${System.currentTimeMillis()}.jpg")
        val outputStream = FileOutputStream(tempFile)

        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
        outputStream.flush()
        outputStream.close()

        Uri.fromFile(tempFile)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}