package com.vadhara7.podplay.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.vadhara7.podplay.R
import com.vadhara7.podplay.repository.ItunesRepo
import com.vadhara7.podplay.service.ItunesService
import com.vadhara7.podplay.service.PodcastResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val TAG = javaClass.simpleName
        val itunesService = ItunesService.instance
        val itunesRepo = ItunesRepo(itunesService)
        itunesRepo.searchByTerm("Android Developer", {
            Log.i(TAG, "Results = $it")
        })
    }
}
