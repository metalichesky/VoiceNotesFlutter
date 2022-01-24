package com.github.olga_yakovleva.rhvoice.worker;

import android.content.Context;
import android.content.Intent;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.work.ListenableWorker;
import androidx.work.WorkerParameters;

import com.github.olga_yakovleva.rhvoice.RHVoice;
import com.github.olga_yakovleva.rhvoice.data.DataPack;
import com.github.olga_yakovleva.rhvoice.data.IDataSyncCallback;
import com.github.olga_yakovleva.rhvoice.data.SyncFlags;
import com.github.olga_yakovleva.rhvoice.language.LanguagePack;
import com.github.olga_yakovleva.rhvoice.voice.VoicePack;

public class DataSyncWorker extends DataWorker implements IDataSyncCallback {
    public static final String ACTION_VOICE_DOWNLOADED = "com.github.olga_yakovleva.rhvoice.android.action.voice_downloaded";
    public static final String ACTION_VOICE_INSTALLED = "com.github.olga_yakovleva.rhvoice.android.action.voice_installed";
    public static final String ACTION_VOICE_REMOVED = "com.github.olga_yakovleva.rhvoice.android.action.voice_removed";

    public boolean isConnected() {
        return false;
    }

    public void onLanguageDownloadStart(LanguagePack language) {

    }

    public void onLanguageDownloadDone(LanguagePack language) {

    }

    public void onLanguageInstallation(LanguagePack language) {

    }

    public void onLanguageRemoval(LanguagePack language) {

    }

    public void onVoiceDownloadStart(VoicePack voice) {

    }

    public void onVoiceDownloadDone(VoicePack voice) {
        Intent event = new Intent(ACTION_VOICE_DOWNLOADED);
        event.putExtra("name", voice.getName());
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(event);
    }

    public void onVoiceInstallation(VoicePack voice) {
        Intent event = new Intent(ACTION_VOICE_INSTALLED);
        event.putExtra("name", voice.getName());
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(event);
    }

    public void onVoiceRemoval(VoicePack voice) {
        Intent event = new Intent(ACTION_VOICE_REMOVED);
        event.putExtra("name", voice.getName());
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(event);
    }

    public final boolean isTaskStopped() {
        return isStopped();
    }

    public DataSyncWorker(Context context, WorkerParameters params) {
        super(context, params);
    }

    protected final boolean doSync(DataPack p) {
        boolean done = p.sync(getApplicationContext(), this);
        if (done)
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(new Intent(RHVoice.ACTION_CHECK_DATA));
        return done;
    }

    @Override
    protected ListenableWorker.Result doWork(DataPack p) {
        doSync(p);
        return (p.getSyncFlag(getApplicationContext()) != SyncFlags.LOCAL) ? ListenableWorker.Result.success(getInputData()) : ListenableWorker.Result.retry();
    }
}
