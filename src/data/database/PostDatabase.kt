package com.samarth.data.database

import com.mongodb.client.model.BsonField
import com.samarth.data.models.Comment
import com.samarth.models.Post
import com.samarth.models.User
import com.samarth.data.models.UserInfo
import org.bson.conversions.Bson
import org.litote.kmongo.*
import java.util.*


suspend fun uploadPost(post: Post):Boolean {
    if(checkIfUserExists(post.createdBy.email)){
        val isPostUploaded = postsCol.insertOne(post).wasAcknowledged()

        return isPostUploaded
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


suspend fun deleteAllPostOfDatabase():Long{
    return postsCol.deleteMany(EMPTY_BSON).deletedCount
}

suspend fun deletePost(postId:String):Boolean{
    return postsCol.deleteOneById(postId).wasAcknowledged()
}

suspend fun addPostLike(userInfo: UserInfo, postId: String):Boolean{
    return postsCol.updateOneById(
        postId,
        addToSet(Post::likedBy,userInfo)
    ).wasAcknowledged()
}


suspend fun removePostLike(userInfo: UserInfo, postId: String):Boolean{
    return postsCol.updateOneById(
        postId,
        pull(Post::likedBy,userInfo)
    ).wasAcknowledged()
}

suspend fun addComment(postId:String, comment:Comment):Boolean{
    return postsCol.updateOneById(
        postId,
        addToSet(Post::comments,comment)
    ).wasAcknowledged()
}


suspend fun likeComment(postId: String,commentId:String,userInfo: UserInfo):Boolean {
    val comments = getPostById(postId)?.comments
    comments?.find { it.id == commentId }?.likedBy?.add(userInfo)
    return postsCol.updateOne(
        Post::id eq postId,
        setValue(Post::comments,comments)
    ).wasAcknowledged()
}

suspend fun getPostById(postId: String) = postsCol.findOneById(postId)

suspend fun removeLikeComment(postId: String,commentId:String,userInfo: UserInfo):Boolean {
    val comments = getPostById(postId)?.comments
    comments?.find { it.id == commentId }?.likedBy?.remove(userInfo)
    return postsCol.updateOne(
        Post::id eq postId,
        setValue(Post::comments,comments)
    ).wasAcknowledged()
}


suspend fun getMostLikePostOfMonth():Post{
    val cal = Calendar.getInstance()
    cal.add(Calendar.MONTH,-1)
    val lastMonth = cal.timeInMillis

    return postsCol.aggregate<Post>(
        match(Post::time gte lastMonth),
        sort(descending(Post::likedBy))
    ).toList().first()
}


suspend fun getMostLikedPostOfTheYear():Post{
    val cal = Calendar.getInstance()
    cal.add(Calendar.YEAR,-1)
    val lastYear = cal.timeInMillis

    return postsCol.aggregate<Post>(
        match(Post::time gte lastYear),
        sort(descending(Post::likedBy))
    ).toList().first()
}




















