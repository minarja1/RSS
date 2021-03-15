package cz.minarik.nasapp.di

import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import cz.minarik.nasapp.data.db.RSSDatabase
import cz.minarik.nasapp.data.db.repository.ArticlesRepository
import cz.minarik.nasapp.data.db.repository.RSSSourceRepository
import cz.minarik.nasapp.data.domain.RSSSource
import cz.minarik.nasapp.data.network.RssApiService
import cz.minarik.nasapp.ui.articles.ArticlesViewModel
import cz.minarik.nasapp.ui.sources.detail.SourceDetailViewModel
import cz.minarik.nasapp.ui.sources.manage.source_detail.SourceListDetailViewModel
import cz.minarik.nasapp.ui.sources.selection.SourcesViewModel
import me.toptas.rssconverter.RssConverterFactory
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit


val appModule = module {

    viewModel {
        ArticlesViewModel(
            androidContext(),
            get(),
            get(),
            get(),
            get(),
        )
    }

    viewModel {
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
        RSSSourceRepository(androidContext(), get(), get())
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
            .build()
    }

    // Dao
    single { get<RSSDatabase>().rssSourceDao() }
    single { get<RSSDatabase>().rssSourceListDao() }
    single { get<RSSDatabase>().starredArticleDao() }
}

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE RSSSourceEntity ADD COLUMN isAtom INTEGER DEFAULT 0 NOT NULL")
    }
}

val allModules = listOf(appModule, dbModule)