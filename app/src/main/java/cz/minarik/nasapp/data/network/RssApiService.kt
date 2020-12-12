package cz.minarik.nasapp.data.network

import me.toptas.rssconverter.RssFeed
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Url

interface RssApiService {
    companion object {
        operator fun invoke(retrofit: Retrofit): RssApiService {
            return retrofit
                .create(RssApiService::class.java)
        }
    }

    @GET
    suspend fun getRss(@Url url: String): Response<RssFeed>
}