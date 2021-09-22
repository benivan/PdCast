package com.example.pdcast.data.repository

import androidx.lifecycle.LiveData
import com.example.pdcast.data.dao.PodcastSubscribeDao
import com.example.pdcast.data.model.PodcastDataBaseModel

class PodcastRepository(private val podcastSubscribeDao: PodcastSubscribeDao) {

    val readAllData: LiveData<List<PodcastDataBaseModel>> =
        podcastSubscribeDao.readAllSubscribedPodcasts()

    suspend fun addPodcast(podcastDataBaseModel: PodcastDataBaseModel) {
        podcastSubscribeDao.insertPodcast(podcastDataBaseModel)
    }
}