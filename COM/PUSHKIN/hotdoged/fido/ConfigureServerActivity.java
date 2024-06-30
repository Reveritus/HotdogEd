package com.pushkin.hotdoged.fido;

import android.annotation.TargetApi;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;
import com.pushkin.ftn.Main;
import com.pushkin.hotdoged.export.Constants;
import com.pushkin.hotdoged.export.CryptUtil;
import com.pushkin.hotdoged.export.Utils;
import com.sun.mail.imap.IMAPStore;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import jnode.ftn.types.FtnAddress;

public class ConfigureServerActivity extends PreferenceActivity {
    private static final boolean ALWAYS_SIMPLE_PREFS = false;
    private static final String CREDENTIALS_STORAGE = "com.pushkin.hotdoged.ftn.servers_credentials";
    private static final String DEFAULT_FTN_IP_DOMAIN = "binkp.net";
    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() { // from class: com.pushkin.hotdoged.fido.ConfigureServerActivity.1
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
    private long lastBackPressed = 0;
    private SharedPreferences prefs;
    protected Uri uri;

    @Override // android.preference.PreferenceActivity
    protected boolean isValidFragment(String fragmentName) {
        return GeneralPreferenceFragment.class.getName().equals(fragmentName);
    }

    @Override // android.app.Activity
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        setupSimplePreferencesScreen();
    }

    private void setupSimplePreferencesScreen() {
        if (isSimplePreferences(this)) {
            addPreferencesFromResource(R.xml.pref_server);
            new PreferenceCategory(this);
            bindPreferenceSummaryToValue(findPreference(Constants.INTENT_EXTRA_SERVERIP));
            bindPreferenceSummaryToValue(findPreference(Constants.INTENT_EXTRA_SERVERNAME));
            bindPreferenceSummaryToValue(findPreference(Constants.INTENT_EXTRA_SERVERAREASURL));
            bindPreferenceSummaryToValue(findPreference(Constants.INTENT_EXTRA_SERVERDESCRIPTION));
            bindPreferenceSummaryToValue(findPreference(Constants.INTENT_EXTRA_SERVERQUOTING));
            bindPreferenceSummaryToValue(findPreference(Constants.INTENT_EXTRA_SERVERCODEPAGE));
            bindPreferenceSummaryToValue(findPreference(Constants.INTENT_EXTRA_NAME));
            bindPreferenceSummaryToValue(findPreference(Constants.INTENT_EXTRA_ADDRESS));
            bindPreferenceSummaryToValue(findPreference("domain"));
            bindPreferenceSummaryToValue(findPreference(Constants.INTENT_EXTRA_KEEPMSGAMOUNTPERGROUP));
            bindPreferenceSummaryToValue(findPreference(Constants.INTENT_EXTRA_KEEPMSGDAYSPERGROUP));
            bindPreferenceSummaryToValue(findPreference(Constants.INTENT_EXTRA_SIGNATURE));
            bindPreferenceSummaryToValue(findPreference(Constants.INTENT_EXTRA_CUSTOMHEADERS));
            bindPreferenceSummaryToValue(findPreference(Constants.INTENT_EXTRA_ORIGIN));
            bindPreferenceSummaryToValue(findPreference(Constants.INTENT_EXTRA_ADD_INFO_01));
        }
    }

    @Override // android.preference.PreferenceActivity
    public boolean onIsMultiPane() {
        return isXLargeTablet(this) && !isSimplePreferences(this);
    }

    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout & 15) >= 4;
    }

    private static boolean isSimplePreferences(Context context) {
        return Build.VERSION.SDK_INT < 11 || !isXLargeTablet(context);
    }

    @Override // android.preference.PreferenceActivity
    @TargetApi(11)
    public void onBuildHeaders(List<PreferenceActivity.Header> target) {
        if (!isSimplePreferences(this)) {
            loadHeadersFromResource(R.xml.pref_server_headers, target);
        }
    }

    /* INFO: Access modifiers changed from: private */
    public static void bindPreferenceSummaryToValue(Preference preference) {
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference, PreferenceManager.getDefaultSharedPreferences(preference.getContext()).getString(preference.getKey(), ""));
    }

    @TargetApi(11)
    public static class GeneralPreferenceFragment extends PreferenceFragment {
        @Override // android.preference.PreferenceFragment, android.app.Fragment
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_server);
            ConfigureServerActivity.bindPreferenceSummaryToValue(findPreference(Constants.INTENT_EXTRA_SERVERIP));
            ConfigureServerActivity.bindPreferenceSummaryToValue(findPreference(Constants.INTENT_EXTRA_SERVERNAME));
            ConfigureServerActivity.bindPreferenceSummaryToValue(findPreference(Constants.INTENT_EXTRA_SERVERAREASURL));
            ConfigureServerActivity.bindPreferenceSummaryToValue(findPreference(Constants.INTENT_EXTRA_SERVERDESCRIPTION));
            ConfigureServerActivity.bindPreferenceSummaryToValue(findPreference(Constants.INTENT_EXTRA_SERVERQUOTING));
            ConfigureServerActivity.bindPreferenceSummaryToValue(findPreference(Constants.INTENT_EXTRA_NAME));
            ConfigureServerActivity.bindPreferenceSummaryToValue(findPreference(Constants.INTENT_EXTRA_ADDRESS));
            ConfigureServerActivity.bindPreferenceSummaryToValue(findPreference("domain"));
            ConfigureServerActivity.bindPreferenceSummaryToValue(findPreference(Constants.INTENT_EXTRA_SERVERCODEPAGE));
            ConfigureServerActivity.bindPreferenceSummaryToValue(findPreference(Constants.INTENT_EXTRA_KEEPMSGAMOUNTPERGROUP));
            ConfigureServerActivity.bindPreferenceSummaryToValue(findPreference(Constants.INTENT_EXTRA_KEEPMSGDAYSPERGROUP));
            ConfigureServerActivity.bindPreferenceSummaryToValue(findPreference(Constants.INTENT_EXTRA_SIGNATURE));
            ConfigureServerActivity.bindPreferenceSummaryToValue(findPreference(Constants.INTENT_EXTRA_CUSTOMHEADERS));
            ConfigureServerActivity.bindPreferenceSummaryToValue(findPreference(Constants.INTENT_EXTRA_ORIGIN));
            ConfigureServerActivity.bindPreferenceSummaryToValue(findPreference(Constants.INTENT_EXTRA_ADD_INFO_01));
        }
    }

    @Override // android.preference.PreferenceActivity, android.app.Activity
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.prefs = PreferenceManager.getDefaultSharedPreferences(this);
        this.uri = Uri.parse(getIntent().getStringExtra("serveruri"));
        loadFromDB(this.uri, this.prefs);
        this.backupPrefs = this.prefs.getAll();
    }

    private boolean loadFromDB(Uri uri, SharedPreferences prefs) {
        if (uri.getLastPathSegment().equals("servers")) {
            return true;
        }
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        if (!cursor.moveToFirst()) {
            Toast.makeText(this, "Failed to load data from database.", 1).show();
            cursor.close();
            return false;
        }
        for (String curColumn : Constants.DB_FTN_FIELDS) {
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
        prefs.edit().putString(Constants.INTENT_EXTRA_ADDRESS, getLocalServerLogin(this, uri.getLastPathSegment())).commit();
        prefs.edit().putString(Constants.INTENT_EXTRA_SERVERPASSWORD, getLocalServerPassword(this, uri.getLastPathSegment())).commit();
        cursor.close();
        return true;
    }

    @Override // android.preference.PreferenceActivity, android.app.Activity
    public void onBackPressed() {
        Intent intent = new Intent();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (!prefs.getAll().equals(this.backupPrefs)) {
            if (checkPrefs(prefs)) {
                saveToDB(prefs, Uri.parse(getIntent().getStringExtra("serveruri")));
                setResult(-1, intent);
                Utils.clearPrefs(prefs, Constants.DB_FTN_FIELDS);
                prefs.edit().remove(Constants.INTENT_EXTRA_SERVERPASSWORD).commit();
                super.onBackPressed();
                return;
            }
            return;
        }
        setResult(-1, intent);
        Utils.clearPrefs(prefs, Constants.DB_FTN_FIELDS);
        prefs.edit().remove(Constants.INTENT_EXTRA_SERVERPASSWORD).commit();
        super.onBackPressed();
    }

    private boolean checkPrefs(SharedPreferences prefs) {
        boolean allFilled = (prefEmpty(prefs, Constants.INTENT_EXTRA_SERVERNAME) || prefEmpty(prefs, Constants.INTENT_EXTRA_NAME) || prefEmpty(prefs, Constants.INTENT_EXTRA_ADDRESS) || prefEmpty(prefs, Constants.INTENT_EXTRA_SERVERPASSWORD)) ? false : true;
        if (allFilled) {
            String bossAddress = prefs.getString(Constants.INTENT_EXTRA_SERVERNAME, null);
            try {
                FtnAddress boss = new FtnAddress(bossAddress);
                if (addressExists(bossAddress)) {
                    Toast.makeText(this, "Sorry, cannot have more than one point at one boss", 1).show();
                    return false;
                }
                String pointAddress = prefs.getString(Constants.INTENT_EXTRA_ADDRESS, null);
                try {
                    FtnAddress point = new FtnAddress(pointAddress);
                    Main.SystemInfo.getLogger().log("ConfigureServerActivity", "Point address: " + point);
                    if (point.getZone() == 0 || point.getNet() == 0 || point.getNode() == 0) {
                        throw new NumberFormatException();
                    }
                } catch (Exception e) {
                    if (!TextUtils.isEmpty(pointAddress)) {
                        if (pointAddress.matches("^\\.\\d+$")) {
                            prefs.edit().putString(Constants.INTENT_EXTRA_ADDRESS, ((int) boss.getZone()) + ":" + ((int) boss.getNet()) + "/" + ((int) boss.getNode()) + pointAddress).commit();
                        }
                    } else {
                        Toast.makeText(this, "Point address is not correctly set, should be Zone:Net/Node.Point and should be a point of the boss node", 1).show();
                        return false;
                    }
                }
                String bossIp = prefs.getString(Constants.INTENT_EXTRA_SERVERIP, "");
                if (bossIp == null || bossIp.trim().length() == 0) {
                    if (boss == null) {
                        Toast.makeText(this, "Boss IP address could not be set from boss FTN address", 1).show();
                        return false;
                    }
                    prefs.edit().putString(Constants.INTENT_EXTRA_SERVERIP, String.format("f%d.n%d.z%d.%s", Short.valueOf(boss.getNode()), Short.valueOf(boss.getNet()), Short.valueOf(boss.getZone()), DEFAULT_FTN_IP_DOMAIN)).commit();
                }
                String sysName = prefs.getString(Constants.INTENT_EXTRA_SERVERDESCRIPTION, "");
                if (sysName != null && !sysName.matches("^[a-zA-Z0-9, :\\-_+=\\(\\)\\!\\@\\\\\\#\\$\\%\\^\\&\\*.\\?\\]\\[\\{\\}]*$")) {
                    Toast.makeText(this, "FTN system name should not contain special and national characters", 1).show();
                    return false;
                }
                return allFilled;
            } catch (Exception e2) {
                Toast.makeText(this, "Boss address is not correctly set, should be Zone:Net/Node", 1).show();
                return false;
            }
        }
        long currentTime = Calendar.getInstance().getTimeInMillis();
        Main.SystemInfo.getLogger().log("ConfigureServerActivity", "currentTime - lastBackPressed = " + currentTime + " - " + this.lastBackPressed + " = " + (currentTime - this.lastBackPressed));
        if (currentTime - this.lastBackPressed < 3000) {
            Utils.clearPrefs(prefs, Constants.DB_FTN_FIELDS);
            prefs.edit().remove(Constants.INTENT_EXTRA_SERVERPASSWORD).commit();
            super.onBackPressed();
            return false;
        }
        this.lastBackPressed = currentTime;
        Toast.makeText(this, "Заполни все поля, отмеченные звездочкой (*), или нажми НАЗАД снова для отмены.", 1).show();
        return allFilled;
    }

    private boolean addressExists(String bossAddress) {
        Cursor cursor = null;
        try {
            Uri serversUri = Uri.parse(getIntent().getStringExtra("serveruri"));
            Log.d("addressExists", "Checking uri: " + serversUri);
            if (!serversUri.getLastPathSegment().equals("servers")) {
                if (0 != 0 && !cursor.isClosed()) {
                    cursor.close();
                }
                return false;
            }
            cursor = getContentResolver().query(serversUri, null, "server_name = ?", new String[]{bossAddress}, null);
            if (cursor.moveToFirst()) {
                if (cursor != null && !cursor.isClosed()) {
                    cursor.close();
                }
                return true;
            }
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
            return false;
        } catch (Exception e) {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
            return false;
        } catch (Throwable th) {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
            throw th;
        }
    }

    private boolean prefEmpty(SharedPreferences prefs, String pref) {
        String prefValue = prefs.getString(pref, "");
        return prefValue.length() == 0;
    }

    private void saveToDB(SharedPreferences prefs, Uri uri) {
        ContentValues cv = new ContentValues();
        for (String curColumn : Constants.DB_FTN_FIELDS) {
            if (Utils.isBooleanExtra(curColumn)) {
                boolean bValue = prefs.getBoolean(curColumn, false);
                cv.put(curColumn, Integer.valueOf(bValue ? 1 : 0));
            } else {
                cv.put(curColumn, prefs.getString(curColumn, null));
            }
        }
        if (uri.getLastPathSegment().equals("servers")) {
            Uri resultUri = getContentResolver().insert(uri, cv);
            insertOrUpdateServerCredentials(this, resultUri.getLastPathSegment(), prefs.getString(Constants.INTENT_EXTRA_ADDRESS, null), prefs.getString(Constants.INTENT_EXTRA_SERVERPASSWORD, null));
            createServerFolders(this, resultUri);
            Toast.makeText(this, "Server added.", 0).show();
        } else {
            int cnt = getContentResolver().update(uri, cv, null, null);
            insertOrUpdateServerCredentials(this, uri.getLastPathSegment(), prefs.getString(Constants.INTENT_EXTRA_ADDRESS, null), prefs.getString(Constants.INTENT_EXTRA_SERVERPASSWORD, null));
            Toast.makeText(this, cnt + " server(s) updated.", 0).show();
        }
        Utils.clearPrefs(prefs, Constants.DB_FTN_FIELDS);
        prefs.edit().remove(Constants.INTENT_EXTRA_SERVERPASSWORD).commit();
    }

    public static void createServerFolders(Context context, Uri serverUri) {
        Uri groupsUri = Uri.withAppendedPath(serverUri, "groups");
        ContentValues cv = new ContentValues();
        cv.put(IMAPStore.ID_NAME, "Outgoing");
        cv.put("grouptype_id", (Integer) 5);
        context.getContentResolver().insert(groupsUri, cv);
        cv.clear();
        cv.put(IMAPStore.ID_NAME, "Sent");
        cv.put("grouptype_id", (Integer) 6);
        context.getContentResolver().insert(groupsUri, cv);
        cv.clear();
        cv.put(IMAPStore.ID_NAME, "Deleted");
        cv.put("grouptype_id", (Integer) 7);
        context.getContentResolver().insert(groupsUri, cv);
        cv.clear();
        cv.put(IMAPStore.ID_NAME, "Netmail");
        cv.put("grouptype_id", (Integer) 1);
        context.getContentResolver().insert(groupsUri, cv);
        cv.clear();
        cv.put(IMAPStore.ID_NAME, "Drafts");
        cv.put("grouptype_id", (Integer) 3);
        context.getContentResolver().insert(groupsUri, cv);
        cv.clear();
        cv.put(IMAPStore.ID_NAME, "To me");
        cv.put("grouptype_id", (Integer) 10);
        String groupId = context.getContentResolver().insert(groupsUri, cv).getLastPathSegment();
        cv.clear();
        Uri filtersUri = Uri.withAppendedPath(groupsUri, groupId + "/filters");
        cv.put(IMAPStore.ID_NAME, "Carbon copy");
        cv.put("filter_type", (Integer) 0);
        cv.put("field", (Integer) 2);
        cv.put("filter_relation", (Integer) 0);
        cv.put("field_value", "%ME%");
        cv.put("field_value_type", (Integer) 0);
        context.getContentResolver().insert(filtersUri, cv);
        cv.clear();
        cv.put(IMAPStore.ID_NAME, "Starred");
        cv.put("grouptype_id", (Integer) 10);
        cv.put("include_special", (Integer) 1);
        String groupId2 = context.getContentResolver().insert(groupsUri, cv).getLastPathSegment();
        cv.clear();
        Uri filtersUri2 = Uri.withAppendedPath(groupsUri, groupId2 + "/filters");
        cv.put(IMAPStore.ID_NAME, "Starred");
        cv.put("filter_type", (Integer) 0);
        cv.put("field", (Integer) 7);
        cv.put("filter_relation", (Integer) 0);
        cv.put("field_value", "1");
        cv.put("field_value_type", (Integer) 2);
        context.getContentResolver().insert(filtersUri2, cv);
        cv.clear();
    }

    public static void insertOrUpdateServerCredentials(Context context, String _id, String login, String password) {
        SharedPreferences prefs = context.getSharedPreferences(CREDENTIALS_STORAGE, 0);
        SharedPreferences.Editor e = prefs.edit();
        String encryptedPassword = "";
        try {
            encryptedPassword = CryptUtil.encrypt(password);
        } catch (Exception exception) {
            Main.SystemInfo.getLogger().log("insertOrUpdateServerCredentials", "Failed to encrypt password: " + exception.getMessage());
            Toast.makeText(context, "Failed to encypt password: " + exception.getMessage(), 0).show();
        }
        e.putString(_id, login + "\n" + encryptedPassword);
        e.commit();
    }

    public static String getLocalServerLogin(Context context, String _id) {
        SharedPreferences prefs = context.getSharedPreferences(CREDENTIALS_STORAGE, 0);
        String[] rc = prefs.getString(_id, "").split("\n", 2);
        return rc.length == 2 ? rc[0] : "";
    }

    public static String getLocalServerPassword(Context context, String _id) {
        SharedPreferences prefs = context.getSharedPreferences(CREDENTIALS_STORAGE, 0);
        String[] rc = prefs.getString(_id, "").split("\n", 2);
        if (rc.length == 2) {
            String encryptedPassword = rc[1];
            try {
                String decryptedPassword = CryptUtil.decrypt(encryptedPassword);
                return decryptedPassword;
            } catch (Exception e) {
                Main.SystemInfo.getLogger().log("getLocalServerPassword", "Failed to decrypt password: " + e.getMessage());
                Toast.makeText(context, "Failed to decrypt password: " + e.getMessage(), 0).show();
                return "";
            }
        }
        return "";
    }
}
