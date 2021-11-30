package com.example.datastoreexample

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

class DataStoreManager(context: Context) {
  private val dataStore = context.dataStore

  companion object {
    val IS_DARK_MODE = booleanPreferencesKey("dark_mode")
  }

  suspend fun setUiMode(uiMode: UiMode) {
    dataStore.edit {
      it[IS_DARK_MODE] = when (uiMode) {
        UiMode.DARK -> true
        else -> false
      }
    }
  }

  val uiModeFlow: Flow<UiMode> = dataStore.data.catch {
    if (it is IOException) {
      it.printStackTrace()
      emit(emptyPreferences())
    } else {
      throw it
    }
  }.map {
    when (it[IS_DARK_MODE] ?: false) {
      true -> UiMode.DARK
      else -> UiMode.LIGHT
    }
  }
}