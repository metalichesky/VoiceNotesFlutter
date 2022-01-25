package com.metalichesky.voicenote.util.synthesize

data class SynthesizeParams(
    val voices: List<SynthesizeVoice>
)

data class SynthesizeVoice(
    val voiceName: String,
    val languageCode: String? = null,
    val languageCountryCode: String? = null
)