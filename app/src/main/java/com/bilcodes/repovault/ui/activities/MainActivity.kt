package com.bilcodes.repovault.ui.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bilcodes.repovault.R
import com.bilcodes.repovault.ui.adapters.RepoListViewAdapter
import com.bilcodes.repovault.data.local.database.AppDatabase
import com.bilcodes.repovault.data.local.database.dao.RepositoryDao
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var repositoryAdapter: RepoListViewAdapter

    private lateinit var repositoryDao: RepositoryDao

    private lateinit var emptyBookmarksTextView : TextView

    private val mainScope = CoroutineScope(Dispatchers.Main)


    suspend fun updateRecyclerView() {
        // Fetch the updated repository data from the Room database
        val repositoryList = withContext(Dispatchers.IO) {
            repositoryDao.getAllRepositories()
        }

        // Update the adapter with the new data
        withContext(Dispatchers.Main) {
            repositoryAdapter.setData(repositoryList)
            if (repositoryList.isEmpty()) {
                emptyBookmarksTextView.visibility = View.VISIBLE
            } else {
                emptyBookmarksTextView.visibility = View.GONE
            }
        }
    }

    override fun onResume() {
        super.onResume()

        mainScope.launch {
            updateRecyclerView()
        }
    }


    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)



        // Hide the app bar
        supportActionBar?.hide()

        val fabAddRepo: MaterialButton = findViewById(R.id.fab_add_repo)
        fabAddRepo.setOnClickListener {
            val intent = Intent(this, SearchActivity::class.java)
            startActivity(intent)
        }

        emptyBookmarksTextView = findViewById(R.id.empty_bookmarks_textview)



        // Get the RepositoryDao instance from the AppDatabase
        val database = AppDatabase.getDatabase(this)
        repositoryDao = database.repositoryDao()

        // Initialize the RecyclerView and its adapter
        recyclerView = findViewById(R.id.recyclerView)
        repositoryAdapter = RepoListViewAdapter(emptyList(), repositoryDao, this)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = repositoryAdapter






    }


}