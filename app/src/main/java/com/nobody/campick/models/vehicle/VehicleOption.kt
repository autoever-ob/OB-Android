package com.nobody.campick.models.vehicle

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable

@Serializable
data class VehicleOption(
    val optionName: String,
    @OptIn(ExperimentalSerializationApi::class)
    @EncodeDefault
    var isInclude: Boolean = false
)