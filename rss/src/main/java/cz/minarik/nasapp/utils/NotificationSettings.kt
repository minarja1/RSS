package cz.minarik.nasapp.utils

data class NotificationSettings(
    var notifyAll: Boolean = true,
    var keyWords: List<String> = emptyList(),
)