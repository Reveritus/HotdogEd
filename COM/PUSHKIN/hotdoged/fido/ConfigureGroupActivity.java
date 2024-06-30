package com.pushkin.hotdoged.fido;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.widget.Toast;
import com.pushkin.hotdoged.export.Constants;
import com.pushkin.hotdoged.export.Utils;
import java.util.Map;

public class ConfigureGroupActivity extends PreferenceActivity {
    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() { // from class: com.pushkin.hotdoged.fido.ConfigureGroupActivity.1
        @Override // android.preference.Preference.OnPreferenceChangeListener
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();
            if (preference instanceof ListPreference) {
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);
                preference.setSummary(index >= 0 ? listPreference.getEntries()[index] : null);
                return true;
            }
            preference.setSummary(stringValue);
            return true;
        }
    };
    private Map<String, ?> backupPrefs;
    private boolean changesMade = false;
    private Uri groupUri;
    private SharedPreferences prefs;

    @Override // android.app.Activity
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        setupSimplePreferencesScreen();
    }

    private void setupSimplePreferencesScreen() {
        addPreferencesFromResource(R.xml.pref_group);
        bindPreferenceSummaryToValue(findPreference(Constants.INTENT_EXTRA_NAME));
        bindPreferenceSummaryToValue(findPreference(Constants.INTENT_EXTRA_SERVERQUOTING));
        bindPreferenceSummaryToValue(findPreference(Constants.INTENT_EXTRA_SERVERCODEPAGE));
        bindPreferenceSummaryToValue(findPreference(Constants.INTENT_EXTRA_KEEPMSGAMOUNTPERGROUP));
        bindPreferenceSummaryToValue(findPreference(Constants.INTENT_EXTRA_KEEPMSGDAYSPERGROUP));
        bindPreferenceSummaryToValue(findPreference(Constants.INTENT_EXTRA_SIGNATURE));
        bindPreferenceSummaryToValue(findPreference(Constants.INTENT_EXTRA_CUSTOMHEADERS));
    }

    private static void bindPreferenceSummaryToValue(Preference preference) {
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference, PreferenceManager.getDefaultSharedPreferences(preference.getContext()).getString(preference.getKey(), ""));
    }

    @Override // android.preference.PreferenceActivity, android.app.Activity
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.prefs = PreferenceManager.getDefaultSharedPreferences(this);
        this.groupUri = Uri.parse(getIntent().getExtras().getString("groupuri"));
        loadFromDB(this.groupUri, this.prefs);
        this.backupPrefs = this.prefs.getAll();
    }

    private boolean loadFromDB(Uri uri, SharedPreferences prefs) {
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        if (!cursor.moveToFirst()) {
            Toast.makeText(this, "Failed to load data from database.", 1).show();
            cursor.close();
            return false;
        }
        for (String curColumn : Constants.DB_FTN_GROUP_FIELDS) {
            int index = cursor.getColumnIndex(curColumn);
            if (index >= 0) {
                if (Utils.isBooleanExtra(curColumn)) {
                    int iValue = cursor.getInt(index);
                    prefs.edit().putBoolean(curColumn, iValue == 1).commit();
                } else {
                    prefs.edit().putString(curColumn, cursor.getString(index)).commit();
                }
            } else {
                Toast.makeText(this, "Database column not found: " + uri + ", " + curColumn, 1).show();
                cursor.close();
                return false;
            }
        }
        cursor.close();
        return true;
    }

    @Override // android.preference.PreferenceActivity, android.app.Activity
    public void onBackPressed() {
        new Intent();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (!prefs.getAll().equals(this.backupPrefs)) {
            if (checkPrefs(prefs)) {
                saveToDB(prefs, this.groupUri);
            } else {
                Toast.makeText(this, "Group settings NOT saved", 1).show();
            }
        }
        Utils.clearPrefs(prefs, Constants.DB_FTN_GROUP_FIELDS);
        if (this.changesMade) {
            Toast.makeText(this, "Group settings changed", 1).show();
        }
        super.onBackPressed();
    }

    private boolean checkPrefs(SharedPreferences prefs) {
        return true;
    }

    private boolean prefEmpty(SharedPreferences prefs, String pref) {
        String prefValue = prefs.getString(pref, "");
        return prefValue.length() == 0;
    }

    private void saveToDB(SharedPreferences prefs, Uri uri) {
        ContentValues cv = new ContentValues();
        for (String curColumn : Constants.DB_FTN_GROUP_FIELDS) {
            if (Utils.isBooleanExtra(curColumn)) {
                boolean bValue = prefs.getBoolean(curColumn, false);
                cv.put(curColumn, Integer.valueOf(bValue ? 1 : 0));
            } else if (!prefEmpty(prefs, curColumn) || this.backupPrefs.containsKey(curColumn)) {
                cv.put(curColumn, prefs.getString(curColumn, null));
                this.changesMade = true;
            }
        }
        getContentResolver().update(uri, cv, null, null);
        Utils.clearPrefs(prefs, Constants.DB_FTN_GROUP_FIELDS);
    }
}
