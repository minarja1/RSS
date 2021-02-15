package cz.minarik.nasapp.ui.articles.simple

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import coil.load
import cz.minarik.nasapp.R
import cz.minarik.nasapp.ui.articles.GenericArticlesFragment
import cz.minarik.nasapp.utils.Constants
import kotlinx.android.synthetic.main.fragment_articles.*
import kotlinx.android.synthetic.main.include_toolbar_with_subtitle.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf


class SimpleArticlesFragment : GenericArticlesFragment(R.layout.fragment_articles) {

    companion object {
        fun newInstance(
            sourceUrl: String,
        ): SimpleArticlesFragment =
            SimpleArticlesFragment().apply {
                arguments = bundleOf(
                    Constants.argSourceUrl to sourceUrl,
                )
            }
    }

    override val backEnabled = true

    val sourceUrl by lazy {
        requireArguments().getString(Constants.argSourceUrl)
    }

    override val viewModel by viewModel<SimpleArticlesFragmentViewModel> {
        parametersOf(sourceUrl)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
        initObserve()
    }

    override fun initViews(view: View?) {
        super.initViews(view)
        swipeRefreshLayout.setOnRefreshListener {
            viewModel.loadArticles()
        }
    }

    override fun initObserve() {
        super.initObserve()

        viewModel.selectedSourceName.observe {
            toolbarSubtitleContainer.isVisible = !it.isNullOrEmpty()
            toolbarSubtitle.text = it
        }
        viewModel.selectedSourceImage.observe {
            toolbarImageView.load(it)
        }

    }
}
