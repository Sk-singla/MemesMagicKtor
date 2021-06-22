package com.samarth.data.database

import com.samarth.data.models.Meme
import com.samarth.others.MAXIMUM_MEME_RESPONSE_SIZE
import org.litote.kmongo.aggregate
import org.litote.kmongo.sample

fun addMeme(meme: Meme){
    memeCol.insertOne(meme).wasAcknowledged()
}

fun getMemes(size:Int):List<Meme> {
    return memeCol.aggregate<Meme>(
        sample(
            if(size <= MAXIMUM_MEME_RESPONSE_SIZE) size else MAXIMUM_MEME_RESPONSE_SIZE
        )
    ).toList()
}

