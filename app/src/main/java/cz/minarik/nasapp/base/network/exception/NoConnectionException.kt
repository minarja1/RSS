package cz.minarik.nasapp.base.network.exception

import androidx.annotation.Keep
import cz.minarik.nasapp.R
import cz.minarik.nasapp.UniverseApp
import java.io.IOException

@Keep
class NoConnectionException : IOException() {
    override val message: String?
        get() = UniverseApp.applicationContext.getString(R.string.no_internet_connection)
}