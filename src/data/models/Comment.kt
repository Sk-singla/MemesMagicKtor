package com.samarth.models

data class Comment(
    val userInfo: UserInfo,
    val text:String,
    val time:Long,
    val likedBy:List<UserInfo>
)
