package com.github.olga_yakovleva.rhvoice.worker;

import android.content.Context;
import android.util.Log;

import androidx.work.WorkerParameters;

import com.github.olga_yakovleva.rhvoice.BuildConfig;
import com.github.olga_yakovleva.rhvoice.data.DataPack;

public class NetworkDataSyncWorker extends DataSyncWorker {
    @Override
    public boolean isConnected() {
        return true;
    }

    public NetworkDataSyncWorker(Context context, WorkerParameters params) {
        super(context, params);
    }

    @Override
    protected Result doWork(DataPack p) {
        boolean done = doSync(p);
        if (BuildConfig.DEBUG)
            Log.v(TAG, "Network download of " + p.getId() + " finished with result " + done);
        return done ? Result.success() : Result.retry();
    }
}