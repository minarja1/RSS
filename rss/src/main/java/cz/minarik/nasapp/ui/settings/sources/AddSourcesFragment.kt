package cz.minarik.nasapp.ui.settings.sources

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.MaterialTheme.typography
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import com.google.android.material.composethemeadapter.MdcTheme
import cz.minarik.base.ui.base.BaseFragment
import cz.minarik.nasapp.R
import cz.minarik.nasapp.data.domain.RSSSource
import org.koin.androidx.viewmodel.ext.android.viewModel


class AddSourcesFragment : BaseFragment(R.layout.fragment_add_sources) {

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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        view?.findViewById<ComposeView>(R.id.composeView)?.setContent {
            MdcTheme {
                Scaffold {
                    Sources(viewModel.allSources.collectAsState(emptyList()))
                }
            }
        }
        return view
    }

    @Preview
    @Composable
    fun Sources(@PreviewParameter(RSSSourcesPreviewProvider::class) sources: State<List<RSSSource>>) {
        LazyColumn(
            Modifier.background(colorResource(id = R.color.colorBackground)),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
        ) {
            items(sources.value) {
                SourceItem(it)
            }
        }
    }

    @Composable
    fun SourceItem(source: RSSSource) {
        Row (verticalAlignment = Alignment.CenterVertically){
            Image(
                painter = rememberImagePainter(source.imageUrl),
                contentDescription = null,
                modifier = Modifier.size(32.dp)
            )
            Text(
                modifier = Modifier.padding(start = 16.dp),
                text = source.title ?: "", style = typography.h6
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
