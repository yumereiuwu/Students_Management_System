package com.example.students_system.fragments // Thay package nếu cần

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.students_system.databinding.FragmentHomeBinding // Import ViewBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Có thể thêm logic khác cho màn hình Home ở đây nếu muốn
        // Ví dụ: Hiển thị tên người dùng đang đăng nhập
        // val mainActivity = activity as? MainActivity
        // binding.textViewWelcome.text = "Chào mừng, ${mainActivity?.currentUser?.name ?: "bạn"}!"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Clear binding khi view bị hủy
    }
}