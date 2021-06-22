package com.samarth.data.database

import com.samarth.data.models.Meme
import com.samarth.models.Post
import com.samarth.models.User
import org.litote.kmongo.KMongo
import org.litote.kmongo.getCollection

private val client = KMongo.createClient(System.getenv("MONGODB_URI"))
//private val db =  client.getDatabase("MemesMagicDB")
private val database =  client.getDatabase("Meme")


// ============ COLLECTIONS =============
val usersCol = database.getCollection<User>()
val postsCol = database.getCollection<Post>()
val memeCol = database.getCollection<Meme>()