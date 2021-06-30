package com.samarth.data.database

import com.samarth.data.models.Reward
import org.litote.kmongo.EMPTY_BSON
import org.litote.kmongo.descending

fun addReward(reward: Reward):Boolean {
    return rewardsCol.insertOne(reward).wasAcknowledged()
}

fun getCurrentMonthReward():Reward? {
    return rewardsCol.find().sort(descending(Reward::time)).limit(1).first()
}

fun removeAllRewards():Long{
    return rewardsCol.deleteMany(EMPTY_BSON).deletedCount
}
