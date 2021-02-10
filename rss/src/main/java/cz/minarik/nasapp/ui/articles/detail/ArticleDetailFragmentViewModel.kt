package cz.minarik.nasapp.ui.articles.detail

import android.content.Context
import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import com.chimbori.crux.articles.Article
import com.chimbori.crux.articles.ArticleExtractor
import cz.minarik.base.di.base.BaseViewModel
import cz.minarik.nasapp.data.db.dao.ArticleDao
import cz.minarik.nasapp.data.db.dao.ReadArticleDao
import cz.minarik.nasapp.ui.custom.ArticleDTO
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.jsoup.Jsoup


class ArticleDetailFragmentViewModel(
    private val articleUrl: String?,
    private val context: Context,
    private val readArticleDao: ReadArticleDao,
    private val articleDao: ArticleDao,
) : BaseViewModel() {

    val articleLiveData: MutableLiveData<Article?> = MutableLiveData()
    val articleStarredLiveData: MutableLiveData<Boolean> = MutableLiveData()
    var webViewState: Bundle = Bundle()

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

    fun markArticleAsStarred(article: ArticleDTO) {
        launch(defaultState = null) {
            val starred = !article.starred
            article.starred = starred
            article.guid?.let { guid ->
                article.date?.let { date ->
                    articleDao.getByGuidAndDate(guid, article.date)?.run {
                        this.starred = starred
                        articleDao.update(this)
                    }
                }
            }
            articleStarredLiveData.postValue(true)
        }
    }
}