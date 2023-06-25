package com.bilcodes.repovault.ui.adapters

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bilcodes.repovault.R
import com.bilcodes.repovault.data.local.database.entities.Repository
import com.bilcodes.repovault.data.local.database.dao.RepositoryDao
import com.bilcodes.repovault.ui.viewmodels.MainViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class RepoListViewAdapter(
    private var repositories: List<Repository>,
    private val repositoryDao: RepositoryDao,
    private val context: Context,
    private val mainViewModel: MainViewModel
) : RecyclerView.Adapter<RepoListViewAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val repositoryCard: LinearLayout = itemView.findViewById(R.id.repoCard)
        val repoFullNameTextView: TextView = itemView.findViewById(R.id.repoFullName)
        val bookmarkIconImageView: ImageView = itemView.findViewById(R.id.bookmarkIcon)
        val shareIconImageView: ImageView = itemView.findViewById(R.id.shareIcon)
        val repoDescriptionTextView: TextView = itemView.findViewById(R.id.repoDescription)
        val repoStarsTextView: TextView = itemView.findViewById(R.id.repoStars)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.repo_card, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val repository = repositories[position]
        holder.repoFullNameTextView.text = repository.name
        holder.repoDescriptionTextView.text = repository.description
        holder.repoStarsTextView.text = repository.stars.toString()

        holder.repositoryCard.setOnClickListener {
            val url = "https://github.com/${repository.name}"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(intent)
        }

        holder.shareIconImageView.setOnClickListener {
            val url = "https://github.com/${repository.name}"

            // Create a share intent
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_TEXT, url)

            // Start the activity to show the share chooser dialog
            val chooserIntent = Intent.createChooser(shareIntent, "Share Github Repo")
            context.startActivity(chooserIntent)
        }

        holder.bookmarkIconImageView.setOnClickListener {
            // Call a method to delete the item from the Room database
            deleteRepository(repository, position)
        }

    }

    override fun getItemCount(): Int {
        return repositories.size
    }

    fun setData(repositories: List<Repository>) {
        this.repositories = repositories
        notifyDataSetChanged()


    }

    private fun deleteRepository(repository: Repository, position: Int) {
        // Delete the repository from the Room database using a coroutine
        CoroutineScope(Dispatchers.IO).launch {
            repositoryDao.deleteRepositoryById(repository.id)

            // Update the RecyclerView on the main thread
            withContext(Dispatchers.Main) {
                notifyItemRemoved(position)
                mainViewModel.updateRecyclerView() // Call updateRecyclerView() on the MainViewModel instance
            }
        }
    }


}