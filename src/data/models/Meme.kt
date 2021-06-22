package com.samarth.data.models

import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId


data class Meme(
    val url:String,
    val author:String,
    @BsonId
    val id:String = ObjectId().toString()
)
