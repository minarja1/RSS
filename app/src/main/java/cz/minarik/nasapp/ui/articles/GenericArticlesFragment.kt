package cz.minarik.nasapp.ui.articles

import android.content.ComponentName
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
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
import androidx.navigation.fragment.FragmentNavigator
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import cz.minarik.base.common.extensions.dividerMedium
import cz.minarik.base.common.extensions.initToolbar
import cz.minarik.base.ui.base.BaseFragment
import cz.minarik.nasapp.R
import cz.minarik.nasapp.data.model.ArticleFilterType
import cz.minarik.nasapp.ui.custom.ArticleDTO
import cz.minarik.nasapp.ui.custom.MaterialSearchView
import cz.minarik.nasapp.utils.*
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent.setEventListener
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEventListener
import timber.log.Timber


abstract class GenericArticlesFragment(@LayoutRes private val layoutId: Int) :
    BaseFragment(layoutId) {

    abstract override val viewModel: GenericArticlesFragmentViewModel

    private var customTabsClient: CustomTabsClient? = null
    var customTabsSession: CustomTabsSession? = null

    var articlesRecyclerView: RecyclerView? = null
    var filterChipGroup: ChipGroup? = null
    var filterAll: Chip? = null
    var filterStarred: Chip? = null
    var filterUnread: Chip? = null
    var toolbar: Toolbar? = null
    var toolbarTitle: TextView? = null
    var searchView: MaterialSearchView? = null
    var toolbarContentContainer: ViewGroup? = null
    var suggestionsListView: ListView? = null

    private val customTabsConnection = object : CustomTabsServiceConnection() {
        override fun onCustomTabsServiceConnected(name: ComponentName, client: CustomTabsClient) {
            customTabsClient = client
            initCustomTabs()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
        }
    }

    abstract fun navigateToArticleDetail(extras: FragmentNavigator.Extras, articleDTO: ArticleDTO)

    val articlesAdapter by lazy {
        ArticlesAdapter(
            onItemClicked = { imageView, position ->
                (articlesRecyclerView?.adapter as? ArticlesAdapter)?.run {
                    getItemAtPosition(position)?.run {
                        read = true
                        viewModel.markArticleAsRead(this)
                        link?.toUri()?.let {
                            //todo pridat do nastaveni moznost volby
//                            requireContext().openCustomTabs(it, CustomTabsIntent.Builder())

                            imageView.transitionName = this.guid
                            val extras = FragmentNavigatorExtras(
                                //todo i title?
                                imageView to (this.guid ?: "")
                            )
                            navigateToArticleDetail(extras, this)
                        }
                    }
                    notifyItemChanged(position)
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
                val success = customTabsSession?.mayLaunchUrl(it.toUri(), null, null)
                Timber.i("Preloading url $it ${if (success == true) "SUCCESS" else "FAILED"}")
            },
            filterBySource = {
                val action = ArticlesFragmentDirections.actionArticlesToSimpleArticles(it ?: "")
                findNavController().navigate(action)
            }
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //init Custom tabs services
        CustomTabsClient.bindCustomTabsService(
            requireContext(),
            CHROME_PACKAGE,
            customTabsConnection
        )
    }

    private fun initCustomTabs() {
        customTabsClient?.run {
            warmup(0)
            customTabsSession = newSession(object : CustomTabsCallback() {
            })
        }
    }

    override fun showError(error: String?) {
        //todo
    }

    override fun showLoading(show: Boolean) {
        //todo
    }

    @CallSuper
    open fun initViews(view: View?) {
        articlesRecyclerView = view?.findViewById(R.id.articlesRecyclerView)
        filterChipGroup = view?.findViewById(R.id.filterChipGroup)
        filterAll = view?.findViewById(R.id.filterAll)
        filterStarred = view?.findViewById(R.id.filterStarred)
        filterUnread = view?.findViewById(R.id.filterUnread)
        toolbar = view?.findViewById(R.id.toolbar)
        toolbarTitle = view?.findViewById(R.id.toolbarTitle)
        searchView = view?.findViewById(R.id.search_view)
        toolbarContentContainer = view?.findViewById(R.id.toolbarContentContainer)
        suggestionsListView = view?.findViewById(R.id.suggestionsListView)

        articlesRecyclerView?.dividerMedium()
        articlesRecyclerView?.adapter = articlesAdapter
        initToolbar()
        initSearchView()
        setupFilters(view)
        initSwipeGestures()
    }

    private fun initSearchView() {
        setEventListener(
            requireActivity(),
            object : KeyboardVisibilityEventListener {
                override fun onVisibilityChanged(isOpen: Boolean) {
                    if (!isOpen) searchView?.dismissSuggestions()
                }
            })

        searchView?.run {
            setSuggestionsListView(suggestionsListView)
            setSuggestions(
                arrayOf("Mercury", "Venus", "Earth", "Mars")
            )
            setSubmitOnClick(true)
            setOnQueryTextListener(object : MaterialSearchView.OnQueryTextListener {
                override fun onQueryTextChange(newText: String?): Boolean {
                    if (newText.isNullOrEmpty()) viewModel.filterBySearchQuery(newText)
                    return true
                }

                override fun onQueryTextSubmit(query: String?): Boolean {
                    viewModel.filterBySearchQuery(query)
                    hideKeyboard()
                    dismissSuggestions()
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
        filterChipGroup?.isVisible = viewModel.prefManager.showArticleFilters
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

    private fun showFilterViews() {
        filterChipGroup?.isVisible = !(filterChipGroup?.isVisible ?: false)
        viewModel.prefManager.showArticleFilters = filterChipGroup?.isVisible ?: false
    }

    open fun initObserve() {
        viewModel.articles.observe {
            articlesAdapter.submitList(it) {
                if (viewModel.shouldScrollToTop) {
                    articlesRecyclerView?.scrollToTop()
                    viewModel.shouldScrollToTop = false
                }
            }
        }
    }

    private fun initToolbar() {
        toolbarTitle?.text = getString(R.string.articles_title)
        toolbar?.let { toolbar ->
            initToolbar(toolbar)
            toolbar.inflateMenu(R.menu.menu_articles_fragment)
            searchView?.setMenuItem(toolbar.menu.findItem(R.id.searchAction))
            toolbar.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.filterAction -> {
                        showFilterViews()
                        true
                    }
                    else -> false
                }
            }
        }
        (requireActivity() as AppCompatActivity).supportActionBar?.setDisplayShowTitleEnabled(false)
    }
}
