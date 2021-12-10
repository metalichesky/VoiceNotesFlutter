package com.metalichesky.voicenote.util.recognize

interface RecognizeListener {
    fun onStateChanged(oldState: RecognizeState, newState: RecognizeState)
    fun onRecognized(result: RecognizeResult)
    fun onError(error: RecognizeError)
}