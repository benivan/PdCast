package com.example.pdcast.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.pdcast.data.model.ItunesPodcast
import com.example.pdcast.data.model.PodcastDataBaseModel

@Dao
interface PodcastSubscribeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPodcast(podcast: PodcastDataBaseModel)

    @Query("SELECT * FROM podcast_subscribe_table ORDER BY id ASC")
    fun readAllSubscribedPodcasts(): LiveData<List<PodcastDataBaseModel>>

}