package com.example.pdcast.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.pdcast.data.dto.DBPodcastsEpisodes
import com.example.pdcast.data.dto.DBRssFeedPodcast
import com.example.pdcast.data.dto.relations.PodcastsWithEpisodes
import kotlinx.coroutines.flow.Flow

@Dao
interface RssFeedPodcastDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPodcast(podcast: DBRssFeedPodcast): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEpisodes(episode: List<DBPodcastsEpisodes>)

    @Query("SELECT * FROM podcast_table ORDER BY podcastId ASC")
    fun getAllPodcasts(): Flow<List<DBRssFeedPodcast>>

    @Transaction
    @Query("SELECT * FROM podcast_table WHERE podcastId =:podcastId")
    fun getEpisodeWithPodcast(podcastId: Long): Flow<List<PodcastsWithEpisodes>>

    @Transaction
    @Query("SELECT * FROM podcast_table WHERE title=:podcastTitle")
    fun getEpisodeWithPodcast(podcastTitle: String): Flow<List<PodcastsWithEpisodes>>

    @Query("SELECT podcastId from podcast_table Where podcastFeedUrl=:feedLink")
    fun getPodcastIdWithFeedUrl(feedLink: String): Long?
}