package eu.vvoleman.gympal.feature.camera.presentation

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import java.util.concurrent.Executors
import android.os.Build
import android.content.Context

actual class CameraController {
    private var cameraProvider: ProcessCameraProvider? = null
    private var cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
    private var preview: Preview? = null
    private var previewView: PreviewView? = null
    private var currentContext: Context? = null
    private var permissionState = mutableStateOf(CameraPermissionState.NOT_DETERMINED)
    private var permissionCallback: (() -> Unit)? = null

    actual fun startCamera() {
        // Implementováno v CameraPreview composable
    }

    actual fun stopCamera() {
        try {
            cameraProvider?.unbindAll()
        } catch (e: Exception) {
            println("Chyba při zastavování kamery: ${e.message}")
        }
    }

    actual fun switchCamera() {
        try {
            cameraSelector = if (cameraSelector == CameraSelector.DEFAULT_FRONT_CAMERA) {
                CameraSelector.DEFAULT_BACK_CAMERA
            } else {
                CameraSelector.DEFAULT_FRONT_CAMERA
            }

            // Znovu připojit kameru s novým selektorem
            previewView?.let { previewView ->
                bindCameraUseCase(previewView)
            }
        } catch (e: Exception) {
            println("Chyba při přepínání kamery: ${e.message}")
        }
    }

    actual fun isCameraAvailable(): Boolean {
        currentContext?.let { context ->
            val manager = context.packageManager
            return manager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)
        }
        return false
    }

    actual fun requestCameraPermission() {
        permissionCallback?.invoke()
    }

    actual fun getCameraPermissionState(): CameraPermissionState {
        return permissionState.value
    }

    actual fun openAppSettings() {
        currentContext?.let { context ->
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }

    actual fun getCurrentFacing(): CameraFacing {
        return if (cameraSelector == CameraSelector.DEFAULT_FRONT_CAMERA) {
            CameraFacing.FRONT
        } else {
            CameraFacing.BACK
        }
    }

    // Nastavení kontextu a kontrola oprávnění
    fun initializeWithContext(
        context: Context,
        onPermissionChanged: (CameraPermissionState) -> Unit
    ) {
        currentContext = context

        // Kontrola aktuálního stavu oprávnění
        val permissionStatus = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
        updatePermissionState(permissionStatus == PackageManager.PERMISSION_GRANTED, onPermissionChanged)
    }

    // Aktualizace stavu oprávnění
    fun updatePermissionState(
        isGranted: Boolean,
        onPermissionChanged: (CameraPermissionState) -> Unit
    ) {
        val newState = if (isGranted) {
            CameraPermissionState.GRANTED
        } else {
            // Na Android je těžké určit, zda bylo oprávnění trvale zamítnuto bez sledování historie
            // Pro jednoduchost používáme DENIED, PERMANENTLY_DENIED bude řešeno v PermissionCallback
            CameraPermissionState.DENIED
        }

        if (permissionState.value != newState) {
            permissionState.value = newState
            onPermissionChanged(newState)
        }
    }

    // Nastavení callback pro vyžádání oprávnění
    fun setPermissionCallback(callback: () -> Unit) {
        permissionCallback = callback
    }

    // Vytvoření a připojení náhledu kamery
    fun bindCameraUseCase(previewView: PreviewView) {
        this.previewView = previewView

        val context = previewView.context
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener({
            try {
                cameraProvider = cameraProviderFuture.get()

                // Vytvořit preview
                preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                try {
                    // Uvolnit předchozí případy použití
                    cameraProvider?.unbindAll()

                    // Navázat use case na kameru
                    val lifecycleOwner = previewView.findViewTreeLifecycleOwner() ?: return@addListener
                    cameraProvider?.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview
                    )
                } catch (exc: Exception) {
                    println("Chyba při bindování kamery: ${exc.message}")
                }
            } catch (exc: Exception) {
                println("Chyba při získávání CameraProvider: ${exc.message}")
            }
        }, ContextCompat.getMainExecutor(context))
    }
}

@Composable
actual fun CameraPreview(
    modifier: Modifier,
    cameraController: CameraController
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var permissionState by remember { mutableStateOf(CameraPermissionState.NOT_DETERMINED) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            cameraController.updatePermissionState(true) { permissionState = it }
        } else {
            // Kontrola, zda bylo oprávnění trvale zamítnuto
            val showRationale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                lifecycleOwner.lifecycle.currentState.isAtLeast(androidx.lifecycle.Lifecycle.State.CREATED) &&
                    context.shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)
            } else {
                // Na starších verzích Androidu nemáme způsob, jak zjistit, zda bylo oprávnění trvale zamítnuto
                true
            }

            cameraController.updatePermissionState(false) {
                permissionState = if (showRationale) {
                    CameraPermissionState.DENIED
                } else {
                    CameraPermissionState.PERMANENTLY_DENIED
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        cameraController.initializeWithContext(context) { permissionState = it }

        // Nastavit callback pro vyžádání oprávnění
        cameraController.setPermissionCallback {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Box(modifier = modifier) {
        if (permissionState == CameraPermissionState.GRANTED) {
            AndroidView(
                factory = { ctx ->
                    val previewView = PreviewView(ctx).apply {
                        this.scaleType = PreviewView.ScaleType.FILL_CENTER
                        layoutParams = android.view.ViewGroup.LayoutParams(
                            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                            android.view.ViewGroup.LayoutParams.MATCH_PARENT
                        )
                    }

                    // Nastavení kamery
                    cameraController.bindCameraUseCase(previewView)

                    previewView
                },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // Zobrazení prázdného prostoru - stav oprávnění bude zpracován v CameraView
        }
    }

    DisposableEffect(lifecycleOwner) {
        onDispose {
            cameraController.stopCamera()
        }
    }
}
