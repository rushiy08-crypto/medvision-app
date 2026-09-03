package com.medvision.app

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat
import com.medvision.app.domain.usecase.SpeechRecognitionUseCase
import com.medvision.app.domain.usecase.TextToSpeechUseCase
import com.medvision.app.ui.HomeScreen
import com.medvision.app.ui.MainViewModel
import com.medvision.app.ui.UiState

class MainActivity : ComponentActivity() {

    private lateinit var mainViewModel: MainViewModel
    private lateinit var speechRecognitionUseCase: SpeechRecognitionUseCase
    private lateinit var textToSpeechUseCase: TextToSpeechUseCase

    private val requestCameraPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (!isGranted) {
                textToSpeechUseCase.apply {
                    // Handle permission denied
                }
            }
        }

    private val requestMicrophonePermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (!isGranted) {
                textToSpeechUseCase.apply {
                    // Handle permission denied
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize use cases
        speechRecognitionUseCase = SpeechRecognitionUseCase(this)
        textToSpeechUseCase = TextToSpeechUseCase(this)

        // Initialize ViewModel
        mainViewModel = MainViewModel(speechRecognitionUseCase, textToSpeechUseCase)

        // Request permissions
        requestPermissions()

        setContent {
            val uiState by mainViewModel.uiState.collectAsState()
            val statusMessage by mainViewModel.statusMessage.collectAsState()

            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color.White
            ) {
                when (uiState) {
                    is UiState.Idle -> {
                        HomeScreen(
                            isListening = false,
                            statusMessage = "MedVision is ready. Tap to speak.",
                            onMicrophoneClick = {
                                mainViewModel.startListening()
                            },
                            onCameraClick = {
                                requestCameraPermissionIfNeeded()
                            },
                            onStopClick = {
                                mainViewModel.stopListening()
                            }
                        )
                    }
                    is UiState.Listening -> {
                        HomeScreen(
                            isListening = true,
                            statusMessage = "Listening...",
                            onMicrophoneClick = {
                                mainViewModel.stopListening()
                            },
                            onCameraClick = {},
                            onStopClick = {
                                mainViewModel.stopListening()
                            }
                        )
                    }
                    is UiState.CameraReady -> {
                        HomeScreen(
                            isListening = false,
                            statusMessage = "Camera is ready. Ask me what you want to know.",
                            onMicrophoneClick = {
                                mainViewModel.startListening()
                            },
                            onCameraClick = {},
                            onStopClick = {
                                mainViewModel.stopListening()
                            }
                        )
                    }
                    is UiState.Analyzing -> {
                        HomeScreen(
                            isListening = false,
                            statusMessage = "Processing your request...",
                            onMicrophoneClick = {},
                            onCameraClick = {},
                            onStopClick = {
                                mainViewModel.stopListening()
                            }
                        )
                    }
                    is UiState.Reading -> {
                        HomeScreen(
                            isListening = false,
                            statusMessage = "Reading text...",
                            onMicrophoneClick = {},
                            onCameraClick = {},
                            onStopClick = {
                                mainViewModel.stopListening()
                            }
                        )
                    }
                    is UiState.ContinuousVision -> {
                        HomeScreen(
                            isListening = false,
                            statusMessage = "Continuous vision active...",
                            onMicrophoneClick = {},
                            onCameraClick = {},
                            onStopClick = {
                                mainViewModel.stopListening()
                            }
                        )
                    }
                }
            }
        }
    }

    private fun requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.RECORD_AUDIO
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestMicrophonePermission.launch(android.Manifest.permission.RECORD_AUDIO)
            }
        }
    }

    private fun requestCameraPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.CAMERA
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestCameraPermission.launch(android.Manifest.permission.CAMERA)
            }
        }
    }
}
