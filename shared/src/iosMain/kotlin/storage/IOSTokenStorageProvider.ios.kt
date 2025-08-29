package storage

import com.russhwolf.settings.Settings
import com.russhwolf.settings.NSUserDefaultsSettings
import platform.Foundation.NSUserDefaults

class IOSTokenStorageProvider : TokenStorageProvider {
    override fun getTokenStorage(): TokenStorage {
        val nsUserDefaults = NSUserDefaults.standardUserDefaults
        val settings: Settings = NSUserDefaultsSettings(nsUserDefaults)
        return TokenStorage(settings)
    }

}