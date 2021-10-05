package com.example.pdcast.ui.view
import android.content.Context
import android.os.Bundle
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.pdcast.R
import com.example.pdcast.data.model.ItunesPodcast
import com.example.pdcast.databinding.FragmentPlayerBinding
import com.example.pdcast.mediaPlayer.MediaPlaybackService
import com.example.pdcast.ui.PlayerVIewModel
import com.example.pdcast.util.PState
import com.google.android.material.transition.MaterialFadeThrough
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class PlayerFragment : Fragment() {

    private var _binding:FragmentPlayerBinding? =null
    private val binding:FragmentPlayerBinding get() = _binding!!


    private val vIewModel:PlayerVIewModel by activityViewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
       super.onCreateView(inflater, container, savedInstanceState)
       _binding = FragmentPlayerBinding.inflate(layoutInflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        enterTransition = MaterialFadeThrough()
        exitTransition = MaterialFadeThrough()

        if (previouslyPlayedData()){
           val data =  getSharedPreData()
            binding.PodcastName.text  = data.elementAt(0)
            binding.podcastEpisodeName.text =data.elementAt(1)
            Glide.with(this).load(data.elementAt(2)).into(binding.podcastImage)

            binding.playerPlayPauseButton.setOnClickListener {
                if(vIewModel.getControllerFromViewModel()?.playbackState != null) {
                    if (vIewModel.getControllerFromViewModel()?.playbackState?.state == PlaybackStateCompat.STATE_PAUSED) {
                        vIewModel.getControllerFromViewModel()?.transportControls?.play()
                    }
                    if (vIewModel.getControllerFromViewModel()?.playbackState?.state == PlaybackStateCompat.STATE_PLAYING) {
                        vIewModel.getControllerFromViewModel()?.transportControls?.pause()
                    }
                }
                if(vIewModel.getControllerFromViewModel()?.playbackState == null){
                    vIewModel.startPlayFromUri(true)
                }
            }

            binding.playerForButton.setOnClickListener {
                vIewModel.getControllerFromViewModel().let {
                    if (it != null) {
                        seekBy(it,30)
                    }
                }

            }

            binding.playerPreButton.setOnClickListener {
                vIewModel.getControllerFromViewModel().let {
                    if (it != null) {
                        seekBy(it,-10)
                    }
                }
            }

        }

        vIewModel.isPlaying
            .onEach {
                if (it) binding.playerPlayPauseButton.setImageResource(R.drawable.pause)
                else  binding.playerPlayPauseButton.setImageResource(R.drawable.play_arrow)
            }.launchIn(lifecycleScope)


    }


    private fun previouslyPlayedData(): Boolean {
        val sharedPref = requireActivity().getSharedPreferences(
            getString(R.string.preference_file_key), Context.MODE_PRIVATE
        )
        return sharedPref.getBoolean("IsPlayedBefore", false)
    }

    private fun getSharedPreData(): List<String?> {
        val sharedPref = requireActivity().getSharedPreferences(
            getString(R.string.preference_file_key), Context.MODE_PRIVATE
        )
        return listOf(
            sharedPref.getString("NowPlyingPodcastName",""),
            sharedPref.getString("NowPlyingPodcastEpisodeName",""),
            sharedPref.getString("NowPlayingMediaImage",""),
        )
    }

    private fun seekBy(controller: MediaControllerCompat, second: Int) {
        val position = controller.playbackState.position + second * 1000
        controller.transportControls.seekTo(position)
    }


    companion object{
        private const val TAG = "PlayerFragment"
    }





}