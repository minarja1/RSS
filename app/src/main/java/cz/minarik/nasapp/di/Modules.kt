package cz.minarik.nasapp.di

import androidx.room.Room
import cz.minarik.nasapp.data.db.UniverseDatabase
import cz.minarik.nasapp.ui.news.NewsFragmentViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module


val appModule = module {

    //todo prefManager

    viewModel {
        NewsFragmentViewModel(
            androidContext(),
            get()
        )
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
}

val allModules = listOf(appModule, dbModule)