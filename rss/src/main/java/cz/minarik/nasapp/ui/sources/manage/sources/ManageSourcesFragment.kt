package cz.minarik.nasapp.ui.sources.manage.sources

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import cz.minarik.base.common.extensions.dividerFullWidth
import cz.minarik.base.ui.base.BaseFragment
import cz.minarik.nasapp.R
import cz.minarik.nasapp.ui.MainActivity
import cz.minarik.nasapp.ui.sources.selection.SourcesViewModel
import kotlinx.android.synthetic.main.fragment_recycler.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class ManageSourcesFragment : BaseFragment(R.layout.fragment_recycler) {

    val viewModel by sharedViewModel<SourcesViewModel>()

    private lateinit var manageSourcesAdapter: ManageSourcesAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
        initObserve()
    }

    private fun initObserve() {
        viewModel.sourcesManagementData.observe {
            manageSourcesAdapter.submitList(it)
        }
    }

    private fun initViews(view: View) {
        manageSourcesAdapter = ManageSourcesAdapter(
            onShow = { source ->
                source.URLs.firstOrNull()?.let {
                    (requireActivity() as MainActivity).navigateToSimpleArticles(it)
                }
            },
            onAdd = {

            },
            onBlock = { source, position ->
                val blocked = !source.isBlocked
                source.isBlocked = blocked
                manageSourcesAdapter.notifyItemChanged(position)
                viewModel.markAsBlocked(source, blocked)
            },
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