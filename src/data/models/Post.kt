package com.samarth.models

import com.samarth.models.enums.PostType
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

data class Post(
    val createdBy:UserInfo,
    val postType:PostType,
    val time:Long,
    val likedBy:MutableList<UserInfo> = mutableListOf(),
    val comments:MutableList<Comment> = mutableListOf(),
    val tags:List<String>?=null,
    val mediaLink:String,
    val description:String?=null,
    @BsonId
    val id:String = ObjectId().toString()
)
