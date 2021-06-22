package com.samarth.models

import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

data class User(
    val userInfo:UserInfo,
    val hashPassword:String,
    val memeBadges: MutableList<MemeBadge> = mutableListOf(),
    val postCount:Int = 0,
    val followings:MutableList<UserInfo> = mutableListOf(),
    val followers:MutableList<UserInfo> = mutableListOf(),
    @BsonId
    val id:String = ObjectId().toString()
)
