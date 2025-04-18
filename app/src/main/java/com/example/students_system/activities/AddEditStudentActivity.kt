package com.example.students_system.activities // Thay package nếu cần

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.students_system.activities.AddEditStudentActivity.Companion.INVALID_SCORE
import com.example.students_system.data.db.StudentDao
import com.example.students_system.data.model.Student
import com.example.students_system.databinding.ActivityAddEditStudentBinding // Import ViewBinding

class AddEditStudentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddEditStudentBinding
    private lateinit var studentDao: StudentDao
    private var currentStudent: Student? = null

    companion object {
        const val EXTRA_STUDENT = "com.example.students_system.EXTRA_STUDENT"
        const val INVALID_SCORE = -1.0
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddEditStudentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        studentDao = StudentDao(this) // Nên dùng applicationContext nếu có thể

        if (intent.hasExtra(EXTRA_STUDENT)) {
            // Nên dùng getParcelableExtra với kiểu dữ liệu cụ thể từ API 33+
            currentStudent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra(EXTRA_STUDENT, Student::class.java)
            } else {
                @Suppress("DEPRECATION")
                intent.getParcelableExtra(EXTRA_STUDENT)
            }

            title = "Sửa thông tin học sinh"
        } else {
            title = "Thêm học sinh mới"
        }

        setupToolbar()
        populateFields()
        setupListeners()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        binding.toolbar.title = title
        binding.toolbar.setNavigationOnClickListener {
            // Trả về kết quả cancel nếu người dùng nhấn back mà không lưu
            setResult(Activity.RESULT_CANCELED)
            finish()
        }
    }

    // --- SỬA HÀM NÀY ---
    private fun populateFields() {
        currentStudent?.let { student ->
            binding.editTextStudentName.setText(student.name)
            binding.editTextStudentClassName.setText(student.className ?: "") // <-- THÊM DÒNG NÀY
            binding.editTextStudentEmail.setText(student.email ?: "")
            binding.editTextMathScore.setText(student.mathScore?.toString() ?: "")
            binding.editTextLiteratureScore.setText(student.literatureScore?.toString() ?: "")
            binding.editTextEnglishScore.setText(student.englishScore?.toString() ?: "")
        }
    }

    private fun setupListeners() {
        binding.buttonSaveStudent.setOnClickListener {
            saveStudent()
        }
    }

    // --- SỬA HÀM NÀY ---
    private fun saveStudent() {
        val name = binding.editTextStudentName.text.toString().trim()
        val className = binding.editTextStudentClassName.text.toString().trim().ifEmpty { null } // <-- THÊM DÒNG NÀY
        val email = binding.editTextStudentEmail.text.toString().trim().ifEmpty { null }

        if (name.isEmpty()) {
            binding.textInputLayoutStudentName.error = "Tên học sinh không được để trống"
            Toast.makeText(this, "Vui lòng nhập tên học sinh", Toast.LENGTH_SHORT).show()
            return
        } else {
            binding.textInputLayoutStudentName.error = null
        }
        // (Optional) Validate className
        // if (className.isNullOrBlank()) { ... }

        val mathScore = parseScore(binding.editTextMathScore.text.toString(), binding.textInputLayoutMathScore)
        val literatureScore = parseScore(binding.editTextLiteratureScore.text.toString(), binding.textInputLayoutLiteratureScore)
        val englishScore = parseScore(binding.editTextEnglishScore.text.toString(), binding.textInputLayoutEnglishScore)

        if (mathScore == INVALID_SCORE || literatureScore == INVALID_SCORE || englishScore == INVALID_SCORE) {
            Toast.makeText(this, "Vui lòng kiểm tra lại điểm số đã nhập", Toast.LENGTH_SHORT).show()
            return
        }

        // --- THÊM className VÀO CONSTRUCTOR ---
        val studentToSave = Student(
            id = currentStudent?.id ?: -1L,
            name = name,
            mathScore = mathScore,
            literatureScore = literatureScore,
            englishScore = englishScore,
            email = email,
            className = className // <-- THÊM Ở ĐÂY
        )

        var success = false
        try {
            // --- Nên chạy trên background thread ---
            if (currentStudent == null) {
                val newId = studentDao.addStudent(studentToSave)
                success = newId != -1L
            } else {
                val rowsAffected = studentDao.updateStudent(studentToSave)
                success = rowsAffected > 0
            }
            // --- ---
        } catch (e: Exception) {
            Toast.makeText(this, "Lỗi khi lưu vào cơ sở dữ liệu.", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
            success = false
        }

        if (success) {
            Toast.makeText(this, "Đã lưu thông tin học sinh!", Toast.LENGTH_SHORT).show()
            setResult(Activity.RESULT_OK)
            finish()
        } else {
            Toast.makeText(this, "Lưu thông tin thất bại.", Toast.LENGTH_SHORT).show()
        }
    }

    // ... parseScore() giữ nguyên ...

    // Xử lý nút back của hệ thống (tương tự nút back trên toolbar)
    override fun onBackPressed() {
        setResult(Activity.RESULT_CANCELED)
        super.onBackPressed()
    }
}

    // Hàm helper để parse điểm và validate
    private fun parseScore(scoreString: String, layout: com.google.android.material.textfield.TextInputLayout): Double? {
        return try {
            if (scoreString.isBlank()) {
                layout.error = null // Điểm trống là hợp lệ (coi như chưa nhập)
                null // Trả về null nếu trống
            } else {
                val score = scoreString.toDouble()
                if (score < 0.0 || score > 10.0) { // Validate điểm từ 0 đến 10
                    layout.error = "Điểm phải từ 0 đến 10"
                    INVALID_SCORE // Trả về giá trị đặc biệt nếu điểm không hợp lệ
                } else {
                    layout.error = null // Xóa lỗi nếu hợp lệ
                    score
                }
            }
        } catch (e: NumberFormatException) {
            layout.error = "Điểm không hợp lệ"
            INVALID_SCORE // Trả về giá trị đặc biệt nếu không phải số
        }
    }
