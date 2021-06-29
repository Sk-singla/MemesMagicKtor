package com.samarth.data.models

data class Comment(
    val userInfo: UserInfo,
    val text:String,
    val time:Long,
    val likedBy:MutableList<UserInfo> = mutableListOf(),
    val id:String
)
