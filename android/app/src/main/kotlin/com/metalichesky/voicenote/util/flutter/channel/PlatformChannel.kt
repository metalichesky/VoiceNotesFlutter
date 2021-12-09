package com.metalichesky.voicenote.util.flutter.channel

import com.metalichesky.voicenote.util.flutter.PlatformUtils
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel

abstract class PlatformChannel {
    abstract val name: String

    var flutterEngine: FlutterEngine? = null
        private set
    var channel: MethodChannel? = null
        private set

    open fun configure(flutterEngine: FlutterEngine) {
        this.flutterEngine = flutterEngine
        channel = MethodChannel(flutterEngine.dartExecutor.binaryMessenger, name)
    }

    abstract fun processMethodCall(call: MethodCall, result: MethodChannel.Result)
}