package cz.minarik.nasapp.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.fragment.app.Fragment
import cz.minarik.nasapp.R
import cz.minarik.nasapp.ui.MainActivity

class SettingsFragment : Fragment() {

    companion object {
        fun newInstance(): SettingsFragment = SettingsFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme {
                    Surface(color = MaterialTheme.colors.background) {
                        SettingsScreen();
                    }
                }
            }
        }
    }


    @Composable
    fun Toolbar() {
        TopAppBar(
            title = {
                Text(
                    text = stringResource(id = R.string.settings),
                    color = colorResource(id = R.color.colorOnSurface)
                )
            },
            navigationIcon = {
                IconButton(onClick = {
                    (requireActivity() as MainActivity).goBack()
                }) {
                    Icon(
                        painterResource(id = R.drawable.ic_action_navigation_arrow_back),
                        contentDescription = null
                    )
                }
            },
            backgroundColor = colorResource(id = R.color.colorBackground),
            contentColor = colorResource(id = R.color.colorOnBackground)
        )
    }

    @Composable
    fun SettingsScreen() {
        Scaffold(
            topBar = {
                Toolbar()
            }, content = {
                SettingsContent()
            })
    }

    @Composable
    fun SettingsContent() {
        LazyColumn(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth()
                .background(colorResource(id = R.color.colorBackground))
        ) {

        }
    }

    @Preview(showBackground = true)
    @Composable
    fun DefaultPreview() {
        MaterialTheme {
            SettingsScreen();
        }
    }
}