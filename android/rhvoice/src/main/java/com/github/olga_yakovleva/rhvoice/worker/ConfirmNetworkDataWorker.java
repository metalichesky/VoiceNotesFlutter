package com.github.olga_yakovleva.rhvoice.worker;

import android.content.Context;
import android.util.Log;

import androidx.work.WorkerParameters;

import com.github.olga_yakovleva.rhvoice.BuildConfig;
import com.github.olga_yakovleva.rhvoice.data.DataPack;
import com.github.olga_yakovleva.rhvoice.data.SyncFlags;

public class ConfirmNetworkDataWorker extends DataWorker {
    public ConfirmNetworkDataWorker(Context context, WorkerParameters params) {
        super(context, params);
    }

    @Override
    protected Result doWork(DataPack p) {
        boolean confirmed = (p.getSyncFlag(getApplicationContext()) == SyncFlags.NETWORK);
        if (BuildConfig.DEBUG)
            Log.v(TAG, "Network requirement confirmation result: " + confirmed);
        return confirmed ? Result.success(getInputData()) : Result.failure();
    }
}