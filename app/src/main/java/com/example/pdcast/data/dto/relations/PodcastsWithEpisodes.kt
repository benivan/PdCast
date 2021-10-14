package com.example.pdcast.data.dto.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.example.pdcast.data.dto.DBPodcastsEpisodes
import com.example.pdcast.data.dto.DBRssFeedPodcast

data class PodcastsWithEpisodes(
    @Embedded
    val podcast: DBRssFeedPodcast,

    @Relation(
        parentColumn = "podcastId",
        entityColumn = "podcastId"
    )
    val episodes: List<DBPodcastsEpisodes>,
)