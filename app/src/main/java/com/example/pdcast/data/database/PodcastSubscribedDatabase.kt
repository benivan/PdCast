package com.example.pdcast.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.pdcast.data.dao.PodcastSubscribeDao
import com.example.pdcast.data.dao.RssFeedPodcastDao
import com.example.pdcast.data.dto.DBPodcast
import com.example.pdcast.data.dto.DBPodcastsEpisodes
import com.example.pdcast.data.dto.DBRssFeedPodcast

@Database(entities = [DBPodcast::class,DBRssFeedPodcast::class,DBPodcastsEpisodes::class], version = 2, exportSchema = false)
abstract class PodcastSubscribedDatabase : RoomDatabase() {
    abstract fun podcastSubscribeDao(): PodcastSubscribeDao
    abstract fun rssFeedPodcastDao():RssFeedPodcastDao

    companion object {
        @Volatile
        private var INSTANCE: PodcastSubscribedDatabase? = null

        fun getDatabase(context: Context): PodcastSubscribedDatabase {
            val tempInstant = INSTANCE
            if (tempInstant != null) {
                return tempInstant
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PodcastSubscribedDatabase::class.java,
                    "podcast_subscribed_database"
                ).build()
                INSTANCE = instance
                return instance
            }
        }

    }
}