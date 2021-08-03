package com.example.pdcast.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pdcast.data.api.RssFeedService
import com.example.pdcast.data.model.PodcastModel
import com.example.pdcast.data.response.RssFeedResponse
import com.example.pdcast.util.Resource
import com.example.pdcast.util.RssXmlParser
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.lang.Exception

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


    companion object {
        private const val TAG = "PodcastViewModel"
    }
}