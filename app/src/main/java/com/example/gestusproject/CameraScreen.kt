package com.example.gestusproject

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.SystemClock
import android.util.Size
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavHostController
import com.example.gestusproject.ui.components.BottomChat
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.ImageProcessingOptions
import com.google.mediapipe.tasks.vision.gesturerecognizer.GestureRecognizer
import com.google.mediapipe.tasks.vision.gesturerecognizer.GestureRecognizerResult
import com.google.mediapipe.tasks.vision.gesturerecognizer.GestureRecognizerOptions

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CameraScreen(navController: NavHostController, gestureId: String) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted -> hasCameraPermission = granted }
    )

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    var topGestureLabel by remember { mutableStateOf<String?>(null) }
    var showChat by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        if (hasCameraPermission) {
            CameraPreviewWithGestures(
                lifecycleOwner = lifecycleOwner,
                onGestureResult = { result ->
                    val label = result.topCategoryNameOrNull()
                    if (label != null && label != topGestureLabel) {
                        topGestureLabel = label
                        showChat = true
                    }
                }
            )
        } else {
            Text(
                "Se requiere permiso de cámara",
                style = MaterialTheme.typography.titleMedium
            )
        }

        val isCorrect =
            isGestureCorrectForId(gestureId = gestureId, detectedLabel = topGestureLabel)
        val message =
            if (isCorrect) "Hola correcto ✋" else "Hola incorrecto ❌"

        AnimatedVisibility(
            visible = showChat,
            enter = slideInVertically(initialOffsetY = { it / 2 }, animationSpec = tween(220)) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it / 2 }, animationSpec = tween(220)) + fadeOut()
        ) {
            BottomChat(
                text = message,
                success = isCorrect
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun CameraPreviewWithGestures(
    lifecycleOwner: androidx.lifecycle.LifecycleOwner,
    onGestureResult: (GestureRecognizerResult) -> Unit
) {
    val context = LocalContext.current

    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx)
            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()

                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                val analysis = ImageAnalysis.Builder()
                    .setTargetResolution(Size(640, 480))
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                    .build()

                val gestureRecognizer = createGestureRecognizer(ctx) { result ->
                    onGestureResult(result)
                }

                analysis.setAnalyzer(ContextCompat.getMainExecutor(ctx)) { imageProxy ->
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

                        val mpImage = BitmapImageBuilder(bitmap).build()
                        val options = ImageProcessingOptions.builder()
                            .setRotationDegrees(rotation)
                            .build()

                        // ✅ Esta es la forma correcta en 0.20230731:
                        gestureRecognizer.recognizeAsync(mpImage, options)
                    } catch (_: Exception) {
                        // Ignorar errores del frame
                    } finally {
                        imageProxy.close()
                    }
                }

                val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, analysis)
                } catch (_: Exception) {
                }
            }, ContextCompat.getMainExecutor(ctx))

            previewView
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(420.dp)
    )
}

private fun createGestureRecognizer(
    context: android.content.Context,
    onResult: (GestureRecognizerResult) -> Unit
): GestureRecognizer {
    val baseOptions = BaseOptions.builder()
        .setModelAssetPath("gesture_recognizer.task")
        .build()

    val options = GestureRecognizerOptions.builder()
        .setBaseOptions(baseOptions)
        .setResultListener { result -> onResult(result) }
        .setErrorListener { e -> e?.printStackTrace() }
        .build()

    return GestureRecognizer.createFromOptions(context, options)
}

private fun GestureRecognizerResult.topCategoryNameOrNull(): String? {
    val categories = gestures()
    if (categories.isEmpty()) return null
    val top = categories.firstOrNull()?.maxByOrNull { it.score() } ?: return null
    return top.categoryName()
}

private fun isGestureCorrectForId(gestureId: String, detectedLabel: String?): Boolean {
    if (detectedLabel == null) return false
    val expected = when (gestureId.lowercase()) {
        "hola" -> setOf("Open_Palm")
        "si" -> setOf("Pointing_Up")
        "no" -> setOf("Closed_Fist")
        "gracias", "porfavor" -> setOf("Open_Palm")
        else -> setOf("Open_Palm")
    }
    return expected.any { detectedLabel.contains(it, ignoreCase = true) }
}
