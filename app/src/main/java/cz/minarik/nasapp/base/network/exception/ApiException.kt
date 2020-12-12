package cz.akcenaprani.eticket.v3.base.network.exception

import androidx.annotation.Keep

@Keep
class ApiException(val text: String?) :
    Exception() {
    override val message: String?
        get() = text
}