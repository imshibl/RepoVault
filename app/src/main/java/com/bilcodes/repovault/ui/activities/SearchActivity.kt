package com.bilcodes.repovault.ui.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.bilcodes.repovault.R
import com.bilcodes.repovault.data.local.database.AppDatabase
import com.bilcodes.repovault.data.local.database.dao.RepositoryDao
import com.bilcodes.repovault.data.local.database.entities.Repository
import com.bilcodes.repovault.ui.viewmodels.SearchViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SearchActivity : AppCompatActivity() {

    private lateinit var searchViewModel: SearchViewModel

    private lateinit var ownerTextField: EditText
    private lateinit var repoTexField: EditText

    private lateinit var repositoryNotFoundTextView: TextView
    private lateinit var otherErrorsTextView: TextView

    private lateinit var backIcon: ImageView

    private lateinit var repoCard: LinearLayout
    private lateinit var repoFullName: TextView
    private lateinit var repoDescription: TextView
    private lateinit var repoStars: TextView
    private lateinit var bookmarkIcon: ImageView
    private lateinit var shareIcon: ImageView

    private lateinit var progressBar: ProgressBar


    private lateinit var database: AppDatabase
    private lateinit var repositoryDao: RepositoryDao


    private var isRepoAlreadySaved: Boolean = false

    private var repositoryId: Long = 0
    private var repositoryName: String = ""
    private var repositoryDescription: String? = null
    private var repositoryStars: Int = 0
    private var repositoryLink: String = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        // Hide the app bar
        supportActionBar?.hide()

        searchViewModel = ViewModelProvider(this)[SearchViewModel::class.java]


        backIcon = findViewById(R.id.arrow_back)

        backIcon.setOnClickListener {
            finish()
        }

        progressBar = findViewById(R.id.progressBar)

        repositoryNotFoundTextView = findViewById(R.id.repositoryNotFoundTextView)
        otherErrorsTextView = findViewById(R.id.otherErrorsTextView)
        repoCard = findViewById(R.id.repoCard)
        repoFullName = findViewById(R.id.repoFullName)
        repoDescription = findViewById(R.id.repoDescription)
        repoStars = findViewById(R.id.repoStars)
        bookmarkIcon = findViewById(R.id.bookmarkIcon)
        shareIcon = findViewById(R.id.shareIcon)

        // Initialize the database and repositoryDao
        database = AppDatabase.getDatabase(this)
        repositoryDao = database.repositoryDao()

        repoCard.setOnClickListener {
            val url = repositoryLink
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        }





        bookmarkIcon.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {

                try {
                    if (isRepoAlreadySaved) {
                        isRepoAlreadySaved = false
                        repositoryDao.deleteRepositoryById(repositoryId)

                        bookmarkIcon.setImageResource(R.drawable.bookmark_icon)


                    } else {
                        isRepoAlreadySaved = true
                        val repoData = Repository(
                            id = repositoryId,
                            repoId = repositoryId,
                            name = repositoryName,
                            description = repositoryDescription ?: "",
                            stars = repositoryStars
                        )
                        repositoryDao.insert(repoData)
                        Log.d("added", "data added")
//                        val repositories = repositoryDao.getAllRepositories()
//                        for (repository in repositories) {
//                            Log.d("added", "Repository: ${repository.name}, Description: ${repository.description}, Stars: ${repository.stars}")
//                        }
                        bookmarkIcon.setImageResource(R.drawable.bookmarked_icon)

                    }


                } catch (e: Exception) {
                    Log.d("added", e.toString())
                }
            }

        }

        shareIcon.setOnClickListener {
            val url = repositoryLink

            // Create a share intent
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_TEXT, url)

            // Start the activity to show the share chooser dialog
            val chooserIntent = Intent.createChooser(shareIntent, "Share Github Repo")
            startActivity(chooserIntent)
        }




        ownerTextField = findViewById(R.id.search_field1)
        repoTexField = findViewById(R.id.search_field2)

        repoTexField.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val ownerName = ownerTextField.text.toString().trim()
                val repoName = repoTexField.text.toString().trim()

                if (ownerName.isNotEmpty() && repoName.isNotEmpty()) {

                    performSearch2(ownerName, repoName)
                }
                return@setOnEditorActionListener true
            }
            false
        }



        searchViewModel.progressBar.observe(this) { visibility ->
            progressBar.visibility = visibility
        }

        searchViewModel.isRepoAlreadySaved.observe(this) { value ->
            isRepoAlreadySaved = value

            if (isRepoAlreadySaved) {
                // Repository exists, update the bookmark icon to filled bookmark
                bookmarkIcon.setImageResource(R.drawable.bookmarked_icon)
            } else {
                // Repository does not exist, update the bookmark icon to outline bookmark
                bookmarkIcon.setImageResource(R.drawable.bookmark_icon)
            }
        }

        searchViewModel.repoCard.observe(this) { visibility -> repoCard.visibility = visibility }

        searchViewModel.repositoryNotFoundTextView.observe(this) { visibility ->
            repositoryNotFoundTextView.visibility = visibility
        }

        searchViewModel.otherErrorsTextView.observe(this) { visibility ->
            otherErrorsTextView.visibility = visibility
        }
    }

    private fun performSearch2(ownerName: String, repoName: String) {


        searchViewModel.performSearch(ownerName, repoName)
        searchViewModel.repository.observe(this) { repository ->
            if (repository != null) {

                repositoryId = repository.id.toLong()
                repositoryName = repository.full_name
                repositoryDescription = repository.description
                repositoryStars = repository.stargazers_count
                repositoryLink = "https://github.com/$repositoryName"

                repoDescription.text = repositoryDescription
                repoStars.text = repositoryStars.toString()
                repoFullName.text = repositoryName




            }


        }


    }


}