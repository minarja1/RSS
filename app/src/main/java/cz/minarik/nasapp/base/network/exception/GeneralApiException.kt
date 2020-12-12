package cz.minarik.nasapp.base.network.exception

import androidx.annotation.Keep
import cz.minarik.nasapp.R
import cz.minarik.nasapp.UniverseApp

@Keep
class GeneralApiException : Exception() {

    override val message: String?
        get() = UniverseApp.applicationContext.getString(R.string.common_base_error)
}