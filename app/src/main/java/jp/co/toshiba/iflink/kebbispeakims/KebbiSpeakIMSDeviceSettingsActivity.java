package jp.co.toshiba.iflink.kebbispeakims;

import android.content.Intent;

import androidx.annotation.NonNull;

import jp.co.toshiba.iflink.ui.BaseSettingsActivity;

public class KebbiSpeakIMSDeviceSettingsActivity extends BaseSettingsActivity {
    /**
     * Preferences名.
     */
    public static final String PREFERENCE_NAME
            = "jp.co.toshiba.iflink.kebbispeakims";

    @Override
    protected final int getPreferencesResId() {
        return R.xml.pref_kebbispeakimsdevice;
    }

    @NonNull
    @Override
    protected final String getPreferencesName() {
        return PREFERENCE_NAME;
    }

    @Override
    protected final Intent getIntentForService() {
        Intent intent = new Intent(
                getApplicationContext(),
                KebbiSpeakIMS.class);
        intent.setPackage(getClass().getPackage().getName());
        return intent;
    }
}