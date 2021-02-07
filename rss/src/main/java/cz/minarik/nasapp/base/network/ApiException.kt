package cz.minarik.nasapp.base.network

import androidx.annotation.Keep

@Keep
class ApiException(val text: String?) :
    Exception() {
    override val message: String?
        get() = text
}