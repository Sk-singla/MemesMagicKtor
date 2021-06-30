package com.samarth.models

import com.samarth.data.models.Reward
import com.samarth.data.models.UserInfo
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

data class User(
    val userInfo: UserInfo,
    val hashPassword:String,
    val rewards: MutableList<Reward> = mutableListOf(),
    var postCount:Int = 0,
    val followings:MutableList<UserInfo> = mutableListOf(),
    val followers:MutableList<UserInfo> = mutableListOf(),
    @BsonId
    val id:String = ObjectId().toString()
)
