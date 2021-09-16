package com.example.pdcast.ui.view

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.pdcast.data.model.ItunesPodcast
import com.example.pdcast.data.response.RssFeedResponse
import com.example.pdcast.databinding.HomeFragmentPodcastImageItemBinding

class HomePodcastAdapter(
    private val podcasts: List<ItunesPodcast>
): RecyclerView.Adapter<HomePodcastAdapter.PodcastItemViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PodcastItemViewHolder {
        val binding =
            HomeFragmentPodcastImageItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        Log.d(TAG, "onCreateViewHolder: ")
        return PodcastItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PodcastItemViewHolder, position: Int) {
        val currentItem = podcasts[position]
        holder.bind(currentItem)
    }

    override fun getItemCount(): Int {
        return podcasts.size
    }

    class PodcastItemViewHolder(private val binding: HomeFragmentPodcastImageItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(currentItem: ItunesPodcast) {
            Glide.with(itemView).load(currentItem.artworkUrl100).into(binding.imageView)

            binding.imageView.setOnClickListener {
                val action = HomeScreenFragmentDirections.actionHomeFragmentToPodcastDetailAndEpisodeFragment(currentItem.feedUrl)
                it.findNavController().navigate(action)
            }

        }

    }

    companion object{
        private const val TAG = "HomePodcastAdapter"
    }
}

