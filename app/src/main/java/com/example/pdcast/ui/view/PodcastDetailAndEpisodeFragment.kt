package com.example.pdcast.ui.view

import android.content.Context
import android.graphics.BlendMode
import android.graphics.BlendModeColorFilter
import android.media.MediaMetadata
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.pdcast.data.response.RssFeedResponse
import com.example.pdcast.databinding.FragmentPodcastDetailAndEpisodeBinding
import com.example.pdcast.ui.MainViewModel
import com.example.pdcast.ui.PodcastViewModel
import com.example.pdcast.util.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch


class PodcastDetailAndEpisodeFragment : Fragment() {

    private val viewModel: PodcastViewModel by viewModels()

    private val mainViewModel: MainViewModel by activityViewModels()

    private var _binding: FragmentPodcastDetailAndEpisodeBinding? = null

    private val binding: FragmentPodcastDetailAndEpisodeBinding get() = _binding!!

    private val args by navArgs<PodcastDetailAndEpisodeFragmentArgs>()

    private lateinit var episodeAdapter: EpisodeItemAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.setRssFeedPodcastRepository(
            mainViewModel.getRssFeedPodcastRepository(),
            args.feedLink
        )
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        _binding = FragmentPodcastDetailAndEpisodeBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d(TAG, "onViewCreated: ")

        binding.progressBarEpisodePage.visibility = View.GONE
        binding.errorTextView.visibility = View.GONE
        binding.podcastSubscribeButton.visibility = View.GONE


        episodeAdapter = EpisodeItemAdapter(
            emptyList(),
            object : EpisodeListener {
                override fun onEpisodeClicked(episodeViewData: RssFeedResponse.EpisodeResponse) {
                    val controller =
                        MediaControllerCompat.getMediaController(requireActivity())

                    val activity = requireActivity()
                    if (controller.playbackState != null) {
                        if (controller.playbackState.state ==
                            PlaybackStateCompat.STATE_PLAYING
                        ) {
//                      controller.transportControls.pause()
                            startPlaying(episodeViewData, activity)
                        } else if (controller.playbackState.state == PlaybackStateCompat.STATE_PAUSED) {
                            startPlaying(episodeViewData, activity)
                        }
                    } else {
                        startPlaying(episodeViewData, activity)
                    }
                }
            },

            )
        binding.rvEpisode.adapter = episodeAdapter


        viewModel.paletteColor.flowWithLifecycle(lifecycle).onEach {
            val vibrantColor = getVibrantColorFromPalette(it)
            val mutedColor = getMutedColorFromPalette(it)
            val darkerColor = getDarkerColorFromPalette(it)
            binding.topSpace.background = darkerColor.toDrawable()
            binding.podcastUnSubscribeButton.background.colorFilter =
                BlendModeColorFilter(vibrantColor, BlendMode.COLOR)
            binding.podcastSubscribeButton.background.colorFilter =
                BlendModeColorFilter(vibrantColor, BlendMode.COLOR)
            episodeAdapter.setPalateColor(it)
            changeColorOfTheStatusBar(requireActivity(),darkerColor)
        }.launchIn(lifecycleScope)

        viewModel.podcast.flowWithLifecycle(lifecycle).onEach { it ->
            when (it) {
                is Resource.Failure -> {
                    Log.d(TAG, "onViewCreated: failure")
                    binding.errorTextView.visibility = View.VISIBLE
                    binding.progressBarEpisodePage.visibility = View.GONE
                    binding.errorTextView.text = it.toString()
                    binding.podcastSubscribeButton.visibility = View.GONE
                }
                is Resource.Loading -> {
                    Log.d(TAG, "onViewCreated: loading")
                    binding.progressBarEpisodePage.visibility = View.VISIBLE
                    binding.errorTextView.visibility = View.GONE
                    binding.podcastSubscribeButton.visibility = View.GONE
                }
                is Resource.Success -> {
                    binding.progressBarEpisodePage.visibility = View.GONE
                    binding.errorTextView.visibility = View.GONE
                    binding.tvDescription.visibility = View.GONE
                    binding.podcastSubscribeButton.visibility = View.VISIBLE
                    setSubscribeListener()
                    binding.podcastSubscribeButton.isVisible = it.data.isSubscribed == false
                    binding.podcastUnSubscribeButton.isVisible = it.data.isSubscribed == true
                    binding.tvDescription.text = it.data.description
                    binding.tvLanguage.text = it.data.language
                    binding.tvTitle.text = it.data.title
                    if (it.data.isSubscribed == true) {
                        binding.podcastSubscribeButton.text = "UnSubscribe"
                    } else binding.podcastSubscribeButton.text = "Subscribe"
                    Glide.with(binding.imageView).load(it.data.imageUrl).into(binding.imageView)
//                    it.data.episodes.forEach { episodeResponse -> Log.d("PodcastDetailAndEpisodeFragment", "episodeDurations: ${episodeResponse.duration},${episodeResponse.title}") }
                    episodeAdapter.submitList(it.data.episodes)

                    CoroutineScope(Dispatchers.IO).launch {
                        viewModel.paletteColor(getPaletteColor(it.data.imageUrl, requireContext()))
                    }


                }
            }
        }.catch { e ->
            e.printStackTrace()
        }.launchIn(lifecycleScope)




    }


    private fun setSubscribeListener() {
        binding.podcastSubscribeButton.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                mainViewModel.addPodcastToSubscribe(args.feedLink)

            }

        }
        binding.podcastUnSubscribeButton.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {

                    mainViewModel.removePodcastFromSubscribeTable(args.feedLink)

                }
            }
    }


    fun startPlaying(
        episodeViewData: RssFeedResponse.EpisodeResponse,
        fragmentActivity: FragmentActivity
    ) {
//        val fragmentActivity = activity as FragmentActivity
        val controller = MediaControllerCompat.getMediaController(fragmentActivity)
        val bundle = Bundle()
//        if (controller.metadata.description.mediaUri.toString() != episodeViewData.episodeUrl)  {
//            mainViewModel.playingDataIsChanged()
//        }

        val sharedPref = fragmentActivity.getSharedPreferences(
            "SharePreferencePdCast", Context.MODE_PRIVATE
        )
        val nowPlayingPodcastMediaUrl = sharedPref.getString("NowPlayingMediaLink","")

        if(nowPlayingPodcastMediaUrl != episodeViewData.episodeUrl){
            mainViewModel.playingDataIsChanged()
        }
        with(sharedPref.edit()) {
            putBoolean("IsPlayedBefore", true)
            putString("NowPlayingMediaLink", episodeViewData.episodeUrl)
            putString("NowPlayingMediaImage", episodeViewData.imageUrl)
            putString("NowPlayingEpisodeDuration", episodeViewData.duration)
            putLong("NowPlayingPosition", 0L)
            putString("NowPlyingPodcastName", episodeViewData.podcastName)
            putString("NowPlyingPodcastEpisodeName", episodeViewData.title)
            apply()
        }

        fun removeColon(duration: String): Long {
            return if (duration.contains(":")) {
                duration.replace(":", "").toLong()
            } else duration.toLong()
        }





        bundle.putString(
            MediaMetadataCompat.METADATA_KEY_TITLE,
            episodeViewData.title
        )

        bundle.putString(
            MediaMetadataCompat.METADATA_KEY_ARTIST,
            episodeViewData.podcastName
        )
        bundle.putString(
            MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI,
            episodeViewData.imageUrl
        )

        episodeViewData.duration?.let { if (it != "null")removeColon(it) else 0 }?.let {
            bundle.putLong(
                MediaMetadata.METADATA_KEY_DURATION,
                it * 1000L
            )
        }

        controller.transportControls.playFromUri(
            Uri.parse(episodeViewData.episodeUrl),
            bundle
        )

    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onResume() {
        super.onResume()
        viewModel.paletteColor.flowWithLifecycle(lifecycle).onEach {
            val vibrantColor = getVibrantColorFromPalette(it)
            binding.podcastSubscribeButton.background.colorFilter =
                BlendModeColorFilter(vibrantColor, BlendMode.MULTIPLY)
        }
    }


    companion object {
        private const val TAG = "PodcastDetailAndEpisode"
    }

}


