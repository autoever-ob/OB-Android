package com.nobody.campick.models.auth

import com.google.gson.annotations.SerializedName

data class SignupRequest(
//    @SerializedName("email")
    val email: String,

//    @SerializedName("password")
    val password: String,

//    @SerializedName("checkedPassword")
    val checkedPassword: String,

//    @SerializedName("nickname")
    val nickname: String,

//    @SerializedName("mobileNumber")
    val mobileNumber: String,

//    @SerializedName("role")
    val role: String,

//    @SerializedName("dealershipName")
    val dealershipName: String,

//    @SerializedName("dealershipRegistrationNumber")
    val dealershipRegistrationNumber: String
)