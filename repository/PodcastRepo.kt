package com.vadhara7.podplay.repository

import com.vadhara7.podplay.model.Podcast

class PodcastRepo {
    fun getPodcast(feedUrl: String, callback: (Podcast?) -> Unit) {
        callback(Podcast(feedUrl, "No Name", "No description", "No image"))
    }
}