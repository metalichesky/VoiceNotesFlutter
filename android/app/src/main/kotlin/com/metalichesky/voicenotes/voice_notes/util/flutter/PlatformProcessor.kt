package com.metalichesky.voicenotes.voice_notes.util.flutter

import android.content.Context
import com.metalichesky.voicenotes.voice_notes.util.BatteryUtils
import com.metalichesky.voicenotes.voice_notes.util.flutter.channel.RecognizeChannel
import com.metalichesky.voicenotes.voice_notes.util.flutter.channel.SystemChannel
import com.metalichesky.voicenotes.voice_notes.util.recognize.RecognizeManager
import com.metalichesky.voicenotes.voice_notes.util.recognize.RecognizeParams
import com.metalichesky.voicenotes.voice_notes.util.recognize.RecognizeState
import io.flutter.embedding.engine.FlutterEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class PlatformProcessor(
        val context: Context,
        val coroutineScope: CoroutineScope
) {
    private val channelSystemCallback = object : SystemChannel.Callback {
        override fun onGetBatteryCharge(): Double {
            return BatteryUtils.getBatteryLevel(context)
        }
    }
    private val channelRecognizeCallback = object : RecognizeChannel.Callback {
        override fun onConfigureRecognize() {
            coroutineScope.launch {
                recognizeManager.setup(RecognizeParams())
            }
        }

        override fun onStartRecognize() {
            recognizeManager.start()
        }

        override fun onPauseRecognize() {
            recognizeManager.pause()
        }

        override fun onStopRecognize() {
            recognizeManager.stop()
        }

        override fun onGetRecognizeState(): RecognizeState {
            return recognizeManager.state
        }
    }

    val channelSystem: SystemChannel = SystemChannel(channelSystemCallback)
    val channelRecognize: RecognizeChannel = RecognizeChannel(channelRecognizeCallback)

    private val recognizeManager: RecognizeManager = RecognizeManager(context)

    fun configure(flutterEngine: FlutterEngine) {
        channelSystem.configure(flutterEngine)
        channelRecognize.configure(flutterEngine)
    }
}