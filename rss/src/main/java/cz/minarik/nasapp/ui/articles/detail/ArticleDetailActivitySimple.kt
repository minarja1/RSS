package cz.minarik.nasapp.ui.articles.detail

import android.os.Bundle
import androidx.fragment.app.Fragment
import cz.minarik.nasapp.R
import cz.minarik.nasapp.ui.base.BaseActivity
import cz.minarik.nasapp.data.domain.ArticleDTO
import cz.minarik.nasapp.utils.Constants

class ArticleDetailActivitySimple : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_article_detail)
        if (savedInstanceState == null) {
            switchFragment(
                ArticleDetailSimpleFragmentTest.newInstance(intent.extras?.getSerializable(Constants.argArticleDTO) as ArticleDTO)
            )
        }
    }

    private fun switchFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, fragment, fragmentTag)
            .setPrimaryNavigationFragment(fragment)
            .commitNowAllowingStateLoss()
    }
}