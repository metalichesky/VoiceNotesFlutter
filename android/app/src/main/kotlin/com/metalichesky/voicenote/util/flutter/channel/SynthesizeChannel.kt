package com.metalichesky.voicenote.util.flutter.channel

import com.metalichesky.voicenote.util.flutter.PlatformUtils
import com.metalichesky.voicenote.util.synthesize.SynthesizeState
import io.flutter.Log
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel

const val METHOD_CONFIGURE_SYNTHESIZE = "configureSynthesize"
const val METHOD_START_SYNTHESIZE = "startSynthesize"
const val METHOD_RESUME_SYNTHESIZE = "resumeSynthesize"
const val METHOD_PAUSE_SYNTHESIZE = "pauseSynthesize"
const val METHOD_STOP_SYNTHESIZE = "stopSynthesize"
const val METHOD_GET_SYNTHESIZE_STATE = "getSynthesizeState"

class SynthesizeChannel(
    val callback: Callback
) : PlatformChannel() {
    companion object {
        const val LOG_TAG = "SynthesizeChannel"
    }

    override val name: String = PlatformUtils.CHANNEL_SYNTHESIZE

    override fun configure(flutterEngine: FlutterEngine) {
        super.configure(flutterEngine)
        channel?.setMethodCallHandler { call, result ->
            processMethodCall(call, result)
        }
    }

    override fun processMethodCall(call: MethodCall, result: MethodChannel.Result) {
        Log.d(LOG_TAG, "processMethodCall: method=${call.method} args=${call.arguments}")
        when (call.method) {
            METHOD_CONFIGURE_SYNTHESIZE -> {
                callback.onConfigureSynthesize()
            }
            METHOD_START_SYNTHESIZE -> {
                val args: Map<String, String> = try {
                    call.arguments as? Map<String, String> ?: mapOf()
                } catch (ex: Exception) {
                    mapOf()
                }
                val text = args["text"]
                callback.onStartSynthesize(text)
                result.success(null)
            }
            METHOD_RESUME_SYNTHESIZE -> {
                callback.onResumeSynthesize()
                result.success(null)
            }
            METHOD_PAUSE_SYNTHESIZE -> {
                callback.onPauseSynthesize()
                result.success(null)
            }
            METHOD_STOP_SYNTHESIZE -> {
                callback.onStopSynthesize()
                result.success(null)
            }
            METHOD_GET_SYNTHESIZE_STATE -> {
                val state = callback.onGetSynthesizeState()
                result.success(state.stateId)
            }
            else -> {
                result.notImplemented()
            }
        }
    }

    fun onSynthesizeStateChanged(oldState: SynthesizeState, newState: SynthesizeState) {
        channel?.invokeMethod(
            "onSynthesizeStateChanged",
            hashMapOf(
                "oldState" to oldState.stateId,
                "newState" to newState.stateId
            )
        )
    }

    interface Callback {
        fun onConfigureSynthesize()
        fun onStartSynthesize(text: String?)
        fun onPauseSynthesize()
        fun onResumeSynthesize()
        fun onStopSynthesize()
        fun onGetSynthesizeState(): SynthesizeState
    }
}