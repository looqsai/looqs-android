package com.sean8.jeminai.core.accessibility.ui

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun SetupScreen(
    viewModel: SetupViewModel = hiltViewModel(),
) {
    val context = LocalContext.current

    // Observing LiveData from ViewModel
    val networkStatus by viewModel.networkStatus.observeAsState(initial = false)
    val accessibilityStatus by viewModel.accessibilityStatus.observeAsState(initial = false)
    val projectionToken by viewModel.projectionToken.observeAsState()
    val latestMessage by viewModel.latestMessage.observeAsState(initial = "")
    val isDefaultAssistant by viewModel.isDefaultAssistant.observeAsState(initial = false)
    val version = viewModel.appVersion

    // Handle navigation to accessibility settings
    val navigateToAccessibility by viewModel.navigateToAccessibility.observeAsState()
    navigateToAccessibility?.getContentIfNotHandled()?.let {
        context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
    }

    // Handle navigation to assistant settings
    val navigateToAssistantSettings by viewModel.navigateToAssistantSettings.observeAsState()
    navigateToAssistantSettings?.getContentIfNotHandled()?.let {
        val intent = Intent(Settings.ACTION_VOICE_INPUT_SETTINGS)
        context.startActivity(intent)
    }

    // Handle projection permission request
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = { result ->
            if (result.resultCode == RESULT_OK && result.data != null) {
                viewModel.onProjectionPermissionResult(result.data!!)
            }
        },
    )

    val requestProjectionPermission by viewModel.requestProjectionPermission.observeAsState()
    requestProjectionPermission?.getContentIfNotHandled()?.let {
        val projectionManager =
            context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        val intent = projectionManager.createScreenCaptureIntent()
        launcher.launch(intent)
    }

    // Refresh assistant status when the screen resumes
    DisposableEffect(Unit) {
        viewModel.checkIfDefaultAssistant()
        onDispose { }
    }

    // Main UI
    SetupOptions(
        networkStatus = networkStatus,
        accessibilityStatus = accessibilityStatus,
        projectionToken = projectionToken,
        latestMessage = latestMessage,
        version = version,
        isDefaultAssistant = isDefaultAssistant,
        onAccessibilityClicked = { viewModel.navigateToAccessibility() },
        onRevokeAccessibilityClicked = { viewModel.revokeAccessibility() },
        onProjectionClicked = { viewModel.askProjection() },
        onAssistantClicked = { viewModel.navigateToAssistantSettings() },
    )
}

@Composable
private fun SetupOptions(
    networkStatus: Boolean,
    accessibilityStatus: Boolean,
    projectionToken: Intent?,
    latestMessage: String,
    version: String,
    isDefaultAssistant: Boolean,
    onAccessibilityClicked: () -> Unit,
    onRevokeAccessibilityClicked: () -> Unit,
    onProjectionClicked: () -> Unit,
    onAssistantClicked: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        // Network Status Button
        Button(
            onClick = { /* No action specified */ },
            colors = ButtonDefaults.buttonColors(
                containerColor = if (networkStatus) Color(0xFF99CC00) else Color.DarkGray,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
        ) {
            Text(text = "Network is ${if (networkStatus) "available" else "not available"}")
        }

        // Accessibility Button with Long Click
        LongClickButton(
            onClick = onAccessibilityClicked,
            onLongClick = onRevokeAccessibilityClicked,
            backgroundColor = if (accessibilityStatus) Color(0xFF99CC00) else Color.DarkGray,
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
        ) {
            Text(text = "Accessibility is ${if (accessibilityStatus) "ON" else "OFF"}")
        }

        // Projection Button
        Button(
            onClick = onProjectionClicked,
            enabled = projectionToken == null,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (projectionToken != null) Color(0xFF99CC00) else Color.DarkGray,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
        ) {
            Text(text = "Projection is ${if (projectionToken != null) "ON" else "OFF"}")
        }

        // Assistant Button
        Button(
            onClick = onAssistantClicked,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isDefaultAssistant) Color(0xFF99CC00) else Color.DarkGray,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
        ) {
            Text(text = "Assistant is ${if (isDefaultAssistant) "ON" else "OFF"}")
        }

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

// Custom Button to handle long clicks
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LongClickButton(
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    backgroundColor: Color,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Surface(
        color = backgroundColor,
        shape = MaterialTheme.shapes.small,
        modifier = modifier
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick,
            ),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp),
        ) {
            content()
        }
    }
}

//@Preview(showBackground = true)
//@Composable
//fun PreviewSetupScreen() {
//    SetupScreen()
//}

@Preview(showBackground = true)
@Composable
fun PreviewSetupOptions() {
    SetupOptions(
        networkStatus = true,
        accessibilityStatus = true,
        projectionToken = null,
        latestMessage = "All systems operational",
        version = "v1.0.0",
        isDefaultAssistant = true,
        onAccessibilityClicked = {},
        onRevokeAccessibilityClicked = {},
        onProjectionClicked = {},
        onAssistantClicked = {},
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewLongClickButton() {
    LongClickButton(
        onClick = {},
        onLongClick = {},
        backgroundColor = Color.Green,
    ) {
        Text(text = "Long Click Button")
    }
}
