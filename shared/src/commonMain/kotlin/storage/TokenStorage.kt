package storage

import com.russhwolf.settings.Settings
import okio.ByteString
import okio.ByteString.Companion.decodeBase64
import okio.ByteString.Companion.encodeUtf8

import kotlin.random.Random

open class TokenStorage(settings: Settings) {
    private val tokenKey = "auth_token"
    private val settings: Settings = settings
    private val encryptionKeyKey = "encryption_key"
    private val refreshTokenKey = "refresh_token"
    private val userIdKey = "user_id"
    private val userRoleKey = "user_role"
    private val userClubIdKey = "user_club_id"

    private fun getOrCreateEncryptionKey(): ByteString {
        val existingKey = settings.getStringOrNull(encryptionKeyKey)
        return if (existingKey != null) {
            existingKey.decodeBase64()!!
        } else {
            val newKey = ByteString.of(*Random.nextBytes(32)) // Generar una clave de 32 bytes
            settings.putString(encryptionKeyKey, newKey.base64())
            newKey
        }
    }

    fun saveTokensAndData(accessToken: String, refreshToken: String, userId: Int, role: String, userClubId: Int) {
        val encryptionKey = getOrCreateEncryptionKey()

        val encryptedToken = xorEncrypt(accessToken.encodeUtf8(), encryptionKey)
        val encryptedRefreshToken = xorEncrypt(refreshToken.encodeUtf8(), encryptionKey)

        settings.putString(tokenKey, encryptedToken.base64())
        settings.putString(refreshTokenKey, encryptedRefreshToken.base64())

        settings.putInt(userIdKey, userId)
        settings.putString(userRoleKey, role)
        settings.putInt(userClubIdKey, userClubId)
    }

    fun saveTokens(accessToken: String, refreshToken: String) {
        val encryptionKey = getOrCreateEncryptionKey()

        val encryptedToken = xorEncrypt(accessToken.encodeUtf8(), encryptionKey)
        val encryptedRefreshToken = xorEncrypt(refreshToken.encodeUtf8(), encryptionKey)

        settings.putString(tokenKey, encryptedToken.base64())
        settings.putString(refreshTokenKey, encryptedRefreshToken.base64())
    }

    fun getToken(): String? {
        val encryptionKey = getOrCreateEncryptionKey()
        val encryptedToken = settings.getStringOrNull(tokenKey) ?: return null
        return xorDecrypt(encryptedToken.decodeBase64()!!, encryptionKey).utf8()
    }

    fun getRefreshToken(): String? {
        val encryptionKey = getOrCreateEncryptionKey()
        val encryptedToken = settings.getStringOrNull(refreshTokenKey) ?: return null
        return xorDecrypt(encryptedToken.decodeBase64()!!, encryptionKey).utf8()
    }

    fun clearTokens() {
        settings.remove(tokenKey)
        settings.remove(refreshTokenKey)
        settings.remove(userRoleKey)
        settings.remove(userIdKey)
        settings.remove(userClubIdKey)
    }

    fun getUserId(): Int? {
        return settings.getIntOrNull(userIdKey)
    }

    fun getUserRole(): String? {
        return settings.getStringOrNull(userRoleKey)
    }

    fun getClubId(): Int? {
        return settings.getIntOrNull(userClubIdKey)
    }

    private fun xorEncrypt(data: ByteString, key: ByteString): ByteString {
        val result = ByteArray(data.size)
        for (i in 0 until data.size) {
            result[i] = (data[i].toInt() xor key[i % key.size].toInt()).toByte()
        }
        return ByteString.of(*result)
    }

    private fun xorDecrypt(data: ByteString, key: ByteString): ByteString {
        return xorEncrypt(data, key) // La operación XOR es reversible
    }
}