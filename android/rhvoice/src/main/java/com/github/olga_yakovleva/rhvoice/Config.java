package com.github.olga_yakovleva.rhvoice;

import android.content.Context;
import android.util.Log;

import java.io.File;

public final class Config {
    private static final String TAG = "RHVoiceConfig";
    private static final String CONFIG_FILE_NAME = "RHVoice.conf";

    public static File getDir(Context context) {
        if (BuildConfig.DEBUG)
            Log.d(TAG, "Requesting path to the private external storage directory");
        File dir = context.getExternalFilesDir(null);
        if (dir == null) {
            if (BuildConfig.DEBUG)
                Log.d(TAG, "The private external storage directory does not exist");
            return context.getDir("config", 0);
        }
        if (BuildConfig.DEBUG)
            Log.d(TAG, "The path to the private external storage directory is " + dir.getAbsolutePath());
        return dir;
    }

    public static File getDictsRootDir(Context ctx) {
        return new File(getDir(ctx), "dicts");
    }

    public static File getLangDictsDir(Context ctx, String langName) {
        return new File(getDictsRootDir(ctx), langName);
    }

    public static File getConfigFile(Context ctx) {
        return new File(getDir(ctx), CONFIG_FILE_NAME);
    }
}
