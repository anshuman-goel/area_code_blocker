package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.BlockerApplication
import com.example.data.entity.BlockedAreaCode
import com.example.data.entity.BlockedLog
import com.example.data.entity.BlockedKeyword
import com.example.data.repository.BlockerRepository
import com.example.util.PhoneUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import android.content.Context

class BlockerViewModel(
    application: Application,
    private val repository: BlockerRepository
) : AndroidViewModel(application) {

    val blockedAreaCodes: StateFlow<List<BlockedAreaCode>> = repository.allBlockedAreaCodes
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val blockedLogs: StateFlow<List<BlockedLog>> = repository.allBlockedLogs
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val blockedKeywords: StateFlow<List<BlockedKeyword>> = repository.allBlockedKeywords
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _inputPhoneNumber = MutableStateFlow("")
    val inputPhoneNumber: StateFlow<String> = _inputPhoneNumber.asStateFlow()

    private val _extractedAreaCode = MutableStateFlow("")
    val extractedAreaCode: StateFlow<String> = _extractedAreaCode.asStateFlow()

    private val _userOwnNumber = MutableStateFlow("")
    val userOwnNumber: StateFlow<String> = _userOwnNumber.asStateFlow()

    private val _appTheme = MutableStateFlow("System")
    val appTheme: StateFlow<String> = _appTheme.asStateFlow()

    init {
        val prefs = application.getSharedPreferences("blocker_prefs", android.content.Context.MODE_PRIVATE)
        _userOwnNumber.value = prefs.getString("user_own_number", "") ?: ""
        _appTheme.value = prefs.getString("app_theme", "System") ?: "System"
    }

    fun updateInputPhoneNumber(number: String) {
        _inputPhoneNumber.value = number
        _extractedAreaCode.value = if (number.isNotBlank()) PhoneUtils.extractAreaCode(number) else ""
    }

    fun setUserOwnNumber(number: String) {
        _userOwnNumber.value = number
        val prefs = getApplication<Application>().getSharedPreferences("blocker_prefs", android.content.Context.MODE_PRIVATE)
        prefs.edit().putString("user_own_number", number).apply()
    }

    fun tryAutoDetectPhoneNumber(context: Context): Boolean {
        val detected = PhoneUtils.getDevicePhoneNumber(context)
        return if (!detected.isNullOrBlank()) {
            setUserOwnNumber(detected)
            true
        } else {
            false
        }
    }

    fun setAppTheme(theme: String) {
        _appTheme.value = theme
        val prefs = getApplication<Application>().getSharedPreferences("blocker_prefs", android.content.Context.MODE_PRIVATE)
        prefs.edit().putString("app_theme", theme).apply()
    }

    fun addAreaCode(areaCode: String) {
        val clean = areaCode.trim().filter { it.isDigit() }
        if (clean.isNotEmpty()) {
            viewModelScope.launch {
                repository.insertAreaCode(clean)
            }
        }
    }

    fun removeAreaCode(areaCode: String) {
        viewModelScope.launch {
            repository.deleteAreaCode(areaCode)
        }
    }

    fun addKeyword(keyword: String) {
        val clean = keyword.trim().lowercase()
        if (clean.isNotEmpty()) {
            viewModelScope.launch {
                repository.insertKeyword(clean)
            }
        }
    }

    fun removeKeyword(keyword: String) {
        viewModelScope.launch {
            repository.deleteKeyword(keyword)
        }
    }

    fun deleteLogById(id: Int) {
        viewModelScope.launch {
            repository.deleteLogById(id)
        }
    }

    fun clearAllLogs() {
        viewModelScope.launch {
            repository.clearLogs()
        }
    }

    /**
     * Helper to check the current user's number's area code as suggestions
     */
    fun extractUserOwnAreaCode(): String {
        return if (_userOwnNumber.value.isNotBlank()) {
            PhoneUtils.extractAreaCode(_userOwnNumber.value)
        } else {
            ""
        }
    }
}

class BlockerViewModelFactory(
    private val application: Application,
    private val repository: BlockerRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BlockerViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BlockerViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
