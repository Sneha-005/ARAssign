package com.devsneha.ar

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.MotionEvent
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.devsneha.ar.ui.theme.ArTheme

class ARActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val drillName = intent.getStringExtra("drillName") ?: "Drill 1"

        setContent {
            ArTheme {
                ARActivityScreen(
                    drillName = drillName,
                    onBackClick = { finish() }
                )
            }
        }
    }
}

@Composable
fun ARActivityScreen(
    drillName: String,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    var hasPermission by remember { mutableStateOf(false) }
    var showPermissionRationale by remember { mutableStateOf(false) }
    var markerPosition by remember { mutableStateOf<Offset?>(null) }
    var showInstructions by remember { mutableStateOf(true) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasPermission = isGranted
        if (!isGranted) {
            showPermissionRationale = true
        }
    }

    LaunchedEffect(Unit) {
        when {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                hasPermission = true
            }
            else -> {
                permissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top Bar
        Surface(
            color = Color(0xFF1976D2),
            shadowElevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                Text(
                    text = drillName,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        when {
            !hasPermission && showPermissionRationale -> {
                PermissionRationaleContent(
                    onRequestPermission = {
                        permissionLauncher.launch(Manifest.permission.CAMERA)
                        showPermissionRationale = false
                    }
                )
            }
            !hasPermission -> {
                LoadingContent("Requesting camera permission...")
            }
            else -> {
                SimulatedARContent(
                    drillName = drillName,
                    markerPosition = markerPosition,
                    onTap = { position ->
                        markerPosition = position
                        showInstructions = false
                        Toast.makeText(context, "$drillName marker placed!", Toast.LENGTH_SHORT).show()
                    },
                    showInstructions = showInstructions
                )
            }
        }
    }
}

@Composable
private fun SimulatedARContent(
    drillName: String,
    markerPosition: Offset?,
    onTap: (Offset) -> Unit,
    showInstructions: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black) // Simulated camera background
    ) {
        // Simulated camera view with grid pattern
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        onTap(offset)
                    }
                }
        ) {
            // Draw grid pattern to simulate plane detection
            val gridSize = 50.dp.toPx()
            val strokeWidth = 2.dp.toPx()

            // Vertical lines
            for (x in 0..size.width.toInt() step gridSize.toInt()) {
                drawLine(
                    color = Color.White.copy(alpha = 0.3f),
                    start = Offset(x.toFloat(), 0f),
                    end = Offset(x.toFloat(), size.height),
                    strokeWidth = strokeWidth
                )
            }

            // Horizontal lines
            for (y in 0..size.height.toInt() step gridSize.toInt()) {
                drawLine(
                    color = Color.White.copy(alpha = 0.3f),
                    start = Offset(0f, y.toFloat()),
                    end = Offset(size.width, y.toFloat()),
                    strokeWidth = strokeWidth
                )
            }

            // Draw marker if placed
            markerPosition?.let { position ->
                drawCircle(
                    color = Color(0xFFFF9800),
                    radius = 20.dp.toPx(),
                    center = position
                )
                drawCircle(
                    color = Color.White,
                    radius = 15.dp.toPx(),
                    center = position
                )
                drawCircle(
                    color = Color(0xFF1976D2),
                    radius = 10.dp.toPx(),
                    center = position
                )
            }
        }

        // Status indicators
        if (showInstructions) {
            Card(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.Green.copy(alpha = 0.9f)
                )
            ) {
                Text(
                    text = "✅ Plane detected",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        // Instructions
        Card(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.Black.copy(alpha = 0.8f)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (showInstructions) "🎯 Tap on ground to place drill marker"
                    else "✅ $drillName marker placed successfully!",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )

                if (!showInstructions) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Tap anywhere to place a new marker",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Marker info
        markerPosition?.let { position ->
            Card(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1976D2).copy(alpha = 0.9f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "📍",
                        fontSize = 20.sp
                    )
                    Text(
                        text = "Marker Active",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = drillName,
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun PermissionRationaleContent(
    onRequestPermission: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "📷",
            fontSize = 64.sp,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Camera Permission Required",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "AR features need camera access to detect surfaces and place objects in the real world.",
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            color = Color(0xFF666666)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onRequestPermission,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF1976D2)
            )
        ) {
            Text("Grant Camera Permission")
        }
    }
}

@Composable
private fun LoadingContent(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                color = Color(0xFF1976D2)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = message,
                fontSize = 16.sp,
                color = Color(0xFF666666)
            )
        }
    }
}