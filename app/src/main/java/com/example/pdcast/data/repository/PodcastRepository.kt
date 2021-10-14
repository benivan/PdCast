package com.example.pdcast.data.repository

import androidx.lifecycle.LiveData
import com.example.pdcast.data.dao.PodcastSubscribeDao
import com.example.pdcast.data.dao.RssFeedPodcastDao
import com.example.pdcast.data.dto.DBPodcast
import com.example.pdcast.data.dto.DBRssFeedPodcast

class PodcastRepository(private val podcastSubscribeDao: PodcastSubscribeDao) {

    val readAllDataDB: LiveData<List<DBPodcast>> =
        podcastSubscribeDao.readAllSubscribedPodcasts()

    suspend fun addPodcast(dbPodcast: DBPodcast) {
        podcastSubscribeDao.insertPodcast(dbPodcast)
    }

}