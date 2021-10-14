package com.example.pdcast.ui.view

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.pdcast.data.model.PodcastModel
import com.example.pdcast.data.response.RssFeedResponse
import com.example.pdcast.databinding.FragmentHomeScreenBinding
import com.example.pdcast.ui.MainViewModel
import com.example.pdcast.util.DataMapper
import com.google.android.material.transition.MaterialFadeThrough


class HomeScreenFragment : Fragment() {

    private var _binding: FragmentHomeScreenBinding? = null
    private val binding: FragmentHomeScreenBinding get() = _binding!!

    private val mainViewModel: MainViewModel by activityViewModels()



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        _binding = FragmentHomeScreenBinding.inflate(layoutInflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        enterTransition = MaterialFadeThrough()
        exitTransition = MaterialFadeThrough()

        val dataMapper = DataMapper()

//       viewModel.readAllDataDB.observe(viewLifecycleOwner, Observer { listOfPodcastDatabaseModel ->
//           binding.errorMessage.visibility =View.GONE
//           binding.homeScreenProgressBar.visibility =View.GONE
//           val listOfPodcasts = listOfPodcastDatabaseModel.map{
//               dataMapper.mapEntityToModel(it)
//           }.toList()
//
//           binding.homeFragmentRecyclerView.adapter = HomePodcastAdapter(listOfPodcasts)
//
//           val layoutManager = GridLayoutManager(requireContext(),2)
//
//           binding.homeFragmentRecyclerView.layoutManager = layoutManager
//       })

        mainViewModel.readAllPodcast.observe(viewLifecycleOwner, Observer { listOfPodcast->
            binding.errorMessage.visibility = View.GONE
            binding.homeScreenProgressBar.visibility =View.GONE

            val podcastList = listOfPodcast.map { PodcastModel(
                podcastId = it.podcastId,
                title = it.title,
                description = it.description,
                language = it.language,
                link = it.link,
                imageUrl = it.imageUrl,
                )
            }.toList()

            binding.homeFragmentRecyclerView.adapter = HomePodcastAdapter(podcastList){
                it.podcastId?.let { mainViewModel.getPodcastWithId(it)}
                mainViewModel.podcastsWithEpisodes?.observe(viewLifecycleOwner, Observer { listPodcastWithEpisode->
                    val podcastsWithEpisodes =  listPodcastWithEpisode.elementAt(0)

                    val podcastDetailWithEpisode = RssFeedResponse(
                        title = podcastsWithEpisodes.podcast.title,
                        description = podcastsWithEpisodes.podcast.description,
                        language = podcastsWithEpisodes.podcast.language,
                        link = podcastsWithEpisodes.podcast.link,
                        imageUrl = podcastsWithEpisodes.podcast.imageUrl,
                        episodes = podcastsWithEpisodes.episodes.map { dbPodcastEpisode ->
                            RssFeedResponse.EpisodeResponse(
                                title = dbPodcastEpisode.title,
                                link = dbPodcastEpisode.link,
                                description = dbPodcastEpisode.description,
                                pubDate = dbPodcastEpisode.pubDate,
                                duration = dbPodcastEpisode.duration,
                                episodeUrl = dbPodcastEpisode.episodeUrl,
                                imageUrl = dbPodcastEpisode.imageUrl,
                                podcastName = dbPodcastEpisode.podcastName
                            )
                        }.toMutableList()
                    )
                    val action = HomeScreenFragmentDirections.actionHomeFragmentToPodcastDetailAndEpisodeFragment("")
                        binding.root.findNavController().navigate(action)

                    Log.d(TAG, "onViewCreated: $podcastDetailWithEpisode")

                })
            }

            val layoutManager = GridLayoutManager(requireContext(),2)

            binding.homeFragmentRecyclerView.layoutManager = layoutManager
        })
    }



    companion object {
        private const val TAG = "HomeScreen"
    }


}