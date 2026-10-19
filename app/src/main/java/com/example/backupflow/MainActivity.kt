package com.example.backupflow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.backupflow.ui.screens.dashboard.DashboardScreen
import com.example.backupflow.ui.screens.dashboard.DashboardViewModel
import com.example.backupflow.ui.screens.settings.SettingsScreen
import com.example.backupflow.ui.screens.settings.SettingsViewModel
import com.example.backupflow.ui.screens.taskform.BackupTaskFormScreen
import com.example.backupflow.ui.screens.taskform.TaskFormViewModel
import com.example.backupflow.ui.theme.*
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BackupFlowTheme {
                BackupFlowApp()
            }
        }
    }
}

enum class AppDestination(val label: String, val icon: ImageVector) {
    DASHBOARD("Dashboard", Icons.Default.Home),
    SETTINGS("Settings", Icons.Default.Settings)
}

@Composable
fun BackupFlowApp() {
    var currentDestination by rememberSaveable { mutableStateOf(AppDestination.DASHBOARD) }
    var showNewTaskForm by rememberSaveable { mutableStateOf(false) }

    if (showNewTaskForm) {
        val formViewModel: TaskFormViewModel = hiltViewModel()
        BackupTaskFormScreen(
            viewModel = formViewModel,
            onNavigateBack = { showNewTaskForm = false }
        )
    } else {
        NavigationSuiteScaffold(
            navigationSuiteItems = {
                AppDestination.entries.forEach { destination ->
                    val isSelected = destination == currentDestination
                    item(
                        icon = {
                            Icon(
                                destination.icon,
                                contentDescription = destination.label,
                                tint = if (isSelected) CyanAccent else TextSecondary
                            )
                        },
                        label = {
                            Text(
                                destination.label,
                                color = if (isSelected) TextPrimary else TextSecondary
                            )
                        },
                        selected = isSelected,
                        onClick = { currentDestination = destination }
                    )
                }
            },
            containerColor = DarkBg,
            contentColor = TextPrimary,
            modifier = Modifier.fillMaxSize()
        ) {
            when (currentDestination) {
                AppDestination.DASHBOARD -> {
                    val dashboardViewModel: DashboardViewModel = hiltViewModel()
                    DashboardScreen(
                        viewModel = dashboardViewModel,
                        onNavigateToNewTask = { showNewTaskForm = true }
                    )
                }
                AppDestination.SETTINGS -> {
                    val settingsViewModel: SettingsViewModel = hiltViewModel()
                    SettingsScreen(viewModel = settingsViewModel)
                }
            }
        }
    }
}
