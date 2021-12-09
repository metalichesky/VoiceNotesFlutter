package com.metalichesky.voicenote.util.recognize

class RecognizeException(
        val error: RecognizeError
) : Exception(error.message) {

}

data class RecognizeError(
        val message: String
)