package com.example.gestusproject

import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.gestusproject.databinding.ActivityVerifyPinBinding
import com.example.gestusproject.utils.PinHashUtil
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

/**
 * VerifyPinActivity - Pantalla para verificar el PIN antes de entrar a HomeActivity
 * 
 * Esta actividad:
 * 1. Verifica si el usuario tiene PIN configurado
 * 2. Si no tiene PIN, redirige a CreatePinActivity (usuarios antiguos)
 * 3. Si tiene PIN, pide que lo ingrese
 * 4. Valida el PIN con el hash almacenado en la base de datos
 * 5. Maneja máximo 3 intentos fallidos
 * 6. Redirige a HomeActivity si el PIN es correcto
 */
class VerifyPinActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityVerifyPinBinding
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance().reference
    
    private var attemptsRemaining = 3
    private var storedPinHash: String? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVerifyPinBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        checkIfPinExists()
        setupViews()
    }
    
    /**
     * Verifica si el usuario tiene PIN configurado
     * Si no tiene PIN, redirige a CreatePinActivity
     */
    private fun checkIfPinExists() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            // Usuario no autenticado, volver a login
            Toast.makeText(this, "Error: Usuario no autenticado", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        
        binding.progressBar.visibility = android.view.View.VISIBLE
        
        // Verificar si el usuario tiene PIN
        database.child("users").child(userId).child("pinHash").get()
            .addOnSuccessListener { snapshot ->
                binding.progressBar.visibility = android.view.View.GONE
                storedPinHash = snapshot.getValue(String::class.java)
                
                if (storedPinHash.isNullOrEmpty()) {
                    // Usuario antiguo sin PIN, redirigir a crear PIN
                    val intent = android.content.Intent(this, CreatePinActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    // Usuario tiene PIN, mostrar formulario
                    updateAttemptsDisplay()
                }
            }
            .addOnFailureListener {
                binding.progressBar.visibility = android.view.View.GONE
                showError("Error al verificar el PIN")
                // Si hay error, asumir que no tiene PIN y redirigir
                val intent = android.content.Intent(this, CreatePinActivity::class.java)
                startActivity(intent)
                finish()
            }
    }
    
    /**
     * Configura todos los listeners y eventos
     */
    private fun setupViews() {
        binding.btnVerifyPin.setOnClickListener { verifyPin() }
        
        // TextWatcher para el campo PIN
        binding.etPin.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            
            override fun afterTextChanged(s: Editable?) {
                clearError()
            }
        })
        
        // Cambiar color cuando el campo obtiene/pierde el foco
        binding.etPin.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) highlightField(binding.tilPin) else resetFieldHighlight(binding.tilPin)
        }
        
        // Autocompletar cuando se ingresen 4 dígitos
        binding.etPin.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            
            override fun afterTextChanged(s: Editable?) {
                if (s?.length == 4 && PinHashUtil.isValidPinFormat(s.toString())) {
                    // Opcional: verificar automáticamente después de 4 dígitos
                    // verifyPin()
                }
            }
        })
    }
    
    /**
     * Limpia el mensaje de error del campo
     */
    private fun clearError() {
        binding.tilPin.error = null
    }
    
    /**
     * Destaca visualmente el campo cuando está enfocado
     */
    private fun highlightField(textInputLayout: com.google.android.material.textfield.TextInputLayout) {
        textInputLayout.boxStrokeColor = ContextCompat.getColor(this, R.color.orange_primary)
        textInputLayout.defaultHintTextColor = android.content.res.ColorStateList.valueOf(
            ContextCompat.getColor(this, R.color.orange_primary)
        )
    }
    
    /**
     * Restaura el color normal del campo cuando pierde el foco
     */
    private fun resetFieldHighlight(textInputLayout: com.google.android.material.textfield.TextInputLayout) {
        textInputLayout.boxStrokeColor = Color.GRAY
    }
    
    /**
     * Actualiza el display de intentos restantes
     */
    private fun updateAttemptsDisplay() {
        binding.tvAttempts.text = "Intentos restantes: $attemptsRemaining"
        
        // Cambiar color según intentos restantes
        val color = when {
            attemptsRemaining <= 1 -> ContextCompat.getColor(this, R.color.error_red)
            attemptsRemaining == 2 -> Color.parseColor("#FFA500") // Naranja
            else -> ContextCompat.getColor(this, R.color.orange_primary)
        }
        binding.tvAttempts.setTextColor(color)
    }
    
    /**
     * Verifica el PIN ingresado contra el hash almacenado
     */
    private fun verifyPin() {
        val pin = binding.etPin.text.toString()
        
        // Validar formato
        if (!PinHashUtil.isValidPinFormat(pin)) {
            binding.tilPin.error = "El PIN debe tener 4 dígitos"
            binding.tilPin.errorIconDrawable = null
            return
        }
        
        // Deshabilitar botón y mostrar progress
        binding.btnVerifyPin.isEnabled = false
        binding.btnVerifyPin.alpha = 0.6f
        binding.progressBar.visibility = android.view.View.VISIBLE
        
        // Validar PIN contra el hash almacenado
        val isValid = PinHashUtil.validatePin(pin, storedPinHash)
        
        // Simular un pequeño delay para UX
        binding.btnVerifyPin.postDelayed({
            binding.progressBar.visibility = android.view.View.GONE
            binding.btnVerifyPin.isEnabled = true
            binding.btnVerifyPin.alpha = 1.0f
            
            if (isValid) {
                // PIN correcto, redirigir a HomeActivity
                showSuccess("PIN verificado correctamente")
                
                // Obtener el nombre del usuario
                val userId = auth.currentUser?.uid ?: return@postDelayed
                database.child("users").child(userId).child("name").get()
                    .addOnSuccessListener { snapshot ->
                        val name = snapshot.getValue(String::class.java) ?: "Usuario"
                        val safeName = android.net.Uri.encode(name)
                        
                        val intent = android.content.Intent(this, HomeActivity::class.java).apply {
                            putExtra("userName", safeName)
                        }
                        startActivity(intent)
                        finish()
                    }
                    .addOnFailureListener {
                        // Continuar de todas formas
                        val intent = android.content.Intent(this, HomeActivity::class.java).apply {
                            putExtra("userName", "Usuario")
                        }
                        startActivity(intent)
                        finish()
                    }
            } else {
                // PIN incorrecto
                attemptsRemaining--
                updateAttemptsDisplay()
                
                // Limpiar campo
                binding.etPin.text?.clear()
                
                if (attemptsRemaining <= 0) {
                    // Máximo de intentos alcanzado
                    showMaxAttemptsReachedDialog()
                } else {
                    binding.tilPin.error = "PIN incorrecto. Intentos restantes: $attemptsRemaining"
                    binding.tilPin.errorIconDrawable = null
                    showError("PIN incorrecto")
                }
            }
        }, 500)
    }
    
    /**
     * Muestra un diálogo cuando se alcanza el máximo de intentos
     */
    private fun showMaxAttemptsReachedDialog() {
        AlertDialog.Builder(this)
            .setTitle("Máximo de intentos alcanzado")
            .setMessage("Has excedido el número máximo de intentos. Por seguridad, se cerrará tu sesión.")
            .setPositiveButton("Aceptar") { _, _ ->
                // Cerrar sesión y volver a LoginActivity
                auth.signOut()
                val intent = android.content.Intent(this, LoginActivity::class.java).apply {
                    addFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP or android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                startActivity(intent)
                finish()
            }
            .setCancelable(false)
            .show()
    }
    
    /**
     * Muestra un mensaje de error usando Snackbar
     */
    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT)
            .setBackgroundTint(ContextCompat.getColor(this, R.color.error_red))
            .setTextColor(Color.WHITE)
            .show()
    }
    
    /**
     * Muestra un mensaje de éxito usando Snackbar
     */
    private fun showSuccess(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT)
            .setBackgroundTint(ContextCompat.getColor(this, R.color.success_green))
            .setTextColor(Color.WHITE)
            .show()
    }
}

