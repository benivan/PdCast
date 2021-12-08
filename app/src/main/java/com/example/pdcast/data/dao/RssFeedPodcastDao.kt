package com.example.pdcast.data.dao

import androidx.paging.PagingSource
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


    @Query("SELECT * FROM podcast_table WHERE isSubscribe =:isSubscribed ORDER BY podcastId ASC")
    fun getAllPodcasts(isSubscribed: Boolean): Flow<List<DBRssFeedPodcast>>

    @Transaction
    @Query("SELECT * FROM podcast_table WHERE podcastId =:podcastId")
    fun getEpisodeWithPodcast(podcastId: Long): Flow<PodcastsWithEpisodes>

    @Transaction
    @Query("SELECT * FROM podcasts_episodes_table WHERE podcastId =:podcastId")
    fun pagingSourceEpisodesWithPodcastId(podcastId: Long): PagingSource<Int, DBPodcastsEpisodes>

    @Transaction
    @Query("SELECT * FROM podcast_table WHERE title=:podcastTitle")
    fun getEpisodeWithPodcast(podcastTitle: String): Flow<PodcastsWithEpisodes>

    @Query("SELECT podcastId from podcast_table Where podcastFeedUrl=:feedLink")
    suspend fun getPodcastIdWithFeedUrl(feedLink: String): Long?

    @Query("SELECT * FROM podcast_table WHERE podcastFeedUrl =:feedLink")
    suspend fun getPodcastWithFeedUrl(feedLink: String):DBRssFeedPodcast

    @Update(entity = DBRssFeedPodcast::class)
    suspend fun updateSubscribePodCast(podcast: DBRssFeedPodcast)


}