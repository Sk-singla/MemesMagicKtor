package com.samarth.data.models

import com.samarth.models.enums.MemeBadgeType
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

data class Reward(
    val memeBadgeType: MemeBadgeType,
    val time:Long,
    val userEmail: String,
    @BsonId
    val id:String = ObjectId().toString()
)
