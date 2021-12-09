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
        setupPlatformProcessor(flutterEngine)
    }

    private fun setupPlatformProcessor(flutterEngine: FlutterEngine) {
        if (platformProcessor == null) {
            platformProcessor = PlatformProcessor(applicationContext, lifecycleScope)
            platformProcessor?.setup(flutterEngine)
        }
    }

    override fun cleanUpFlutterEngine(flutterEngine: FlutterEngine) {
        releasePlatformProcessor()
        super.cleanUpFlutterEngine(flutterEngine)
    }

    private fun releasePlatformProcessor() {
        platformProcessor?.release()
        platformProcessor = null
    }
}
