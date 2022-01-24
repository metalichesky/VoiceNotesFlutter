package com.github.olga_yakovleva.rhvoice;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.github.olga_yakovleva.rhvoice.data.Data;

public final class OnPackageReceiver extends BroadcastReceiver {
    private static final String TAG = "RHOnPackageReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        int uid = context.getApplicationInfo().uid;
        if (intent.getIntExtra(Intent.EXTRA_UID, uid) != uid)
            return;
        String packageName = intent.getData().getSchemeSpecificPart();
        if (BuildConfig.DEBUG)
            Log.i(TAG, "Package " + packageName + " has been installed/updated/removed");
        Data.scheduleSync(context, true);
    }
}
