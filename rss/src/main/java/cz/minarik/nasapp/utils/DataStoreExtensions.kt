package cz.minarik.nasapp.utils

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

//todo move to base
fun DataStore<Preferences>.getBooleanData(
    key: String,
    defaultValue: Boolean = false
): Flow<Boolean> {
    return data
        .map { preferences ->
            preferences[booleanPreferencesKey(key)] ?: defaultValue
        }
}

suspend fun DataStore<Preferences>.setBooleanData(key: String, data: Boolean) {
    edit { settings ->
        settings[booleanPreferencesKey(key)] = data
    }
}

fun DataStore<Preferences>.getIntData(
    key: String,
    defaultValue: Int = 0
): Flow<Int> {
    return data
        .map { preferences ->
            preferences[intPreferencesKey(key)] ?: defaultValue
        }
}

suspend fun DataStore<Preferences>.setDoubleData(
    key: String,
    data: Double
) {
    edit { settings ->
        settings[doublePreferencesKey(key)] = data
    }
}

fun DataStore<Preferences>.getDoubleData(
    key: String,
    defaultValue: Double = 0.0
): Flow<Double> {
    return data
        .map { preferences ->
            preferences[doublePreferencesKey(key)] ?: defaultValue
        }
}

suspend fun DataStore<Preferences>.setFloatData(
    key: String,
    data: Float
) {
    edit { settings ->
        settings[floatPreferencesKey(key)] = data
    }
}

fun DataStore<Preferences>.getFloatData(
    key: String,
    defaultValue: Float = 0f
): Flow<Float> {
    return data
        .map { preferences ->
            preferences[floatPreferencesKey(key)] ?: defaultValue
        }
}

suspend fun DataStore<Preferences>.setLongData(
    key: String,
    data: Long
) {
    edit { settings ->
        settings[longPreferencesKey(key)] = data
    }
}

fun DataStore<Preferences>.getLongData(
    key: String,
    defaultValue: Long = 0
): Flow<Long> {
    return data
        .map { preferences ->
            preferences[longPreferencesKey(key)] ?: defaultValue
        }
}

suspend fun DataStore<Preferences>.setStringSetData(
    key: String,
    data: Set<String>
) {
    edit { settings ->
        settings[stringSetPreferencesKey(key)] = data
    }
}

fun DataStore<Preferences>.getStringSetData(
    key: String,
    defaultValue: Set<String> = setOf()
): Flow<Set<String>> {
    return data
        .map { preferences ->
            preferences[stringSetPreferencesKey(key)] ?: defaultValue
        }
}