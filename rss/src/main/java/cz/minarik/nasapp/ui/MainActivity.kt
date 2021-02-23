package cz.minarik.nasapp.ui

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import cz.minarik.base.ui.base.BaseFragment
import cz.minarik.nasapp.R
import cz.minarik.nasapp.ui.articles.ArticlesFragment
import cz.minarik.nasapp.ui.articles.ArticlesViewModel
import cz.minarik.nasapp.ui.articles.detail.ArticleDetailFragment
import cz.minarik.nasapp.ui.articles.simple.SimpleArticlesFragment
import cz.minarik.nasapp.ui.custom.ArticleDTO
import cz.minarik.nasapp.ui.sources.detail.SourceDetailFragment
import cz.minarik.nasapp.ui.sources.selection.SourceSelectionFragment
import cz.minarik.nasapp.utils.ExitWithAnimation
import cz.minarik.nasapp.utils.exitCircularReveal
import cz.minarik.nasapp.utils.findLocationOfCenterOnTheScreen
import kotlinx.android.synthetic.main.activity_main.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity() {

    companion object {
        const val fragmentTag = "fragmentTag"
        const val sourcesFragmentTag = "sourcesFragment"
    }

    val viewModel by viewModel<ArticlesViewModel>()
    private var sourcesFragment: Fragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        viewModel //initialization
        if (savedInstanceState == null) {
            replaceFragment(ArticlesFragment())
        }
        initViews()
    }

    private fun initViews() {
        fab.setOnClickListener {
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
        if (supportFragmentManager.backStackEntryCount == 1) fab.show()
    }

    private fun replaceFragment(
        fragment: BaseFragment,
        vararg sharedElements: Pair<View, String>,
    ) {
        if (supportFragmentManager.executePendingTransactions()) return

        val currentFragment = supportFragmentManager.primaryNavigationFragment
        if (fragment.javaClass == currentFragment?.javaClass) return

        val transaction = supportFragmentManager.beginTransaction().apply {
//            if (sharedElements.isEmpty()) {
            setCustomAnimations(
                R.anim.slide_in_right,
                R.anim.slide_out_left,
                android.R.anim.slide_in_left,
                android.R.anim.slide_out_right
            )
//            }
            setReorderingAllowed(true)
        }

        currentFragment?.let {
            transaction.hide(it)
        }

        transaction.add(R.id.nav_host_container, fragment, fragmentTag)

        //todo sharedElements not working properly with recycler
//        for (sharedElement in sharedElements) {
//            transaction.addSharedElement(sharedElement.first, sharedElement.second)
//        }

        transaction.apply {
            setPrimaryNavigationFragment(fragment)
            addToBackStack(null)
            commitAllowingStateLoss()
        }

        showHideSourceSelection(false)
        if (fragment is ArticlesFragment) {
            fab.show()
        } else {
            fab.hide()
        }
    }

    fun sourcesVisible(): Boolean {
        return supportFragmentManager.findFragmentByTag(sourcesFragmentTag)?.isVisible ?: false
    }

    fun showHideSourceSelection(show: Boolean) {
        supportFragmentManager.executePendingTransactions()
        val transaction = supportFragmentManager.beginTransaction()
        if (show) {
            if (sourcesFragment == null) {
                sourcesFragment =
                    SourceSelectionFragment.newInstance(fab.findLocationOfCenterOnTheScreen())
            }
            sourcesFragment?.let {
                transaction.replace(
                    R.id.source_selection_container,
                    it,
                    sourcesFragmentTag
                ).commitAllowingStateLoss()
            }
        } else {
            sourcesFragment?.let {
                (it as ExitWithAnimation).run {
                    it.view?.exitCircularReveal(it.referencedViewPosX, it.referencedViewPosY) {
                        transaction.remove(it).commitNowAllowingStateLoss()
                    }
                }
            }
        }
        if (show) {
            fab.hide()
        } else {
            fab.show()
        }
    }

    fun navigateToArticleDetail(
        articleDTO: ArticleDTO,
        vararg sharedElements: Pair<View, String>,
    ) {
        replaceFragment(ArticleDetailFragment.newInstance(articleDTO), *sharedElements)
    }

    fun navigateToSimpleArticles(sourceUrl: String) {
        replaceFragment(SimpleArticlesFragment.newInstance(sourceUrl))
    }

    fun navigateToSourceDetail(sourceUrl: String) {
        replaceFragment(SourceDetailFragment.newInstance(sourceUrl))
    }
}
