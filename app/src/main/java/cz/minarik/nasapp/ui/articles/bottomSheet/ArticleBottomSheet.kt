package cz.minarik.nasapp.ui.articles.bottomSheet

import android.os.Bundle
import androidx.core.os.bundleOf
import cz.minarik.nasapp.R
import cz.minarik.nasapp.ui.base.BaseBottomSheet
import cz.minarik.nasapp.ui.custom.ArticleDTO
import cz.minarik.nasapp.utils.Constants
import kotlinx.android.synthetic.main.bottom_sheet_article.*

class ArticleBottomSheet : BaseBottomSheet() {

    override val layoutId = R.layout.bottom_sheet_article

    var listener: ArticleBottomSheetListener? = null

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
    }


}