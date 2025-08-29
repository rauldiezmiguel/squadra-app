package storage

interface TokenStorageProvider {
    fun getTokenStorage(): TokenStorage
}
