package cz.minarik.nasapp.ui.sources.selection

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cz.minarik.base.ui.base.BaseFragment
import cz.minarik.nasapp.R
import cz.minarik.nasapp.RSSApp
import cz.minarik.nasapp.data.domain.ArticleSourceButton
import cz.minarik.nasapp.ui.MainActivity
import cz.minarik.nasapp.ui.articles.ArticlesFragmentDirections
import cz.minarik.nasapp.utils.ExitWithAnimation
import cz.minarik.nasapp.utils.startCircularReveal
import kotlinx.android.synthetic.main.fragment_source_selection.*
import org.koin.android.ext.android.inject

class SourceSelectionFragment : BaseFragment(R.layout.fragment_source_selection),
    ExitWithAnimation {

    val viewModel: SourcesViewModel by inject()

    private lateinit var concatAdapter: ConcatAdapter
    private lateinit var sourcesAdapter: ArticleSourceAdapter
    private lateinit var sourceListAdapter: ArticleSourceAdapter

    private var listsVisible = true
    private var sourcesVisible = true

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
    }

    private fun initViews() {
        articleSourcesRecyclerView.layoutManager =
            LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)


        val manageSourcesAdapter = ArticleSourceButtonAdapter(
            listOf(
                ArticleSourceButton(
                    getString(R.string.manage_sources),
                    R.drawable.ic_baseline_tap_and_play_24
                )
            ), onItemClicked = {
                try {
                    val action = ArticlesFragmentDirections.actionArticlesToSourceManagement()
                    findNavController().navigate(action)
                } catch (e: Exception) {
                }
            })

        sourceListAdapter = ArticleSourceAdapter(
            onItemClicked = { if (!it.selected) viewModel.onSourceSelected(it) },
            showPopupMenu = false
        )

        sourcesAdapter = ArticleSourceAdapter(
            onItemClicked = { if (!it.selected) viewModel.onSourceSelected(it) },
            onItemBlocked = { viewModel.markAsBlocked(it, !it.isBlocked) },
            onItemInfo = {
                (requireActivity() as MainActivity).navigateToSourceDetail(it.URLs[0])
            },
        )

        val allowSourceManagement = RSSApp.sharedInstance.allowSourceManagement
        concatAdapter = ConcatAdapter()

        if (allowSourceManagement) {
            concatAdapter.addAdapter(manageSourcesAdapter)
            concatAdapter.addAdapter(
                TitleAdapter(listOf(getString(R.string.lists)), onItemClicked = {
                    listsVisible = if (listsVisible) {
                        concatAdapter.removeAdapter(sourceListAdapter)
                        false
                    } else {
                        concatAdapter.addAdapter(2, sourceListAdapter)
                        true
                    }
                }),
            )
        }
        concatAdapter.addAdapter(sourceListAdapter)
        if (allowSourceManagement) {
            concatAdapter.addAdapter(
                TitleAdapter(listOf(getString(R.string.sources)), onItemClicked = {
                    sourcesVisible = if (sourcesVisible) {
                        concatAdapter.removeAdapter(sourcesAdapter)
                        false
                    } else {
                        concatAdapter.addAdapter(concatAdapter.adapters.size, sourcesAdapter)
                        true
                    }
                })
            )
        }
        concatAdapter.addAdapter(sourcesAdapter)

        articleSourcesRecyclerView.adapter = concatAdapter

        backgroundView.setOnClickListener {
            (requireActivity() as MainActivity).showHideSourceSelection(false)
        }
    }

}