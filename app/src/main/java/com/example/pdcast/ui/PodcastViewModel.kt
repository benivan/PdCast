package com.example.pdcast.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pdcast.data.api.RssFeedService
import com.example.pdcast.data.database.PodcastSubscribedDatabase
import com.example.pdcast.data.dto.DBPodcastsEpisodes
import com.example.pdcast.data.model.PodcastEpisodeModel
import com.example.pdcast.data.model.PodcastModel
import com.example.pdcast.data.repository.RssFeedPodcastRepository
import com.example.pdcast.data.response.RssFeedResponse
import com.example.pdcast.util.Resource
import com.example.pdcast.util.RssXmlParser
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.lang.Exception
import kotlin.math.log

class PodcastViewModel() : ViewModel() {


    private val _podcast = MutableSharedFlow<Resource<RssFeedResponse>>(1)
    val podcast: SharedFlow<Resource<RssFeedResponse>> = _podcast

    fun setRssFeedPodcastRepository(
        rssFeedPodcastRepository: RssFeedPodcastRepository,
        feedUrl: String
    ) {
        val handler = CoroutineExceptionHandler { _, throwable ->
            throwable.printStackTrace()
            _podcast.tryEmit(Resource.Failure(throwable))
        }
        viewModelScope.launch(handler + Dispatchers.IO) {
            _podcast.emit(Resource.Loading())
            try {
                Log.d(TAG, "getPodcastWithFeedUrl: ")
                rssFeedPodcastRepository.getPodcastFromFeed(feedUrl).collect {
                    val podcast = it.first()
                    _podcast.emit(Resource.Success(
                        RssFeedResponse(
                            title = podcast.podcast.title,
                            description = podcast.podcast.description,
                            language = podcast.podcast.language,
                            link = podcast.podcast.link,
                            imageUrl = podcast.podcast.imageUrl,
                            episodes = podcast.episodes.map {
                                RssFeedResponse.EpisodeResponse(
                                    title = it.title,
                                    link = it.link,
                                    description = it.description,
                                    pubDate = it.pubDate,
                                    duration = it.duration,
                                    episodeUrl = it.episodeUrl,
                                    imageUrl = it.imageUrl,
                                    podcastName = it.podcastName
                                )
                            }.toMutableList()
                        )
                    ))
                }


            } catch (e: Exception) {
                _podcast.emit(Resource.Failure(e))
            }

        }
    }


    override fun onCleared() {
        viewModelScope.cancel()
    }


    companion object {
        private const val TAG = "PodcastViewModel"
    }
}



