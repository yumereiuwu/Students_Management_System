package com.example.students_system.activities // Thay package nếu cần

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.bumptech.glide.Glide // Import Glide
import com.example.students_system.R
import com.example.students_system.data.db.UserDao
import com.example.students_system.data.model.User
import com.example.students_system.data.preferences.AppPreferences
import com.example.students_system.databinding.ActivityMainBinding // Import ViewBinding
import com.example.students_system.utils.EmailUtil
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var navController: NavController
    private lateinit var userDao: UserDao
    private var currentUser: User? = null

    // Views trong NavHeader
    private var headerImageViewAvatar: ImageView? = null
    private var headerTextViewUserName: TextView? = null
    private var headerTextViewUserEmail: TextView? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userDao = UserDao(this)

        setupNavigation()
        loadUserInfo()
        setupNavHeader() // Thiết lập NavHeader sau khi đã có thông tin user
    }

    private fun setupNavigation() {
        setSupportActionBar(binding.toolbar) // Đặt toolbar làm action bar

        val drawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView

        // Lấy NavController từ NavHostFragment
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment
        navController = navHostFragment.navController

        // Định nghĩa các destination cấp cao nhất (không có nút back trên Toolbar)
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_profile, R.id.nav_student_list, R.id.nav_ranking
            ), drawerLayout
        )

        // Liên kết ActionBar với NavController (hiển thị tiêu đề Fragment, xử lý nút back/menu)
        setupActionBarWithNavController(navController, appBarConfiguration)

        // Liên kết NavigationView với NavController (xử lý click item menu)
        navView.setupWithNavController(navController)

        // Thiết lập listener riêng cho NavigationView nếu cần xử lý đặc biệt (như Logout)
        navView.setNavigationItemSelectedListener(this)

        // (Optional) Cài đặt ActionBarDrawerToggle để có hiệu ứng animation đóng/mở drawer
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, binding.toolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
    }

    // Tải thông tin người dùng hiện tại từ DB
    private fun loadUserInfo() {
        val userId = AppPreferences.getLoggedInUserId(this)
        if (userId != -1L) {
            // Thực hiện trên background thread
            currentUser = userDao.getUserById(userId)
        } else {
            // Nếu không có user ID, có thể là lỗi -> nên logout
            handleLogout()
        }
    }

    // Cập nhật thông tin trên Nav Header
    private fun setupNavHeader() {
        val headerView = binding.navView.getHeaderView(0) // Lấy header view
        headerImageViewAvatar = headerView.findViewById(R.id.imageViewAvatarHeader)
        headerTextViewUserName = headerView.findViewById(R.id.textViewUserNameHeader)
        headerTextViewUserEmail = headerView.findViewById(R.id.textViewUserEmailHeader)

        updateNavHeaderUI() // Cập nhật lần đầu

        // (Optional) Cho phép click vào header để đi đến trang Profile
        headerView.setOnClickListener {
            if (navController.currentDestination?.id != R.id.nav_profile) {
                navController.navigate(R.id.nav_profile)
            }
            binding.drawerLayout.closeDrawer(GravityCompat.START) // Đóng drawer
        }
    }

    // Hàm cập nhật UI cho NavHeader (có thể gọi lại từ ProfileFragment sau khi thay đổi)
    fun updateNavHeaderUI() {
        currentUser?.let { user ->
            headerTextViewUserName?.text = user.name ?: user.username // Hiển thị tên hoặc username
            headerTextViewUserEmail?.text = user.email ?: "Chưa cập nhật email"

            // Load avatar bằng Glide
            headerImageViewAvatar?.let { imageView ->
                if (!user.avatarPath.isNullOrBlank()) {
                    Glide.with(this)
                        .load(user.avatarPath) // Đường dẫn file đã lưu
                        .placeholder(R.drawable.ic_person_placeholder) // Ảnh chờ
                        .error(R.drawable.ic_person_placeholder) // Ảnh lỗi
                        .circleCrop() // Bo tròn nếu không dùng CircleImageView
                        .into(imageView)
                } else {
                    // Đặt ảnh mặc định nếu không có avatarPath
                    Glide.with(this)
                        .load(R.drawable.ic_person_placeholder)
                        .circleCrop()
                        .into(imageView)
                    // Hoặc imageView.setImageResource(R.drawable.ic_person_placeholder)
                }
            }
        }
    }

    // Xử lý khi nhấn nút back của hệ thống (hoặc trên toolbar)
    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    // Xử lý khi nhấn nút back của thiết bị (để đóng drawer nếu đang mở)
    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed() // Thoát app nếu ở màn hình gốc hoặc back fragment
        }
    }

    // Xử lý các item trong NavigationView (được gọi bởi setupWithNavController và listener)
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Đóng drawer trước
        binding.drawerLayout.closeDrawer(GravityCompat.START)

        // Sử dụng NavController để điều hướng (setupWithNavController đã xử lý hầu hết)
        // Chỉ cần xử lý các trường hợp đặc biệt không có trong NavGraph hoặc cần action riêng

        when (item.itemId) {
            R.id.nav_message_dev -> {
                // TODO: Thay bằng email của bạn
                EmailUtil.sendMessageToDeveloper(this, "yumerei28092005@gmail.com")
                return true // true nếu đã xử lý item này
            }
            R.id.nav_logout -> {
                handleLogout()
                return true
            }
            // Các item khác (nav_home, nav_profile,...) sẽ được NavController xử lý tự động
            else -> {
                // Cho phép NavController xử lý các item menu khác
                // Cần gọi navigate thủ công nếu không dùng setupWithNavController hoàn toàn
                // Hoặc để trống nếu setupWithNavController đã xử lý
                // Ví dụ: navController.navigate(item.itemId)
                // Tuy nhiên, setupWithNavController thường đã làm việc này.
                // Trả về false để cho các xử lý mặc định khác (nếu có) chạy
                // return false
                return NavigationUI.onNavDestinationSelected(item, navController) || super.onOptionsItemSelected(item)

            }
        }
        // Đảm bảo đóng drawer nếu chưa đóng
        // binding.drawerLayout.closeDrawer(GravityCompat.START)
        // return true // Đã xử lý click
    }

    fun handleLogout() {
        // Xóa dữ liệu đăng nhập khỏi SharedPreferences
        AppPreferences.clearLoginData(this)
        // Không xóa Remember Me ở đây, để người dùng tiện đăng nhập lại

        // Chuyển về màn hình Login
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish() // Đóng MainActivity
    }

    // --- (Optional) Cập nhật lại thông tin user nếu quay lại từ ProfileFragment ---
    override fun onResume() {
        super.onResume()
        // Tải lại thông tin user nếu cần thiết (ví dụ sau khi sửa ở Profile)
        val oldUserId = currentUser?.id
        loadUserInfo() // Tải lại user từ DB
        if (currentUser?.id == oldUserId) { // Chỉ cập nhật UI nếu user không đổi (tránh lỗi khi logout)
            updateNavHeaderUI() // Cập nhật lại NavHeader
        }
    }
}