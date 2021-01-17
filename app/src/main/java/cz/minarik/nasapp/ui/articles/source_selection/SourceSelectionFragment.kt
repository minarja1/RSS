package cz.minarik.nasapp.ui.articles.source_selection

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cz.minarik.base.ui.base.BaseFragment
import cz.minarik.nasapp.R
import cz.minarik.nasapp.data.domain.ArticleSourceButton
import kotlinx.android.synthetic.main.fragment_source_selection.*
import org.koin.android.ext.android.inject

class SourceSelectionFragment : BaseFragment(R.layout.fragment_source_selection) {

    override val viewModel: SourceSelectionViewModel by inject()

    private lateinit var concatAdapter: ConcatAdapter
    private lateinit var sourcesAdapter: ArticleSourceAdapter
    private lateinit var sourceListAdapter: ArticleSourceAdapter

    private var listsVisible = true
    private var sourcesVisible = true

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
        viewModel.sourceListsData.observe { sources ->
            sourceListAdapter.submitList(sources)
        }
        viewModel.sourcesData.observe { sources ->
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
                //todo
            })

        sourceListAdapter = ArticleSourceAdapter {
            viewModel.onSourceSelected(it)
        }

        sourcesAdapter = ArticleSourceAdapter {
            viewModel.onSourceSelected(it)
        }

        concatAdapter =
            ConcatAdapter(
                manageSourcesAdapter,
                TitleAdapter(listOf("Lists"), onItemClicked = {
                    listsVisible = if (listsVisible) {
                        concatAdapter.removeAdapter(sourceListAdapter)
                        false
                    } else {
                        concatAdapter.addAdapter(2, sourceListAdapter)
                        true
                    }
                }),
                sourceListAdapter,
                TitleAdapter(listOf("Sources"), onItemClicked = {
                    sourcesVisible = if (sourcesVisible) {
                        concatAdapter.removeAdapter(sourcesAdapter)
                        false
                    } else {
                        concatAdapter.addAdapter(concatAdapter.adapters.size, sourcesAdapter)
                        true
                    }
                }),
                sourcesAdapter,
            )

        articleSourcesRecyclerView.adapter = concatAdapter
    }

}