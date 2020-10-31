package cz.minarik.nasapp.di

import androidx.room.Room
import cz.minarik.nasapp.data.db.UniverseDatabase
import cz.minarik.nasapp.data.db.repository.RSSSourceRepository
import cz.minarik.nasapp.ui.articles.ArticlesFragmentViewModel
import cz.minarik.nasapp.ui.articles.source_selection.SourceSelectionViewModel
import cz.minarik.nasapp.utils.UniversePrefManager
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module


val appModule = module {

    //todo prefManager

    viewModel {
        ArticlesFragmentViewModel(
            androidContext(),
            get(),
            get(),
            get(),
            get(),
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