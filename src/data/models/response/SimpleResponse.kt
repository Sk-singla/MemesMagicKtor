package com.samarth.data.models.response

data class SimpleResponse<T>(
    val success:Boolean,
    val message:String,
    val data:T? = null
)