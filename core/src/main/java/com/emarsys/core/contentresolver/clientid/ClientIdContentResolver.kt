package com.emarsys.core.contentresolver.clientid

import com.emarsys.core.Mockable
import com.emarsys.core.contentresolver.EmarsysContentResolver
import com.emarsys.core.crypto.ClientIdentificationCrypto
import com.emarsys.core.database.DatabaseContract

@Mockable
class ClientIdContentResolver(
    private val emarsysContentResolver: EmarsysContentResolver,
    private val crypto: ClientIdentificationCrypto,
    private val sharedPackageNames: List<String>?
) {

    fun resolveClientId(): String? {
        var index = 0
        var sharedClientId: String? = null
        var encryptedClientId: String? = null
        if (sharedPackageNames != null) {
            while (encryptedClientId == null && index < sharedPackageNames.size) {
                val cursor = emarsysContentResolver.query(
                    DatabaseContract.getClientIdProviderUri(sharedPackageNames[index]),
                    arrayOf(
                        DatabaseContract.CLIENT_IDENTIFICATION_COLUMN_NAME_ENCRYPTED_CLIENT_ID,
                        DatabaseContract.CLIENT_IDENTIFICATION_COLUMN_NAME_SALT,
                        DatabaseContract.CLIENT_IDENTIFICATION_COLUMN_NAME_IV
                    ),
                    null, null, null
                )
                if (cursor != null && cursor.moveToFirst()) {
                    encryptedClientId =
                        cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.CLIENT_IDENTIFICATION_COLUMN_NAME_ENCRYPTED_CLIENT_ID))
                    val salt =
                        cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.CLIENT_IDENTIFICATION_COLUMN_NAME_SALT))
                    val iv =
                        cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.CLIENT_IDENTIFICATION_COLUMN_NAME_IV))
                    sharedClientId = crypto.decrypt(encryptedClientId, salt, iv)
                    cursor.close()
                }
                index++
            }
        }
        return sharedClientId
    }
}