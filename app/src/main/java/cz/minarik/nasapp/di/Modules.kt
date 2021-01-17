package cz.minarik.nasapp.di

import androidx.room.Room
import cz.minarik.nasapp.data.db.UniverseDatabase
import cz.minarik.nasapp.data.db.repository.ArticlesRepository
import cz.minarik.nasapp.data.db.repository.RSSSourceRepository
import cz.minarik.nasapp.data.network.RssApiService
import cz.minarik.nasapp.ui.articles.ArticlesFragmentViewModel
import cz.minarik.nasapp.ui.articles.detail.ArticleDetailFragmentViewModel
import cz.minarik.nasapp.ui.articles.simple.SimpleArticlesFragmentViewModel
import cz.minarik.nasapp.ui.articles.source_selection.SourceSelectionViewModel
import cz.minarik.nasapp.utils.UniversePrefManager
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
        SourceSelectionViewModel(
            androidContext(),
            get(),
            get(),
            get(),
        )
    }

    single {
        UniversePrefManager(androidContext())
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
                UniverseDatabase::class.java,
                UniverseDatabase.Name
            )
            //.addMigrations(Migration_4_to_5)
            .build()
    }

    // Dao
    single { get<UniverseDatabase>().readArticleDao() }
    single { get<UniverseDatabase>().rssSourceDao() }
    single { get<UniverseDatabase>().rssSourceListDao() }
    single { get<UniverseDatabase>().starredArticleDao() }

}

val allModules = listOf(appModule, dbModule)