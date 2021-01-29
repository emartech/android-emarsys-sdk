package com.emarsys.core.storage

import android.annotation.TargetApi
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.security.KeyPairGeneratorSpec
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import kotlin.math.abs
import androidx.annotation.RequiresApi
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import androidx.security.crypto.MasterKeys
import com.emarsys.core.util.AndroidVersionUtils
import com.emarsys.core.util.log.Logger
import com.emarsys.core.util.log.entry.StatusLog
import java.math.BigInteger
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.util.*
import javax.security.auth.x500.X500Principal

class SecureSharedPreferencesProvider(context: Context, fileName: String, oldSharedPreferences: SharedPreferences) {

    private var sharedPreferences: SharedPreferences

    init {
        sharedPreferences =
                if (!AndroidVersionUtils.isBelowMarshmallow()) {
                    EncryptedSharedPreferences.create(context,
                            fileName,
                            createMasterKey(context),
                            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                    )
                } else {
                    EncryptedSharedPreferences.create(fileName,
                            createMasterKeyBelowM(context),
                            context,
                            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM)
                }

        if (oldSharedPreferences.all.isNotEmpty()) {
            oldSharedPreferences.all.asSequence().forEach { entry ->
                with(sharedPreferences.edit()) {
                    when (entry.value) {
                        is Int -> putInt(entry.key, entry.value as Int)
                        is Boolean -> putBoolean(entry.key, entry.value as Boolean)
                        is Float -> putFloat(entry.key, entry.value as Float)
                        is Long -> putLong(entry.key, entry.value as Long)
                        is String -> putString(entry.key, entry.value as String)
                        else -> Logger.error(StatusLog(this::class.java, "sharedPreferencesProvider#migrationToSecure", mapOf(entry.key to entry.value)))
                    }
                    commit()
                }
            }
            oldSharedPreferences.edit().clear().commit()
        }
    }

    private fun createMasterKeyBelowM(context: Context): String {
        val alias = MasterKey.DEFAULT_MASTER_KEY_ALIAS
        val start: Calendar = GregorianCalendar()
        val end: Calendar = GregorianCalendar()
        end.add(Calendar.YEAR, 30)

        val spec = KeyPairGeneratorSpec.Builder(context)
                .setAlias(alias)
                .setSubject(X500Principal("CN=$alias"))
                .setSerialNumber(BigInteger.valueOf(abs(alias.hashCode().toLong())))
                .setStartDate(start.time)
                .setEndDate(end.time)
                .build()

        val kpGenerator: KeyPairGenerator = KeyPairGenerator.getInstance(
                "RSA",
                "AndroidKeyStore"
        )
        kpGenerator.initialize(spec)
        val kp: KeyPair = kpGenerator.generateKeyPair()

        return kp.public.toString()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun createMasterKey(context: Context): MasterKey {
        val spec = KeyGenParameterSpec.Builder(
                MasterKey.DEFAULT_MASTER_KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(MasterKey.DEFAULT_AES_GCM_MASTER_KEY_SIZE)
                .build()
        return MasterKey.Builder(context)
                .setKeyGenParameterSpec(spec)
                .build()

    }

    fun provide(): SharedPreferences {
        return sharedPreferences
    }

}