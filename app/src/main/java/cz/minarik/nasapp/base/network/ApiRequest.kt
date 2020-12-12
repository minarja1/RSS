package cz.minarik.nasapp.base.network

import android.annotation.SuppressLint
import cz.akcenaprani.eticket.v3.base.network.exception.ApiException
import cz.minarik.nasapp.base.network.exception.GeneralApiException
import cz.minarik.nasapp.base.network.exception.TimeoutConnectionException
import kotlinx.coroutines.CoroutineExceptionHandler
import retrofit2.Response
import timber.log.Timber
import java.io.IOException
import java.net.SocketTimeoutException

class ApiRequest {
    val TAG = ApiRequest::class.java.simpleName

    companion object {
        suspend fun <T> getResult(call: suspend () -> Response<T>): ApiResponse<T> {
            try {
                val response = call()

                return if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        ApiResponse.success(body)
                    } else {
                        ApiResponse.error(
                            GeneralApiException()
                        )
                    }
                } else {
                    error(" ${response.code()} ${response.message()}")
                }
            } catch (e: Exception) {
                Timber.e(e)
                return ApiResponse.error(
                    when (e) {
                        is IOException -> when (e) {
                            is SocketTimeoutException -> TimeoutConnectionException()
                            else -> GeneralApiException()
                        }
                        else -> GeneralApiException()
                    }
                )
            }
        }
    }

    fun <T> error(message: String): ApiResponse<T> {
        Timber.e(message)
        return ApiResponse.error(
            ApiException(message)
        )
    }

    @SuppressLint("unused")
    fun getJobErrorHandler() = CoroutineExceptionHandler { _, e ->
        postError(e.message ?: e.toString())
    }

    private fun postError(message: String) {
        Timber.e(message)
    }
}