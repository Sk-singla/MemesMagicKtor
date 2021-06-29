package com.samarth.data.database

import com.mongodb.client.model.UpdateOptions
import com.samarth.models.User
import com.samarth.models.UserInfo
import com.samarth.others.getHash
import org.litote.kmongo.*



suspend fun registerUser(user:User):Boolean{
    return usersCol.insertOne(user).wasAcknowledged()
}

fun findUserByEmail(email: String):User?{
    return usersCol.findOne(User::userInfo / UserInfo::email eq email)
}

suspend fun checkIfUserExists(email:String):Boolean {
    return findUserByEmail(email) != null
}

suspend fun checkPasswordForEmail(email: String,passwordToCheck:String):Boolean{
    val actualPassword = findUserByEmail(email)?.hashPassword ?: return false
    return actualPassword == getHash(passwordToCheck)
}


suspend fun getAllUsers():List<User>{
    return usersCol.find().toList()
}

fun getUserInfo(email: String):UserInfo?{
    return  usersCol.find(User::userInfo / UserInfo::email eq email , UserInfo::class.java ).toList().single()
}

fun incrementPostCount(email: String):Boolean{
    return usersCol.updateOne(
        User::userInfo / UserInfo::email eq email,
        inc(User::postCount,1)
    ).wasAcknowledged()
}


fun addFollower(user:UserInfo,follower:UserInfo):Boolean{
    return usersCol.updateOne(
        User::userInfo / UserInfo::email eq user.email,
        addToSet(User::followers,follower)
    ).wasAcknowledged()
}


fun addFollowing(follower:UserInfo,toFollow:UserInfo):Boolean{
    return usersCol.updateOne(
        User::userInfo / UserInfo::email eq follower.email,
        addToSet(User::followers,toFollow)
    ).wasAcknowledged()
}

fun removeFollower(user:UserInfo,follower:UserInfo):Boolean{
    return usersCol.updateOne(
        User::userInfo / UserInfo::email eq user.email,
        pull(User::followers,follower)
    ).wasAcknowledged()
}


fun removeFollowing(follower:UserInfo,toUnFollow:UserInfo):Boolean{
    return usersCol.updateOne(
        User::userInfo / UserInfo::email eq follower.email,
        pull(User::followers,toUnFollow)
    ).wasAcknowledged()
}



























