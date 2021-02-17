package cz.minarik.nasapp.ui.articles

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.activity.addCallback
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.core.view.isVisible
import androidx.lifecycle.MutableLiveData
import coil.load
import cz.minarik.base.common.extensions.isScrolledToTop
import cz.minarik.base.common.extensions.scrollToTop
import cz.minarik.base.common.extensions.showToast
import cz.minarik.base.common.extensions.tint
import cz.minarik.base.data.NetworkState
import cz.minarik.nasapp.R
import cz.minarik.nasapp.ui.MainActivity
import cz.minarik.nasapp.ui.custom.ArticleDTO
import cz.minarik.nasapp.ui.sources.selection.SourceSelectionFragment
import cz.minarik.nasapp.ui.sources.selection.SourcesViewModel
import cz.minarik.nasapp.utils.toFreshLiveData
import kotlinx.android.synthetic.main.fragment_articles.*
import kotlinx.android.synthetic.main.include_toolbar_with_subtitle.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel


class ArticlesFragment : GenericArticlesFragment(R.layout.fragment_articles) {

    private val sourcesViewModel: SourcesViewModel by inject()

    override val viewModel by sharedViewModel<ArticlesViewModel>()

    override fun getArticlesLiveData(): MutableLiveData<List<ArticleDTO>> = viewModel.articles

    private var doubleBackToExitPressedOnce = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(this) {
            if ((requireActivity() as MainActivity).getCurrentFragment() != this@ArticlesFragment) {
                (requireActivity() as MainActivity).goBack()
            } else {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START)
                } else if (searchView?.isSearchOpen == true) {
                    searchView?.closeSearch()
                } else if (articlesRecyclerView?.isScrolledToTop() == true) {
                    when {
                        doubleBackToExitPressedOnce -> {
                            requireActivity().finish()
                        }
                        else -> {
                            doubleBackToExitPressedOnce = true;
                            showToast(
                                requireContext(),
                                getString(R.string.press_back_again_to_leave)
                            )

                            Handler(Looper.getMainLooper()).postDelayed({
                                doubleBackToExitPressedOnce = false
                            }, 2000)

                        }
                    }
                } else {
                    articlesRecyclerView?.scrollToTop()
                    appBarLayout.setExpanded(true)
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
        initObserve()
        initSwipeToRefresh()
    }

    override fun initViews(view: View?) {
        super.initViews(view)
        setupDrawerNavigation()
        articlesRecyclerView?.let {
            stateView.attacheContentView(it)
        }
    }


    private fun setupDrawerNavigation() {
        drawerLayout?.let {
            val toggle = ActionBarDrawerToggle(
                activity, it, toolbar, 0, 0
            )
            it.addDrawerListener(toggle)
            toggle.syncState()
        }
        toolbar?.navigationIcon?.tint(requireContext(), R.color.colorOnBackground)

        val fragmentManager = childFragmentManager
        fragmentManager.executePendingTransactions()
        val transaction = fragmentManager.beginTransaction()
        transaction.let {
            it.replace(R.id.nav_view_content, SourceSelectionFragment())
            it.commit()
        }
    }

    override fun initObserve() {
        super.initObserve()
        sourcesViewModel.selectedSourceChanged.toFreshLiveData().observe {
            viewModel.loadArticles(scrollToTop = true)
            Handler(Looper.getMainLooper()).postDelayed({
                drawerLayout?.closeDrawer(GravityCompat.START)
            }, 350)
        }
        sourcesViewModel.selectedSourceName.observe {
            toolbarSubtitleContainer.isVisible = !it.isNullOrEmpty()
            toolbarSubtitle.text = it
        }
        sourcesViewModel.selectedSourceImage.observe {
            toolbarImageView.load(it)
        }

        sourcesViewModel.sourceRepository.sourcesChanged.toFreshLiveData().observe {
            if (it) viewModel.loadArticles(updateDb = true)
        }

        sourcesViewModel.sourceRepository.state.toFreshLiveData().observe {
            if (it == NetworkState.SUCCESS) {
                sourcesViewModel.updateAll()
            }
            viewState.loadingSourcesState = it
        }
    }

    private fun initSwipeToRefresh() {
        swipeRefreshLayout.setOnRefreshListener {
            viewModel.loadArticles(scrollToTop = false, updateDb = true, isFromSwipeRefresh = true)
        }
    }

}
