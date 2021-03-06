package cz.minarik.nasapp.ui.sources.manage.lists

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import cz.minarik.base.common.extensions.dividerFullWidth
import cz.minarik.base.ui.base.BaseFragment
import cz.minarik.nasapp.R
import cz.minarik.nasapp.ui.sources.selection.SourcesViewModel
import kotlinx.android.synthetic.main.fragment_recycler.*
import org.koin.android.ext.android.inject

class ManageSourceListsFragment : BaseFragment(R.layout.fragment_recycler) {

    val viewModel: SourcesViewModel by inject()

    private lateinit var manageSourcesAdapter: ManageListsAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
        initObserve()
    }

    private fun initObserve() {
        viewModel.sourceManagementListsData.observe {
            manageSourcesAdapter.submitList(it)
        }
    }

    private fun initViews(view: View) {
        manageSourcesAdapter = ManageListsAdapter(
            onItemClicked = { source ->
                //todo
            }
        )

        recyclerView.run {
            adapter = manageSourcesAdapter
            (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
            layoutManager =
                LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
            dividerFullWidth()
        }
    }
}