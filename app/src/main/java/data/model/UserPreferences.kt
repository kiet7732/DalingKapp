import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

private val Context.dataStore by preferencesDataStore(name = "user_prefs")
private val AUTH_TOKEN_KEY = stringPreferencesKey("auth_token")

object UserPreferences {
    private val USER_ID_KEY = stringPreferencesKey("user_id")

    fun saveUserId(context: Context, userId: String) {
        runBlocking {
            context.dataStore.edit { preferences ->
                preferences[USER_ID_KEY] = userId
            }
        }
    }

    fun getUserId(context: Context): String? {
        return runBlocking {
            context.dataStore.data.map { preferences ->
                preferences[USER_ID_KEY]
            }.first()
        }
    }

    // Xóa userId từ DataStore
    suspend fun clearUserId(context: Context) {
        context.dataStore.edit { preferences ->
            preferences.remove(USER_ID_KEY)
        }
    }

    suspend fun clearAuthToken(context: Context) {
        context.dataStore.edit { preferences ->
            preferences.remove(AUTH_TOKEN_KEY)
        }
    }

}
