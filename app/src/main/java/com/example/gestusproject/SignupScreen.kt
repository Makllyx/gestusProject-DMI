package com.example.gestusproject

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

// --- Helpers de validación ---
private fun isLengthValid(pw: String) = pw.length >= 8
private fun hasUppercase(pw: String) = pw.any { it.isUpperCase() }
private fun hasLowercase(pw: String) = pw.any { it.isLowerCase() }
private fun hasDigit(pw: String) = pw.any { it.isDigit() }
private fun hasSpecialChar(pw: String) = pw.any { !it.isLetterOrDigit() }

private fun passwordStrength(pw: String): Int {
    var score = 0
    if (isLengthValid(pw)) score++
    if (hasUppercase(pw)) score++
    if (hasLowercase(pw)) score++
    if (hasDigit(pw)) score++
    if (hasSpecialChar(pw)) score++
    return score // 0..5
}

@SuppressLint("UnrememberedMutableState")
@Composable
fun SignupScreen(navController: NavHostController) {

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val database = FirebaseDatabase.getInstance().reference

    // Validaciones en tiempo real
    val lengthValid by derivedStateOf { isLengthValid(password) }
    val upperValid by derivedStateOf { hasUppercase(password) }
    val lowerValid by derivedStateOf { hasLowercase(password) }
    val digitValid by derivedStateOf { hasDigit(password) }
    val specialValid by derivedStateOf { hasSpecialChar(password) }
    val allValid by derivedStateOf { lengthValid && upperValid && lowerValid && digitValid && specialValid }
    val strength by derivedStateOf { passwordStrength(password) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {

            Text("Regístrate", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nombre") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Correo electrónico") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Contraseña") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val icon = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                    Icon(
                        imageVector = icon,
                        contentDescription = if (passwordVisible) "Ocultar" else "Mostrar",
                        modifier = Modifier
                            .clickable { passwordVisible = !passwordVisible }
                            .padding(8.dp)
                    )
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Indicador de fuerza simple
            StrengthBar(strength = strength)

            Spacer(modifier = Modifier.height(12.dp))

            // Lista de reglas con checkmarks
            ValidationRow("Mínimo 8 caracteres", lengthValid)
            ValidationRow("Al menos 1 mayúscula", upperValid)
            ValidationRow("Al menos 1 minúscula", lowerValid)
            ValidationRow("Al menos 1 número", digitValid)
            ValidationRow("Al menos 1 carácter especial", specialValid)

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                onClick = {
                    if (name.isNotEmpty() && email.isNotEmpty() && allValid) {
                        auth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    val userId = task.result?.user?.uid ?: return@addOnCompleteListener
                                    val userData = mapOf(
                                        "name" to name,
                                        "email" to email
                                    )
                                    database.child("users").child(userId).setValue(userData)
                                        .addOnSuccessListener {
                                            Toast.makeText(context, "Registro exitoso", Toast.LENGTH_SHORT).show()
                                            navController.navigate("login") {
                                                popUpTo("signup") { inclusive = true }
                                            }
                                        }
                                        .addOnFailureListener {
                                            Toast.makeText(context, "Error al guardar datos", Toast.LENGTH_SHORT).show()
                                        }
                                } else {
                                    Toast.makeText(context, "Error: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                    } else {
                        // Mensaje útil si faltan reglas
                        if (!allValid) {
                            Toast.makeText(context, "La contraseña no cumple las reglas", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                enabled = allValid && name.isNotEmpty() && email.isNotEmpty()
            ) {
                Text("Crear cuenta")
            }

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(onClick = {
                navController.navigate("login") {
                    popUpTo("signup") { inclusive = true }
                }
            }) {
                Text("¿Ya tienes cuenta? Inicia sesión")
            }
        }
    }
}

@Composable
private fun ValidationRow(text: String, valid: Boolean) {
    val color = if (valid) Color(0xFF2E7D32) else Color(0xFFB00020)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = if (valid) "✓" else "○",
            color = color,
            modifier = Modifier.width(24.dp)
        )
        Text(text = text, color = color)
    }
}

@Composable
private fun StrengthBar(strength: Int) {
    // strength 0..5
    val percent = (strength / 5f)
    val label = when (strength) {
        0,1 -> "Muy débil"
        2 -> "Débil"
        3 -> "Medio"
        4 -> "Fuerte"
        5 -> "Excelente"
        else -> ""
    }
    val barColor = when (strength) {
        0,1 -> Color(0xFFB00020)
        2 -> Color(0xFFF57C00)
        3 -> Color(0xFFFFC107)
        4 -> Color(0xFF2E7D32)
        5 -> Color(0xFF1B5E20)
        else -> Color.Gray
    }

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
        // barra simple
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .background(Color(0xFFE0E0E0), shape = RoundedCornerShape(4.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(percent)
                    .height(8.dp)
                    .background(barColor, shape = RoundedCornerShape(4.dp))
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = label, style = MaterialTheme.typography.bodySmall)
            Text(text = "${(percent * 100).toInt()}%", style = MaterialTheme.typography.bodySmall)
        }
    }
}
