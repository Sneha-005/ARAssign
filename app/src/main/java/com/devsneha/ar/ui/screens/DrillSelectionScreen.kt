package com.devsneha.ar.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devsneha.ar.data.Drill
import com.devsneha.ar.data.DrillRepository
import com.devsneha.ar.ui.theme.ARTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrillSelectionScreen(
    onDrillSelected: (Int) -> Unit = {}
) {
    val drills = DrillRepository.drills

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "AR Drill Assignment",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1976D2)
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFF5F5F5),
                            Color(0xFFE3F2FD)
                        )
                    )
                )
        ) {
            // Header Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "🎯",
                        fontSize = 48.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Select Drill",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF333333),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Choose a drill from the dropdown below",
                        fontSize = 14.sp,
                        color = Color(0xFF666666),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            // Drill List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(drills) { drill ->
                    DrillCard(
                        drill = drill,
                        onClick = { onDrillSelected(drill.id) }
                    )
                }

                // Add some bottom padding
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
fun DrillCard(
    drill: Drill,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Drill Icon
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        getDifficultyColor(drill.difficulty),
                        RoundedCornerShape(28.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = drill.imageEmoji,
                    fontSize = 28.sp
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Drill Info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = drill.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF333333)
                )
                Text(
                    text = "${drill.difficulty} • ${drill.duration}",
                    fontSize = 12.sp,
                    color = Color(0xFF666666),
                    modifier = Modifier.padding(top = 2.dp)
                )
                Text(
                    text = "Tap to view details",
                    fontSize = 11.sp,
                    color = Color(0xFF1976D2),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // Arrow Icon
            Text(
                text = "▶️",
                fontSize = 20.sp,
                color = Color(0xFF1976D2)
            )
        }
    }
}

@Composable
private fun getDifficultyColor(difficulty: String): Color {
    return when (difficulty.lowercase()) {
        "beginner" -> Color(0xFF4CAF50)
        "intermediate" -> Color(0xFFFF9800)
        "advanced" -> Color(0xFFF44336)
        else -> Color(0xFF1976D2)
    }
}

@Preview(showBackground = true)
@Composable
fun DrillSelectionPreview() {
    ARTheme {
        DrillSelectionScreen()
    }
}