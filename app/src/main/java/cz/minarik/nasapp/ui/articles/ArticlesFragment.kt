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
import cz.minarik.base.data.NetworkState
import cz.minarik.base.ui.base.BaseFragment
import cz.minarik.nasapp.R
import cz.minarik.nasapp.data.model.ArticleFilterType
import cz.minarik.nasapp.ui.articles.source_selection.SourceSelectionViewModel
import cz.minarik.nasapp.utils.openCustomTabs
import kotlinx.android.synthetic.main.fragment_articles.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class ArticlesFragment : BaseFragment(R.layout.fragment_articles) {

    companion object {
        const val sourcesDialogTag = "sourcesDialogTag"
    }

    override val viewModel by viewModel<ArticlesFragmentViewModel>()

    private val sourcesViewModel: SourceSelectionViewModel by inject()

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
    }

    private fun setupFilters(view: View) {
        filterChipGroup.isVisible = viewModel.prefManager.showArticleFilters
        viewModel.prefManager.getArticleFilter().let {
            view.findViewById<Chip>(it.chipId)?.isChecked = true
        }

        //todo actually filter
        filterALl.setOnCheckedChangeListener { _, checked ->
            if (checked)
                viewModel.prefManager.setArticleFilter(ArticleFilterType.All)
        }
        filterStarred.setOnCheckedChangeListener { _, checked ->
            if (checked)
                viewModel.prefManager.setArticleFilter(ArticleFilterType.Starred)
        }
        filterUnread.setOnCheckedChangeListener { _, checked ->
            if (checked)
                viewModel.prefManager.setArticleFilter(ArticleFilterType.Unread)
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
        viewModel.articles.observe {
            if (!it.isNullOrEmpty()) {
                showProgressBar(false)
            }
            articlesAdapter.submitList(it)
        }

        sourcesViewModel.selectedSource.observe {
            viewModel.loadNews()
        }
        sourcesViewModel.selectedSourceName.observe {
            toolbar.subtitle = it
        }

        sourcesViewModel.sourceRepository.state.observe {
            if (it == NetworkState.SUCCESS) {
                sourcesViewModel.updateSources()
                viewModel.loadNews()
            }

            //no Items and downloading sources-> show progressBar
            showProgressBar(it == NetworkState.LOADING && !sourcesViewModel.hasData())
        }
    }

    private fun showProgressBar(show: Boolean) {
        progressBar.isVisible = show
        articlesRecyclerView.isVisible = !show
    }

    private fun initSwipeToRefresh() {
        viewModel.state.observe {
            swipeRefreshLayout.isRefreshing = it == NetworkState.LOADING
        }

        swipeRefreshLayout.setOnRefreshListener {
            viewModel.loadNews()
        }
    }

}
