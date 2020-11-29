package cz.minarik.nasapp.ui.articles

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import cz.minarik.nasapp.R
import cz.minarik.nasapp.ui.custom.ArticleDTO
import cz.minarik.nasapp.ui.custom.ArticleListItemView
import kotlinx.android.synthetic.main.article_list_item.view.*

class ArticlesAdapter(
    private var onItemClicked: (imageView: ImageView, titleTextView: TextView, position: Int) -> Unit,
    private var onItemExpanded: (position: Int) -> Unit,
    private var preloadUrl: (url: String) -> Unit,
    private var filterBySource: (url: String?) -> Unit,
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
            onItemClicked: (imageView: ImageView, titleTextView: TextView, position: Int) -> Unit,
            onItemExpanded: (position: Int) -> Unit,
            filterBySource: (url: String?) -> Unit,
            position: Int,
        ) {
            if (article == null) return
            articleItemView.set(article)
            articleItemView.setOnClickListener {
                onItemClicked(if(article.expanded) articleItemView.articleFullImageView else articleItemView.articleImageView, articleItemView.titleTextView, adapterPosition)
            }
            articleItemView.onItemExpanded = {
                onItemExpanded.invoke(position)
            }
            articleItemView.filterBySource = {
                filterBySource.invoke(it)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        (holder as VideoViewHolder).bind(item, onItemClicked, onItemExpanded, filterBySource, position)
        item.link?.let {
            preloadUrl.invoke(it)
        }
    }

    fun getItemAtPosition(position: Int): ArticleDTO? {
        return getItem(position)
    }
}