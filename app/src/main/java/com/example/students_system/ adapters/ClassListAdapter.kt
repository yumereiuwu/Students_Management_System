package com.example.students_system.adapters // Thay package nếu cần

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.students_system.databinding.ItemClassNameBinding // Import binding cho item_class_name.xml

// Interface để xử lý khi người dùng nhấn vào tên lớp
interface OnClassClickListener {
    fun onClassClick(className: String)
}

class ClassListAdapter(private val listener: OnClassClickListener) :
    ListAdapter<String, ClassListAdapter.ClassViewHolder>(ClassDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClassViewHolder {
        val binding = ItemClassNameBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ClassViewHolder(binding, listener)
    }

    override fun onBindViewHolder(holder: ClassViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ClassViewHolder(
        private val binding: ItemClassNameBinding,
        private val listener: OnClassClickListener
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(className: String) {
            binding.textViewClassNameItem.text = className // Hiển thị tên lớp
            itemView.setOnClickListener {
                listener.onClassClick(className) // Gọi listener khi nhấn
            }
        }
    }

    class ClassDiffCallback : DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem // So sánh tên lớp
        }

        override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }
    }
}