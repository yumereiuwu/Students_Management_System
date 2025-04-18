package com.example.students_system.fragments // Thay package nếu cần

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.students_system.adapters.OnRankClickListener
import com.example.students_system.adapters.RankingAdapter
import com.example.students_system.data.db.StudentDao
import com.example.students_system.data.model.Student
import com.example.students_system.databinding.FragmentRankingBinding // Import ViewBinding

class RankingFragment : Fragment(), OnRankClickListener { // Implement listener (tùy chọn)

    private var _binding: FragmentRankingBinding? = null
    private val binding get() = _binding!!

    private lateinit var studentDao: StudentDao
    private lateinit var rankingAdapter: RankingAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRankingBinding.inflate(inflater, container, false)
        studentDao = StudentDao(requireContext())
        setupRecyclerView()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadAndRankStudents()
    }

    private fun setupRecyclerView() {
        rankingAdapter = RankingAdapter(this) // Truyền listener nếu có implement
        binding.recyclerViewRanking.apply {
            adapter = rankingAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun loadAndRankStudents() {
        // --- QUAN TRỌNG: Thực hiện trên background thread ---
        val allStudents = studentDao.getAllStudents()
        // Sắp xếp danh sách theo điểm trung bình giảm dần
        val rankedStudents = allStudents.sortedByDescending { it.calculateAverage() }
        // --- ---

        // Cập nhật RecyclerView trên UI thread
        requireActivity().runOnUiThread {
            if (rankedStudents.isEmpty()) {
                binding.recyclerViewRanking.visibility = View.GONE
                binding.textViewEmptyRanking.visibility = View.VISIBLE
            } else {
                binding.recyclerViewRanking.visibility = View.VISIBLE
                binding.textViewEmptyRanking.visibility = View.GONE
            }
            rankingAdapter.submitList(rankedStudents)
        }
    }

    // --- Implement OnRankClickListener (Tùy chọn) ---
    override fun onRankItemClick(student: Student, rank: Int) {
        // Xử lý khi người dùng nhấn vào một item trong bảng xếp hạng
        Toast.makeText(requireContext(), "Hạng $rank: ${student.name} - Điểm TB: ${student.getFormattedAverage()}", Toast.LENGTH_SHORT).show()
        // Có thể điều hướng đến trang chi tiết học sinh nếu muốn
    }
    // --- ---

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}