package com.example.gestusproject

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import androidx.recyclerview.widget.GridLayoutManager
import com.example.gestusproject.databinding.ActivityHomeBinding

class HomeActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityHomeBinding
    private lateinit var gestureAdapter: GestureAdapter
    
    private val gestures = listOf(
        Gesture("hola", "Hola", "Saludo básico en LSM.", R.drawable.hola),
        Gesture("gracias", "Gracias", "Gesto para agradecer.", R.drawable.gracias),
        Gesture("porfavor", "Por favor", "Gesto de cortesía.", R.drawable.por_favor),
        Gesture("si", "Sí", "Afirmación.", R.drawable.si),
        Gesture("no", "No", "Negación.", R.drawable.no)
    )
    
    data class Gesture(
        val id: String,
        val title: String,
        val description: String,
        val imageRes: Int
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        val userName = intent.getStringExtra("userName") ?: "Usuario"
        
        setupToolbar(userName)
        setupRecyclerView()
        setupLogoutButton()
    }
    
    private fun setupToolbar(userName: String) {
        binding.toolbar.title = "¡Hola, $userName!"
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        // Menú con acción de cerrar sesión
        binding.toolbar.inflateMenu(R.menu.menu_home)
        binding.toolbar.setOnMenuItemClickListener { item ->
            if (item.itemId == R.id.action_logout) {
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this, LoginActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                startActivity(intent)
                finish()
                true
            } else {
                false
            }
        }
    }
    
    private fun setupRecyclerView() {
        gestureAdapter = GestureAdapter(gestures) { gesture ->
            val intent = Intent(this, GestureDetailActivity::class.java).apply {
                putExtra("gestureId", gesture.id)
            }
            startActivity(intent)
        }
        
        binding.rvGestures.apply {
            layoutManager = GridLayoutManager(this@HomeActivity, 2)
            adapter = gestureAdapter
        }
    }

    private fun setupLogoutButton() {
        binding.btnLogout.setOnClickListener {
            com.google.firebase.auth.FirebaseAuth.getInstance().signOut()
            val intent = Intent(this, LoginActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(intent)
            finish()
        }
    }
}

