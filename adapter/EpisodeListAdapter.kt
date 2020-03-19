package com.vadhara7.podplay.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.vadhara7.podplay.R
import com.vadhara7.podplay.util.DateUtils
import com.vadhara7.podplay.util.HtmlUtils
import com.vadhara7.podplay.viewmodel.PodcastViewModel

class EpisodeListAdapter(
    private var episodeViewList: List<PodcastViewModel.EpisodeViewData>?,
    private val episodeListAdapterListener: EpisodeListAdapterListener) : RecyclerView.Adapter<EpisodeListAdapter.ViewHolder>() {
    class ViewHolder(v: View, private val episodeListAdapterListener: EpisodeListAdapterListener) : RecyclerView.ViewHolder(v) {
        var episodeViewData: PodcastViewModel.EpisodeViewData? = null
        val titleTextView: TextView = v.findViewById(R.id.titleView)
        val descTextView: TextView = v.findViewById(R.id.descView)
        val durationTextView: TextView = v.findViewById(R.id.durationView)
        val releaseDateTextView: TextView = v.findViewById(R.id.releaseDateView)

        init {
            v.setOnClickListener {
                episodeViewData?.let {
                    episodeListAdapterListener.onSelectedEpisode(it)
                }
            }
        }
    }
    fun setViewData(episodeList: List<PodcastViewModel.EpisodeViewData>) {
        episodeViewList = episodeList
        this.notifyDataSetChanged()
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EpisodeListAdapter.ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.episode_item, parent, false), episodeListAdapterListener)
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val episodeViewList = episodeViewList ?: return
        val episodeView = episodeViewList[position]
        holder.episodeViewData = episodeView
        holder.titleTextView.text = episodeView.title
        holder.descTextView.text = HtmlUtils.htmlToSpannable(episodeView.description ?: "")
        holder.durationTextView.text = episodeView.duration
        holder.releaseDateTextView.text = episodeView.releaseDate?.let {
            DateUtils.dateToShortDate(it)
        }
    }
    override fun getItemCount(): Int {
        return episodeViewList?.size ?: 0
    }

    interface EpisodeListAdapterListener {
        fun onSelectedEpisode(episodeViewData: PodcastViewModel.EpisodeViewData)
    }
}