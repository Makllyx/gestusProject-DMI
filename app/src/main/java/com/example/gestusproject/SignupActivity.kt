package com.example.gestusproject

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.content.res.ColorStateList
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.gestusproject.databinding.ActivitySignupBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

/**
 * SignupActivity - Pantalla de registro
 * 
 * Esta actividad permite a los usuarios crear una cuenta nueva con:
 * - Nombre completo (solo letras y espacios, sin números ni símbolos)
 * - Email con validación de formato
 * - Contraseña con validación en tiempo real que muestra:
 *   - Mínimo 8 caracteres
 *   - Al menos una mayúscula
 *   - Al menos un número
 * 
 * Características principales:
 * 1. Validación de nombre: solo letras y espacios (sin números ni símbolos)
 * 2. Validación de contraseña: muestra checkmarks en verde cuando se cumplen los requisitos
 * 3. Barra de progreso que indica la fortaleza de la contraseña
 * 4. El botón de registro se habilita solo cuando todos los campos son válidos
 * 5. Los campos se iluminan con color naranja (#FA6609) cuando están enfocados
 * 6. Diseño accesible con contentDescription para lectores de pantalla
 */
class SignupActivity : AppCompatActivity() {
    
    // Binding para acceder a las vistas de forma segura
    private lateinit var binding: ActivitySignupBinding
    
    // Instancia de Firebase Auth
    private val auth = FirebaseAuth.getInstance()
    
    // Referencia a la base de datos de Firebase
    private val database = FirebaseDatabase.getInstance().reference
    
    // Variables para controlar el estado de los campos
    private var nameValid = false
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
        binding = ActivitySignupBinding.inflate(layoutInflater)
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
        // Click listener del botón de crear cuenta
        binding.btnCreateAccount.setOnClickListener { performSignup() }
        
        // Click listener del enlace de login
        binding.tvLoginLink.setOnClickListener {
            startActivity(android.content.Intent(this, LoginActivity::class.java))
            finish()
        }
        
        // TextWatcher para el campo de nombre - valida solo letras y espacios
        binding.etName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            
            override fun afterTextChanged(s: Editable?) {
                validateName(s.toString())
                validateFields()
            }
        })
        
        // TextWatcher para el campo de email
        binding.etEmail.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            
            override fun afterTextChanged(s: Editable?) {
                validateEmail(s.toString())
                validateFields()
            }
        })
        
        // TextWatcher para el campo de contraseña - valida y muestra feedback visual
        binding.etPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            
            override fun afterTextChanged(s: Editable?) {
                validatePassword(s.toString())
                validateFields()
            }
        })
        
        // Cambiar color cuando los campos obtienen/ Pierden el foco
        binding.etName.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) highlightField(binding.tilName) else resetFieldHighlight(binding.tilName)
        }
        
        binding.etEmail.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) highlightField(binding.tilEmail) else resetFieldHighlight(binding.tilEmail)
        }
        
        binding.etPassword.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) highlightField(binding.tilPassword) else resetFieldHighlight(binding.tilPassword)
        }
    }

    /**
     * Valida que el nombre contenga solo letras y espacios
     * No permite números ni símbolos
     * @param name El nombre a validar
     */
    private fun validateName(name: String) {
        val trimmed = name.trim()
        
        when {
            trimmed.isEmpty() -> {
                binding.tilName.error = null
                nameValid = false
            }
            !trimmed.matches(Regex("^[a-zA-ZáéíóúÁÉÍÓÚñÑüÜ\\s]+$")) -> {
                binding.tilName.error = "Solo se permiten letras y espacios"
                binding.tilName.errorIconDrawable = null
                nameValid = false
            }
            else -> {
                binding.tilName.error = null
                nameValid = true
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
     * Valida la contraseña según los requisitos:
     * - Mínimo 8 caracteres
     * - Al menos una mayúscula
     * - Al menos un número
     * (NO se requiere carácter especial)
     * 
     * Muestra feedback visual con checkmarks en verde cuando se cumplen los requisitos
     * @param password La contraseña a validar
     */
    private fun validatePassword(password: String) {
        // Validaciones individuales
        val lengthValid = password.length >= 8
        val upperValid = password.any { it.isUpperCase() }
        val lowerValid = password.any { it.isLowerCase() }
        val digitValid = password.any { it.isDigit() }
        
        // Actualizar visualización de cada requisito
        updateValidationRow(binding.tvLength, lengthValid, "Mínimo 8 caracteres")
        updateValidationRow(binding.tvUppercase, upperValid, "Al menos una mayúscula")
        updateValidationRow(binding.tvDigit, digitValid, "Al menos un número")
        
        // Simular validaciones adicionales para compatibilidad con layout anterior
        updateValidationRow(binding.tvLowercase, lowerValid, "")
        updateValidationRow(binding.tvSpecial, true, "")
        
        // Calcular fortaleza de la contraseña
        val strength = listOf(lengthValid, upperValid, lowerValid, digitValid).count { it }
        updateStrengthBar(strength)
        
        // La contraseña es válida si se cumplen todos los requisitos mínimos
        passwordValid = lengthValid && upperValid && lowerValid && digitValid
    }

    /**
     * Actualiza visualmente cada regla de validación
     * Muestra checkmark verde (✓) si es válida, círculo vacío (○) si no
     * @param textView TextView que muestra la regla
     * @param valid Si la regla se cumple
     * @param text Texto de la regla
     */
    private fun updateValidationRow(textView: android.widget.TextView, valid: Boolean, text: String) {
        if (text.isEmpty()) return
        
        textView.text = if (valid) "✓ $text" else "○ $text"
        textView.setTextColor(
            if (valid) ContextCompat.getColor(this, R.color.success_green)
            else ContextCompat.getColor(this, R.color.error_red)
        )
    }

    /**
     * Actualiza la barra de progreso y el texto de fortaleza
     * @param strength Número de requisitos cumplidos (0-4)
     */
    private fun updateStrengthBar(strength: Int) {
        binding.progressStrength.progress = (strength * 25) // 0-100 basado en 4 requisitos
        binding.tvStrength.text = when (strength) {
            0, 1 -> "Muy débil"
            2 -> "Débil"
            3 -> "Medio"
            4 -> "Fuerte"
            else -> ""
        }
        
        // Cambiar color de la barra según la fortaleza
        binding.progressStrength.progressTintList = ColorStateList.valueOf(
            when {
                strength <= 1 -> ContextCompat.getColor(this, R.color.error_red)
                strength == 2 -> Color.parseColor("#FFA500") // Naranja
                strength == 3 -> Color.parseColor("#90EE90") // Verde claro
                else -> ContextCompat.getColor(this, R.color.success_green)
            }
        )
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
     * Habilita o deshabilita el botón de registro según el estado de los campos
     */
    private fun validateFields() {
        binding.btnCreateAccount.isEnabled = nameValid && emailValid && passwordValid
        
        // Cambiar opacidad del botón visualmente
        if (binding.btnCreateAccount.isEnabled) {
            binding.btnCreateAccount.alpha = 1.0f
        } else {
            binding.btnCreateAccount.alpha = 0.6f
        }
    }

    /**
     * Realiza el proceso de registro con Firebase
     */
    private fun performSignup() {
        val name = binding.etName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString()
        
        // Validación adicional
        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            showError("Completa todos los campos")
            return
        }
        
        // Deshabilitar botón y mostrar progress
        binding.btnCreateAccount.isEnabled = false
        binding.btnCreateAccount.alpha = 0.6f
        
        // Intentar crear cuenta con Firebase Auth
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = task.result?.user?.uid ?: return@addOnCompleteListener
                    
                    // Guardar datos adicionales del usuario en la base de datos
                    val userData = mapOf(
                        "name" to name,
                        "email" to email
                    )
                    
                    database.child("users").child(userId).setValue(userData)
                        .addOnSuccessListener {
                            showSuccess("Registro exitoso. Configura tu PIN...")
                            // Redirigir a CreatePinActivity para crear el PIN
                            binding.btnCreateAccount.postDelayed({
                                val intent = android.content.Intent(this, CreatePinActivity::class.java)
                                startActivity(intent)
                                finish()
                            }, 1000)
                        }
                        .addOnFailureListener {
                            showError("Error al guardar datos del usuario")
                            binding.btnCreateAccount.isEnabled = true
                            binding.btnCreateAccount.alpha = 1.0f
                        }
                } else {
                    // Mostrar error de Firebase
                    val errorMessage = when {
                        task.exception?.message?.contains("weak-password") == true -> 
                            "La contraseña es demasiado débil"
                        task.exception?.message?.contains("email-already-in-use") == true -> 
                            "Este email ya está registrado"
                        task.exception?.message?.contains("invalid-email") == true -> 
                            "Email inválido"
                        else -> "Error: ${task.exception?.message}"
                    }
                    showError(errorMessage)
                    binding.btnCreateAccount.isEnabled = true
                    binding.btnCreateAccount.alpha = 1.0f
                }
            }
    }

    /**
     * Muestra un mensaje de error usando Snackbar
     */
    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
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
