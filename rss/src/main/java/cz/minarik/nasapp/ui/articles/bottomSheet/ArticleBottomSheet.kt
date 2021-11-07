package cz.minarik.nasapp.ui.articles.bottomSheet

import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import coil.load
import cz.minarik.base.common.extensions.getFavIcon
import cz.minarik.nasapp.R
import cz.minarik.nasapp.ui.base.BaseBottomSheet
import cz.minarik.nasapp.data.domain.ArticleDTO
import cz.minarik.nasapp.utils.Constants
import kotlinx.android.synthetic.main.bottom_sheet_article.*
import java.net.MalformedURLException
import java.net.URL

class ArticleBottomSheet : BaseBottomSheet() {

    override val layoutId = R.layout.bottom_sheet_article

    var listener: ArticleBottomSheetListener? = null

    lateinit var article: ArticleDTO

    companion object {
        fun newInstance(
            articleDTO: ArticleDTO
        ): ArticleBottomSheet =
            ArticleBottomSheet().apply {
                arguments = bundleOf(
                    Constants.argArticleDTO to articleDTO,
                )
            }
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        article = arguments?.getSerializable(Constants.argArticleDTO) as ArticleDTO

        articleTitleTextView.text = article.title

        starImageView.setImageDrawable(
            ContextCompat.getDrawable(
                requireContext(),
                if (article.starred) R.drawable.ic_baseline_star_outline_24 else R.drawable.ic_baseline_star_24
            )
        )
        starTextView.text =
            getString(if (article.starred) R.string.remove_star else R.string.star_article)

        readImageView.setImageDrawable(
            ContextCompat.getDrawable(
                requireContext(),
                if (article.read) R.drawable.ic_baseline_undo_24 else R.drawable.ic_baseline_check_24
            )
        )
        readTextView.text =
            getString(if (article.read) R.string.mark_article_as_unread else R.string.mark_article_as_read)

        starButton.setOnClickListener {
            listener?.onStarred()
            dismiss()
        }
        readButton.setOnClickListener {
            listener?.onRead()
            dismiss()
        }
        shareButton.setOnClickListener {
            listener?.onShare()
            dismiss()
        }

        sourceButton.isVisible = article.showSource

        article.sourceUrl?.let { sourceUrl ->
            try {
                val url = URL(sourceUrl)
                sourceImageView.load(url.getFavIcon())
            } catch (e: MalformedURLException) {
            }

            article.sourceName?.let {
                val sourceText = "${getString(R.string.more_from)} $it"
                sourceTextView.text = sourceText
            }
            sourceButton.setOnClickListener {
                listener?.onSource(sourceUrl)
                dismiss()
            }
        }
    }


}