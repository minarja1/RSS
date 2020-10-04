package cz.minarik.nasapp.ui.articles

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.net.toUri
import androidx.core.view.isVisible
import cz.minarik.base.common.extensions.dividerMedium
import cz.minarik.base.common.extensions.initToolbar
import cz.minarik.base.data.NetworkState
import cz.minarik.base.ui.base.BaseFragment
import cz.minarik.nasapp.R
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
            initViews()
            initSwipeToRefresh()
        }
    }

    override fun showError(error: String?) {
        //todo
    }

    override fun showLoading(show: Boolean) {
        //todo
    }

    private fun initViews() {
        initToolbar()
        articlesRecyclerView.dividerMedium()
        articlesRecyclerView.adapter = articlesAdapter
        setupDrawerNavigation()
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
        //todo filter by ALL, UNREAD, STARRED
//        activity?.run {
//            supportFragmentManager.let { fragmentManager ->
//                val transaction = fragmentManager.beginTransaction()
//                transaction.addToBackStack(sourcesDialogTag)
//                ArticlesSourceSelectionDialogFragment.newInstance(viewModel.getSources())
//                    .show(transaction, sourcesDialogTag)
//                ArticlesSourceSelectionDialogFragment.onSouurceSelected = {
//                    viewModel.onSourceSelected(it)
//                }
//            }
//        }
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
