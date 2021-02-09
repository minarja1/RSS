package cz.minarik.nasapp.di

import androidx.room.Room
import cz.minarik.nasapp.data.db.RSSDatabase
import cz.minarik.nasapp.data.db.repository.ArticlesRepository
import cz.minarik.nasapp.data.db.repository.RSSSourceRepository
import cz.minarik.nasapp.data.domain.RSSSource
import cz.minarik.nasapp.data.network.RssApiService
import cz.minarik.nasapp.ui.articles.ArticlesFragmentViewModel
import cz.minarik.nasapp.ui.articles.detail.ArticleDetailFragmentViewModel
import cz.minarik.nasapp.ui.articles.simple.SimpleArticlesFragmentViewModel
import cz.minarik.nasapp.ui.sources.detail.SourceDetailViewModel
import cz.minarik.nasapp.ui.sources.manage.source_detail.SourceListDetailViewModel
import cz.minarik.nasapp.ui.sources.selection.SourcesViewModel
import cz.minarik.nasapp.utils.RSSPrefManager
import me.toptas.rssconverter.RssConverterFactory
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit


val appModule = module {

    viewModel {
        ArticlesFragmentViewModel(
            androidContext(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
        )
    }
    viewModel { (sourceUrl: String) ->
        SimpleArticlesFragmentViewModel(
            sourceUrl,
            androidContext(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
        )
    }

    viewModel { (articleUrl: String?) ->
        ArticleDetailFragmentViewModel(
            articleUrl,
            androidContext(),
            get(),
            get(),
        )
    }

    single {
        SourcesViewModel(
            androidContext(),
            get(),
            get(),
            get(),
        )
    }

    viewModel { (source: RSSSource) ->
        SourceListDetailViewModel(source)
    }

    viewModel { (sourceUrl: String) ->
        SourceDetailViewModel(sourceUrl, get())
    }

    single {
        RSSPrefManager(androidContext())
    }
    single {
        RSSSourceRepository(androidContext(), get(), get(), get())
    }
    single {
        ArticlesRepository(get(), get())
    }

    single {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://github.com")
            .addConverterFactory(RssConverterFactory.create())
            .build()

        RssApiService(retrofit)
    }
}


val dbModule = module {

    // db
    single {
        Room
            .databaseBuilder(
                androidApplication(),
                RSSDatabase::class.java,
                RSSDatabase.Name
            )
            //.addMigrations(Migration_4_to_5)
            .build()
    }

    // Dao
    single { get<RSSDatabase>().readArticleDao() }
    single { get<RSSDatabase>().rssSourceDao() }
    single { get<RSSDatabase>().rssSourceListDao() }
    single { get<RSSDatabase>().starredArticleDao() }

}

val allModules = listOf(appModule, dbModule)