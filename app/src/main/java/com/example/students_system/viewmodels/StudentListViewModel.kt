package com.example.students_system.viewmodels // Tạo package mới nếu cần

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.example.students_system.data.db.StudentDao
import com.example.students_system.data.model.Student
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// Enum định nghĩa các kiểu sắp xếp
enum class SortType {
    NAME_ASC, AVG_DESC, AVG_ASC
}

class StudentListViewModel(application: Application) : AndroidViewModel(application) {

    private val studentDao = StudentDao(application) // Dùng Application Context

    // LiveData chứa danh sách lớp học để đổ vào Spinner
    private val _classList = MutableLiveData<List<String>>()
    val classList: LiveData<List<String>> = _classList

    // LiveData chứa danh sách học sinh đã được lọc và sắp xếp
    private val _filteredStudents = MutableLiveData<List<Student>>()
    val filteredStudents: LiveData<List<Student>> = _filteredStudents

    // LiveData cho điểm trung bình của lớp được chọn (hoặc null nếu xem tất cả)
    private val _selectedClassAverage = MutableLiveData<Double?>()
    val selectedClassAverage: LiveData<Double?> = _selectedClassAverage

    // Trạng thái hiện tại
    private var currentClassFilter: String? = null // null nghĩa là xem tất cả
    private var currentSearchQuery: String = ""
    private val _currentSortType = MutableLiveData(SortType.NAME_ASC) // Mặc định sắp xếp theo tên
    val currentSortType: LiveData<SortType> = _currentSortType
    init {
        loadClassList() // Tải danh sách lớp khi ViewModel được tạo
        applyFiltersAndSort() // Tải danh sách học sinh ban đầu
    }
    fun getCurrentClassNameFilter(): String? {
        return currentClassFilter
    }
    fun getCurrentSearchQuery(): String {
        return currentSearchQuery
    }

    // Tải danh sách các lớp học duy nhất từ DB
    private fun loadClassList() {
        viewModelScope.launch {
            val distinctClasses = withContext(Dispatchers.IO) {
                // Cần thêm hàm này vào StudentDao
                studentDao.getDistinctClassNames()
            }
            // Thêm tùy chọn "Tất cả lớp" vào đầu danh sách
            _classList.value = listOf("Tất cả lớp") + distinctClasses
        }
    }
    fun getCurrentSortType(): SortType {
        return _currentSortType.value ?: SortType.NAME_ASC // Trả về giá trị hiện tại hoặc mặc định
    }
    // Hàm được gọi khi người dùng thay đổi bộ lọc, tìm kiếm hoặc sắp xếp
    fun applyFiltersAndSort(
        className: String? = currentClassFilter,
        searchQuery: String = currentSearchQuery,
        sortType: SortType = _currentSortType.value ?: SortType.NAME_ASC // Lấy giá trị hiện tại làm mặc định
    ) {
        val effectiveClassName = if (className == "Tất cả lớp") null else className
        val effectiveSearchQuery = searchQuery.trim()

        // Chỉ thực hiện nếu có sự thay đổi bộ lọc hoặc sắp xếp
        if (effectiveClassName == currentClassFilter && effectiveSearchQuery == this.currentSearchQuery && sortType == _currentSortType.value) {
            Log.d("StudentListVM", "Filters and sort unchanged, skipping apply.")
            return
        }

        Log.d("StudentListVM", "Applying filters: Class='${effectiveClassName ?: "All"}', Search='$effectiveSearchQuery', Sort='$sortType'")

        currentClassFilter = effectiveClassName
        this.currentSearchQuery = effectiveSearchQuery
        // --- SỬA DÒNG NÀY ---
        // Cập nhật LiveData nếu giá trị sortType mới khác giá trị hiện tại
        if (_currentSortType.value != sortType) {
            _currentSortType.value = sortType
        }
        // --- KẾT THÚC SỬA ---

        viewModelScope.launch {
            val students = withContext(Dispatchers.IO) {
                try {
                    studentDao.getFilteredStudents(currentClassFilter, currentSearchQuery)
                } catch (e: Exception) {
                    Log.e("StudentListVM", "Error getting filtered students", e)
                    emptyList<Student>()
                }
            }
            Log.d("StudentListVM", "Fetched ${students.size} students matching filters.")

            // Sử dụng giá trị sortType đã được cập nhật hoặc truyền vào
            val finalSortType = _currentSortType.value ?: SortType.NAME_ASC
            val sortedStudents = sortStudents(students, finalSortType)
            _filteredStudents.value = sortedStudents
            calculateSelectedClassAverage(students)
        }
    }
    // Hàm sắp xếp danh sách học sinh
    private fun sortStudents(students: List<Student>, sortType: SortType): List<Student> {
        return when (sortType) {
            SortType.NAME_ASC -> students.sortedBy { it.name.lowercase() }
            SortType.AVG_DESC -> students.sortedByDescending { it.calculateAverage() }
            SortType.AVG_ASC -> students.sortedBy { it.calculateAverage() }
        }
    }

    // Tính điểm TB của danh sách học sinh được truyền vào (thường là danh sách đã lọc theo lớp)
    private fun calculateSelectedClassAverage(studentsInClass: List<Student>) {
        if (currentClassFilter != null && studentsInClass.isNotEmpty()) {
            val average = studentsInClass.sumOf { it.calculateAverage() } / studentsInClass.size
            _selectedClassAverage.value = average
        } else {
            _selectedClassAverage.value = null // Không hiển thị điểm TB nếu xem tất cả hoặc lớp trống
        }
    }
    fun deleteStudent(studentId: Long) {
        viewModelScope.launch {
            // Thực hiện xóa trên background thread
            withContext(Dispatchers.IO) {
                studentDao.deleteStudent(studentId)
            }
            // Sau khi xóa xong, tải lại dữ liệu để cập nhật danh sách
            refreshData()
            // (Tùy chọn) Bạn có thể thêm LiveData để báo trạng thái xóa thành công/thất bại về Fragment
        }
    }
    // Gọi hàm này khi có thay đổi dữ liệu (thêm/sửa/xóa) để tải lại
    fun refreshData() {
        loadClassList() // Cập nhật danh sách lớp nếu cần
        applyFiltersAndSort()
    }
}