package cz.minarik.nasapp.ui.settings.sources

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.MaterialTheme.typography
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import cz.minarik.nasapp.R
import cz.minarik.nasapp.data.domain.RSSSource
import cz.minarik.nasapp.data.domain.RSSSourceNotificationListItem
import cz.minarik.nasapp.databinding.FragmentAddSourcesBinding
import cz.minarik.nasapp.ui.base.BaseFragment
import org.koin.androidx.viewmodel.ext.android.viewModel


class AddSourcesFragment : BaseFragment<FragmentAddSourcesBinding>() {

    companion object {
        fun newInstance() = AddSourcesFragment()
    }

    val viewModel by viewModel<AddSourcesViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
    }

    private fun initViews(view: View) {
        setHasOptionsMenu(true)

        (requireActivity() as AppCompatActivity).run {
            setSupportActionBar(view.findViewById(R.id.toolbar))
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.title = requireContext().getString(R.string.notification_settings)
        }
    }

    override fun getViewBinding(): FragmentAddSourcesBinding =
        FragmentAddSourcesBinding.inflate(layoutInflater)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        view?.findViewById<ComposeView>(R.id.composeView)?.setComposeContent {
            Scaffold {
                Sources(viewModel.allSources.collectAsState().value)
            }
        }
        return view
    }

    @Preview
    @Composable
    fun Sources(
        @PreviewParameter(RSSSourcesPreviewProvider::class)
        sources: List<RSSSourceNotificationListItem>
    ) {
        LazyColumn(
            Modifier.background(colorResource(id = R.color.colorBackground)),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(sources) {
                SourceItem(it)
            }
        }
    }

    @Composable
    fun SourceItem(source: RSSSourceNotificationListItem) {
        Row(
            modifier = Modifier
                .clickable {
                    viewModel.sourceSelected(source)
                }
                .height(48.dp)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = rememberImagePainter(source.imageUrl),
                contentDescription = null,
                modifier = Modifier.size(32.dp)
            )
            Text(
                modifier = Modifier
                    .padding(start = 16.dp)
                    .weight(1f),
                text = source.title ?: "", style = typography.body1
            )
            Image(
                modifier = Modifier.alpha(if (source.notificationsEnabled) 1f else 0f),
                painter = painterResource(id = R.drawable.ic_baseline_check_circle_outline_24),
                contentDescription = null,
                colorFilter = ColorFilter.tint(colorResource(id = R.color.colorAccent)),
            )

        }
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


    class RSSSourcesPreviewProvider : PreviewParameterProvider<RSSSource> {
        private val fakeSources = listOf(
            RSSSource(title = "NASA.com"),
            RSSSource(title = "ESA.com"),
            RSSSource(title = "KAK.com"),
        )

        override val values = fakeSources.asSequence()
    }

}
