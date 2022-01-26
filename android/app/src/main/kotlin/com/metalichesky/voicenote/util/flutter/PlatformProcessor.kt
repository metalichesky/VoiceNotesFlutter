package com.metalichesky.voicenote.util.flutter

import android.content.Context
import android.util.Log
import com.metalichesky.voicenote.util.BatteryUtils
import com.metalichesky.voicenote.util.flutter.channel.RecognizeChannel
import com.metalichesky.voicenote.util.flutter.channel.SynthesizeChannel
import com.metalichesky.voicenote.util.flutter.channel.SystemChannel
import com.metalichesky.voicenote.util.recognize.*
import com.metalichesky.voicenote.util.synthesize.*
import io.flutter.embedding.engine.FlutterEngine
import kotlinx.coroutines.CoroutineScope

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
            val params = RecognizeParams()
            recognizeManager.setup(params)
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
    private val channelSynthesizeCallback = object : SynthesizeChannel.Callback {
        override fun onConfigureSynthesize() {
            Log.d(LOG_TAG, "onConfigureSynthesize: thread=${Thread.currentThread().name}")
            val alan = SynthesizeVoice("Alan", "eng", "USA")
            val stl = SynthesizeVoice("SLT", "eng", "USA")
            val alexandr = SynthesizeVoice("Aleksandr", "rus", "RUS")
            val anna = SynthesizeVoice("Anna", "rus", "RUS")
            val arina = SynthesizeVoice("Arina", "rus", "RUS")
            val yuriy = SynthesizeVoice("Yuriy", "rus", "RUS")
            val params = SynthesizeParams(
                voices = listOf(alan, alexandr, anna, arina, stl, yuriy),
                preferredVoices = listOf(alexandr, alan)
            )
            synthesizeManager.setup(params)
        }

        override fun onStartSynthesize(text: String?) {
            synthesizeManager.synthesizeText(text)
        }

        override fun onResumeSynthesize() {
            synthesizeManager.resume()
        }

        override fun onPauseSynthesize() {
            synthesizeManager.pause()
        }

        override fun onStopSynthesize() {
            synthesizeManager.stop()
        }

        override fun onGetSynthesizeState(): SynthesizeState {
            return synthesizeManager.state
        }
    }

    val channelSystem: SystemChannel = SystemChannel(channelSystemCallback)
    val channelRecognize: RecognizeChannel = RecognizeChannel(channelRecognizeCallback)
    val channelSynthesize: SynthesizeChannel = SynthesizeChannel(channelSynthesizeCallback)
    lateinit var recognizeManager: RecognizeManager
    lateinit var synthesizeManager: SynthesizeManager


    fun setup(flutterEngine: FlutterEngine) {
        Log.d(LOG_TAG, "setup: thread=${Thread.currentThread().name}")
        recognizeManager = RecognizeManager(context)
        recognizeManager.setRecognizeListener(object : RecognizeListener {
            var previousRecognized: RecognizeResult? = null

            override fun onStateChanged(oldState: RecognizeState, newState: RecognizeState) {
                Log.d(LOG_TAG, "onStateChanged: oldState=${oldState} newState=${newState}")
                channelRecognize.onRecognizeStateChanged(oldState, newState)
            }

            override fun onRecognized(result: RecognizeResult) {
                if (result != previousRecognized || !result.isEmpty() && previousRecognized?.isEmpty() == true) {
                    channelRecognize.onRecognizeResult(result)
                    previousRecognized = result
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
                channelSynthesize.onSynthesizeStateChanged(oldState, newState)
            }
        })

        channelSystem.configure(flutterEngine)
        channelRecognize.configure(flutterEngine)
        channelSynthesize.configure(flutterEngine)
    }

    fun release() {
        synthesizeManager.release()
        synthesizeManager.release()
        recognizeManager.release()
    }
}