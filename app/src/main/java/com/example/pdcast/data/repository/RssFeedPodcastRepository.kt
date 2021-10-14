package com.example.pdcast.data.repository

import androidx.lifecycle.LiveData
import com.example.pdcast.data.dao.RssFeedPodcastDao
import com.example.pdcast.data.dto.DBPodcastsEpisodes
import com.example.pdcast.data.dto.DBRssFeedPodcast
import com.example.pdcast.data.dto.relations.PodcastsWithEpisodes

class RssFeedPodcastRepository(private val rssFeedPodcastDao: RssFeedPodcastDao){

    val readAllPodcast: LiveData<List<DBRssFeedPodcast>> =
        rssFeedPodcastDao.getAllPodcasts()



    fun readPodcastsWithEpisodes(podcastTitle:String):LiveData<List<PodcastsWithEpisodes>>{
       return rssFeedPodcastDao.getEpisodeWithPodcast(podcastTitle)
    }
    fun readPodcastsWithEpisodes(id:Long):LiveData<List<PodcastsWithEpisodes>>{
        return rssFeedPodcastDao.getEpisodeWithPodcast(id)
    }

    suspend fun addPodcast(podcast:DBRssFeedPodcast):Long{
        return rssFeedPodcastDao.insertPodcast(podcast)
    }
    suspend fun addPodcastEpisode(episodes: List<DBPodcastsEpisodes>){
        rssFeedPodcastDao.insertEpisodes(episodes)
    }


}