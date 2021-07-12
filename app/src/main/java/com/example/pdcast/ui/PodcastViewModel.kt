package com.example.pdcast.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pdcast.data.model.PodcastModel
import com.example.pdcast.data.repository.PodcastRepository
import com.example.pdcast.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PodcastViewModel : ViewModel() {

    private val repository = PodcastRepository()

    private val _podcasts = MutableStateFlow<Resource<PodcastModel>>(Resource.Loading())

    val podcasts: StateFlow<Resource<PodcastModel>> = _podcasts


    fun getPodcastWithTerms(term: String) {
        viewModelScope.launch {
            try {
                val podcastWithTerms = repository.getPodcastWithTerms(term)
                _podcasts.emit(Resource.Success(podcastWithTerms))
            } catch (e: Exception) {
                _podcasts.emit(Resource.Failure(e))
            }
        }
    }
}