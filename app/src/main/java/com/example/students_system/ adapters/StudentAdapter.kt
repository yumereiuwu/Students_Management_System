package com.example.students_system.adapters // Thay package nếu cần

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.students_system.R
import com.example.students_system.data.model.Student
import com.example.students_system.databinding.ItemStudentBinding // Đảm bảo import đúng ViewBinding

// Interface để xử lý các sự kiện click trên item
interface OnStudentClickListener {
    fun onEditClick(student: Student)
    fun onDeleteClick(student: Student)
    fun onSendEmailClick(student: Student)
}

class StudentAdapter(private val listener: OnStudentClickListener) :
    ListAdapter<Student, StudentAdapter.StudentViewHolder>(StudentDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StudentViewHolder {
        val binding = ItemStudentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return StudentViewHolder(binding, listener)
    }

    override fun onBindViewHolder(holder: StudentViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    // --- ViewHolder ---
    inner class StudentViewHolder(
        private val binding: ItemStudentBinding,
        private val listener: OnStudentClickListener
    ) : RecyclerView.ViewHolder(binding.root) {

        // --- SỬA HÀM NÀY ---
        fun bind(student: Student) {
            binding.textViewItemStudentName.text = student.name
            // --- THÊM DÒNG NÀY ĐỂ HIỂN THỊ LỚP ---
            binding.textViewItemStudentClass.text = student.className ?: "Chưa xếp lớp" // Hiển thị lớp hoặc text mặc định
            // --- KẾT THÚC THÊM DÒNG ---
            binding.textViewItemStudentAverage.text = student.getFormattedAverage()
            binding.textViewItemStudentEmail.text = student.email ?: "Chưa có email"
            binding.textViewItemStudentScores.text =
                "Toán: ${student.getMathScoreString()} - Văn: ${student.getLiteratureScoreString()} - Anh: ${student.getEnglishScoreString()}"

            // Gán listener cho các nút
            binding.buttonItemEditStudent.setOnClickListener {
                listener.onEditClick(student)
            }
            binding.buttonItemDeleteStudent.setOnClickListener {
                listener.onDeleteClick(student)
            }
            binding.buttonItemSendEmail.setOnClickListener {
                listener.onSendEmailClick(student)
            }
            // Ẩn nút email nếu học sinh không có email
            binding.buttonItemSendEmail.isEnabled = !student.email.isNullOrBlank()
            binding.buttonItemSendEmail.alpha = if (student.email.isNullOrBlank()) 0.5f else 1.0f
        }
    }

    // --- DiffCallback để tối ưu RecyclerView ---
    class StudentDiffCallback : DiffUtil.ItemCallback<Student>() {
        override fun areItemsTheSame(oldItem: Student, newItem: Student): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Student, newItem: Student): Boolean {
            // So sánh cả className để biết nội dung thay đổi
            return oldItem == newItem
        }
    }
}