package cz.minarik.nasapp.ui.articles

import android.content.ComponentName
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.activity.addCallback
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.*
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.view.GravityCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.google.android.material.chip.Chip
import cz.minarik.base.common.extensions.dividerMedium
import cz.minarik.base.common.extensions.initToolbar
import cz.minarik.base.common.extensions.showToast
import cz.minarik.base.common.extensions.tint
import cz.minarik.base.data.NetworkState
import cz.minarik.base.data.Status
import cz.minarik.base.ui.base.BaseFragment
import cz.minarik.nasapp.R
import cz.minarik.nasapp.data.model.ArticleFilterType
import cz.minarik.nasapp.ui.articles.source_selection.SourceSelectionViewModel
import cz.minarik.nasapp.ui.custom.ArticleDTO
import cz.minarik.nasapp.utils.*
import kotlinx.android.synthetic.main.fragment_articles.*
import kotlinx.android.synthetic.main.include_toolbar_with_subtitle.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber


class ArticlesFragment : BaseFragment(R.layout.fragment_articles) {

    override val viewModel by viewModel<ArticlesFragmentViewModel>()

    private val sourcesViewModel: SourceSelectionViewModel by inject()

    private val viewState = ViewState()

    private var customTabsClient: CustomTabsClient? = null
    private var customTabsSession: CustomTabsSession? = null

    private val customTabsConnection = object : CustomTabsServiceConnection() {
        override fun onCustomTabsServiceConnected(name: ComponentName, client: CustomTabsClient) {
            customTabsClient = client
            initCustomTabs()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            val test = 1
        }
    }

    private val articlesAdapter by lazy {
        ArticlesAdapter(
            onItemClicked = { _, position ->
                (articlesRecyclerView.adapter as? ArticlesAdapter)?.run {
                    getItemAtPosition(position)?.run {
                        read = true
                        viewModel.markArticleAsRead(this)
                        link?.toUri()?.let {
                            requireContext().openCustomTabs(it, CustomTabsIntent.Builder())
                        }
                    }
                    notifyItemChanged(position)
                }
            },
            onItemExpanded = { position ->
                //make sure bottom of item is on screen
                (articlesRecyclerView.layoutManager as? LinearLayoutManager)?.run {
                    Handler(Looper.getMainLooper()).postDelayed({
                        if (findLastCompletelyVisibleItemPosition() < position) {
                            val recyclerOffset = articlesRecyclerView.height - appBarLayout.bottom
                            val offset =
                                recyclerOffset - (findViewByPosition(position)?.height ?: 0)
                            scrollToPositionWithOffset(
                                position,
                                offset - 30
                            )//add 30 px todo test on more devices
                        }
                    }, Constants.ARTICLE_EXPAND_ANIMATION_DURATION)
                }
            },
            preloadUrl = {
                val success = customTabsSession?.mayLaunchUrl(it.toUri(), null, null)
                Timber.i("Preloading url $it ${if (success == true) "SUCCESS" else "FAILED"}")
            }
        )
    }

    var doubleBackToExitPressedOnce = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //init Custom tabs services
        CustomTabsClient.bindCustomTabsService(
            requireContext(),
            CHROME_PACKAGE,
            customTabsConnection
        )

        requireActivity().onBackPressedDispatcher.addCallback(this) {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START)
            } else if (articlesRecyclerView.isScrolledToTop()) {
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
                articlesRecyclerView.scrollToTop()
            }
        }
    }

    private fun initCustomTabs() {
        customTabsClient?.run {
            warmup(0)
            customTabsSession = newSession(object : CustomTabsCallback() {
            })
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initObserve()
        initViews(view)
        initSwipeToRefresh()
    }

    override fun showError(error: String?) {
        //todo
    }

    override fun showLoading(show: Boolean) {
        //todo
    }

    private fun initViews(view: View?) {
        initToolbar()
        articlesRecyclerView.dividerMedium()
        articlesRecyclerView.adapter = articlesAdapter
        setupDrawerNavigation()
        setupFilters(view)
        stateView.attacheContentView(articlesRecyclerView)
        initSwipeGestures()
    }

    private fun initSwipeGestures() {
        val itemTouchHelper = ItemTouchHelper(
            getSwipeActionItemTouchHelperCallback(
                colorDrawableBackground = ColorDrawable(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.colorBackground
                    )
                ),
                getIcon = ::getSwipeIcon,
                callback = ::starItem
            )
        )
        itemTouchHelper.attachToRecyclerView(articlesRecyclerView)
    }

    private fun getSwipeIcon(adapterPosition: Int, viewHolder: RecyclerView.ViewHolder): Drawable {
        val article = articlesAdapter.getItemAtPosition(adapterPosition)
        val icon = ContextCompat.getDrawable(
            requireContext(),
            if (article?.starred == true) R.drawable.ic_baseline_star_24 else R.drawable.ic_baseline_star_outline_24
        )!!
        icon.setTint(ContextCompat.getColor(requireContext(), R.color.yellow))
        return icon
    }

    private fun starItem(adapterPosition: Int, viewHolder: RecyclerView.ViewHolder) {
        val article = articlesAdapter.getItemAtPosition(adapterPosition)
        article?.let {
            it.starred = !it.starred
            articlesAdapter.notifyItemChanged(adapterPosition)
            viewModel.markArticleAsStarred(it)
        }
    }

    private fun setupFilters(view: View?) {
        filterChipGroup.isVisible = viewModel.prefManager.showArticleFilters
        viewModel.prefManager.getArticleFilter().let {
            view?.findViewById<Chip>(it.chipId)?.isChecked = true
        }

        filterAll.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                viewModel.filterArticles(ArticleFilterType.All)
            }
        }
        filterStarred.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                viewModel.filterArticles(ArticleFilterType.Starred)
            }
        }
        filterUnread.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                viewModel.filterArticles(ArticleFilterType.Unread)
            }
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
        toolbar.navigationIcon?.tint(requireContext(), R.color.colorOnBackground)
    }

    private fun initToolbar() {
        initToolbar(toolbar)
        toolbarTitle.text = getString(R.string.articles_title)
        (requireActivity() as AppCompatActivity).supportActionBar?.setDisplayShowTitleEnabled(false)
        toolbar.inflateMenu(R.menu.menu_articles_fragment)
        toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.searchAction -> {
                    true
                }
                R.id.filterAction -> {
                    showFilterViews()
                    true
                }
                else -> false
            }
        }
    }

    private fun showFilterViews() {
        filterChipGroup.isVisible = !filterChipGroup.isVisible
        viewModel.prefManager.showArticleFilters = filterChipGroup.isVisible
    }

    private fun initObserve() {
        viewModel.state.observe {
            //todo drzet si v NetworkState i exception a rozlisovat noInternet od jinych
            viewState.loadingArticlesState = it
        }

        viewModel.articles.observe {
            articlesAdapter.submitList(it) {
                if (viewModel.shouldScrollToTop) {
                    articlesRecyclerView.scrollToTop()
                }
            }
            viewState.articles = it
        }   

        sourcesViewModel.selectedSource.observe {
            viewModel.loadArticles(scrollToTop = true)
        }
        sourcesViewModel.selectedSourceName.observe {
            toolbarSubtitleContainer.isVisible = !it.isNullOrEmpty()
            toolbarSubtitle.text = it
        }
        sourcesViewModel.selectedSourceImage.observe {
            toolbarImageView.load(it)
        }

        sourcesViewModel.sourceRepository.state.observe {
            if (it == NetworkState.SUCCESS) {
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
