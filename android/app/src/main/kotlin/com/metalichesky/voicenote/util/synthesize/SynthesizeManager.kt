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
import com.github.pemistahl.lingua.api.IsoCode639_3
import com.github.pemistahl.lingua.api.LanguageDetector
import com.github.pemistahl.lingua.api.LanguageDetectorBuilder

class SynthesizeManager(
    var context: Context
) {
    companion object {
        val LOG_TAG: String = SynthesizeManager.javaClass.simpleName;
        const val DEFAULT_LANGUAGE_CODE = "rus"
    }

    private var params: SynthesizeParams? = null
    private var state: SynthesizeState = SynthesizeState.IDLE
    private var listener: SynthesizeListener? = null

    private var synthesizer: RHVoice? = null
    private var synthesizePlayer: AudioTrackPlayer? = null
    private var synthesizeLanguages: List<LanguagePack> = emptyList()
    private var synthesizeVoices: List<VoicePack> = emptyList()
    private var languageDetector: LanguageDetector? = null

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
            }.distinctBy {
                it.tag
            }
            synthesizePlayer = AudioTrackPlayer()
            synthesizer = RHVoice(context)
            synthesizer?.setup()

            val isoCodes = synthesizeLanguages.mapNotNull { languagePack ->
                IsoCode639_3.values().find { it.name.equals(languagePack.code, true) }
            }.toTypedArray()
            languageDetector = LanguageDetectorBuilder.fromIsoCodes639_3(*isoCodes).build()
            setState(SynthesizeState.READY)
        } else {
            Log.e(LOG_TAG, "setup: has no voices for synthesis")
            setState(SynthesizeState.IDLE)
        }
    }

    private fun getLanguageCodes(text: String): List<String> {
        return languageDetector?.computeLanguageConfidenceValues(text)
            ?.map { it.key.isoCode639_3.toString().lowercase() }
            ?: listOf(DEFAULT_LANGUAGE_CODE)
    }

    fun synthesizeText(text: String) {
        val synthesizePlayer = synthesizePlayer ?: return
        val synthesizer = synthesizer ?: return
        var request = RHSynthesisRequest(text)
        request.speechRate = 100
        request.pitch = 100
        val voicesInfo = Data.getVoicesInfo(context)
        val languages = getLanguageCodes(text)
        Log.d(LOG_TAG, "synthesizeText: detected languages ${languages.joinToString()}")
        val voiceInfo = voicesInfo.find { voiceInfo ->
            languages.find { language ->
                language.equals(voiceInfo.language.alpha3Code, true) ||
                        language.equals(voiceInfo.language.alpha3CountryCode, true)
            } != null
        }
        Log.d(LOG_TAG, "synthesizeText: choosen language ${voiceInfo?.language?.alpha3Code}")
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
        languageDetector?.destroy()
        languageDetector = null
        setState(SynthesizeState.IDLE)
    }

}