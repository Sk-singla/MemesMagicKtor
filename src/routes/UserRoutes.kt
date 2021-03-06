package com.samarth.routes

import com.samarth.authentication.JwtService
import com.samarth.data.*
import com.samarth.data.database.*
import com.samarth.data.models.request.LoginRequest
import com.samarth.data.models.request.RegisterUserRequest
import com.samarth.data.models.response.SimpleResponse
import com.samarth.models.User
import com.samarth.data.models.UserInfo
import com.samarth.data.models.request.UserInfoRequest
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
const val SEARCH_USERS = "$USER/search"
const val DELETE_USER = "$USER/delete"
const val DELETE_SINGLE_USER = "$DELETE_USER/single"
const val DELETE_ALL_USERS = "$DELETE_USER/all"
const val UPDATE_USERINFO = "$USER/update"


@Location(USER_REGISTER)
class UserRegisterRoute

@Location(USER_LOGIN)
class UserLoginRoute

@Location(GET_USER)
class UserAllGetRoute

@Location(UPDATE_USERINFO)
class UserInfoUpdateRoute

@Location(DELETE_ALL_USERS)
class UserAllDeleteRoute


@Location(DELETE_SINGLE_USER)
class UserSingleDeleteRoute


@Location("$FOLLOW_USER/{email}")
class UserFollowRoute(val email: String)

@Location("$UNFOLLOW_USER/{email}")
class UserUnfollowRoute(val email: String)


@Location("$GET_USER/{email}")
class UserSingleGetRoute(val email:String)

@Location("$SEARCH_USERS/{searchKeyWord}")
class UserSearchRoute(val searchKeyWord:String)

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
            if(findUserByEmail(email=registerUserRequest.email) != null){
                SimpleResponse<String>(false, "Email Id Already Present!!")
                return@post
            }


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

        delete<UserAllDeleteRoute>{
            try {
                call.respond(
                    HttpStatusCode.OK,
                    SimpleResponse<Long>(
                        true,
                        "Deleted Accounts of All Users",
                        deleteAllUserAccounts()
                    )
                )
            }catch (e:Exception){
                call.respond(
                    HttpStatusCode.Conflict,
                    SimpleResponse<Long>(false, e.message ?: "Some Problem Occurred!")
                )
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


        delete<UserSingleDeleteRoute> {
            try {
                val email = call.principal<UserIdPrincipal>()!!.name
                if(deleteSingleUser(email))
                    call.respond(
                        HttpStatusCode.OK,
                        SimpleResponse(
                            true,
                            "",
                            "Deleted user with email Id: $email",
                        ))
                else
                    call.respond(
                        HttpStatusCode.Conflict,
                        SimpleResponse<String>(false,  "Some Problem Occurred!")
                    )
            }catch (e:Exception){
                call.respond(HttpStatusCode.Conflict,SimpleResponse<String>(false, e.message ?: "Some Problem Occurred!"))
            }
        }

    }

    authenticate("jwt") {

        post<UserFollowRoute> { route ->
            try {
                val userToFollow = findUserByEmail(route.email)
                if (userToFollow == null) {
                    call.respond(HttpStatusCode.Conflict, SimpleResponse<UserInfo>(false, "User Not Found"))
                    return@post
                }

                val curUserEmail = call.principal<UserIdPrincipal>()!!.name
                val curUser = findUserByEmail(curUserEmail)!!

                if(curUser.followings.contains(userToFollow.userInfo)){
                    call.respond(HttpStatusCode.Conflict, SimpleResponse<UserInfo>(false, "Already Following!!"))
                    return@post
                }


                if (addFollower(userToFollow.userInfo, curUser.userInfo)) {
                    if (addFollowing(curUser.userInfo, userToFollow.userInfo)) {
                        call.respond(HttpStatusCode.OK, SimpleResponse<UserInfo>(true, "", userToFollow.userInfo))
                    } else {
                        removeFollower(userToFollow.userInfo, curUser.userInfo)
                        call.respond(
                            HttpStatusCode.Conflict,
                            SimpleResponse<UserInfo>(false, "Some Problem Occurred!!")
                        )
                    }
                } else {
                    call.respond(HttpStatusCode.Conflict, SimpleResponse<UserInfo>(false, "Some Problem Occurred!!"))
                }

            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.Conflict,
                    SimpleResponse<UserInfo>(false, e.message ?: "Some Problem Occurred!")
                )
            }
        }


        post<UserUnfollowRoute> { route ->
            try {
                val userToUnFollow = findUserByEmail(route.email)
                if (userToUnFollow == null) {
                    call.respond(HttpStatusCode.Conflict, SimpleResponse<UserInfo>(false, "User Not Found"))
                    return@post
                }

                val curUserEmail = call.principal<UserIdPrincipal>()!!.name
                val curUser = findUserByEmail(curUserEmail)!!

                if(!curUser.followings.contains(userToUnFollow.userInfo)){
                    call.respond(HttpStatusCode.Conflict, SimpleResponse<UserInfo>(false, "Already Not Following!!"))
                    return@post
                }

                if (removeFollower(userToUnFollow.userInfo, curUser.userInfo)) {
                    if (removeFollowing(curUser.userInfo, userToUnFollow.userInfo)) {
                        call.respond(HttpStatusCode.OK, SimpleResponse<UserInfo>(true, "", userToUnFollow.userInfo))
                    } else {
                        addFollower(userToUnFollow.userInfo, curUser.userInfo)
                        call.respond(
                            HttpStatusCode.Conflict,
                            SimpleResponse<UserInfo>(false, "Some Problem Occurred!!")
                        )
                    }
                } else {
                    call.respond(HttpStatusCode.Conflict, SimpleResponse<UserInfo>(false, "Some Problem Occurred!!"))
                }

            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.Conflict,
                    SimpleResponse<UserInfo>(false, e.message ?: "Some Problem Occurred!")
                )
            }
        }



        get<UserSearchRoute> { route->
            try {
                val curUserEmail = call.principal<UserIdPrincipal>()!!.name

                call.respond(HttpStatusCode.OK,SimpleResponse<List<UserInfo>>(
                    true,
                    "",
                    findUserByName(route.searchKeyWord).filter { it.email != curUserEmail }
                ))
            }catch (e:Exception){
                call.respond(
                    HttpStatusCode.Conflict,
                    SimpleResponse<List<UserInfo>>(true, e.message ?: "Some Problem Occurred!!")
                )
            }
        }

        post<UserInfoUpdateRoute> {
            val userInfoRequest = try {
                call.receive<UserInfoRequest>()
            }catch (e:Exception){
                call.respond(HttpStatusCode.BadRequest,SimpleResponse<UserInfo>(false,"Missing Fields"))
                return@post
            }
            try {
                val email = call.principal<UserIdPrincipal>()!!.name
                val prevUserInfo = findUserByEmail(email)!!.userInfo
                val userInfo = UserInfo(
                    userInfoRequest.name ?: prevUserInfo.name,
                    email,
                    userInfoRequest.profilePic ?: prevUserInfo.profilePic,
                    userInfoRequest.bio ?: prevUserInfo.bio
                )
                if(updateUserInfo(userInfo)){
                    call.respond(HttpStatusCode.OK,SimpleResponse<UserInfo>(true,"",userInfo))
                } else {
                    call.respond(HttpStatusCode.Conflict,SimpleResponse<UserInfo>(false,"Some Problem Occurred!"))
                }
            } catch (e:Exception){
                call.respond(HttpStatusCode.Conflict,SimpleResponse<UserInfo>(false,e.message ?: "Some Problem Occurred!"))
            }
        }

    }
}


































