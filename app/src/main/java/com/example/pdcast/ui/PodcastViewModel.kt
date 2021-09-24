package com.example.pdcast.ui

import android.content.Context
import android.media.MediaMetadata
import android.media.MediaMetadata.METADATA_KEY_ALBUM_ART_URI
import android.net.Uri
import android.os.Bundle
import android.provider.Settings.Secure.getString
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI
import android.support.v4.media.session.MediaControllerCompat
import android.util.Log
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pdcast.R
import com.example.pdcast.data.api.RssFeedService
import com.example.pdcast.data.model.PodcastModel
import com.example.pdcast.data.response.RssFeedResponse
import com.example.pdcast.ui.view.PodcastDetailAndEpisodeFragment
import com.example.pdcast.util.Resource
import com.example.pdcast.util.RssXmlParser
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.lang.Exception
import java.time.Duration

class PodcastViewModel : ViewModel() {


    private val _podcast = MutableSharedFlow<Resource<RssFeedResponse>>(1)

    val podcast: SharedFlow<Resource<RssFeedResponse>> = _podcast



    private val rssXmlParser = RssXmlParser()

    private var isFirstTime: Boolean = true



    fun fetchXmlFromfeedUrl(it: String) {

        val handler = CoroutineExceptionHandler { _, throwable ->
            throwable.printStackTrace()
        }

        viewModelScope.launch(handler) {
            if (isFirstTime) {
                isFirstTime = false
                try {
                    _podcast.emit(Resource.Loading())
                    val response = RssFeedService.getFeedXml(it)
                    val parseXml = rssXmlParser.parseXml(response.byteInputStream())

                    _podcast.emit(Resource.Success(parseXml))
                } catch (e: Exception) {
                    _podcast.emit(Resource.Failure(e))
                }

            }
        }

    }






    override fun onCleared() {
        viewModelScope.cancel()
    }


    companion object {
        private const val TAG = "PodcastViewModel"
    }
}



