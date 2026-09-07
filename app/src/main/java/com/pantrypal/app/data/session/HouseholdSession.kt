package com.pantrypal.app.data.session

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "pantrypal_prefs")

/**
 * Persists which household this phone belongs to. Both phones store the
 * same household id locally, which is what makes them read/write the same
 * Firestore data.
 */
class HouseholdSession(private val context: Context) {

    private object Keys {
        val HOUSEHOLD_ID = stringPreferencesKey("household_id")
        val HOUSEHOLD_NAME = stringPreferencesKey("household_name")
    }

    val householdId: Flow<String?> = context.dataStore.data.map { it[Keys.HOUSEHOLD_ID] }
    val householdName: Flow<String?> = context.dataStore.data.map { it[Keys.HOUSEHOLD_NAME] }

    suspend fun setHousehold(id: String, name: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.HOUSEHOLD_ID] = id
            prefs[Keys.HOUSEHOLD_NAME] = name
        }
    }

    suspend fun leaveHousehold() {
        context.dataStore.edit { it.clear() }
    }
}
