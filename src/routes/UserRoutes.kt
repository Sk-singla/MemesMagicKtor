package com.samarth.routes

import com.samarth.authentication.JwtService
import com.samarth.data.*
import com.samarth.data.database.findUserByEmail
import com.samarth.data.database.getAllUsers
import com.samarth.data.database.registerUser
import com.samarth.data.models.request.LoginRequest
import com.samarth.data.models.request.RegisterUserRequest
import com.samarth.data.models.response.SimpleResponse
import com.samarth.models.User
import com.samarth.models.UserInfo
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


const val USER = "$API_VERSION/user"
const val USER_REGISTER = "$USER/register"
const val USER_LOGIN = "$USER/login"
const val GET_USER = "$USER/get"

@Location(USER_REGISTER)
class UserRegisterRoute

@Location(USER_LOGIN)
class UserLoginRoute

@Location(GET_USER)
class UserAllGetRoute

@Location("$GET_USER/{email}")
class UserSingleGetRoute(val email:String)

fun Route.UserRoutes(
    jwtService: JwtService
){


    // =========== REGISTER SINGLE USER =============
    post<UserRegisterRoute>{
        val registerUserRequest = try {
            call.receive<RegisterUserRequest>()
        }catch (e: Exception){
            call.respond(
                HttpStatusCode.BadRequest,
                SimpleResponse<String>(false, "Missing Fields")
            )
            return@post
        }

        try {
            val user = User(
                UserInfo(
                    registerUserRequest.name,
                    registerUserRequest.email,
                    registerUserRequest.profilePic
                ),
                getHash(registerUserRequest.password))

            val isUserRegistered = registerUser(user)
            if(isUserRegistered) {
                call.respond(
                    HttpStatusCode.OK,
                    SimpleResponse<String>(true,"User Registered Successfully!", jwtService.generateToken(user))
                )
            } else {
                call.respond(
                    HttpStatusCode.Conflict,
                    SimpleResponse<String>(false, "Can't register User!")
                )
            }
        }catch (e:Exception){
            call.respond(
                HttpStatusCode.Conflict,
                SimpleResponse<String>(false, e.message ?: "Some Problem Occurred!")
            )
        }
    }

    post<UserLoginRoute> {

        val loginRequest = try {
            call.receive<LoginRequest>()
        }catch (e: Exception){
            call.respond(
                HttpStatusCode.BadRequest,
                SimpleResponse<String>(false, "Missing Fields")
            )
            return@post
        }

        try {
            val user = findUserByEmail( loginRequest.email)

            if( user != null) {
                if(user.hashPassword == getHash(loginRequest.password)) {
                    call.respond(
                        HttpStatusCode.OK,
                        SimpleResponse<String>(true, "User Logged in Successfully!", jwtService.generateToken(user))
                    )
                } else {
                    call.respond(
                        HttpStatusCode.OK,
                        SimpleResponse<String>(false, "Password Incorrect!")
                    )
                }
            } else {
                call.respond(
                    HttpStatusCode.Conflict,
                    SimpleResponse<String>(false, "User with email Id: ${loginRequest.email} not registered!")
                )
            }
        }catch (e:Exception){
            call.respond(
                HttpStatusCode.Conflict,
                SimpleResponse<String>(false, e.message ?: "Some Problem Occurred!")
            )
        }

    }


    authenticate("admin_auth") {
        // ============ GET ALL USERS ===========

        get<UserAllGetRoute>{
            try {
                call.respond(SimpleResponse(true,"All Users", getAllUsers()))
            }catch (e:Exception){
                call.respond(HttpStatusCode.Conflict,SimpleResponse<List<User>>(false, e.message ?: "Some Problem Occurred!"))
            }
        }



        // ============= GET USER BY EMAIL ==============
        get<UserSingleGetRoute>{ route->
            try {
                val user = findUserByEmail(route.email)
                call.respond(SimpleResponse<User>(
                    user != null,
                    if(user!=null) "User found!" else "User with email id: ${route.email} Not found!",
                    user
                ))

            }catch (e:Exception){
                call.respond(HttpStatusCode.Conflict,SimpleResponse<User>(false,e.message?: "Some Problem Occurred!"))
            }
        }

    }








}