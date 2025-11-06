package com.example.gestusproject

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.gestusproject.databinding.ActivityLoginBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.launch

// (fix) Eliminada declaración inválida añadida por error

/**
 * LoginActivity - Pantalla de inicio de sesión
 * 
 * Esta actividad permite a los usuarios iniciar sesión con email y contraseña.
 * Incluye validaciones visuales en tiempo real y feedback inmediato.
 * 
 * Características principales:
 * 1. Validación de email con formato correcto
 * 2. Los campos se iluminan con color naranja (#FA6609) cuando están enfocados
 * 3. El botón de login se habilita solo cuando todos los campos son válidos
 * 4. Mensajes de error claros en rojo debajo de los campos
 * 5. Indicador de progreso durante el inicio de sesión
 * 6. Diseño accesible con contentDescription para lectores de pantalla
 */
class LoginActivity : AppCompatActivity() {
    
    // Binding para acceder a las vistas de forma segura
    private lateinit var binding: ActivityLoginBinding
    
    // Instancia de Firebase Auth
    private val auth = FirebaseAuth.getInstance()
    
    // Referencia a la base de datos de Firebase
    private val database = FirebaseDatabase.getInstance().reference
    
    // Flag para evitar múltiples intentos de login
    private var isLoading = false
    
    // Variables para controlar el estado de los campos
    private var emailValid = false
    private var passwordValid = false
    
    /**
     * Launcher para solicitar permisos de notificaciones (Android 13+)
     */
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            Toast.makeText(this, "Permiso de notificaciones requerido", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Inflar el layout usando View Binding
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Solicitar permisos si es necesario
        requestNotificationPermission()
        
        // Configurar listeners y validaciones
        setupViews()
    }

    /**
     * Solicita permiso de notificaciones en Android 13+
     */
    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    /**
     * Configura todos los listeners y eventos de los campos
     */
    private fun setupViews() {
        // Click listener del botón de login
        binding.btnLogin.setOnClickListener { performLogin() }
        
        // Click listener del enlace de registro
        binding.tvSignUpLink.setOnClickListener {
            startActivity(android.content.Intent(this, SignupActivity::class.java))
            finish()
        }
        
        // TextWatcher para el campo de email - valida en tiempo real
        binding.etEmail.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            
            override fun afterTextChanged(s: Editable?) {
                validateEmail(s.toString())
                validateFields()
            }
        })
        
        // TextWatcher para el campo de contraseña
        binding.etPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            
            override fun afterTextChanged(s: Editable?) {
                validatePassword(s.toString())
                validateFields()
            }
        })
        
        // Cambiar color cuando el campo de email obtiene/ Pierde el foco
        binding.etEmail.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                highlightField(binding.tilEmail)
            } else {
                resetFieldHighlight(binding.tilEmail)
            }
        }
        
        // Cambiar color cuando el campo de contraseña obtiene/ Pierde el foco
        binding.etPassword.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                highlightField(binding.tilPassword)
            } else {
                resetFieldHighlight(binding.tilPassword)
            }
        }
    }

    /**
     * Valida que el email tenga formato correcto
     * @param email El email a validar
     */
    private fun validateEmail(email: String) {
        val trimmed = email.trim()
        
        when {
            trimmed.isEmpty() -> {
                binding.tilEmail.error = null
                emailValid = false
            }
            !Patterns.EMAIL_ADDRESS.matcher(trimmed).matches() -> {
                binding.tilEmail.error = "Email inválido"
                binding.tilEmail.errorIconDrawable = null
                emailValid = false
            }
            else -> {
                binding.tilEmail.error = null
                emailValid = true
            }
        }
    }

    /**
     * Valida que la contraseña tenga contenido
     * @param password La contraseña a validar
     */
    private fun validatePassword(password: String) {
        passwordValid = password.isNotBlank()
        
        if (!passwordValid && password.isNotEmpty()) {
            binding.tilPassword.error = "La contraseña no puede estar vacía"
        } else {
            binding.tilPassword.error = null
        }
    }

    /**
     * Destaca visualmente el campo cuando está enfocado
     */
    private fun highlightField(textInputLayout: com.google.android.material.textfield.TextInputLayout) {
        textInputLayout.boxStrokeColor = ContextCompat.getColor(this, R.color.orange_primary)
        textInputLayout.defaultHintTextColor = ColorStateList.valueOf(
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
     * Habilita o deshabilita el botón de login según el estado de los campos
     */
    private fun validateFields() {
        binding.btnLogin.isEnabled = emailValid && passwordValid && !isLoading
        
        // Cambiar opacidad del botón visualmente
        if (binding.btnLogin.isEnabled) {
            binding.btnLogin.alpha = 1.0f
        } else {
            binding.btnLogin.alpha = 0.6f
        }
    }

    /**
     * Realiza el proceso de inicio de sesión con Firebase
     */
    private fun performLogin() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString()
        
        // Validación adicional antes de proceder
        if (email.isEmpty() || password.isEmpty()) {
            showError("Completa todos los campos")
            return
        }
        
        // Deshabilitar botón y mostrar progress
        isLoading = true
        binding.btnLogin.isEnabled = false
        binding.btnLogin.alpha = 0.6f
        binding.progressBarLogin.visibility = View.VISIBLE
        
        // Intentar iniciar sesión con Firebase Auth
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                isLoading = false
                binding.progressBarLogin.visibility = View.GONE
                
                if (task.isSuccessful) {
                    // Mostrar mensaje de éxito
                    showSuccess("Autenticación exitosa")
                    
                    // Redirigir a VerifyPinActivity para verificar el PIN
                    val intent = android.content.Intent(this, VerifyPinActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    // Mostrar error de Firebase
                    showError("Error: ${task.exception?.message}")
                    binding.btnLogin.isEnabled = true
                    binding.btnLogin.alpha = 1.0f
                }
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
