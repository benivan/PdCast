package com.example.pdcast.data.dto

import androidx.room.*


@Entity(tableName = "podcast_table")
data class DBRssFeedPodcast(
    @PrimaryKey(autoGenerate = true)
    val podcastId: Long? = null,
    val podcastFeedUrl:String,
    val title: String,
    val description: String,
    val language:String,
    val link: String,
    val imageUrl:String,
)





