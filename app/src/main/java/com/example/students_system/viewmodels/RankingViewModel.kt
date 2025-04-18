package com.example.students_system.viewmodels // Tạo package mới nếu cần

import android.app.Application
import androidx.lifecycle.*
import com.example.students_system.data.db.StudentDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RankingViewModel(application: Application) : AndroidViewModel(application) {

    private val studentDao = StudentDao(application)

    private val _classList = MutableLiveData<List<String>>()
    val classList: LiveData<List<String>> = _classList

    init {
        loadClassList()
    }

    fun loadClassList() {
        viewModelScope.launch {
            val distinctClasses = withContext(Dispatchers.IO) {
                studentDao.getDistinctClassNames() // Dùng lại hàm từ StudentDao
            }
            _classList.value = distinctClasses
        }
    }
}