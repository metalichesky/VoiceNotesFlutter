package com.metalichesky.voicenotes.voice_notes.util.flutter.channel

import com.metalichesky.voicenotes.voice_notes.util.flutter.PlatformUtils
import io.flutter.Log
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel

const val METHOD_GET_BATTERY_CHARGE = "getBatteryCharge"

class SystemChannel(
    val callback: Callback
) : PlatformChannel() {
    companion object {
        const val LOG_TAG = "SystemChannel"
    }

    override val name: String = PlatformUtils.CHANNEL_SYSTEM

    override fun configure(flutterEngine: FlutterEngine) {
        super.configure(flutterEngine)
        channel?.setMethodCallHandler { call, result ->
            processMethodCall(call, result)
        }
    }

    override fun processMethodCall(call: MethodCall, result: MethodChannel.Result) {
        Log.d(LOG_TAG, "processMethodCall: method=${call.method}")
        when (call.method) {
            METHOD_GET_BATTERY_CHARGE -> {
                val batteryCharge = callback.onGetBatteryCharge()
                Log.d(LOG_TAG, "processMethodCall: batteryCharge=${batteryCharge}")
                if (batteryCharge >= 0.0) {
                    result.success(batteryCharge)
                } else {
                    result.error("UNAVAILABLE", "Battery level not available.", null)
                }
            }
            else -> {
                result.notImplemented()
            }
        }
    }

    interface Callback {
        fun onGetBatteryCharge(): Double
    }
}