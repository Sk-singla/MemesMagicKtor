package com.samarth.data.database

import com.samarth.models.Post
import com.samarth.models.User
import com.samarth.models.UserInfo
import org.litote.kmongo.div
import org.litote.kmongo.eq
import org.litote.kmongo.inc


suspend fun uploadPost(post: Post):Boolean {
    if(checkIfUserExists(post.createdBy.email)){
        val isPostUploaded = postsCol.insertOne(post).wasAcknowledged()
        val isPostCountIncremented = usersCol.updateOne(
            User::userInfo / UserInfo::email eq post.createdBy.email, inc(
                User::postCount,1)
        ).wasAcknowledged()

        if(!isPostCountIncremented){
            postsCol.deleteOne(Post::id eq post.id)
        }

        return isPostUploaded && isPostCountIncremented
    }
    return false
}


suspend fun getAllPostsOfUser(email: String):List<Post>{
    if(!checkIfUserExists(email)){
        return emptyList()
    }
    return postsCol.find(Post::createdBy / UserInfo::email eq email).toList()
}

suspend fun deleteAllPostOfUser(email: String):Boolean{
    if(!checkIfUserExists(email)){
        return false
    }
    return postsCol.deleteMany(Post::createdBy / UserInfo::email eq email).wasAcknowledged()
}








