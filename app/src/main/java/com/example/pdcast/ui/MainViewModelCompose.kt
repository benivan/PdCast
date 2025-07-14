package com.example.pdcast.ui

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.pdcast.data.dto.DBPodcastsEpisodes
import com.example.pdcast.data.dto.DBRssFeedPodcast
import com.example.pdcast.data.dto.relations.PodcastsWithEpisodes
import com.example.pdcast.data.repository.RssFeedPodcastRepository
import com.example.pdcast.data.response.RssFeedResponse
import com.example.pdcast.util.PaletteColor
import com.example.pdcast.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * Enhanced MainViewModel with Compose UI state management
 * Supports both legacy and modern UI systems
 */
class MainViewModel(
    private val rssFeedPodcastRepository: RssFeedPodcastRepository,
    application: Application
) : AndroidViewModel(application) {

    val readAllPodcast: Flow<List<DBRssFeedPodcast>> = rssFeedPodcastRepository.readAllPodcast

    private val _podcast = MutableSharedFlow<Resource<RssFeedResponse>>(0)
    val podcast: SharedFlow<Resource<RssFeedResponse>> = _podcast.asSharedFlow()

    private val _currentPlayingPosition = MutableSharedFlow<Long>(1)
    var currentPlayingPosition = _currentPlayingPosition.asSharedFlow()

    private val _nowPlayingMetaChanged = MutableStateFlow(false)
    var nowPlayingDataIsCHanged = _nowPlayingMetaChanged.asStateFlow()

    private val _paletteColor = MutableSharedFlow<PaletteColor>(1)
    var paletteColor = _paletteColor.asSharedFlow()

    private val _isPlaying: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isPlaying = _isPlaying.asStateFlow()

    private val _bufferLevel: MutableSharedFlow<Int> = MutableSharedFlow(0)
    val bufferLevel = _bufferLevel.asSharedFlow()

    private val _playFromUri: MutableSharedFlow<Boolean> = MutableSharedFlow(1)
    val playFromUri = _playFromUri.asSharedFlow()

    // New Compose-specific UI state
    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    data class UiState(
        val currentPodcast: DBRssFeedPodcast? = null,
        val isLoading: Boolean = false,
        val searchQuery: String = "",
        val podcasts: List<DBRssFeedPodcast> = emptyList(),
        val episodes: List<DBPodcastsEpisodes> = emptyList(),
        val currentEpisode: DBPodcastsEpisodes? = null,
        val playbackPosition: Long = 0L,
        val duration: Long = 0L,
        val paletteColor: PaletteColor? = null
    )

    init {
        // Collect podcast data and update UI state
        viewModelScope.launch {
            readAllPodcast.collect { podcasts ->
                _uiState.value = _uiState.value.copy(podcasts = podcasts)
            }
        }

        // Collect playing position and update UI state
        viewModelScope.launch {
            currentPlayingPosition.collect { position ->
                _uiState.value = _uiState.value.copy(playbackPosition = position)
            }
        }

        // Collect palette color and update UI state
        viewModelScope.launch {
            paletteColor.collect { color ->
                _uiState.value = _uiState.value.copy(paletteColor = color)
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun setLoading(isLoading: Boolean) {
        _uiState.value = _uiState.value.copy(isLoading = isLoading)
    }

    fun setCurrentPodcast(podcast: DBRssFeedPodcast) {
        _uiState.value = _uiState.value.copy(currentPodcast = podcast)
    }

    fun setCurrentEpisode(episode: DBPodcastsEpisodes) {
        _uiState.value = _uiState.value.copy(currentEpisode = episode)
    }

    fun playingDataIsChanged() {
        viewModelScope.launch {
            _currentPlayingPosition.resetReplayCache()
            Log.d(TAG, "playingDataIsChanged: DELETED REPLAY CACHE")
            _currentPlayingPosition.emit(0L)
            _nowPlayingMetaChanged.emit(true)
        }
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

    fun getPodcastsWithEpisodes(podcastId: Long) {
        viewModelScope.launch {
            val podcastsWithEpisodes = rssFeedPodcastRepository.getPodcastsWithEpisodes(podcastId)
            // Update UI state with episodes
            val episodes = podcastsWithEpisodes?.episodes ?: emptyList()
            _uiState.value = _uiState.value.copy(episodes = episodes)
        }
    }

    fun updatePosition(position: Long) {
        viewModelScope.launch {
            _currentPlayingPosition.emit(position)
        }
    }

    fun bufferingLevel(level: Int) {
        viewModelScope.launch {
            _bufferLevel.emit(level)
        }
    }

    fun searchPodcast(feedUrl: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                _podcast.emit(Resource.Loading())
                val response = rssFeedPodcastRepository.getRssFeedPodcast(feedUrl)
                _podcast.emit(Resource.Success(response))
            } catch (e: Exception) {
                _podcast.emit(Resource.Error(e.localizedMessage ?: "Unknown error"))
            } finally {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    companion object {
        private const val TAG = "MainViewModel"
    }
}