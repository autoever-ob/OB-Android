package com.nobody.campick.models

import kotlinx.serialization.Serializable

@Serializable
data class ApiResponse<T>(
    val status: Int,
    val success: Boolean,
    val message: String,
    val data: T?
)