package cz.minarik.nasapp.ui.sources.selection

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cz.minarik.nasapp.data.datastore.DataStoreManager
import cz.minarik.nasapp.data.domain.RSSSource
import cz.minarik.nasapp.databinding.FragmentSourceSelectionBinding
import cz.minarik.nasapp.ui.MainActivity
import cz.minarik.nasapp.ui.articles.ArticlesViewModel
import cz.minarik.nasapp.ui.base.BaseFragment
import cz.minarik.nasapp.utils.ExitWithAnimation
import cz.minarik.nasapp.utils.startCircularReveal
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class SourceSelectionFragment : BaseFragment<FragmentSourceSelectionBinding>(), ExitWithAnimation {

    override fun getViewBinding(): FragmentSourceSelectionBinding =
        FragmentSourceSelectionBinding.inflate(layoutInflater)

    val viewModel by sharedViewModel<SourcesViewModel>()
    private val articlesViewModel by inject<ArticlesViewModel>()

    private lateinit var concatAdapter: ConcatAdapter
    private lateinit var sourcesAdapter: ArticleSourceAdapter

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
        viewModel.allSources.collectWhenStarted { sources ->
            sourcesAdapter.submitList(sources)
        }

        DataStoreManager.getShouldShowLongPressHint().collectWhenStarted {
            binding.longPressHint.isVisible = it
        }
    }

    private fun initViews() {
        binding.longPressDismissButton.setOnClickListener {
            lifecycleScope.launch {
                DataStoreManager.setShouldShowLongPressHint(false)
            }
        }

        binding.articleSourcesRecyclerView.layoutManager =
            LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)

        sourcesAdapter = ArticleSourceAdapter(
            onItemClicked = { rssSource ->
                if (!rssSource.selected) {
                    rssSource.URLs.firstOrNull()?.let { onSourceSelected(rssSource) }
                }
            },
            onItemBlocked = {
                val hidden = !it.isHidden
                viewModel.logSourceBlocked(it, hidden)
                viewModel.markAsBlocked(it, hidden)
            },
            onItemInfo = {
                viewModel.logSourceDetailOpened(it)
                (requireActivity() as MainActivity).navigateToSourceDetail(it.URLs[0])
            },
        )

        concatAdapter = ConcatAdapter()

        concatAdapter.addAdapter(sourcesAdapter)

        binding.articleSourcesRecyclerView.adapter = concatAdapter

        binding.backgroundView.setOnClickListener {
            (requireActivity() as MainActivity).showHideSourceSelection(false)
        }
    }

    private fun onSourceSelected(source: RSSSource) {
        if (!source.isList) {
            source.URLs.firstOrNull()?.let {
                viewModel.logNavigateToSimpleArticles(it)
                (requireActivity() as MainActivity).navigateToSimpleArticles(it)
            }
        }
    }

}