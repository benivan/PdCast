package com.example.pdcast.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "podcast_subscribe_table")
data class PodcastDataBaseModel(
    @PrimaryKey(autoGenerate = true)
    val id:Int? = null,
    val collectionCensoredName: String,
    val feedUrl: String,
    val artworkUrl30: String,
    val artworkUrl100: String,
    val releaseDate: String
)
