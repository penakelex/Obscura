package org.penakelex.obscura.data.storage

expect class AccountKeyStorage {
    suspend fun saveMasterKey(masterKey: ByteArray)
    suspend fun loadMasterKey(): ByteArray?
    suspend fun clear()
}