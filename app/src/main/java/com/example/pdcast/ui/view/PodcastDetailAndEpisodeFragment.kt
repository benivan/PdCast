package com.example.pdcast.ui.view

import android.content.ComponentName
import android.content.Context
import android.media.MediaMetadata
import android.net.Uri
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.pdcast.data.response.RssFeedResponse
import com.example.pdcast.databinding.FragmentPodcastDetailAndEpisodeBinding
import com.example.pdcast.mediaPlayer.MediaPlaybackService
import com.example.pdcast.ui.PlayerVIewModel
import com.example.pdcast.ui.PodcastViewModel
import com.example.pdcast.util.Resource
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlin.coroutines.suspendCoroutine as suspendCoroutine


class PodcastDetailAndEpisodeFragment : Fragment() {

    private lateinit var mediaBrowser: MediaBrowserCompat
//    private var mediaControllerCallback: MediaControllerCallback? = null

    private val viewModel: PodcastViewModel by viewModels()

    private val playerViewModel: PlayerVIewModel by activityViewModels()

    private var _binding: FragmentPodcastDetailAndEpisodeBinding? = null

    private val binding: FragmentPodcastDetailAndEpisodeBinding get() = _binding!!

    lateinit var feedLink: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            feedLink = it.getString("feedLink").toString()
        }
        viewModel.fetchXmlFromfeedUrl(feedLink)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        _binding = FragmentPodcastDetailAndEpisodeBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val handler = CoroutineExceptionHandler { _, throwable ->
            throwable.printStackTrace()
        }

        binding.errorTextView.visibility = View.GONE
        binding.progressBarEpisodePage.visibility = View.GONE

        viewModel.podcast.flowWithLifecycle(lifecycle).onEach {
            when (it) {
                is Resource.Failure -> {
                    binding.errorTextView.visibility = View.VISIBLE
                    binding.progressBarEpisodePage.visibility = View.GONE
                    binding.errorTextView.text = it.toString()
                }
                is Resource.Loading -> {
                    binding.progressBarEpisodePage.visibility = View.VISIBLE
                    binding.errorTextView.visibility = View.GONE
                }
                is Resource.Success -> {
                    binding.progressBarEpisodePage.visibility = View.GONE
                    binding.errorTextView.visibility = View.GONE
                    binding.tvDescription.visibility = View.GONE
                    binding.tvDescription.text = it.data.description
                    binding.tvLanguage.text = it.data.language
                    binding.tvTitle.text = it.data.title
                    Glide.with(this).load(it.data.imageUrl).into(binding.imageView)

                    binding.rvEpisode.adapter =
                        EpisodeItemAdapter(it.data.episodes, object : EpisodeListener {
                            override fun onEpisodeClicked(episodeViewData: RssFeedResponse.EpisodeResponse) {
                                val controller =
                                    MediaControllerCompat.getMediaController(requireActivity())

                                val activity = requireActivity()
                                if (controller.playbackState != null) {
                                    if (controller.playbackState.state ==
                                        PlaybackStateCompat.STATE_PLAYING
                                    ) {
//                                        controller.transportControls.pause()
                                        startPlaying(episodeViewData, activity)
                                    } else if (controller.playbackState.state == PlaybackStateCompat.STATE_PAUSED) {
                                        startPlaying(episodeViewData, activity)
                                    }
                                } else {
                                    startPlaying(episodeViewData, activity)
                                }
                            }

                            override fun onSelectedEpisode(episodeViewData: RssFeedResponse.EpisodeResponse) {
                            }
                        })
                    binding.rvEpisode.layoutManager = LinearLayoutManager(requireContext())

                }
                is Resource.None -> {

                }
            }
        }.launchIn(lifecycleScope)
    }

    fun startPlaying(episodeViewData: RssFeedResponse.EpisodeResponse,fragmentActivity: FragmentActivity) {
//        val fragmentActivity = activity as FragmentActivity

        val sharedPref = fragmentActivity.getSharedPreferences(
            "SharePreferencePdCast", Context.MODE_PRIVATE
        )
        with(sharedPref.edit()) {
            putBoolean("IsPlayedBefore",true)
            putString("NowPlayingMediaLink",episodeViewData.episodeUrl)
            putString("NowPlayingMediaImage", episodeViewData.imageUrl)
            putString("NowPlayingEpisodeDuration",episodeViewData.duration)
            putLong("NowPlayingPosition", 0L)
            putString("NowPlyingPodcastName",episodeViewData.podcastName)
            putString("NowPlyingPodcastEpisodeName",episodeViewData.title)
            apply()
        }

        fun removeColon(duration: String):Long{
            return if(duration.contains(":")){
                duration.replace(":","").toLong()
            } else duration.toLong()
        }

        val controller = MediaControllerCompat.getMediaController(fragmentActivity)

        val bundle = Bundle()

        bundle.putString(
            MediaMetadataCompat.METADATA_KEY_TITLE,
            episodeViewData.podcastName
        )

        bundle.putString(
            MediaMetadataCompat.METADATA_KEY_ARTIST,
            episodeViewData.title
        )
        bundle.putString(
            MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI,
            episodeViewData.imageUrl
        )

        episodeViewData.duration?.let { removeColon(it) }?.let {
            bundle.putLong(
                MediaMetadata.METADATA_KEY_DURATION,
                it
            )
        }

        controller.transportControls.playFromUri(
            Uri.parse(episodeViewData.episodeUrl),
            bundle
        )
        Log.d(TAG, "startPlaying: This is called ${episodeViewData.episodeUrl} HERE WE GOOOO!!")
    }






    companion object {
        private const val TAG = "PodcastDetailAndEpisode"
    }


}