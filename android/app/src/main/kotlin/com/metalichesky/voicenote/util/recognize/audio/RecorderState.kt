package com.metalichesky.voicenote.util.recognize.audio

enum class RecorderState(val stateId: Int, val stateName: String) {
    STATE_NONE(0, "NONE"),
    STATE_PREPARING(1, "PREPARING"),
    STATE_PREPARED(2, "PREPARED"),
    STATE_STARTING(3, "STARTING"),
    STATE_STARTED(4, "STARTED"),
    STATE_PAUSING(5, "PAUSING"),
    STATE_PAUSED(6, "PAUSED"),
    STATE_STOPPING(7, "STOPPING"),
    STATE_STOPPED(8, "STOPPED")
}