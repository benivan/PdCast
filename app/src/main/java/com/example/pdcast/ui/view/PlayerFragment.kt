package com.example.pdcast.ui.view

import android.content.Context
import android.os.Bundle
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.pdcast.R
import com.example.pdcast.databinding.FragmentPlayerBinding
import com.example.pdcast.ui.MainViewModel
import com.google.android.material.transition.MaterialFadeThrough
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlin.time.Duration

class PlayerFragment : Fragment() {

    private var _binding: FragmentPlayerBinding? = null
    private val binding: FragmentPlayerBinding get() = _binding!!


    private val mainViewModel: MainViewModel by activityViewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        _binding = FragmentPlayerBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        enterTransition = MaterialFadeThrough()
        exitTransition = MaterialFadeThrough()

        if (previouslyPlayedData()) {
            val data = getSharedPreData()
            binding.PodcastName.text = data.elementAt(0)
            binding.podcastEpisodeName.text = data.elementAt(1)
            Glide.with(this).load(data.elementAt(2)).into(binding.podcastImage)
            binding.endDuration.text = convertSecondIntoDuration(data.elementAt(3)!!.toInt())

            binding.playerPlayPauseButton.setOnClickListener {
                if (mainViewModel.getControllerFromViewModel()?.playbackState != null) {
                    if (mainViewModel.getControllerFromViewModel()?.playbackState?.state == PlaybackStateCompat.STATE_PAUSED) {
                        mainViewModel.getControllerFromViewModel()?.transportControls?.play()
                    }
                    if (mainViewModel.getControllerFromViewModel()?.playbackState?.state == PlaybackStateCompat.STATE_PLAYING) {
                        mainViewModel.getControllerFromViewModel()?.transportControls?.pause()
                    }
                }
                if (mainViewModel.getControllerFromViewModel()?.playbackState == null) {
                    mainViewModel.startPlayFromUri(true)
                }
            }

            binding.playerForButton.setOnClickListener {
                mainViewModel.getControllerFromViewModel().let {
                    if (it != null) {
                        seekBy(it, 30)
                    }
                }

            }

            binding.playerPreButton.setOnClickListener {
                mainViewModel.getControllerFromViewModel().let {
                    if (it != null) {
                        seekBy(it, -10)
                    }
                }
            }

        }

        viewLifecycleOwner.lifecycleScope.launch {
            currentPosition()
        }
        mainViewModel.isPlaying
            .onEach {
                if (it) binding.playerPlayPauseButton.setImageResource(R.drawable.pause)
                else binding.playerPlayPauseButton.setImageResource(R.drawable.play_arrow)
            }.launchIn(lifecycleScope)


    }

    override fun onResume() {
        Log.d(TAG, "onResume: ")
        super.onResume()
        viewLifecycleOwner.lifecycleScope.launch {
            currentPosition()
        }
    }

    private fun previouslyPlayedData(): Boolean {
        val sharedPref = requireActivity().getSharedPreferences(
            getString(R.string.preference_file_key), Context.MODE_PRIVATE
        )
        return sharedPref.getBoolean("IsPlayedBefore", false)
    }

    private fun getNowPlayingPodcastDurationFromSharedPre(): Long {
        val sharedPref = requireActivity().getSharedPreferences(
            getString(R.string.preference_file_key), Context.MODE_PRIVATE
        )
        return sharedPref.getString("NowPlayingEpisodeDuration", "0")?.toLong() ?: 0
    }


    private fun getSharedPreData(): List<String?> {
        val sharedPref = requireActivity().getSharedPreferences(
            getString(R.string.preference_file_key), Context.MODE_PRIVATE
        )
        return listOf(
            sharedPref.getString("NowPlyingPodcastName", ""),
            sharedPref.getString("NowPlyingPodcastEpisodeName", ""),
            sharedPref.getString("NowPlayingMediaImage", ""),
            sharedPref.getString("NowPlayingEpisodeDuration","")
        )
    }

    private fun seekBy(controller: MediaControllerCompat, second: Int) {
        val position = controller.playbackState.position + second * 1000
        controller.transportControls.seekTo(position)
    }

    private suspend fun currentPosition() {
        mainViewModel.getControllerFromViewModel()?.let {
            val duration = getNowPlayingPodcastDurationFromSharedPre().toString()
            val position = it.playbackState.position
            binding.playerSeekBar.max = duration.toInt() * 1000
            binding.playerSeekBar.progress = (position).toInt()
            binding.startDuration.text = convertSecondIntoDuration(position.toInt()/1000)
            delay(1000)
            currentPosition()
        }
    }

    private fun convertSecondIntoDuration(duration: Int): String {
        var time = duration
        var convertedDuration:String = ""

        val hour  = time/3600

        time %= 3600
        val min = time/60

        time %= 60
        val second = time

//        when {
//            hour != 0 -> {
//                convertedDuration += if (hour < 10){
//                    "0$hour"
//                } else "$hour:"
//            }
//            min != 0 -> {
//                convertedDuration += if (hour < 10){
//                    "0$min:"
//                } else "$min:"
//            }
//            second != 0 -> {
//                convertedDuration += if (hour < 10){
//                    "0$second"
//                } else "$second"
//            }
//        }

        convertedDuration = "$hour:$min:$second"
        return convertedDuration
    }


    companion object {
        private const val TAG = "PlayerFragment"
    }


}