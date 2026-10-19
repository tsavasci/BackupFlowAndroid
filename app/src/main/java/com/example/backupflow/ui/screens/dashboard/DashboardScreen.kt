package com.example.backupflow.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.backupflow.R
import com.example.backupflow.data.local.BackupTaskEntity
import com.example.backupflow.domain.model.BackupType
import com.example.backupflow.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onNavigateToNewTask: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_backupflow_logo),
                            contentDescription = "Logo",
                            tint = Color.Unspecified,
                            modifier = Modifier.size(32.dp)
                        )
                        Text(
                            text = "BackupFlow",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 22.sp,
                            color = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkBg
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToNewTask,
                shape = CircleShape,
                containerColor = Color.Transparent,
                contentColor = Color.White,
                modifier = Modifier
                    .size(60.dp)
                    .background(PrimaryGradient, CircleShape)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Task", modifier = Modifier.size(28.dp))
            }
        },
        containerColor = DarkBg
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            item {
                HeroStatusCard()
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Saved Backup Tasks",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "${uiState.tasks.size} Active",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }

            if (uiState.tasks.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, DarkBorder, RoundedCornerShape(20.dp)),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkSurface)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "No tasks configured yet",
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Tap the + button to create your first automated cloud-free backup.",
                                fontSize = 13.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }
            } else {
                items(uiState.tasks) { task ->
                    TaskCard(
                        task = task,
                        onRunNow = { viewModel.triggerTask(task.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun HeroStatusCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(HeroCardGradient)
            .border(1.dp, CardBorderGradient, RoundedCornerShape(24.dp))
            .padding(20.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(NeonGreen, CircleShape)
                    )
                    Text(
                        text = "SYSTEM STATUS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonGreen,
                        letterSpacing = 1.sp
                    )
                }
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Safe",
                    tint = NeonGreen,
                    modifier = Modifier.size(20.dp)
                )
            }

            Text(
                text = "Protected & Ready to Sync",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Text(
                text = "Zero 3rd-party clouds. Direct encryption over local Wi-Fi or VPN to your private server.",
                fontSize = 13.sp,
                color = TextSecondary,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
fun TaskCard(
    task: BackupTaskEntity,
    onRunNow: () -> Unit
) {
    val protocolBrush = when (task.backupType) {
        BackupType.SFTP -> PrimaryGradient
        BackupType.WEBDAV -> SecondaryGradient
        BackupType.FTP -> Brush.horizontalGradient(listOf(AmberWarning, Color(0xFFF97316)))
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(DarkSurface)
            .border(1.dp, DarkBorder, RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(protocolBrush)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = task.backupType.name,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }

                    Text(
                        text = task.displayName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = TextPrimary
                    )
                }

                Text(
                    text = "${task.serverHost}:${task.serverPort}${task.destinationPath}",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }

            IconButton(
                onClick = onRunNow,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(DarkSurfaceElevated)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Run Now",
                    tint = CyanAccent,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
