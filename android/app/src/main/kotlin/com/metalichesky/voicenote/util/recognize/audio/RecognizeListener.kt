package com.metalichesky.voicenote.util.recognize.audio

interface RecognizeListener {
    fun onPartialResult(hypothesis: String?)
    fun onResult(hypothesis: String?)
    fun onFinalResult(hypothesis: String?)
    fun onError(exception: Exception?)
    fun onTimeout()
}