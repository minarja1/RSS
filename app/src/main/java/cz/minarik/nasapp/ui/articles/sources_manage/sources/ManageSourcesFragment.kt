package cz.minarik.nasapp.ui.articles.sources_manage.sources

import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cz.minarik.base.ui.base.BaseFragment
import cz.minarik.nasapp.R
import kotlinx.android.synthetic.main.fragment_recycler.*

class ManageSourcesFragment : BaseFragment(R.layout.fragment_recycler) {

    override val viewModel by lazy {
        ViewModelProvider(requireActivity()).get(ManageSourcesViewModel::class.java)
    }

    private lateinit var adapter: ManageSourcesAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
        initObserve()
    }

    private fun initObserve() {
        viewModel.sourcesData.observe {
            adapter.submitList(it)
        }
    }

    private fun initViews(view: View) {
        adapter = ManageSourcesAdapter {

        }
        recyclerView.adapter = adapter
        recyclerView.layoutManager =
            LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
    }

    override fun showError(error: String?) {
    }

    override fun showLoading(show: Boolean) {
    }

}