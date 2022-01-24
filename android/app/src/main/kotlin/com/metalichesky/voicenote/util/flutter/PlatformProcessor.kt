package com.metalichesky.voicenote.util.flutter

import android.content.Context
import android.util.Log
import com.github.olga_yakovleva.rhvoice.RHSynthesisRequest
import com.github.olga_yakovleva.rhvoice.RHVoice
import com.github.olga_yakovleva.rhvoice.data.Data
import com.github.olga_yakovleva.rhvoice.player.AudioTrackPlayer
import com.github.olga_yakovleva.rhvoice.voice.AndroidVoiceInfo
import com.github.olga_yakovleva.rhvoice.voice.VoicePack
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
    lateinit var synthesizeManager: RHVoice
    lateinit var synthesizePlayer: AudioTrackPlayer
    lateinit var synthesizeVoice: VoicePack

    var previousRecognized: RecognizeResult? = null

    fun setup(flutterEngine: FlutterEngine) {
        Log.d(LOG_TAG, "setup: thread=${Thread.currentThread().name}")
        recognizeManager = RecognizeManager(context)
        recognizeManager.setRecognizeListener(object : RecognizeListener {
            override fun onStateChanged(oldState: RecognizeState, newState: RecognizeState) {
                Log.d(LOG_TAG, "onStateChanged: oldState=${oldState} newState=${newState}")
                channelRecognize.onRecognizeStateChanged(oldState, newState)
            }

            override fun onRecognized(result: RecognizeResult) {
                if (result != previousRecognized || !result.isEmpty() && previousRecognized?.isEmpty() == true) {
                    channelRecognize.onRecognizeResult(result)
                    previousRecognized = result
                    if (result.type == RecognizeResult.Type.TEXT && !result.isEmpty() && !synthesizeManager.isPlaying) {
                        coroutineScope.launch(Dispatchers.IO) {
                            var request = RHSynthesisRequest(result.text)
                            val voices = Data.getVoices(context)
                            voices.find {
                                it.language.name == synthesizeVoice.language.name
                            }?.let{voiceInfo ->
                                val androidVoiceInfo = AndroidVoiceInfo(voiceInfo)
                                request.setLanguage(androidVoiceInfo.language, androidVoiceInfo.country, "");
                                request.speechRate = 100;
                                request.pitch = 100;
                                synthesizeManager.synthesizeText(request, null, synthesizePlayer)
                            }
                        }
                    }
                    Log.d(LOG_TAG, "onRecognized: result=${result}")
                }
            }

            override fun onError(error: RecognizeError) {
                Log.d(LOG_TAG, "onError: error=${error.message}")
            }
        })
        synthesizePlayer = AudioTrackPlayer()
        val language = Data.getLanguage("rus-RUS")
        synthesizeVoice = language.voices.find { it.name == "Aleksandr" || it.name == "Yuriy" }
            ?: language.defaultVoice

        synthesizeManager = RHVoice(context)
        synthesizeManager.setup()
        synthesizeVoice.setEnabled(context, true)

        channelSystem.configure(flutterEngine)
        channelRecognize.configure(flutterEngine)
    }

    fun release() {
        synthesizePlayer.release()
        synthesizeManager.release()
        recognizeManager.release()
    }
}