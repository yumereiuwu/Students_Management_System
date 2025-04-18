package com.example.students_system.activities // Thay package nếu cần

import android.content.Intent
// import android.os.Build // Không cần nữa
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.widget.Toast
// import androidx.annotation.RequiresApi // Không cần nữa
import androidx.appcompat.app.AppCompatActivity
import com.example.students_system.R
import com.example.students_system.data.db.UserDao
import com.example.students_system.data.preferences.AppPreferences
import com.example.students_system.databinding.ActivityLoginBinding // Import ViewBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var userDao: UserDao

    // --- XÓA @RequiresApi Ở ĐÂY ---
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userDao = UserDao(this)

        // Kiểm tra nếu đã đăng nhập trước đó
        if (AppPreferences.isLoggedIn(this)) {
            navigateToMain()
            return // Thoát khỏi onCreate sớm
        }

        setupUI()
        setupListeners()
    }

    private fun setupUI() {
        // ... (Giữ nguyên)
        AppPreferences.getRememberedUsername(this)?.let { username ->
            binding.editTextUsername.setText(username)
            binding.checkBoxRememberMe.isChecked = true
        }
        binding.editTextPassword.transformationMethod = PasswordTransformationMethod.getInstance()
    }

    // --- XÓA @RequiresApi Ở ĐÂY ---
    private fun setupListeners() {
        binding.buttonLogin.setOnClickListener {
            handleLogin()
        }

        binding.textViewRegisterLink.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        binding.textViewForgotPassword.setOnClickListener {
            Toast.makeText(this, "Chức năng Quên mật khẩu chưa được cài đặt", Toast.LENGTH_SHORT).show()
        }

        binding.checkBoxRememberMe.setOnCheckedChangeListener { _, isChecked ->
            if (!isChecked) {
                AppPreferences.clearRememberMe(this@LoginActivity)
            }
        }
    }

    // --- XÓA @RequiresApi Ở ĐÂY ---
    private fun handleLogin() {
        val username = binding.editTextUsername.text.toString().trim()
        val password = binding.editTextPassword.text.toString()

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập tên đăng nhập và mật khẩu", Toast.LENGTH_SHORT).show()
            return
        }

        // --- QUAN TRỌNG: Thực hiện trên background thread trong app thực tế ---
        // Gọi userDao.checkLogin đã được sửa (không còn @RequiresApi)
        val loggedInUser = userDao.checkLogin(username, password)
        // --- ---

        if (loggedInUser != null) {
            Toast.makeText(this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show()
            AppPreferences.setLoggedIn(this, true, loggedInUser.id)

            if (binding.checkBoxRememberMe.isChecked) {
                AppPreferences.setRememberMe(this, true, username)
            } else {
                AppPreferences.setRememberMe(this, false)
            }

            navigateToMain()
        } else {
            Toast.makeText(this, "Tên đăng nhập hoặc mật khẩu không đúng", Toast.LENGTH_LONG).show()
            AppPreferences.setLoggedIn(this, false)
        }
    }

    private fun navigateToMain() {
        // ... (Giữ nguyên)
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}