package com.samarth.routes

import com.samarth.data.*
import com.samarth.data.database.*
import com.samarth.data.models.Comment
import com.samarth.data.models.Meme
import com.samarth.data.models.request.PostRequest
import com.samarth.data.models.response.SimpleResponse
import com.samarth.models.Post
import com.samarth.data.models.UserInfo
import com.samarth.data.models.request.CommentRequest
import com.samarth.others.API_VERSION
import com.samarth.others.getHash
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.locations.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import java.lang.Exception

const val POST = "$API_VERSION/posts"
const val CREATE_POST = "$POST/create"
const val GET_SINGLE_POST = "$POST/get/single"
const val DELETE_ALL_POST_OF_USER = "$POST/deleteAll"
const val GET_ALL_POST = "$POST/get"
const val ADD_LIKE = "$POST/like"
const val REMOVE_LIKE = "$POST/dislike"
const val COMMENT = "$API_VERSION/comments"
const val ADD_COMMENT = "$COMMENT/add"
const val LIKE_COMMENT = "$COMMENT/like"
const val REMOVE_LIKE_OF_COMMENT = "$COMMENT/dislike"



@Location(CREATE_POST)
class PostCreateRoute

@Location("$DELETE_ALL_POST_OF_USER/{email}")
class PostAllDeleteRoute(val email:String)

@Location("$GET_SINGLE_POST/{postId}")
class PostGetSingleRoute(val postId:String)

@Location("$GET_ALL_POST/{email}")
class PostGetAllRoute(val email:String)

@Location("$ADD_LIKE/{postId}")
class PostAddLike(val postId: String)

@Location("$REMOVE_LIKE/{postId}")
class PostRemoveLike(val postId: String)

@Location("$ADD_COMMENT/{postId}")
class PostCommentAddRoute(val postId: String)

@Location("$LIKE_COMMENT/{postId}/{commentId}")
class PostCommentLikeRoute(val postId: String,val commentId:String)

@Location("$REMOVE_LIKE_OF_COMMENT/{postId}/{commentId}")
class PostCommentRemoveLikeRoute(val postId: String,val commentId:String)

fun Route.PostRoutes(){


    authenticate("jwt") {
        // =========== UPLOAD A POST ================
        post<PostCreateRoute>{
            val postRequest = try{
                call.receive<PostRequest>()
            } catch (e:Exception){
                call.respond(HttpStatusCode.BadRequest,SimpleResponse<String>(false,"Missing fields"))
                return@post
            }

            try {

                val email = call.principal<UserIdPrincipal>()!!.name
                val user = findUserByEmail(email)!!
                val post = Post(
                            user.userInfo,
                            postRequest.postType,
                            postRequest.time,
                            tags = postRequest.tags,
                            mediaLink = postRequest.mediaLink,
                            description = postRequest.description
                        )
                val isPosted = uploadPost(post)


                addMeme(Meme(postRequest.mediaLink, getHash(email)))


                if(isPosted){
                    if(incrementPostCount(email)){
                        call.respond(
                            HttpStatusCode.OK,
                            SimpleResponse<String>(true, "","Post Uploaded Successfully!")
                        )
                    } else {
                        deletePost(postId = post.id)
                        call.respond(
                            HttpStatusCode.Conflict,
                            SimpleResponse<String>(false, "Sorry! Cannot upload Post")
                        )
                    }
                } else {
                    call.respond(
                        HttpStatusCode.Conflict,
                        SimpleResponse<String>(false, "Sorry! Cannot upload Post")
                    )
                }
            } catch (e:Exception){
                call.respond(
                    HttpStatusCode.Conflict,
                    SimpleResponse<String>(false, e.message ?: "Some Problem Occurred!")
                )
            }
        }





        // ============== GET ALL POSTS OF A USER =============
        get<PostGetAllRoute> { route->
            try {
                call.respond(SimpleResponse<List<Post>>(true,"",getAllPostsOfUser(route.email)))
            }catch (e: Exception){
                call.respond(HttpStatusCode.Conflict,SimpleResponse<List<Post>>(false,e.message ?: "Some Problem Occurred!"))
            }
        }


        post<PostAddLike>{ route->
            try{
                val email = call.principal<UserIdPrincipal>()!!.name
                val user = findUserByEmail(email)!!
                if(addPostLike(user.userInfo,route.postId)){
                    call.respond(HttpStatusCode.OK,SimpleResponse<UserInfo>(true,"",user.userInfo))
                } else{
                    call.respond(HttpStatusCode.Conflict,SimpleResponse<UserInfo>(false,"Can't Like Post!!"))
                }
            }catch (e:Exception){
                call.respond(HttpStatusCode.Conflict,SimpleResponse<UserInfo>(false,e.message ?: "Can't Like Post!!"))
            }

        }

        post<PostRemoveLike>{ route->
            try{
                val email = call.principal<UserIdPrincipal>()!!.name
                val user = findUserByEmail(email)!!
                if(removePostLike(user.userInfo,route.postId)){
                    call.respond(HttpStatusCode.OK,SimpleResponse<UserInfo>(true,"",user.userInfo))
                } else{
                    call.respond(HttpStatusCode.Conflict,SimpleResponse<UserInfo>(false,"Can't dislike Post!!"))
                }
            }catch (e:Exception){
                call.respond(HttpStatusCode.Conflict,SimpleResponse<UserInfo>(false,e.message ?: "Can't dislike Post!!"))
            }

        }


        post<PostCommentAddRoute> { route ->
            val commentRequest = try {
                call.receive<CommentRequest>()
            } catch (e:Exception){
                call.respond(HttpStatusCode.BadRequest,SimpleResponse<Comment>(false,"Missing Fields in Body!!"))
                return@post
            }



            try {
                val email = call.principal<UserIdPrincipal>()!!.name
                val userInfo = findUserByEmail(email)!!.userInfo
                val comment = Comment(userInfo,commentRequest.text,commentRequest.time,id = commentRequest.id)
                if(addComment(route.postId,comment)){
                    call.respond(HttpStatusCode.OK,SimpleResponse<Comment>(true,"",comment))
                } else {
                    call.respond(HttpStatusCode.Conflict,SimpleResponse<Comment>(false,"Can't Add Comment!!"))
                }

            }catch (e:Exception){
                call.respond(HttpStatusCode.Conflict,SimpleResponse<Comment>(false,e.message?:"Can't Add Comment!!"))
            }
        }


        post<PostCommentLikeRoute>{ route ->
            try {
                val email = call.principal<UserIdPrincipal>()!!.name
                val userInfo = findUserByEmail(email)!!.userInfo

                if(likeComment(route.postId,route.commentId,userInfo)){
                    call.respond(HttpStatusCode.OK,SimpleResponse(true,"","Comment Like SuccessFully"))
                } else {
                    call.respond(HttpStatusCode.Conflict,SimpleResponse<String>(false,"Can't like Comment!!"))
                }

            } catch (e:Exception){
                call.respond(HttpStatusCode.Conflict,SimpleResponse<String>(false,e.message ?: "Can't like Comment!!"))
            }

        }


        post<PostCommentRemoveLikeRoute>{ route ->
            try {
                val email = call.principal<UserIdPrincipal>()!!.name
                val userInfo = findUserByEmail(email)!!.userInfo

                if(removeLikeComment(route.postId,route.commentId,userInfo)){
                    call.respond(HttpStatusCode.OK,SimpleResponse(true,"","Comment Disliked SuccessFully"))
                } else {
                    call.respond(HttpStatusCode.Conflict,SimpleResponse<String>(false,"Can't dislike Comment!!"))
                }

            } catch (e:Exception){
                call.respond(HttpStatusCode.Conflict,SimpleResponse<String>(false,e.message ?: "Can't dislike Comment!!"))
            }

        }

        get<PostGetSingleRoute>{ route ->

            try {
                val post = getPostById(route.postId)
                if(post == null){
                    call.respond(HttpStatusCode.BadRequest, SimpleResponse<Post>(false,"Wrong Post Id"))
                } else {
                    call.respond(HttpStatusCode.OK, SimpleResponse<Post>(true, "", post))
                }
            } catch (e:Exception){
                call.respond(HttpStatusCode.Conflict, SimpleResponse<Post>(false,e.message?:"Some Problem Occurred!!"))
            }

        }





    }


    authenticate("admin_auth"){
        // ============== DELETE ALL POSTS OF A USER =============
        delete<PostAllDeleteRoute> {
            try {
                val email = call.principal<UserIdPrincipal>()!!.name
                if (deleteAllPostOfUser(email)) {
                    call.respond(
                        HttpStatusCode.OK,
                        SimpleResponse<String>(
                            true,
                            "",
                            "Successfully Deleted All posts of a User!"
                        )
                    )
                } else {
                    call.respond(
                        HttpStatusCode.OK,
                        SimpleResponse<String>(
                            false,
                            "Can't Delete!"
                        )
                    )
                }
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.Conflict,
                    SimpleResponse<String>(
                        false,
                        e.message ?: "Some Problem Occurred!"
                    )
                )
            }
        }










    }









}