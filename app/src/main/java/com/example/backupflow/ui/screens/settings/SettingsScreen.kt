package com.example.backupflow.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.backupflow.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold, color = TextPrimary) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBg)
            )
        },
        containerColor = DarkBg
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text("Execution Conditions", style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.Bold)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(DarkSurface)
                    .border(1.dp, DarkBorder, RoundedCornerShape(20.dp))
                    .padding(20.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Minimum Battery Level", fontSize = 14.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
                            Text("${uiState.minimumBatteryThreshold}%", fontSize = 14.sp, color = CyanAccent, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Slider(
                            value = uiState.minimumBatteryThreshold.toFloat(),
                            onValueChange = { viewModel.updateBatteryThreshold(it.toInt()) },
                            valueRange = 10f..90f,
                            steps = 8,
                            colors = SliderDefaults.colors(
                                thumbColor = CyanAccent,
                                activeTrackColor = ElectricBlue,
                                inactiveTrackColor = DarkSurfaceElevated
                            )
                        )
                    }

                    HorizontalDivider(color = DarkBorder)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Require External Power", fontSize = 14.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
                            Text("Only run backups when charger is connected", fontSize = 12.sp, color = TextSecondary)
                        }
                        Switch(
                            checked = uiState.requireExternalPower,
                            onCheckedChange = { viewModel.updateRequireExternalPower(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = ElectricBlue,
                                uncheckedThumbColor = TextMuted,
                                uncheckedTrackColor = DarkSurfaceElevated
                            )
                        )
                    }
                }
            }

            Text("Data Management", style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.Bold)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(DarkSurface)
                    .border(1.dp, DarkBorder, RoundedCornerShape(20.dp))
                    .padding(20.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Clear All Application Data", fontSize = 14.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
                    Text("Deletes all saved tasks, credentials, and local database history.", fontSize = 12.sp, color = TextSecondary)

                    Spacer(modifier = Modifier.height(4.dp))

                    Button(
                        onClick = { viewModel.resetAllData() },
                        colors = ButtonDefaults.buttonColors(containerColor = RoseError.copy(alpha = 0.2f), contentColor = RoseError),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Reset All Data", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
