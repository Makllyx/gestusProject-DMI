package com.example.gestusproject

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.os.SystemClock
import android.util.Size
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.example.gestusproject.databinding.ActivityCameraBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.gesturerecognizer.GestureRecognizer
import com.google.mediapipe.tasks.vision.gesturerecognizer.GestureRecognizer.GestureRecognizerOptions
import com.google.mediapipe.tasks.vision.gesturerecognizer.GestureRecognizerResult
import java.util.concurrent.Executors

class CameraActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCameraBinding
    private var gestureRecognizer: GestureRecognizer? = null
    private var topGestureLabel: String? = null
    private var topGestureScore: Float? = null
    private var showChat = false
    private var showSuccessMessage = false
    private lateinit var gestureId: String

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val database = FirebaseDatabase.getInstance().reference

    private var lastSavedLabel: String? = null
    private var lastSavedCorrectState: Boolean = false

    private fun formatPercentage(score: Float): String {
        val percentage = (score * 100).coerceIn(0f, 100f)
        return "${percentage.toInt()}%"
    }

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            setupCamera()
        } else {
            binding.layoutPermissionInfo.visibility = android.view.View.VISIBLE
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)

        gestureId = intent.getStringExtra("gestureId") ?: "hola"

        setupToolbar()
        checkCameraPermission()
        setupGestureRecognizer()

        // Forzar que el FAB quede arriba de todo
        binding.fabClose.post {
            binding.fabClose.bringToFront()
            binding.fabClose.z = 999f
            binding.fabClose.elevation = 999f
        }


    }

    private fun setupToolbar() {
        val gestureDisplayName = getGestureDisplayName(gestureId)
        binding.tvGestureName.text = "Practica el gesto: $gestureDisplayName"

        val instructions = when (gestureId.lowercase()) {
            "hola" -> "Muestra tu mano abierta con la palma hacia la cámara"
            "si" -> "Cierra tu mano en un puño"
            "no" -> "Muestra tu pulgar hacia abajo"
            "bien" -> "Muestra tu pulgar hacia arriba"
            "uno" -> "Apunta hacia arriba con tu dedo índice"
            "dos" -> "Muestra el gesto de victoria (índice y medio)"
            "te_amo" -> "Muestra el gesto de te amo (pulgar, índice y meñique extendidos)"
            else -> "Mantén tu mano visible en la cámara"
        }
        binding.tvGestureInstructions.text = instructions

        binding.fabClose.setOnClickListener {
            finish()
        }
    }

    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            binding.layoutPermissionInfo.visibility = android.view.View.GONE
            setupCamera()
        } else {
            binding.layoutPermissionInfo.visibility = android.view.View.VISIBLE
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun setupCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.previewView.surfaceProvider)
                }

            val analysis = ImageAnalysis.Builder()
                .setDefaultResolution(Size(640, 480))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                .build()

            analysis.setAnalyzer(Executors.newSingleThreadExecutor()) { imageProxy ->
                try {
                    val rotation = imageProxy.imageInfo.rotationDegrees
                    val buffer = imageProxy.planes[0].buffer
                    buffer.rewind()

                    val bitmap = Bitmap.createBitmap(
                        imageProxy.width,
                        imageProxy.height,
                        Bitmap.Config.ARGB_8888
                    )
                    bitmap.copyPixelsFromBuffer(buffer)

                    val rotatedBitmap = if (rotation != 0) rotateBitmap(bitmap, rotation.toFloat()) else bitmap
                    val mpImage = BitmapImageBuilder(rotatedBitmap).build()

                    gestureRecognizer?.recognizeAsync(mpImage, SystemClock.uptimeMillis())
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    try {
                        imageProxy.close()
                    } catch (_: Exception) {}
                }
            }

            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, analysis)
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    binding.layoutPermissionInfo.visibility = android.view.View.VISIBLE
                    binding.tvPermissionInfo.text = "Error al iniciar la cámara."
                }
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun setupGestureRecognizer() {
        try {
            val baseOptions = BaseOptions.builder()
                .setModelAssetPath("gesture_recognizer.task")
                .build()

            val options = GestureRecognizerOptions.builder()
                .setBaseOptions(baseOptions)
                .setRunningMode(RunningMode.LIVE_STREAM)
                .setResultListener { result: GestureRecognizerResult, _ ->
                    try {
                        handleGestureResult(result)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                .setErrorListener { e: Exception? -> e?.printStackTrace() }
                .build()

            gestureRecognizer = GestureRecognizer.createFromOptions(this, options)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun handleGestureResult(result: GestureRecognizerResult) {
        runOnUiThread {
            val label = result.topCategoryNameOrNull() ?: return@runOnUiThread
            val score = result.topCategoryScoreOrZero()

            topGestureLabel = label
            topGestureScore = score
            showChat = true

            val isCorrect = isGestureCorrectForId(gestureId, label)
            updateFeedback()

            if (isCorrect) {
                val finalLabel = if (gestureId.equals("hola", ignoreCase = true)) "Open_Palm" else label

                if (lastSavedLabel != finalLabel || !lastSavedCorrectState) {
                    saveGestureAttempt(finalLabel, score, true)
                    lastSavedLabel = finalLabel
                    lastSavedCorrectState = true
                }
            } else {
                if (lastSavedCorrectState) {
                    lastSavedCorrectState = false
                }
            }

            // Mostrar mensaje verde
            if (isCorrect && !showSuccessMessage) {
                val gestureDisplayName = getGestureDisplayName(gestureId)
                binding.tvSuccessMessage.text =
                    "¡$gestureDisplayName detectado correctamente! 🎉 (${formatPercentage(topGestureScore ?: 0f)})"
                binding.cardSuccessMessage.visibility = android.view.View.VISIBLE
                showSuccessMessage = true

                binding.root.postDelayed({
                    binding.cardSuccessMessage.visibility = android.view.View.GONE
                    showSuccessMessage = false
                }, 3000)
            }
        }
    }

    private fun updateFeedback() {
        val isCorrect = isGestureCorrectForId(gestureId, topGestureLabel)
        val gestureDisplayName = getGestureDisplayName(gestureId)

        binding.cardFeedback.setCardBackgroundColor(
            ContextCompat.getColor(
                this,
                if (isCorrect) R.color.success_green else R.color.error_red
            )
        )

        binding.ivFeedbackIcon.setImageResource(
            if (isCorrect) android.R.drawable.ic_dialog_info
            else android.R.drawable.ic_menu_close_clear_cancel
        )

        binding.tvFeedbackMessage.text =
            if (isCorrect) "$gestureDisplayName correcto! ✨"
            else "Inténtalo de nuevo"

        binding.tvDetectedGesture.text =
            topGestureLabel?.let { "Detectado: $it" } ?: "Detectado: --"
    }

    private fun GestureRecognizerResult.topCategoryNameOrNull(): String? {
        val categories = gestures()
        if (categories.isEmpty()) return null
        val top = categories.firstOrNull()?.maxByOrNull { it.score() } ?: return null
        return top.categoryName()
    }

    private fun GestureRecognizerResult.topCategoryScoreOrZero(): Float {
        val categories = gestures()
        if (categories.isEmpty()) return 0f
        val top = categories.firstOrNull()?.maxByOrNull { it.score() } ?: return 0f
        return top.score()
    }

    private fun getGestureDisplayName(gestureId: String): String {
        return when (gestureId.lowercase()) {
            "hola" -> "Hola"
            "si" -> "Sí"
            "no" -> "No"
            "bien" -> "Bien"
            "uno" -> "Uno"
            "dos" -> "Dos"
            "te_amo" -> "Te Amo"
            else -> gestureId.replaceFirstChar { it.uppercase() }
        }
    }

    private fun getExpectedGesturesForId(gestureId: String): Set<String> {
        return when (gestureId.lowercase()) {
            "hola" -> setOf("Open_Palm")
            "si" -> setOf("Closed_Fist")
            "no" -> setOf("Thumb_Down")
            "bien" -> setOf("Thumb_Up")
            "uno" -> setOf("Pointing_Up")
            "dos" -> setOf("Victory")
            "te_amo" -> setOf("ILoveYou")
            else -> emptySet()
        }
    }

    private fun isGestureCorrectForId(gestureId: String, detectedLabel: String?): Boolean {
        if (detectedLabel == null) return false
        val expected = getExpectedGesturesForId(gestureId)
        return expected.any { detectedLabel.contains(it, ignoreCase = true) }
    }

    private fun saveGestureAttempt(label: String, score: Float, isCorrect: Boolean) {
        val userId = auth.currentUser?.uid ?: return
        val attemptRef = database
            .child("gestureAttempts")
            .child(userId)
            .child(gestureId)
            .push()

        val attemptData = mapOf(
            "gestureId" to gestureId,
            "detectedLabel" to label,
            "score" to score, // ← ESTA ES LA LÍNEA NUEVA
            "percentage" to (score * 100f),
            "isCorrect" to isCorrect,
            "timestamp" to System.currentTimeMillis()
        )

        attemptRef.setValue(attemptData)
    }


    private fun rotateBitmap(source: Bitmap, angleDegrees: Float): Bitmap {
        val matrix = android.graphics.Matrix().apply { postRotate(angleDegrees) }
        return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        gestureRecognizer?.close()
    }


}
