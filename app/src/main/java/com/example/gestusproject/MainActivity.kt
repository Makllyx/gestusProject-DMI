package com.example.gestusproject

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {
    
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Verificar si el usuario ya está autenticado
        val currentUser = auth.currentUser
        
        if (currentUser != null) {
            // Usuario ya autenticado, verificar PIN antes de entrar a Home
            startActivity(Intent(this, VerifyPinActivity::class.java))
        } else {
            // Usuario no autenticado, ir a Login
            startActivity(Intent(this, LoginActivity::class.java))
        }
        
        finish()
    }
}