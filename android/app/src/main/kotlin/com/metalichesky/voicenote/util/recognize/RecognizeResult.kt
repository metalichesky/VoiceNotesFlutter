package com.metalichesky.voicenote.util.recognize

import com.google.gson.Gson
import java.lang.Exception

class RecognizeResult {
    companion object {
        private val voskApiDelimiterRegex = Regex("(( )*:( )*)")
        private val voskApiBracketsRegex = Regex("(^(( )*\")|(\"( )*)\$)")
        private val jsonParser = Gson()

        fun fromVoskApiResult(voskApiResult: String): RecognizeResult {
            return try {
                jsonParser.fromJson(voskApiResult, RecognizeResult::class.java)
            } catch (ex: Exception) {
                RecognizeResult()
            }
        }
    }

    enum class Type(val typeName: String) {
        PARTIAL("partial"),
        TEXT("text"),
        UNKNOWN("unknown")
    }

    var partial: String? = null
    var text: String? = null
    val type: Type by lazy {
        when {
            text != null -> {
                Type.TEXT
            }
            partial != null -> {
                Type.PARTIAL
            }
            else -> {
                Type.UNKNOWN
            }
        }
    }
    val anyResult: String?
    get() {
        return text ?: partial
    }

    fun toVoskApiResult(): String {
        return "\"${type.typeName}\" : \"${anyResult}\""
    }

    override fun toString(): String {
        return toVoskApiResult()
    }

    override fun equals(other: Any?): Boolean {
        return other is RecognizeResult &&
                other.type == type &&
                other.text == text &&
                other.partial == partial
    }

    override fun hashCode(): Int {
        var result = partial?.hashCode() ?: 0
        result = 31 * result + (text?.hashCode() ?: 0)
        return result
    }
}