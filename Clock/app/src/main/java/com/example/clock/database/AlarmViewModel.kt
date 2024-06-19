package com.example.clock.database
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class AlarmViewModel(private val repository: AlarmRepository) : ViewModel() {

    val allAlarms: LiveData<List<Alarm>> = repository.allAlarms

    fun insert(alarm: Alarm) {
        viewModelScope.launch {
            repository.insert(alarm)
        }
    }

    fun update(alarm: Alarm) {
        viewModelScope.launch {
            repository.update(alarm)
        }
    }

    fun delete(name: String, hour: Int, minute: Int) {
        viewModelScope.launch {
            repository.delete(name, hour, minute)
        }
    }

    class AlarmViewModelFactory(private val repository: AlarmRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AlarmViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return AlarmViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
