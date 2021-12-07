package com.metalichesky.voicenotes.voice_notes.util.recognize

import android.content.Context
import android.util.Log
import kotlinx.coroutines.suspendCancellableCoroutine
import org.vosk.Model
import org.vosk.Recognizer
import org.vosk.android.RecognitionListener
import org.vosk.android.SpeechService
import org.vosk.android.StorageService
import java.io.IOException
import java.lang.Exception

const val PATH_MODEL_DIR = "model"

class RecognizeManager(
        val context: Context
) {
    companion object {
        val LOG_TAG = RecognizeManager::class.java.simpleName
        const val DEFAULT_MODEL_NAME = ""
        const val DEFAULT_SAMPLE_RATE = 16000
    }

    var state: RecognizeState = RecognizeState.IDLE
        private set
    var listener: RecognizeListener? = null
        private set
    var params: RecognizeParams? = null
        private set
    private var model: Model? = null
    private var speechService: SpeechService? = null
    private var recognitionListener = object: RecognitionListener {
        override fun onPartialResult(hypothesis: String?) {

        }

        override fun onResult(hypothesis: String?) {

        }

        override fun onFinalResult(hypothesis: String?) {

        }

        override fun onError(exception: Exception?) {
            listener?.onError(RecognizeError(exception?.message ?: "Unknown Error"))
        }

        override fun onTimeout() {

        }
    }

    private fun setState(newState: RecognizeState) {
        val oldState = this.state
        this.state = newState
        listener?.onStateChanged(oldState, newState)
    }

    fun setRecognizeListener(recognizeListener: RecognizeListener?) {
        this.listener = recognizeListener
    }

    suspend fun setup(params: RecognizeParams) {
        this.params = params
        if (state != RecognizeState.IDLE) {
            release()
        }
        val configured = try {
            suspendCancellableCoroutine<Boolean> { continuation ->
                StorageService.unpack(context, "model-${params.locale}", PATH_MODEL_DIR,
                        { model: Model ->
                            Log.d(LOG_TAG, "initModel: model initialized")
                            this.model = model
                            continuation.resumeWith(Result.success(true))
                        },
                        { exception: IOException ->
                            Log.e(LOG_TAG, "initModel: model error initialization", exception)
                            continuation.resumeWith(Result.failure(RecognizeException(
                                    RecognizeError(
                                            message = "Error Model Initialization"
                                    )
                            )))
                        })
            }
        } catch (ex: RecognizeException) {
            listener?.onError(ex.error)
            false
        }
        if (configured) {
            setState(RecognizeState.READY)
        } else {
            setState(RecognizeState.IDLE)
        }
    }

    fun start() {
        when(state) {
            RecognizeState.READY, RecognizeState.STOPPED -> {
                val params = this.params
                        ?: throw IllegalStateException("params must not be null here")
                try {
                    val rec = Recognizer(model, DEFAULT_SAMPLE_RATE.toFloat())
                    speechService = SpeechService(rec, DEFAULT_SAMPLE_RATE.toFloat())
                    speechService?.startListening(recognitionListener)
                    setState(RecognizeState.STARTED)
                } catch (ex: IOException) {
                    Log.e(LOG_TAG, "start:", ex)
                    listener?.onError(RecognizeError("Error Recognizer Start"))
                    setState(RecognizeState.READY)
                }
            }
            RecognizeState.PAUSED -> {
                speechService?.setPause(false)
            }
        }
    }

    fun pause() {
        when(state) {
            RecognizeState.STARTED -> {
                speechService?.setPause(true)
                setState(RecognizeState.PAUSED)
            }
        }
    }

    fun stop() {
        when(state) {
            RecognizeState.STARTED, RecognizeState.PAUSED -> {
                speechService?.stop()
                setState(RecognizeState.STOPPED)
            }
        }
    }

    fun release() {
        stop()
        speechService?.shutdown()
        speechService = null
        model = null
        setState(RecognizeState.IDLE)
    }

}