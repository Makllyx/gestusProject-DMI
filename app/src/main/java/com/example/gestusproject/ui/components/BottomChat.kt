package com.example.gestusproject.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.gestusproject.ui.theme.SuccessGreen
import com.example.gestusproject.ui.theme.ErrorRed

@Composable
fun BottomChat(text: String, success: Boolean) {
    val backgroundColor = if (success) SuccessGreen else ErrorRed

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 4.dp
    ) {
        Box(
            modifier = Modifier
                .background(backgroundColor)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}
