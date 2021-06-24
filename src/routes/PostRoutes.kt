package com.samarth.routes

import com.samarth.data.*
import com.samarth.data.database.*
import com.samarth.data.models.Meme
import com.samarth.data.models.request.PostRequest
import com.samarth.data.models.response.SimpleResponse
import com.samarth.models.Post
import com.samarth.models.enums.PostType
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
const val DELETE_ALL_POST_OF_USER = "$POST/deleteAll"
const val GET_ALL_POST = "$POST/get"


@Location(CREATE_POST)
class PostCreateRoute

@Location("$DELETE_ALL_POST_OF_USER/{email}")
class PostAllDeleteRoute(val email:String)

@Location(GET_ALL_POST)
class PostGetAllRoute



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
                val userInfo = findUserByEmail(email)!!
                val isPosted = uploadPost(
                    Post(
                        userInfo.userInfo,
                        postRequest.postType,
                        postRequest.time,
                        tags = postRequest.tags,
                        mediaLink = postRequest.mediaLink,
                        description = postRequest.desctiption
                    )
                )


                addMeme(Meme(postRequest.mediaLink, getHash(email)))


                if(isPosted){
                    call.respond(
                        HttpStatusCode.OK,
                        SimpleResponse<String>(true, "","Post Uploaded Successfully!")
                    )
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
                val email = call.principal<UserIdPrincipal>()!!.name
                call.respond(SimpleResponse<List<Post>>(true,"",getAllPostsOfUser(email)))
            }catch (e: Exception){
                call.respond(HttpStatusCode.Conflict,SimpleResponse<List<Post>>(false,e.message ?: "Some Problem Occurred!"))
            }
        }


        // ================ GET FEED ================



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