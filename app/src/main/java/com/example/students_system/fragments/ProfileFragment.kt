package com.example.students_system.fragments // Thay package nếu cần

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.students_system.R
import com.example.students_system.activities.MainActivity
import com.example.students_system.data.db.UserDao
import com.example.students_system.data.model.User
import com.example.students_system.data.preferences.AppPreferences
import com.example.students_system.databinding.FragmentProfileBinding // Import ViewBinding
import com.example.students_system.utils.ImagePickerUtil
import com.example.students_system.utils.PasswordUtil
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var userDao: UserDao
    private var currentUser: User? = null
    private var selectedAvatarUri: Uri? = null // Lưu Uri ảnh mới chọn

    private lateinit var imagePickerLauncher: ActivityResultLauncher<Intent>
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        userDao = UserDao(requireContext())
        setupLaunchers() // Khởi tạo các launchers
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadUserProfile()
        setupListeners()
    }

    // Khởi tạo ActivityResultLaunchers
    private fun setupLaunchers() {
        // Launcher để chọn ảnh
        imagePickerLauncher = ImagePickerUtil.registerImagePicker(this) { uri ->
            if (uri != null) {
                selectedAvatarUri = uri // Lưu Uri mới
                // Hiển thị ảnh mới chọn ngay lập tức
                Glide.with(this)
                    .load(uri)
                    .placeholder(R.drawable.ic_person_placeholder)
                    .error(R.drawable.ic_person_placeholder)
                    .circleCrop()
                    .into(binding.imageViewProfileAvatar)
                // Lưu ý: Ảnh chưa được lưu vào DB cho đến khi nhấn "Lưu thay đổi"
            }
        }

        // Launcher để yêu cầu quyền
        requestPermissionLauncher = ImagePickerUtil.registerPermissionLauncher(this) { isGranted ->
            if (isGranted) {
                // Quyền đã được cấp, mở thư viện ảnh
                ImagePickerUtil.pickImageFromGallery(requireContext(), imagePickerLauncher)
            } else {
                // Quyền bị từ chối
                Toast.makeText(requireContext(), "Quyền truy cập bộ nhớ bị từ chối.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Tải thông tin người dùng từ DB
    private fun loadUserProfile() {
        val userId = AppPreferences.getLoggedInUserId(requireContext())
        if (userId != -1L) {
            // --- QUAN TRỌNG: Thực hiện trên background thread ---
            currentUser = userDao.getUserById(userId)
            // --- ---
            populateUI()
        } else {
            // Lỗi không có user ID, nên xử lý (ví dụ: quay lại login)
            Toast.makeText(requireContext(), "Lỗi: Không tìm thấy thông tin người dùng.", Toast.LENGTH_LONG).show()
            (activity as? MainActivity)?.handleLogout() // Gọi logout từ MainActivity
        }
    }

    // Điền thông tin user vào các trường UI
    private fun populateUI() {
        currentUser?.let { user ->
            binding.editTextProfileName.setText(user.name ?: "")
            binding.editTextProfilePhone.setText(user.phone ?: "")
            binding.editTextProfileEmail.setText(user.email ?: "")

            // Load avatar bằng Glide
            if (!user.avatarPath.isNullOrBlank()) {
                Glide.with(this)
                    .load(user.avatarPath) // Load từ đường dẫn đã lưu
                    .placeholder(R.drawable.ic_person_placeholder)
                    .error(R.drawable.ic_person_placeholder)
                    .circleCrop()
                    .into(binding.imageViewProfileAvatar)
            } else {
                Glide.with(this)
                    .load(R.drawable.ic_person_placeholder) // Load ảnh mặc định
                    .circleCrop()
                    .into(binding.imageViewProfileAvatar)
            }
        }
    }

    private fun setupListeners() {
        binding.imageViewEditAvatar.setOnClickListener {
            requestStoragePermissionAndPickImage()
        }
        binding.imageViewProfileAvatar.setOnClickListener { // Cũng cho phép click vào ảnh lớn
            requestStoragePermissionAndPickImage()
        }

        binding.buttonSaveChanges.setOnClickListener {
            saveChanges()
        }

        binding.buttonChangePassword.setOnClickListener {
            // TODO: Hiển thị Dialog hoặc Fragment đổi mật khẩu
            showChangePasswordDialog() // Ví dụ gọi hàm hiển thị dialog
            // Toast.makeText(requireContext(), "Chức năng đổi mật khẩu đang phát triển", Toast.LENGTH_SHORT).show()
        }
    }

    // Kiểm tra quyền và mở trình chọn ảnh
    private fun requestStoragePermissionAndPickImage() {
        val permission = ImagePickerUtil.getReadStoragePermission()
        if (ContextCompat.checkSelfPermission(requireContext(), permission) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            // Đã có quyền, mở thư viện
            ImagePickerUtil.pickImageFromGallery(requireContext(), imagePickerLauncher)
        } else {
            // Chưa có quyền, yêu cầu quyền
            requestPermissionLauncher.launch(permission)
            // TODO: Có thể hiển thị giải thích tại sao cần quyền trước khi gọi launch
        }
    }


    // Lưu thay đổi thông tin người dùng
    private fun saveChanges() {
        val name = binding.editTextProfileName.text.toString().trim()
        val phone = binding.editTextProfilePhone.text.toString().trim().ifEmpty { null }
        val email = binding.editTextProfileEmail.text.toString().trim().ifEmpty { null }

        // TODO: Validate dữ liệu (ví dụ: định dạng email, sđt)

        currentUser?.let { user ->
            user.name = name
            user.phone = phone
            user.email = email

            // Xử lý ảnh mới chọn (nếu có)
            if (selectedAvatarUri != null) {
                // --- QUAN TRỌNG: Nên copy ảnh vào bộ nhớ trong của app và lưu đường dẫn file đó ---
                // --- Thao tác file nên thực hiện trên background thread ---
                val newAvatarPath = saveImageToInternalStorage(selectedAvatarUri!!) // !! chỉ dùng khi chắc chắn không null
                if (newAvatarPath != null) {
                    // Xóa ảnh cũ nếu có và khác ảnh mới
                    if (!user.avatarPath.isNullOrBlank() && user.avatarPath != newAvatarPath) {
                        try { File(user.avatarPath!!).delete() } catch (e: Exception) { /* Bỏ qua lỗi xóa */ }
                    }
                    user.avatarPath = newAvatarPath // Cập nhật đường dẫn mới
                } else {
                    Toast.makeText(requireContext(), "Lỗi khi lưu ảnh đại diện", Toast.LENGTH_SHORT).show()
                    // Không nên dừng việc lưu các thông tin khác nếu chỉ lỗi lưu ảnh
                }
                selectedAvatarUri = null // Reset sau khi xử lý
            }


            // --- Cập nhật user vào DB (Thực hiện trên background thread) ---
            val rowsAffected = userDao.updateUser(user)
            // --- ---

            if (rowsAffected > 0) {
                Toast.makeText(requireContext(), "Cập nhật hồ sơ thành công", Toast.LENGTH_SHORT).show()
                // Cập nhật lại NavHeader trong MainActivity
                (activity as? MainActivity)?.updateNavHeaderUI()
            } else {
                Toast.makeText(requireContext(), "Cập nhật hồ sơ thất bại", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Hàm ví dụ để lưu ảnh vào bộ nhớ trong (CẦN HOÀN THIỆN VÀ CHẠY TRÊN BACKGROUND THREAD)
    private fun saveImageToInternalStorage(uri: Uri): String? {
        try {
            val inputStream: InputStream? = requireContext().contentResolver.openInputStream(uri)
            if (inputStream == null) {
                Log.e("ProfileFragment", "Cannot open InputStream for Uri: $uri")
                return null
            }

            // Tạo tên file duy nhất (ví dụ: dùng timestamp)
            val fileName = "avatar_${System.currentTimeMillis()}.jpg"
            // Lấy thư mục pictures riêng của ứng dụng
            val outputDir = File(requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES), "avatars")
            if (!outputDir.exists()) {
                outputDir.mkdirs() // Tạo thư mục nếu chưa có
            }
            val outputFile = File(outputDir, fileName)
            val outputStream = FileOutputStream(outputFile)

            // Copy dữ liệu từ InputStream sang FileOutputStream
            inputStream.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
            Log.d("ProfileFragment", "Avatar saved to: ${outputFile.absolutePath}")
            return outputFile.absolutePath // Trả về đường dẫn tuyệt đối của file đã lưu
        } catch (e: Exception) {
            Log.e("ProfileFragment", "Error saving image to internal storage", e)
            e.printStackTrace()
            return null // Trả về null nếu có lỗi
        }
    }


    // Hàm ví dụ hiển thị dialog đổi mật khẩu (Cần thiết kế layout cho dialog này)
    @RequiresApi(Build.VERSION_CODES.O)
    private fun showChangePasswordDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Đổi Mật khẩu")

        // Inflate layout tùy chỉnh cho dialog (ví dụ: dialog_change_password.xml)
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_change_password, null) // Cần tạo layout này
        val oldPasswordEditText = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.editTextOldPassword)
        val newPasswordEditText = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.editTextNewPassword)
        val confirmPasswordEditText = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.editTextConfirmNewPassword)
        // Thêm TextInputLayout tương ứng...

        builder.setView(dialogView)

        builder.setPositiveButton("Xác nhận") { dialog, _ ->
            val oldPassword = oldPasswordEditText.text.toString()
            val newPassword = newPasswordEditText.text.toString()
            val confirmPassword = confirmPasswordEditText.text.toString()

            // --- Validate ---
            if (oldPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(context, "Vui lòng nhập đủ mật khẩu", Toast.LENGTH_SHORT).show()
                // Không đóng dialog - hoặc cần xử lý phức tạp hơn để giữ dialog mở
                return@setPositiveButton
            }
            if (newPassword.length < 6) {
                Toast.makeText(context, "Mật khẩu mới phải có ít nhất 6 ký tự", Toast.LENGTH_SHORT).show()
                return@setPositiveButton
            }
            if (newPassword != confirmPassword) {
                Toast.makeText(context, "Mật khẩu mới không khớp", Toast.LENGTH_SHORT).show()
                // TODO: Set error cho TextInputLayout của confirm password
                return@setPositiveButton
            }

            // --- Xác thực mật khẩu cũ (Thực hiện trên background thread) ---
            currentUser?.let { user ->
                if (PasswordUtil.verifyPassword(oldPassword, user.passwordHash)) {
                    // --- Hash mật khẩu mới và cập nhật DB (Thực hiện trên background thread) ---
                    val newPasswordHash = PasswordUtil.hashPassword(newPassword)
                    if (newPasswordHash != null) {
                        user.passwordHash = newPasswordHash // Cập nhật hash mới cho user object
                        val rowsAffected = userDao.updatePassword(user.id, newPassword) // Gọi hàm update riêng
                        if (rowsAffected > 0) {
                            Toast.makeText(context, "Đổi mật khẩu thành công", Toast.LENGTH_SHORT).show()
                            dialog.dismiss()
                        } else {
                            Toast.makeText(context, "Lỗi khi cập nhật mật khẩu", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(context, "Lỗi xử lý mật khẩu mới", Toast.LENGTH_SHORT).show()
                    }
                    // --- ---
                } else {
                    Toast.makeText(context, "Mật khẩu cũ không đúng", Toast.LENGTH_SHORT).show()
                    // TODO: Set error cho TextInputLayout của mật khẩu cũ
                }
            }
            // --- ---
        }
        builder.setNegativeButton("Hủy") { dialog, _ ->
            dialog.cancel()
        }
        builder.create().show()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}