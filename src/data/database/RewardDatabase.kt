package com.samarth.data.database

import com.samarth.data.models.Reward
import com.samarth.data.models.UserInfo
import com.samarth.models.User
import com.samarth.models.enums.MemeBadgeType
import org.litote.kmongo.*

fun addReward(reward: Reward):Boolean {
    return rewardsCol.insertOne(reward).wasAcknowledged()
}

fun getCurrentMonthReward():Reward? {
    return rewardsCol.find(
        Reward::memeBadgeType eq MemeBadgeType.MEMER_OF_THE_MONTH
    ).sort(descending(Reward::time)).limit(1).first()
}


fun getLastYearReward():Reward? {
    return rewardsCol.find(
        Reward::memeBadgeType eq MemeBadgeType.MEMER_OF_THE_YEAR
    ).sort(descending(Reward::time)).limit(1).first()
}


fun removeAllRewards():Long{
    return rewardsCol.deleteMany(EMPTY_BSON).deletedCount
}


fun addRewardToUser(reward: Reward):Boolean{
    return usersCol.updateOne(
        User::userInfo / UserInfo::email eq reward.userEmail,
        addToSet(User::rewards,reward)
    ).wasAcknowledged()
}

fun getRewardsOfUser(email:String):List<Reward>{
    return rewardsCol.find( Reward::userEmail eq email).toList()
}





















