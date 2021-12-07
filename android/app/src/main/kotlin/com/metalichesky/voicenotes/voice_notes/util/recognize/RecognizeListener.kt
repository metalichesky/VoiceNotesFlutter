package com.metalichesky.voicenotes.voice_notes.util.recognize

interface RecognizeListener {
    fun onStateChanged(oldState: RecognizeState, newState: RecognizeState)
    fun onRecognized(text: String)
    fun onError(error: RecognizeError)
}