package cz.minarik.nasapp.utils

import cz.minarik.nasapp.data.domain.NotificationKeyword

data class NotificationSettings(
    val notifyAll: Boolean = false,
    val keyWords: List<NotificationKeyword> = mutableListOf(),
)