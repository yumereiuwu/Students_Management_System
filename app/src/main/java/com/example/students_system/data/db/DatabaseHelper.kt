package com.example.students_system.data.db // Thay package nếu cần

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log // Thêm import Log

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context.applicationContext, DATABASE_NAME, null, DATABASE_VERSION) { // Dùng applicationContext

    companion object {
        private const val TAG = "DatabaseHelper" // Thêm TAG để log
        private const val DATABASE_NAME = "student_manager.db"
        private const val DATABASE_VERSION = 2 // Đã tăng version lên 2 - ĐÚNG

        // Bảng Users
        const val TABLE_USERS = "users"
        const val COLUMN_USER_ID = "_id"
        const val COLUMN_USER_USERNAME = "username"
        const val COLUMN_USER_PASSWORD_HASH = "password_hash"
        const val COLUMN_USER_NAME = "name"
        const val COLUMN_USER_PHONE = "phone"
        const val COLUMN_USER_EMAIL = "email"
        const val COLUMN_USER_AVATAR_PATH = "avatar_path"

        // Bảng Students
        const val TABLE_STUDENTS = "students"
        const val COLUMN_STUDENT_ID = "_id"
        const val COLUMN_STUDENT_NAME = "name"
        const val COLUMN_STUDENT_MATH = "math_score"
        const val COLUMN_STUDENT_LITERATURE = "literature_score"
        const val COLUMN_STUDENT_ENGLISH = "english_score"
        const val COLUMN_STUDENT_EMAIL = "email" // Cột email đã có
        const val COLUMN_STUDENT_CLASS_NAME = "class_name" // Cột lớp học mới
    }

    // Câu lệnh tạo bảng Users (Giữ nguyên)
    private val CREATE_TABLE_USERS_SQL = """
        CREATE TABLE $TABLE_USERS (
            $COLUMN_USER_ID INTEGER PRIMARY KEY AUTOINCREMENT,
            $COLUMN_USER_USERNAME TEXT UNIQUE NOT NULL,
            $COLUMN_USER_PASSWORD_HASH TEXT NOT NULL,
            $COLUMN_USER_NAME TEXT,
            $COLUMN_USER_PHONE TEXT,
            $COLUMN_USER_EMAIL TEXT,
            $COLUMN_USER_AVATAR_PATH TEXT
        );
    """.trimIndent()

    // --- SỬA CÂU LỆNH TẠO BẢNG STUDENTS ---
    private val CREATE_TABLE_STUDENTS_SQL = """
        CREATE TABLE $TABLE_STUDENTS (
            $COLUMN_STUDENT_ID INTEGER PRIMARY KEY AUTOINCREMENT,
            $COLUMN_STUDENT_NAME TEXT NOT NULL,
            $COLUMN_STUDENT_MATH REAL,
            $COLUMN_STUDENT_LITERATURE REAL,
            $COLUMN_STUDENT_ENGLISH REAL,
            $COLUMN_STUDENT_EMAIL TEXT,
            $COLUMN_STUDENT_CLASS_NAME TEXT  -- Đã thêm cột lớp học
        );
    """.trimIndent()

    override fun onCreate(db: SQLiteDatabase?) {
        Log.i(TAG, "Creating database tables (Version $DATABASE_VERSION)...") // Thêm Log
        try {
            db?.execSQL(CREATE_TABLE_USERS_SQL)
            db?.execSQL(CREATE_TABLE_STUDENTS_SQL) // Gọi câu lệnh CREATE đã sửa
            Log.i(TAG, "Database tables created successfully.")
        } catch (e: Exception) {
            Log.e(TAG, "Error creating tables", e)
        }
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        Log.w(TAG, "Upgrading database from version $oldVersion to $newVersion")
        if (db == null) {
            Log.e(TAG, "Database is null in onUpgrade, cannot upgrade.")
            return
        }

        // Xử lý nâng cấp từ version 1 lên 2: Thêm cột class_name
        if (oldVersion < 2) {
            try {
                Log.i(TAG, "Upgrading DB v1 to v2: Adding column '$COLUMN_STUDENT_CLASS_NAME'")
                // --- SỬA CỘT CẦN THÊM ---
                db.execSQL("ALTER TABLE $TABLE_STUDENTS ADD COLUMN $COLUMN_STUDENT_CLASS_NAME TEXT;")
                Log.i(TAG, "Column '$COLUMN_STUDENT_CLASS_NAME' added successfully.")
            } catch (e: Exception) {
                Log.e(TAG, "Error adding column '$COLUMN_STUDENT_CLASS_NAME' during upgrade from v$oldVersion", e)
                // Cân nhắc giải pháp khác nếu ALTER không thành công (ví dụ: tạo bảng tạm, copy dữ liệu, xóa bảng cũ, đổi tên bảng tạm)
                // Hoặc giải pháp đơn giản nhất là xóa và tạo lại (nhưng MẤT DỮ LIỆU)
                // Log.w(TAG, "Dropping and recreating tables due to upgrade error.")
                // db.execSQL("DROP TABLE IF EXISTS $TABLE_STUDENTS")
                // db.execSQL(CREATE_TABLE_STUDENTS_SQL)
            }
        }
        // Thêm các khối 'if (oldVersion < X)' khác ở đây cho các phiên bản tương lai
        // Ví dụ:
        // if (oldVersion < 3) {
        //    // Nâng cấp từ version 2 lên 3
        //    Log.i(TAG, "Upgrading DB v2 to v3: ...")
        //    db.execSQL("ALTER TABLE ...")
        // }
    }

    // onDowngrade giữ nguyên hoặc làm tương tự onUpgrade nếu cần
    override fun onDowngrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        Log.w(TAG, "Downgrading database from version $oldVersion to $newVersion. Recreating tables (DATA LOSS).")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_STUDENTS")
        onCreate(db)
    }
}