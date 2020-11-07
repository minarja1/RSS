package cz.minarik.nasapp.ui.articles.detail

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.core.view.isVisible
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import androidx.transition.TransitionInflater
import coil.load
import cz.minarik.base.common.extensions.getFavIcon
import cz.minarik.base.ui.base.BaseFragment
import cz.minarik.nasapp.R
import cz.minarik.nasapp.utils.imgUrlSafe
import cz.minarik.nasapp.utils.loadImageWithDefaultSettings
import cz.minarik.nasapp.utils.styleHtml
import cz.minarik.nasapp.utils.toTimeElapsed
import kotlinx.android.synthetic.main.fragment_article_detail.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import java.net.URL

class ArticleDetailFragment : BaseFragment(R.layout.fragment_article_detail) {

    private val args: ArticleDetailFragmentArgs by navArgs()

    val articleDTO by lazy {
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedElementEnterTransition =
            TransitionInflater.from(requireContext()).inflateTransition(android.R.transition.move)
    }

    private fun initViews() {
        toolbarExpandedImage.transitionName = articleDTO.guid

        toolbarExpandedImage.load(articleDTO.image)

        val navController = NavHostFragment.findNavController(this)
        val appBarConfiguration = AppBarConfiguration(navController.graph)

        toolbarLayout.setupWithNavController(toolbar, navController, appBarConfiguration)

        try {
            val url = URL(articleDTO.sourceUrl)
            sourceImageView.load(url.getFavIcon())
        } catch (e: Exception) {
        }

        val sourceNameAndPubDate = "${articleDTO.sourceName} | ${articleDTO.date?.toTimeElapsed()}"
        sourceNameDateTextView.text = sourceNameAndPubDate
    }

    private fun initObserve() {
        viewModel.articleLiveData.observe { article ->
            article?.imgUrlSafe?.let {
                toolbarExpandedImage.loadImageWithDefaultSettings(
                    uri = it.replace("http://", "https://"),
                    placeholder = toolbarExpandedImage.drawable
                )
            }
            toolbarLayout.title = article?.title

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
                    webView?.isVisible = true
                }, 100) //to prevent white screen from flashing (webView still loading styles)
            }

            //todo handle article media (images and/or videos)
        }
    }

    override fun showError(error: String?) {
    }

    override fun showLoading(show: Boolean) {
    }

}