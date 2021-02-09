package cz.minarik.nasapp.ui.sources.detail

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import coil.load
import com.google.android.material.appbar.MaterialToolbar
import cz.minarik.base.common.extensions.getFavIcon
import cz.minarik.base.common.extensions.showToast
import cz.minarik.base.ui.base.BaseFragment
import cz.minarik.nasapp.R
import cz.minarik.nasapp.utils.copyToClipBoard
import cz.minarik.nasapp.utils.openCustomTabs
import kotlinx.android.synthetic.main.fragment_source_detail.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import java.net.URL


class SourceDetailFragment : BaseFragment(R.layout.fragment_source_detail) {

    private val args: SourceDetailFragmentArgs by navArgs()
    private lateinit var toolbarTitleTextView: TextView
    private lateinit var toolbarImageView: ImageView

    val sourceUrl by lazy {
        args.sourceUrl
    }
    override val viewModel by viewModel<SourceDetailViewModel> {
        parametersOf(sourceUrl)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
        initObserve()
    }

    private fun initObserve() {
        viewModel.sourceLiveData.observe { rssSource ->
            toolbarTitleTextView.text = rssSource.title
            toolbarImageView.load(URL(sourceUrl).getFavIcon())
            descriptionTextView.text = rssSource.description

            feedUrlButton.setText(sourceUrl)
            feedUrlButton.setOnClickListener {
                requireContext().copyToClipBoard("source URL", sourceUrl)
                showToast(requireContext(), getString(R.string.copied_to_clipboard))
            }

            val homePage = rssSource.homePage?.toUri()
            homepageButton.isVisible = homePage != null
            homepageButton.setOnClickListener {
                homePage?.let {
                    requireContext().openCustomTabs(it, CustomTabsIntent.Builder())
                }
            }

            val contact = rssSource.contactUrl?.toUri()
            contactInfoButton.isVisible = contact != null
            contactInfoButton.setOnClickListener {
                contact?.let {
                    requireContext().openCustomTabs(it, CustomTabsIntent.Builder())
                }
            }
        }
    }

    private fun initViews(view: View) {
        val navController = findNavController()
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        view.findViewById<MaterialToolbar>(R.id.toolbar)
            .setupWithNavController(navController, appBarConfiguration)
        toolbarTitleTextView = view.findViewById(R.id.toolbarTitle)
        toolbarImageView = view.findViewById(R.id.toolbarImageView)
    }

    override fun showError(error: String?) {
    }

    override fun showLoading(show: Boolean) {
    }


}