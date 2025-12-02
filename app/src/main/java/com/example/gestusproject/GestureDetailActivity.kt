package com.example.gestusproject

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.gestusproject.databinding.ActivityGestureDetailBinding

class GestureDetailActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityGestureDetailBinding
    
    private val gestures = mapOf(
        "hola" to Gesture("hola", "Hola", "Saludo básico en LSM. Muestra tu mano abierta y muévela lateralmente.", R.drawable.hola_gesto),
        "si" to Gesture("si", "Sí", "Afirmación. Cierra tu puño y muévelo verticalmente.", R.drawable.si_gesto),
        "no" to Gesture("no", "No", "Negación. Muestra tu pulgar hacia abajo.", R.drawable.no_gesto),
        "bien" to Gesture("bien", "Bien", "Aprobación. Muestra tu pulgar hacia arriba.", R.drawable.bien_gesto),
        "uno" to Gesture("uno", "Uno", "Número uno. Apunta hacia arriba con tu dedo índice.", R.drawable.uno_gesto),
        "dos" to Gesture("dos", "Dos", "Número dos. Muestra el gesto de victoria.", R.drawable.dos_gesto),
        "te_amo" to Gesture("te_amo", "Te Amo", "Expresión de amor. Muestra el gesto de te amo.", R.drawable.teamo_gesto)
    )
    
    data class Gesture(
        val id: String,
        val title: String,
        val description: String,
        val imageRes: Int
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGestureDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        val gestureId = intent.getStringExtra("gestureId") ?: "hola"
        val gesture = gestures[gestureId] ?: gestures["hola"]!!
        
        setSupportActionBar(findViewById(androidx.appcompat.R.id.action_bar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        
        binding.tvGestureTitle.text = gesture.title
        binding.tvGestureDescription.text = gesture.description
        binding.ivGesture.setImageResource(gesture.imageRes)
        
        binding.btnOpenCamera.setOnClickListener {
            val intent = Intent(this, CameraActivity::class.java).apply {
                putExtra("gestureId", gesture.id)
            }
            startActivity(intent)
        }
    }
    
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}

