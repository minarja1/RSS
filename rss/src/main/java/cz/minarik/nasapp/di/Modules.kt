package cz.minarik.nasapp.di

import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import cz.minarik.nasapp.data.db.RSSDatabase
import cz.minarik.nasapp.data.db.repository.ArticlesRepository
import cz.minarik.nasapp.data.db.repository.RSSSourceRepository
import cz.minarik.nasapp.data.network.RssApiService
import cz.minarik.nasapp.ui.articles.ArticlesViewModel
import cz.minarik.nasapp.ui.settings.sources.AddSourcesViewModel
import cz.minarik.nasapp.ui.sources.detail.SourceDetailViewModel
import cz.minarik.nasapp.ui.sources.selection.SourcesViewModel
import me.toptas.rssconverter.RssConverterFactory
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit


val appModule = module {

    single {
        ArticlesViewModel(
            get(),
            get(),
            get(),
        )
    }

    viewModel {
        SourcesViewModel(
            get(),
            get(),
        )
    }

    viewModel {
        AddSourcesViewModel(
            get(),
        )
    }

    viewModel { (sourceUrl: String) ->
        SourceDetailViewModel(sourceUrl, get())
    }

    single {
        RSSSourceRepository(androidContext(), get())
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
            .addMigrations(MIGRATION_1_2)
            .addMigrations(MIGRATION_2_3)
            .build()
    }

    // Dao
    single { get<RSSDatabase>().rssSourceDao() }
    single { get<RSSDatabase>().starredArticleDao() }
}

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE RSSSourceEntity ADD COLUMN isAtom INTEGER DEFAULT 0 NOT NULL")
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE RSSSourceEntity ADD COLUMN isNotificationsEnabled INTEGER DEFAULT 0 NOT NULL")
    }
}

val allModules = listOf(appModule, dbModule)