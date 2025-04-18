package com.example.students_system.data.db

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import com.example.students_system.data.model.Student

class StudentDao(context: Context) {

    private val dbHelper = DatabaseHelper(context)

    fun addStudent(student: Student): Long {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.COLUMN_STUDENT_NAME, student.name)
            put(DatabaseHelper.COLUMN_STUDENT_MATH, student.mathScore)
            put(DatabaseHelper.COLUMN_STUDENT_LITERATURE, student.literatureScore)
            put(DatabaseHelper.COLUMN_STUDENT_ENGLISH, student.englishScore)
            put(DatabaseHelper.COLUMN_STUDENT_EMAIL, student.email)
            put(DatabaseHelper.COLUMN_STUDENT_CLASS_NAME, student.className)
        }
        val id = db.insert(DatabaseHelper.TABLE_STUDENTS, null, values)
        return id
    }

    @SuppressLint("Range")
    fun getAllStudents(): List<Student> { // Hàm này vẫn hữu ích cho việc tính TB toàn lớp
        // ... (code như trước) ...
        val studentList = mutableListOf<Student>()
        val db = dbHelper.readableDatabase
        var cursor: Cursor? = null
        try {
            cursor = db.query(
                DatabaseHelper.TABLE_STUDENTS, null, null, null, null, null,
                "${DatabaseHelper.COLUMN_STUDENT_NAME} ASC"
            )
            cursor?.use {
                while (it.moveToNext()) {
                    // ... tạo Student object như trước ...
                    val student = Student(
                        id = it.getLong(it.getColumnIndex(DatabaseHelper.COLUMN_STUDENT_ID)),
                        name = it.getString(it.getColumnIndex(DatabaseHelper.COLUMN_STUDENT_NAME)),
                        mathScore = if (it.isNull(it.getColumnIndex(DatabaseHelper.COLUMN_STUDENT_MATH))) null
                        else it.getDouble(it.getColumnIndex(DatabaseHelper.COLUMN_STUDENT_MATH)),
                        literatureScore = if (it.isNull(it.getColumnIndex(DatabaseHelper.COLUMN_STUDENT_LITERATURE))) null
                        else it.getDouble(it.getColumnIndex(DatabaseHelper.COLUMN_STUDENT_LITERATURE)),
                        englishScore = if (it.isNull(it.getColumnIndex(DatabaseHelper.COLUMN_STUDENT_ENGLISH))) null
                        else it.getDouble(it.getColumnIndex(DatabaseHelper.COLUMN_STUDENT_ENGLISH)),
                        email = it.getString(it.getColumnIndex(DatabaseHelper.COLUMN_STUDENT_EMAIL)),
                        className = it.getString(it.getColumnIndex(DatabaseHelper.COLUMN_STUDENT_CLASS_NAME))
                    )
                    studentList.add(student)
                }
            }
        } finally {
            cursor?.close()
        }
        return studentList
    }

    @SuppressLint("Range")
    fun getStudentById(studentId: Long): Student? {
        val db = dbHelper.readableDatabase
        var student: Student? = null
        var cursor: Cursor? = null
        try {
            cursor = db.query(
                DatabaseHelper.TABLE_STUDENTS,
                null,
                "${DatabaseHelper.COLUMN_STUDENT_ID} = ?",
                arrayOf(studentId.toString()),
                null, null, null
            )

            cursor?.use {
                if (it.moveToFirst()) {
                    student = Student(
                        id = it.getLong(it.getColumnIndex(DatabaseHelper.COLUMN_STUDENT_ID)),
                        name = it.getString(it.getColumnIndex(DatabaseHelper.COLUMN_STUDENT_NAME)),
                        mathScore = it.getDouble(it.getColumnIndex(DatabaseHelper.COLUMN_STUDENT_MATH)),
                        literatureScore = it.getDouble(it.getColumnIndex(DatabaseHelper.COLUMN_STUDENT_LITERATURE)),
                        englishScore = it.getDouble(it.getColumnIndex(DatabaseHelper.COLUMN_STUDENT_ENGLISH)),
                        email = it.getString(it.getColumnIndex(DatabaseHelper.COLUMN_STUDENT_EMAIL)),
                        className = it.getString(it.getColumnIndex(DatabaseHelper.COLUMN_STUDENT_CLASS_NAME))
                    )
                }
            }
        } finally {
            cursor?.close()
        }
        return student
    }

    fun updateStudent(student: Student): Int {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.COLUMN_STUDENT_NAME, student.name)
            put(DatabaseHelper.COLUMN_STUDENT_MATH, student.mathScore)
            put(DatabaseHelper.COLUMN_STUDENT_LITERATURE, student.literatureScore)
            put(DatabaseHelper.COLUMN_STUDENT_ENGLISH, student.englishScore)
            put(DatabaseHelper.COLUMN_STUDENT_EMAIL, student.email)
            put(DatabaseHelper.COLUMN_STUDENT_CLASS_NAME, student.className)
        }
        val rowsAffected = db.update(
            DatabaseHelper.TABLE_STUDENTS,
            values,
            "${DatabaseHelper.COLUMN_STUDENT_ID} = ?",
            arrayOf(student.id.toString())
        )
        return rowsAffected
    }

    fun deleteStudent(studentId: Long): Int {
        val db = dbHelper.writableDatabase
        val rowsAffected = db.delete(
            DatabaseHelper.TABLE_STUDENTS,
            "${DatabaseHelper.COLUMN_STUDENT_ID} = ?",
            arrayOf(studentId.toString())
        )
        return rowsAffected
    }

    fun calculateClassAverage(): Double {
        val students = getAllStudents()
        if (students.isEmpty()) {
            return 0.0
        }
        val totalAverageSum = students.sumOf { it.calculateAverage() }
        return totalAverageSum / students.size
    }
    // --- HÀM MỚI: Lấy danh sách tên lớp duy nhất ---
    @SuppressLint("Range")
    fun getDistinctClassNames(): List<String> {
        val classNames = mutableSetOf<String>() // Dùng Set để tự động loại bỏ trùng lặp
        val db = dbHelper.readableDatabase
        var cursor: Cursor? = null
        try {
            cursor = db.query(
                true, // distinct = true
                DatabaseHelper.TABLE_STUDENTS,
                arrayOf(DatabaseHelper.COLUMN_STUDENT_CLASS_NAME), // Chỉ lấy cột class_name
                "${DatabaseHelper.COLUMN_STUDENT_CLASS_NAME} IS NOT NULL AND ${DatabaseHelper.COLUMN_STUDENT_CLASS_NAME} != ''", // Chỉ lấy lớp có tên
                null, null, null,
                "${DatabaseHelper.COLUMN_STUDENT_CLASS_NAME} ASC", // Sắp xếp theo tên lớp
                null
            )
            cursor?.use {
                while (it.moveToNext()) {
                    val className = it.getString(it.getColumnIndex(DatabaseHelper.COLUMN_STUDENT_CLASS_NAME))
                    // Thêm kiểm tra null/blank lần nữa cho chắc
                    if (!className.isNullOrBlank()) {
                        classNames.add(className)
                    }
                }
            }
        } finally {
            cursor?.close()
            // db.close()
        }
        return classNames.toList() // Chuyển Set thành List
    }
    // --- HÀM MỚI: Lấy danh sách học sinh đã lọc theo lớp và tìm kiếm ---
    @SuppressLint("Range")
    fun getFilteredStudents(classNameFilter: String?, searchQuery: String): List<Student> {
        val studentList = mutableListOf<Student>()
        val db = dbHelper.readableDatabase
        var cursor: Cursor? = null

        // Xây dựng điều kiện WHERE động
        var selection: String? = null
        val selectionArgs = mutableListOf<String>()

        // 1. Thêm điều kiện lọc theo lớp (nếu có)
        if (!classNameFilter.isNullOrBlank()) {
            selection = "${DatabaseHelper.COLUMN_STUDENT_CLASS_NAME} = ?"
            selectionArgs.add(classNameFilter)
        }

        // 2. Thêm điều kiện tìm kiếm (nếu có)
        if (searchQuery.isNotBlank()) {
            val searchCondition = "(${DatabaseHelper.COLUMN_STUDENT_NAME} LIKE ? OR ${DatabaseHelper.COLUMN_STUDENT_CLASS_NAME} LIKE ?)"
            selection = if (selection == null) {
                searchCondition // Nếu chưa có điều kiện lớp
            } else {
                "$selection AND $searchCondition" // Nếu đã có điều kiện lớp
            }
            selectionArgs.add("%$searchQuery%") // Tìm kiếm tên chứa searchQuery
            selectionArgs.add("%$searchQuery%") // Tìm kiếm lớp chứa searchQuery
        }

        try {
            cursor = db.query(
                DatabaseHelper.TABLE_STUDENTS,
                null, // Lấy tất cả các cột
                selection, // Điều kiện WHERE đã xây dựng
                selectionArgs.toTypedArray(), // Các tham số cho điều kiện WHERE
                null, null,
                null // Việc sắp xếp sẽ làm trong ViewModel
                //"${DatabaseHelper.COLUMN_STUDENT_NAME} ASC" // Sắp xếp mặc định nếu muốn
            )

            cursor?.use {
                while (it.moveToNext()) {
                    // ... tạo Student object như trong getAllStudents ...
                    val student = Student(
                        id = it.getLong(it.getColumnIndex(DatabaseHelper.COLUMN_STUDENT_ID)),
                        name = it.getString(it.getColumnIndex(DatabaseHelper.COLUMN_STUDENT_NAME)),
                        mathScore = if (it.isNull(it.getColumnIndex(DatabaseHelper.COLUMN_STUDENT_MATH))) null else it.getDouble(it.getColumnIndex(DatabaseHelper.COLUMN_STUDENT_MATH)),
                        literatureScore = if (it.isNull(it.getColumnIndex(DatabaseHelper.COLUMN_STUDENT_LITERATURE))) null else it.getDouble(it.getColumnIndex(DatabaseHelper.COLUMN_STUDENT_LITERATURE)),
                        englishScore = if (it.isNull(it.getColumnIndex(DatabaseHelper.COLUMN_STUDENT_ENGLISH))) null else it.getDouble(it.getColumnIndex(DatabaseHelper.COLUMN_STUDENT_ENGLISH)),
                        email = it.getString(it.getColumnIndex(DatabaseHelper.COLUMN_STUDENT_EMAIL)),
                        className = it.getString(it.getColumnIndex(DatabaseHelper.COLUMN_STUDENT_CLASS_NAME))
                    )
                    studentList.add(student)
                }
            }
        } finally {
            cursor?.close()
            // db.close()
        }
        return studentList
    }

}
