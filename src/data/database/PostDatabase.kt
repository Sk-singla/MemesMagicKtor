package com.samarth.data.database

import com.samarth.data.models.Comment
import com.samarth.models.Post
import com.samarth.models.User
import com.samarth.data.models.UserInfo
import org.litote.kmongo.*
import java.util.*


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


suspend fun deletePost(postId:String){
    postsCol.deleteOneById(postId)
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
        project(
            LikesCount::count from Post::likedBy.count(),
        ),
        sort(ascending(LikesCount::count))
    ).toList().first()
}


fun getMonth(time:Long):Int{
    val cal = Calendar.getInstance()
    cal.timeInMillis = time
    return cal.get(Calendar.MONTH)
}

data class LikesCount(
    val count:Int
)




















