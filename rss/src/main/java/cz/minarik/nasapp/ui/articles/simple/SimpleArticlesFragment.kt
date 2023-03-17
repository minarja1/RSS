package cz.minarik.nasapp.ui.articles.simple

import android.os.Bundle
import android.view.Menu
import android.view.View
import android.widget.LinearLayout
import androidx.activity.addCallback
import androidx.appcompat.widget.Toolbar
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import coil.load
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.chip.Chip
import cz.minarik.nasapp.R
import cz.minarik.nasapp.data.domain.ArticleDTO
import cz.minarik.nasapp.databinding.FragmentArticlesBinding
import cz.minarik.nasapp.ui.MainActivity
import cz.minarik.nasapp.ui.articles.ArticlesViewModel
import cz.minarik.nasapp.ui.articles.GenericArticlesFragment
import cz.minarik.nasapp.ui.custom.StateView
import cz.minarik.nasapp.utils.Constants
import org.koin.android.ext.android.inject


class SimpleArticlesFragment : GenericArticlesFragment<FragmentArticlesBinding>() {

    override fun getViewBinding() = FragmentArticlesBinding.inflate(layoutInflater)

    override val articlesRecyclerView: RecyclerView
        get() = binding.articlesRecyclerView
    override val swipeRefreshLayout: SwipeRefreshLayout
        get() = binding.swipeRefreshLayout
    override val appBarLayout: AppBarLayout
        get() = binding.appBarLayout
    override val filterUnread: Chip
        get() = binding.filterUnread
    override val filterStarred: Chip
        get() = binding.filterStarred
    override val filterAll: Chip
        get() = binding.filterAll
    override val stateView: StateView
        get() = binding.stateView
    override val shimmerLayout: LinearLayout
        get() = binding.shimmerLayout
    override val toolbar: Toolbar
        get() = binding.toolbarWithSubtitleContainer.toolbar


    override fun getArticlesLiveData(): MutableLiveData<List<ArticleDTO>> = viewModel.articlesSimple

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(this) {
            if ((requireActivity() as MainActivity).getCurrentFragment() != this@SimpleArticlesFragment) {
                (requireActivity() as MainActivity).goBack()
            } else {
                when {
                    (requireActivity() as MainActivity).sourcesFragmentShown -> {
                        (requireActivity() as MainActivity).showHideSourceSelection(false)
                    }
                    searchView?.isSearchOpen == true -> {
                        searchView?.closeSearch()
                    }
                    else -> {
                        (requireActivity() as MainActivity).goBack()
                    }
                }
            }
        }
    }

    companion object {
        fun newInstance(
            sourceUrl: String,
        ): SimpleArticlesFragment =
            SimpleArticlesFragment().apply {
                arguments = bundleOf(
                    Constants.argSourceUrl to sourceUrl,
                )
            }
    }

    override val backEnabled = true

    val sourceUrl by lazy {
        requireArguments().getString(Constants.argSourceUrl)
    }

    override val viewModel by inject<ArticlesViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (savedInstanceState == null) {
            viewModel.isInSimpleMode = true
            viewModel.loadSelectedSource(sourceUrl ?: "")
        }
        initViews(view)
        initObserve()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.isInSimpleMode = false
    }

    override fun initViews(view: View?) {
        super.initViews(view)
        swipeRefreshLayout.setOnRefreshListener {
            viewModel.loadArticles()
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        menu.findItem(R.id.settingsAction)?.isVisible = false
        menu.findItem(R.id.aboutAction)?.isVisible = false
    }

    override fun initObserve() {
        super.initObserve()

        viewModel.selectedSourceName.observe {
            binding.toolbarWithSubtitleContainer.toolbarSubtitleContainer.isVisible =
                !it.isNullOrEmpty()
            binding.toolbarWithSubtitleContainer.toolbarSubtitle.text = it
        }
        viewModel.selectedSourceImage.observe {
            binding.toolbarWithSubtitleContainer.toolbarImageView.load(it)
        }

    }
}
