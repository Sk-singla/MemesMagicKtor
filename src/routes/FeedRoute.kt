package com.samarth.routes

import com.samarth.data.database.findUserByEmail
import com.samarth.data.database.getAllPostsOfUser
import com.samarth.data.models.response.SimpleResponse
import com.samarth.models.Post
import com.samarth.models.User
import com.samarth.others.API_VERSION
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.locations.*
import io.ktor.response.*
import io.ktor.routing.*
import java.lang.Exception

const val FEED = "$API_VERSION/feed"



@Location(FEED)
class FeedGetRoute



fun Route.FeedRoute(){


    authenticate("jwt") {

        post<FeedGetRoute> {

            try {
                val email = call.principal<UserIdPrincipal>()!!.name
                val user = findUserByEmail(email)!!        // Here i only need following of user
                val posts = mutableListOf<Post>()

                for(following in user.followings){
                    posts.addAll(getAllPostsOfUser(following.email))
                }

                call.respond(
                    HttpStatusCode.OK,
                    SimpleResponse(true, "", posts)
                )
            }catch (e:Exception){
                call.respond(
                    HttpStatusCode.Conflict,
                    SimpleResponse<List<Post>>(
                        false,
                        e.message ?: "Some Problem Occurred!!"
                    )
                )
            }

        }

    }
















}





































