package com.bilcodes.repovault.ui.viewmodels

import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bilcodes.repovault.data.local.database.AppDatabase
import com.bilcodes.repovault.data.local.database.dao.RepositoryDao
import com.bilcodes.repovault.ui.adapters.RepoListViewAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MainViewModel : ViewModel() {

    private lateinit var repositoryDao: RepositoryDao
    private lateinit var repositoryAdapter: RepoListViewAdapter

    val emptyBookmarksVisibility: MutableLiveData<Int> = MutableLiveData()

    fun init(database: AppDatabase, adapter: RepoListViewAdapter) {
        repositoryDao = database.repositoryDao()
        repositoryAdapter = adapter
        emptyBookmarksVisibility.value = View.GONE
    }

    suspend fun updateRecyclerView() {
        withContext(Dispatchers.IO) {
            val repositoryList = repositoryDao.getAllRepositories()
            withContext(Dispatchers.Main) {
                repositoryAdapter.setData(repositoryList)
                if (repositoryList.isEmpty()) {
                    emptyBookmarksVisibility.value = View.VISIBLE
                } else {
                    emptyBookmarksVisibility.value = View.GONE
                }
            }
        }
    }


}