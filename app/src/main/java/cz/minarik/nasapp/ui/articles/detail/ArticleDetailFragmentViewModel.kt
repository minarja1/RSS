package cz.minarik.nasapp.ui.articles.detail

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.chimbori.crux.articles.Article
import com.chimbori.crux.articles.ArticleExtractor
import cz.minarik.base.common.extensions.isInternetAvailable
import cz.minarik.base.data.NetworkState
import cz.minarik.base.di.base.BaseViewModel
import cz.minarik.nasapp.data.model.exception.NoConnectionException
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.jsoup.Jsoup


class ArticleDetailFragmentViewModel(
    private val articleUrl: String?,
    private val context: Context,
) : BaseViewModel() {

    val articleLiveData: MutableLiveData<Article?> = MutableLiveData()

    init {
        loadArticleDetail()
    }

    fun loadArticleDetail() {
        launch {
            articleUrl?.let {
                val parse = articleUrl.toHttpUrl()
                val doc = Jsoup.connect(articleUrl).get()
                val article = ArticleExtractor(parse, doc)
                    .extractMetadata()
                    .extractContent()
                    .article

                articleLiveData.postValue(article)
            }
        }
    }
}