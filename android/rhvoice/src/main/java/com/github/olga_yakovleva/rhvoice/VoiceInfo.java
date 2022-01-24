package com.github.olga_yakovleva.rhvoice;

import com.github.olga_yakovleva.rhvoice.LanguageInfo;

public final class VoiceInfo {
    private String name = null;
    private LanguageInfo language = null;

    void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    void setLanguage(LanguageInfo lang) {
        language = lang;
    }

    public LanguageInfo getLanguage() {
        return language;
    }
}
