package com.example.pdcast.data.repository

import android.util.Log
import androidx.lifecycle.LiveData
import com.example.pdcast.data.api.RssFeedService
import com.example.pdcast.data.dao.RssFeedPodcastDao
import com.example.pdcast.data.dto.DBPodcastsEpisodes
import com.example.pdcast.data.dto.DBRssFeedPodcast
import com.example.pdcast.data.dto.relations.PodcastsWithEpisodes
import com.example.pdcast.data.response.RssFeedResponse
import com.example.pdcast.util.RssXmlParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext

class RssFeedPodcastRepository(
    private val rssFeedPodcastDao: RssFeedPodcastDao,
    private val rssFeedService: RssFeedService
) {

    private val rssXmlParser = RssXmlParser()
    val readAllPodcast: Flow<List<DBRssFeedPodcast>> =
        rssFeedPodcastDao.getAllPodcasts()

    suspend fun getPodcastFromFeed(feedLink: String): Flow<List<PodcastsWithEpisodes>>{
        return withContext(Dispatchers.IO) {
            val podcastId = try {
                getPodcastIdWithFeedUrl(feedLink)!!
            } catch (e: Exception) {
                getPodcastFromNetwork(feedLink)
            }
            return@withContext readPodcastsWithEpisodes(podcastId)
        }
    }

    private suspend fun getPodcastFromNetwork(feedLink: String): Long {
        val response = rssFeedService.getFeedXml(feedLink)
        val parseXml = rssXmlParser.parseXml(response.byteInputStream())


        val podcastId = addPodcastWIthFeedUrl(parseXml, feedLink)
        val episodesList = parseXml.episodes.map {
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
        withContext(Dispatchers.IO) {
            addPodcastEpisode(episodesList)
        }
        Log.d(TAG, "getPodcastFromNetwork:$podcastId ")
        return podcastId
    }


    private suspend fun addPodcastWIthFeedUrl(
        data: RssFeedResponse,
        feedLink: String,
    ): Long {
        return addPodcast(
            DBRssFeedPodcast(
                title = data.title.toString(),
                podcastFeedUrl = feedLink,
                description = data.description.toString(),
                language = data.language.toString(),
                link = data.link.toString(),
                imageUrl = data.imageUrl.toString()
            )
        )
    }
//
//    fun readPodcastsWithEpisodes(podcastTitle: String): LiveData<List<PodcastsWithEpisodes>> {
//        return rssFeedPodcastDao.getEpisodeWithPodcast(podcastTitle)
//    }

    private suspend fun readPodcastsWithEpisodes(id: Long): Flow<List<PodcastsWithEpisodes>> {
        Log.d(TAG, "readPodcastsWithEpisodes: ")
        return rssFeedPodcastDao.getEpisodeWithPodcast(id)
    }

    private  fun getPodcastIdWithFeedUrl(feedUrl: String): Long? {
        return rssFeedPodcastDao.getPodcastIdWithFeedUrl(feedUrl)
    }

    suspend fun addPodcast(podcast: DBRssFeedPodcast): Long {
        return rssFeedPodcastDao.insertPodcast(podcast)
    }

    suspend fun addPodcastEpisode(episodes: List<DBPodcastsEpisodes>) {
        rssFeedPodcastDao.insertEpisodes(episodes)
    }

    companion object {
        private const val TAG = "RssFeedPodcastRepository"
    }

}