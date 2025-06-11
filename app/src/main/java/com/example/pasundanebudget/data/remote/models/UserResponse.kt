package com.pasundane_budget.data.remote.models

import com.google.gson.annotations.SerializedName

data class UserResponse(
    val id: Int,
    val name: String,
    val email: String,
    @SerializedName("api_token")
    val apiToken: String
)
