package com.metalichesky.voicenote.util.synthesize

import android.content.Context
import android.os.Handler
import android.os.Looper
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
import com.metalichesky.voicenote.util.job.Job
import com.metalichesky.voicenote.util.job.JobManager
import com.metalichesky.voicenote.util.job.JobWorker
import com.metalichesky.voicenote.util.job.JobWorkerThread

class SynthesizeManager(
    var context: Context
) {
    companion object {
        val LOG_TAG: String = SynthesizeManager.javaClass.simpleName;
        const val JOB_SETUP = "SETUP"
        const val JOB_SYNTHESIZE_TEXT = "SYNTHESIZE_TEXT"
        const val JOB_RELEASE = "RELEASE"
        const val DEFAULT_LANGUAGE_CODE = "rus"
    }

    private var params: SynthesizeParams? = null
    var state: SynthesizeState = SynthesizeState.IDLE
        private set
    private var listener: SynthesizeListener? = null

    private var synthesizer: RHVoice? = null
    private var synthesizePlayer: AudioTrackPlayer? = null
    private var synthesizeLanguages: List<LanguagePack> = emptyList()
    private var synthesizeVoices: List<VoicePack> = emptyList()
    private var languageDetector: LanguageDetector? = null
    private val jobWorker = JobWorker.getWorker("SynthesizeManagerWorker")
    private val jobManager: JobManager = JobManager(
        jobWorkerProvider = { jobWorker }
    )
    private val mainHandler: Handler = Handler(Looper.myLooper() ?: Looper.getMainLooper())

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

    @JobWorkerThread
    private fun setStateInternal(newState: SynthesizeState) {
        mainHandler.post {
            setState(newState)
        }
    }

    fun setSynthesizeListener(recognizeListener: SynthesizeListener?) {
        this.listener = recognizeListener
    }

    fun setup(params: SynthesizeParams) {
        if (state != SynthesizeState.IDLE) {
            return
        }
        jobManager.post(Job(JOB_SETUP){
            setupInternal(params)
        })
    }

    @JobWorkerThread
    private fun setupInternal(params: SynthesizeParams) {
        if (state != SynthesizeState.IDLE){
            releaseInternal()
        }
        this.params = params
        setStateInternal(SynthesizeState.PREPARING)
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
        voicesToInstall.forEach { voicePack ->
            voicePack.install(context, dataSyncPlug)
            val isEnabled = params.preferredVoices.isEmpty()
                    || params.preferredVoices.find {  voicePack.name.equals(it.voiceName, true) } != null
            voicePack.setEnabled(context, isEnabled)
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
            if (isoCodes.size > 1) {
                languageDetector = LanguageDetectorBuilder.fromIsoCodes639_3(*isoCodes).build()
            }
            setStateInternal(SynthesizeState.READY)
        } else {
            Log.e(LOG_TAG, "setup: has no voices for synthesis")
            setStateInternal(SynthesizeState.IDLE)
        }
    }

    private fun getLanguageCodes(text: String): List<String> {
        return languageDetector?.computeLanguageConfidenceValues(text)
            ?.map { it.key.isoCode639_3.toString().lowercase() }
            ?: synthesizeLanguages.map { it.code }
            ?: listOf(DEFAULT_LANGUAGE_CODE)
    }

    fun synthesizeText(text: String?) {
        text ?: return
        jobManager.post(Job(JOB_SYNTHESIZE_TEXT){
            synthesizeTextInternal(text)
        })
    }

    @JobWorkerThread
    private fun synthesizeTextInternal(text: String?) {
        text ?: return
        val synthesizePlayer = synthesizePlayer ?: return
        val synthesizer = synthesizer ?: return
        var request = RHSynthesisRequest(text)
        request.speechRate = 100
        request.pitch = 100
        val voicesInfo = Data.getVoicesInfo(context)
        Log.d(LOG_TAG, "synthesizeText: detected voices ${voicesInfo.joinToString{ it.name }}")
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
        setStateInternal(SynthesizeState.STARTED)
        synthesizer.synthesizeText(request, null, synthesizePlayer)
        setStateInternal(SynthesizeState.STOPPED)
    }

    fun isPlaying(): Boolean {
        return synthesizePlayer?.isPlaying == true
    }

    fun resume() {
        when (state) {
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
        jobManager.post(Job(JOB_RELEASE){
            releaseInternal()
        })
    }

    @JobWorkerThread
    private fun releaseInternal() {
        stop()
        synthesizer?.stop()
        synthesizer?.release()
        synthesizer = null
        synthesizePlayer?.stop()
        synthesizePlayer?.release()
        synthesizePlayer = null
        languageDetector?.destroy()
        languageDetector = null
        setStateInternal(SynthesizeState.IDLE)
    }

}