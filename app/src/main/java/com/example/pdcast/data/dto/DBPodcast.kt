package com.example.pdcast.data.dto

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.CASCADE
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "podcast_subscribe_table")
data class DBPodcast(
    @PrimaryKey(autoGenerate = true)
    val id:Int? = null,
    val collectionCensoredName: String,
    val feedUrl: String,
    val artworkUrl30: String,
    val artworkUrl100: String,
    val releaseDate: String
)






