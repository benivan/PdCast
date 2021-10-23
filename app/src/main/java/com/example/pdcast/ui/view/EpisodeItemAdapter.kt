package com.example.pdcast.ui.view


import android.graphics.PorterDuff
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import com.example.pdcast.data.response.RssFeedResponse
import com.example.pdcast.databinding.EpisodeItemsBinding

class EpisodeItemAdapter(
    private var episodes: List<RssFeedResponse.EpisodeResponse>,
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

    fun submitList(list: List<RssFeedResponse.EpisodeResponse>) {
        episodes = list
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return episodes.size
    }

    fun removeColon(duration: String):String{
        return if(duration.contains(":")){
            duration.replace(":","")
        } else duration
    }


    inner class EpisodeItemViewHolder(private val binding: EpisodeItemsBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(currentItem: RssFeedResponse.EpisodeResponse) {
            binding.tvTitle.text = currentItem.title
            binding.tvDescription.text =
                currentItem.description?.replace("\\<.*?\\>".toRegex(), "") ?:currentItem.description
            binding.tvPubdate.text = currentItem.pubDate
            binding.duration.text = currentItem.duration
            binding.play.setOnClickListener {
                Log.d(TAG, "bind-Link: ${currentItem.episodeUrl}")
                episodeListener.onEpisodeClicked(currentItem)
            }
        }

    }

    companion object {
        private const val TAG = "EpisodeItemAdapter"
    }
}

interface EpisodeListener {
    fun onEpisodeClicked(episodeViewData: RssFeedResponse.EpisodeResponse)
}