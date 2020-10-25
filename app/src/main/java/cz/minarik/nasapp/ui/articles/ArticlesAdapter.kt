package cz.minarik.nasapp.ui.articles

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

class ArticlesAdapter(
    private var onItemClicked: (imageView: ImageView, position: Int) -> Unit,
    private var onItemExpanded: (position: Int)-> Unit,
) :
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
        private val articleItemView: ArticleListItemView = view.findViewById(R.id.articleListItem)

        fun bind(
            article: ArticleDTO?,
            onItemClicked: (imageView: ImageView, position: Int) -> Unit,
            onItemExpanded: (position: Int)-> Unit,
            position: Int,
        ) {
            if (article == null) return
            articleItemView.set(article)
            articleItemView.setOnClickListener {
                onItemClicked(articleItemView.articleImageView, adapterPosition)
            }
            articleItemView.onItemExpanded = {
                onItemExpanded.invoke(position)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as VideoViewHolder).bind(getItem(position), onItemClicked, onItemExpanded, position)
    }

    public fun getItemAtPosition(position: Int): ArticleDTO? {
        return getItem(position)
    }
}