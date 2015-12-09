package com.example.liaohuaida.sunshine;

import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.util.Log;

public class SettingFragment extends PreferenceFragment
    implements Preference.OnPreferenceChangeListener {

    private static final String LOG_TAG = SettingFragment.class.getSimpleName();

    public SettingFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.v(LOG_TAG, "in Setting fragment");
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_general);

        bindSummaryToValues(findPreference(getString(R.string.pref_city_key)));
    }

    private void bindSummaryToValues(Preference preference) {
        preference.setOnPreferenceChangeListener(this);

        onPreferenceChange(preference,
                getPreferenceManager().getSharedPreferences()
                        .getString(getString(R.string.pref_city_key), ""));

    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String value = newValue.toString();

        if (preference instanceof EditTextPreference) {
            preference.setSummary(value);
        }

        return true;
    }
}
