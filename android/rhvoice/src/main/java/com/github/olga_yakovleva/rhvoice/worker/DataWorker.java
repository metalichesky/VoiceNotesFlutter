package com.github.olga_yakovleva.rhvoice.worker;


import android.content.Context;
import android.util.Log;

import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.github.olga_yakovleva.rhvoice.BuildConfig;
import com.github.olga_yakovleva.rhvoice.data.Data;
import com.github.olga_yakovleva.rhvoice.data.DataPack;
import com.github.olga_yakovleva.rhvoice.language.LanguagePack;

abstract class DataWorker extends Worker {
    protected static final String TAG = "RHVoice.DataWorker";

    protected abstract Result doWork(DataPack p);

    protected DataWorker(Context context, WorkerParameters params) {
        super(context, params);
    }

    private DataPack getTarget() {
        androidx.work.Data d = getInputData();
        if (d == null)
            return null;
        String langId = d.getString("language_id");
        if (langId == null)
            return null;
        LanguagePack lang = Data.getLanguageById(langId);
        if (lang == null)
            return null;
        String voiceId = d.getString("voice_id");
        if (voiceId == null)
            return lang;
        return lang.findVoiceById(voiceId);
    }

    @Override
    public final Result doWork() {
        if (BuildConfig.DEBUG)
            Log.v(TAG, "doWork: " + getClass().getName());
        DataPack p = getTarget();
        if (p == null) {
            if (BuildConfig.DEBUG)
                Log.e(TAG, "No target defined");
            return Result.failure();
        }
        if (BuildConfig.DEBUG)
            Log.v(TAG, "Target: " + p.getId());
        synchronized (p) {
            Result res = doWork(p);
            if (BuildConfig.DEBUG)
                Log.v(TAG, "Result from " + getClass().getName() + ": " + res);
            return res;
        }
    }
}
