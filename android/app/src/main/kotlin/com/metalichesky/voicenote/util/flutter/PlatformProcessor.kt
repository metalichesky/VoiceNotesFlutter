package com.metalichesky.voicenote.util.flutter

import android.content.Context
import android.util.Log
import com.metalichesky.voicenote.util.BatteryUtils
import com.metalichesky.voicenote.util.flutter.channel.RecognizeChannel
import com.metalichesky.voicenote.util.flutter.channel.SystemChannel
import com.metalichesky.voicenote.util.recognize.*
import com.metalichesky.voicenote.util.synthesize.*
import io.flutter.embedding.engine.FlutterEngine
import kotlinx.coroutines.*

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
            val params = SynthesizeParams(
                listOf(
                    SynthesizeVoice("Alan"),
                    SynthesizeVoice("Alexandr"),
                    SynthesizeVoice("Anna"),
                    SynthesizeVoice("Yuriy"),
                )
            )
            synthesizeManager.setup(params)
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
    lateinit var synthesizeManager: SynthesizeManager

    var previousRecognized: RecognizeResult? = null

    fun setup(flutterEngine: FlutterEngine) {
        Log.d(LOG_TAG, "setup: thread=${Thread.currentThread().name}")
        recognizeManager = RecognizeManager(context)
        recognizeManager.setRecognizeListener(object : RecognizeListener {
            override fun onStateChanged(oldState: RecognizeState, newState: RecognizeState) {
                Log.d(LOG_TAG, "onStateChanged: oldState=${oldState} newState=${newState}")
                channelRecognize.onRecognizeStateChanged(oldState, newState)
            }

            var synthesizeJob: Job? = null
            override fun onRecognized(result: RecognizeResult) {
                if (result != previousRecognized || !result.isEmpty() && previousRecognized?.isEmpty() == true) {
                    channelRecognize.onRecognizeResult(result)
                    previousRecognized = result
                    if (synthesizeJob == null && result.type == RecognizeResult.Type.TEXT
                        && !result.isEmpty() && !synthesizeManager.isPlaying()
                    ) {
                        synthesizeJob = coroutineScope.launch(Dispatchers.IO) {
                            delay(1000L)
                            synthesizeManager.synthesizeText(result.text ?: "")
                            synthesizeJob = null
                        }
                    }
                    Log.d(LOG_TAG, "onRecognized: result=${result}")
                }
            }

            override fun onError(error: RecognizeError) {
                Log.d(LOG_TAG, "onError: error=${error.message}")
            }
        })
        synthesizeManager = SynthesizeManager(context)
        synthesizeManager.setSynthesizeListener(object : SynthesizeListener {
            override fun onStateChanged(oldState: SynthesizeState, newState: SynthesizeState) {

            }
        })

        channelSystem.configure(flutterEngine)
        channelRecognize.configure(flutterEngine)
    }

    fun release() {
        synthesizeManager.release()
        synthesizeManager.release()
        recognizeManager.release()
    }
}