package com.example.pdcast.ui.view

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import com.example.pdcast.data.response.RssFeedResponse
import com.example.pdcast.databinding.EpisodeItemsBinding

class EpisodeItemAdapter(
    private val episodes: List<RssFeedResponse.EpisodeResponse>,
    private val episodeListener: EpisodeListener
) : RecyclerView.Adapter<EpisodeItemAdapter.EpisodeItemViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EpisodeItemViewHolder {
        val binding =
            EpisodeItemsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return EpisodeItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EpisodeItemViewHolder, position: Int) {
        val currentItem = episodes[position]
        holder.bind(currentItem)
    }

    override fun getItemCount(): Int {
        return episodes.size
    }


    inner class EpisodeItemViewHolder(private val binding: EpisodeItemsBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(currentItem: RssFeedResponse.EpisodeResponse) {
            binding.tvTitle.text = currentItem.title
            binding.tvDescription.text = currentItem.description
            binding.tvPubdate.text = currentItem.pubDate
            binding.duration.text = currentItem.duration
            binding.play.setOnClickListener {
                Log.d(TAG, "bind-Link: ${currentItem.episodeUrl}")
                currentItem.episodeUrl?.let { it1 -> episodeListener.onEpisodeClicked(it1) }
            }

            binding.root.setOnLongClickListener {
                Log.d(TAG, "bind: ${currentItem.episodeUrl}")
                episodeListener.onEpisodeLongClicked(currentItem.episodeUrl ?: "")
                true
            }
        }

    }

    companion object {
        private const val TAG = "EpisodeItemAdapter"
    }
}

interface EpisodeListener {
    fun onEpisodeClicked(url: String)
    fun onEpisodeLongClicked(url: String)
}