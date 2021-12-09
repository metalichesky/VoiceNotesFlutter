package com.metalichesky.voicenote.util.flutter

import android.content.Context
import android.util.Log
import com.metalichesky.voicenote.util.BatteryUtils
import com.metalichesky.voicenote.util.flutter.channel.RecognizeChannel
import com.metalichesky.voicenote.util.flutter.channel.SystemChannel
import com.metalichesky.voicenote.util.recognize.*
import io.flutter.embedding.engine.FlutterEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PlatformProcessor(
        val context: Context,
        val coroutineScope: CoroutineScope
) {
    companion object {
        const val LOG_TAG = "PlatformProcessor"
    }


    private val channelSystemCallback = object : SystemChannel.Callback {
        override fun onGetBatteryCharge(): Double {
            return BatteryUtils.getBatteryLevel(context)
        }
    }
    private val channelRecognizeCallback = object : RecognizeChannel.Callback {
        override fun onConfigureRecognize() {
            Log.d(LOG_TAG, "onConfigureRecognize: thread=${Thread.currentThread().name}")
            val recognizeParams = RecognizeParams()
            recognizeManager.setup(recognizeParams)
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
    lateinit var recognizeManager: RecognizeManager

    var previousRecognized: String = ""

    fun setup(flutterEngine: FlutterEngine) {
        Log.d(LOG_TAG, "setup: thread=${Thread.currentThread().name}")
        recognizeManager = RecognizeManager(context)
        recognizeManager.setRecognizeListener(object : RecognizeListener {
            override fun onStateChanged(oldState: RecognizeState, newState: RecognizeState) {
                Log.d(LOG_TAG, "onStateChanged: oldState=${oldState} newState=${newState}")
                channelRecognize.onRecognizeStateChanged(oldState, newState)
            }

            override fun onRecognized(text: String) {
                if (text != previousRecognized) {
                    previousRecognized = text
                    Log.d(LOG_TAG, "onRecognized: text=${text}")
                }
            }

            override fun onError(error: RecognizeError) {
                Log.d(LOG_TAG, "onError: error=${error.message}")
            }
        })

        channelSystem.configure(flutterEngine)
        channelRecognize.configure(flutterEngine)
    }

    fun release() {
        recognizeManager.release()
    }
}