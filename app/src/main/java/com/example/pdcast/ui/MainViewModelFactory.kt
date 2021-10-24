package com.example.pdcast.ui

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.pdcast.data.repository.RssFeedPodcastRepository

@Suppress("UNCHECKED_CAST")
class MainViewModelFactory(
    private var rssFeedPodcastRepository: RssFeedPodcastRepository,
    private val application: Application
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(rssFeedPodcastRepository, application) as T
        }
        throw IllegalArgumentException("ViewModel class not found")
    }


}