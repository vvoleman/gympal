package eu.vvoleman.gympal.feature.camera.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.bytedeco.javacv.Frame
import org.bytedeco.javacv.Java2DFrameConverter
import org.bytedeco.javacv.OpenCVFrameConverter
import org.bytedeco.javacv.OpenCVFrameGrabber
import org.jetbrains.skia.Image
import java.awt.image.BufferedImage
import java.nio.ByteBuffer
import java.util.concurrent.atomic.AtomicBoolean

actual class CameraController actual constructor() {

    private var frameGrabber = OpenCVFrameGrabber(0)
    private val running = AtomicBoolean(false)
    private var cameraIndex: Int = 0

    private val frameConverter = OpenCVFrameConverter.ToMat()

    // holds latest frame as ImageBitmap
    private val _currentImage = mutableStateOf<ImageBitmap?>(null)
    val currentImage: ImageBitmap? get() = _currentImage.value

    actual fun startCamera() {
        try {
            frameGrabber.start()
            running.set(true)
        } catch (e: Exception) {
            println("Nepodařilo se spustit kameru: ${e.message}")
            running.set(false)
        }
    }

    actual fun stopCamera() {
        running.set(false)
        try {
            frameGrabber.stop()
        } catch (_: Exception) {
        }
    }

    /** Loop that captures frames until stopCamera() is called. */
    private val java2dConverter = Java2DFrameConverter()

    suspend fun captureFrames() {
        withContext(Dispatchers.IO) {
            while (running.get()) {
                try {
                    val frame: Frame = frameGrabber.grab() ?: continue

                    // Convert frame directly to BufferedImage
                    val buffered: BufferedImage? = java2dConverter.convert(frame)
                    if (buffered != null) {
                        _currentImage.value = buffered.toComposeImageBitmap()
                    }

                } catch (e: Exception) {
                    println("Error grabbing frame: ${e.message}")
                    delay(50)
                }
            }
        }
    }


    /** Utility to convert BGR24 → RGBA32 */
    private fun convertBGRtoRGBA(src: ByteBuffer, width: Int, height: Int): ByteBuffer {
        val dst = ByteBuffer.allocateDirect(width * height * 4)
        for (i in 0 until width * height) {
            val b = src.get(i * 3).toInt() and 0xFF
            val g = src.get(i * 3 + 1).toInt() and 0xFF
            val r = src.get(i * 3 + 2).toInt() and 0xFF
            dst.put(i * 4, r.toByte())
            dst.put(i * 4 + 1, g.toByte())
            dst.put(i * 4 + 2, b.toByte())
            dst.put(i * 4 + 3, 0xFF.toByte())
        }
        return dst
    }

    actual fun switchCamera() {
        throw Exception("Doesnt support switching camera on JVM desktop")
        try {
            // naive toggle between 0 and 1 (desktop numbering is device-dependent)
            val newIndex = if (cameraIndex == 0) 1 else 0

            stopCamera()
            cameraIndex = newIndex
            frameGrabber = OpenCVFrameGrabber(cameraIndex)
            startCamera()
        } catch (e: Exception) {
            println("Chyba při přepínání kamery: ${e.message}")
        }
    }

    actual fun isCameraAvailable(): Boolean {
        return try {
            val testGrabber = OpenCVFrameGrabber(0)
            testGrabber.start()
            val available = testGrabber.grab() != null
            testGrabber.stop()
            available
        } catch (_: Exception) {
            false
        }
    }

    actual fun requestCameraPermission() {
        // JVM desktop has no runtime permissions → nothing to do
    }

    actual fun getCameraPermissionState(): CameraPermissionState {
        return if (isCameraAvailable()) {
            CameraPermissionState.GRANTED
        } else {
            CameraPermissionState.DENIED
        }
    }

    actual fun openAppSettings() {
        // Not supported on desktop JVM
    }

    actual fun getCurrentFacing(): CameraFacing {
        // Desktop doesn't define front/back reliably; heuristically map index 0 -> FRONT
        return if (cameraIndex == 0) CameraFacing.FRONT else CameraFacing.BACK
    }
}


@Composable
actual fun CameraPreview(
    modifier: Modifier,
    cameraController: CameraController
) {
    var cameraAvailable by remember { mutableStateOf<Boolean?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        cameraAvailable = cameraController.isCameraAvailable()
        if (cameraAvailable == true) {
            cameraController.startCamera()
            scope.launch { cameraController.captureFrames() }
        }
    }

    Box(modifier = modifier.background(Color.Black)) {
        when (cameraAvailable) {
            true -> {
                cameraController.currentImage?.let { bitmap ->
                    Image(
                        bitmap = bitmap,
                        contentDescription = "Náhled kamery",
                        modifier = Modifier.fillMaxSize()
                    )
                } ?: Text(
                    "Inicializace kamery...",
                    color = Color.White,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            false -> Text(
                "Kamera není dostupná",
                color = Color.White,
                modifier = Modifier.align(Alignment.Center)
            )
            null -> Text(
                "Kontrola dostupnosti kamery...",
                color = Color.White,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            cameraController.stopCamera()
        }
    }
}
