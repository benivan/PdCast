package com.example.pdcast.ui.view

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.VerifiedInputEvent
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import com.example.pdcast.databinding.FragmentHomeScreenBinding
import com.example.pdcast.ui.MainViewModel
import com.example.pdcast.util.DataMapper
import com.google.android.material.transition.MaterialFadeThrough
import kotlinx.coroutines.flow.onEach


class HomeScreenFragment : Fragment() {

    private var _binding: FragmentHomeScreenBinding? = null
    private val binding: FragmentHomeScreenBinding get() = _binding!!

    private val viewModel: MainViewModel by viewModels()


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

       viewModel.readAllData.observe(viewLifecycleOwner, Observer { listOfPodcastDatabaseModel ->
           binding.errorMessage.visibility =View.GONE
           binding.homeScreenProgressBar.visibility =View.GONE
           val listOfPodcasts = listOfPodcastDatabaseModel.map{
               dataMapper.mapEntityToModel(it)
           }.toList()

           binding.homeFragmentRecyclerView.adapter = HomePodcastAdapter(listOfPodcasts)

           val layoutManager = GridLayoutManager(requireContext(),2)

           binding.homeFragmentRecyclerView.layoutManager = layoutManager
       })
    }



    companion object {
        private const val TAG = "HomeScreen"
    }


}