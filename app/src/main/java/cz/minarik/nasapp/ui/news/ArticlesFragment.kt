package cz.minarik.nasapp.ui.news

import android.os.Bundle
import android.view.View
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.net.toUri
import cz.minarik.base.common.extensions.dividerMedium
import cz.minarik.base.common.extensions.initToolbar
import cz.minarik.base.data.NetworkState
import cz.minarik.base.ui.base.BaseFragment
import cz.minarik.nasapp.R
import cz.minarik.nasapp.ui.dialog.ArticlesSourceSelectionDialogFragment
import cz.minarik.nasapp.utils.openCustomTabs
import kotlinx.android.synthetic.main.fragment_articles.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class ArticlesFragment : BaseFragment(R.layout.fragment_articles) {

    companion object {
        const val sourcesDialogTag = "sourcesDialogTag"
    }

    override val viewModel by viewModel<ArticlesFragmentViewModel>()

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
                    showFilterDialog()
                    true
                }
                else -> false
            }
        }
    }

    private fun showFilterDialog() {
        activity?.run {
            supportFragmentManager.let { fragmentManager ->
                val transaction = fragmentManager.beginTransaction()
                transaction.addToBackStack(sourcesDialogTag)
                ArticlesSourceSelectionDialogFragment.newInstance(viewModel.getSources())
                    .show(transaction, sourcesDialogTag)
                ArticlesSourceSelectionDialogFragment.onSouurceSelected = {
                    viewModel.onSourceSelected(it)
                }
            }
        }
    }

    private fun initObserve() {
        viewModel.articles.observe {
            articlesAdapter.submitList(it)
        }

        viewModel.sourceRepository.state.observe {
            if (it == NetworkState.SUCCESS) {
                viewModel.updateSourcesAndReload()
            }
        }
    }

    private fun initSwipeToRefresh() {
        viewModel.state.observe {
            swipeRefreshLayout.isRefreshing = it == NetworkState.LOADING
        }

        swipeRefreshLayout.setOnRefreshListener {
            viewModel.forceReload()
        }
    }

}
