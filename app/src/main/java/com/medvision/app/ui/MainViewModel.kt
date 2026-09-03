package com.medvision.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medvision.app.domain.usecase.SpeechRecognitionUseCase
import com.medvision.app.domain.usecase.TextToSpeechUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MainViewModel(
    private val speechRecognitionUseCase: SpeechRecognitionUseCase,
    private val textToSpeechUseCase: TextToSpeechUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState

    private val _statusMessage = MutableStateFlow("")
    val statusMessage: StateFlow<String> = _statusMessage

    init {
        viewModelScope.launch {
            speechRecognitionUseCase.recognitionResults.collect { result ->
                handleVoiceCommand(result)
            }
        }
    }

    fun startListening() {
        _uiState.value = UiState.Listening
        viewModelScope.launch {
            textToSpeechUseCase.speak("I'm listening.")
        }
        speechRecognitionUseCase.startListening()
    }

    fun stopListening() {
        speechRecognitionUseCase.stopListening()
        _uiState.value = UiState.Idle
    }

    private fun handleVoiceCommand(command: String) {
        val lowerCommand = command.toLowerCase()
        when {
            lowerCommand.contains("camera") || lowerCommand.contains("open") -> {
                _uiState.value = UiState.CameraReady
                viewModelScope.launch {
                    textToSpeechUseCase.speak("Camera is ready. Ask me what you want to know.")
                }
            }
            lowerCommand.contains("what do you see") || lowerCommand.contains("describe") -> {
                _uiState.value = UiState.Analyzing
                viewModelScope.launch {
                    textToSpeechUseCase.speak("Processing your request...")
                }
            }
            lowerCommand.contains("read") -> {
                _uiState.value = UiState.Reading
                viewModelScope.launch {
                    textToSpeechUseCase.speak("Reading text...")
                }
            }
            lowerCommand.contains("stop") || lowerCommand.contains("home") -> {
                _uiState.value = UiState.Idle
                viewModelScope.launch {
                    textToSpeechUseCase.speak("Camera stopped.")
                }
            }
            else -> {
                viewModelScope.launch {
                    textToSpeechUseCase.speak("I didn't understand. Please try again.")
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        textToSpeechUseCase.shutdown()
        speechRecognitionUseCase.destroy()
    }
}

sealed class UiState {
    object Idle : UiState()
    object Listening : UiState()
    object CameraReady : UiState()
    object Analyzing : UiState()
    object Reading : UiState()
    object ContinuousVision : UiState()
}
