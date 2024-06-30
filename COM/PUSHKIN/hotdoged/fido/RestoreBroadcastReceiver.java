package com.pushkin.hotdoged.fido;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;
import com.pushkin.hotdoged.export.XMLPrefs;

public class RestoreBroadcastReceiver extends BroadcastReceiver {
    private static final String DIR_SUFFIX = "backup/cp/ftn";
    private static final String TAG = "ftn_RestoreBroadcastReceiver";

    @Override // android.content.BroadcastReceiver
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Starting restoring provider data");
        XMLPrefs.restorePrefs(context, Environment.getExternalStorageDirectory() + "/HotdogEd/backup/cp/ftn/shared_prefs");
        Log.d(TAG, "Provider data restored OK.");
    }
}
