package cz.minarik.nasapp.ui.articles.detail

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.chimbori.crux.articles.Article
import com.chimbori.crux.articles.ArticleExtractor
import cz.minarik.base.di.base.BaseViewModel
import cz.minarik.nasapp.data.db.dao.ReadArticleDao
import cz.minarik.nasapp.data.db.dao.StarredArticleDao
import cz.minarik.nasapp.data.db.entity.ReadArticleEntity
import cz.minarik.nasapp.data.db.entity.StarredArticleEntity
import cz.minarik.nasapp.ui.custom.ArticleDTO
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.jsoup.Jsoup


class ArticleDetailFragmentViewModel(
    private val articleUrl: String?,
    private val context: Context,
    private val readArticleDao: ReadArticleDao,
    private val starredArticleDao: StarredArticleDao,
) : BaseViewModel() {

    val articleLiveData: MutableLiveData<Article?> = MutableLiveData()
    val articleStarredLiveData: MutableLiveData<Boolean> = MutableLiveData()

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


    fun markArticleAsRead(article: ArticleDTO) {
        launch(defaultState = null) {
            val read = !article.read
            article.read = read
            article.guid?.let { guid ->
                article.date?.let { date ->
                    val entity = ReadArticleEntity(guid, date)
                    if (read) {
                        readArticleDao.insert(entity)
                    } else {
                        readArticleDao.delete(entity)
                    }
                }
            }
        }
    }

    fun markArticleAsStarred(article: ArticleDTO) {
        launch(defaultState = null) {
            val starred = !article.starred
            article.starred = starred
            article.guid?.let {
                val entity = StarredArticleEntity.fromModel(article)

                if (starred) {
                    starredArticleDao.insert(entity)
                } else {
                    starredArticleDao.delete(entity)
                }
            }
            articleStarredLiveData.postValue(true)
        }
    }


}