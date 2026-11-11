package com.example.util

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class SpeechManager(private val context: Context) : TextToSpeech.OnInitListener {
    private var tts: TextToSpeech? = null
    private var isInitialized = false

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    init {
        try {
            tts = TextToSpeech(context.applicationContext, this)
        } catch (e: Exception) {
            Log.e("SpeechManager", "Failed to initialize TTS", e)
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isInitialized = true
            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    _isSpeaking.value = true
                }

                override fun onDone(utteranceId: String?) {
                    _isSpeaking.value = false
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    _isSpeaking.value = false
                }

                override fun onError(utteranceId: String?, errorCode: Int) {
                    _isSpeaking.value = false
                }
            })
        } else {
            Log.e("SpeechManager", "TTS initialization failed with status $status")
        }
    }

    fun speak(text: String, languageCode: String = "en", speechRate: Float = 1.0f, pitch: Float = 1.0f) {
        if (!isInitialized || tts == null) {
            Log.w("SpeechManager", "TTS not initialized yet")
            return
        }

        try {
            val locale = if (languageCode.equals("hi", ignoreCase = true)) {
                Locale("hi", "IN")
            } else {
                Locale.US
            }

            tts?.language = locale
            tts?.setSpeechRate(speechRate)
            tts?.setPitch(pitch)

            // Strip markdown asterisks, hashtags for clean speech
            val cleanText = text
                .replace(Regex("[*#_`>]"), "")
                .replace(Regex("\\[.*?\\]\\(.*?\\)"), "")
                .trim()

            val params = Bundle().apply {
                putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "mg_ai_utterance_${System.currentTimeMillis()}")
            }

            tts?.speak(cleanText, TextToSpeech.QUEUE_FLUSH, params, "mg_ai_utterance")
            _isSpeaking.value = true
        } catch (e: Exception) {
            Log.e("SpeechManager", "Error speaking text", e)
            _isSpeaking.value = false
        }
    }

    fun stop() {
        try {
            tts?.stop()
            _isSpeaking.value = false
        } catch (e: Exception) {
            Log.e("SpeechManager", "Error stopping TTS", e)
        }
    }

    fun shutdown() {
        try {
            tts?.stop()
            tts?.shutdown()
            tts = null
        } catch (e: Exception) {
            Log.e("SpeechManager", "Error shutting down TTS", e)
        }
    }
}
