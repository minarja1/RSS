package cz.minarik.nasapp.ui.articles

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.net.toUri
import androidx.core.view.isVisible
import com.google.android.material.chip.Chip
import cz.minarik.base.common.extensions.dividerMedium
import cz.minarik.base.common.extensions.initToolbar
import cz.minarik.base.common.extensions.showToast
import cz.minarik.base.data.NetworkState
import cz.minarik.base.data.Status
import cz.minarik.base.ui.base.BaseFragment
import cz.minarik.nasapp.R
import cz.minarik.nasapp.data.model.ArticleFilterType
import cz.minarik.nasapp.ui.articles.source_selection.SourceSelectionViewModel
import cz.minarik.nasapp.ui.custom.ArticleDTO
import cz.minarik.nasapp.utils.openCustomTabs
import cz.minarik.nasapp.utils.scrollToTop
import kotlinx.android.synthetic.main.fragment_articles.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class ArticlesFragment : BaseFragment(R.layout.fragment_articles) {

    companion object {
        const val sourcesDialogTag = "sourcesDialogTag"
    }

    override val viewModel by viewModel<ArticlesFragmentViewModel>()

    private val sourcesViewModel: SourceSelectionViewModel by inject()

    private val viewState = ViewState()

    private val articlesAdapter by lazy {
        ArticlesAdapter { article, imageView, position ->
            article.link?.toUri()?.let {
                val builder = CustomTabsIntent.Builder()
                //todo ikonka
//                builder.setActionButton(icon, description, pendingIntent, tint);
                requireContext().openCustomTabs(it, builder)
                viewModel.markArticleAsRead(article)
                article.read = true
                articlesRecyclerView.adapter?.notifyItemChanged(position)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (savedInstanceState == null) {
            initObserve()
            initViews(view)
            initSwipeToRefresh()
        }
    }

    override fun showError(error: String?) {
        //todo
    }

    override fun showLoading(show: Boolean) {
        //todo
    }

    private fun initViews(view: View) {
        initToolbar()
        articlesRecyclerView.dividerMedium()
        articlesRecyclerView.adapter = articlesAdapter
        setupDrawerNavigation()
        setupFilters(view)
        stateView.attacheContentView(articlesRecyclerView)
    }

    private fun setupFilters(view: View) {
        filterChipGroup.isVisible = viewModel.prefManager.showArticleFilters
        viewModel.prefManager.getArticleFilter().let {
            view.findViewById<Chip>(it.chipId)?.isChecked = true
        }

        filterAll.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                viewModel.filterArticles(ArticleFilterType.All)
            }
        }
        filterStarred.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                viewModel.filterArticles(ArticleFilterType.Starred)
            }
        }
        filterUnread.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                viewModel.filterArticles(ArticleFilterType.Unread)
            }
        }
    }

    private fun setupDrawerNavigation() {
        drawerLayout?.let {
            val toggle = ActionBarDrawerToggle(
                activity, it, toolbar, 0, 0
            )
            it.addDrawerListener(toggle)
            toggle.syncState()
        }
    }

    private fun initToolbar() {
        initToolbar(toolbar)
        toolbar.inflateMenu(R.menu.menu_articles_fragment)
        toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.searchAction -> {
                    true
                }//todo
                R.id.filterAction -> {
                    showFilterViews()
                    true
                }
                else -> false
            }
        }
    }

    private fun showFilterViews() {
        filterChipGroup.isVisible = !filterChipGroup.isVisible
        viewModel.prefManager.showArticleFilters = filterChipGroup.isVisible
    }

    private fun initObserve() {
        viewModel.state.observe {
            //todo drzet si v NetworkState i exception a rozlisovat noInternet od jinych
            viewState.loadingArticlesState = it
        }

        viewModel.articles.observe {
            viewState.articles = it
        }

        sourcesViewModel.selectedSource.observe {
            viewModel.loadArticles(scrollToTop = true)
        }
        sourcesViewModel.selectedSourceName.observe {
            toolbar.subtitle = it
        }

        sourcesViewModel.sourceRepository.state.observe {
            if (it == NetworkState.SUCCESS) {
                sourcesViewModel.updateSources()
                viewModel.loadArticles()
            }
            viewState.loadingSourcesState = it
        }
    }

    private fun initSwipeToRefresh() {
        viewModel.state.observe {
            swipeRefreshLayout.isRefreshing = it == NetworkState.LOADING
        }

        swipeRefreshLayout.setOnRefreshListener {
            viewModel.loadArticles(true)
        }
    }

    private fun updateViews() {
        val loadingArticles = viewState.loadingArticlesState == NetworkState.LOADING
        val loadingSources = viewState.loadingSourcesState == NetworkState.LOADING
        val isError = viewState.loadingArticlesState?.status == Status.FAILED
        val articlesEmpty = viewState.articles.isEmpty()
        val loadingMessage = viewState.loadingArticlesState?.message

        articlesAdapter.submitList(viewState.articles) {
            if (viewModel.shouldScrollToTop) {
                articlesRecyclerView.scrollToTop()
            }
        }

        if (articlesEmpty && !isError && !loadingArticles) {
            stateView.empty(true)
        } else if (loadingSources && articlesEmpty && !isError) {
            stateView.loading(true, getString(R.string.updating_sources))
        } else if (isError) {
            if (articlesEmpty) {
                //full-screen error
                stateView.error(show = true, message = loadingMessage) {
                    viewModel.loadArticles()
                }
            } else {
                showToast(requireContext(), loadingMessage ?: getString(R.string.common_base_error))
            }
        } else {
            stateView.loading(false)
        }
    }

    inner class ViewState {
        var loadingArticlesState: NetworkState? = null
            set(value) {
                if (field != value) {
                    field = value
                    updateViews()
                }
            }
        var loadingSourcesState: NetworkState? = null
            set(value) {
                if (field != value) {
                    field = value
                    updateViews()
                }
            }
        var articles: List<ArticleDTO> = emptyList()
            set(value) {
                field = value
                updateViews()
            }
    }
}
