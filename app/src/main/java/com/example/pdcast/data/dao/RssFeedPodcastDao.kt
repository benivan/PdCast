package com.example.pdcast.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.pdcast.data.dto.DBPodcastsEpisodes
import com.example.pdcast.data.dto.DBRssFeedPodcast
import com.example.pdcast.data.dto.relations.PodcastsWithEpisodes

@Dao
interface RssFeedPodcastDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPodcast(podcast: DBRssFeedPodcast):Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEpisodes(episode: List<DBPodcastsEpisodes>)

    @Query("SELECT * FROM podcast_table ORDER BY podcastId ASC")
    fun getAllPodcasts(): LiveData<List<DBRssFeedPodcast>>

    @Transaction
    @Query("SELECT * FROM podcast_table WHERE podcastId =:podcastId")
    fun getEpisodeWithPodcast(podcastId:Long):LiveData<List<PodcastsWithEpisodes>>

    @Transaction
    @Query("SELECT * FROM podcast_table WHERE title=:podcastTitle")
    fun getEpisodeWithPodcast(podcastTitle:String):LiveData<List<PodcastsWithEpisodes>>
}