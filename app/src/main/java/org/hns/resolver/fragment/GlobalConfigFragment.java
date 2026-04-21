package org.hns.resolver.fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.preference.*;

import org.hns.resolver.Daedalus;
import org.hns.resolver.R;
import org.hns.resolver.activity.AppFilterActivity;
import org.hns.resolver.activity.MainActivity;
import org.hns.resolver.server.DnsServerHelper;

import java.util.ArrayList;

public class GlobalConfigFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        Daedalus.getPrefs().edit()
                .putString("primary_server", DnsServerHelper.getPrimary())
                .putString("secondary_server", DnsServerHelper.getSecondary())
                .apply();

        addPreferencesFromResource(R.xml.perf_settings);

        // ✅ Server preferences
        for (String k : new ArrayList<String>() {{
            add("primary_server");
            add("secondary_server");
        }}) {
            ListPreference listPref = findPreference(k);
            if (listPref != null) {
                listPref.setEntries(DnsServerHelper.getNames(Daedalus.getInstance()));
                listPref.setEntryValues(DnsServerHelper.getIds());
                listPref.setSummary(DnsServerHelper.getDescription(listPref.getValue(), Daedalus.getInstance()));
                listPref.setOnPreferenceChangeListener((preference, newValue) -> {
                    preference.setSummary(DnsServerHelper.getDescription((String) newValue, Daedalus.getInstance()));
                    return true;
                });
            }
        }

        // ✅ DNS test servers
        EditTextPreference testDNSServers = findPreference("dns_test_servers");
        if (testDNSServers != null) {
            testDNSServers.setSummary(testDNSServers.getText());
            testDNSServers.setOnPreferenceChangeListener((preference, newValue) -> {
                preference.setSummary((String) newValue);
                return true;
            });
        }

        // ✅ Log size
        EditTextPreference logSize = findPreference("settings_log_size");
        if (logSize != null) {
            logSize.setSummary(logSize.getText());
            logSize.setOnPreferenceChangeListener((preference, newValue) -> {
                preference.setSummary((String) newValue);
                return true;
            });
        }

        // ✅ Dark theme
        SwitchPreference darkTheme = findPreference("settings_dark_theme");
        if (darkTheme != null) {
            darkTheme.setOnPreferenceChangeListener((preference, o) -> {
                getActivity().startActivity(new Intent(Daedalus.getInstance(), MainActivity.class)
                        .putExtra(MainActivity.LAUNCH_FRAGMENT, MainActivity.FRAGMENT_SETTINGS)
                        .putExtra(MainActivity.LAUNCH_NEED_RECREATE, true));
                return true;
            });
        }

        // ✅ Advanced toggle
        SwitchPreference advanced = findPreference("settings_advanced_switch");
        if (advanced != null) {
            advanced.setOnPreferenceChangeListener((preference, newValue) -> {
                updateOptions((boolean) newValue, "settings_advanced");
                return true;
            });
        }

        // ✅ App filter toggle
        SwitchPreference appFilter = findPreference("settings_app_filter_switch");
        if (appFilter != null) {
            appFilter.setOnPreferenceChangeListener((p, w) -> {
                updateOptions((boolean) w, "settings_app_filter");
                return true;
            });
        }

        // ✅ App filter list click
        Preference appFilterList = findPreference("settings_app_filter_list");
        if (appFilterList != null) {
            appFilterList.setOnPreferenceClickListener(preference -> {
                startActivity(new Intent(getActivity(), AppFilterActivity.class));
                return false;
            });
        }

        // ❌ REMOVED (these caused crash)
        // settings_check_update
        // settings_issue_tracker
        // settings_manual
        // settings_privacy_policy

        // ✅ Init state
        if (advanced != null) updateOptions(advanced.isChecked(), "settings_advanced");
        if (appFilter != null) updateOptions(appFilter.isChecked(), "settings_app_filter");
    }

    private void updateOptions(boolean checked, String pref) {
        PreferenceCategory category = findPreference(pref);
        if (category == null) return;

        for (int i = 1; i < category.getPreferenceCount(); i++) {
            Preference preference = category.getPreference(i);
            if (checked) {
                preference.setEnabled(true);
            } else {
                preference.setEnabled(false);
                if (preference instanceof SwitchPreference) {
                    ((SwitchPreference) preference).setChecked(false);
                }
            }
        }
    }
}
