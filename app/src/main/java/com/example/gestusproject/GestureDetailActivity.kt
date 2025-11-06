package com.example.gestusproject

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.gestusproject.databinding.ActivityGestureDetailBinding

class GestureDetailActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityGestureDetailBinding
    
    private val gestures = mapOf(
        "hola" to Gesture("hola", "Hola", "Saludo básico en LSM.", R.drawable.hola),
        "gracias" to Gesture("gracias", "Gracias", "Gesto para agradecer.", R.drawable.gracias),
        "porfavor" to Gesture("porfavor", "Por favor", "Gesto de cortesía.", R.drawable.por_favor),
        "si" to Gesture("si", "Sí", "Afirmación.", R.drawable.si),
        "no" to Gesture("no", "No", "Negación.", R.drawable.no)
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

