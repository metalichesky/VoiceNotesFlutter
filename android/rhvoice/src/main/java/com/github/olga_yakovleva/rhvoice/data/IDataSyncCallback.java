package com.github.olga_yakovleva.rhvoice.data;

import com.github.olga_yakovleva.rhvoice.language.LanguagePack;
import com.github.olga_yakovleva.rhvoice.voice.VoicePack;

public interface IDataSyncCallback {
    public boolean isConnected();

    public void onLanguageDownloadStart(LanguagePack language);

    public void onLanguageDownloadDone(LanguagePack language);

    public void onLanguageInstallation(LanguagePack language);

    public void onLanguageRemoval(LanguagePack language);

    public void onVoiceDownloadStart(VoicePack voice);

    public void onVoiceDownloadDone(VoicePack voice);

    public void onVoiceInstallation(VoicePack voice);

    public void onVoiceRemoval(VoicePack voice);

    public boolean isTaskStopped();
}

