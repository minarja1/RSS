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
import cz.minarik.nasapp.utils.openCustomTabs
import kotlinx.android.synthetic.main.fragment_news.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class NewsFragment : BaseFragment(R.layout.fragment_news) {

    override val viewModel by viewModel<NewsFragmentViewModel>()

    private val articlesAdapter by lazy {
        ArticlesAdapter { article, imageView, position ->
            article.link?.toUri()?.let {
                val builder = CustomTabsIntent.Builder()
                //todo ikonka
//                builder.setActionButton(icon, description, pendingIntent, tint);
                requireContext().openCustomTabs(it, builder)
                viewModel.markArticleAsRead(article)
                article.read = true
                recyclerView.adapter?.notifyItemChanged(position)
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
        initToolbar(toolbar)
        recyclerView.dividerMedium()
        recyclerView.adapter = articlesAdapter
    }


    private fun initObserve() {
        viewModel.articles.observe {
            articlesAdapter.submitList(it)
        }
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
