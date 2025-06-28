package com.example.urbanmaintenancemanager.ui

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.urbanmaintenancemanager.data.datastore.DarkThemeConfig
import com.example.urbanmaintenancemanager.data.datastore.UserPreferencesRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import android.net.Uri
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow

class MainViewModel(
    private val application: Application,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    val uiState: StateFlow<MainUiState> =
        combine(
            userPreferencesRepository.darkThemeConfig,
            userPreferencesRepository.appLanguage
        ) { themeConfig, language ->
            MainUiState(
                darkThemeConfig = themeConfig,
                language = language
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = MainUiState()
        )

    private val _toastMessages = MutableSharedFlow<String>()
    val toastMessages = _toastMessages.asSharedFlow()

    private val _events = Channel<MainEvent>()
    val events = _events.receiveAsFlow()

    fun setDarkThemeConfig(darkThemeConfig: DarkThemeConfig) {
        viewModelScope.launch {
            userPreferencesRepository.setDarkThemeConfig(darkThemeConfig)
        }
    }

    fun setAppLanguage(languageCode: String) {
        viewModelScope.launch {
            userPreferencesRepository.setAppLanguage(languageCode)
            _events.send(MainEvent.RecreateActivity)
        }
    }

    fun backupDatabase(uri: Uri) {
        viewModelScope.launch {
            val dbFile = application.getDatabasePath("urban_maintenance_manager_db")
            try {
                application.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    dbFile.inputStream().use { inputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
                _toastMessages.emit("پشتیبان‌گیری با موفقیت انجام شد.")
            } catch (e: IOException) {
                e.printStackTrace()
                _toastMessages.emit("پشتیبان‌گیری با شکست مواجه شد.")
            }
        }
    }

    fun restoreDatabase(uri: Uri) {
        viewModelScope.launch {
            val dbFile = application.getDatabasePath("urban_maintenance_manager_db")
            try {
                application.contentResolver.openInputStream(uri)?.use { inputStream ->
                    dbFile.outputStream().use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
                _toastMessages.emit("بازیابی با موفقیت انجام شد. لطفاً برنامه را مجدداً راه‌اندازی کنید.")
            } catch (e: IOException) {
                e.printStackTrace()
                _toastMessages.emit("بازیابی با شکست مواجه شد.")
            }
        }
    }
}

data class MainUiState(
    val darkThemeConfig: DarkThemeConfig = DarkThemeConfig.FOLLOW_SYSTEM,
    val language: String = "fa"
)

class MainViewModelFactory(
    private val application: Application
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            val userPreferencesRepository = UserPreferencesRepository(application.applicationContext)
            return MainViewModel(application, userPreferencesRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

sealed class MainEvent {
    object RecreateActivity : MainEvent()
} 