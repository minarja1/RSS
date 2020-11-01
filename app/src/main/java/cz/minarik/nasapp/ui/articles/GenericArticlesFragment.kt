package cz.minarik.nasapp.ui.articles

import android.content.ComponentName
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.browser.customtabs.*
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.view.isVisible
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
import cz.minarik.nasapp.ui.articles.source_selection.SourceSelectionViewModel
import cz.minarik.nasapp.utils.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber


abstract class GenericArticlesFragment(@LayoutRes private val layoutId: Int) :
    BaseFragment(layoutId) {

    override val viewModel by viewModel<ArticlesFragmentViewModel>()

    private val sourcesViewModel: SourceSelectionViewModel by inject()

    private var customTabsClient: CustomTabsClient? = null
    var customTabsSession: CustomTabsSession? = null

    var articlesRecyclerView: RecyclerView? = null
    var filterChipGroup: ChipGroup? = null
    var filterAll: Chip? = null
    var filterStarred: Chip? = null
    var filterUnread: Chip? = null
    var toolbar: Toolbar? = null
    var toolbarTitle: TextView? = null

    private val customTabsConnection = object : CustomTabsServiceConnection() {
        override fun onCustomTabsServiceConnected(name: ComponentName, client: CustomTabsClient) {
            customTabsClient = client
            initCustomTabs()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
        }
    }

    val articlesAdapter by lazy {
        ArticlesAdapter(
            onItemClicked = { _, position ->
                (articlesRecyclerView?.adapter as? ArticlesAdapter)?.run {
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
//                it?.let { sourcesViewModel.onSourceSelected(it) }
                findNavController().navigate(R.id.action_articles_to_simple_articles)
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
    }

    override fun showError(error: String?) {
        //todo
    }

    override fun showLoading(show: Boolean) {
        //todo
    }

    open fun initViews(view: View?) {
        articlesRecyclerView = view?.findViewById(R.id.articlesRecyclerView)
        filterChipGroup = view?.findViewById(R.id.filterChipGroup)
        filterAll = view?.findViewById(R.id.filterAll)
        filterStarred = view?.findViewById(R.id.filterStarred)
        filterUnread = view?.findViewById(R.id.filterUnread)
        toolbar = view?.findViewById(R.id.toolbar)
        toolbarTitle = view?.findViewById(R.id.toolbarTitle)

        articlesRecyclerView?.dividerMedium()
        articlesRecyclerView?.adapter = articlesAdapter
        initToolbar()
        setupFilters(view)
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
                }
            }
        }
    }

    private fun initToolbar() {
        toolbarTitle?.text = getString(R.string.articles_title)
        toolbar?.let {
            initToolbar(it)
            it.inflateMenu(R.menu.menu_articles_fragment)
            it.setOnMenuItemClickListener {
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
        (requireActivity() as AppCompatActivity).supportActionBar?.setDisplayShowTitleEnabled(false)
    }
}
