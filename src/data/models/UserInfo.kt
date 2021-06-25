package com.samarth.models

data class UserInfo(
    val name:String,
    val email:String,
    val profilePic:String?=null,
    val bio:String?=null
)
