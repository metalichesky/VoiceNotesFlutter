package com.metalichesky.voicenote.util.recognize

enum class RecognizeState(val stateId: Int) {
    IDLE(0),
    READY(1),
    STARTED(2),
    PAUSED(3),
    STOPPED(4)
}