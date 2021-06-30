package com.samarth.routes

import com.samarth.data.database.*
import com.samarth.data.models.Reward
import com.samarth.data.models.response.SimpleResponse
import com.samarth.models.Post
import com.samarth.models.enums.MemeBadgeType
import com.samarth.others.API_VERSION
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.locations.*
import io.ktor.response.*
import io.ktor.routing.*
import java.lang.Exception


const val REWARD = "$API_VERSION/rewards"
const val ADD_REWARD = "$REWARD/add"
const val ADD_MONTH_REWARD  = "$ADD_REWARD/month"
const val ADD_YEAR_REWARD  = "$ADD_REWARD/year"

const val GET_REWARD = "$REWARD/get"
const val GET_CURRENT_MONTH_REWARD = "$GET_REWARD/month"
const val GET_LAST_YEAR_REWARD = "$GET_REWARD/year"
const val GET_USER_REWARDS = "$GET_REWARD/user"

const val REMOVE_REWARD = "$REWARD/remove"
const val REMOVE_ALL_REWARD = "$REMOVE_REWARD/all"

@Location(ADD_MONTH_REWARD)
class RewardAddMonthRoute

@Location(ADD_YEAR_REWARD)
class RewardAddYearRoute


@Location(GET_CURRENT_MONTH_REWARD)
class RewardCurrentMonthGetRoute

@Location(GET_LAST_YEAR_REWARD)
class RewardLastYearGetRoute

@Location(REMOVE_ALL_REWARD)
class RewardAllRemoveRoute

@Location(GET_USER_REWARDS)
class RewardUserGetRoute

fun Route.RewardRoutes(){


    authenticate("admin_auth") {

        post<RewardAddMonthRoute>{
            try {

                val mostLikedPost = getMostLikePostOfMonth()
                val reward = Reward(
                    MemeBadgeType.MEMER_OF_THE_MONTH,
                    System.currentTimeMillis(),
                    mostLikedPost.createdBy.email
                )
                if(addReward(reward) && addRewardToUser(reward)){
                    call.respond(HttpStatusCode.OK,SimpleResponse<Post>(true,"",mostLikedPost))
                } else {
                    call.respond(HttpStatusCode.Conflict,SimpleResponse<Post>(false,"Can't add reward!!"))
                }

            }catch (e:Exception){
                call.respond(HttpStatusCode.Conflict,SimpleResponse<Post>(false,e.message ?: "Some Problem Occurred!!"))
            }

        }


        post<RewardAddYearRoute>{
            try {

                val mostLikedPost = getMostLikedPostOfTheYear()
                val reward = Reward(
                    MemeBadgeType.MEMER_OF_THE_YEAR,
                    System.currentTimeMillis(),
                    mostLikedPost.createdBy.email
                )
                if(addReward(reward) && addRewardToUser(reward)){
                    call.respond(HttpStatusCode.OK,SimpleResponse<Post>(true,"",mostLikedPost))
                } else {
                    call.respond(HttpStatusCode.Conflict,SimpleResponse<Post>(false,"Can't add reward!!"))
                }
            }catch (e:Exception){
                call.respond(HttpStatusCode.Conflict,SimpleResponse<Post>(false,e.message ?: "Some Problem Occurred!!"))
            }

        }


        delete<RewardAllRemoveRoute>{

            try {
                call.respond(HttpStatusCode.OK,SimpleResponse<Long>(true,"", removeAllRewards()))
            }catch (e:Exception){
                call.respond(HttpStatusCode.OK,SimpleResponse<Long>(true,e.message?:"Some Problem Occurred!!"))
            }

        }
    }


    authenticate("jwt") {

        get<RewardCurrentMonthGetRoute>{
            try {
                call.respond(HttpStatusCode.OK,SimpleResponse<Reward>(true,"", getCurrentMonthReward()))
            }catch (e:Exception){
                call.respond(HttpStatusCode.OK,SimpleResponse<Reward>(true,e.message?:"No Reward Found!!"))
            }
        }

        get<RewardLastYearGetRoute>{
            try {
                call.respond(HttpStatusCode.OK,SimpleResponse<Reward>(true,"", getLastYearReward()))
            }catch (e:Exception){
                call.respond(HttpStatusCode.OK,SimpleResponse<Reward>(true,e.message?:"No Reward Found!!"))
            }
        }

        get {
            try {
                val email = call.principal<UserIdPrincipal>()!!.name
                call.respond(HttpStatusCode.OK,SimpleResponse<List<Reward>>(true,"", getRewardsOfUser(email)))
            }catch (e:Exception){
                call.respond(HttpStatusCode.OK,SimpleResponse<List<Reward>>(true,e.message?:"No Reward Found!!"))
            }
        }


    }

}