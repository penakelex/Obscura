package org.penakelex.obscura.data.storage

expect class GuestKeyStorage {
    suspend fun saveGuestMasterKey(masterKey: ByteArray)
    suspend fun loadGuestMasterKey(): ByteArray?
    suspend fun saveGuestKeyset(encryptedKeysetJson: String)
    suspend fun loadGuestKeyset(): String?
    suspend fun clearGuestData()
}