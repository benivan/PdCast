package com.example.pdcast.ui.view

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.annotation.RequiresApi
import androidx.core.graphics.drawable.toDrawable
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.pdcast.R
import com.example.pdcast.databinding.FragmentPlayerBinding
import com.example.pdcast.ui.MainViewModel
import com.example.pdcast.ui.PlayerViewModel
import com.google.android.material.transition.MaterialFadeThrough
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class PlayerFragment : Fragment() {

    private var _binding: FragmentPlayerBinding? = null
    private val binding: FragmentPlayerBinding get() = _binding!!


    private val mainViewModel: MainViewModel by activityViewModels()
    private val viewModel: PlayerViewModel by viewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        _binding = FragmentPlayerBinding.inflate(layoutInflater, container, false)

        mainViewModel.getController()?.let { viewModel.currentPosition(it) }
        return binding.root
    }


    @RequiresApi(Build.VERSION_CODES.Q)
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
            binding.playerSeekBar.max = data.elementAt(3)!!.toInt() * 1000

            if (mainViewModel.getController()?.playbackState == null) {
                binding.playerSeekBar.progress = data.elementAt(4)?.toInt() ?: 0
                binding.startDuration.text =
                    convertSecondIntoDuration(data.elementAt(4)!!.toInt() / 1000)
            }

            binding.playerPlayPauseButton.setOnClickListener {

                if (mainViewModel.getController()?.playbackState != null) {
                    if (mainViewModel.getController()?.playbackState?.state == PlaybackStateCompat.STATE_PAUSED) {
                        mainViewModel.getController()?.transportControls?.play()
                    }
                    if (mainViewModel.getController()?.playbackState?.state == PlaybackStateCompat.STATE_PLAYING) {
                        mainViewModel.getController()?.transportControls?.pause()
                    }
                } else if (mainViewModel.getController()?.playbackState == null) {
                    mainViewModel.startPlayFromUri(true)
                }
            }

            binding.playerForButton.setOnClickListener {
                mainViewModel.getController()?.let { it1 -> seekBy(it1, 30) }
                if (mainViewModel.getController()?.playbackState?.state == PlaybackStateCompat.STATE_PAUSED) {
                    mainViewModel.getController()?.transportControls?.play()
                }
            }

            binding.playerPreButton.setOnClickListener {
                mainViewModel.getController()?.let { it1 -> seekBy(it1, -10) }
                if (mainViewModel.getController()?.playbackState?.state == PlaybackStateCompat.STATE_PAUSED) {
                    mainViewModel.getController()?.transportControls?.play()
                }
            }
        }

        mainViewModel.paletteColor.onEach {
            val color = it.muted.toDrawable()
            color.alpha = 255 / 3
            binding.playerConstraintLayout.background = color
//            binding.playerSeekBar.horizontalScrollbarThumbDrawable = it.mutedDark.toDrawable()
////            binding.playerSeekBar.progressDrawable = it.mutedDark.toDrawable()
        }.launchIn(lifecycleScope)

        mainViewModel.isPlaying
            .onEach {
                if (it) binding.playerPlayPauseButton.setImageResource(R.drawable.pause)
                else binding.playerPlayPauseButton.setImageResource(R.drawable.play_arrow)
            }.launchIn(lifecycleScope)

        mainViewModel.bufferLevel
            .onEach {
                binding.playerSeekBar.secondaryProgress = it
            }.launchIn(lifecycleScope)

        mainViewModel.currentPlayingPosition
            .onEach {

                binding.playerSeekBar.progress = it.toInt()
                binding.startDuration.text =
                    convertSecondIntoDuration((it / 1000).toInt())

            }.launchIn(lifecycleScope)

        binding.playerSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    mainViewModel.getController()?.transportControls?.seekTo(progress.toLong())
                    if (mainViewModel.getController()?.playbackState == null) {
                        val sharedPref = requireActivity().getSharedPreferences(
                            getString(R.string.preference_file_key), Context.MODE_PRIVATE
                        )
                        with(sharedPref.edit()) {
                            putLong("NowPlayingPosition", progress.toLong())
                            apply()
                        }
                    }
                    if (mainViewModel.getController()?.playbackState?.state == PlaybackStateCompat.STATE_PAUSED) {

                        binding.playerSeekBar.progress = progress
                        binding.startDuration.text = convertSecondIntoDuration(progress / 1000)
                        mainViewModel.getController()?.transportControls?.play()
                    }
                    binding.startDuration.text = convertSecondIntoDuration(progress / 1000)
                    binding.playerSeekBar.progress = progress
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit

            override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit

        })

    }


    override fun onResume() {
        Log.d(TAG, "onResume: ")
        super.onResume()
    }

    private fun previouslyPlayedData(): Boolean {
        val sharedPref = requireActivity().getSharedPreferences(
            getString(R.string.preference_file_key), Context.MODE_PRIVATE
        )
        return sharedPref.getBoolean("IsPlayedBefore", false)
    }

    private fun getPositionFromShared(): Int {
        val sharedPref = requireActivity().getSharedPreferences(
            getString(R.string.preference_file_key), Context.MODE_PRIVATE
        )
        return sharedPref.getLong("NowPlayingPosition", 0L).toInt()
    }


    private fun getSharedPreData(): List<String?> {
        val sharedPref = requireActivity().getSharedPreferences(
            getString(R.string.preference_file_key), Context.MODE_PRIVATE
        )
        return listOf(
            sharedPref.getString("NowPlyingPodcastName", ""),
            sharedPref.getString("NowPlyingPodcastEpisodeName", ""),
            sharedPref.getString("NowPlayingMediaImage", ""),
            sharedPref.getString("NowPlayingEpisodeDuration", ""),
            sharedPref.getLong("NowPlayingPosition", 0L).toString()
        )
    }

    private fun seekBy(controller: MediaControllerCompat, second: Int) {
        val position = controller.playbackState.position + second * 1000
        controller.transportControls.seekTo(position)
    }

    companion object {
        private const val TAG = "PlayerFragment"
    }


    private fun convertSecondIntoDuration(duration: Int): String {
        var time = duration
        var convertedDuration: String = ""

        val hour = time / 3600

        time %= 3600
        val min = time / 60

        time %= 60
        val second = time

        convertedDuration = if (hour == 0) {
            "${addZero(min)}:${addZero(second)}"
        } else "${addZero(hour)}:${addZero(min)}:${addZero(second)}"
        return convertedDuration
    }

    private fun addZero(time: Int): String {
        return if (time.toString().length == 1) {
            "0$time"
        } else time.toString()

    }


}











