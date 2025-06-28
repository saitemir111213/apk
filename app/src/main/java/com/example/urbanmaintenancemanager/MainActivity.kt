package com.example.urbanmaintenancemanager

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.urbanmaintenancemanager.ui.MainEvent
import com.example.urbanmaintenancemanager.ui.MainViewModel
import com.example.urbanmaintenancemanager.ui.MainViewModelFactory
import com.example.urbanmaintenancemanager.ui.screens.AnalyticsScreen
import com.example.urbanmaintenancemanager.ui.screens.DailyReportScreen
import com.example.urbanmaintenancemanager.ui.screens.ExportScreen
import com.example.urbanmaintenancemanager.ui.screens.SettingsScreen
import com.example.urbanmaintenancemanager.ui.screens.WorkerProfilesScreen
import com.example.urbanmaintenancemanager.ui.screens.AbsenceScreen
import com.example.urbanmaintenancemanager.ui.theme.UrbanMaintenanceManagerTheme
import java.util.Locale

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels {
        MainViewModelFactory(application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val currentLanguage = viewModel.uiState.value.language
        setLocale(currentLanguage)

        setContent {
            val uiState by viewModel.uiState.collectAsState()

            LaunchedEffect(Unit) {
                viewModel.events.collect { event ->
                    when (event) {
                        is MainEvent.RecreateActivity -> {
                            recreate()
                        }
                    }
                }
            }

            val layoutDirection = if (uiState.language == "fa") LayoutDirection.Rtl else LayoutDirection.Ltr
            CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
                UrbanMaintenanceManagerTheme(darkThemeConfig = uiState.darkThemeConfig) {
                    MainScreen(viewModel)
                }
            }
        }
    }

    private fun setLocale(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = Configuration(resources.configuration)
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
    }
}

sealed class Screen(val route: String, val resourceId: Int, val icon: Int) {
    object DailyReport : Screen("daily_report", R.string.title_daily_report, R.drawable.ic_report)
    object WorkerProfiles : Screen("worker_profiles", R.string.title_worker_profiles, R.drawable.ic_person)
    object Absences : Screen("absences", R.string.absences, R.drawable.ic_person)
    object Analytics : Screen("analytics", R.string.title_analytics, R.drawable.ic_analytics)
    object Settings : Screen("settings", R.string.title_settings, R.drawable.ic_analytics)
    object Export : Screen("export", R.string.title_export, R.drawable.ic_report)
}

val items = listOf(
    Screen.DailyReport,
    Screen.WorkerProfiles,
    Screen.Absences,
    Screen.Analytics,
    Screen.Settings,
    Screen.Export,
)

@Composable
fun MainScreen(viewModel: MainViewModel) {
    val navController = rememberNavController()
    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                items.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(painterResource(id = screen.icon), contentDescription = null) },
                        label = { Text(stringResource(screen.resourceId)) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(navController, startDestination = Screen.DailyReport.route, Modifier.padding(innerPadding)) {
            composable(Screen.DailyReport.route) { DailyReportScreen() }
            composable(Screen.WorkerProfiles.route) { WorkerProfilesScreen() }
            composable(Screen.Absences.route) { AbsenceScreen() }
            composable(Screen.Analytics.route) { AnalyticsScreen() }
            composable(Screen.Settings.route) { SettingsScreen(viewModel) }
            composable(Screen.Export.route) { ExportScreen() }
        }
    }
} 