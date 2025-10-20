package com.example.gestusproject

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

@Composable
fun GestureDetailScreen(navController: NavHostController, gestureId: String) {
    val gesture = when (gestureId) {
        "hola" -> Gesture("hola", "Hola", "Saludo básico en LSM.", R.drawable.hola)
        "gracias" -> Gesture("gracias", "Gracias", "Gesto para agradecer.", R.drawable.gracias)
        "porfavor" -> Gesture("porfavor", "Por favor", "Gesto de cortesía.", R.drawable.por_favor)
        "si" -> Gesture("si", "Sí", "Afirmación.", R.drawable.si)
        "no" -> Gesture("no", "No", "Negación.", R.drawable.no)
        else -> Gesture(gestureId, gestureId, "", R.drawable.hola) // puedes dejar hola como default
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = gesture.title, style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(12.dp))
        Image(
            painter = painterResource(id = gesture.imageRes),
            contentDescription = gesture.title,
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(text = gesture.description, style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = { navController.navigate("camera/${gesture.id}") }) {
            Text("Abrir cámara para recrear gesto")
        }
    }
}
