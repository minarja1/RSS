package cz.minarik.nasapp.ui.articles

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.activity.addCallback
import androidx.appcompat.widget.Toolbar
import androidx.core.view.isVisible
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import coil.load
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.chip.Chip
import cz.minarik.base.common.extensions.isScrolledToTop
import cz.minarik.base.common.extensions.showToast
import cz.minarik.nasapp.R
import cz.minarik.nasapp.data.datastore.DataStoreManager
import cz.minarik.nasapp.data.domain.ArticleDTO
import cz.minarik.nasapp.data.domain.ArticleFilterType
import cz.minarik.nasapp.databinding.FragmentArticlesBinding
import cz.minarik.nasapp.ui.MainActivity
import cz.minarik.nasapp.ui.custom.StateView
import cz.minarik.nasapp.ui.sources.selection.SourcesViewModel
import cz.minarik.nasapp.utils.toFreshLiveData
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel


class ArticlesFragment : GenericArticlesFragment<FragmentArticlesBinding>() {

    private val sourcesViewModel by sharedViewModel<SourcesViewModel>()

    override val viewModel by inject<ArticlesViewModel>()

    override val articlesRecyclerView: RecyclerView
        get() = binding.articlesRecyclerView
    override val swipeRefreshLayout: SwipeRefreshLayout
        get() = binding.swipeRefreshLayout
    override val appBarLayout: AppBarLayout
        get() = binding.appBarLayout
    override val filterUnread: Chip
        get() = binding.filterUnread
    override val filterStarred: Chip
        get() = binding.filterStarred
    override val filterAll: Chip
        get() = binding.filterAll
    override val stateView: StateView
        get() = binding.stateView
    override val shimmerLayout: LinearLayout
        get() = binding.shimmerLayout
    override val toolbar: Toolbar
        get() = binding.toolbarWithSubtitleContainer.toolbar
    override val progressBar: ProgressBar
        get() = (requireActivity() as MainActivity).progressBar

    override val shouldShowHorizontalProgressBar: Boolean
        get() = true

    override fun getArticlesLiveData(): MutableLiveData<List<ArticleDTO>> = viewModel.articles

    override fun getViewBinding() = FragmentArticlesBinding.inflate(layoutInflater)

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
                    binding.articlesRecyclerView.isScrolledToTop() -> {
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
        binding.run {
            stateView.attacheContentView(articlesRecyclerView)
            toolbarWithSubtitleContainer.toolbarPadding.isVisible = true
            newPostsCardView.setOnClickListener {
                viewModel.logNewPostsCardClicked()
                viewModel.reloadArticles(scrollToTop = true)
            }
        }
    }

    override fun initObserve() {
        super.initObserve()
        sourcesViewModel.selectedSource.collectWhenStarted {
            binding.toolbarWithSubtitleContainer.run {
                toolbarSubtitleContainer.isVisible = !it?.title.isNullOrEmpty()
                toolbarSubtitle.text = it?.title
                toolbarImageView.load(it?.imageUrl)
            }
        }

        sourcesViewModel.sourceRepository.sourcesChanged.toFreshLiveData().observe {
            if (it) viewModel.updateFromServer()
        }

        sourcesViewModel.sourceRepository.state.toFreshLiveData().observe {
            viewState.loadingSourcesState = it
        }
        viewModel.articlesRepository.newArticlesCount.observe {
            lifecycleScope.launch {
                binding.newPostsCardView.isVisible =
                    it > 0 && DataStoreManager.getArticleFilter()
                        .first() != ArticleFilterType.Starred
                binding.newPostsTV.text =
                    resources.getQuantityString(R.plurals.new_articles, it, it)
            }
        }
        viewModel.sourceDao.getAllUnblockedLiveData().toFreshLiveData().observe {
            viewModel.reloadArticles()
        }
    }

    private fun initSwipeToRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.reloadArticles(
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
