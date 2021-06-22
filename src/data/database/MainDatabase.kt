package com.samarth.data.database

import com.samarth.data.models.Meme
import com.samarth.models.Post
import com.samarth.models.User
import org.litote.kmongo.KMongo
import org.litote.kmongo.getCollection

private val client = KMongo.createClient()
private val database = client.getDatabase("MemesMagicDB")


// ============ COLLECTIONS =============
val usersCol = database.getCollection<User>()
val postsCol = database.getCollection<Post>()
val memeCol = database.getCollection<Meme>()