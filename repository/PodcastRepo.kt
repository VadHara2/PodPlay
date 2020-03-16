package com.vadhara7.podplay.repository

import android.provider.Contacts
import androidx.lifecycle.LiveData
import com.vadhara7.podplay.db.PodcastDao
import com.vadhara7.podplay.model.Episode
import com.vadhara7.podplay.model.Podcast
import com.vadhara7.podplay.service.FeedService
import com.vadhara7.podplay.service.RssFeedResponse
import com.vadhara7.podplay.service.RssFeedService
import com.vadhara7.podplay.util.DateUtils
import kotlinx.coroutines.CommonPool
import kotlinx.coroutines.android.UI
import kotlinx.coroutines.launch

class PodcastRepo(private var feedService: FeedService, private var podcastDao: PodcastDao) {



    fun getPodcast(feedUrl: String, callback: (Podcast?) -> Unit) {
//      launch(CommonPool) {
//            val podcast = podcastDao.loadPodcast(feedUrl)
//            if (podcast != null) {
//                podcast.id?.let {
//                    podcast.episodes = podcastDao.loadEpisodes(it)
//                    launch(UI){
//                        callback(podcast)
//                    }
//                }
//            } else {
        feedService.getFeed(feedUrl) { feedResponse ->
            var podcast: Podcast? = null
            if (feedResponse != null) {
                podcast = rssResponseToPodcast(feedUrl, "", feedResponse)
            }
            launch(UI) {
                callback(podcast)
            }
        }
//            }
//       }
    }

    private fun rssItemsToEpisodes(episodeResponses:
                                   List<RssFeedResponse.EpisodeResponse>): List<Episode> {
        return episodeResponses.map {
            Episode(
                it.guid ?: "",
                null,
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
        return Podcast(null, feedUrl, rssResponse.title, description, imageUrl,
            rssResponse.lastUpdated, episodes = rssItemsToEpisodes(items))
    }

    fun save(podcast: Podcast) {
        launch(CommonPool) {
            val podcastId = podcastDao.insertPodcast(podcast)
            for (episode in podcast.episodes) {
                episode.podcastId = podcastId
                podcastDao.insertEpisode(episode)
            }
        }
    }

    fun getAll(): LiveData<List<Podcast>>
    {
        return podcastDao.loadPodcasts()
    }

    fun delete(podcast: Podcast) {
        launch(CommonPool) {
            podcastDao.deletePodcast(podcast)
        }
    }
}