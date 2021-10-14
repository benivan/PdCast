package com.example.pdcast.ui.view


import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.pdcast.data.model.ItunesPodcast
import com.example.pdcast.databinding.PodcastSearchViewItemBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch


class SearchItemAdapter(
    private val podcasts: List<ItunesPodcast>,
    private var listener: (Int) -> Unit,
    private var subscribeButtonListener:(ItunesPodcast) ->Unit
) : RecyclerView.Adapter<SearchItemAdapter.SearchItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchItemViewHolder {
        val itemBinding =
            PodcastSearchViewItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SearchItemViewHolder(itemBinding)

    }

    override fun onBindViewHolder(holder: SearchItemViewHolder, position: Int) {
        val currentItem = podcasts[position]
        listener(position)
        holder.bind(currentItem)
    }

    override fun getItemCount(): Int {
        return podcasts.size
    }



    inner class SearchItemViewHolder(
        private val itemBinding: PodcastSearchViewItemBinding,
    ) : RecyclerView.ViewHolder(itemBinding.root) {
        fun bind(currentItem: ItunesPodcast) {

            Glide.with(itemBinding.ivPicture).load(currentItem.artworkUrl100).into(itemBinding.ivPicture)

            itemBinding.tvHeading.text = currentItem.collectionCensoredName

            itemBinding.tvHeading.setOnClickListener {
                Log.d(TAG, "${currentItem.feedUrl}")
                    val action = SearchFragmentDirections.actionSearchFragmentToPodcastDetailAndEpisodeFragment(currentItem.feedUrl)
                    it.findNavController().navigate(action)
            }

            itemBinding.ivPicture.setOnClickListener {
                Log.d(TAG, "${currentItem.feedUrl}")
                val action = SearchFragmentDirections.actionSearchFragmentToPodcastDetailAndEpisodeFragment(currentItem.feedUrl)
                it.findNavController().navigate(action)
            }

            itemBinding.subscribeButton.setOnClickListener {
                subscribeButtonListener(currentItem)
            }

            CoroutineScope(Dispatchers.IO).launch {

            }
        }

    }



    companion object {
        private const val TAG = "SearchItemAdapter"
    }
}


