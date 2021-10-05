package com.example.pdcast.ui

import android.support.v4.media.session.MediaControllerCompat
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class PlayerVIewModel : ViewModel() {

    private var _controller: MediaControllerCompat? = null

    private val _isPlaying: MutableSharedFlow<Boolean> = MutableSharedFlow(1)

    val isPlaying = _isPlaying.asSharedFlow()

    private val _playFromUri: MutableSharedFlow<Boolean> =MutableSharedFlow(1)

    val playFromUri = _playFromUri.asSharedFlow()

    fun setController(controllerCompat: MediaControllerCompat) {
        Log.d(TAG, "setController: ${controllerCompat.playbackState}")
        _controller = controllerCompat
    }

    fun setPlaying(isPlayingX: Boolean) = viewModelScope.launch {
        _isPlaying.emit(isPlayingX)
    }

    fun startPlayFromUri(isPlayFromUri:Boolean) = viewModelScope.launch{
        _playFromUri.emit(isPlayFromUri)
    }



    fun getControllerFromViewModel():MediaControllerCompat?{
       return _controller
    }


    companion object {
        private const val TAG = "PlayerVIewModel"
    }


}