package com.metalichesky.voicenotes.voice_notes.util.recognize

class RecognizeException(
        val error: RecognizeError
) : Exception(error.message) {

}

data class RecognizeError(
        val message: String
)