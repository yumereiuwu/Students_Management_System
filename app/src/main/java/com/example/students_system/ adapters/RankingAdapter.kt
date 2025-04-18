package com.example.students_system.adapters // Thay package nếu cần

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.students_system.R
import com.example.students_system.data.model.Student
import com.example.students_system.databinding.ItemRankingBinding // Đảm bảo import đúng ViewBinding

// Interface (tùy chọn, nếu muốn xử lý click vào item xếp hạng)
interface OnRankClickListener {
    fun onRankItemClick(student: Student, rank: Int)
}

class RankingAdapter(private val listener: OnRankClickListener? = null) : // Listener là tùy chọn
    ListAdapter<Student, RankingAdapter.RankingViewHolder>(StudentDiffCallback()) { // Dùng lại DiffCallback

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RankingViewHolder {
        val binding = ItemRankingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RankingViewHolder(binding, listener)
    }

    override fun onBindViewHolder(holder: RankingViewHolder, position: Int) {
        holder.bind(getItem(position), position + 1) // position + 1 là thứ hạng
    }

    // --- ViewHolder ---
    inner class RankingViewHolder(
        private val binding: ItemRankingBinding,
        private val listener: OnRankClickListener?
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(student: Student, rank: Int) {
            binding.textViewItemRankName.text = student.name
            binding.textViewItemRankAverage.text = student.getFormattedAverage()

            // Hiển thị thứ hạng và icon (ví dụ cho Top 3)
            binding.textViewItemRankNumber.text = rank.toString()
            when (rank) {
                1 -> {
                    binding.imageViewRankIcon.visibility = View.VISIBLE
                    binding.textViewItemRankNumber.visibility = View.INVISIBLE // Ẩn số nếu có icon
                    binding.imageViewRankIcon.setImageResource(R.drawable.ic_rank_gold) // Cần icon vàng
                    binding.imageViewRankIcon.setColorFilter(ContextCompat.getColor(itemView.context, R.color.rank_gold)) // Màu vàng
                }
                2 -> {
                    binding.imageViewRankIcon.visibility = View.VISIBLE
                    binding.textViewItemRankNumber.visibility = View.INVISIBLE
                    binding.imageViewRankIcon.setImageResource(R.drawable.ic_rank_silver) // Cần icon bạc
                    binding.imageViewRankIcon.setColorFilter(ContextCompat.getColor(itemView.context, R.color.rank_silver)) // Màu bạc
                }
                3 -> {
                    binding.imageViewRankIcon.visibility = View.VISIBLE
                    binding.textViewItemRankNumber.visibility = View.INVISIBLE
                    binding.imageViewRankIcon.setImageResource(R.drawable.ic_rank_bronze) // Cần icon đồng
                    binding.imageViewRankIcon.setColorFilter(ContextCompat.getColor(itemView.context, R.color.rank_bronze)) // Màu đồng
                }
                else -> {
                    binding.imageViewRankIcon.visibility = View.GONE
                    binding.textViewItemRankNumber.visibility = View.VISIBLE
                }
            }
            // Thêm màu sắc rank_gold, rank_silver, rank_bronze trong colors.xml

            // Xử lý sự kiện click vào item nếu có listener
            itemView.setOnClickListener {
                listener?.onRankItemClick(student, rank)
            }
        }
    }

    // --- DiffCallback (Dùng lại từ StudentAdapter nếu logic so sánh giống nhau) ---
    class StudentDiffCallback : DiffUtil.ItemCallback<Student>() {
        override fun areItemsTheSame(oldItem: Student, newItem: Student): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Student, newItem: Student): Boolean {
            return oldItem == newItem
        }
    }
}