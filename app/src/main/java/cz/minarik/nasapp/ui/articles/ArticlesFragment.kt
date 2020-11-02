package cz.minarik.nasapp.ui.articles

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.activity.addCallback
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.core.view.isVisible
import coil.load
import cz.minarik.base.common.extensions.showToast
import cz.minarik.base.common.extensions.tint
import cz.minarik.base.data.NetworkState
import cz.minarik.base.data.Status
import cz.minarik.nasapp.R
import cz.minarik.nasapp.ui.articles.source_selection.SourceSelectionFragment
import cz.minarik.nasapp.ui.articles.source_selection.SourceSelectionViewModel
import cz.minarik.nasapp.ui.custom.ArticleDTO
import cz.minarik.nasapp.utils.isScrolledToTop
import cz.minarik.nasapp.utils.scrollToTop
import cz.minarik.nasapp.utils.toFreshLiveData
import kotlinx.android.synthetic.main.fragment_articles.*
import kotlinx.android.synthetic.main.include_toolbar_with_subtitle.*
import org.koin.android.ext.android.inject
import timber.log.Timber


class ArticlesFragment : GenericArticlesFragment(R.layout.fragment_articles) {

    private val sourcesViewModel: SourceSelectionViewModel by inject()

    private val viewState = ViewState()

    private var doubleBackToExitPressedOnce = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(this) {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START)
            } else if (articlesRecyclerView?.isScrolledToTop() == true) {
                if (doubleBackToExitPressedOnce) {
                    requireActivity().finish()
                } else {
                    doubleBackToExitPressedOnce = true;
                    showToast(requireContext(), getString(R.string.press_back_again_to_leave))

                    Handler(Looper.getMainLooper()).postDelayed({
                        doubleBackToExitPressedOnce = false
                    }, 2000)

                }
            } else {
                articlesRecyclerView?.scrollToTop()
                appBarLayout.setExpanded(true)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
        initObserve()
        initSwipeToRefresh()
    }

    override fun showError(error: String?) {
        //todo
    }

    override fun showLoading(show: Boolean) {
        //todo
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

        //todo tohle je nechutne
        Handler(Looper.getMainLooper()).postDelayed({
            try {
                val supportFragmentManager = activity?.supportFragmentManager
                val transaction = supportFragmentManager?.beginTransaction()
                transaction?.let {
                    it.replace(R.id.nav_view_content, SourceSelectionFragment())
                    it.commitNow()
                }
            } catch (e: Exception) {

            }
        }, 500)
    }

    override fun initObserve() {
        super.initObserve()

        viewModel.articles.observe {
            viewState.articles = it
            if (viewModel.shouldScrollToTop) {
                appBarLayout.setExpanded(true)
            }
        }

        viewModel.state.observe {
            //todo drzet si v NetworkState i exception a rozlisovat noInternet od jinych
            viewState.loadingArticlesState = it
        }


        sourcesViewModel.selectedSource.toFreshLiveData().observe {
            viewModel.loadArticles(scrollToTop = true)

        }
        sourcesViewModel.selectedSourceName.observe {
            toolbarSubtitleContainer.isVisible = !it.isNullOrEmpty()
            toolbarSubtitle.text = it
        }
        sourcesViewModel.selectedSourceImage.observe {
            toolbarImageView.load(it)
        }

        sourcesViewModel.sourceRepository.state.toFreshLiveData().observe {
            if (it == NetworkState.SUCCESS) {
                //todo tohle je spatny
                sourcesViewModel.updateSources()
                viewModel.loadArticles()
            }
            viewState.loadingSourcesState = it
        }
    }

    private fun initSwipeToRefresh() {
        viewModel.state.observe {
            swipeRefreshLayout.isRefreshing = it == NetworkState.LOADING
        }

        swipeRefreshLayout.setOnRefreshListener {
            viewModel.loadArticles(true)
        }
    }

    private fun updateViews() {
        val loadingArticles = viewState.loadingArticlesState == NetworkState.LOADING
        val loadingSources = viewState.loadingSourcesState == NetworkState.LOADING
        val isError = viewState.loadingArticlesState?.status == Status.FAILED
        val articlesEmpty = viewState.articles.isEmpty()
        val loadingMessage = viewState.loadingArticlesState?.message

        if (articlesEmpty && !isError && !loadingArticles && !loadingSources) {
            stateView.empty(true)
        } else if (loadingSources && articlesEmpty && !isError) {
            stateView.loading(true, getString(R.string.updating_sources))
        } else if (isError) {
            if (articlesEmpty) {
                //full-screen error
                stateView.error(show = true, message = loadingMessage) {
                    viewModel.loadArticles()
                }
            } else {
                showToast(requireContext(), loadingMessage ?: getString(R.string.common_base_error))
            }
        } else {
            stateView.loading(false)
        }
    }

    inner class ViewState {
        var loadingArticlesState: NetworkState? = null
            set(value) {
                field = value
                updateViews()
            }
        var loadingSourcesState: NetworkState? = null
            set(value) {
                field = value
                updateViews()
            }
        var articles: List<ArticleDTO> = emptyList()
            set(value) {
                field = value
                updateViews()
            }
    }
}
