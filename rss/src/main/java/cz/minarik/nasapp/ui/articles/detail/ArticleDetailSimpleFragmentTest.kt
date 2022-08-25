package cz.minarik.nasapp.ui.articles.detail

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Point
import android.os.Build
import android.os.Bundle
import android.view.*
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.setPadding
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.lifecycleScope
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewFeature
import cz.minarik.base.common.extensions.openCustomTabs
import cz.minarik.base.common.extensions.warmUpBrowser
import cz.minarik.base.ui.base.BaseFragment
import cz.minarik.nasapp.BuildConfig
import cz.minarik.nasapp.R
import cz.minarik.nasapp.data.datastore.DataStoreManager
import cz.minarik.nasapp.data.domain.ArticleDTO
import cz.minarik.nasapp.ui.articles.ArticlesViewModel
import cz.minarik.nasapp.utils.Constants
import cz.minarik.nasapp.utils.shareArticle
import kotlinx.android.synthetic.main.fragment_article_detail_simple.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import timber.log.Timber


class ArticleDetailSimpleFragmentTest : BaseFragment(R.layout.fragment_article_detail_simple) {

    companion object {
        fun newInstance(
            articleDTO: ArticleDTO
        ): ArticleDetailSimpleFragmentTest =
            ArticleDetailSimpleFragmentTest().apply {
                arguments = bundleOf(
                    Constants.argArticleDTO to articleDTO,
                )
            }
    }

    private var starMenuItem: MenuItem? = null

    private val articleDTO by lazy {
        arguments?.getSerializable(Constants.argArticleDTO) as ArticleDTO
    }

    private var userInteracted = false

    private var urlsBeingLoaded = mutableListOf<String>()

    val viewModel by inject<ArticlesViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (savedInstanceState == null) {
            viewModel.loadArticleDetail(articleDTO)
        }
        initViews()
        initObserve()
    }

    private fun initObserve() {
        viewModel.articleStarredLiveData.observe {
            updateArticleStarred()
        }
    }

    private fun updateArticleStarred() {
        starMenuItem?.icon = ContextCompat.getDrawable(
            requireContext(),
            if (articleDTO.starred) R.drawable.ic_baseline_star_24 else R.drawable.ic_baseline_star_outline_24
        )
    }

    private fun initViews() {
        prepareWebView()
        loadArticleWebView()
        requireContext().warmUpBrowser(articleDTO.link?.toUri())
        initToolbar()
        webView.updateLayoutParams<ViewGroup.MarginLayoutParams> {
            setMargins(0, 0, 0, requireContext().navigationBarHeight)
        }
    }

    private fun initToolbar() {
        toolbar.let {
            (requireActivity() as AppCompatActivity).run {
                setSupportActionBar(it)
                supportActionBar?.setDisplayShowTitleEnabled(true)
                supportActionBar?.setDisplayHomeAsUpEnabled(true)
                supportActionBar?.setTitle(articleDTO.title)
            }
        }
    }

    private fun openWebsite() {
        lifecycleScope.launch {
            articleDTO.link?.toUri()?.let {
                if (DataStoreManager.getUseExternalBrowser().first()) {
                    startActivity(Intent(Intent.ACTION_VIEW, it))
                } else {
                    requireContext().openCustomTabs(it, CustomTabsIntent.Builder())
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_article_detail_simple, menu)
        starMenuItem = menu.findItem(R.id.actionStar)
        super.onCreateOptionsMenu(menu, inflater)
        updateArticleStarred()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.actionShareArticle -> {
                shareArticle(articleDTO)
                true
            }
            R.id.actionOpenWeb -> {
                openWebsite()
                true
            }
            android.R.id.home -> {
                requireActivity().onBackPressed()
                true
            }
            R.id.actionStar -> {
                viewModel.markArticleAsStarred(articleDTO)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    @SuppressLint("SetJavaScriptEnabled", "ClickableViewAccessibility")
    private fun prepareWebView() {
        webView.setOnTouchListener { _, _ ->
            userInteracted = true
            false
        }

        webView.settings.run {
            javaScriptCanOpenWindowsAutomatically = true
            javaScriptEnabled = true
            cacheMode = WebSettings.LOAD_DEFAULT
            allowContentAccess = true
            loadWithOverviewMode = true
            builtInZoomControls = false
            domStorageEnabled = true
        }
        webView.webViewClient = ArticleWebViewClient()
        webView.setOnKeyListener(View.OnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_BACK
                && event.action == MotionEvent.ACTION_UP
                && webView.canGoBack()
            ) {
                webView.goBack()
                return@OnKeyListener true
            }
            false
        })

        if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {
            WebSettingsCompat.setForceDark(webView.settings, WebSettingsCompat.FORCE_DARK_ON)
        }

        if (BuildConfig.DEBUG) {
            WebView.setWebContentsDebuggingEnabled(true)
        }
    }

    private fun loadArticleWebView() {
        articleDTO.link?.let {
            webView.loadUrl(it)
        }
    }


    private inner class ArticleWebViewClient : WebViewClient() {
        override fun shouldOverrideUrlLoading(
            view: WebView?,
            request: WebResourceRequest?
        ): Boolean {
            if (userInteracted) {
                articleDTO.link?.let { link ->
                    val isAnotherArticle = request?.url?.toString()?.contains(link, true) == false
                    if (isAnotherArticle) {
                        request?.url?.let { uri ->
                            context?.openCustomTabs(uri)
                        }
                    }
                    return isAnotherArticle
                }
            }

            return false
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            Timber.i("onPageFinished: $url")
            url?.let {
                urlsBeingLoaded.remove(url)
            }
        }

        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            Timber.i("onPageStarted: $url")
            url?.let {
                urlsBeingLoaded.add(url)
            }
            super.onPageStarted(view, url, favicon)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }
}

val Context.navigationBarHeight: Int
    get() {
        val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        return if (Build.VERSION.SDK_INT >= 30) {
            windowManager
                .currentWindowMetrics
                .windowInsets
                .getInsets(WindowInsets.Type.navigationBars())
                .bottom

        } else {
            val currentDisplay = try {
                display
            } catch (e: NoSuchMethodError) {
                windowManager.defaultDisplay
            }

            val appUsableSize = Point()
            val realScreenSize = Point()
            currentDisplay?.apply {
                getSize(appUsableSize)
                getRealSize(realScreenSize)
            }

            // navigation bar on the side
            if (appUsableSize.x < realScreenSize.x) {
                return realScreenSize.x - appUsableSize.x
            }

            // navigation bar at the bottom
            return if (appUsableSize.y < realScreenSize.y) {
                realScreenSize.y - appUsableSize.y
            } else 0
        }
    }