package com.example.pdcast.data.response

import java.util.*

data class RssFeedResponse(
    var title: String? = null,
    var description: String? = null,
    var language:String? = null,
    var link: String? = null,
    var imageUrl:String? = null,
    var episodes: MutableList<EpisodeResponse> = mutableListOf()
) {
    data class EpisodeResponse(
        var title: String? = null,
        var link: String? = null,
        var description: String? = null,
        var pubDate: String? = null,
        var duration: String? = null,
        var episodeUrl: String? = null,
        var imageUrl: String? = null,
        var podcastName:String?=null
//        var type: String? = null
//        var guid: String? = null,
    )
}