package com.example.pdcast.data.model

data class PodcastModel(
    val podcastId:Long?,
    val podcastFeedUrl:String,
    val title:String,
    val description:String,
    val language:String,
    val link:String,
    val imageUrl:String
)
