package com.example.pdcast.data.model

import androidx.room.AutoMigration
import androidx.room.Entity
import androidx.room.PrimaryKey

data class Podcast(
    val resultCount: Int,
    val results: List<ItunesPodcast>
)

data class ItunesPodcast(
    val collectionCensoredName: String,
    val feedUrl: String,
    val artworkUrl30: String,
    val artworkUrl100: String,
    val releaseDate: String
)