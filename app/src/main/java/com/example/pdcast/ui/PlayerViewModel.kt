package com.example.pdcast.ui


import android.support.v4.media.session.MediaControllerCompat
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pdcast.ui.MainViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class PlayerViewModel:ViewModel() {



    private val _position: MutableSharedFlow<Int> = MutableSharedFlow(1)
    val position = _position.asSharedFlow()


    fun  currentPosition(controller:MediaControllerCompat) {
        viewModelScope.launch {
            controller?.let {
                _position.emit(it.playbackState.position.toInt())
            }
            delay(1000)
            currentPosition(controller)
        }
    }

    companion object{
        private const val TAG = "PlayerViewModel"
    }

}

