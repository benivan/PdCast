package com.example.pdcast.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.pdcast.data.database.PodcastSubscribedDatabase
import com.example.pdcast.data.dto.DBPodcast
import com.example.pdcast.data.repository.PodcastRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainPodcastViewModel(application: Application) : AndroidViewModel(application) {

    val readAllDataDB: LiveData<List<DBPodcast>>
    private val podcastRepository: PodcastRepository

    init {
        val podcastSubscribeDao =
            PodcastSubscribedDatabase.getDatabase(application).podcastSubscribeDao()
        podcastRepository = PodcastRepository(podcastSubscribeDao)
        readAllDataDB = podcastRepository.readAllDataDB
    }

    fun addPodcast(DBPodcast: DBPodcast) {
        viewModelScope.launch(Dispatchers.IO) {
            podcastRepository.addPodcast(DBPodcast)
        }
    }


}