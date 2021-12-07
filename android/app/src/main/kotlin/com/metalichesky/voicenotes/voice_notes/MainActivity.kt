package com.metalichesky.voicenotes.voice_notes

import androidx.lifecycle.lifecycleScope
import com.metalichesky.voicenotes.voice_notes.util.flutter.PlatformProcessor
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugins.GeneratedPluginRegistrant

class MainActivity : FlutterActivity() {
    private var platformProcessor: PlatformProcessor? = null

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        GeneratedPluginRegistrant.registerWith(flutterEngine)
        configurePlatformProcessor(flutterEngine)
    }

    private fun configurePlatformProcessor(flutterEngine: FlutterEngine) {
        val platformProcessor = PlatformProcessor(this, lifecycleScope)
        this.platformProcessor = platformProcessor
        platformProcessor.configure(flutterEngine)
    }
}
