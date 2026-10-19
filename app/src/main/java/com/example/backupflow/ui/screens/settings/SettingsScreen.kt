package com.example.backupflow.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text("Execution Conditions", style = MaterialTheme.typography.titleMedium)

            Column {
                Text("Minimum Battery Level: ${uiState.minimumBatteryThreshold}%")
                Slider(
                    value = uiState.minimumBatteryThreshold.toFloat(),
                    onValueChange = { viewModel.updateBatteryThreshold(it.toInt()) },
                    valueRange = 10f..90f,
                    steps = 8
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Require External Power (Charging)")
                Switch(
                    checked = uiState.requireExternalPower,
                    onCheckedChange = { viewModel.updateRequireExternalPower(it) }
                )
            }

            HorizontalDivider()

            Text("Data Management", style = MaterialTheme.typography.titleMedium)

            Button(
                onClick = { viewModel.resetAllData() },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Reset All Data")
            }
        }
    }
}
