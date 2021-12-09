package com.metalichesky.voicenote

import androidx.lifecycle.lifecycleScope
import com.metalichesky.voicenote.util.flutter.PlatformProcessor
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
        if (platformProcessor == null) {
            platformProcessor = PlatformProcessor(applicationContext, lifecycleScope)
            platformProcessor?.configure(flutterEngine)
        }
    }

    override fun cleanUpFlutterEngine(flutterEngine: FlutterEngine) {
        platformProcessor = null
        super.cleanUpFlutterEngine(flutterEngine)
    }
}
