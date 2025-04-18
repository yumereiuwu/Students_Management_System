// utils/EmailUtil.kt
package com.example.students_system.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.example.students_system.data.model.Student

object EmailUtil {

    fun sendStudentResultEmail(context: Context, student: Student) {
        val recipientEmail = student.email
        if (recipientEmail.isNullOrBlank()) {
            Toast.makeText(context, "Email của học sinh ${student.name} không có.", Toast.LENGTH_SHORT).show()
            return
        }

        val subject = "Thông báo kết quả học tập - Học sinh ${student.name}"
        val body = """
            Xin chào,

            Đây là kết quả học tập của học sinh ${student.name}:
            - Toán: ${student.mathScore ?: "Chưa nhập"}
            - Văn: ${student.literatureScore ?: "Chưa nhập"}
            - Anh: ${student.englishScore ?: "Chưa nhập"}
            - Điểm trung bình: ${String.format("%.2f", student.calculateAverage())}

            Trân trọng,
            Ban Giám Hiệu (Hoặc Tên Ứng Dụng)
        """.trimIndent()

        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:") // Chỉ mở ứng dụng email
            putExtra(Intent.EXTRA_EMAIL, arrayOf(recipientEmail))
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
        }

        try {
            // Kiểm tra xem có ứng dụng nào xử lý được Intent này không
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
            } else {
                Toast.makeText(context, "Không tìm thấy ứng dụng Email.", Toast.LENGTH_SHORT).show()
                // Có thể thử dùng ACTION_SEND nếu SENDTO không được
                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "message/rfc822" // Chuẩn MIME cho email
                    putExtra(Intent.EXTRA_EMAIL, arrayOf(recipientEmail))
                    putExtra(Intent.EXTRA_SUBJECT, subject)
                    putExtra(Intent.EXTRA_TEXT, body)
                }
                if (sendIntent.resolveActivity(context.packageManager) != null) {
                    context.startActivity(Intent.createChooser(sendIntent, "Gửi email bằng..."))
                } else {
                    Toast.makeText(context, "Không tìm thấy ứng dụng nào để gửi Email.", Toast.LENGTH_LONG).show()
                }
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Lỗi khi mở ứng dụng Email.", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    fun sendMessageToDeveloper(context: Context, developerEmail: String, subjectPrefix: String = "Góp ý ứng dụng") {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:") // Chỉ mở ứng dụng email
            putExtra(Intent.EXTRA_EMAIL, arrayOf(developerEmail))
            putExtra(Intent.EXTRA_SUBJECT, subjectPrefix)
            // putExtra(Intent.EXTRA_TEXT, "Nội dung email...") // Có thể thêm nội dung mẫu
        }
        try {
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
            } else {
                Toast.makeText(context, "Không tìm thấy ứng dụng Email.", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Lỗi khi mở ứng dụng Email.", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }
}