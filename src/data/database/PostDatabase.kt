package com.samarth.data.database

import com.samarth.data.models.Comment
import com.samarth.models.Post
import com.samarth.models.User
import com.samarth.data.models.UserInfo
import com.sun.org.apache.xpath.internal.operations.Bool
import org.litote.kmongo.*


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
    return postsCol.updateOne(
        "{\"id\":$postId, \"comments\": {\"id\":$commentId } }",
        addToSet(Comment::likedBy,userInfo)
    ).wasAcknowledged()
}


suspend fun removeLikeComment(postId: String,commentId:String,userInfo: UserInfo):Boolean {
    return postsCol.updateOne(
        "{\"id\":$postId, \"comments\": {\"id\":$commentId } }",
        pull(Comment::likedBy,userInfo)
    ).wasAcknowledged()
}























