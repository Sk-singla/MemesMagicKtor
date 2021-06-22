package com.samarth.routes

import com.samarth.data.database.getMemes import com.samarth.others.DEFAULT_MEME_RESPONSE_SIZE
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import java.lang.Exception


fun Route.MemeRoutes(){
    get("/memes/{count}"){
        val countString = call.parameters["count"]
        val count = try {
            if(countString!=null && countString.isNotEmpty()){
                countString.toInt()
            } else {
                DEFAULT_MEME_RESPONSE_SIZE
            }
        } catch (e: Exception){
            call.respond(HttpStatusCode.BadRequest,e.message ?: "Missing Parameters!")
            return@get
        }
        call.respond(getMemes(count))
    }



}