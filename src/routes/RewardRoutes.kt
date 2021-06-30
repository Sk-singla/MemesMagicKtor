package com.samarth.routes

import com.samarth.data.database.addReward
import com.samarth.data.database.getCurrentMonthReward
import com.samarth.data.database.getMostLikePostOfMonth
import com.samarth.data.database.removeAllRewards
import com.samarth.data.models.Reward
import com.samarth.data.models.response.SimpleResponse
import com.samarth.models.Post
import com.samarth.models.enums.MemeBadgeType
import com.samarth.models.enums.PostType
import com.samarth.others.API_VERSION
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.locations.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.routing.post
import javafx.geometry.Pos
import java.lang.Exception


const val REWARD = "$API_VERSION/rewards"
const val ADD_REWARD = "$REWARD/add"
const val ADD_MONTH_REWARD  = "$ADD_REWARD/month"
const val GET_REWARD = "$REWARD/get"
const val GET_LAST_MONTH_REWARD = "$GET_REWARD/month"
const val REMOVE_REWARD = "$REWARD/remove"
const val REMOVE_ALL_REWARD = "$REMOVE_REWARD/all"

@Location(ADD_MONTH_REWARD)
class RewardAddMonthRoute


@Location(GET_LAST_MONTH_REWARD)
class RewardGetLastMonthRoute

@Location(REMOVE_ALL_REWARD)
class RewardAllRemoveRoute

fun Route.RewardRoutes(){


    authenticate("admin_auth") {

        post<RewardAddMonthRoute>{

            try {

                val mostLikedPost = getMostLikePostOfMonth()
                if(addReward(Reward(
                        MemeBadgeType.MEMER_OF_THE_MONTH,
                        System.currentTimeMillis(),
                        mostLikedPost.createdBy.email
                ))){
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

        get<RewardGetLastMonthRoute>{

            try {
                call.respond(HttpStatusCode.OK,SimpleResponse<Reward>(true,"", getCurrentMonthReward()))
            }catch (e:Exception){
                call.respond(HttpStatusCode.OK,SimpleResponse<Reward>(true,e.message?:"No Reward Found!!"))
            }
        }

    }

}