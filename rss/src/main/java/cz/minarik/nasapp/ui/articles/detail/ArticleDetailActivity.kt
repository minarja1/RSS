package cz.minarik.nasapp.ui.articles.detail

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import cz.minarik.nasapp.R
import cz.minarik.nasapp.ui.custom.ArticleDTO
import cz.minarik.nasapp.utils.Constants

class ArticleDetailActivity : AppCompatActivity() {

    companion object {
        private const val FRAGMENT_TAG = "FRAGMENT_TAG"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_article_detail)
        postponeEnterTransition()
        if (savedInstanceState == null) {
            switchFragment(
                ArticleDetailFragment.newInstance(intent.extras?.getSerializable(Constants.argArticleDTO) as ArticleDTO)
            )
        }
    }

    private fun switchFragment(fragment: Fragment, tag: String? = null) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, fragment, tag ?: FRAGMENT_TAG)
            .commitNowAllowingStateLoss()
    }

}