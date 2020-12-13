package cz.minarik.nasapp.ui.articles.detail

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.transition.Transition
import android.view.View
import android.widget.ImageView
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import androidx.transition.TransitionInflater
import coil.load
import com.chimbori.crux.articles.Article
import com.liaoinstan.springview.widget.SpringView
import com.stfalcon.imageviewer.StfalconImageViewer
import cz.minarik.base.common.extensions.getFavIcon
import cz.minarik.base.common.extensions.isInternetAvailable
import cz.minarik.base.data.Status
import cz.minarik.base.ui.base.BaseFragment
import cz.minarik.nasapp.R
import cz.minarik.nasapp.ui.custom.ArticleDetailFooter
import cz.minarik.nasapp.ui.custom.ArticleDetailHeader
import cz.minarik.nasapp.ui.custom.GalleryViewClickListener
import cz.minarik.nasapp.ui.custom.GalleryViewImageDTO
import cz.minarik.nasapp.utils.*
import kotlinx.android.synthetic.main.fragment_article_detail.*
import okhttp3.HttpUrl
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import java.net.MalformedURLException
import java.net.URL

class ArticleDetailFragment : BaseFragment(R.layout.fragment_article_detail),
    GalleryViewClickListener {

    private val args: ArticleDetailFragmentArgs by navArgs()

    private var viewer: StfalconImageViewer<GalleryViewImageDTO>? = null

    private val articleDTO by lazy {
        args.article
    }

    override val viewModel by viewModel<ArticleDetailFragmentViewModel> {
        parametersOf(articleDTO.link, requireContext())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        initObserve()
    }

    private fun initViews() {
        initToolbar()
        sourceNameTextView.text = articleDTO.sourceName
        dateTextView.text = articleDTO.date?.toTimeElapsed()
        stateView.attacheContentView(contentContainer)
        initSpringView()
        requireContext().warmUpBrowser(articleDTO.link?.toUri())
        initArticleStarred()
        updateArticleStarred()
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

    private fun initSpringView() {
        springView.footer = ArticleDetailFooter()
        springView.header = ArticleDetailHeader()
        springView.setBackgroundColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.colorBackground
            )
        )
        springView.setListener(object : SpringView.OnFreshListener {
            override fun onRefresh() {
                openWebsite()
            }

            override fun onLoadmore() {
                openWebsite()
            }
        })
    }

    private fun openWebsite() {
        articleDTO.link?.toUri()?.let {
            requireContext().openCustomTabs(it, CustomTabsIntent.Builder())
        }
        springView.onFinishFreshAndLoad()
    }

    private fun initObserve() {
        viewModel.articleStarredLiveData.observe {
            updateArticleStarred()
        }
        viewModel.articleLiveData.observe { article ->
            updateArticleViews(article)
        }
        viewModel.state.observe {
            if (it.status == Status.FAILED) {
                if (!requireContext().isInternetAvailable) {
                    stateView.error(show = true, getString(R.string.no_internet_connection)) {
                        if (requireContext().isInternetAvailable) {
                            viewModel.loadArticleDetail()
                        }
                    }
                } else {
                    stateView.error(show = true, it.message) {
                        viewModel.loadArticleDetail()
                    }
                }
            } else {
                stateView.loading(false)
            }
        }
    }

    private fun initToolbar() {
        toolbar?.let {
            it.inflateMenu(R.menu.menu_article_detail)
            it.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.articleShareItem -> {
                        shareArticle(articleDTO)
                        true
                    }
                    else -> false
                }
            }
        }

        toolbarExpandedImage.load(articleDTO.image)
        fakeTitleTextView.text = articleDTO.title

        toolbarExpandedImage.transitionName = articleDTO.guid.toImageSharedTransitionName()
        fakeTitleTextView.transitionName = articleDTO.guid.toTitleSharedTransitionName()

        val navController = NavHostFragment.findNavController(this)
        val appBarConfiguration = AppBarConfiguration(navController.graph)

        toolbarLayout.setupWithNavController(toolbar, navController, appBarConfiguration)

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
            toolbarExpandedImage.setOnClickListener {
                showImage(0, toolbarExpandedImage)
            }
        }

        article?.document?.styleHtml(requireContext())
        article?.document?.html()?.let {
            webView.loadDataWithBaseURL(
                "",
                it.styleHtml(requireContext()),
                "text/html",
                "UTF-8",
                null
            )

            Handler(Looper.getMainLooper()).postDelayed({
                webViewContainer.isVisible = true
                webView?.isVisible = true
                shimmerViewContainer?.isVisible = false
            }, 100) //to prevent white screen from flashing (webView still loading styles)
        }

        article?.images?.let {
            if (it.size > 1) {
                initGallery(it)
            }
        }

        article?.videoUrl?.let {
            initVideo(it)
        }

    }

    //todo handle video
    private fun initVideo(videoUrl: HttpUrl) {
    }

    private fun initGallery(images: List<Article.Image>) {
        galleryView.setImages(images.map {
            GalleryViewImageDTO.fromApi(it)
        })
        galleryView.setListener(this)
        galleryContainer.isVisible = true
    }

    override fun showError(error: String?) {
    }

    override fun showLoading(show: Boolean) {
    }

    override fun onImageClicked(position: Int, clickedView: ImageView) {
        showImage(position + 1, clickedView)
    }

    private fun showImage(position: Int, clickedView: ImageView) {
        val images = viewModel.articleLiveData.value?.images?.map {
            GalleryViewImageDTO.fromApi(it)
        }?.toMutableList() ?: mutableListOf()
        viewModel.articleLiveData.value?.imgUrlSafe?.let {
            images.add(0, GalleryViewImageDTO(it))
        }

        val builder = StfalconImageViewer.Builder(
            context,
            images
        ) { view, image ->
            view.loadImageWithDefaultSettings(image.image)
        }
            .withStartPosition(position)
            .withTransitionFrom(
                clickedView
            )
            .withImageChangeListener {
                if (it == 0) {
                    viewer?.updateTransitionImage(
                        toolbarExpandedImage
                    )
                } else {
                    viewer?.updateTransitionImage(
                        galleryView.getImageForTransition(it - 1)
                    )
                }
            }
            .withHiddenStatusBar(false)

        viewer = builder.show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedElementEnterTransition =
            TransitionInflater.from(requireContext()).inflateTransition(android.R.transition.move)
                .addListener(
                    object : Transition.TransitionListener,
                        androidx.transition.Transition.TransitionListener {
                        override fun onTransitionStart(transition: androidx.transition.Transition) {
                        }

                        override fun onTransitionEnd(transition: androidx.transition.Transition) {
                            fakeTitleTextView.text = ""
                            toolbarLayout.title = articleDTO.title
                        }

                        override fun onTransitionCancel(transition: androidx.transition.Transition) {
                        }

                        override fun onTransitionPause(transition: androidx.transition.Transition) {
                        }

                        override fun onTransitionResume(transition: androidx.transition.Transition) {
                        }

                        override fun onTransitionStart(transition: Transition?) {
                        }

                        override fun onTransitionEnd(transition: Transition?) {
                        }

                        override fun onTransitionCancel(transition: Transition?) {
                        }

                        override fun onTransitionPause(transition: Transition?) {
                        }

                        override fun onTransitionResume(transition: Transition?) {
                        }

                    }
                )
    }
}