package com.samarth.data

import com.samarth.data.database.usersCol
import com.samarth.models.User
import com.samarth.models.UserInfo
import com.samarth.others.getHash
import org.litote.kmongo.*



suspend fun registerUser(user:User):Boolean{
    return usersCol.insertOne(user).wasAcknowledged()
}

suspend fun findUserByEmail(email: String):User?{
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

