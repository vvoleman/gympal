package eu.vvoleman.gympal.feature.camera.presentation

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.interop.UIKitView
import kotlinx.cinterop.CValue
import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFoundation.*
import platform.CoreGraphics.CGRect
import platform.Foundation.NSError
import platform.Foundation.NSURL
import platform.QuartzCore.CATransaction
import platform.QuartzCore.kCATransactionDisableActions
import platform.UIKit.*

@OptIn(ExperimentalForeignApi::class)
actual class CameraController {
    private val captureSession = AVCaptureSession()
    private var camera: AVCaptureDevice? = null
    private var previewLayer: AVCaptureVideoPreviewLayer? = null
    private var isFrontCamera = true
    private val permissionState = mutableStateOf(CameraPermissionState.NOT_DETERMINED)

    init {
        // Kontrola aktuálního stavu oprávnění při inicializaci
        updatePermissionState()
    }

    actual fun startCamera() {
        // Implementováno v CameraPreview
    }

    actual fun stopCamera() {
        if (captureSession.running) {
            captureSession.stopRunning()
        }
    }

    actual fun switchCamera() {
        isFrontCamera = !isFrontCamera

        // Zastavit aktuální session
        if (captureSession.running) {
            captureSession.stopRunning()
        }

        // Odstranit všechny existující vstupy
        captureSession.inputs.forEach { input ->
            captureSession.removeInput(input as AVCaptureInput)
        }

        // Přidat nový vstup pro vybranou kameru
        setupCameraInput()

        // Restartovat session
        captureSession.startRunning()
    }

    fun setupCaptureSession(bounds: CValue<CGRect>): AVCaptureVideoPreviewLayer? {
        // Pokud nemáme oprávnění, nemůžeme inicializovat kameru
        if (permissionState.value != CameraPermissionState.GRANTED) {
            return null
        }

        try {
            // Nastavení session
            captureSession.sessionPreset = AVCaptureSessionPresetHigh

            // Přidat vstup kamery
            setupCameraInput()

            // Vytvořit preview layer
            previewLayer = AVCaptureVideoPreviewLayer(session = captureSession)
            previewLayer?.videoGravity = AVLayerVideoGravityResizeAspectFill
            previewLayer?.frame = bounds

            // Spustit session
            captureSession.startRunning()
        } catch (e: Exception) {
            println("Chyba při nastavování kamery: ${e}")
            return null
        }

        return previewLayer
    }

    private fun setupCameraInput() {
        val deviceDiscoverySession = AVCaptureDeviceDiscoverySession.discoverySessionWithDeviceTypes(
            deviceTypes = listOf(
                AVCaptureDeviceTypeBuiltInWideAngleCamera
            ),
            mediaType = AVMediaTypeVideo,
            position = if (isFrontCamera) AVCaptureDevicePositionFront else AVCaptureDevicePositionBack
        )

        camera = deviceDiscoverySession?.devices?.firstOrNull()

        camera?.let { device ->
            do {
                try {
                    val input = AVCaptureDeviceInput.deviceInputWithDevice(device, null)
                    if (captureSession.canAddInput(input)) {
                        captureSession.addInput(input)
                    }
                } catch (e: NSError) {
                    println("Chyba při nastavení kamery: ${e.localizedDescription}")
                }
            } while(false) // Hack pro try/catch
        }
    }

    actual fun isCameraAvailable(): Boolean {
        val deviceDiscoverySession = AVCaptureDeviceDiscoverySession.discoverySessionWithDeviceTypes(
            deviceTypes = listOf(
                AVCaptureDeviceTypeBuiltInWideAngleCamera
            ),
            mediaType = AVMediaTypeVideo,
            position = AVCaptureDevicePositionUnspecified
        )

        return (deviceDiscoverySession?.devices?.size ?: 0) > 0
    }

    actual fun requestCameraPermission() {
        if (permissionState.value == CameraPermissionState.NOT_DETERMINED) {
            AVCaptureDevice.requestAccessForMediaType(AVMediaTypeVideo) { granted ->
                permissionState.value = if (granted) CameraPermissionState.GRANTED else CameraPermissionState.DENIED
            }
        }
    }

    actual fun getCameraPermissionState(): CameraPermissionState {
        return permissionState.value
    }

    private fun updatePermissionState() {
        permissionState.value = when (AVCaptureDevice.authorizationStatusForMediaType(AVMediaTypeVideo)) {
            AVAuthorizationStatusAuthorized -> CameraPermissionState.GRANTED
            AVAuthorizationStatusDenied, AVAuthorizationStatusRestricted -> CameraPermissionState.PERMANENTLY_DENIED
            AVAuthorizationStatusNotDetermined -> CameraPermissionState.NOT_DETERMINED
            else -> CameraPermissionState.DENIED
        }
    }

    actual fun openAppSettings() {
        val settingsUrl = NSURL.URLWithString(UIApplicationOpenSettingsURLString)
        UIApplication.sharedApplication.openURL(settingsUrl!!)
    }

    actual fun getCurrentFacing(): CameraFacing {
        return if (isFrontCamera) CameraFacing.FRONT else CameraFacing.BACK
    }
}

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun CameraPreview(
    modifier: Modifier,
    cameraController: CameraController
) {
    var permissionState by remember { mutableStateOf(cameraController.getCameraPermissionState()) }

    LaunchedEffect(Unit) {
        if (permissionState == CameraPermissionState.NOT_DETERMINED) {
            cameraController.requestCameraPermission()
        }
    }

    // Sledování změn stavu oprávnění
    LaunchedEffect(permissionState) {
        permissionState = cameraController.getCameraPermissionState()
    }

    Box(modifier = modifier) {
        if (permissionState == CameraPermissionState.GRANTED) {
            UIKitView(
                modifier = Modifier.fillMaxSize(),
                factory = { context ->
                    val view = UIView()
                    view.backgroundColor = UIColor.blackColor

                    CATransaction.begin()
                    CATransaction.setValue(true, kCATransactionDisableActions)

                    val previewLayer = cameraController.setupCaptureSession(view.bounds)
                    if (previewLayer != null) {
                        view.layer.addSublayer(previewLayer)
                    }

                    CATransaction.commit()

                    view
                },
                onResize = { view, rect ->
                    CATransaction.begin()
                    CATransaction.setValue(true, kCATransactionDisableActions)

                    cameraController.previewLayer?.frame = view.bounds

                    CATransaction.commit()
                }
            )
        } else {
            // Zobrazení prázdného prostoru - stav oprávnění bude zpracován v CameraView
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            cameraController.stopCamera()
        }
    }
}
