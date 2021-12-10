package com.metalichesky.voicenote.util.job

import java.lang.Exception

class Job<T>(
    val name: String = "",
    val onComplete: OnComplete<T>? = null,
    val work: suspend () -> T
) {

    interface OnComplete<T> {
        fun onResult(result: T)
        fun onFailure(exception: Exception?)
    }
}