package cz.minarik.nasapp.data.domain

import android.content.Context
import cz.minarik.nasapp.R

enum class DbCleanupItem(val titleRes: Int, val daysValue: Int) {
    TwoWeeks(R.string.two_weeks, -14),
    OneMonth(R.string.one_month, -30),
    TwoMonths(R.string.two_months, -60),
    ThreeMonths(R.string.three_months, -90);

    companion object {
        fun fromDaysValue(days: Int) = values().firstOrNull { it.daysValue == days } ?: OneMonth
        fun fromString(string: String, context: Context) =
            values().firstOrNull() { it.toString(context) == string } ?: OneMonth

        fun getAsArray(context: Context): Array<String> {
            val list = mutableListOf<String>()
            for (themeSetting in values()) {
                list.add(themeSetting.toString(context))
            }
            return list.toTypedArray()
        }
    }

    fun toString(context: Context): String {
        return context.getString(titleRes)
    }
}