package cz.minarik.nasapp.ui.articles

import android.content.ComponentName
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.*
import android.widget.TextView
import androidx.annotation.CallSuper
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.browser.customtabs.CustomTabsCallback
import androidx.browser.customtabs.CustomTabsClient
import androidx.browser.customtabs.CustomTabsServiceConnection
import androidx.browser.customtabs.CustomTabsSession
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.google.android.material.chip.Chip
import cz.minarik.base.common.extensions.*
import cz.minarik.base.data.NetworkState
import cz.minarik.base.data.Status
import cz.minarik.base.ui.base.BaseFragment
import cz.minarik.nasapp.R
import cz.minarik.nasapp.base.Loading
import cz.minarik.nasapp.base.ViewModelState
import cz.minarik.nasapp.data.domain.ArticleFilterType
import cz.minarik.nasapp.ui.MainActivity
import cz.minarik.nasapp.ui.articles.bottomSheet.ArticleBottomSheet
import cz.minarik.nasapp.ui.articles.bottomSheet.ArticleBottomSheetListener
import cz.minarik.nasapp.ui.custom.ArticleDTO
import cz.minarik.nasapp.ui.custom.MaterialSearchView
import cz.minarik.nasapp.utils.*
import kotlinx.android.synthetic.main.fragment_articles.*
import kotlinx.android.synthetic.main.fragment_recycler.*
import timber.log.Timber


abstract class GenericArticlesFragment(@LayoutRes private val layoutId: Int) :
    BaseFragment(layoutId) {

    abstract val viewModel: ArticlesViewModel

    val viewState = ViewState()

    open val backEnabled = false

    private var customTabsClient: CustomTabsClient? = null
    var customTabsSession: CustomTabsSession? = null

    var toolbar: Toolbar? = null
    var toolbarTitle: TextView? = null
    var searchView: MaterialSearchView? = null
    var toolbarContentContainer: ViewGroup? = null

    private val customTabsConnection = object : CustomTabsServiceConnection() {
        override fun onCustomTabsServiceConnected(name: ComponentName, client: CustomTabsClient) {
            customTabsClient = client
            initCustomTabs()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
        }
    }

    fun navigateToArticleDetail(
        articleDTO: ArticleDTO,
        position: Int,
        vararg sharedElements: Pair<View, String>,
    ) {
        (requireActivity() as MainActivity).navigateToArticleDetail(articleDTO, *sharedElements)
    }

    private val articlesAdapter by lazy {
        ArticlesAdapter(
            onItemClicked = { imageView, titleTextView, position ->
                (articlesRecyclerView?.adapter as? ArticlesAdapter)?.run {
                    getItemAtPosition(position)?.run {
                        read = true
                        viewModel.markArticleAsReadOrUnread(this, true)
                        link?.toUri()?.let {
                            if (openExternally) {
                                link?.toUri()?.let {
                                    requireContext().openCustomTabs(it)
                                }
                            } else {
                                imageView.transitionName = this.guid?.toImageSharedTransitionName()
                                titleTextView.transitionName =
                                    this.guid?.toTitleSharedTransitionName()
                                navigateToArticleDetail(
                                    this,
                                    position,
                                    imageView to (this.guid?.toImageSharedTransitionName() ?: ""),
                                    titleTextView to (this.guid?.toTitleSharedTransitionName()
                                        ?: ""),
                                )
                            }
                        }
                    }
                    notifyItemChanged(position)
                }
            },
            onItemLongClicked = { position ->
                val adapter = (articlesRecyclerView?.adapter as? ArticlesAdapter)
                val article = adapter?.getItemAtPosition(position)

                article?.run {
                    val sheet = ArticleBottomSheet.newInstance(this)
                    sheet.listener = object : ArticleBottomSheetListener {
                        override fun onStarred() {
                            article.starred = !article.starred
                            adapter.notifyItemChanged(position)
                            viewModel.markArticleAsStarred(article)
                        }

                        override fun onRead() {
                            article.read = !article.read
                            adapter.notifyItemChanged(position)
                            viewModel.markArticleAsReadOrUnread(article)
                        }

                        override fun onShare() {
                            shareArticle(article)
                        }

                        override fun onSource(sourceUrl: String) {
                            filterBySource(sourceUrl)
                        }

                    }
                    sheet.show(childFragmentManager, Constants.ARTICLE_BOTTOM_SHEET_TAG)
                }
            },
            onItemExpanded = { position ->
                //make sure bottom of item is on screen
                (articlesRecyclerView?.layoutManager as? LinearLayoutManager)?.run {
                    Handler(Looper.getMainLooper()).postDelayed({
                        if (findLastCompletelyVisibleItemPosition() < position) {
                            val recyclerOffset =
                                (articlesRecyclerView?.height ?: 0) //todo - appBarLayout.bottom
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
                //todo podle nastaveni
//                val success = customTabsSession?.mayLaunchUrl(it.toUri(), null, null)
//                Timber.i("Preloading url $it ${if (success == true) "SUCCESS" else "FAILED"}")
            },
            filterBySource = {
                if (it != null) {
                    filterBySource(it)
                }
            }
        )
    }

    private fun filterBySource(sourceUrl: String) {
        (requireActivity() as MainActivity).navigateToSimpleArticles(sourceUrl)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        //init Custom tabs services
        val success = CustomTabsClient.bindCustomTabsService(
            requireContext(),
            CHROME_PACKAGE,
            customTabsConnection
        )
        Timber.i("Binding Custom Tabs service ${if (success) "SUCCESSFUL" else "FAILED"}")
    }

    private fun initCustomTabs() {
        customTabsClient?.run {
            warmup(0)
            customTabsSession = newSession(object : CustomTabsCallback() {
            })
        }
    }

    @CallSuper
    open fun initViews(view: View?) {
        toolbar = view?.findViewById(R.id.toolbar)
        toolbarTitle = view?.findViewById(R.id.toolbarTitle)
        searchView = view?.findViewById(R.id.search_view)
        toolbarContentContainer = view?.findViewById(R.id.toolbarContentContainer)

        articlesRecyclerView?.apply {
            dividerMedium()
            layoutManager =
                LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
            //todo swiping not working when uncommented
//            (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
            adapter = articlesAdapter
        }
        initToolbar()
        initSearchView()
        setupFilters(view)
        initSwipeGestures()


        swipeRefreshLayout.setColorSchemeColors(
            ContextCompat.getColor(
                requireContext(),
                R.color.colorOnSurface
            )
        )
        swipeRefreshLayout.setProgressBackgroundColorSchemeColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.colorSurface
            )
        )
    }

    private fun initSearchView() {
        searchView?.run {
            setOnQueryTextListener(object : MaterialSearchView.OnQueryTextListener {
                override fun onQueryTextChange(newText: String?): Boolean {
//                    if (newText.isNullOrEmpty()) viewModel.filterBySearchQuery(newText)
                    return true
                }

                override fun onQueryTextSubmit(query: String?): Boolean {
                    viewModel.filterBySearchQuery(query)
                    hideKeyboard()
                    searchView?.let {
                        mSearchSrcTextView.clearFocus()
                    }
                    return true
                }
            })

            setOnSearchViewListener(object : MaterialSearchView.SearchViewListener {
                override fun onSearchViewClosed() {
                    viewModel.filterBySearchQuery("")
                    toolbarContentContainer?.isVisible = true
                }

                override fun onSearchViewShown() {
                    toolbarContentContainer?.isVisible = false
                }
            })
        }
    }

    private fun initSwipeGestures() {
        val starTouchHelper = ItemTouchHelper(
            getSwipeActionItemTouchHelperCallback(
                colorDrawableBackground = ColorDrawable(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.colorBackground
                    )
                ),
                getIcon = ::getStarIcon,
                callback = ::starItem,
                iconMarginHorizontal = 32.dpToPx,
                swipeDirs = ItemTouchHelper.LEFT
            )
        )
        starTouchHelper.attachToRecyclerView(articlesRecyclerView)

        val readTouchHelper = ItemTouchHelper(
            getSwipeActionItemTouchHelperCallback(
                colorDrawableBackground = ColorDrawable(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.colorBackground
                    )
                ),
                getIcon = ::getReadIcon,
                callback = ::markAsReadOrUnread,
                iconMarginHorizontal = 32.dpToPx,
                swipeDirs = ItemTouchHelper.RIGHT
            )
        )
        readTouchHelper.attachToRecyclerView(articlesRecyclerView)
    }

    private fun getReadIcon(adapterPosition: Int, viewHolder: RecyclerView.ViewHolder): Drawable {
        val article = articlesAdapter.getItemAtPosition(adapterPosition)
        val icon = ContextCompat.getDrawable(
            requireContext(),
            if (article?.read == true)
                R.drawable.ic_baseline_undo_24
            else
                R.drawable.ic_baseline_check_24
        )!!
        icon.setTint(ContextCompat.getColor(requireContext(), R.color.colorAccent))
        return icon
    }

    private fun getStarIcon(adapterPosition: Int, viewHolder: RecyclerView.ViewHolder): Drawable {
        val article = articlesAdapter.getItemAtPosition(adapterPosition)
        val icon = ContextCompat.getDrawable(
            requireContext(),
            if (article?.starred == true)
                R.drawable.ic_baseline_star_outline_24
            else
                R.drawable.ic_baseline_star_24
        )!!
        icon.setTint(ContextCompat.getColor(requireContext(), R.color.yellow))
        return icon
    }

    private fun markAsReadOrUnread(adapterPosition: Int, viewHolder: RecyclerView.ViewHolder) {
        val article = articlesAdapter.getItemAtPosition(adapterPosition)
        article?.let {
            it.read = !it.read
            articlesAdapter.notifyItemChanged(adapterPosition)
            viewModel.markArticleAsReadOrUnread(it)
        }
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
        viewModel.prefManager.getArticleFilter().let {
            view?.findViewById<Chip>(it.chipId)?.isChecked = true
        }

        filterAll?.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                viewModel.filterArticles(ArticleFilterType.All)
            }
        }
        filterStarred?.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                viewModel.filterArticles(ArticleFilterType.Starred)
            }
        }
        filterUnread?.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                viewModel.filterArticles(ArticleFilterType.Unread)
            }
        }
    }

    abstract fun getArticlesLiveData(): MutableLiveData<List<ArticleDTO>>

    open fun initObserve() {
        viewModel.state.observe {
            viewState.loadingArticlesState = it
        }

        viewModel.articlesRepository.state.observe {
            viewState.loadingArticlesFromServer = it
        }

        getArticlesLiveData().observe {
            viewState.articles = it
            articlesAdapter.submitList(it)

            if (viewModel.shouldScrollToTop) {
                appBarLayout.setExpanded(true)
                articlesRecyclerView?.scrollToTop()
                viewModel.shouldScrollToTop = false
            }
        }
    }

    private fun initToolbar() {
        toolbarTitle?.text = getString(R.string.app_name)
        (requireActivity() as AppCompatActivity).run {
            setSupportActionBar(toolbar)
            supportActionBar?.setDisplayShowTitleEnabled(false)
            supportActionBar?.setDisplayHomeAsUpEnabled(backEnabled)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_articles_fragment, menu)
        searchView?.setMenuItem(menu.findItem(R.id.searchAction))
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                requireActivity().onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun updateViews() {
        val loadingArticles =
            viewState.loadingArticlesState == NetworkState.LOADING || viewState.loadingArticlesFromServer == Loading
        val loadingSources = viewState.loadingSourcesState == NetworkState.LOADING
        val loading = loadingArticles || loadingSources
        val isError = viewState.loadingArticlesState?.status == Status.FAILED
        val articlesEmpty = viewState.articles.isEmpty()
        val loadingMessage = viewState.loadingArticlesState?.message

        val showShimmer = loading && articlesEmpty && !isError
        shimmerLayout.isVisible = showShimmer

        val showLoadingSwipeRefresh =
            loadingArticles && !showShimmer && !isError && viewModel.isFromSwipeRefresh
        swipeRefreshLayout.isRefreshing = showLoadingSwipeRefresh
        swipeRefreshLayout.isEnabled = !showShimmer

        if (articlesEmpty && !isError && !loading) {
            stateView.empty(true)
        } else if (isError) {
            if (articlesEmpty) {
                //full-screen error
                stateView.error(show = true, message = loadingMessage) {
                    viewModel.loadArticles()
                }
            } else {
                showToast(
                    requireContext(),
                    loadingMessage ?: getString(R.string.common_base_error)
                )
                stateView.error(false)
            }
        } else {
            //hide stateView
            stateView.show(false)
        }
    }

    inner class ViewState {
        var loadingArticlesState: NetworkState? = null
            set(value) {
                field = value
                updateViews()
            }
        var loadingArticlesFromServer: ViewModelState? = null
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

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            articlesAdapter.notifyItemRangeChanged(
                (articlesRecyclerView.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition(),
                (articlesRecyclerView.layoutManager as LinearLayoutManager).findLastVisibleItemPosition()
            )
        }
    }
}
