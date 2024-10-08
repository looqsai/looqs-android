package com.sean8.jeminai.feature.setup

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
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
    val isAudioPermissionGranted by viewModel.isAudioPermissionGranted.collectAsState()
    val projectionStatus by viewModel.projectionStatus.collectAsState()
    val version = viewModel.appVersion

    // Handle navigation to accessibility settings
    LaunchedEffect(Unit) {
        viewModel.navigateToAccessibility.collectLatest {
            context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }
    }

    // Projection Permission Launcher
    val projectionPermissionLauncher = rememberLauncherForActivityResult(
        contract = StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            if (data != null) {
                viewModel.onProjectionPermissionResult(result.resultCode, data)
            }
        }
    }

    val requestProjectionPermission by viewModel.requestProjectionPermission.collectAsState(initial = null)
    LaunchedEffect(requestProjectionPermission) {
        requestProjectionPermission?.let {
            val mediaProjectionManager =
                context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
            val captureIntent = mediaProjectionManager.createScreenCaptureIntent()
            projectionPermissionLauncher.launch(captureIntent)
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
        projectionStatus = projectionStatus,
        version = version,
        isAudioPermissionGranted = isAudioPermissionGranted,
        onAccessibilityClicked = { viewModel.navigateToAccessibility() },
        onProjectionClicked = { viewModel.askProjectionPermission() },
        onRequestAudioPermissionClicked = { viewModel.requestAudioPermission() },
    )
}

@Composable
private fun SetupOptions(
    networkStatus: Boolean,
    accessibilityStatus: Boolean,
    projectionStatus: MediaProjection?,
    version: String,
    isAudioPermissionGranted: Boolean,
    onAccessibilityClicked: () -> Unit,
    onProjectionClicked: () -> Unit,
    onRequestAudioPermissionClicked: () -> Unit,
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

        // Projection Status
        val projectionEnabled = projectionStatus != null
        StatusButton(
            text = "Projection is ${if (projectionEnabled) "Active" else "Inactive"}",
            isActive = projectionEnabled,
            onClick = {
                if (!projectionEnabled) {
                    onProjectionClicked()
                }
            }
        )

        StatusButton(
            text = "Audio Permission is ${if (isAudioPermissionGranted) "GRANTED" else "NOT GRANTED"}",
            isActive = isAudioPermissionGranted,
            onClick = onRequestAudioPermissionClicked,
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
        version = "Version 1.0.0",
        isAudioPermissionGranted = true,
        projectionStatus = null,
        onAccessibilityClicked = { /* No action */ },
        onProjectionClicked = { /* No action */ },
        onRequestAudioPermissionClicked = { /* No action */ },
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