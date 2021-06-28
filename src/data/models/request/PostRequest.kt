package com.samarth.data.models.request

import com.samarth.models.enums.PostType

data class PostRequest(
    val postType:PostType,
    val time:Long,
    val tags:List<String>?=null,
    val mediaLink:String,
    val description:String?=null,
)
