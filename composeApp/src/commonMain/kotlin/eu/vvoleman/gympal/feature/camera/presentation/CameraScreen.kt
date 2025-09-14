package eu.vvoleman.gympal.feature.camera.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import eu.vvoleman.gympal.common.domain.service.LogLevel
import eu.vvoleman.gympal.common.domain.service.LoggerInterface
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

/**
 * Stavy kamery
 */
enum class CameraPermissionState {
    GRANTED,
    DENIED,
    NOT_DETERMINED,
    PERMANENTLY_DENIED
}

enum class CameraFacing {
    FRONT,
    BACK
}

object CameraScreen : Screen {
    @Composable
    override fun Content() {
        val logger = koinInject<LoggerInterface>()
        val navigator = LocalNavigator.current

        CameraView(
            onBack = {
                logger.log(LogLevel.INFO, "Navigating back from Camera")
                navigator?.pop()
            }
        )
    }
}

/**
 * Platform-specific camera controller
 */
expect class CameraController() {
    fun startCamera()
    fun stopCamera()
    fun switchCamera()
    fun isCameraAvailable(): Boolean
    fun requestCameraPermission()
    fun getCameraPermissionState(): CameraPermissionState
    fun openAppSettings()
    fun getCurrentFacing(): CameraFacing
}

@Composable
fun CameraView(
    onBack: () -> Unit
) {
    val cameraController = remember { CameraController() }
    var permissionState by remember { mutableStateOf(cameraController.getCameraPermissionState()) }
    var isCameraAvailable by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (permissionState == CameraPermissionState.GRANTED) {
            isCameraAvailable = cameraController.isCameraAvailable()
            if (isCameraAvailable) {
                cameraController.startCamera()
            }
        } else if (permissionState == CameraPermissionState.NOT_DETERMINED) {
            cameraController.requestCameraPermission()
        }
    }

    // Sledování změn stavu oprávnění
    LaunchedEffect(permissionState) {
        if (permissionState == CameraPermissionState.GRANTED) {
            isCameraAvailable = cameraController.isCameraAvailable()
            if (isCameraAvailable) {
                cameraController.startCamera()
            }
        }
    }

    val coroutineScope = rememberCoroutineScope()

    val snackbarHostState = remember { androidx.compose.material3.SnackbarHostState() }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (permissionState) {
                CameraPermissionState.GRANTED -> {
                    if (isCameraAvailable) {
                        // Camera view se implementuje na platformě specifickým způsobem
                        CameraPreview(
                            modifier = Modifier.fillMaxSize(),
                            cameraController = cameraController
                        )

                        // Ovládací prvky kamery
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Button(onClick = {
                                try {
                                    cameraController.switchCamera()
                                } catch (e: Exception) {
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar(
                                            message = "Zařízení nepodporuje přepínání kamer",
                                        )
                                    }
                                    // show snackbar or similar
                                }
                            }) {
                                Text(if (cameraController.getCurrentFacing() == CameraFacing.FRONT)
                                    "Přepnout na zadní kameru"
                                    else "Přepnout na přední kameru")
                            }

                            Button(onClick = onBack) {
                                Text("Zpět")
                            }
                        }
                    } else {
                        // Kamera je nedostupná i přes udělené oprávnění
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                "Kamera není k dispozici na tomto zařízení",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.error
                            )

                            Button(
                                onClick = onBack,
                                modifier = Modifier.padding(top = 16.dp)
                            ) {
                                Text("Zpět")
                            }
                        }
                    }
                }
                CameraPermissionState.DENIED, CameraPermissionState.NOT_DETERMINED -> {
                    // Oprávnění k použití kamery bylo zamítnuto
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            "Pro použití kamery je potřeba udělit oprávnění",
                            style = MaterialTheme.typography.bodyLarge
                        )

                        Button(
                            onClick = { cameraController.requestCameraPermission() },
                            modifier = Modifier.padding(top = 16.dp)
                        ) {
                            Text("Udělit oprávnění")
                        }

                        Button(
                            onClick = onBack,
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Text("Zpět")
                        }
                    }
                }
                CameraPermissionState.PERMANENTLY_DENIED -> {
                    // Oprávnění k použití kamery bylo trvale zamítnuto
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            "Oprávnění k použití kamery bylo zamítnuto",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error
                        )

                        Text(
                            "Pro použití kamery je potřeba povolit oprávnění v nastavení aplikace",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 8.dp, start = 32.dp, end = 32.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )

                        Button(
                            onClick = { cameraController.openAppSettings() },
                            modifier = Modifier.padding(top = 16.dp)
                        ) {
                            Text("Otevřít nastavení")
                        }

                        Button(
                            onClick = onBack,
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Text("Zpět")
                        }
                    }
                }
            }
        }
    }
}

/**
 * Platform-specific camera preview
 */
@Composable
expect fun CameraPreview(
    modifier: Modifier = Modifier,
    cameraController: CameraController
)
