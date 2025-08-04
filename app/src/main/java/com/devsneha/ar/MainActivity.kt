package com.devsneha.ar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.devsneha.ar.ui.screens.DrillDetailScreen
import com.devsneha.ar.ui.screens.DrillSelectionScreen
import com.devsneha.ar.ui.theme.ARTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ARTheme {
                ARAssignApp()
            }
        }
    }
}

@Composable
fun ARAssignApp() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "drill_selection"
    ) {
        composable("drill_selection") {
            DrillSelectionScreen(
                onDrillSelected = { drillId ->
                    navController.navigate("drill_detail/$drillId")
                }
            )
        }

        composable("drill_detail/{drillId}") { backStackEntry ->
            val drillId = backStackEntry.arguments?.getString("drillId")?.toIntOrNull() ?: 0
            DrillDetailScreen(
                drillId = drillId,
                onBackClick = {
                    navController.popBackStack()
                },
                onStartAR = { drillName ->
                    navController.navigate("ar_session/$drillName")
                }
            )
        }

        composable("ar_session/{drillName}") { backStackEntry ->
            val drillName = backStackEntry.arguments?.getString("drillName") ?: "Drill 1"
            ARSessionScreen(
                drillName = drillName,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}