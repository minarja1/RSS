package cz.minarik.nasapp.ui.custom

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.MenuItem
import android.widget.LinearLayout
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import coil.load
import cz.minarik.base.common.extensions.iconizeMenu
import cz.minarik.base.common.extensions.tint
import cz.minarik.nasapp.R
import cz.minarik.nasapp.data.datastore.DataStoreManager
import cz.minarik.nasapp.data.domain.RSSSource
import kotlinx.android.synthetic.main.source_selection_list_item.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class SourceSelectionItemView(context: Context, attrs: AttributeSet? = null) :
    LinearLayout(context, attrs) {

    var source: RSSSource? = null

    init {
        inflate(context, R.layout.source_selection_list_item, this)
    }

    fun set(source: RSSSource) {
        this.source = source

        sourceNameTextView.text = source.title

        when {
            !source.imageUrl.isNullOrEmpty() -> {
                sourceImageView.load(source.imageUrl) {
                    fallback(R.drawable.ic_baseline_article_24)
                }
            }
            source.isList -> {
                sourceImageView.load(R.drawable.ic_baseline_article_24)
            }
            else -> {
                val drawable = ContextCompat.getDrawable(context, R.drawable.comet_24px)
                drawable?.tint(context, R.color.textColorPrimary)
                sourceImageView.load(drawable)
            }
        }

        sourceImageView.alpha = if (source.isHidden) 0.5f else 1f

        sourceNameTextView.setTextColor(
            ContextCompat.getColor(
                context,
                if (source.isHidden) R.color.textColorSecondary else R.color.textColorPrimary
            )
        )

        hiddenImageView.isVisible = source.isHidden

        invalidate()
        requestLayout()
    }

    fun showPopUp(
        onItemBlocked: ((item: RSSSource) -> Unit)?,
        onItemInfo: ((item: RSSSource) -> Unit)?
    ) {
        CoroutineScope(Dispatchers.Default).launch {
            DataStoreManager.setShouldShowLongPressHint(false)
        }
        val popup = PopupMenu(context, this)
        popup.menuInflater.inflate(R.menu.menu_rss_source, popup.menu)

        popup.setOnMenuItemClickListener { menuItem: MenuItem ->
            when (menuItem.itemId) {
                R.id.unblockAction, R.id.blockAction -> {
                    source?.let {
                        onItemBlocked?.invoke(it)
                    }
                }
                R.id.infoAction -> {
                    source?.let {
                        onItemInfo?.invoke(it)
                    }
                }
            }
            true
        }

        popup.iconizeMenu(resources)

        popup.gravity = Gravity.END

        popup.menu.findItem(R.id.blockAction).isVisible = source?.isHidden == false
        popup.menu.findItem(R.id.unblockAction).isVisible = source?.isHidden == true

        popup.show()
    }

    override fun setOnClickListener(l: OnClickListener?) {
        sourceBackground.setOnClickListener(l)
    }

    override fun setOnLongClickListener(l: OnLongClickListener?) {
        sourceBackground.setOnLongClickListener(l)
    }
}