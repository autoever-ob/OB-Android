package com.nobody.campick.models.auth

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class UserType {
    @SerialName("normal")
    NORMAL,

    @SerialName("dealer")
    DEALER
}