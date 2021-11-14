package com.example.pdcast.ui.view

import android.content.res.Configuration
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pdcast.data.model.PodcastModel
import com.example.pdcast.data.response.RssFeedResponse
import com.example.pdcast.databinding.FragmentHomeScreenBinding
import com.example.pdcast.ui.HomeViewModel
import com.example.pdcast.ui.MainViewModel
import com.example.pdcast.util.DataMapper
import com.google.android.material.transition.MaterialFadeThrough
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach


class HomeScreenFragment : Fragment() {

    private var _binding: FragmentHomeScreenBinding? = null
    private val binding: FragmentHomeScreenBinding get() = _binding!!

    private val mainViewModel: MainViewModel by activityViewModels()
    private val viewModel: HomeViewModel by viewModels()


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



        mainViewModel.readAllPodcast.onEach { listOfPodcast ->
            binding.errorMessage.visibility = View.GONE
            binding.homeScreenProgressBar.visibility = View.GONE

            val podcastList = listOfPodcast.map {
                PodcastModel(
                    podcastId = it.podcastId,
                    podcastFeedUrl = it.podcastFeedUrl,
                    title = it.title,
                    description = it.description,
                    language = it.language,
                    link = it.link,
                    imageUrl = it.imageUrl,
                )
            }.toList()

            val adapter = HomePodcastAdapter(podcastList)

            val orientation = this.resources.configuration.orientation;
            if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                val layoutManager = GridLayoutManager(requireContext(), 2)
                binding.homeFragmentRecyclerView.layoutManager = layoutManager
                binding.homeFragmentRecyclerView.adapter = adapter
            } else {
                val layoutManager = GridLayoutManager(requireContext(), 5)
                binding.homeFragmentRecyclerView.layoutManager = layoutManager
                binding.homeFragmentRecyclerView.adapter = adapter
            }



            }.launchIn(lifecycleScope)
        }


        companion object {
            private const val TAG = "HomeScreen"
        }


    }