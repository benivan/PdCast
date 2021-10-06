package com.example.pdcast.data.repository

import com.example.pdcast.data.api.ItunesService
import com.example.pdcast.data.model.ItunesPodcast
import com.example.pdcast.data.model.Podcast

class ItunesPodcastRepository() {

    private val podcastService = ItunesService.createItunesService()


    suspend fun getPodcastWithTerms(
        term: String
    ): Podcast {
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
        return Podcast(searchPodcastByTerms.resultCount, toList)
    }

    companion object {
        private const val TAG = "PodcastRepository"
    }
}