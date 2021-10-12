package com.example.pdcast.data.dto

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "podcasts_episodes_table"
)
data class DBPodcastsEpisodes(
    @PrimaryKey(autoGenerate = true)
    val episodeId: Long? = null,
    val podcastId: Long,
    val title: String,
    val link: String,
    val description: String,
    val pubDate: String,
    val duration: String,
    val episodeUrl: String,
    val imageUrl: String,
    val podcastName: String
)
