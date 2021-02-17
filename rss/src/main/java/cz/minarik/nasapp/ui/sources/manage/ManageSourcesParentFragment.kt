package cz.minarik.nasapp.ui.sources.manage

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.tabs.TabLayoutMediator
import cz.minarik.base.ui.base.BaseFragment
import cz.minarik.nasapp.R
import cz.minarik.nasapp.ui.sources.selection.SourcesViewModel
import cz.minarik.nasapp.ui.sources.manage.lists.ManageSourceListsFragment
import cz.minarik.nasapp.ui.sources.manage.sources.ManageSourcesFragment
import kotlinx.android.synthetic.main.fragment_manage_sources_parent.*
import org.koin.android.ext.android.inject

class ManageSourcesParentFragment : BaseFragment(R.layout.fragment_manage_sources_parent) {

    val viewModel: SourcesViewModel by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val valuablesPagerAdapter = ManageSourcesParentAdapter()
        viewPager.adapter = valuablesPagerAdapter
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = valuablesPagerAdapter.getPageTitle(position)
        }.attach()
        viewPager.registerOnPageChangeCallback(
            object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    if (position == 0) fab.hide() else fab.show()
                }
            }
        )

        val navController = findNavController()
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        view.findViewById<MaterialToolbar>(R.id.toolbar)
            .setupWithNavController(navController, appBarConfiguration)
    }

    private inner class ManageSourcesParentAdapter : FragmentStateAdapter(this) {

        fun getPageTitle(position: Int): CharSequence {
            return getString(
                when (position) {
                    0 -> R.string.sources
                    1 -> R.string.lists
                    else -> R.string.lists
                }
            )
        }

        override fun getItemCount() = 2

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> ManageSourcesFragment()
                1 -> ManageSourceListsFragment()
                else -> ManageSourceListsFragment()
            }
        }
    }

}