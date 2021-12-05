package com.metalichesky.voicenotes.voice_notes.util.flutter

import android.content.Context
import com.metalichesky.voicenotes.voice_notes.util.BatteryUtils
import com.metalichesky.voicenotes.voice_notes.util.flutter.channel.SystemChannel
import io.flutter.embedding.engine.FlutterEngine

class PlatformProcessor(
    val context: Context
) {
    private val channelSystemCallback = object: SystemChannel.Callback {
        override fun onGetBatteryCharge(): Double {
            return BatteryUtils.getBatteryLevel(context)
        }
    }

    val channelSystem: SystemChannel = SystemChannel(channelSystemCallback)

    fun configure(flutterEngine: FlutterEngine) {
        channelSystem.configure(flutterEngine)
    }
}