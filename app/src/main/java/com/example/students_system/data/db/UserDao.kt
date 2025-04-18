package com.example.students_system.data.db

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
// import android.os.Build // Không cần nữa
// import androidx.annotation.RequiresApi // Không cần nữa
import com.example.students_system.data.db.DatabaseHelper
import com.example.students_system.data.model.User
import com.example.students_system.utils.PasswordUtil // Import lớp tiện ích hash mật khẩu

class UserDao(context: Context) {

    private val dbHelper = DatabaseHelper(context)

    // --- KHÔNG CẦN CÁC HÀM PRIVATE hashPassword/verifyPassword Ở ĐÂY NỮA ---

    // XÓA @RequiresApi
    fun addUser(user: User): Long {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.COLUMN_USER_USERNAME, user.username)
            // Gọi trực tiếp PasswordUtil
            val hashedPassword = user.passwordHash // Nên lấy mật khẩu gốc từ user object
            // Quan trọng: Xử lý trường hợp hashPassword trả về null (lỗi)
            if (hashedPassword == null) {
                db.close() // Đóng DB trước khi return lỗi
                return -1L // Hoặc ném Exception
            }
            put(DatabaseHelper.COLUMN_USER_PASSWORD_HASH, hashedPassword)
            put(DatabaseHelper.COLUMN_USER_NAME, user.name)
            put(DatabaseHelper.COLUMN_USER_PHONE, user.phone)
            put(DatabaseHelper.COLUMN_USER_EMAIL, user.email)
            put(DatabaseHelper.COLUMN_USER_AVATAR_PATH, user.avatarPath)
        }
        val id = db.insert(DatabaseHelper.TABLE_USERS, null, values)
        db.close()
        return id
    }

    @SuppressLint("Range")
    fun getUserByUsername(username: String): User? {
        // ... (giữ nguyên)
        val db = dbHelper.readableDatabase
        var user: User? = null
        val cursor = db.query(
            DatabaseHelper.TABLE_USERS,
            null, // Lấy tất cả các cột
            // Cân nhắc dùng LOWER() nếu muốn username không phân biệt hoa thường
            "${DatabaseHelper.COLUMN_USER_USERNAME} = ?",
            arrayOf(username),
            null, null, null
        )

        cursor?.use { // Tự động đóng cursor
            if (it.moveToFirst()) {
                user = User(
                    id = it.getLong(it.getColumnIndex(DatabaseHelper.COLUMN_USER_ID)),
                    username = it.getString(it.getColumnIndex(DatabaseHelper.COLUMN_USER_USERNAME)),
                    passwordHash = it.getString(it.getColumnIndex(DatabaseHelper.COLUMN_USER_PASSWORD_HASH)),
                    name = it.getString(it.getColumnIndex(DatabaseHelper.COLUMN_USER_NAME)),
                    phone = it.getString(it.getColumnIndex(DatabaseHelper.COLUMN_USER_PHONE)),
                    email = it.getString(it.getColumnIndex(DatabaseHelper.COLUMN_USER_EMAIL)),
                    avatarPath = it.getString(it.getColumnIndex(DatabaseHelper.COLUMN_USER_AVATAR_PATH))
                )
            }
        }
        // KHÔNG đóng db ở đây nếu cursor vẫn đang được dùng bởi luồng khác (nếu bất đồng bộ)
        // Chỉ đóng khi chắc chắn không dùng nữa. Cursor.use tự đóng cursor.
        // db.close() // Cân nhắc lại việc đóng DB ở đây
        return user
    }

    @SuppressLint("Range")
    fun getUserById(userId: Long): User? {
        // ... (giữ nguyên)
        val db = dbHelper.readableDatabase
        var user: User? = null
        val cursor = db.query(
            DatabaseHelper.TABLE_USERS,
            null,
            "${DatabaseHelper.COLUMN_USER_ID} = ?",
            arrayOf(userId.toString()),
            null, null, null
        )

        cursor?.use {
            if (it.moveToFirst()) {
                user = User(
                    id = it.getLong(it.getColumnIndex(DatabaseHelper.COLUMN_USER_ID)),
                    username = it.getString(it.getColumnIndex(DatabaseHelper.COLUMN_USER_USERNAME)),
                    passwordHash = it.getString(it.getColumnIndex(DatabaseHelper.COLUMN_USER_PASSWORD_HASH)),
                    name = it.getString(it.getColumnIndex(DatabaseHelper.COLUMN_USER_NAME)),
                    phone = it.getString(it.getColumnIndex(DatabaseHelper.COLUMN_USER_PHONE)),
                    email = it.getString(it.getColumnIndex(DatabaseHelper.COLUMN_USER_EMAIL)),
                    avatarPath = it.getString(it.getColumnIndex(DatabaseHelper.COLUMN_USER_AVATAR_PATH))
                )
            }
        }
        // db.close() // Cân nhắc lại
        return user
    }

    // XÓA @RequiresApi
    fun checkLogin(username: String, passwordAttempt: String): User? {
        val user = getUserByUsername(username) // Nên chạy trên background thread
        if (user == null) {
            return null // User không tồn tại
        }
        // Gọi trực tiếp PasswordUtil (Nên chạy trên background thread)
        val isPasswordCorrect = PasswordUtil.verifyPassword(passwordAttempt, user.passwordHash)

        return if (isPasswordCorrect) {
            user
        } else {
            null
        }
    }

    fun updateUser(user: User): Int {
        // ... (giữ nguyên)
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.COLUMN_USER_NAME, user.name)
            put(DatabaseHelper.COLUMN_USER_PHONE, user.phone)
            put(DatabaseHelper.COLUMN_USER_EMAIL, user.email)
            put(DatabaseHelper.COLUMN_USER_AVATAR_PATH, user.avatarPath)
        }
        val rowsAffected = db.update(
            DatabaseHelper.TABLE_USERS,
            values,
            "${DatabaseHelper.COLUMN_USER_ID} = ?",
            arrayOf(user.id.toString())
        )
        db.close()
        return rowsAffected
    }

    fun updatePassword(userId: Long, newPasswordPlain: String): Int {
        val db = dbHelper.writableDatabase
        // Gọi trực tiếp PasswordUtil
        val newPasswordHash = PasswordUtil.hashPassword(newPasswordPlain)
        if (newPasswordHash == null) {
            db.close()
            return 0 // Hoặc ném exception, báo lỗi hash
        }

        val values = ContentValues().apply {
            put(DatabaseHelper.COLUMN_USER_PASSWORD_HASH, newPasswordHash)
        }
        val rowsAffected = db.update(
            DatabaseHelper.TABLE_USERS,
            values,
            "${DatabaseHelper.COLUMN_USER_ID} = ?",
            arrayOf(userId.toString())
        )
        db.close()
        return rowsAffected
    }
}