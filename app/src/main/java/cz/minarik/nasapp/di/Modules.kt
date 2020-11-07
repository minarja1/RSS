package cz.minarik.nasapp.di

import android.content.Context
import androidx.room.Room
import cz.minarik.nasapp.data.db.UniverseDatabase
import cz.minarik.nasapp.data.db.repository.ArticlesRepository
import cz.minarik.nasapp.data.db.repository.RSSSourceRepository
import cz.minarik.nasapp.ui.articles.ArticlesFragmentViewModel
import cz.minarik.nasapp.ui.articles.detail.ArticleDetailFragmentViewModel
import cz.minarik.nasapp.ui.articles.simple.SimpleArticlesFragmentViewModel
import cz.minarik.nasapp.ui.articles.source_selection.SourceSelectionViewModel
import cz.minarik.nasapp.utils.UniversePrefManager
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module


val appModule = module {

    viewModel {
        ArticlesFragmentViewModel(
            androidContext(),
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
        )
    }

    viewModel { (articleUrl: String?, context: Context) ->
        ArticleDetailFragmentViewModel(
            articleUrl,
            context,
        )
    }

    single {
        SourceSelectionViewModel(
            androidContext(),
            get(),
            get(),
        )
    }

    single {
        UniversePrefManager(androidContext())
    }
    single {
        RSSSourceRepository(androidContext(), get())
    }
    single {
        ArticlesRepository(get(), get())
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
    single { get<UniverseDatabase>().starredArticleDao() }

}

val allModules = listOf(appModule, dbModule)