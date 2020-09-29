package cz.minarik.nasapp.ui.articles.source_selection

import android.os.Bundle
import android.view.View
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import cz.minarik.base.common.extensions.dividerMedium
import cz.minarik.base.ui.base.BaseFragment
import cz.minarik.nasapp.R
import cz.minarik.nasapp.ui.dialog.ArticleSourceAdapter
import kotlinx.android.synthetic.main.fragment_source_selection.*
import kotlinx.coroutines.delay
import org.koin.android.ext.android.inject
import timber.log.Timber

class SourceSelectionFragment : BaseFragment(R.layout.fragment_source_selection) {

    override val viewModel: SourceSelectionViewModel by inject()

    private lateinit var adapter: ArticleSourceAdapter

    override fun showError(error: String?) {
    }

    override fun showLoading(show: Boolean) {
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (savedInstanceState == null) {
            initViews()
            initObserve()
        }
    }

    private fun initObserve() {
        viewModel.sourcesData.observe {
            adapter.sources = it
            adapter.notifyDataSetChanged()
        }
    }

    private fun initViews() {
        adapter = ArticleSourceAdapter(mutableListOf()) {
            viewModel.onSourceSelected(it)

            try {
                (view?.parent?.parent as? DrawerLayout)?.closeDrawer(GravityCompat.START)
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
        articleSourcesRecyclerView.dividerMedium()
        articleSourcesRecyclerView.adapter = adapter
    }


}