package com.example.students_system.utils // Thay package nếu cần

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

object ImagePickerUtil {

    // Launcher để chọn ảnh từ thư viện
    fun registerImagePicker(
        fragment: Fragment, // Hoặc Activity
        onImagePicked: (Uri?) -> Unit
    ): ActivityResultLauncher<Intent> {
        return fragment.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == android.app.Activity.RESULT_OK) {
                val data: Intent? = result.data
                onImagePicked(data?.data) // Trả về Uri của ảnh đã chọn
            } else {
                onImagePicked(null) // Không chọn ảnh hoặc có lỗi
            }
        }
    }

    // Hàm để khởi chạy việc chọn ảnh
    fun pickImageFromGallery(context: Context, launcher: ActivityResultLauncher<Intent>) {
        // TODO: Nên kiểm tra và yêu cầu quyền READ_MEDIA_IMAGES hoặc READ_EXTERNAL_STORAGE ở đây
        //       trước khi khởi chạy Intent nếu ứng dụng chưa có quyền.
        //       Sử dụng registerForActivityResult(ActivityResultContracts.RequestPermission()...)

        if (hasReadStoragePermission(context)) {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            // Hoặc dùng Intent.ACTION_GET_CONTENT nếu muốn picker rộng hơn
            // val intent = Intent(Intent.ACTION_GET_CONTENT)
            // intent.type = "image/*"
            try {
                launcher.launch(intent)
            } catch (e: Exception) {
                Toast.makeText(context, "Không thể mở thư viện ảnh.", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }

        } else {
            // TODO: Yêu cầu quyền nếu chưa có
            Toast.makeText(context, "Cần cấp quyền truy cập bộ nhớ để chọn ảnh.", Toast.LENGTH_LONG).show()
            // Ví dụ gọi hàm yêu cầu quyền (cần tạo launcher riêng cho quyền)
            // requestPermissionLauncher.launch(getReadStoragePermission())
        }
    }

    // Hàm kiểm tra quyền đọc bộ nhớ
    private fun hasReadStoragePermission(context: Context): Boolean {
        val permission = getReadStoragePermission()
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }

    // Lấy tên quyền đọc bộ nhớ phù hợp với phiên bản Android
    fun getReadStoragePermission(): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13+
            Manifest.permission.READ_MEDIA_IMAGES
        } else { // Dưới Android 13
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
    }

    // (Tùy chọn) Launcher để yêu cầu quyền
    fun registerPermissionLauncher(
        fragment: Fragment, // Hoặc Activity
        onPermissionResult: (Boolean) -> Unit
    ): ActivityResultLauncher<String> {
        return fragment.registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            onPermissionResult(isGranted)
        }
    }

    // (Tùy chọn) Hàm yêu cầu quyền
    fun requestReadStoragePermission(launcher: ActivityResultLauncher<String>) {
        launcher.launch(getReadStoragePermission())
    }

}