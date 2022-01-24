package com.github.olga_yakovleva.rhvoice.voice;

import android.content.Context;
import android.content.Intent;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.github.olga_yakovleva.rhvoice.RHVoice;
import com.github.olga_yakovleva.rhvoice.data.DataPack;
import com.github.olga_yakovleva.rhvoice.data.IDataSyncCallback;
import com.github.olga_yakovleva.rhvoice.language.LanguagePack;

public final class VoicePack extends DataPack {
    private final LanguagePack lang;

    public VoicePack(String id, String name, LanguagePack lang, int format, int revision, byte[] checksum) {
        super(id, name, format, revision, checksum);
        this.lang = lang;
    }

    public VoicePack(String id, String name, LanguagePack lang, int format, int revision, byte[] checksum, String altLink, String tempLink) {
        super(id, name, format, revision, checksum, altLink, tempLink);
        this.lang = lang;
    }

    public VoicePack(String name, LanguagePack lang, int format, int revision, byte[] checksum) {
        this(null, name, lang, format, revision, checksum);
    }

    public VoicePack(String name, LanguagePack lang, int format, int revision, byte[] checksum, String altLink, String tempLink) {
        this(null, name, lang, format, revision, checksum, altLink, tempLink);
    }

    public String getType() {
        return "voice";
    }

    public String getDisplayName() {
        return getName();
    }

    protected String getBaseFileName() {
        return String.format("RHVoice-voice-%s-%s", lang.getName(), getName());
    }

    private String getEnabledKey() {
        return String.format("voice.%s.enabled", getId());
    }

    @Override
    public final boolean getEnabled(Context context) {
        return getPrefs(context).getBoolean(getEnabledKey(), getPackageInfo(context) != null);
    }

    public final void setEnabled(Context context, boolean value) {
        boolean langEnabled = lang.getEnabled(context);
        boolean oldValue = getEnabled(context);
        getPrefs(context).edit().putBoolean(getEnabledKey(), value).apply();
        if (value == oldValue) {
            return;
        }
        LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(RHVoice.ACTION_CHECK_DATA));
        if (lang.getEnabled(context) != langEnabled) {
            lang.scheduleSync(context, true);
        }
        scheduleSync(context, true);
    }

    @Override
    protected void notifyDownloadStart(IDataSyncCallback callback) {
        callback.onVoiceDownloadStart(this);
    }

    @Override
    protected void notifyDownloadDone(IDataSyncCallback callback) {
        callback.onVoiceDownloadDone(this);
    }

    @Override
    protected void notifyInstallation(IDataSyncCallback callback) {
        callback.onVoiceInstallation(this);
    }

    @Override
    protected void notifyRemoval(IDataSyncCallback callback) {
        callback.onVoiceRemoval(this);
    }

    public LanguagePack getLanguage() {
        return lang;
    }

    @Override
    public androidx.work.Data.Builder setWorkInput(androidx.work.Data.Builder b) {
        lang.setWorkInput(b);
        return super.setWorkInput(b);
    }
}
