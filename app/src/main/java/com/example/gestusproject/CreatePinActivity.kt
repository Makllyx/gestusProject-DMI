package com.example.gestusproject

import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.gestusproject.databinding.ActivityCreatePinBinding
import com.example.gestusproject.utils.PinHashUtil
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

/**
 * CreatePinActivity - Pantalla para crear un PIN de 4 dígitos
 * 
 * Esta actividad se muestra:
 * 1. Después del registro de usuarios nuevos
 * 2. La primera vez que usuarios antiguos inicien sesión (si no tienen PIN)
 * 
 * Características:
 * - Valida que el PIN tenga exactamente 4 dígitos
 * - Requiere confirmación del PIN (ambos deben coincidir)
 * - Hashea el PIN con SHA-256 antes de guardarlo
 * - Guarda el hash en Realtime Database en users/{uid}/pinHash
 * - Redirige a HomeActivity después de crear el PIN exitosamente
 */
class CreatePinActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityCreatePinBinding
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance().reference
    
    private var pinValid = false
    private var confirmPinValid = false
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreatePinBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupViews()
    }
    
    /**
     * Configura todos los listeners y eventos
     */
    private fun setupViews() {
        binding.btnCreatePin.setOnClickListener { createPin() }
        
        // TextWatcher para el campo PIN
        binding.etPin.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            
            override fun afterTextChanged(s: Editable?) {
                validatePin(s.toString())
                validateFields()
            }
        })
        
        // TextWatcher para el campo confirmar PIN
        binding.etConfirmPin.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            
            override fun afterTextChanged(s: Editable?) {
                validateConfirmPin(s.toString())
                validateFields()
            }
        })
        
        // Cambiar color cuando los campos obtienen/pierden el foco
        binding.etPin.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) highlightField(binding.tilPin) else resetFieldHighlight(binding.tilPin)
        }
        
        binding.etConfirmPin.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) highlightField(binding.tilConfirmPin) else resetFieldHighlight(binding.tilConfirmPin)
        }
    }
    
    /**
     * Valida que el PIN tenga exactamente 4 dígitos numéricos
     */
    private fun validatePin(pin: String) {
        when {
            pin.isEmpty() -> {
                binding.tilPin.error = null
                pinValid = false
            }
            !PinHashUtil.isValidPinFormat(pin) -> {
                binding.tilPin.error = "El PIN debe tener 4 dígitos"
                binding.tilPin.errorIconDrawable = null
                pinValid = false
            }
            else -> {
                binding.tilPin.error = null
                pinValid = true
            }
        }
    }
    
    /**
     * Valida que el PIN de confirmación coincida con el PIN original
     */
    private fun validateConfirmPin(confirmPin: String) {
        val pin = binding.etPin.text.toString()
        
        when {
            confirmPin.isEmpty() -> {
                binding.tilConfirmPin.error = null
                confirmPinValid = false
            }
            confirmPin != pin -> {
                binding.tilConfirmPin.error = "Los PINs no coinciden"
                binding.tilConfirmPin.errorIconDrawable = null
                confirmPinValid = false
            }
            else -> {
                binding.tilConfirmPin.error = null
                confirmPinValid = true
            }
        }
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
     * Habilita o deshabilita el botón según el estado de los campos
     */
    private fun validateFields() {
        binding.btnCreatePin.isEnabled = pinValid && confirmPinValid
        
        if (binding.btnCreatePin.isEnabled) {
            binding.btnCreatePin.alpha = 1.0f
        } else {
            binding.btnCreatePin.alpha = 0.6f
        }
    }
    
    /**
     * Crea el PIN y lo guarda en la base de datos
     */
    private fun createPin() {
        val pin = binding.etPin.text.toString()
        val confirmPin = binding.etConfirmPin.text.toString()
        
        // Validación final
        if (!PinHashUtil.isValidPinFormat(pin)) {
            showError("El PIN debe tener 4 dígitos numéricos")
            return
        }
        
        if (pin != confirmPin) {
            showError("Los PINs no coinciden")
            return
        }
        
        val userId = auth.currentUser?.uid
        if (userId == null) {
            showError("Error: Usuario no autenticado")
            finish()
            return
        }
        
        // Hashear el PIN
        val pinHash = PinHashUtil.hashPin(pin)
        if (pinHash == null) {
            showError("Error al procesar el PIN. Intenta de nuevo")
            return
        }
        
        // Deshabilitar botón y mostrar progress
        binding.btnCreatePin.isEnabled = false
        binding.btnCreatePin.alpha = 0.6f
        binding.progressBar.visibility = android.view.View.VISIBLE
        
        // Guardar el hash del PIN en la base de datos
        database.child("users").child(userId).child("pinHash").setValue(pinHash)
            .addOnSuccessListener {
                showSuccess("PIN creado exitosamente")
                
                // Obtener el nombre del usuario para pasar a HomeActivity
                database.child("users").child(userId).child("name").get()
                    .addOnSuccessListener { snapshot ->
                        val name = snapshot.getValue(String::class.java) ?: "Usuario"
                        val safeName = android.net.Uri.encode(name)
                        
                        // Esperar un momento antes de redirigir
                        binding.btnCreatePin.postDelayed({
                            val intent = android.content.Intent(this, HomeActivity::class.java).apply {
                                putExtra("userName", safeName)
                            }
                            startActivity(intent)
                            finish()
                        }, 1000)
                    }
                    .addOnFailureListener {
                        // Continuar de todas formas
                        val intent = android.content.Intent(this, HomeActivity::class.java).apply {
                            putExtra("userName", "Usuario")
                        }
                        startActivity(intent)
                        finish()
                    }
            }
            .addOnFailureListener {
                binding.progressBar.visibility = android.view.View.GONE
                binding.btnCreatePin.isEnabled = true
                binding.btnCreatePin.alpha = 1.0f
                showError("Error al guardar el PIN. Intenta de nuevo")
            }
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

