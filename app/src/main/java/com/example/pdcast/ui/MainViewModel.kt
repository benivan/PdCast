package com.example.pdcast.ui

import android.app.Application
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.pdcast.data.dto.DBPodcastsEpisodes
import com.example.pdcast.data.dto.DBRssFeedPodcast
import com.example.pdcast.data.dto.relations.PodcastsWithEpisodes
import com.example.pdcast.data.repository.RssFeedPodcastRepository
import com.example.pdcast.data.response.RssFeedResponse
import com.example.pdcast.util.PaletteColor
import com.example.pdcast.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch


class MainViewModel(
    private val rssFeedPodcastRepository: RssFeedPodcastRepository,
    application: Application
) :
    AndroidViewModel(application) {

    val readAllPodcast: Flow<List<DBRssFeedPodcast>> = rssFeedPodcastRepository.readAllPodcast

    var podcastsWithEpisodes: LiveData<List<PodcastsWithEpisodes>>? = null

    private val _podcast = MutableSharedFlow<Resource<RssFeedResponse>>(0)
    val podcast: SharedFlow<Resource<RssFeedResponse>> = _podcast.asSharedFlow()

    private var controller: MediaControllerCompat? = null

    private val _currentPlayingPosition = MutableSharedFlow<Long>(1)
    var currentPlayingPosition = _currentPlayingPosition.asSharedFlow()

    private val _nowPlayingMetaChanged = MutableStateFlow(false)
    var nowPlayingDataIsCHanged = _nowPlayingMetaChanged.asStateFlow()

    private val _paletteColor = MutableSharedFlow<PaletteColor>(1)
    var paletteColor = _paletteColor.asSharedFlow()


    private val _isPlaying: MutableSharedFlow<Boolean> = MutableSharedFlow(1)
    val isPlaying = _isPlaying.asSharedFlow()

    private val _bufferLevel: MutableSharedFlow<Int> = MutableSharedFlow(0)
    val bufferLevel = _bufferLevel.asSharedFlow()

    private val _playFromUri: MutableSharedFlow<Boolean> = MutableSharedFlow(1)

    val playFromUri = _playFromUri.asSharedFlow()


    fun setController(controllerCompat: MediaControllerCompat) {

        controller = controllerCompat
        viewModelScope.launch {
//            Log.d(TAG, "setController: ReplayCache${currentPlayingPosition.replayCache}")
            if (controller!!.playbackState.state == PlaybackStateCompat.STATE_PLAYING) {
                controllerCompat.playbackState.position.let {
                        if (it > 990L) {
                            Log.d(TAG, "setController: $it")
                            _currentPlayingPosition.emit(it)
                        }

                }
            }
            delay(1000)
            setController(controllerCompat)
        }

    }




    fun playingDataIsChanged() {
        viewModelScope.launch {
            _currentPlayingPosition.resetReplayCache()
            Log.d(TAG, "playingDataIsChanged: DELETED REPLAY CACHE ${_currentPlayingPosition.replayCache.indexOf(0)}")
            _currentPlayingPosition.emit(0L)
            _nowPlayingMetaChanged.emit(true)
        }

    }


    fun getController(): MediaControllerCompat? {

        return controller
    }

    fun paletteColor(paletteColor: PaletteColor) {
        viewModelScope.launch {
            _paletteColor.emit(paletteColor)
        }

    }


    fun setPlaying(isPlayingX: Boolean) = viewModelScope.launch {
        _isPlaying.emit(isPlayingX)
    }

    fun startPlayFromUri(isPlayFromUri: Boolean) = viewModelScope.launch {
        _playFromUri.emit(isPlayFromUri)
    }

    fun getRssFeedPodcastRepository(): RssFeedPodcastRepository {
        return rssFeedPodcastRepository
    }


    fun addRssPodcast(podcast: DBRssFeedPodcast, callback: (Long) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val podcastId = rssFeedPodcastRepository.addPodcast(podcast)
            callback(podcastId)
        }
    }

    suspend fun addPodcastToSubscribe(feedUrl: String) {
        rssFeedPodcastRepository.addPodcastToSubscribed(feedUrl)
    }

    suspend fun removePodcastFromSubscribeTable(feedUrl: String) {
        rssFeedPodcastRepository.removePodcastToSubscribe(feedUrl)
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

    fun bufferingLevel() {
        Log.d(TAG, "bufferingLevel: Called")
        viewModelScope.launch {
            getController()?.playbackState?.bufferedPosition?.toInt()?.let { _bufferLevel.emit(it) }
        }
    }


    companion object {
        private const val TAG = "PlayerVIewModel"
    }


}