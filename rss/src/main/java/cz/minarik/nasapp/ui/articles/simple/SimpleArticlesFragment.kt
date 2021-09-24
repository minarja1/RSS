package cz.minarik.nasapp.ui.articles.simple

import android.os.Bundle
import android.view.Menu
import android.view.View
import androidx.activity.addCallback
import androidx.core.os.bundleOf
import androidx.core.view.GravityCompat
import androidx.core.view.isVisible
import androidx.lifecycle.MutableLiveData
import coil.load
import cz.minarik.nasapp.R
import cz.minarik.nasapp.ui.MainActivity
import cz.minarik.nasapp.ui.articles.ArticlesViewModel
import cz.minarik.nasapp.ui.articles.GenericArticlesFragment
import cz.minarik.nasapp.ui.custom.ArticleDTO
import cz.minarik.nasapp.utils.Constants
import kotlinx.android.synthetic.main.fragment_articles.*
import kotlinx.android.synthetic.main.include_toolbar_with_subtitle.*
import org.koin.androidx.viewmodel.ext.android.sharedViewModel


class SimpleArticlesFragment : GenericArticlesFragment(R.layout.fragment_articles) {

    override fun getArticlesLiveData(): MutableLiveData<List<ArticleDTO>> = viewModel.articlesSimple

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(this) {
            if ((requireActivity() as MainActivity).getCurrentFragment() != this@SimpleArticlesFragment) {
                (requireActivity() as MainActivity).goBack()
            } else {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START)
                } else if ((requireActivity() as MainActivity).sourcesFragmentShown) {
                    (requireActivity() as MainActivity).showHideSourceSelection(false)
                } else if (searchView?.isSearchOpen == true) {
                    searchView?.closeSearch()
                } else {
                    (requireActivity() as MainActivity).goBack()
                }
            }
        }
    }

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

    override val viewModel by sharedViewModel<ArticlesViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (savedInstanceState == null) {
            viewModel.isInSimpleMode = true
            viewModel.loadSelectedSource(sourceUrl ?: "")
        }
        initViews(view)
        initObserve()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.isInSimpleMode = false
    }

    override fun initViews(view: View?) {
        super.initViews(view)
        swipeRefreshLayout.setOnRefreshListener {
            viewModel.loadArticles()
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        menu.findItem(R.id.settingsAction)?.isVisible = false
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
