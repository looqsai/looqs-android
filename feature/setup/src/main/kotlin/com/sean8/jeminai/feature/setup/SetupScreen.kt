package com.sean8.jeminai.feature.setup

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.flow.collectLatest

@Composable
fun SetupScreen(
    viewModel: SetupViewModel = hiltViewModel(),
) {
    val context = LocalContext.current

    // Collecting StateFlow from ViewModel
    val networkStatus by viewModel.networkStatus.collectAsState()
    val accessibilityStatus by viewModel.accessibilityStatus.collectAsState()
    val projectionToken by viewModel.projectionToken.collectAsState()
    val latestMessage by viewModel.latestMessage.collectAsState()
    val version = viewModel.appVersion
    val isListening by viewModel.isListening.collectAsState()
    val isAudioPermissionGranted by viewModel.isAudioPermissionGranted.collectAsState()

    // Handle navigation to accessibility settings
    LaunchedEffect(Unit) {
        viewModel.navigateToAccessibility.collectLatest {
            context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }
    }

    // Handle projection permission request
    val projectionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = { result ->
            if (result.resultCode == RESULT_OK && result.data != null) {
                viewModel.onProjectionPermissionResult(result.data!!)
            }
        },
    )

    LaunchedEffect(Unit) {
        viewModel.requestProjectionPermission.collectLatest {
            val projectionManager =
                context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
            val intent = projectionManager.createScreenCaptureIntent()
            projectionLauncher.launch(intent)
        }
    }

    // Handle audio permission request
    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            viewModel.onAudioPermissionResult(isGranted)
        },
    )

    LaunchedEffect(Unit) {
        viewModel.requestAudioPermission.collectLatest {
            audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    // Main UI
    SetupOptions(
        networkStatus = networkStatus,
        accessibilityStatus = accessibilityStatus,
        projectionToken = projectionToken,
        latestMessage = latestMessage,
        version = version,
        isListening = isListening,
        isAudioPermissionGranted = isAudioPermissionGranted,
        onAccessibilityClicked = { viewModel.navigateToAccessibility() },
        onProjectionClicked = { viewModel.askProjection() },
        onRequestAudioPermissionClicked = { viewModel.requestAudioPermission() },
        onListeningClicked = {
            if (isListening) {
                viewModel.stopListeningService()
            } else {
                viewModel.startListeningService()
            }
        },
    )
}

@Composable
private fun SetupOptions(
    networkStatus: Boolean,
    accessibilityStatus: Boolean,
    projectionToken: Intent?,
    latestMessage: String,
    version: String,
    isListening: Boolean,
    isAudioPermissionGranted: Boolean,
    onAccessibilityClicked: () -> Unit,
    onProjectionClicked: () -> Unit,
    onRequestAudioPermissionClicked: () -> Unit,
    onListeningClicked: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        StatusButton(
            text = "Network is ${if (networkStatus) "available" else "not available"}",
            isActive = networkStatus,
            onClick = { /* No action specified */ },
        )

        StatusButton(
            text = "Accessibility is ${if (accessibilityStatus) "ON" else "OFF"}",
            isActive = accessibilityStatus,
            onClick = onAccessibilityClicked,
        )

        StatusButton(
            text = "Projection is ${if (projectionToken != null) "ON" else "OFF"}",
            isActive = projectionToken != null,
            onClick = onProjectionClicked,
        )

        StatusButton(
            text = "Audio Permission is ${if (isAudioPermissionGranted) "GRANTED" else "NOT GRANTED"}",
            isActive = isAudioPermissionGranted,
            onClick = onRequestAudioPermissionClicked,
        )

        StatusButton(
            text = "Listening is ${if (isListening) "ON" else "OFF"}",
            isActive = isListening,
            onClick = onListeningClicked,
        )

        // Latest Message Text
        Text(
            text = latestMessage,
            fontSize = 18.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
        )

        Spacer(modifier = Modifier.weight(1f))

        // Version Text
        Text(
            text = version,
            textAlign = TextAlign.End,
            modifier = Modifier
                .fillMaxWidth()
                .padding(5.dp),
        )
    }
}

// Custom StatusButton to ensure consistent styling
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun StatusButton(
    text: String,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val backgroundColor = if (isActive) Color(0xFF99CC00) else Color.DarkGray

    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
        ),
        modifier = modifier
            .fillMaxWidth()
            .padding(10.dp),
    ) {
        Text(text = text)
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewSetupOptions() {
    SetupOptions(
        networkStatus = true,
        accessibilityStatus = true,
        projectionToken = null,
        latestMessage = "All systems are functional",
        version = "Version 1.0.0",
        isListening = false,
        isAudioPermissionGranted = true,
        onAccessibilityClicked = { /* No action */ },
        onProjectionClicked = { /* No action */ },
        onRequestAudioPermissionClicked = { /* No action */ },
        onListeningClicked = { /* No action */ },
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewStatusButton() {
    StatusButton(
        text = "Network is available",
        isActive = true,
        onClick = { /* No action */ },
    )
}