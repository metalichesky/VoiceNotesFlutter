package com.metalichesky.voicenotes.voice_notes.util.recognize

enum class RecognizeState(val stateId: Int) {
    IDLE(1),
    READY(2),
    STARTED(3),
    PAUSED(4),
    STOPPED(5)
}