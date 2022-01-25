package com.metalichesky.voicenote.util.synthesize

import android.content.Context
import android.util.Log
import com.github.olga_yakovleva.rhvoice.RHSynthesisRequest
import com.github.olga_yakovleva.rhvoice.RHVoice
import com.github.olga_yakovleva.rhvoice.data.Data
import com.github.olga_yakovleva.rhvoice.data.IDataSyncCallback
import com.github.olga_yakovleva.rhvoice.language.LanguagePack
import com.github.olga_yakovleva.rhvoice.player.AudioTrackPlayer
import com.github.olga_yakovleva.rhvoice.voice.AndroidVoiceInfo
import com.github.olga_yakovleva.rhvoice.voice.VoicePack

class SynthesizeManager(
    var context: Context
) {
    companion object {
        val LOG_TAG: String = SynthesizeManager.javaClass.simpleName;
    }

    private var params: SynthesizeParams? = null
    private var state: SynthesizeState = SynthesizeState.IDLE
    private var listener: SynthesizeListener? = null

    private var synthesizer: RHVoice? = null
    private var synthesizePlayer: AudioTrackPlayer? = null
    private var synthesizeLanguages: List<LanguagePack> = emptyList()
    private var synthesizeVoices: List<VoicePack> = emptyList()

    private val dataSyncPlug = object : IDataSyncCallback {
        override fun isConnected(): Boolean = true
        override fun onLanguageDownloadStart(language: LanguagePack?) {}
        override fun onLanguageDownloadDone(language: LanguagePack?) {}
        override fun onLanguageInstallation(language: LanguagePack?) {}
        override fun onLanguageRemoval(language: LanguagePack?) {}
        override fun onVoiceDownloadStart(voice: VoicePack?) {}
        override fun onVoiceDownloadDone(voice: VoicePack?) {}
        override fun onVoiceInstallation(voice: VoicePack?) {}
        override fun onVoiceRemoval(voice: VoicePack?) {}
        override fun isTaskStopped(): Boolean = false
    }

    private fun setState(newState: SynthesizeState) {
        val oldState = this.state
        this.state = newState
        listener?.onStateChanged(oldState, newState)
    }

    fun setSynthesizeListener(recognizeListener: SynthesizeListener?) {
        this.listener = recognizeListener
    }

    fun setup(params: SynthesizeParams) {
        this.params = params
        if (state != SynthesizeState.IDLE) {
            release()
        }
        setState(SynthesizeState.PREPARING)
        val voicesToInstall = params.voices.mapNotNull {
            val voice =
                Data.findMatchingVoice(it.voiceName, it.languageCode, it.languageCountryCode)
            if (voice != null) {
                Log.d(
                    LOG_TAG,
                    "setup: use voice ${voice.name} with language ${voice.language.name}"
                )
            }
            voice
        }
        voicesToInstall.forEach {
            it.install(context, dataSyncPlug)
            it.setEnabled(context, true)
        }
        synthesizeVoices = voicesToInstall.filter {
            it.isInstalled(context)
        }
        if (synthesizeVoices.isNotEmpty()) {
            synthesizeLanguages = synthesizeVoices.map {
                it.language
            }
            synthesizePlayer = AudioTrackPlayer()
            synthesizer = RHVoice(context)
            synthesizer?.setup()
            setState(SynthesizeState.READY)
        } else {
            Log.e(LOG_TAG, "setup: has no voices for synthesis")
            setState(SynthesizeState.IDLE)
        }
    }

    fun synthesizeText(text: String) {
        val synthesizePlayer = synthesizePlayer ?: return
        val synthesizer = synthesizer ?: return
        var request = RHSynthesisRequest(text)
        request.speechRate = 100
        request.pitch = 100
        val voicesInfo = Data.getVoicesInfo(context)
        val voiceInfo = voicesInfo.find {
            it.language.alpha3Code.equals("rus", true)
                    || it.language.alpha3Code.equals("ru", true)
        }
        val androidVoiceInfo = AndroidVoiceInfo(voiceInfo)
        request.setLanguage(androidVoiceInfo.language, androidVoiceInfo.country, "")

        setState(SynthesizeState.STARTED)
        synthesizer.synthesizeText(request, null, synthesizePlayer)
        setState(SynthesizeState.STOPPED)
    }

    fun isPlaying(): Boolean {
        return synthesizePlayer?.isPlaying == true
    }

    fun start() {
        when (state) {
            SynthesizeState.READY, SynthesizeState.STOPPED -> {
//                val params = this.params
//                    ?: throw IllegalStateException("params must not be null here")
            }
            SynthesizeState.PAUSED -> {
                if (synthesizePlayer?.isPlaying == false && synthesizePlayer?.isStarted == true) {
                    synthesizePlayer?.play()
                    setState(SynthesizeState.STARTED)
                } else {
                    setState(SynthesizeState.READY)
                }
            }
        }
    }

    fun pause() {
        when (state) {
            SynthesizeState.STARTED -> {
                if (synthesizePlayer != null) {
                    synthesizePlayer?.pause()
                    setState(SynthesizeState.PAUSED)
                } else {
                    setState(SynthesizeState.IDLE)
                }
            }
        }
    }

    fun stop() {
        when (state) {
            SynthesizeState.STARTED, SynthesizeState.PAUSED -> {
                if (synthesizePlayer?.isStarted == true || synthesizePlayer?.isPlaying == true) {
                    synthesizePlayer?.stop()
                }
                setState(SynthesizeState.STOPPED)
            }
        }
    }

    fun release() {
        stop()
        synthesizer?.stop()
        synthesizer?.release()
        synthesizer = null
        synthesizePlayer?.stop()
        synthesizePlayer?.release()
        synthesizePlayer = null
        setState(SynthesizeState.IDLE)
    }

}
