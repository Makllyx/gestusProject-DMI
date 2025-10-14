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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import android.net.Uri
import com.google.firebase.Firebase
import com.google.firebase.database.FirebaseDatabase

@Composable
fun LoginScreen(navController: NavHostController){

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    var error by remember { mutableStateOf("") }
    var context = LocalContext.current

    Box(modifier = Modifier.fillMaxSize().padding(16.dp),
        contentAlignment = Alignment.Center) {
        Column (horizontalAlignment = Alignment.CenterHorizontally
        ){
            Text("Inicia Sesión", style = MaterialTheme.typography.headlineMedium)

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(value = email,
                onValueChange = { email = it},
                label = { Text("Correo Electronico")})

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(value = password,
                onValueChange = { password = it},
                label = { Text("Contraseña")},
                visualTransformation = PasswordVisualTransformation())

            Spacer(modifier = Modifier.height(8.dp))

            Button(modifier = Modifier.fillMaxWidth().padding(16.dp),
                onClick = {
                    val database = FirebaseDatabase.getInstance().reference.child("users")

                    database.get().addOnSuccessListener { data ->
                        var matchedName: String? = null
                        for (user in data.children){
                            val userEmail = user.child("Correo Electronico").getValue(String::class.java)
                                ?: user.child("email").getValue(String::class.java)
                            val userPassword = user.child("Contraseña").getValue(String::class.java)
                                ?: user.child("password").getValue(String::class.java)

                            if (email == userEmail && password == userPassword) {
                                matchedName = user.child("Nombre").getValue(String::class.java)
                                    ?: user.child("name").getValue(String::class.java)
                                break
                            }
                        }
                        if (matchedName != null) {
                            val safeName = Uri.encode(matchedName)
                            navController.navigate("home/$safeName"){ popUpTo(0) }
                        } else {
                            error = "Credenciales incorrectas"
                        }
                    }.addOnFailureListener {
                        error = "Error de conexión"
                    }
                }) {
                Text("Iniciar Sesión")
            }
            Spacer(modifier = Modifier.height(8.dp))

            TextButton(onClick = {
                navController.navigate("signup"){
                    popUpTo(0)
                }
            }) {
                Text("¿No tienes una cuenta? Registrate")
            }
            if (error.isNotEmpty()){
                Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
            }

        }
    }
}

