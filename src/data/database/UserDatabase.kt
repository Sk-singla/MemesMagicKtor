package com.samarth.data.database

import com.samarth.models.User
import com.samarth.data.models.UserInfo
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

fun getUserInfo(email: String): UserInfo?{
    return  usersCol.find(User::userInfo / UserInfo::email eq email , UserInfo::class.java ).toList().single()
}

fun incrementPostCount(email: String):Boolean{
    return usersCol.updateOne(
        User::userInfo / UserInfo::email eq email,
        inc(User::postCount,1)
    ).wasAcknowledged()
}

fun decrementPostCount(email: String):Boolean{
    return usersCol.updateOne(
        User::userInfo / UserInfo::email eq email,
        inc(User::postCount,-1)
    ).wasAcknowledged()
}


fun addFollower(user: UserInfo, follower: UserInfo):Boolean{
    return usersCol.updateOne(
        User::userInfo / UserInfo::email eq user.email,
        addToSet(User::followers,follower)
    ).wasAcknowledged()
}


fun addFollowing(follower: UserInfo, toFollow: UserInfo):Boolean{
    return usersCol.updateOne(
        User::userInfo / UserInfo::email eq follower.email,
        addToSet(User::followings,toFollow)
    ).wasAcknowledged()
}

fun removeFollower(user: UserInfo, follower: UserInfo):Boolean{
    return usersCol.updateOne(
        User::userInfo / UserInfo::email eq user.email,
        pull(User::followers,follower)
    ).wasAcknowledged()
}


fun removeFollowing(follower: UserInfo, toUnFollow: UserInfo):Boolean{
    return usersCol.updateOne(
        User::userInfo / UserInfo::email eq follower.email,
        pull(User::followings,toUnFollow)
    ).wasAcknowledged()
}


fun findUserByName(name:String):List<UserInfo> {
    return usersCol.find( (User::userInfo / UserInfo::name).regex(name,"\$i")).map{ it.userInfo }.toList()
}

fun deleteAllUserAccounts():Long{
    return usersCol.deleteMany(EMPTY_BSON).deletedCount
}

fun deleteSingleUser(email: String):Boolean{
    return usersCol.deleteOne(User::userInfo / UserInfo::email eq email).wasAcknowledged()
}

fun updateUserInfo(userInfo:UserInfo):Boolean{
    return usersCol.updateOne(User::userInfo / UserInfo::email eq userInfo.email, setValue(User::userInfo,userInfo)).wasAcknowledged()
}






















