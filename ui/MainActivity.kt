package com.vadhara7.podplay.ui

import kotlinx.android.synthetic.main.activity_main.*
import android.app.SearchManager
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.vadhara7.podplay.R
import com.vadhara7.podplay.adapter.PodcastListAdapter
import com.vadhara7.podplay.repository.ItunesRepo
import com.vadhara7.podplay.service.ItunesService
import com.vadhara7.podplay.service.PodcastResponse
import com.vadhara7.podplay.viewmodel.SearchViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity(), PodcastListAdapter.PodcastListAdapterListener {
    val TAG = javaClass.simpleName
    private lateinit var searchViewModel: SearchViewModel
    private lateinit var podcastListAdapter: PodcastListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupToolbar()
        setupViewModels()
        updateControls()
        handleIntent(intent)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_search, menu)
        val searchMenuItem = menu.findItem(R.id.search_item)
        val searchView = searchMenuItem?.actionView as SearchView
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName))
        return true
    }

    private fun performSearch(term: String) {
        showProgressBar()
        searchViewModel.searchPodcasts(term, { results ->
            hideProgressBar()
            toolbar.title = getString(R.string.search_results)
            podcastListAdapter.setSearchData(results)
        })
    }

    private fun handleIntent(intent: Intent) {
        if (Intent.ACTION_SEARCH == intent.action) {
            val query = intent.getStringExtra(SearchManager.QUERY)
            performSearch(query)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
    }

    private fun setupViewModels() {
        val service = ItunesService.instance
        searchViewModel = ViewModelProviders.of(this).get(
            SearchViewModel::class.java)
        searchViewModel.iTunesRepo = ItunesRepo(service)
    }

    private fun updateControls() {
        podcastRecyclerView.setHasFixedSize(true)
        val layoutManager = LinearLayoutManager(this)
        podcastRecyclerView.layoutManager = layoutManager
        val dividerItemDecoration = androidx.recyclerview.widget.DividerItemDecoration(podcastRecyclerView.context, layoutManager.orientation)//EDFUF
        podcastRecyclerView.addItemDecoration(dividerItemDecoration)
        podcastListAdapter = PodcastListAdapter(null, this, this)
        podcastRecyclerView.adapter = podcastListAdapter
    }

    override fun onShowDetails(
        podcastSummaryViewData: SearchViewModel.PodcastSummaryViewData
    ) {
// Not implemented yet
    }

    private fun showProgressBar() {
        progressBar.visibility = View.VISIBLE
    }
    private fun hideProgressBar() {
        progressBar.visibility = View.INVISIBLE
    }


}
