package com.nobody.campick.models.vehicle

import kotlinx.serialization.Serializable

@Serializable
data class VehicleOption(
    val optionName: String,
    var isInclude: Boolean = false
)