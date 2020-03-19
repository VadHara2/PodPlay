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

        launch(CommonPool) {
            val podcast = podcastDao.loadPodcast(feedUrl)
            if (podcast != null) {
                fromStorage(podcast,callback)
            }else{
                fromInternet(feedUrl,callback)
            }

    }
        }

    fun fromStorage(podcast: Podcast, callback: (Podcast?) -> Unit) {
        podcast.id?.let {
            podcast.episodes = podcastDao.loadEpisodes(it)
            launch(UI){
                callback(podcast)
            }
        }
    }

    fun fromInternet(feedUrl: String, callback: (Podcast?) -> Unit){

        feedService.getFeed(feedUrl) { feedResponse ->
            var podcast: Podcast?
            if (feedResponse != null) {

                podcast = rssResponseToPodcast(feedUrl, "", feedResponse)
                launch(UI){
                    callback(podcast)
                }
            }

        }
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

    private fun rssResponseToPodcast(feedUrl: String, imageUrl: String, rssResponse: RssFeedResponse): Podcast? {
        val items = rssResponse.episodes ?: return null
        val description = if (rssResponse.description == "") rssResponse.summary else rssResponse.description
        return Podcast(null, feedUrl, rssResponse.title, description, imageUrl, rssResponse.lastUpdated, episodes = rssItemsToEpisodes(items))
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

    private fun getNewEpisodes(localPodcast: Podcast, callBack: (List<Episode>) -> Unit) {
        feedService.getFeed(localPodcast.feedUrl) { response ->
            if (response != null) {
                val remotePodcast = rssResponseToPodcast(localPodcast.feedUrl, localPodcast.imageUrl, response)
                remotePodcast?.let {
                    val localEpisodes = podcastDao.loadEpisodes(localPodcast.id!!)
                    val newEpisodes = remotePodcast.episodes.filter { episode ->
                        localEpisodes.find { episode.guid == it.guid } == null
                    }
                    callBack(newEpisodes)
                }
            } else {
                callBack(listOf())
            }
        }
    }

    private fun saveNewEpisodes(podcastId: Long, episodes: List<Episode>) {
        launch(CommonPool) {
            for (episode in episodes) {
                episode.podcastId = podcastId
                podcastDao.insertEpisode(episode)
            }
        }
    }

    class PodcastUpdateInfo (val feedUrl: String, val name: String, val newCount: Int)

    fun updatePodcastEpisodes(callback: (List<PodcastUpdateInfo>) -> Unit) {
        val updatedPodcasts: MutableList<PodcastUpdateInfo> = mutableListOf()
        val podcasts = podcastDao.loadPodcastsStatic()
        var processCount = podcasts.count()
        for (podcast in podcasts) {
            getNewEpisodes(podcast) { newEpisodes ->
                if (newEpisodes.count() > 0) {
                    saveNewEpisodes(podcast.id!!, newEpisodes)
                    updatedPodcasts.add(PodcastUpdateInfo(podcast.feedUrl, podcast.feedTitle, newEpisodes.count()))
                }
                processCount--
                if (processCount == 0) {
                    callback(updatedPodcasts)
                }
            }
        }
    }



}


