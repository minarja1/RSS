package cz.minarik.nasapp.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import cz.minarik.nasapp.R
import cz.minarik.nasapp.data.datastore.DataStoreManager
import cz.minarik.nasapp.databinding.ActivityMainBinding
import cz.minarik.nasapp.ui.about.AboutFragment
import cz.minarik.nasapp.ui.articles.ArticlesFragment
import cz.minarik.nasapp.ui.articles.simple.SimpleArticlesFragment
import cz.minarik.nasapp.ui.base.BaseActivity
import cz.minarik.nasapp.ui.settings.SettingsFragment
import cz.minarik.nasapp.ui.sources.detail.SourceDetailFragment
import cz.minarik.nasapp.ui.sources.selection.SourceSelectionFragment
import cz.minarik.nasapp.ui.sources.selection.SourcesViewModel
import cz.minarik.nasapp.utils.ExitWithAnimation
import cz.minarik.nasapp.utils.exitCircularReveal
import cz.minarik.nasapp.utils.findLocationOfCenterOnTheScreen
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : BaseActivity() {

    private lateinit var binding: ActivityMainBinding

    companion object {
        const val sourcesFragmentTag = "sourcesFragment"
    }

    val sourcesViewModel by viewModel<SourcesViewModel>()
    private var sourcesFragment: Fragment? = null
    private var initialSyncFinished: Boolean = false
    var sourcesFragmentShown: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        sourcesViewModel //initialization
        if (savedInstanceState == null) {
            replaceFragment(ArticlesFragment())
        } else {
            sourcesFragment = supportFragmentManager.findFragmentByTag(sourcesFragmentTag)
            showHideSourceSelection(sourcesFragmentShown)
        }
        initViews()
        initObserve()
    }

    private fun initObserve() {
        lifecycleScope.launchWhenStarted {
            DataStoreManager.getInitialSyncFinished().collect {
                initialSyncFinished = it
                updateFabVisibility()
            }
        }
    }

    private fun initViews() {
        binding.fab.setOnClickListener {
            showHideSourceSelection(true)
        }
        supportFragmentManager.addOnBackStackChangedListener {
            updateFabVisibility()
        }
    }

    fun getCurrentFragment(): Fragment? {
        return supportFragmentManager.primaryNavigationFragment
    }

    fun goBack() {
        supportFragmentManager.popBackStackImmediate()
    }

    private fun updateFabVisibility() {
        if (supportFragmentManager.backStackEntryCount == 1 && !sourcesFragmentShown && initialSyncFinished) binding.fab.show() else binding.fab.hide()
    }

    fun showHideSourceSelection(show: Boolean) {
        sourcesFragmentShown = show
        supportFragmentManager.executePendingTransactions()
        val transaction = supportFragmentManager.beginTransaction()
        if (show) {
            if (sourcesFragment == null) {
                sourcesFragment =
                    SourceSelectionFragment.newInstance(binding.fab.findLocationOfCenterOnTheScreen())
            }
            sourcesFragment?.let {
                transaction.replace(
                    R.id.source_selection_container,
                    it,
                    sourcesFragmentTag
                ).commitAllowingStateLoss()
            }
            updateFabVisibility()
        } else {
            sourcesFragment?.let { fragment ->
                (fragment as ExitWithAnimation).run {
                    fragment.view?.let {
                        it.exitCircularReveal(
                            fragment.referencedViewPosX,
                            fragment.referencedViewPosY
                        ) {
                            updateFabVisibility()
                            transaction.remove(fragment).commitNowAllowingStateLoss()
                        }
                    } ?: transaction.remove(fragment).commitNowAllowingStateLoss()
                }
            }
        }
    }

    fun navigateToSimpleArticles(sourceUrl: String) {
        replaceFragment(SimpleArticlesFragment.newInstance(sourceUrl))
    }

    fun navigateToSourceDetail(sourceUrl: String) {
        replaceFragment(SourceDetailFragment.newInstance(sourceUrl))
    }

    fun navigateToSettings() {
        replaceFragment(SettingsFragment.newInstance())
    }

    fun navigateToAbout() {
        replaceFragment(AboutFragment.newInstance())
    }

    override fun onFragmentReplaced() {
        showHideSourceSelection(false)
    }
}
