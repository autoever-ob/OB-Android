package com.nobody.campick.models

import kotlinx.serialization.Serializable

@Serializable
data class ImageUploadResponse(
    val status: Int,
    val success: Boolean,
    val message: String,
    val data: List<ImageUploadItem>?
)

@Serializable
data class ImageUploadItem(
    val productImageUrl: String
)