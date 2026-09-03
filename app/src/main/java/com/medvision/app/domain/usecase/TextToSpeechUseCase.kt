package com.medvision.app.domain.usecase

import android.content.Context
import android.graphics.Bitmap
import android.speech.tts.TextToSpeech
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class TextToSpeechUseCase(context: Context) {
    private var tts: TextToSpeech? = null
    private var isInitialized = false

    init {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isInitialized = true
            }
        }
    }

    suspend fun speak(text: String) = suspendCancellableCoroutine<Unit> { continuation ->
        if (!isInitialized) {
            continuation.resume(Unit)
            return@suspendCancellableCoroutine
        }

        tts?.setOnUtteranceProgressListener(object : android.speech.tts.UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {}
            override fun onDone(utteranceId: String?) {
                continuation.resume(Unit)
            }
            override fun onError(utteranceId: String?) {
                continuation.resume(Unit)
            }
        })

        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "utterance_id")
    }

    fun shutdown() {
        tts?.shutdown()
    }
}
