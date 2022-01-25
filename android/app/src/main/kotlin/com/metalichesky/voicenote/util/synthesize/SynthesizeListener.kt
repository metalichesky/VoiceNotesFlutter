package com.metalichesky.voicenote.util.synthesize

interface SynthesizeListener {
    fun onStateChanged(oldState: SynthesizeState, newState: SynthesizeState)
}