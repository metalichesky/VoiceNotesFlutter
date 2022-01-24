package com.github.olga_yakovleva.rhvoice.logger;


import android.util.Log;

import com.github.olga_yakovleva.rhvoice.BuildConfig;
import com.github.olga_yakovleva.rhvoice.LogLevel;
import com.github.olga_yakovleva.rhvoice.Logger;

public final class CoreLogger implements Logger {
    private static final String BASE_TAG = "RHVoiceCore";
    public static final CoreLogger instance = new CoreLogger();

    private CoreLogger() {
    }

    @Override
    public void log(String subtag, LogLevel level, String message) {
        if (!BuildConfig.DEBUG)
            return;
        final String tag = BASE_TAG + "/" + subtag;
        switch (level) {
            case ERROR:
                Log.e(tag, message);
                break;
            case WARNING:
                Log.w(tag, message);
                break;
            case INFO:
                Log.i(tag, message);
                break;
            case DEBUG:
                Log.d(tag, message);
                break;
            default:
                Log.v(tag, message);
                break;
        }
    }
}
