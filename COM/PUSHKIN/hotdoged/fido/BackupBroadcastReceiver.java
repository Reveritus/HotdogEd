package com.pushkin.hotdoged.fido;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;
import com.pushkin.hotdoged.export.HotdogedException;
import com.pushkin.hotdoged.export.Utils;

public class BackupBroadcastReceiver extends BroadcastReceiver {
    public static final String DIR_SUFFIX = "backup/cp/ftn";
    private static final String TAG = "ftn_BackupBroadcastReceiver";

    @Override // android.content.BroadcastReceiver
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Starting backup provider data");
        String prefsDir = Environment.getDataDirectory() + "/data/" + context.getPackageName() + "/shared_prefs/";
        try {
            Utils.copyFileToExtMemory(prefsDir + "com.pushkin.hotdoged.ftn.servers_credentials.xml", "backup/cp/ftn/shared_prefs", false);
            Log.d(TAG, "Provider data backed up OK.");
        } catch (HotdogedException e) {
            Log.e(TAG, "Ошибка бэкапа данных провайдера: " + e.getMessage());
            Toast.makeText(context, "Ошибка бэкапа данных провайдера: " + e.getMessage(), 1).show();
            e.printStackTrace();
        }
    }
}
