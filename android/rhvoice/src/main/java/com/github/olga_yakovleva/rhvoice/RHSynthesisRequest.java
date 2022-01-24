package com.github.olga_yakovleva.rhvoice;

import android.os.Bundle;

public class RHSynthesisRequest {
    private final CharSequence mText;
    private final Bundle mParams;
    private String mVoiceName;
    private String mLanguage;
    private String mCountry;
    private String mVariant;
    private int mSpeechRate;
    private int mPitch;

    public RHSynthesisRequest(CharSequence text, Bundle params) {
        mText = text;
        mParams = new Bundle(params);
    }

    public RHSynthesisRequest(CharSequence text) {
        mText = text;
        mParams = new Bundle();
    }

    /**
     * Gets the text which should be synthesized.
     *
     * @deprecated As of API level 21, replaced by {@link #getCharSequenceText}.
     */
    @Deprecated
    public String getText() {
        return mText.toString();
    }

    /**
     * Gets the text which should be synthesized.
     */
    public CharSequence getCharSequenceText() {
        return mText;
    }

    /**
     * Gets the name of the voice to use.
     */
    public String getVoiceName() {
        return mVoiceName;
    }

    /**
     * Gets the ISO 3-letter language code for the language to use.
     */
    public String getLanguage() {
        return mLanguage;
    }

    /**
     * Gets the ISO 3-letter country code for the language to use.
     */
    public String getCountry() {
        return mCountry;
    }

    /**
     * Gets the language variant to use.
     */
    public String getVariant() {
        return mVariant;
    }

    /**
     * Gets the speech rate to use. The normal rate is 100.
     */
    public int getSpeechRate() {
        return mSpeechRate;
    }

    /**
     * Gets the pitch to use. The normal pitch is 100.
     */
    public int getPitch() {
        return mPitch;
    }

    /**
     * Gets the additional params, if any.
     */
    public Bundle getParams() {
        return mParams;
    }

    /**
     * Sets the locale for the request.
     */
    public void setLanguage(String language, String country, String variant) {
        mLanguage = language;
        mCountry = country;
        mVariant = variant;
    }

    /**
     * Sets the voice name for the request.
     */
    public void setVoiceName(String voiceName) {
        mVoiceName = voiceName;
    }

    /**
     * Sets the speech rate.
     */
    public void setSpeechRate(int speechRate) {
        mSpeechRate = speechRate;
    }

    /**
     * Sets the pitch.
     */
    public void setPitch(int pitch) {
        mPitch = pitch;
    }
}