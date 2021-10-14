package com.example.pdcast.ui

import android.app.Application
import android.icu.text.CaseMap
import android.support.v4.media.session.MediaControllerCompat
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.pdcast.data.database.PodcastSubscribedDatabase
import com.example.pdcast.data.dto.DBPodcast
import com.example.pdcast.data.dto.DBPodcastsEpisodes
import com.example.pdcast.data.dto.DBRssFeedPodcast
import com.example.pdcast.data.dto.relations.PodcastsWithEpisodes
import com.example.pdcast.data.repository.PodcastRepository
import com.example.pdcast.data.repository.RssFeedPodcastRepository
import com.example.pdcast.data.response.RssFeedResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resumeWithException

class MainViewModel(application: Application) : AndroidViewModel(application) {

    val readAllDataDB: LiveData<List<DBPodcast>>
    val readAllPodcast: LiveData<List<DBRssFeedPodcast>>
     var podcastsWithEpisodes:LiveData<List<PodcastsWithEpisodes>>? = null

    private val podcastRepository: PodcastRepository
    private val rssFeedPodcastRepository: RssFeedPodcastRepository

    init {
        val podcastSubscribeDao =
            PodcastSubscribedDatabase.getDatabase(application).podcastSubscribeDao()
        podcastRepository = PodcastRepository(podcastSubscribeDao)
        readAllDataDB = podcastRepository.readAllDataDB


        val rssFeedPodcastDao =
            PodcastSubscribedDatabase.getDatabase(application).rssFeedPodcastDao()
        rssFeedPodcastRepository = RssFeedPodcastRepository(rssFeedPodcastDao)

        readAllPodcast = rssFeedPodcastRepository.readAllPodcast

    }

    fun getPodcastDataWithTitle(title: String){
        podcastsWithEpisodes = rssFeedPodcastRepository.readPodcastsWithEpisodes(title)
    }
    fun getPodcastWithId(id:Long){
        podcastsWithEpisodes  = rssFeedPodcastRepository.readPodcastsWithEpisodes(id)
    }



    private var _controller: MediaControllerCompat? = null

    private val _isPlaying: MutableSharedFlow<Boolean> = MutableSharedFlow(1)

    val isPlaying = _isPlaying.asSharedFlow()

    private val _playFromUri: MutableSharedFlow<Boolean> = MutableSharedFlow(1)

    val playFromUri = _playFromUri.asSharedFlow()

    fun setController(controllerCompat: MediaControllerCompat) {
        Log.d(TAG, "setController: ${controllerCompat.playbackState}")
        _controller = controllerCompat
    }

    fun setPlaying(isPlayingX: Boolean) = viewModelScope.launch {
        _isPlaying.emit(isPlayingX)
    }

    fun startPlayFromUri(isPlayFromUri: Boolean) = viewModelScope.launch {
        _playFromUri.emit(isPlayFromUri)
    }


    fun addPodcast(DBPodcast: DBPodcast) {
        viewModelScope.launch(Dispatchers.IO) {
            podcastRepository.addPodcast(DBPodcast)
        }
    }

    fun addRssPodcast(podcast: DBRssFeedPodcast, callback: (Long) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val podcastId = rssFeedPodcastRepository.addPodcast(podcast)
            callback(podcastId)
        }
    }

    fun addPodcastsEpisodes(podcastId: Long, episodes: List<RssFeedResponse.EpisodeResponse>) {
        viewModelScope.launch {
            val episodesList = episodes.map {
                DBPodcastsEpisodes(
                    title = it.title.toString(),
                    podcastId = podcastId,
                    link = it.link.toString(),
                    description = it.description.toString(),
                    pubDate = it.pubDate.toString(),
                    duration = it.duration.toString(),
                    episodeUrl = it.episodeUrl.toString(),
                    imageUrl = it.imageUrl.toString(),
                    podcastName = it.podcastName.toString(),
                )
            }.toList()
            rssFeedPodcastRepository.addPodcastEpisode(episodesList)
        }
    }


    fun getControllerFromViewModel(): MediaControllerCompat? {
        return _controller
    }


    companion object {
        private const val TAG = "PlayerVIewModel"
    }


}