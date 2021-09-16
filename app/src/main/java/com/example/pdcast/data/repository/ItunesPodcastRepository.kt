package com.example.pdcast.data.repository

import android.util.Log
import com.example.pdcast.data.api.ItunesService
import com.example.pdcast.data.api.RssFeedService
import com.example.pdcast.data.dao.PodcastSubscribeDao
import com.example.pdcast.data.model.ItunesPodcast
import com.example.pdcast.data.model.PodcastModel
import com.example.pdcast.data.response.PodcastResponse
import kotlin.random.Random

class ItunesPodcastRepository() {

    private val podcastService = ItunesService.createItunesService()


    suspend fun getPodcastWithTerms(
        term: String
    ): PodcastModel {
        val searchPodcastByTerms = podcastService.searchPodcastByTerms(term)
        val toList = searchPodcastByTerms.results.map {
            ItunesPodcast(
                collectionCensoredName = it.collectionCensoredName,
                feedUrl = it.feedUrl ?: "",
                artworkUrl30 = it.artworkUrl30,
                artworkUrl100 = it.artworkUrl100,
                releaseDate = it.releaseDate
            )
        }.toList()
        return PodcastModel(searchPodcastByTerms.resultCount, toList)
    }

    companion object {
        private const val TAG = "PodcastRepository"
    }
}