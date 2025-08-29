package storage

import android.content.Context
import com.russhwolf.settings.SharedPreferencesSettings

class AndroidTokenStorageProvider(private val context: Context) : TokenStorageProvider {
    override fun getTokenStorage(): TokenStorage {
        val settings = SharedPreferencesSettings(context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE))
        return TokenStorage(settings)
    }

}