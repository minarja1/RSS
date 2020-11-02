package cz.minarik.nasapp.ui.articles.simple

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.navigation.fragment.navArgs
import coil.load
import cz.minarik.nasapp.R
import cz.minarik.nasapp.ui.articles.ArticlesFragmentViewModel
import cz.minarik.nasapp.ui.articles.GenericArticlesFragment
import kotlinx.android.synthetic.main.fragment_articles.*
import kotlinx.android.synthetic.main.include_toolbar_with_subtitle.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf


class SimpleArticlesFragment : GenericArticlesFragment(R.layout.fragment_simple_articles) {

    private val args: SimpleArticlesFragmentArgs by navArgs()

    val sourceUrl by lazy {
        args.sourceUrl
    }

    override val viewModel by viewModel<SimpleArticlesFragmentViewModel> {
        parametersOf(sourceUrl)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
        initObserve()
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

        viewModel.articles.observe {
            articlesAdapter.submitList(it)
            if (viewModel.shouldScrollToTop) {
                appBarLayout.setExpanded(true)
            }
        }

    }
}
