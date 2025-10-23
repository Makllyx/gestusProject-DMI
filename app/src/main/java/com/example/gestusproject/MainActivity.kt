package com.example.gestusproject

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.gestusproject.ui.theme.GestusProjectTheme

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val navController = rememberNavController()
            GestusProjectTheme {
                NavHost(
                    navController = navController,
                    startDestination = "login"
                ) {
                    composable("login") {
                        LoginScreen(navController)
                    }
                    composable("signup") {
                        SignupScreen(navController)
                    }
                    composable("home/{name}") { backStackEntry ->
                        val nameArg = backStackEntry.arguments?.getString("name") ?: ""
                        HomeScreen(navController = navController, userName = nameArg)
                    }
                    composable("gesture/{gestureId}") { backStackEntry ->
                        val gestureId = backStackEntry.arguments?.getString("gestureId") ?: ""
                        GestureDetailScreen(navController = navController, gestureId = gestureId)
                    }
                    composable("camera/{gestureId}") { backStackEntry ->
                        val gestureId = backStackEntry.arguments?.getString("gestureId") ?: ""
                        CameraScreen(navController = navController, gestureId = gestureId)
                    }
                }
            }
        }
    }
}
