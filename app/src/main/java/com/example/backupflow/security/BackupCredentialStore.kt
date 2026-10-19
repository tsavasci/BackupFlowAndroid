package com.example.backupflow.security

import android.content.Context
import com.google.crypto.tink.Aead
import com.google.crypto.tink.KeyTemplates
import com.google.crypto.tink.aead.AeadConfig
import com.google.crypto.tink.integration.android.AndroidKeysetManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import java.util.UUID

@Singleton
class BackupCredentialStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val preferences = context.getSharedPreferences("backup_credentials", Context.MODE_PRIVATE)
    private var aead: Aead? = null

    init {
        AeadConfig.register()
        val keysetManager = AndroidKeysetManager.Builder()
            .withSharedPref(context, "tink_keyset", "tink_prefs")
            .withKeyTemplate(KeyTemplates.get("AES256_GCM"))
            .withMasterKeyUri("android-keystore://tink_master_key")
            .build()
        aead = keysetManager.keysetHandle.getPrimitive(Aead::class.java)
    }

    fun savePassword(keyRef: String, password: String) {
        val encrypted = aead?.encrypt(password.toByteArray(Charsets.UTF_8), keyRef.toByteArray(Charsets.UTF_8))
        encrypted?.let {
            val encoded = android.util.Base64.encodeToString(it, android.util.Base64.DEFAULT)
            preferences.edit().putString(keyRef, encoded).apply()
        }
    }

    fun getPassword(keyRef: String): String? {
        val encoded = preferences.getString(keyRef, null) ?: return null
        return try {
            val encrypted = android.util.Base64.decode(encoded, android.util.Base64.DEFAULT)
            val decrypted = aead?.decrypt(encrypted, keyRef.toByteArray(Charsets.UTF_8))
            decrypted?.let { String(it, Charsets.UTF_8) }
        } catch (e: Exception) {
            null
        }
    }

    fun deletePassword(keyRef: String) {
        preferences.edit().remove(keyRef).apply()
    }
}