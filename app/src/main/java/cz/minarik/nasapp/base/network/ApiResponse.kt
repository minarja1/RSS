package cz.minarik.nasapp.base.network

import cz.minarik.nasapp.base.FailedWithError
import cz.minarik.nasapp.base.Loading
import cz.minarik.nasapp.base.Success
import cz.minarik.nasapp.base.ViewModelState

data class ApiResponse<out T>(
    var status: Status,
    val data: T? = null,
    val error: Exception? = null,
) {
    enum class Status {
        ERROR,
        SUCCESS,
        LOADING,
    }

    fun toViewModelState(): ViewModelState {
        return when (status) {
            Status.ERROR -> FailedWithError(error)
            Status.SUCCESS -> Success
            Status.LOADING -> Loading
        }
    }

    companion object {
        fun <T> loading(): ApiResponse<T> {
            return ApiResponse(
                Status.LOADING,
            )
        }

        fun <T> success(data: T?): ApiResponse<T> {
            return ApiResponse(
                Status.SUCCESS,
                data,
            )
        }

        fun <T> error(data: T? = null, message: String? = null): ApiResponse<T> {
            return ApiResponse(
                Status.ERROR,
                error = ApiException(message),
                data = data
            )
        }

        fun <T> error(error: Exception? = null): ApiResponse<T> {
            return ApiResponse(
                Status.ERROR,
                error = error,
            )
        }

        fun <T> error(error: Throwable? = null): ApiResponse<T> {
            val ex: java.lang.Exception = java.lang.Exception(error)
            return ApiResponse(
                Status.ERROR,
                error = ex,
            )
        }

        fun <T> error(message: String?): ApiResponse<T> {
            return ApiResponse(
                Status.ERROR,
                error = ApiException(message),
            )
        }
    }
}