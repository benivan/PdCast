package com.example.pdcast.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pdcast.data.model.PodcastModel
import com.example.pdcast.data.repository.ItunesPodcastRepository
import com.example.pdcast.util.Resource
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class PodcastsSearchViewModel : ViewModel() {

    private val repository = ItunesPodcastRepository()

    private val _podcasts = MutableStateFlow<Resource<PodcastModel>>(Resource.None())

    val podcasts: StateFlow<Resource<PodcastModel>> = _podcasts

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