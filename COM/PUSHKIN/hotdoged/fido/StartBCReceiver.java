package com.pushkin.hotdoged.fido;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.pushkin.ftn.Main;
import com.pushkin.hotdoged.export.HotdogedException;
import com.pushkin.hotdoged.export.Utils;

public class StartBCReceiver extends BroadcastReceiver {
    private static final String ADD_SERVER_ACTIVITY_NAME = "com.pushkin.hotdoged.fido.AddServerActivity";
    private static final String CFG_GROUP_ACTIVITY_NAME = "com.pushkin.hotdoged.fido.ConfigureGroupActivity";
    private static final String CFG_SERVER_ACTIVITY_NAME = "com.pushkin.hotdoged.fido.ConfigureServerActivity";
    private static final String CFG_SUBSCRIBE_ACTIVITY_NAME = "com.pushkin.hotdoged.fido.SubscribeActivity";
    public static final String SYNC_INTENT_NAME = "com.pushkin.hotdoged.fido.ContentFetchService";
    private static final String TAG = "fidobcr";

    @Override // android.content.BroadcastReceiver
    public void onReceive(Context context, Intent intent) {
        Main.SystemInfo.getLogger().log("ХотДог ФИДО-провайдер", "Startup request received");
        try {
            Utils.registerContentProvider(context, intent, ContentFetchService.CATEGORY_NAME, "ФИДОнет - сеть друзей!", null, ADD_SERVER_ACTIVITY_NAME, CFG_SERVER_ACTIVITY_NAME, CFG_GROUP_ACTIVITY_NAME, CFG_SUBSCRIBE_ACTIVITY_NAME, SYNC_INTENT_NAME, 1, 0, 1, "2.14.5");
        } catch (HotdogedException e) {
            Main.SystemInfo.getLogger().log(TAG, e.getLocalizedMessage());
        }
    }
}
