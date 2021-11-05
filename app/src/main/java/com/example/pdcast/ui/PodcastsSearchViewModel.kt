package com.example.pdcast.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pdcast.data.dao.PodcastSubscribeDao
import com.example.pdcast.data.dto.DBPodcast
import com.example.pdcast.data.model.Podcast
import com.example.pdcast.data.repository.ItunesPodcastRepository
import com.example.pdcast.data.repository.PodcastRepository
import com.example.pdcast.util.Resource
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

class PodcastsSearchViewModel : ViewModel() {


    private val repository = ItunesPodcastRepository()



    private val _podcasts = MutableSharedFlow<Resource<Podcast>>(1)

    val podcasts: SharedFlow<Resource<Podcast>> = _podcasts

    private var itemPosition:Int = 0


    fun setPosition(position:Int){
        itemPosition = position
    }

    fun getPosition():Int{
        return itemPosition
    }



    fun getPodcastWithTerms(term: String) {
        viewModelScope.launch {
            try {
                _podcasts.emit(Resource.Loading())
                val podcastWithTerms = repository.getPodcastWithTerms(term)
                _podcasts.emit(Resource.Success(podcastWithTerms))
            } catch (e: Exception) {
                _podcasts.emit(Resource.Failure(e))
            }
        }
    }



    companion object {
        private const val TAG = "PodcastViewModel"
    }


}