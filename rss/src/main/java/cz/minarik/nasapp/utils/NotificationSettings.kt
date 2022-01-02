package cz.minarik.nasapp.utils

import cz.minarik.nasapp.ui.settings.NotificationKeyword

data class NotificationSettings(
    var notifyAll: Boolean = true,
    var keyWords: MutableList<NotificationKeyword> = mutableListOf(),
)