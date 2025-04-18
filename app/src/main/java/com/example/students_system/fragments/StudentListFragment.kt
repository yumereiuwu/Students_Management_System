package com.example.students_system.fragments // Thay package nếu cần

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log // Thêm Log
import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.appcompat.widget.SearchView // Import đúng
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope // Đảm bảo đã thêm dependency lifecycle-runtime-ktx
import androidx.navigation.fragment.navArgs // Import nếu dùng SafeArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.students_system.R
import com.example.students_system.activities.AddEditStudentActivity
import com.example.students_system.adapters.OnStudentClickListener
import com.example.students_system.adapters.StudentAdapter
import com.example.students_system.data.model.Student
import com.example.students_system.databinding.FragmentStudentListBinding
import com.example.students_system.utils.EmailUtil
import com.example.students_system.viewmodels.SortType
import com.example.students_system.viewmodels.StudentListViewModel // Import ViewModel
import kotlinx.coroutines.launch // Import launch
import java.util.Locale

class StudentListFragment : Fragment(), OnStudentClickListener {

    private var _binding: FragmentStudentListBinding? = null
    private val binding get() = _binding!! // Đảm bảo không gọi sau onDestroyView

    private lateinit var studentAdapter: StudentAdapter
    private lateinit var viewModel: StudentListViewModel

    private var classListAdapter: ArrayAdapter<String>? = null
    private var isInitialSpinnerSelection = true // Cờ cho Spinner

    // Lấy argument (ví dụ dùng Safe Args - cần setup plugin và nav graph)
    // private val args: StudentListFragmentArgs by navArgs()
    // Hoặc lấy thủ công

    private var initialClassName: String? = null

    private val addEditStudentLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                viewModel.refreshData() // Yêu cầu ViewModel tải lại dữ liệu
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Lấy argument nếu lấy thủ công
        initialClassName = arguments?.getString("className")
        Log.d("StudentListFragment", "Received initialClassName argument: $initialClassName") // Log để kiểm tra
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d("StudentListFragment", "onCreateView called")
        _binding = FragmentStudentListBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this).get(StudentListViewModel::class.java)
        setupRecyclerView()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("StudentListFragment", "onViewCreated called")

        // val classNameFromArgs = args.className // Nếu dùng SafeArgs
        val classNameFromArgs = initialClassName

        if (classNameFromArgs != null) {
            Log.d("StudentListFragment", "Mode: Viewing specific class ($classNameFromArgs)")
            // Chế độ xem chi tiết lớp
            binding.spinnerClassFilter.visibility = View.GONE
            binding.searchViewStudent.visibility = View.GONE
            binding.buttonSortOptions.visibility = View.GONE
            binding.textViewClassAverage.visibility = View.VISIBLE // Sẽ được cập nhật bởi observer
            (activity as? AppCompatActivity)?.supportActionBar?.title = "Lớp $classNameFromArgs"
            // Yêu cầu ViewModel lọc ban đầu (ViewModel sẽ tự load khi observer được gắn)
            viewModel.applyFiltersAndSort(className = classNameFromArgs, sortType = SortType.AVG_DESC)
        } else {
            Log.d("StudentListFragment", "Mode: Viewing all students")
            // Chế độ xem tổng quát
            binding.spinnerClassFilter.visibility = View.VISIBLE
            binding.searchViewStudent.visibility = View.VISIBLE
            binding.buttonSortOptions.visibility = View.VISIBLE
            binding.textViewClassAverage.visibility = View.GONE
            (activity as? AppCompatActivity)?.supportActionBar?.title = getString(R.string.title_student_list)
            setupSpinner() // Chỉ setup khi cần
            setupSearchView()
            setupSortButton()
            // ViewModel tự load mặc định trong init, không cần gọi apply ở đây
        }

        setupFabListener()
        observeViewModel() // Quan trọng: Bắt đầu observe sau khi UI đã sẵn sàng
    }

    private fun setupRecyclerView() {
        studentAdapter = StudentAdapter(this)
        binding.recyclerViewStudents.apply {
            adapter = studentAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun setupSpinner() {
        Log.d("StudentListFragment", "setupSpinner called")
        isInitialSpinnerSelection = true // Reset cờ khi setup
        classListAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, mutableListOf())
        classListAdapter?.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerClassFilter.adapter = classListAdapter

        binding.spinnerClassFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (isInitialSpinnerSelection) {
                    isInitialSpinnerSelection = false
                    // Logic kiểm tra và bỏ qua nếu không thay đổi selection so với trạng thái ViewModel
                    val selectedClass = parent?.getItemAtPosition(position) as? String
                    val currentFilter = viewModel.getCurrentClassNameFilter() ?: if (classListAdapter?.count ?: 0 > 0 && position == 0) "Tất cả lớp" else null
                    if (selectedClass == currentFilter || (selectedClass == "Tất cả lớp" && currentFilter == null)) {
                        Log.d("StudentListFragment", "Spinner initial selection or no change, skipping filter apply.")
                        return
                    }
                }

                val selectedClass = parent?.getItemAtPosition(position) as? String
                Log.d("StudentListFragment", "Spinner item selected: $selectedClass")
                viewModel.applyFiltersAndSort(className = selectedClass)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) { /* Không cần làm gì */ }
        }
    }

    private fun setupSearchView() {
        binding.searchViewStudent.setOnQueryTextListener(object : SearchView.OnQueryTextListener { // Chỉ cần interface này
            override fun onQueryTextSubmit(query: String?): Boolean {
                Log.d("StudentListFragment", "Search submitted: $query")
                viewModel.applyFiltersAndSort(searchQuery = query ?: "")
                binding.searchViewStudent.clearFocus()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                Log.d("StudentListFragment", "Search text changed: $newText")
                viewModel.applyFiltersAndSort(searchQuery = newText ?: "")
                return true
            }
        })
    }

    private fun setupSortButton() {
        binding.buttonSortOptions.setOnClickListener { view ->
            val popup = PopupMenu(requireContext(), view)
            try {
                popup.menuInflater.inflate(R.menu.sort_options_menu, popup.menu)
            } catch (e: Exception){
                Toast.makeText(requireContext(), "Lỗi menu sắp xếp", Toast.LENGTH_SHORT).show()
                Log.e("StudentListFragment", "Error inflating sort menu", e)
                return@setOnClickListener
            }

            val currentSort = viewModel.getCurrentSortType() // Lấy từ ViewModel
            Log.d("StudentListFragment", "Current sort type for menu check: $currentSort")
            try {
                when (currentSort) {
                    SortType.NAME_ASC -> popup.menu.findItem(R.id.sort_by_name_asc)?.isChecked = true
                    SortType.AVG_DESC -> popup.menu.findItem(R.id.sort_by_avg_desc)?.isChecked = true
                    SortType.AVG_ASC -> popup.menu.findItem(R.id.sort_by_avg_asc)?.isChecked = true
                }
            } catch (e: Exception) {
                Log.e("StudentListFragment", "Error checking sort menu item", e)
            }


            popup.setOnMenuItemClickListener { menuItem ->
                Log.d("StudentListFragment", "Sort option clicked: ${menuItem.title}")
                val newSortType = when (menuItem.itemId) {
                    R.id.sort_by_name_asc -> SortType.NAME_ASC
                    R.id.sort_by_avg_desc -> SortType.AVG_DESC
                    R.id.sort_by_avg_asc -> SortType.AVG_ASC
                    else -> viewModel.getCurrentSortType() // Giữ nguyên
                }
                Log.d("StudentListFragment", "Applying new sort type: $newSortType")
                viewModel.applyFiltersAndSort(sortType = newSortType)
                true
            }
            popup.show()
        }
    }

    private fun setupFabListener() {
        binding.fabAddStudent.setOnClickListener {
            val intent = Intent(requireContext(), AddEditStudentActivity::class.java)
            // val classNameFromArgs = args.className // Nếu dùng SafeArgs
            val classNameFromArgs = initialClassName
            classNameFromArgs?.let { currentClass ->
                // intent.putExtra("prefill_class_name", currentClass)
                Log.d("StudentListFragment", "Launching AddEditStudentActivity, prefilling class: $currentClass")
            }
                ?: Log.d("StudentListFragment", "Launching AddEditStudentActivity without prefilling class")
            addEditStudentLauncher.launch(intent)
        }
    }
    private var isInitialSpinnerSetup = true
    private fun observeViewModel() {
        Log.d("StudentListFragment", "Observing ViewModel LiveData")
        viewModel.classList.observe(viewLifecycleOwner) { classes ->
            Log.d("StudentListFragment", "Class list updated: $classes")
            // Chỉ cập nhật spinner nếu nó đang hiển thị (chế độ xem tổng quát)
            if (binding.spinnerClassFilter.visibility == View.VISIBLE) {
                isInitialSpinnerSetup = true // Reset cờ khi dữ liệu mới đến
                val currentSelection = binding.spinnerClassFilter.selectedItem as? String // Lưu lựa chọn hiện tại

                classListAdapter?.clear()
                if (classes != null) {
                    classListAdapter?.addAll(classes) // Thêm danh sách mới
                }
                classListAdapter?.notifyDataSetChanged() // Quan trọng

                // Cố gắng khôi phục lựa chọn cũ
                if (currentSelection != null) {
                    val position = classListAdapter?.getPosition(currentSelection) ?: -1
                    if (position >= 0) {
                        binding.spinnerClassFilter.setSelection(position, false) // false để không trigger listener
                        isInitialSpinnerSetup = false // Đã khôi phục, lần chọn sau là của người dùng
                        Log.d("StudentListFragment", "Restored spinner selection to: $currentSelection")
                    } else {
                        Log.d("StudentListFragment", "Could not restore spinner selection: $currentSelection")
                    }
                } else if (classListAdapter?.count ?: 0 > 0) {
                    binding.spinnerClassFilter.setSelection(0, false) // Chọn "Tất cả lớp"
                    isInitialSpinnerSetup = false
                    Log.d("StudentListFragment", "Set spinner selection to default (All classes)")
                }
            }
        }

        viewModel.filteredStudents.observe(viewLifecycleOwner) { students ->
            Log.d("StudentListFragment", "Filtered students updated, count: ${students?.size ?: 0}")
            val query = viewModel.getCurrentSearchQuery()
            val className = viewModel.getCurrentClassNameFilter()
            if (students.isNullOrEmpty()) {
                binding.recyclerViewStudents.visibility = View.GONE
                binding.textViewEmptyList.visibility = View.VISIBLE
                binding.textViewEmptyList.text = when {
                    query.isNotEmpty() -> "Không tìm thấy học sinh với từ khóa '$query'"
                    className != null -> "Không có học sinh trong lớp $className"
                    else -> "Chưa có học sinh nào. Nhấn + để thêm." // Sửa text mặc định
                }
            } else {
                binding.recyclerViewStudents.visibility = View.VISIBLE
                binding.textViewEmptyList.visibility = View.GONE
            }
            studentAdapter.submitList(students) // ListAdapter xử lý diffing hiệu quả
        }

        viewModel.selectedClassAverage.observe(viewLifecycleOwner) { average ->
            // val classNameFromArgs = args.className // Nếu dùng SafeArgs
            val classNameFromArgs = initialClassName
            Log.d("StudentListFragment", "Selected class average updated: $average for class $classNameFromArgs")
            if (average != null && classNameFromArgs != null) {
                binding.textViewClassAverage.visibility = View.VISIBLE
                binding.textViewClassAverage.text = String.format(Locale.getDefault(), "Điểm TB lớp %s: %.2f", classNameFromArgs, average)
            } else {
                // Nếu đang ở chế độ xem chi tiết lớp mà average là null (lớp trống), có thể hiện TB = 0
                if(classNameFromArgs != null) {
                    binding.textViewClassAverage.visibility = View.VISIBLE
                    binding.textViewClassAverage.text = String.format(Locale.getDefault(), "Điểm TB lớp %s: 0.00", classNameFromArgs)
                } else {
                    // Ẩn nếu xem tất cả lớp
                    binding.textViewClassAverage.visibility = View.GONE
                }
            }
        }
    }

    // --- Implement OnStudentClickListener ---
    override fun onEditClick(student: Student) {
        Log.d("StudentListFragment", "Edit clicked for student ID: ${student.id}")
        val intent = Intent(requireContext(), AddEditStudentActivity::class.java)
        intent.putExtra(AddEditStudentActivity.EXTRA_STUDENT, student)
        addEditStudentLauncher.launch(intent)
    }

    override fun onDeleteClick(student: Student) {
        Log.d("StudentListFragment", "Delete clicked for student ID: ${student.id}")
        AlertDialog.Builder(requireContext())
            .setTitle("Xác nhận xóa")
            .setMessage("Bạn có chắc chắn muốn xóa học sinh ${student.name}?")
            .setIcon(R.drawable.ic_delete)
            .setPositiveButton("Xóa") { _, _ ->
                Log.d("StudentListFragment", "Confirming delete for student ID: ${student.id}")
                viewModel.deleteStudent(student.id) // Gọi ViewModel để xóa
                // Thông báo Toast có thể chuyển vào ViewModel hoặc để Fragment tự xử lý sau khi observe trạng thái xóa
                Toast.makeText(context, "Đang xóa ${student.name}...", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    override fun onSendEmailClick(student: Student) {
        Log.d("StudentListFragment", "Send email clicked for student ID: ${student.id}")
        EmailUtil.sendStudentResultEmail(requireContext(), student)
    }
    // --- ---

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d("StudentListFragment", "onDestroyView called")
        binding.recyclerViewStudents.adapter = null // Quan trọng: Gỡ adapter
        _binding = null // Quan trọng: Clear binding reference
    }
}