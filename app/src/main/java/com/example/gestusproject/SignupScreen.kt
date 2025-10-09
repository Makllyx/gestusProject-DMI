package com.example.gestusproject

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.firebase.database.FirebaseDatabase

@Composable
fun SignupScreen(navController: NavHostController){

    var name  by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    var error by remember { mutableStateOf("") }
    var context = LocalContext.current

    Box(modifier = Modifier.fillMaxSize().padding(16.dp),
        contentAlignment = Alignment.Center) {
        Column (horizontalAlignment = Alignment.CenterHorizontally
        ){
            Text("Registrate", style = MaterialTheme.typography.headlineMedium)

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(value = name,
                onValueChange = { name = it},
                label = { Text("Nombre")})

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(value = email,
                onValueChange = { email = it},
                label = { Text("Correo Electronico")})

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(value = password,
                onValueChange = { password = it},
                label = { Text("Contraseña")})

            Spacer(modifier = Modifier.height(8.dp))

            Button(modifier = Modifier.fillMaxWidth().padding(16.dp),
                onClick = {
                    if (name.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()){
                        val database = FirebaseDatabase.getInstance().reference.child("users")
                        val userId = database.push().key ?: ""

                        val userData = mapOf(
                            "name" to name,
                            "email" to email,
                            "password" to password
                        )
                        database.child(userId).setValue(userData).addOnFailureListener {
                            navController.navigate("login"){
                                popUpTo(0)
                            }
                        }
                    } else {
                        error = "Por favor, completa todos los campos"
                    }
                }) {
                Text("Crear Cuenta")
            }
            Spacer(modifier = Modifier.height(8.dp))

            TextButton(onClick = {
                navController.navigate("login"){
                    popUpTo(0)
                }
            }) {
                Text("Ya tienes una cuenta? Inicia Sesión")
            }
            if (error.isNotEmpty()){
                Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
            }

        }
    }
}