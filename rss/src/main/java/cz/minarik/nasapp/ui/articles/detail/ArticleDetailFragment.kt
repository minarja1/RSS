package cz.minarik.nasapp.ui.articles.detail

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.*
import android.webkit.*
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewFeature
import coil.load
import com.chimbori.crux.articles.Article
import com.google.android.material.appbar.AppBarLayout
import cz.minarik.base.common.extensions.*
import cz.minarik.base.data.Status
import cz.minarik.nasapp.BuildConfig
import cz.minarik.nasapp.R
import cz.minarik.nasapp.data.datastore.DataStoreManager
import cz.minarik.nasapp.data.domain.ArticleDTO
import cz.minarik.nasapp.databinding.FragmentArticleDetailBinding
import cz.minarik.nasapp.ui.articles.ArticlesViewModel
import cz.minarik.nasapp.ui.base.BaseActivity
import cz.minarik.nasapp.ui.base.BaseFragment
import cz.minarik.nasapp.ui.sources.detail.SourceDetailFragment
import cz.minarik.nasapp.utils.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import timber.log.Timber
import java.net.MalformedURLException
import java.net.URL
import kotlin.math.abs


class ArticleDetailFragment : BaseFragment<FragmentArticleDetailBinding>() {

    override fun getViewBinding(): FragmentArticleDetailBinding =
        FragmentArticleDetailBinding.inflate(layoutInflater)

    companion object {
        fun newInstance(
            articleDTO: ArticleDTO
        ): ArticleDetailFragment =
            ArticleDetailFragment().apply {
                arguments = bundleOf(
                    Constants.argArticleDTO to articleDTO,
                )
            }
    }

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

    private fun initViews() {
        initToolbar()
        prepareWebView()
        loadArticleWebView()
        binding.sourceNameTextView.text = articleDTO.sourceName
        binding.dateTextView.text = articleDTO.date?.toTimeElapsed()
        binding.stateView.attacheContentView(binding.contentContainer)
        requireContext().warmUpBrowser(articleDTO.link?.toUri())
        initArticleStarred()
        updateArticleStarred()
        binding.sourceInfoBackground.setOnClickListener {
            articleDTO.sourceUrl?.let {
                (requireActivity() as BaseActivity).replaceFragment(
                    SourceDetailFragment.newInstance(
                        it
                    )
                )
            }
        }
    }

    private fun updateArticleStarred() {
        binding.starImageButton.setImageDrawable(
            ContextCompat.getDrawable(

                requireContext(),
                if (articleDTO.starred) R.drawable.ic_baseline_star_24 else R.drawable.ic_baseline_star_outline_24
            )
        )
    }

    private fun initArticleStarred() {
        binding.starImageButton.setOnClickListener {
            viewModel.markArticleAsStarred(articleDTO)
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

    private fun initObserve() {
        viewModel.articleStarredLiveData.observe {
            updateArticleStarred()
        }
        viewModel.articleLiveData.toFreshLiveData().observe { article ->
            updateArticleViews(article)
        }
        viewModel.state.observe {
            if (it.status == Status.FAILED) {
                if (!requireContext().isInternetAvailable) {
                    binding.stateView.error(
                        show = true,
                        getString(R.string.no_internet_connection)
                    ) {
                        if (requireContext().isInternetAvailable) {
                            viewModel.loadArticleDetail(articleDTO)
                        }
                    }
                } else {
                    binding.stateView.error(show = true, it.message) {
                        viewModel.loadArticleDetail(articleDTO)
                    }
                }
            } else {
                binding.stateView.loading(false)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_article_detail, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.actionShareArticle -> {
                shareArticle(articleDTO)
                viewModel.logArticleShared(articleDTO)
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
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun initToolbar() {
        binding.run {
            toolbar.let {
                (requireActivity() as AppCompatActivity).run {
                    setSupportActionBar(it)
                    supportActionBar?.setDisplayShowTitleEnabled(false)
                    supportActionBar?.setDisplayHomeAsUpEnabled(true)
                }
            }

            appBarLayout.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
                if (abs(verticalOffset) - appBarLayout.totalScrollRange == 0) {
                    // Collapsed
                    toolbarLayout.title = articleDTO.title
                } else {
                    // Expanded
                    toolbarLayout.title = " "
                }
            })

            fakeTitleTextView.text = articleDTO.title
            fakeTitleTextView.transitionName = articleDTO.guid.toTitleSharedTransitionName()

            toolbarExpandedImage.transitionName = articleDTO.guid.toImageSharedTransitionName()
            toolbarExpandedImage.load(articleDTO.image)
            requireActivity().startPostponedEnterTransition()
            toolbarLayout.setExpandedTitleColor(Color.TRANSPARENT)
            try {
                val url = URL(articleDTO.sourceUrl)
                sourceImageView.load(url.getFavIcon())
            } catch (e: MalformedURLException) {
            }
        }
    }


    private fun updateArticleViews(article: Article?) {
        article?.imgUrlSafe?.let {
            binding.toolbarExpandedImage.loadImageWithDefaultSettings(
                uri = it.replace("http://", "https://"),
                placeholder = binding.toolbarExpandedImage.drawable
            )
        }
    }

    @SuppressLint("SetJavaScriptEnabled", "ClickableViewAccessibility")
    private fun prepareWebView() {
        binding.run {
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

            if (WebViewFeature.isFeatureSupported(WebViewFeature.ALGORITHMIC_DARKENING)) {
                Timber.d("Force dark supported")
                WebSettingsCompat.setAlgorithmicDarkeningAllowed(
                    webView.settings,
                    true,
                )
            }

            if (BuildConfig.DEBUG) {
                WebView.setWebContentsDebuggingEnabled(true)
            }
        }
    }

    private fun loadArticleWebView() {
        articleDTO.link?.let {
            binding.webView.loadUrl(it)
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

        @TargetApi(Build.VERSION_CODES.M)
        override fun onReceivedError(
            view: WebView?,
            request: WebResourceRequest?,
            error: WebResourceError?
        ) {
            error?.let {
                handleError(it.errorCode)
            }

            super.onReceivedError(view, request, error)
        }

        @SuppressWarnings("deprecation")
        override fun onReceivedError(
            view: WebView?,
            errorCode: Int,
            description: String?,
            failingUrl: String?
        ) {
            handleError(errorCode)
            super.onReceivedError(view, errorCode, description, failingUrl)
        }


        override fun onPageFinished(view: WebView?, url: String?) {
            Timber.i("onPageFinished: $url")
            url?.let {
                urlsBeingLoaded.remove(url)
                invalidateProgressBar()
            }
        }

        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            Timber.i("onPageStarted: $url")
            url?.let {
                urlsBeingLoaded.add(url)
                invalidateProgressBar()
            }
            super.onPageStarted(view, url, favicon)
        }
    }

    private fun invalidateProgressBar() {
        binding.progressBar?.isVisible = urlsBeingLoaded.size > 0
    }

    fun handleError(code: Int) {
        if (code == -2) {
            //other errors ignored because sometimes webView will just throw an error but the page is actually loaded
            context?.let {
                if (!it.isInternetAvailable) {
                    binding.stateView.noInternet(true) {
                        if (it.isInternetAvailable) {
                            binding.stateView.loading(false)
                            loadArticleWebView()
                        }
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

}