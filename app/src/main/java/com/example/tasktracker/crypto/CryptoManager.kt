package com.example.tasktracker.crypto

import android.util.Log
import java.nio.charset.StandardCharsets
import java.security.spec.KeySpec
import java.util.*
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec


class CryptoManager  {

    companion object{
        private const val TAG = "CryptoManager"
        private const val SALT = "ksnVJHVavghvdbyjjahdlkaaskjdbVKJjgHJbFGWNXN"
        private const val SECRET_KEY = "skdvJHsVskjOYsdU6REYRsvBNvyrYRUTIYiojKJ"
    }

    fun encryptData(strToEncrypt: String): String? {
        try {
            val iv = byteArrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)
            val ivSpec = IvParameterSpec(iv)
            val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
            val spec: KeySpec = PBEKeySpec(SECRET_KEY.toCharArray(), SALT.toByteArray(), 65536, 256)
            val tmp = factory.generateSecret(spec)
            val mSecretKeySpec = SecretKeySpec(tmp.encoded, "AES")
            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            cipher.init(Cipher.ENCRYPT_MODE, mSecretKeySpec, ivSpec)
            return Base64.getEncoder().encodeToString(
                cipher.doFinal(
                    strToEncrypt.toByteArray(
                        StandardCharsets.UTF_8
                    )
                )
            )
        } catch (e: Exception) {
            Log.d(TAG, "Error while encrypting: $e")
        }
        return null
    }

    fun decryptData(strToDecrypt: String?): String? {
        try {
            val iv = byteArrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)
            val ivSpec = IvParameterSpec(iv)
            val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
            val spec: KeySpec = PBEKeySpec(SECRET_KEY.toCharArray(), SALT.toByteArray(), 65536, 256)
            val tmp = factory.generateSecret(spec)
            val mSecretKeySpec = SecretKeySpec(tmp.encoded, "AES")
            val cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING")
            cipher.init(Cipher.DECRYPT_MODE, mSecretKeySpec, ivSpec)
            return String(cipher.doFinal(Base64.getDecoder().decode(strToDecrypt)))
        } catch (e: Exception) {
            Log.d(TAG, "Error while decrypting: $e")
        }
        return null
    }
}