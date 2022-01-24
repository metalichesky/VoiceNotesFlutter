package com.github.olga_yakovleva.rhvoice.voice;

import android.speech.tts.Voice;
import android.text.TextUtils;

import com.github.olga_yakovleva.rhvoice.VoiceInfo;

import java.util.HashSet;
import java.util.Locale;

public final class AndroidVoiceInfo {
    private final VoiceInfo source;

    public AndroidVoiceInfo(VoiceInfo source) {
        this.source = source;
    }

    public VoiceInfo getSource() {
        return source;
    }

    public String getLanguage() {
        return source.getLanguage().getAlpha3Code();
    }

    public String getCountry() {
        String code = source.getLanguage().getAlpha3CountryCode();
        if (TextUtils.isEmpty(code))
            return "";
        else
            return code.toUpperCase();
    }

    public String getVariant() {
        return source.getName();
    }

    public int getSupportLevel(String language, String country, String variant) {
        int result = 0;
        if (TextUtils.isEmpty(language) || language.equalsIgnoreCase(getLanguage())) {
            ++result;
            if (TextUtils.isEmpty(country) || country.equalsIgnoreCase(getCountry())) {
                ++result;
                if (TextUtils.isEmpty(variant) || variant.equalsIgnoreCase(getVariant()))
                    ++result;
            }
        }
        return result;
    }

    public boolean matches(String voice) {
        final String[] parts = voice.split("-");
        if ((parts.length == 0) || (parts.length > 3))
            return false;
        final String language = parts[0];
        String country = "";
        String variant = "";
        if (parts.length > 1) {
            country = parts[1];
            if (parts.length == 3)
                variant = parts[2];
        }
        return (getSupportLevel(language, country, variant) == parts.length);
    }

    @Override
    public String toString() {
        return (getLanguage() + "-" + getCountry() + "-" + getVariant());
    }

    @Override
    public boolean equals(Object other) {
        if (this == other)
            return true;
        if (!(other instanceof AndroidVoiceInfo))
            return false;
        return (source.getName().equals(((AndroidVoiceInfo) other).source.getName()));
    }

    public String getName() {
        return source.getName();
    }

    public Locale getLocale() {
        String language = source.getLanguage().getAlpha2Code();
        String country = source.getLanguage().getAlpha2CountryCode();
        if (TextUtils.isEmpty(country))
            return new Locale(language);
        else
            return new Locale(language, country);
    }

    public Voice getAndroidVoice() {
        return new Voice(getName(), getLocale(), Voice.QUALITY_NORMAL, Voice.LATENCY_NORMAL, false, new HashSet<String>());
    }
}