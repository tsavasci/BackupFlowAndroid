package com.example.backupflow.ui.screens.taskform

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.backupflow.domain.model.BackupType
import com.example.backupflow.domain.model.MediaType
import com.example.backupflow.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupTaskFormScreen(
    viewModel: TaskFormViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New Backup Task", fontWeight = FontWeight.Bold, color = TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBg)
            )
        },
        containerColor = DarkBg
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            if (uiState.errorMessage != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = RoseError.copy(alpha = 0.2f))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = "Error", tint = RoseError)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = uiState.errorMessage!!,
                            style = MaterialTheme.typography.bodyMedium,
                            color = RoseError,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            CustomTextField(
                value = uiState.displayName,
                onValueChange = { viewModel.updateDisplayName(it) },
                label = "Task Name"
            )

            Text("Protocol Selection", style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(DarkSurface)
                    .border(1.dp, DarkBorder, RoundedCornerShape(16.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                BackupType.entries.forEach { type ->
                    val isSelected = uiState.backupType == type
                    Button(
                        onClick = { viewModel.updateProtocol(type) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSelected) ElectricBlue else Color.Transparent,
                            contentColor = if (isSelected) Color.White else TextSecondary
                        ),
                        contentPadding = PaddingValues(vertical = 10.dp)
                    ) {
                        Text(type.name, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, fontSize = 13.sp)
                    }
                }
            }

            if (uiState.showPlaintextFtpWarning) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = AmberWarning.copy(alpha = 0.15f))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = "Warning", tint = AmberWarning)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "FTP transmits credentials in plaintext. Consider using SFTP or WebDAV over HTTPS for security.",
                            style = MaterialTheme.typography.bodySmall,
                            color = AmberWarning
                        )
                    }
                }
            }

            CustomTextField(
                value = uiState.serverHost,
                onValueChange = { viewModel.updateHost(it) },
                label = "Server Host / IP",
                placeholder = "e.g. 192.168.1.100 or mynas.local"
            )

            CustomTextField(
                value = uiState.serverPort.toString(),
                onValueChange = { viewModel.updatePort(it) },
                label = "Port",
                keyboardType = KeyboardType.Number
            )

            CustomTextField(
                value = uiState.serverUsername,
                onValueChange = { viewModel.updateUsername(it) },
                label = "Username"
            )

            CustomTextField(
                value = uiState.passwordInput,
                onValueChange = { viewModel.updatePassword(it) },
                label = "Password",
                isPassword = true
            )

            CustomTextField(
                value = uiState.destinationPath,
                onValueChange = { viewModel.updateDestinationPath(it) },
                label = "Remote Destination Path"
            )

            Text("Media Type to Sync", style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(DarkSurface)
                    .border(1.dp, DarkBorder, RoundedCornerShape(16.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                MediaType.entries.forEach { mediaType ->
                    val isSelected = uiState.mediaType == mediaType
                    val label = when (mediaType) {
                        MediaType.PHOTOS_ONLY -> "Photos"
                        MediaType.VIDEOS_ONLY -> "Videos"
                        MediaType.PHOTOS_AND_VIDEOS -> "All Media"
                    }
                    Button(
                        onClick = { viewModel.updateMediaType(mediaType) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSelected) PurpleAccent else Color.Transparent,
                            contentColor = if (isSelected) Color.White else TextSecondary
                        ),
                        contentPadding = PaddingValues(vertical = 10.dp)
                    ) {
                        Text(label, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = { viewModel.saveTask() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .background(PrimaryGradient, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                enabled = !uiState.isSaving
            ) {
                Text(
                    text = if (uiState.isSaving) "Saving Task..." else "Save Backup Task",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String? = null,
    isPassword: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = TextSecondary) },
        placeholder = placeholder?.let { { Text(it, color = TextMuted) } },
        singleLine = true,
        visualTransformation = if (isPassword) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = CyanAccent,
            unfocusedBorderColor = DarkBorder,
            focusedContainerColor = DarkSurface,
            unfocusedContainerColor = DarkSurface,
            focusedLabelColor = CyanAccent,
            unfocusedLabelColor = TextSecondary,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary
        ),
        modifier = Modifier.fillMaxWidth()
    )
}
