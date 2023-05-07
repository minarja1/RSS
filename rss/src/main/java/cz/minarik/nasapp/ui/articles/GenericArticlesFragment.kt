package cz.minarik.nasapp.ui.articles

import android.content.ComponentName
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.*
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.CallSuper
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
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.viewbinding.ViewBinding
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.chip.Chip
import cz.minarik.base.common.extensions.*
import cz.minarik.base.data.NetworkState
import cz.minarik.base.data.Status
import cz.minarik.nasapp.R
import cz.minarik.nasapp.base.Loading
import cz.minarik.nasapp.base.ViewModelState
import cz.minarik.nasapp.data.datastore.DataStoreManager
import cz.minarik.nasapp.data.domain.ArticleDTO
import cz.minarik.nasapp.data.domain.ArticleFilterType
import cz.minarik.nasapp.ui.MainActivity
import cz.minarik.nasapp.ui.articles.bottomSheet.ArticleBottomSheet
import cz.minarik.nasapp.ui.articles.bottomSheet.ArticleBottomSheetListener
import cz.minarik.nasapp.ui.articles.detail.ArticleDetailActivitySimple
import cz.minarik.nasapp.ui.base.BaseFragment
import cz.minarik.nasapp.ui.custom.MaterialSearchView
import cz.minarik.nasapp.ui.custom.StateView
import cz.minarik.nasapp.utils.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber


abstract class GenericArticlesFragment<Binding : ViewBinding> :
    BaseFragment<Binding>() {

    abstract val viewModel: ArticlesViewModel

    val viewState = ViewState()

    open val backEnabled = false

    abstract val articlesRecyclerView: RecyclerView
    abstract val swipeRefreshLayout: SwipeRefreshLayout
    abstract val appBarLayout: AppBarLayout
    abstract val filterUnread: Chip
    abstract val filterStarred: Chip
    abstract val filterAll: Chip
    abstract val stateView: StateView
    abstract val shimmerLayout: LinearLayout
    abstract val toolbar: Toolbar
    abstract val progressBar: ProgressBar
    abstract val shouldShowHorizontalProgressBar: Boolean

    private var customTabsClient: CustomTabsClient? = null
    var customTabsSession: CustomTabsSession? = null

    var toolbarTitle: TextView? = null
    var searchView: MaterialSearchView? = null
    var toolbarContentContainer: ViewGroup? = null

    private val articleDetailLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            updateVisibleItems()
        }

    private val customTabsConnection = object : CustomTabsServiceConnection() {
        override fun onCustomTabsServiceConnected(name: ComponentName, client: CustomTabsClient) {
            customTabsClient = client
            initCustomTabs()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
        }
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
        toolbarTitle = view?.findViewById(R.id.toolbarTitle)
        searchView = view?.findViewById(R.id.search_view)
        toolbarContentContainer = view?.findViewById(R.id.toolbarContentContainer)

        articlesRecyclerView?.apply {
            dividerMedium()
            layoutManager =
                LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
            (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
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
                    viewModel.logFilterBySearchQuery(query)
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

    private fun navigateToArticleDetail(
        articleDTO: ArticleDTO,
        position: Int,
        imageView: ImageView,
        textView: TextView,
    ) {
        val intent = Intent(requireContext(), ArticleDetailActivitySimple::class.java).apply {
            putExtra(Constants.argArticleDTO, articleDTO)
        }

        articleDetailLauncher.launch(intent)
    }

    protected val articlesAdapter by lazy {
        ArticlesAdapter(
            onArticleClicked = ::onArticleClicked,
            onItemLongClicked = ::onArticleLongClicked,
            onArticleExpanded = ::onArticleExpanded,
            onContactInfoClicked = ::onContactInfoClicked,
            filterBySource = { it?.let { filterBySource(it) } },
            articleShown = {
                //todo podle nastaveni prednacist url
//                val success = customTabsSession?.mayLaunchUrl(it.toUri(), null, null)
//                Timber.i("Preloading url $it ${if (success == true) "SUCCESS" else "FAILED"}")
            }
        )
    }


    private fun onContactInfoClicked(position: Int) {
        val adapter = (articlesRecyclerView?.adapter as? ArticlesAdapter)
        val article = adapter?.getItemAtPosition(position)
        article?.sourceUrl?.let {
            // todo log
            (requireActivity() as MainActivity).navigateToSourceDetail(it)
        }

    }

    private fun filterBySource(sourceUrl: String) {
        viewModel.logNavigateToSimpleArticles(sourceUrl)
        (requireActivity() as MainActivity).navigateToSimpleArticles(sourceUrl)
    }

    private fun onArticleExpanded(position: Int) {
        viewModel.logArticleExpanded()
    }

    private fun onArticleLongClicked(position: Int) {
        val adapter = (articlesRecyclerView?.adapter as? ArticlesAdapter)
        val article = adapter?.getItemAtPosition(position)

        viewModel.logArticleLongClicked(article)

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
                    viewModel.logArticleShared(article)
                }

                override fun onSource(sourceUrl: String) {
                    filterBySource(sourceUrl)
                }

            }
            sheet.show(childFragmentManager, Constants.ARTICLE_BOTTOM_SHEET_TAG)
        }
    }

    private fun onArticleClicked(imageView: ImageView, titleTextView: TextView, position: Int) {
        lifecycleScope.launch {
            val articlesAdapter = articlesRecyclerView?.adapter as? ArticlesAdapter
            articlesAdapter?.getItemAtPosition(position)?.apply {
                viewModel.logArticleClick(this)
                read = true
                viewModel.markArticleAsReadOrUnread(
                    article = this,
                    forceRead = true
                )
                link?.toUri()?.let {
                    val openExternally =
                        viewModel.sourceDao.getByUrl(this.sourceUrl ?: "")?.forceOpenExternally
                            ?: false
                    if (openExternally || DataStoreManager.getOpenArticlesInBrowser().first()) {
                        link?.toUri()?.let {
                            if (DataStoreManager.getUseExternalBrowser().first()) {
                                startActivity(Intent(Intent.ACTION_VIEW, it))
                            } else {
                                requireContext().openCustomTabs(it)
                            }
                        }
                    } else {
                        imageView.transitionName = this.guid?.toImageSharedTransitionName()
                        titleTextView.transitionName =
                            this.guid?.toTitleSharedTransitionName()
                        navigateToArticleDetail(this, position, imageView, titleTextView)
                    }
                }
            }
            articlesAdapter?.notifyItemChanged(position)
        }
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
        enableRecyclerAnimationsTemporarily()
        val article = articlesAdapter.getItemAtPosition(adapterPosition)
        article?.let {
            it.read = !it.read
            articlesAdapter.notifyItemChanged(adapterPosition)
            viewModel.markArticleAsReadOrUnread(it)
        }
    }

    private fun enablerRecyclerAnimations(enable: Boolean) {
        (articlesRecyclerView?.itemAnimator as? SimpleItemAnimator)?.supportsChangeAnimations =
            enable
    }

    //this is necessary for swipe actions
    private fun enableRecyclerAnimationsTemporarily() {
        enablerRecyclerAnimations(true)
        Handler(Looper.getMainLooper()).postDelayed({
            enablerRecyclerAnimations(false)
        }, 200)
    }

    private fun starItem(adapterPosition: Int, viewHolder: RecyclerView.ViewHolder) {
        enableRecyclerAnimationsTemporarily()
        val article = articlesAdapter.getItemAtPosition(adapterPosition)
        article?.let {
            it.starred = !it.starred
            articlesAdapter.notifyItemChanged(adapterPosition)
            viewModel.markArticleAsStarred(it)
        }
    }

    private fun setupFilters(view: View?) {
        DataStoreManager.getArticleFilter().collectWhenStarted {
            view?.findViewById<Chip>(it.chipId)?.isChecked = true
        }

        filterAll.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                viewModel.setArticlesFilter(ArticleFilterType.All)
            }
        }
        filterStarred.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                viewModel.setArticlesFilter(ArticleFilterType.Starred)
            }
        }
        filterUnread.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                viewModel.setArticlesFilter(ArticleFilterType.Unread)
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
            articlesAdapter.submitList(it) {
                if (viewModel.shouldScrollToTop) scrollToTop()
            }

        }
    }

    fun scrollToTop() {
        appBarLayout.setExpanded(true)
        articlesRecyclerView.scrollToTop()
        viewModel.shouldScrollToTop = false
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
            R.id.settingsAction -> {
                viewModel.logSettingsOpened()
                (requireActivity() as MainActivity).navigateToSettings()
                true
            }
            R.id.aboutAction -> {
                viewModel.logAboutOpened()
                (requireActivity() as MainActivity).navigateToAbout()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    /**
     * Update all necessary views to properly reflect viewState
     */
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

        val showHorizontalProgressBar =
            shouldShowHorizontalProgressBar && loading && !articlesEmpty && !isError && !showLoadingSwipeRefresh

        Timber.i(
            "updateViews: loadingArticles: $loadingArticles, loadingSources: " +
                    "$loadingSources, loading: $loading, isError: $isError, articlesEmpty:" +
                    " $articlesEmpty, loadingMessage: $loadingMessage, showShimmer: $showShimmer, " +
                    "showLoadingSwipeRefresh: $showLoadingSwipeRefresh " +
                    "isFromSwipeRefresh: ${viewModel.isFromSwipeRefresh}"
        )

        swipeRefreshLayout.isRefreshing = showLoadingSwipeRefresh
        swipeRefreshLayout.isEnabled = !showShimmer

        progressBar.isVisible = showHorizontalProgressBar

        if (articlesEmpty && !isError && !loading) {
            if (filterStarred.isChecked) {
                stateView.emptyStarred(true)
            } else {
                if (requireContext().isInternetAvailable) {
                    stateView.empty(true)
                } else {
                    stateView.noInternet(true) {
                        if (requireContext().isInternetAvailable) {
                            viewModel.loadArticlesOrSources()
                        }
                    }
                }
            }
        } else if (isError) {
            if (articlesEmpty) {
                //full-screen error
                stateView.error(show = true, message = loadingMessage) {
                    viewModel.loadArticlesOrSources()
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

    protected fun updateVisibleItems() {
        //update visible items in adapter (changed might have been made in article detail OR SimpleArticlesFragment)
        articlesAdapter.notifyItemRangeChanged(
            (articlesRecyclerView.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition(),
            (articlesRecyclerView.layoutManager as LinearLayoutManager).findLastVisibleItemPosition()
        )
    }
}
