package com.example.gestusproject

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

data class Gesture(
    val id: String,
    val title: String,
    val description: String,
    val imageRes: Int
)

// Lista de ejemplos (usa tus recursos reales)
private val sampleGestures = listOf(
    Gesture("hola", "Hola", "Saludo básico en LSM.", R.drawable.hola),
    Gesture("gracias", "Gracias", "Gesto para agradecer.", R.drawable.gracias),
    Gesture("porfavor", "Por favor", "Gesto de cortesía.", R.drawable.por_favor),
    Gesture("si", "Sí", "Afirmación.", R.drawable.si),
    Gesture("no", "No", "Negación.", R.drawable.no)
)


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavHostController, userName: String) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text(text = "Bienvenido @$userName") })
        }
    ) { innerPadding ->
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 160.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(sampleGestures) { gesture ->
                GestureCard(gesture = gesture) {
                    navController.navigate("gesture/${gesture.id}")
                }
            }
        }
    }
}

@Composable
private fun GestureCard(gesture: Gesture, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .clickable { onClick() }
            .fillMaxWidth()
            .height(180.dp), // Fijamos altura para evitar errores de medida
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            Image(
                painter = painterResource(id = gesture.imageRes),
                contentDescription = gesture.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .height(100.dp)
                    .fillMaxWidth()
            )
            Text(
                text = gesture.title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
            )
        }
    }
}
