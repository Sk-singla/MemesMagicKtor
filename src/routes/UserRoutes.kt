package com.samarth.routes

import com.samarth.authentication.JwtService
import com.samarth.data.*
import com.samarth.data.database.*
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
const val FOLLOW_USER = "$USER/follow"
const val UNFOLLOW_USER = "$USER/unfollow"


@Location(USER_REGISTER)
class UserRegisterRoute

@Location(USER_LOGIN)
class UserLoginRoute

@Location(GET_USER)
class UserAllGetRoute

@Location("$FOLLOW_USER/{email}")
class UserFollowRoute(val email: String)

@Location("$UNFOLLOW_USER/{email}")
class UserUnfollowRoute(val email: String)


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


    }


    authenticate("jwt","admin_auth") {

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

    authenticate("jwt"){

        post<UserFollowRoute> { route ->
            try {
                val userToFollow = findUserByEmail(route.email)
                if(userToFollow == null){
                    call.respond(HttpStatusCode.Conflict,SimpleResponse<UserInfo>(false,"User Not Found"))
                    return@post
                }

                val curUserEmail = call.principal<UserIdPrincipal>()!!.name
                val curUser = findUserByEmail(curUserEmail)!!

                if(addFollower(userToFollow.userInfo,curUser.userInfo)){
                    if(addFollowing(curUser.userInfo,userToFollow.userInfo)){
                        call.respond(HttpStatusCode.OK,SimpleResponse<UserInfo>(false,"",userToFollow.userInfo))
                    } else {
                        removeFollower(userToFollow.userInfo,curUser.userInfo)
                        call.respond(HttpStatusCode.Conflict,SimpleResponse<UserInfo>(false,"Some Problem Occurred!!"))
                    }
                } else {
                    call.respond(HttpStatusCode.Conflict,SimpleResponse<UserInfo>(false,"Some Problem Occurred!!"))
                }

            }catch (e:Exception){
                call.respond(HttpStatusCode.Conflict,SimpleResponse<UserInfo>(false,e.message?: "Some Problem Occurred!"))
            }
        }
    }

    post<UserUnfollowRoute> { route ->
        try {
            val userToUnFollow = findUserByEmail(route.email)
            if(userToUnFollow == null){
                call.respond(HttpStatusCode.Conflict,SimpleResponse<UserInfo>(false,"User Not Found"))
                return@post
            }

            val curUserEmail = call.principal<UserIdPrincipal>()!!.name
            val curUser = findUserByEmail(curUserEmail)!!

            if(removeFollower(userToUnFollow.userInfo,curUser.userInfo)){
                if(removeFollowing(curUser.userInfo,userToUnFollow.userInfo)){
                    call.respond(HttpStatusCode.OK,SimpleResponse<UserInfo>(false,"",userToUnFollow.userInfo))
                } else {
                    addFollower(userToUnFollow.userInfo,curUser.userInfo)
                    call.respond(HttpStatusCode.Conflict,SimpleResponse<UserInfo>(false,"Some Problem Occurred!!"))
                }
            } else {
                call.respond(HttpStatusCode.Conflict,SimpleResponse<UserInfo>(false,"Some Problem Occurred!!"))
            }

        }catch (e:Exception){
            call.respond(HttpStatusCode.Conflict,SimpleResponse<UserInfo>(false,e.message?: "Some Problem Occurred!"))
        }
    }
}








}