package cz.minarik.nasapp.ui.articles

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.activity.addCallback
import androidx.core.view.isVisible
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import coil.load
import cz.minarik.base.common.extensions.isScrolledToTop
import cz.minarik.base.common.extensions.showToast
import cz.minarik.nasapp.R
import cz.minarik.nasapp.data.datastore.DataStoreManager
import cz.minarik.nasapp.data.domain.ArticleFilterType
import cz.minarik.nasapp.ui.MainActivity
import cz.minarik.nasapp.data.domain.ArticleDTO
import cz.minarik.nasapp.ui.sources.selection.SourcesViewModel
import cz.minarik.nasapp.utils.toFreshLiveData
import kotlinx.android.synthetic.main.fragment_articles.*
import kotlinx.android.synthetic.main.include_toolbar_with_subtitle.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel


class ArticlesFragment : GenericArticlesFragment(R.layout.fragment_articles) {

    private val sourcesViewModel by sharedViewModel<SourcesViewModel>()

    override val viewModel by inject<ArticlesViewModel>()

    override fun getArticlesLiveData(): MutableLiveData<List<ArticleDTO>> = viewModel.articles

    private var doubleBackToExitPressedOnce = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initBackPressHandling()
    }

    private fun initBackPressHandling() {
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            if ((requireActivity() as MainActivity).getCurrentFragment() != this@ArticlesFragment) {
                (requireActivity() as MainActivity).goBack()
            } else {
                when {
                    (requireActivity() as MainActivity).sourcesFragmentShown -> {
                        (requireActivity() as MainActivity).showHideSourceSelection(false)
                    }
                    searchView?.isSearchOpen == true -> {
                        searchView?.closeSearch()
                    }
                    articlesRecyclerView?.isScrolledToTop() == true -> {
                        when {
                            doubleBackToExitPressedOnce -> {
                                requireActivity().finish()
                            }
                            else -> {
                                doubleBackToExitPressedOnce = true;
                                showToast(
                                    requireContext(),
                                    getString(R.string.press_back_again_to_leave)
                                )

                                Handler(Looper.getMainLooper()).postDelayed({
                                    doubleBackToExitPressedOnce = false
                                }, 2000)

                            }
                        }
                    }
                    else -> {
                        scrollToTop()
                    }
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
        initObserve()
        initSwipeToRefresh()
    }

    override fun initViews(view: View?) {
        super.initViews(view)
        articlesRecyclerView?.let {
            stateView.attacheContentView(it)
        }
        toolbarPadding.isVisible = true
        newPostsCardView.setOnClickListener {
            viewModel.loadArticles(scrollToTop = true)
        }
    }

    override fun initObserve() {
        super.initObserve()
        sourcesViewModel.selectedSourceChanged.toFreshLiveData().observe {
            viewModel.loadArticles(scrollToTop = true)
            (requireActivity() as MainActivity).showHideSourceSelection(false)
        }
        sourcesViewModel.selectedSourceName.observe {
            toolbarSubtitleContainer.isVisible = !it.isNullOrEmpty()
            toolbarSubtitle.text = it
        }
        sourcesViewModel.selectedSourceImage.observe {
            toolbarImageView.load(it)
        }

        sourcesViewModel.sourceRepository.sourcesChanged.toFreshLiveData().observe {
            if (it) viewModel.updateFromServer()
        }

        sourcesViewModel.sourceRepository.state.toFreshLiveData().observe {
            viewState.loadingSourcesState = it
        }
        viewModel.articlesRepository.newArticlesCount.observe {
            lifecycleScope.launch {
                newPostsCardView.isVisible =
                    it > 0 && DataStoreManager.getArticleFilter()
                        .first() != ArticleFilterType.Starred
                newPostsTV.text = resources.getQuantityString(R.plurals.new_articles, it, it)
            }
        }
    }

    private fun initSwipeToRefresh() {
        swipeRefreshLayout.setOnRefreshListener {
            viewModel.loadArticles(
                scrollToTop = false,
                isFromSwipeRefresh = true
            )
            viewModel.updateFromServer()
        }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) updateVisibleItems()
    }
}