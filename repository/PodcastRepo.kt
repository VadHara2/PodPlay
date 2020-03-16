package com.vadhara7.podplay.repository

import android.provider.Contacts
import com.vadhara7.podplay.model.Episode
import com.vadhara7.podplay.model.Podcast
import com.vadhara7.podplay.service.FeedService
import com.vadhara7.podplay.service.RssFeedResponse
import com.vadhara7.podplay.service.RssFeedService
import com.vadhara7.podplay.util.DateUtils
import kotlinx.coroutines.android.UI
import kotlinx.coroutines.launch

class PodcastRepo(private var feedService: FeedService) {

    fun getPodcast(feedUrl: String, callback: (Podcast?) -> Unit) {
        feedService.getFeed(feedUrl) { feedResponse ->
            var podcast: Podcast? = null
            if (feedResponse != null) {
                podcast = rssResponseToPodcast(feedUrl, "", feedResponse)
            }
            launch(UI) {
                callback(podcast)
            }
        }
    }

    private fun rssItemsToEpisodes(episodeResponses:
                                   List<RssFeedResponse.EpisodeResponse>): List<Episode> {
        return episodeResponses.map {
            Episode(
                it.guid ?: "",
                it.title ?: "",
                it.description ?: "",
                it.url ?: "",
                it.type ?: "",
                DateUtils.xmlDateToDate(it.pubDate),
                it.duration ?: ""
            )
        }
    }

    private fun rssResponseToPodcast(feedUrl: String, imageUrl:
    String, rssResponse: RssFeedResponse): Podcast? {
        val items = rssResponse.episodes ?: return null
        val description = if (rssResponse.description == "")
            rssResponse.summary else rssResponse.description
        return Podcast(feedUrl, rssResponse.title, description, imageUrl,
            rssResponse.lastUpdated, episodes = rssItemsToEpisodes(items))
    }
}