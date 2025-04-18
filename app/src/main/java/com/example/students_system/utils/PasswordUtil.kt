package com.example.students_system.utils

// import android.os.Build // Không cần nữa
// import androidx.annotation.RequiresApi // Không cần nữa
import android.util.Base64 // <-- THAY ĐỔI IMPORT
import java.security.MessageDigest
import java.security.SecureRandom
import android.util.Log // Thêm Log để debug

object PasswordUtil {
    private const val TAG = "PasswordUtil" // Tag cho Log
    private const val SALT_LENGTH = 16
    private const val ALGORITHM = "SHA-256"
    private const val ENCODING_CHARSET = "UTF-8"

    private fun generateSalt(): ByteArray {
        // ... (giữ nguyên)
        val random = SecureRandom()
        val salt = ByteArray(SALT_LENGTH)
        random.nextBytes(salt)
        return salt
    }

    private fun hash(password: String, salt: ByteArray): ByteArray? {
        // ... (giữ nguyên, có thể thêm try-catch và log)
        return try {
            val md = MessageDigest.getInstance(ALGORITHM)
            md.update(salt)
            md.digest(password.toByteArray(charset(ENCODING_CHARSET)))
        } catch (e: Exception) {
            Log.e(TAG, "Error hashing password", e)
            null
        }
    }

    // XÓA @RequiresApi
    fun hashPassword(password: String): String? {
        val salt = generateSalt()
        val hash = hash(password, salt) ?: return null
        // SỬA CÁCH GỌI BASE64
        val saltBase64 = Base64.encodeToString(salt, Base64.NO_WRAP) // NO_WRAP để tránh thêm dòng mới
        val hashBase64 = Base64.encodeToString(hash, Base64.NO_WRAP)
        Log.d(TAG, "Generated Salt (B64): $saltBase64") // Log salt
        Log.d(TAG, "Generated Hash (B64): $hashBase64") // Log hash
        return "$saltBase64:$hashBase64"
    }

    // XÓA @RequiresApi
    fun verifyPassword(passwordAttempt: String, storedHashString: String): Boolean {
        Log.d(TAG, "Verifying password attempt against stored hash: $storedHashString")
        try {
            val parts = storedHashString.split(":")
            if (parts.size != 2) {
                Log.w(TAG, "Invalid stored hash format. Expected 2 parts, got ${parts.size}")
                return false
            }

            val saltBase64 = parts[0]
            val storedHashBase64 = parts[1]
            Log.d(TAG, "Extracted Salt (B64): $saltBase64")
            Log.d(TAG, "Extracted Hash (B64): $storedHashBase64")


            // SỬA CÁCH GỌI BASE64
            val salt = Base64.decode(saltBase64, Base64.NO_WRAP)
            val storedHash = Base64.decode(storedHashBase64, Base64.NO_WRAP)
            Log.d(TAG, "Decoded Salt length: ${salt.size}")
            Log.d(TAG, "Decoded Stored Hash length: ${storedHash.size}")


            val attemptHash = hash(passwordAttempt, salt)
            if (attemptHash == null) {
                Log.e(TAG, "Failed to hash password attempt during verification.")
                return false
            }
            Log.d(TAG, "Hashed Attempt length: ${attemptHash.size}")
            // Log thêm hash của attempt để so sánh (chỉ dùng khi debug)
            // Log.d(TAG, "Hashed Attempt (B64): ${Base64.encodeToString(attemptHash, Base64.NO_WRAP)}")

            val isEqual = MessageDigest.isEqual(storedHash, attemptHash)
            Log.d(TAG, "Verification result (isEqual): $isEqual")
            return isEqual
        } catch (e: Exception) {
            Log.e(TAG, "Error verifying password", e)
            return false
        }
    }
}