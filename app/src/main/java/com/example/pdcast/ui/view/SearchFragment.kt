package com.example.pdcast.ui.view

import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pdcast.databinding.FragmentSearchBinding
import com.example.pdcast.ui.MainViewModel
import com.example.pdcast.ui.PodcastsSearchViewModel
import com.example.pdcast.util.DataMapper
import com.example.pdcast.util.Resource
import com.example.pdcast.util.hideKeyboard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch


class SearchFragment : Fragment() {

    private val viewModel: PodcastsSearchViewModel by viewModels()


    private var _binding: FragmentSearchBinding? = null
    private val binding: FragmentSearchBinding get() = _binding!!
    private var itemPosition: Int? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        _binding = FragmentSearchBinding.inflate(layoutInflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.textView.visibility = View.GONE
        binding.progressBar.visibility = View.GONE
        binding.floatingActionButton.visibility = View.GONE

        binding.btnSearch.setOnClickListener {
            viewModel.getPodcastWithTerms(binding.searchTextInput.text.toString())
        }


        viewModel.podcasts
            .flowWithLifecycle(lifecycle)
            .onEach {
                when (it) {
                    is Resource.Failure -> {
                        binding.textView.visibility = View.VISIBLE
                        binding.textView.text = it.throwable.toString()
                        it.throwable.printStackTrace()
                    }
                    is Resource.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                        binding.textView.visibility = View.GONE
                    }
                    is Resource.Success -> {
                        val dataMapper = DataMapper()

//                        lifecycleScope.launch(Dispatchers.IO) {
//                          it.data.results.forEach {itunesData ->
//                                mainPodcastViewModel.addPodcast(dataMapper.mapModelToEntity(itunesData))
//                            }
//                        }

                        binding.textView.visibility = View.GONE
                        binding.progressBar.visibility = View.GONE
                        binding.recyclerView.adapter =
                            SearchItemAdapter(it.data.results,
                                listener = { position ->
                                    val itemCount = it.data.resultCount
                                    itemPosition = position
                                    if (position > itemCount / 3) {
                                        binding.floatingActionButton.visibility = View.VISIBLE
                                    } else {
                                        binding.floatingActionButton.visibility = View.GONE
                                    }
                                })
                        val layoutManager = LinearLayoutManager(requireContext())

                        binding.recyclerView.layoutManager = layoutManager

                        itemPosition?.let { position ->
                            with(layoutManager) {
                                scrollToPosition(position - 5)
                            }
                        }


                        binding.floatingActionButton.setOnClickListener {
                            binding.recyclerView.scrollToPosition(0)
                        }
                    }
                }
            }.launchIn(lifecycleScope)


        binding.searchTextInput.setOnEditorActionListener { _, actionId, _ ->
            if (EditorInfo.IME_ACTION_SEARCH == actionId) {
                viewModel.getPodcastWithTerms(binding.searchTextInput.text.toString())
                requireActivity().hideKeyboard()
                return@setOnEditorActionListener true
            }
            false
        }
    }


    override fun onPause() {
        super.onPause()
        itemPosition?.let { viewModel.setPosition(it) }
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        itemPosition = viewModel.getPosition()
    }


    companion object {
        private const val TAG = "SearchFragment"
    }


}