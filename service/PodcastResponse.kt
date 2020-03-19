package com.vadhara7.podplay.service

data class PodcastResponse(
    val resultCount: Int,
    val results: List<ItunesPodcast>) {
    data class ItunesPodcast(
        val artworkUrl100: String,
        val collectionCensoredName: String,
        val feedUrl: String,
        val artworkUrl30: String,
        val releaseDate: String
    )
}