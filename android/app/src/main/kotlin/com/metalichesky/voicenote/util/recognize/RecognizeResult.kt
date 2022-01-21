package com.metalichesky.voicenote.util.recognize

import com.google.gson.Gson
import com.google.gson.annotations.Expose
import java.lang.Exception

class RecognizeResult {
    companion object {
//        private val voskApiDelimiterRegex = Regex("(( )*:( )*)")
//        private val voskApiBracketsRegex = Regex("(^(( )*\")|(\"( )*)\$)")
        private val gson = Gson()

        fun fromVoskApiResult(voskApiResult: String): RecognizeResult {
            return try {
                gson.fromJson(voskApiResult, RecognizeResult::class.java)
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
    val type: Type
    get() {
        return when {
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
        return gson.toJson(this);
    }

    fun isEmpty(): Boolean {
        return anyResult == null || anyResult?.isEmpty() == true;
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