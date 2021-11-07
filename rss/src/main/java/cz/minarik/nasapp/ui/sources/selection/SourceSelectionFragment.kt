package cz.minarik.nasapp.ui.sources.selection

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cz.minarik.base.ui.base.BaseFragment
import cz.minarik.nasapp.R
import cz.minarik.nasapp.data.datastore.DataStoreManager
import cz.minarik.nasapp.data.domain.RSSSource
import cz.minarik.nasapp.ui.MainActivity
import cz.minarik.nasapp.ui.articles.ArticlesViewModel
import cz.minarik.nasapp.utils.ExitWithAnimation
import cz.minarik.nasapp.utils.startCircularReveal
import kotlinx.android.synthetic.main.fragment_source_selection.*
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class SourceSelectionFragment : BaseFragment(R.layout.fragment_source_selection),
    ExitWithAnimation {

    val viewModel by sharedViewModel<SourcesViewModel>()
    private val articlesViewModel by inject<ArticlesViewModel>()

    private lateinit var concatAdapter: ConcatAdapter
    private lateinit var sourcesAdapter: ArticleSourceAdapter
    private lateinit var sourceListAdapter: ArticleSourceAdapter

    override var referencedViewPosX: Int = 0
    override var referencedViewPosY: Int = 0
    override fun isToBeExitedWithAnimation() = true

    companion object {
        @JvmStatic
        fun newInstance(referencedViewPosition: IntArray? = null): SourceSelectionFragment =
            SourceSelectionFragment().apply {
                if (referencedViewPosition != null && referencedViewPosition.size == 2) {
                    referencedViewPosX = referencedViewPosition[0]
                    referencedViewPosY = referencedViewPosition[1]
                }
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.startCircularReveal(false)
        if (savedInstanceState == null) {
            initViews()
            initObserve()
        }
    }

    private fun initObserve() {
        viewModel.sourceSelectionsListsData.observe { sources ->
            sourceListAdapter.submitList(sources)
        }
        viewModel.sourcesSelectionData.observe { sources ->
            sourcesAdapter.submitList(sources)
        }

        DataStoreManager.getShouldShowLongPressHint().collectWhenStarted {
            longPressHint.isVisible = it
        }
    }

    private fun initViews() {
        longPressDismissButton.setOnClickListener {
            lifecycleScope.launch {
                DataStoreManager.setShouldShowLongPressHint(false)
            }
        }

        articleSourcesRecyclerView.layoutManager =
            LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)

        sourceListAdapter = ArticleSourceAdapter(
            onItemClicked = { rssSource ->
                if (!rssSource.selected) {
                    rssSource.URLs.firstOrNull()?.let { onSourceSelected(rssSource) }
                }
            },
            showPopupMenu = false
        )

        sourcesAdapter = ArticleSourceAdapter(
            onItemClicked = { rssSource ->
                if (!rssSource.selected) {
                    rssSource.URLs.firstOrNull()?.let { onSourceSelected(rssSource) }
                }
            },
            onItemBlocked = {
                viewModel.markAsBlocked(it, !it.isHidden) {
                    articlesViewModel.loadArticles()
                }
            },
            onItemInfo = {
                (requireActivity() as MainActivity).navigateToSourceDetail(it.URLs[0])
            },
        )

        concatAdapter = ConcatAdapter()


        concatAdapter.addAdapter(sourceListAdapter)
        concatAdapter.addAdapter(sourcesAdapter)

        articleSourcesRecyclerView.adapter = concatAdapter

        backgroundView.setOnClickListener {
            (requireActivity() as MainActivity).showHideSourceSelection(false)
        }
    }

    private fun onSourceSelected(source: RSSSource) {
        if (!source.isList) {
            source.URLs.firstOrNull()?.let {
                (requireActivity() as MainActivity).navigateToSimpleArticles(it)
            }
        }
    }

    private fun getTotalItemCount(): Int {
        return sourceListAdapter.itemCount + sourcesAdapter.itemCount
    }

}