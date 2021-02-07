package cz.minarik.nasapp.data.domain.exception

import androidx.annotation.Keep
import cz.minarik.nasapp.R
import cz.minarik.nasapp.RSSApp
import java.io.IOException

@Keep
class NoConnectionException : IOException() {
    override val message: String?
        get() = RSSApp.applicationContext.getString(R.string.no_internet_connection)
}