package com.example.util

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

private const val TAG = "VoiceRecognizerHelper"

class VoiceRecognizerHelper(private val context: Context) {
    private var speechRecognizer: SpeechRecognizer? = null
    private val mainHandler = Handler(Looper.getMainLooper())

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _partialText = MutableStateFlow("")
    val partialText: StateFlow<String> = _partialText.asStateFlow()

    private val _rmsLevel = MutableStateFlow(0f) // Normalized 0f to 1f
    val rmsLevel: StateFlow<Float> = _rmsLevel.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private var onFinalResultCallback: ((String) -> Unit)? = null

    val isRecognitionAvailable: Boolean
        get() = SpeechRecognizer.isRecognitionAvailable(context)

    fun startListening(
        language: String = "en",
        onFinalResult: (String) -> Unit
    ) {
        mainHandler.post {
            try {
                cancel()
                _errorMessage.value = null
                _partialText.value = ""
                _rmsLevel.value = 0f
                onFinalResultCallback = onFinalResult

                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                    setRecognitionListener(object : RecognitionListener {
                        override fun onReadyForSpeech(params: Bundle?) {
                            Log.d(TAG, "onReadyForSpeech")
                            _isListening.value = true
                        }

                        override fun onBeginningOfSpeech() {
                            Log.d(TAG, "onBeginningOfSpeech")
                            _isListening.value = true
                        }

                        override fun onRmsChanged(rmsdB: Float) {
                            // rmsdB typically ranges from -2dB to 10dB
                            val normalized = ((rmsdB + 2f) / 12f).coerceIn(0.05f, 1f)
                            _rmsLevel.value = normalized
                        }

                        override fun onBufferReceived(buffer: ByteArray?) {}

                        override fun onEndOfSpeech() {
                            Log.d(TAG, "onEndOfSpeech")
                            _isListening.value = false
                        }

                        override fun onError(error: Int) {
                            val msg = when (error) {
                                SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                                SpeechRecognizer.ERROR_CLIENT -> "Client recognition error"
                                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission required"
                                SpeechRecognizer.ERROR_NETWORK -> "Network connection error"
                                SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timed out"
                                SpeechRecognizer.ERROR_NO_MATCH -> "No speech detected. Please speak clearly."
                                SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recognition service busy. Retrying..."
                                SpeechRecognizer.ERROR_SERVER -> "Server recognition error"
                                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input received"
                                else -> "Speech recognition error ($error)"
                            }
                            Log.w(TAG, "Recognition error: $msg ($error)")
                            _isListening.value = false
                            _rmsLevel.value = 0f
                            // Only set error if no partial text was caught
                            if (_partialText.value.isBlank()) {
                                _errorMessage.value = msg
                            }
                        }

                        override fun onResults(results: Bundle?) {
                            _isListening.value = false
                            _rmsLevel.value = 0f
                            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                            val recognized = matches?.firstOrNull()?.trim().orEmpty()
                            Log.d(TAG, "onResults: $recognized")
                            if (recognized.isNotEmpty()) {
                                _partialText.value = recognized
                                onFinalResultCallback?.invoke(recognized)
                            } else if (_partialText.value.isNotEmpty()) {
                                onFinalResultCallback?.invoke(_partialText.value)
                            }
                        }

                        override fun onPartialResults(partialResults: Bundle?) {
                            val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                            val partial = matches?.firstOrNull().orEmpty()
                            if (partial.isNotEmpty()) {
                                _partialText.value = partial
                                Log.d(TAG, "onPartialResults: $partial")
                            }
                        }

                        override fun onEvent(eventType: Int, params: Bundle?) {}
                    })
                }

                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
                    putExtra(
                        RecognizerIntent.EXTRA_LANGUAGE,
                        if (language.equals("hi", ignoreCase = true)) "hi-IN" else "en-US"
                    )
                }

                speechRecognizer?.startListening(intent)
                _isListening.value = true
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start speech recognizer", e)
                _isListening.value = false
                _errorMessage.value = e.localizedMessage ?: "Failed to start microphone"
            }
        }
    }

    fun stopListening() {
        mainHandler.post {
            try {
                speechRecognizer?.stopListening()
                _isListening.value = false
                _rmsLevel.value = 0f
            } catch (e: Exception) {
                Log.e(TAG, "Error stopping recognizer", e)
            }
        }
    }

    fun cancel() {
        mainHandler.post {
            try {
                speechRecognizer?.cancel()
                speechRecognizer?.destroy()
                speechRecognizer = null
                _isListening.value = false
                _rmsLevel.value = 0f
            } catch (e: Exception) {
                Log.e(TAG, "Error cancelling recognizer", e)
            }
        }
    }

    fun destroy() {
        cancel()
    }
}
