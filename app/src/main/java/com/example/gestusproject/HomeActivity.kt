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
        Gesture("hola", "Hola", "Saludo básico en LSM. Muestra tu mano abierta y muévela lateralmente.", R.drawable.hola_gesto),
        Gesture("si", "Sí", "Afirmación. Cierra tu puño y muévelo verticalmente.", R.drawable.si_gesto),
        Gesture("no", "No", "Negación. Muestra tu pulgar hacia abajo.", R.drawable.no_gesto),
        Gesture("bien", "Bien", "Aprobación. Muestra tu pulgar hacia arriba.", R.drawable.bien_gesto),
        Gesture("uno", "Uno", "Número uno. Apunta hacia arriba con tu dedo índice.", R.drawable.uno_gesto),
        Gesture("dos", "Dos", "Número dos. Muestra el gesto de victoria.", R.drawable.dos_gesto),
        Gesture("te_amo", "Te Amo", "Expresión de amor. Muestra el gesto de te amo.", R.drawable.teamo_gesto)
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
        
        // Decodificar el nombre del usuario que viene codificado
        val encodedUserName = intent.getStringExtra("userName") ?: "Usuario"
        val userName = android.net.Uri.decode(encodedUserName)
        
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

