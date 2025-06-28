package com.example.urbanmaintenancemanager.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.urbanmaintenancemanager.R
import com.example.urbanmaintenancemanager.data.datastore.DarkThemeConfig
import com.example.urbanmaintenancemanager.ui.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(mainViewModel: MainViewModel) {
    val uiState by mainViewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(uiState.toastMessage) {
        uiState.toastMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            mainViewModel.clearToastMessage()
        }
    }

    val backupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/octet-stream"),
        onResult = { uri ->
            uri?.let { mainViewModel.backupDatabase(it) }
        }
    )

    val restoreLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            uri?.let { mainViewModel.restoreDatabase(it) }
        }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.title_settings)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                SettingsGroup(title = stringResource(R.string.appearance_settings)) {
                    ThemeSettings(
                        selectedTheme = uiState.darkThemeConfig,
                        onThemeChange = { mainViewModel.setDarkThemeConfig(it) }
                    )
                    Divider(modifier = Modifier.padding(horizontal = 16.dp))
                    LanguageSettings(
                        selectedLanguage = uiState.language,
                        onLanguageChange = { mainViewModel.setAppLanguage(it) }
                    )
                }
            }

            item {
                SettingsGroup(title = stringResource(R.string.data_management)) {
                    BackupRestoreSettings(
                        onBackup = { backupLauncher.launch("urban-maintenance-backup.db") },
                        onRestore = { restoreLauncher.launch(arrayOf("application/octet-stream")) }
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsGroup(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
        )
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                content()
            }
        }
    }
}

@Composable
fun ColumnScope.BackupRestoreSettings(onBackup: () -> Unit, onRestore: () -> Unit) {
    SettingItem(
        title = stringResource(R.string.backup_data),
        onClick = onBackup
    )
    Divider(modifier = Modifier.padding(horizontal = 16.dp))
    SettingItem(
        title = stringResource(R.string.restore_data),
        onClick = onRestore
    )
}

@Composable
fun ColumnScope.ThemeSettings(
    selectedTheme: DarkThemeConfig,
    onThemeChange: (DarkThemeConfig) -> Unit
) {
    val themes = listOf(
        DarkThemeConfig.FOLLOW_SYSTEM to stringResource(R.string.theme_system),
        DarkThemeConfig.LIGHT to stringResource(R.string.theme_light),
        DarkThemeConfig.DARK to stringResource(R.string.theme_dark)
    )
    Column(Modifier.selectableGroup()) {
        themes.forEach { (theme, label) ->
            RadioSettingItem(
                label = label,
                selected = selectedTheme == theme,
                onClick = { onThemeChange(theme) }
            )
        }
    }
}

@Composable
fun ColumnScope.LanguageSettings(
    selectedLanguage: String,
    onLanguageChange: (String) -> Unit
) {
    val languages = listOf(
        "fa" to stringResource(R.string.persian),
        "en" to stringResource(R.string.english)
    )
    Column(Modifier.selectableGroup()) {
        languages.forEach { (code, name) ->
            RadioSettingItem(
                label = name,
                selected = selectedLanguage == code,
                onClick = { onLanguageChange(code) }
            )
        }
    }
}

@Composable
fun SettingItem(
    title: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
fun RadioSettingItem(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .height(56.dp)
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.RadioButton
            )
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = null // Recommended for accessibility
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(start = 16.dp)
        )
    }
} 