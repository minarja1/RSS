package cz.minarik.nasapp.ui.news

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import cz.minarik.nasapp.R
import cz.minarik.nasapp.ui.custom.ArticleDTO
import cz.minarik.nasapp.ui.custom.ArticleListItemView

class ArticlesAdapter(private var onItemClicked: (article: ArticleDTO, imageView: ImageView, position: Int) -> Unit) :
    ListAdapter<ArticleDTO, RecyclerView.ViewHolder>(diffCallback) {

    companion object {
        private val diffCallback = object : DiffUtil.ItemCallback<ArticleDTO>() {

            override fun areItemsTheSame(oldItem: ArticleDTO, newItem: ArticleDTO): Boolean {
                return oldItem.guid == newItem.guid
            }

            override fun areContentsTheSame(oldItem: ArticleDTO, newItem: ArticleDTO): Boolean {
                return oldItem == newItem
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return VideoViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.row_article_item, parent, false)
        )
    }

    class VideoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val articleItem: ArticleListItemView = view.findViewById(R.id.articleListItem)

        fun bind(
            video: ArticleDTO?,
            onItemClicked: (video: ArticleDTO, imageView: ImageView, position: Int) -> Unit
        ) {
            if (video == null) return
            articleItem.set(video)
            articleItem.setOnClickListener {
                onItemClicked(video, articleItem.articleImageView, adapterPosition)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as VideoViewHolder).bind(getItem(position), onItemClicked)
    }
}