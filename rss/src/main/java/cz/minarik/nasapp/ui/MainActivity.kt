package cz.minarik.nasapp.ui

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import cz.minarik.base.ui.base.BaseFragment
import cz.minarik.nasapp.R
import cz.minarik.nasapp.ui.articles.ArticlesFragment
import cz.minarik.nasapp.ui.articles.detail.ArticleDetailFragment
import cz.minarik.nasapp.ui.articles.simple.SimpleArticlesFragment
import cz.minarik.nasapp.ui.custom.ArticleDTO
import cz.minarik.nasapp.ui.sources.detail.SourceDetailFragment

class MainActivity : AppCompatActivity() {

    companion object {
        const val fragmentTag = "fragmentTag"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (savedInstanceState == null) {
            replaceFragment(ArticlesFragment())
        }
    }

    fun getCurrentFragment(): Fragment? {
        return supportFragmentManager.primaryNavigationFragment
    }

    fun goBack() {
        supportFragmentManager.popBackStack()
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
            commit()
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
