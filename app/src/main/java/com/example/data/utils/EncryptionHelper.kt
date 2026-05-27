package com.example.data.utils

import android.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

object EncryptionHelper {
    private const val ALGORITHM = "AES"
    
    // A stable, static local keyset for local-encryption testing purposes
    private val keyBytes = byteArrayOf(
        0x50, 0x65, 0x72, 0x69, 0x6f, 0x64, 0x43, 0x61, 
        0x72, 0x65, 0x53, 0x65, 0x63, 0x72, 0x65, 0x74  // "PeriodCareSecret" (16 bytes)
    )
    private val keySpec = SecretKeySpec(keyBytes, ALGORITHM)

    fun encrypt(value: String): String {
        if (value.isEmpty()) return value
        return try {
            val cipher = Cipher.getInstance(ALGORITHM)
            cipher.init(Cipher.ENCRYPT_MODE, keySpec)
            val encrypted = cipher.doFinal(value.toByteArray(Charsets.UTF_8))
            Base64.encodeToString(encrypted, Base64.NO_WRAP).trim()
        } catch (e: Exception) {
            value
        }
    }

    fun decrypt(encryptedValue: String): String {
        if (encryptedValue.isEmpty()) return encryptedValue
        return try {
            val cipher = Cipher.getInstance(ALGORITHM)
            cipher.init(Cipher.DECRYPT_MODE, keySpec)
            val decodedBytes = Base64.decode(encryptedValue, Base64.NO_WRAP)
            val decrypted = cipher.doFinal(decodedBytes)
            String(decrypted, Charsets.UTF_8)
        } catch (e: Exception) {
            encryptedValue
        }
    }
}
