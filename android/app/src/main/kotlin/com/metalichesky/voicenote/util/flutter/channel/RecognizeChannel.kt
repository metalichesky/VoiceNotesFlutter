package com.metalichesky.voicenote.util.flutter.channel

import com.metalichesky.voicenote.util.flutter.PlatformUtils
import com.metalichesky.voicenote.util.recognize.RecognizeParams
import com.metalichesky.voicenote.util.recognize.RecognizeResult
import com.metalichesky.voicenote.util.recognize.RecognizeState
import io.flutter.Log
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel

const val METHOD_CONFIGURE_RECOGNIZE = "configureRecognize"
const val METHOD_START_RECOGNIZE = "startRecognize"
const val METHOD_PAUSE_RECOGNIZE = "pauseRecognize"
const val METHOD_STOP_RECOGNIZE = "stopRecognize"
const val METHOD_GET_RECOGNIZE_STATE = "getRecognizeState"

class RecognizeChannel(
        val callback: Callback
) : PlatformChannel() {
    companion object {
        const val LOG_TAG = "RecognizeChannel"
    }

    override val name: String = PlatformUtils.CHANNEL_RECOGNIZE

    override fun configure(flutterEngine: FlutterEngine) {
        super.configure(flutterEngine)
        channel?.setMethodCallHandler { call, result ->
            processMethodCall(call, result)
        }
    }

    override fun processMethodCall(call: MethodCall, result: MethodChannel.Result) {
        Log.d(LOG_TAG, "processMethodCall: method=${call.method}")
        when (call.method) {
            METHOD_CONFIGURE_RECOGNIZE -> {
                callback.onConfigureRecognize()
            }
            METHOD_START_RECOGNIZE -> {
                callback.onStartRecognize()
            }
            METHOD_PAUSE_RECOGNIZE -> {
                callback.onPauseRecognize()
            }
            METHOD_STOP_RECOGNIZE -> {
                callback.onStopRecognize()
            }
            METHOD_GET_RECOGNIZE_STATE -> {
                val state = callback.onGetRecognizeState()
                result.success(state.stateId)
            }
            else -> {
                result.notImplemented()
            }
        }
    }

    fun onRecognizeStateChanged(oldState: RecognizeState, newState: RecognizeState) {
        channel?.invokeMethod(
                "onRecognizeStateChanged",
                hashMapOf(
                        "oldState" to oldState.stateId,
                        "newState" to newState.stateId
                )
        )
    }

    fun onRecognizeResult(recognizeResult: RecognizeResult) {
        channel?.invokeMethod(
            "onRecognizeResult",
            hashMapOf(
                "recognizeResult" to recognizeResult.toString(),
            )
        )
    }

    interface Callback {
        fun onConfigureRecognize()
        fun onStartRecognize()
        fun onPauseRecognize()
        fun onStopRecognize()
        fun onGetRecognizeState(): RecognizeState
    }
}