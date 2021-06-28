package cz.minarik.nasapp.ui.articles.detail

import android.annotation.SuppressLint
import android.annotation.TargetApi
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
import androidx.transition.TransitionInflater
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewFeature
import coil.load
import com.chimbori.crux.articles.Article
import com.google.android.material.appbar.AppBarLayout
import cz.minarik.base.common.extensions.*
import cz.minarik.base.data.Status
import cz.minarik.base.ui.base.BaseFragment
import cz.minarik.nasapp.BuildConfig
import cz.minarik.nasapp.R
import cz.minarik.nasapp.ui.MainActivity
import cz.minarik.nasapp.ui.articles.ArticlesViewModel
import cz.minarik.nasapp.ui.custom.ArticleDTO
import cz.minarik.nasapp.utils.*
import kotlinx.android.synthetic.main.fragment_article_detail.*
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import timber.log.Timber
import java.net.MalformedURLException
import java.net.URL
import kotlin.math.abs


class ArticleDetailFragment : BaseFragment(R.layout.fragment_article_detail) {

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

    val viewModel by sharedViewModel<ArticlesViewModel>()

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
        sourceNameTextView.text = articleDTO.sourceName
        dateTextView.text = articleDTO.date?.toTimeElapsed()
        stateView.attacheContentView(contentContainer)
        requireContext().warmUpBrowser(articleDTO.link?.toUri())
        initArticleStarred()
        updateArticleStarred()
        sourceInfoBackground.setOnClickListener {
            articleDTO.sourceUrl?.let {
                (requireActivity() as MainActivity).navigateToSourceDetail(it)
            }
        }
    }

    private fun updateArticleStarred() {
        starImageButton.setImageDrawable(
            ContextCompat.getDrawable(
                requireContext(),
                if (articleDTO.starred) R.drawable.ic_baseline_star_24 else R.drawable.ic_baseline_star_outline_24
            )
        )
    }

    private fun initArticleStarred() {
        starImageButton.setOnClickListener {
            viewModel.markArticleAsStarred(articleDTO)
        }
    }

    private fun openWebsite() {
        articleDTO.link?.toUri()?.let {
            requireContext().openCustomTabs(it, CustomTabsIntent.Builder())
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
                    stateView.error(show = true, getString(R.string.no_internet_connection)) {
                        if (requireContext().isInternetAvailable) {
                            viewModel.loadArticleDetail(articleDTO)
                        }
                    }
                } else {
                    stateView.error(show = true, it.message) {
                        viewModel.loadArticleDetail(articleDTO)
                    }
                }
            } else {
                stateView.loading(false)
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

        toolbarExpandedImage.loadImageWithDefaultSettings(articleDTO.image)
        fakeTitleTextView.text = articleDTO.title

        toolbarLayout.setExpandedTitleColor(Color.TRANSPARENT)

        try {
            val url = URL(articleDTO.sourceUrl)
            sourceImageView.load(url.getFavIcon())
        } catch (e: MalformedURLException) {
        }
    }


    private fun updateArticleViews(article: Article?) {
        article?.imgUrlSafe?.let {
            toolbarExpandedImage.loadImageWithDefaultSettings(
                uri = it.replace("http://", "https://"),
                placeholder = toolbarExpandedImage.drawable
            )
//            toolbarExpandedImage.setOnClickListener {
//                showImage(0, toolbarExpandedImage)
//            }
        }

//        article?.document?.styleHtml(requireContext())
//        article?.document?.html()?.let {
//            webView.loadDataWithBaseURL(
//                "",
//                it.styleHtml(requireContext()),
//                "text/html",
//                "UTF-8",
//                null
//            )
//
//            Handler(Looper.getMainLooper()).postDelayed({
//                webViewContainer?.isVisible = true
//                webView?.isVisible = true
//                shimmerViewContainer?.isVisible = false
//            }, 100) //to prevent white screen from flashing (webView still loading styles)
//        }
//
//        article?.images?.let {
//            if (it.size > 1) {
//                initGallery(it)
//            }
//        }
//
//        article?.videoUrl?.let {
//            initVideo(it)
//        }
    }

    @SuppressLint("SetJavaScriptEnabled", "ClickableViewAccessibility")
    private fun prepareWebView() {
        webView.setOnTouchListener { v, event ->
            userInteracted = true
            false
        }

        webView.settings.run {
            javaScriptCanOpenWindowsAutomatically = true
            javaScriptEnabled = true
            cacheMode = WebSettings.LOAD_DEFAULT
            allowContentAccess = true
            loadWithOverviewMode = true
            builtInZoomControls = true
            builtInZoomControls = true
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
                    if (request?.url?.toString()?.contains(link, true) == false) {
                        request.url?.let {
                            requireContext().openCustomTabs(it)
                        }
                    }
                    return request?.url?.toString()?.contains(link, true) == false
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
        progressBar?.isVisible = urlsBeingLoaded.size > 0
    }

    fun handleError(code: Int) {
        if (code == -2) {
            stateView?.noInternet(true) {
                if (requireContext().isInternetAvailable) {
                    stateView.loading(false)
                    loadArticleWebView()
                }
            }
        }
        //other errors ignored because sometimes webView will just throw an error but the page is actually loaded
    }

//    override fun onImageClicked(position: Int, clickedView: ImageView) {
//        showImage(position + 1, clickedView)
//    }

//    private fun showImage(position: Int, clickedView: ImageView) {
//        val images = viewModel.articleLiveData.value?.images?.map {
//            GalleryViewImageDTO.fromApi(it)
//        }?.toMutableList() ?: mutableListOf()
//        viewModel.articleLiveData.value?.imgUrlSafe?.let {
//            images.add(0, GalleryViewImageDTO(it))
//        }
//
//        val builder = StfalconImageViewer.Builder(
//            context,
//            images
//        ) { view, image ->
//            view.loadImageWithDefaultSettings(image.image)
//        }
//            .withStartPosition(position)
//            .withTransitionFrom(
//                clickedView
//            )
//            .withImageChangeListener {
//                if (it == 0) {
//                    viewer?.updateTransitionImage(
//                        toolbarExpandedImage
//                    )
//                }
//            }
//            .withHiddenStatusBar(false)
//
//        viewer = builder.show()
//    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

}