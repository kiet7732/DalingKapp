package data.model

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Locale

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

object LanguagePreferences {
    private val LANGUAGE_KEY = stringPreferencesKey("language")

    suspend fun saveLanguage(context: Context, language: String) {
        try {
            context.dataStore.edit { preferences ->
                preferences[LANGUAGE_KEY] = language
            }
        } catch (e: Exception) {
            Log.e("LanguagePreferences", "Error saving language: ${e.message}")
        }
    }

    fun getLanguage(context: Context): Flow<String> {
        return context.dataStore.data.map { preferences ->
            val storedLanguage = preferences[LANGUAGE_KEY]
            if (storedLanguage != null) {
                storedLanguage // Trả về ngôn ngữ đã lưu trong DataStore
            } else {
                // Lấy ngôn ngữ hệ thống nếu không có giá trị trong DataStore
                val systemLanguage = Locale.getDefault().language
                // Chỉ lấy mã ngôn ngữ (ví dụ: "vi", "en"), bỏ phần mã vùng (nếu có)
                if (systemLanguage in listOf("vi", "en")) systemLanguage else "vi"
            }
        }
    }
}