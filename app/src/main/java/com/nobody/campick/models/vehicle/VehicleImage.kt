package com.nobody.campick.models.vehicle

import android.net.Uri

data class VehicleImage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val imageUri: Uri,
    var isMain: Boolean = false,
    var uploadedUrl: String? = null
)