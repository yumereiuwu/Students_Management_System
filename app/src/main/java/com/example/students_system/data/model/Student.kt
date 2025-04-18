package com.example.students_system.data.model // Thay package nếu cần

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.math.RoundingMode
import java.text.DecimalFormat

@Parcelize
data class Student(
    val id: Long = -1,
    var name: String,
    var mathScore: Double?,
    var literatureScore: Double?,
    var englishScore: Double?,
    var email: String? = null,
    var className: String? = null
) : Parcelable {

    fun calculateAverage(): Double {
        val scores = listOfNotNull(mathScore, literatureScore, englishScore)
        return if (scores.isNotEmpty()) {
            scores.average()
        } else {
            0.0
        }
    }

    // **** HÀM QUAN TRỌNG CẦN CÓ ****
    fun getFormattedAverage(pattern: String = "#.##"): String {
        val average = calculateAverage()
        if (average == 0.0) return "0" // Trả về "0" nếu điểm TB là 0
        val df = DecimalFormat(pattern)
        df.roundingMode = RoundingMode.HALF_UP
        return df.format(average)
        // Hoặc đơn giản: return String.format("%.2f", calculateAverage())
    }
    // **** KẾT THÚC HÀM QUAN TRỌNG ****


    // Hàm lấy điểm dưới dạng String, hiển thị "N/A" nếu null
    fun getMathScoreString(): String = mathScore?.toString() ?: "N/A"
    fun getLiteratureScoreString(): String = literatureScore?.toString() ?: "N/A"
    fun getEnglishScoreString(): String = englishScore?.toString() ?: "N/A"

}