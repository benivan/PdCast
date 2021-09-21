package com.example.pdcast.ui

import android.app.Application
import android.support.v4.media.session.MediaControllerCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.pdcast.Hilt_PdCastApplication
import com.example.pdcast.data.dao.PodcastSubscribeDao
import com.example.pdcast.data.database.PodcastSubscribedDatabase
import com.example.pdcast.data.model.PodcastDataBaseModel
import com.example.pdcast.data.repository.PodcastRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainPodcastViewModel(application: Application) : AndroidViewModel(application) {

    val readAllData: LiveData<List<PodcastDataBaseModel>>
    private val podcastRepository: PodcastRepository

    init {
        val podcastSubscribeDao =
            PodcastSubscribedDatabase.getDatabase(application).podcastSubscribeDao()
        podcastRepository = PodcastRepository(podcastSubscribeDao)
        readAllData = podcastRepository.readAllData
    }

    fun addPodcast(podcast: PodcastDataBaseModel) {
        viewModelScope.launch(Dispatchers.IO) {
            podcastRepository.addPodcast(podcast)
        }
    }


}