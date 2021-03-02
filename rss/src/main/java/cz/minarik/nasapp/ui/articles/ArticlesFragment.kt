package cz.minarik.nasapp.ui.articles

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.addCallback
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.core.view.isVisible
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import coil.load
import cz.minarik.base.common.extensions.isScrolledToTop
import cz.minarik.base.common.extensions.scrollToTop
import cz.minarik.base.common.extensions.showToast
import cz.minarik.base.common.extensions.tint
import cz.minarik.nasapp.R
import cz.minarik.nasapp.data.datastore.DataStoreManager
import cz.minarik.nasapp.ui.MainActivity
import cz.minarik.nasapp.ui.custom.ArticleDTO
import cz.minarik.nasapp.ui.sources.selection.SourceSelectionFragment
import cz.minarik.nasapp.ui.sources.selection.SourcesViewModel
import cz.minarik.nasapp.utils.toFreshLiveData
import kotlinx.android.synthetic.main.fragment_articles.*
import kotlinx.android.synthetic.main.include_toolbar_with_subtitle.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.sharedViewModel


class ArticlesFragment : GenericArticlesFragment(R.layout.fragment_articles) {

    private val sourcesViewModel by sharedViewModel<SourcesViewModel>()

    override val viewModel by sharedViewModel<ArticlesViewModel>()

    override fun getArticlesLiveData(): MutableLiveData<List<ArticleDTO>> = viewModel.articles

    private var doubleBackToExitPressedOnce = false

    private val useDrawer = false

    private val newArticlesFlow = DataStoreManager.getNewArticlesIDs()

    private var notificationBadge: ViewGroup? = null
    private var notificationBadgeTextView: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(this) {
            if ((requireActivity() as MainActivity).getCurrentFragment() != this@ArticlesFragment) {
                (requireActivity() as MainActivity).goBack()
            } else {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START)
                } else if ((requireActivity() as MainActivity).sourcesFragmentShown) {
                    (requireActivity() as MainActivity).showHideSourceSelection(false)
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
                    scrollToTop()
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
        if (useDrawer) setupDrawerNavigation()
        articlesRecyclerView?.let {
            stateView.attacheContentView(it)
        }
        toolbarPadding.isVisible = true
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
            drawerLayout?.closeDrawer(GravityCompat.START)
            (requireActivity() as MainActivity).showHideSourceSelection(false)

            lifecycleScope.launch {
                DataStoreManager.setNewArticlesIDs(setOf())
            }
        }
        sourcesViewModel.selectedSourceName.observe {
            toolbarSubtitleContainer.isVisible = !it.isNullOrEmpty()
            toolbarSubtitle.text = it
        }
        sourcesViewModel.selectedSourceImage.observe {
            toolbarImageView.load(it)
        }

        sourcesViewModel.sourceRepository.sourcesChanged.toFreshLiveData().observe {
            if (it) viewModel.loadArticles(updateFromServer = true)
        }

        sourcesViewModel.sourceRepository.state.toFreshLiveData().observe {
            viewState.loadingSourcesState = it
        }
        newArticlesFlow.collectWhenStarted {
            updateBadgeNumber(it.size)
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        val newArticlesItem = menu.findItem(R.id.newArticlesAction)
        val notificationsActionView = newArticlesItem?.actionView

        notificationsActionView?.setOnClickListener {
            onOptionsItemSelected(newArticlesItem)
        }

        if (notificationBadge == null) {
            notificationBadge = notificationsActionView?.findViewById(R.id.notificationBadge)
        }

        if (notificationBadgeTextView == null) {
            notificationBadgeTextView =
                notificationsActionView?.findViewById(R.id.notificationCountTextView)
        }
    }

    //todo move to base!
    protected fun <T> Flow<T>.collectWhenStarted(function: (value: T) -> Unit) {
        this.let { flow ->
            lifecycleScope.launchWhenStarted {
                flow.collect {
                    function.invoke(it)
                }
            }
        }
    }

    private fun initSwipeToRefresh() {
        swipeRefreshLayout.setOnRefreshListener {
            viewModel.loadArticles(
                scrollToTop = false,
                updateFromServer = true,
                isFromSwipeRefresh = true
            )
        }
    }

    private fun updateBadgeNumber(badgeNumber: Int?) {
        notificationBadge?.isVisible = badgeNumber != null && badgeNumber > 0
        val countString =
            when {
                badgeNumber == null -> ""
                badgeNumber < 100 -> badgeNumber.toString()
                else -> "99+"
            }
        notificationBadgeTextView?.text = countString
    }

}