package com.example.pdcast.ui.view


import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.pdcast.data.response.RssFeedResponse
import com.example.pdcast.databinding.EpisodeItemsBinding
import com.example.pdcast.util.PaletteColor
import com.example.pdcast.util.getVibrantColorFromPalette
import com.example.pdcast.util.setBackgroundColorToImageButton
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlin.system.measureNanoTime

class EpisodeItemAdapter(
    private var episodes: List<RssFeedResponse.EpisodeResponse>,
    private val episodeListener: EpisodeListener,
) : RecyclerView.Adapter<EpisodeItemAdapter.EpisodeItemViewHolder>() {

    private var paletteColor:Int = 100000

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EpisodeItemViewHolder {
        val binding =
            EpisodeItemsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return EpisodeItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EpisodeItemViewHolder, position: Int) {
        val currentItem = episodes[position]
        if (currentItem.duration != "null"){
            holder.bind(currentItem)
        }

    }

    fun submitList(list: List<RssFeedResponse.EpisodeResponse>) {
        episodes = list
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return episodes.size
    }

    fun removeColon(duration: String): String {
        return if (duration.contains(":")) {
            duration.replace(":", "")
        } else duration
    }


    fun setPalateColor(color: PaletteColor) {
        paletteColor = getVibrantColorFromPalette(color)
        notifyDataSetChanged()
    }




    inner class EpisodeItemViewHolder(private val binding: EpisodeItemsBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(currentItem: RssFeedResponse.EpisodeResponse) {
            binding.tvTitle.text = currentItem.title
            binding.tvDescription.text =
                currentItem.description?.replace("\\<.*?\\>".toRegex(), "")
                    ?: currentItem.description
            binding.tvPubdate.text = currentItem.pubDate
            binding.duration.text =
                currentItem.duration?.let {
                    if (currentItem.duration != "null"){
                        convertSecondIntoDuration(it.toInt())
                    }else "0"
//                    convertSecondIntoDuration(it.toInt())
                }
            binding.play.setOnClickListener {
                Log.d(TAG, "bind-Link: ${currentItem.episodeUrl}")
                episodeListener.onEpisodeClicked(currentItem)
            }
            binding.play.setBackgroundColorToImageButton(paletteColor)


        }

    }

    companion object {
        private const val TAG = "EpisodeItemAdapter"
    }

    private fun convertSecondIntoDuration(duration: Int): String {
        var time = duration
        var convertedDuration: String = ""

        val hour = time / 3600

        time %= 3600
        val min = time / 60

        time %= 60
        val second = time


        convertedDuration = if (hour == 0) {
            "${addZero(min)}:${addZero(second)}"
        } else "${addZero(hour)}:${addZero(min)}:${addZero(second)}"

        if (convertedDuration == null ) convertedDuration = "0"

        return convertedDuration
    }

    private fun addZero(time: Int): String {
        return if (time.toString().length == 1) {
            "0$time"
        } else time.toString()

    }

}

interface EpisodeListener {
    fun onEpisodeClicked(episodeViewData: RssFeedResponse.EpisodeResponse)
}