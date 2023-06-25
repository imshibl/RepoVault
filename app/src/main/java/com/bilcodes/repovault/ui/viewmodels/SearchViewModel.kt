package com.bilcodes.repovault.ui.viewmodels

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat.getSystemService
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bilcodes.repovault.R
import com.bilcodes.repovault.data.local.database.AppDatabase
import com.bilcodes.repovault.data.local.database.dao.RepositoryDao
import com.bilcodes.repovault.data.remote.RetrofitClient
import com.bilcodes.repovault.data.remote.models.Repository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SearchViewModel(application: Application) : AndroidViewModel(application) {

    private val apiService = RetrofitClient.create()

    private var repositoryDao: RepositoryDao

    val repository: MutableLiveData<Repository?> = MutableLiveData()

    val isRepoAlreadySaved = MutableLiveData<Boolean>()

    val repoCard: MutableLiveData<Int> = MutableLiveData()
    val repositoryNotFoundTextView: MutableLiveData<Int> = MutableLiveData()
    val otherErrorsTextView: MutableLiveData<Int> = MutableLiveData()
    val progressBar: MutableLiveData<Int> = MutableLiveData()


    init {
        // Initialize the database and repositoryDao
        val context = getApplication<Application>().applicationContext
        val database = AppDatabase.getDatabase(context)
        repositoryDao = database.repositoryDao()

        isRepoAlreadySaved.value = false
        repoCard.value = View.GONE
        repositoryNotFoundTextView.value = View.GONE
        otherErrorsTextView.value = View.GONE
        progressBar.value = View.GONE
    }

    fun performSearch(ownerName: String, repoName: String) {

        CoroutineScope(Dispatchers.IO).launch {

            withContext(Dispatchers.Main) {
                repoCard.value = View.GONE
                repositoryNotFoundTextView.value = View.GONE
                otherErrorsTextView.value = View.GONE
                // Show the progress bar
                progressBar.value = View.VISIBLE
            }


            try {
                if (!isNetworkAvailable()) {
                    withContext(Dispatchers.Main) {
                        // No network connection, display an error message
                        Log.e("SearchActivity", "No network connection")
                        otherErrorsTextView.value = View.VISIBLE
                        progressBar.value = View.GONE

                    }
                    return@launch
                }

                val response = apiService.getRepository(ownerName, repoName)
                if (response.isSuccessful) {
                    val repository = response.body()

                    withContext(Dispatchers.Main) {
                        repository?.let {
                            this@SearchViewModel.repository.postValue(it)
                            isRepoAlreadySaved.value =
                                repositoryDao.isRepositoryExists(repository.id)
                            Log.d("repo_stat", isRepoAlreadySaved.value.toString())
                            // Repository found, update UI with the repository information
                            Log.d("SearchActivity", "Repository: $repository")
                            otherErrorsTextView.value = View.GONE
                            repoCard.value = View.VISIBLE
                            progressBar.value = View.GONE


                        }


                    }
                } else {
                    withContext(Dispatchers.Main) {
                        this@SearchViewModel.repository.postValue(null)
                        val errorMessage = when (response.code()) {
                            404 -> {
                                repositoryNotFoundTextView.value = View.VISIBLE
                                progressBar.value = View.GONE

                                "Repository not found"
                            }

                            else -> {

                                otherErrorsTextView.value = View.VISIBLE
                                progressBar.value = View.GONE

                                "API call failed: ${response.code()}"
                            }
                        }
                        withContext(Dispatchers.Main) {
                            Log.e("SearchActivity", errorMessage)

                        }
                    }

                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    // Handle exception
                    Log.e("SearchActivity", "Exception: $e")
                    otherErrorsTextView.value = View.VISIBLE
                    progressBar.value = View.GONE

                }
            }
        }

    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager =
            getApplication<Application>().applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val networkCapabilities = connectivityManager.activeNetwork ?: return false
            val activeNetwork =
                connectivityManager.getNetworkCapabilities(networkCapabilities) ?: return false
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                    activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
        } else {
            val networkInfo = connectivityManager.activeNetworkInfo
            networkInfo != null && networkInfo.isConnected
        }
    }

}