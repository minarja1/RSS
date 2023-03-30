package cz.minarik.nasapp.ui.articles

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import cz.minarik.base.ui.base.BaseListAdapter
import cz.minarik.nasapp.R
import cz.minarik.nasapp.data.domain.ArticleDTO
import cz.minarik.nasapp.ui.custom.ArticleListItemView

class ArticlesAdapter(
    private var onArticleClicked: (imageView: ImageView, titleTextView: TextView, position: Int) -> Unit,
    private var onItemLongClicked: (position: Int) -> Unit,
    private var onArticleExpanded: (position: Int) -> Unit,
    private var onContactInfoClicked: (position: Int) -> Unit,
    private var articleShown: (article: ArticleDTO) -> Unit,
    private var filterBySource: (url: String?) -> Unit,
) : BaseListAdapter<ArticleDTO>(
    R.layout.row_article_item,
    diffCallback,
) {
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

    fun getItemAtPosition(position: Int): ArticleDTO? {
        return try {
            getItem(position)
        } catch (e: Exception) {//sometimes will randomly throw AIOOB
            null
        }
    }

    override fun bind(
        itemView: View,
        item: ArticleDTO,
        position: Int,
        viewHolder: BaseViewHolderImp
    ) {
        val articleItemView = itemView.findViewById<ArticleListItemView>(R.id.articleListItem)
        articleItemView.set(item)
        articleItemView.setOnClickListener {
            onArticleClicked(
                articleItemView.articleFullImageView,
                if (articleItemView.expanded) {
                    articleItemView.findViewById(R.id.titleTextViewExpanded)
                } else {
                    articleItemView.findViewById(R.id.titleTextViewCollapsed)
                },
                viewHolder.bindingAdapterPosition
            )
        }
        articleItemView.setOnLongClickListener {
            onItemLongClicked(viewHolder.bindingAdapterPosition)
            true
        }
        articleItemView.onItemExpanded = {
            onArticleExpanded.invoke(position)
        }
        articleItemView.onContactInfoClicked = {
            onContactInfoClicked.invoke(position)
        }
        articleItemView.filterBySource = {
            filterBySource.invoke(it)
        }
    }


    override fun itemOnScreen(item: ArticleDTO, itemView: View) {
        super.itemOnScreen(item, itemView)
        articleShown.invoke(item)
    }
}