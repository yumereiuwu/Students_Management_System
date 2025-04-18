package com.example.students_system.activities // Thay package nếu cần

import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.students_system.data.db.UserDao
import com.example.students_system.data.model.User
import com.example.students_system.databinding.ActivityRegisterBinding // Import ViewBinding
import com.example.students_system.utils.PasswordUtil

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var userDao: UserDao

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userDao = UserDao(this)

        setupToolbar()
        setupListeners()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // Hiển thị nút back
        supportActionBar?.setDisplayShowHomeEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed() // Xử lý khi nhấn nút back trên toolbar
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupListeners() {
        binding.buttonRegister.setOnClickListener {
            handleRegister()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun handleRegister() {
        val username = binding.editTextRegUsername.text.toString().trim()
        val password = binding.editTextRegPassword.text.toString()
        val confirmPassword = binding.editTextRegConfirmPassword.text.toString()
        // Lấy thêm tên, email, sđt nếu có thêm trường trên layout đăng ký

        // --- Validation ---
        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show()
            return
        }

        if (password.length < 6) { // Ví dụ: yêu cầu mật khẩu tối thiểu 6 ký tự
            Toast.makeText(this, "Mật khẩu phải có ít nhất 6 ký tự", Toast.LENGTH_SHORT).show()
            return
        }

        if (password != confirmPassword) {
            binding.textInputLayoutRegConfirmPassword.error = "Mật khẩu xác nhận không khớp"
            // Xóa lỗi ở trường password nếu có
            binding.textInputLayoutRegPassword.error = null
            return
        } else {
            // Xóa lỗi nếu đã khớp
            binding.textInputLayoutRegConfirmPassword.error = null
            binding.textInputLayoutRegPassword.error = null
        }

        // --- Kiểm tra username tồn tại (Thực hiện trên background thread) ---
        if (userDao.getUserByUsername(username) != null) {
            binding.textInputLayoutRegUsername.error = "Tên đăng nhập đã tồn tại"
            Toast.makeText(this, "Tên đăng nhập đã tồn tại", Toast.LENGTH_SHORT).show()
            return
        } else {
            binding.textInputLayoutRegUsername.error = null
        }
        // --- ---

        // --- Hash mật khẩu (Thực hiện trên background thread) ---
        val passwordHash = PasswordUtil.hashPassword(password)
        if (passwordHash == null) {
            Toast.makeText(this, "Lỗi xử lý mật khẩu, vui lòng thử lại.", Toast.LENGTH_SHORT).show()
            return
        }
        // --- ---

        // --- Tạo User và thêm vào DB (Thực hiện trên background thread) ---
        val newUser = User(
            username = username,
            passwordHash = passwordHash // Lưu hash, không lưu pass gốc
            // name = name, // Nếu có
            // email = email, // Nếu có
            // phone = phone  // Nếu có
        )

        val userId = userDao.addUser(newUser)
        // --- ---

        if (userId != -1L) {
            // Đăng ký thành công
            Toast.makeText(this, "Đăng ký thành công! Vui lòng đăng nhập.", Toast.LENGTH_LONG).show()
            finish() // Đóng màn hình Register, quay lại Login
        } else {
            // Đăng ký thất bại (Lỗi DB?)
            Toast.makeText(this, "Đăng ký thất bại, vui lòng thử lại.", Toast.LENGTH_SHORT).show()
        }
    }
}