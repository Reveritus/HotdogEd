package com.pushkin.hotdoged.export;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;
import ch.boye.httpclientandroidlib.HttpResponse;
import ch.boye.httpclientandroidlib.NameValuePair;
import ch.boye.httpclientandroidlib.client.ClientProtocolException;
import ch.boye.httpclientandroidlib.client.HttpClient;
import ch.boye.httpclientandroidlib.client.entity.UrlEncodedFormEntity;
import ch.boye.httpclientandroidlib.client.methods.HttpGet;
import ch.boye.httpclientandroidlib.client.methods.HttpPost;
import ch.boye.httpclientandroidlib.entity.ContentType;
import ch.boye.httpclientandroidlib.entity.mime.HttpMultipartMode;
import ch.boye.httpclientandroidlib.entity.mime.MultipartEntityBuilder;
import ch.boye.httpclientandroidlib.impl.client.DefaultHttpClient;
import com.sun.mail.imap.IMAPStore;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.channels.FileChannel;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public final class Utils {
    public static void registerContentProvider(Context context, Intent intent, String category, String description, String configureCategoryActivityName, String addServerActivityName, String configureServerActivityName, String configureGroupActivityName, String subscribeActivity, String syncIntentName, int canUpdatePerServer, int canUpdatePerGroup, int writable, String version) throws HotdogedException {
        Intent respIntent = new Intent();
        PendingIntent pi = (PendingIntent) intent.getParcelableExtra(Constants.ONSTART_PENDING_INTENT);
        respIntent.putExtra(Constants.INTENT_EXTRA_CATEGORY, category);
        respIntent.putExtra(Constants.INTENT_EXTRA_DESCRIPTION, description);
        respIntent.putExtra(Constants.INTENT_EXTRA_CFGACTIVITY, configureCategoryActivityName);
        respIntent.putExtra(Constants.INTENT_EXTRA_ADDSRVACTIVITY, addServerActivityName);
        respIntent.putExtra(Constants.INTENT_EXTRA_CFGSRVACTIVITY, configureServerActivityName);
        respIntent.putExtra(Constants.INTENT_EXTRA_CFGGRPACTIVITY, configureGroupActivityName);
        respIntent.putExtra(Constants.INTENT_EXTRA_SYNCINTENT, syncIntentName);
        respIntent.putExtra(Constants.INTENT_EXTRA_PS_UPDATE, canUpdatePerServer);
        respIntent.putExtra(Constants.INTENT_EXTRA_PG_UPDATE, canUpdatePerGroup);
        respIntent.putExtra(Constants.INTENT_EXTRA_SUBSCRIBEACTIVITY, subscribeActivity);
        respIntent.putExtra(Constants.INTENT_EXTRA_WRITABLE, writable);
        respIntent.putExtra("version", version);
        try {
            pi.send(context, 0, respIntent);
        } catch (PendingIntent.CanceledException e) {
            throw new HotdogedException("Failed to register as content provider: " + e.getLocalizedMessage());
        }
    }

    public static boolean isBooleanExtra(String extra) {
        return extra.equals(Constants.INTENT_EXTRA_SERVERAUTHENABLE) || extra.equals(Constants.INTENT_EXTRA_SERVERACTIVE) || extra.equals(Constants.INTENT_EXTRA_PURGEREAD) || extra.equals(Constants.INTENT_EXTRA_ADD_INT_01) || extra.equals(Constants.INTENT_EXTRA_ADD_INT_02);
    }

    public static long getCategoryIdByName(Context context, String category) {
        String CATEGORIES = "content://com.pushkin.hotdoged.provider/Categories/" + category;
        Uri CATEGORIES_URI = Uri.parse(CATEGORIES);
        Cursor cursor = context.getContentResolver().query(CATEGORIES_URI, null, "name = ?", new String[]{category}, null);
        if (cursor.moveToFirst()) {
            long id = cursor.getLong(cursor.getColumnIndex(Constants.INTENT_EXTRA_DBID));
            cursor.close();
            return id;
        }
        cursor.close();
        return -1L;
    }

    public static String getCategoryNameById(Context context, int id) throws HotdogedException {
        Cursor cursor = null;
        try {
            try {
                Uri CATEGORIES_URI = Uri.parse("content://com.pushkin.hotdoged.provider/Categories");
                cursor = context.getContentResolver().query(CATEGORIES_URI, new String[]{IMAPStore.ID_NAME}, "_id = " + id, null, null);
                if (cursor.moveToFirst()) {
                    String name = cursor.getString(0);
                    return name;
                }
                if (cursor != null && !cursor.isClosed()) {
                    cursor.close();
                }
                throw new HotdogedException("Category " + id + " not found");
            } catch (Exception e) {
                throw new HotdogedException("Error getting category info, id = " + id + ": " + e.getMessage());
            }
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
    }

    public static void runSyncGroup(Context context, Uri groupUri, String intentName, int purgePeriod) {
        Log.d("runSyncGroup", "URI: " + groupUri);
        GroupEntry.clearCache();
        ItemEntry.clearCache();
        Intent intent = new Intent(intentName);
        intent.putExtra(Constants.INTENT_EXTRA_SYNCTYPE, 1);
        intent.putExtra("uri", groupUri.toString());
        intent.putExtra(Constants.INTENT_EXTRA_PURGEPERIOD, purgePeriod);
        intent.addFlags(32);
        try {
            context.startService(createExplicitFromImplicitIntent(context, intent));
        } catch (Exception e) {
            Toast.makeText(context, "Неудачное начало синхронизации: " + e, 1).show();
            Log.e("Sync", "Неудачное начало синхронизации: " + e);
        }
    }

    public static void runSyncCategory(Context context, Uri categoryUri, String intentName, int purgePeriod) {
        Log.d("runSyncCategory", "URI: " + categoryUri);
        GroupEntry.clearCache();
        ItemEntry.clearCache();
        Intent intent = new Intent(intentName);
        intent.putExtra(Constants.INTENT_EXTRA_SYNCTYPE, 3);
        intent.putExtra("uri", categoryUri.toString());
        intent.putExtra(Constants.INTENT_EXTRA_PURGEPERIOD, purgePeriod);
        intent.addFlags(32);
        try {
            context.startService(createExplicitFromImplicitIntent(context, intent));
        } catch (Exception e) {
            Toast.makeText(context, "Не удалось начать синхронизациюy: " + e, 1).show();
            Log.e("Sync", "Неудачное начало синхронизации: " + e);
        }
    }

    public static void runSyncServer(Context context, Uri serverUri, String intentName, int purgePeriod) {
        Log.d("runSyncServer", "URI: " + serverUri);
        GroupEntry.clearCache();
        ItemEntry.clearCache();
        Intent intent = new Intent(intentName);
        intent.putExtra(Constants.INTENT_EXTRA_SYNCTYPE, 2);
        intent.putExtra("uri", serverUri.toString());
        intent.putExtra(Constants.INTENT_EXTRA_PURGEPERIOD, purgePeriod);
        intent.addFlags(32);
        try {
            context.startService(createExplicitFromImplicitIntent(context, intent));
        } catch (Exception e) {
            Toast.makeText(context, "Неудачное начало синхронизации: " + e, 1).show();
            Log.e("Sync", "Неудачное начало синхронизации: " + e);
        }
    }

    public static void runSyncSendUnsent(Context context, Uri serverUri, String intentName) {
        Log.d("runSyncSendUnsent", "URI: " + serverUri);
        GroupEntry.clearCache();
        ItemEntry.clearCache();
        Intent intent = new Intent(intentName);
        intent.putExtra(Constants.INTENT_EXTRA_SYNCTYPE, 5);
        intent.putExtra("uri", serverUri.toString());
        intent.addFlags(32);
        try {
            context.startService(createExplicitFromImplicitIntent(context, intent));
        } catch (Exception e) {
            Toast.makeText(context, "Неудачное начало синхронизации: " + e, 1).show();
            Log.e("Sync", "Неудачное начало синхронизации: " + e);
        }
    }

    public static void notifyContentUpdated(Context context, String category, String serverId, String groupId, int newMsgs) throws HotdogedException {
        Intent intent = new Intent(Constants.ONCONTENTUPDATED_BROADCAST);
        intent.addFlags(32);
        intent.putExtra(Constants.INTENT_EXTRA_SERVER, serverId);
        intent.putExtra(Constants.INTENT_EXTRA_GROUP, groupId);
        intent.putExtra(Constants.INTENT_EXTRA_NEWMSGS, newMsgs);
        context.sendBroadcast(intent);
        Log.d("notifyContentUpdated()", "Notification intent sent to all receivers");
    }

    public static int getNewMsgs(Context context, Uri groupUri) throws HotdogedException {
        GroupEntry.clearCache();
        ItemEntry.clearCache();
        GroupEntry groupEntry = new GroupEntry(context, groupUri);
        if (groupUri != null) {
            return groupEntry.getNew_msgs();
        }
        throw new HotdogedException("Error fetching group entry: " + groupUri);
    }

    public static void setNewMsgs(Context context, Uri groupUri, int newMsgs, boolean notify) throws HotdogedException {
        ContentValues cv = new ContentValues();
        GroupEntry.clearCache();
        ItemEntry.clearCache();
        cv.put(Constants.INTENT_EXTRA_NEWMSGS, Integer.valueOf(newMsgs));
        try {
            context.getContentResolver().update(groupUri, cv, null, null);
            Log.d("setNewMsgs", "Updated " + groupUri.toString() + ": " + newMsgs);
            if (notify) {
                String category = groupUri.getPathSegments().get(0);
                String serverId = groupUri.getPathSegments().get(4);
                String groupId = groupUri.getLastPathSegment();
                notifyContentUpdated(context, category, serverId, groupId, newMsgs);
            }
        } catch (Exception e) {
            throw new HotdogedException("Error updating db: " + e.getMessage());
        }
    }

    public static String getPreferredCodePage(ServerEntry serverEntry, GroupEntry groupEntry) throws HotdogedException {
        String codePage = groupEntry.getCodepage();
        if (codePage == null) {
            codePage = serverEntry.getServer_codepage();
        }
        if (codePage == null) {
            throw new HotdogedException("Failed to determine codepage for group " + groupEntry.getName());
        }
        return codePage;
    }

    public static String formatDateShort(String sDate) {
        if (sDate == null) {
            return "Date N/A";
        }
        long lDate = Long.valueOf(sDate, 10).longValue();
        if (lDate == 0) {
            return "Date N/A";
        }
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(1000 * lDate);
        DateFormat df = DateFormat.getDateTimeInstance(3, 3);
        return df.format(cal.getTime());
    }

    public static void setLastRead(Context context, Uri groupUri, long _id) throws HotdogedException {
        ContentValues cv = new ContentValues();
        cv.put("last_read", Long.valueOf(_id));
        try {
            context.getContentResolver().update(groupUri, cv, null, null);
            Log.d("setLastRead", "Last read pointer set to id " + _id);
        } catch (Exception e) {
            throw new HotdogedException("Error updating db: " + e.getMessage());
        }
    }

    public static void setMessageRead(Context context, Uri itemUri, boolean read) throws HotdogedException {
        ContentValues cv = new ContentValues();
        cv.put("read", Integer.valueOf(read ? 1 : 0));
        try {
            context.getContentResolver().update(itemUri, cv, null, null);
        } catch (Exception e) {
            throw new HotdogedException("Error updating db: " + e.getMessage());
        }
    }

    public static void restoreListPosition(ListView listView, Bundle bundle) {
        int index = bundle.getInt("index");
        int top = bundle.getInt("top");
        if (index != 0 || top != 0) {
            listView.setSelectionFromTop(index, top);
        }
    }

    public static void saveListPosition(ListView listView, Bundle bundle) {
        int index = listView.getFirstVisiblePosition();
        View v = listView.getChildAt(0);
        int top = v != null ? v.getTop() - v.getPaddingTop() : 0;
        if (index != 0 || top != 0) {
            bundle.putInt("index", index);
            bundle.putInt("top", top);
        }
    }

    public static String normalizeSubject(String subject) {
        if (subject == null) {
            return null;
        }
        return subject.replaceFirst("^ *[Rr][Ee][ :^]*(.*)", "$1").replaceFirst("^ *[Ff][Ww][Dd][ :^]*(.*)", "$1").trim();
    }

    public static void setLastDownloaded(Context context, Uri groupUri, int last_downloaded) throws HotdogedException {
        ContentValues cv = new ContentValues();
        try {
            cv.put("last_downloaded", Integer.valueOf(last_downloaded));
            context.getContentResolver().update(groupUri, cv, null, null);
        } catch (Exception e) {
            throw new HotdogedException("Error updating db: " + e.getMessage());
        }
    }

    public static String createMessageId(String categoryName) {
        if (categoryName == null) {
            return null;
        }
        if (categoryName.equalsIgnoreCase("NNTP")) {
            return "<1234@localhost>";
        }
        if (categoryName.equalsIgnoreCase("FTN")) {
        }
        return null;
    }

    public static int getSpecialGroupIdForServer(Context context, int groupType, Uri serverUri) throws HotdogedException {
        Cursor cursor = null;
        try {
            try {
                cursor = context.getContentResolver().query(Uri.withAppendedPath(serverUri, "groupsnoaux"), new String[]{Constants.INTENT_EXTRA_DBID}, "grouptype_id = ?", new String[]{String.valueOf(groupType)}, Constants.INTENT_EXTRA_DBID);
                if (cursor.moveToFirst()) {
                    int group_id = cursor.getInt(0);
                    return group_id;
                }
                throw new HotdogedException("Group with type " + groupType + " not found for server " + serverUri.toString());
            } catch (Exception e) {
                throw new HotdogedException(e);
            }
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
    }

    public static String extractNameFromHeaderField(String field) {
        if (field == null) {
            return "";
        }
        String rc = field.replaceFirst("^\"?(.+?)\"? \\<.*$", "$1");
        if (rc.equals(field) && rc.indexOf(60) >= 0) {
            return rc.substring(0, rc.indexOf(60)).trim();
        }
        return rc;
    }

    public static String extractAddressFromHeaderField(String field) {
        if (field == null) {
            return "";
        }
        String rc = field.replaceFirst("^.*\\<(.+)\\>$", "$1");
        if (rc.equals(field)) {
            return "";
        }
        return rc;
    }

    public static String date2NntpDate(long date) throws HotdogedException {
        SimpleDateFormat df = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.US);
        try {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(1000 * date);
            String result = df.format(cal.getTime());
            return result;
        } catch (Exception e) {
            throw new HotdogedException("Error formatting date: " + date + ": " + e.getMessage());
        }
    }

    public static void setPurgeTimestamp(Context context, Uri groupUri, long timeStamp) throws HotdogedException {
        ContentValues cv = new ContentValues();
        cv.put("purged", Long.valueOf(timeStamp));
        try {
            context.getContentResolver().update(groupUri, cv, null, null);
        } catch (Exception e) {
            throw new HotdogedException("Error updating db: " + e.getMessage());
        }
    }

    public static int getUnsentForServer(Context context, String categoryName, long serverId) throws HotdogedException {
        Cursor cursor = null;
        try {
            try {
                Uri serverUri = Uri.parse("content://com.pushkin.hotdoged.provider/" + categoryName + "/servers/" + serverId);
                cursor = context.getContentResolver().query(Uri.withAppendedPath(serverUri, "groups/" + getSpecialGroupIdForServer(context, 5, serverUri)), new String[]{"total"}, null, null, null);
                if (cursor.moveToFirst()) {
                    int total = cursor.getInt(0);
                    return total;
                }
                throw new HotdogedException("Failed to get amount of unsent messages for server " + serverUri.toString());
            } catch (Exception e) {
                throw new HotdogedException(e);
            }
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
    }

    public static int getGroupIdByName(Context context, Uri serverUri, String groupName) throws HotdogedException {
        Cursor cursor = null;
        try {
            try {
                cursor = context.getContentResolver().query(Uri.withAppendedPath(serverUri, "groups"), new String[]{Constants.INTENT_EXTRA_DBID}, "lower(name) = ?", new String[]{groupName.toLowerCase()}, null);
                if (cursor.moveToFirst()) {
                    return cursor.getInt(0);
                }
                if (cursor != null && !cursor.isClosed()) {
                    cursor.close();
                }
                throw new HotdogedException("Group " + groupName + " not found in server " + serverUri);
            } catch (Exception e) {
                throw new HotdogedException(e);
            }
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
    }

    public static void clearPrefs(SharedPreferences prefs, String[] fields) {
        if (prefs != null && fields != null && fields.length != 0) {
            for (String key : fields) {
                prefs.edit().remove(key).commit();
            }
        }
    }

    public static boolean isValidFTNAddress(String address) {
        return !TextUtils.isEmpty(address) && address.matches("^\\d{1,3}:\\d{1,5}\\/\\d{1,5}(\\.\\d{1,5})?");
    }

    public static int getArticleCount(Context context, GroupEntry groupEntry) throws HotdogedException {
        try {
            Cursor cursor = context.getContentResolver().query(Uri.withAppendedPath(groupEntry.getGroupUri(), "items"), new String[]{"count(_id)"}, null, null, null);
            int count = 0;
            if (cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
            cursor.close();
            return count;
        } catch (Exception e) {
            Log.e("getArticleCount", e.getMessage());
            throw new HotdogedException("Error getting article count: " + e.getMessage());
        }
    }

    public static String extractFTNHeader(String fullArticle, String header) {
        int i;
        if (fullArticle != null && (i = fullArticle.indexOf("\u0001" + header + ": ")) >= 0) {
            String headerValue = fullArticle.substring(i + 3 + header.length(), fullArticle.indexOf("\n", i + 3 + header.length()));
            return headerValue;
        }
        return null;
    }

    public static String getOriginalGroup(Context context, ItemEntry itemEntry, Uri serverUri) throws HotdogedException {
        String newsgroups = null;
        String categoryName = serverUri.getPathSegments().get(0);
        Uri originalItemUri = Uri.parse("content://com.pushkin.hotdoged.provider/" + categoryName + "/items/" + itemEntry.get_id());
        Cursor cursor = context.getContentResolver().query(originalItemUri, new String[]{"group_id"}, null, null, null);
        if (cursor.moveToFirst()) {
            int groupId = cursor.getInt(0);
            Uri originalGroupUri = Uri.withAppendedPath(serverUri, "groups/" + groupId);
            GroupEntry originalGroupEntry = new GroupEntry(context, originalGroupUri);
            newsgroups = originalGroupEntry.getName();
        }
        cursor.close();
        return newsgroups;
    }

    public static String copyFileToExtMemory(String fileName, String dirSuffix, boolean deleteSource) throws HotdogedException {
        String state = Environment.getExternalStorageState();
        if ("mounted".equals(state)) {
            File extDir = new File(Environment.getExternalStorageDirectory() + "/HotdogEd/" + dirSuffix);
            if (!extDir.mkdirs() && !extDir.isDirectory()) {
                throw new HotdogedException("Невозможно создать папку: " + extDir.getAbsolutePath());
            }
            File srcFile = new File(fileName);
            if (!srcFile.canRead()) {
                throw new HotdogedException("Cannot access file for read: " + srcFile.getAbsolutePath());
            }
            if (!srcFile.canWrite() && deleteSource) {
                throw new HotdogedException("Cannot access file for write: " + srcFile.getAbsolutePath());
            }
            String dstFileName = srcFile.getName();
            File dstFile = new File(extDir.getAbsolutePath() + "/" + dstFileName);
            try {
                FileChannel src = new FileInputStream(srcFile).getChannel();
                FileChannel dst = new FileOutputStream(dstFile).getChannel();
                dst.transferFrom(src, 0L, src.size());
                src.close();
                dst.close();
                if (deleteSource) {
                    srcFile.delete();
                }
                return dstFile.getAbsolutePath();
            } catch (Exception e) {
                throw new HotdogedException(e);
            }
        } else if ("mounted_ro".equals(state)) {
            throw new HotdogedException("External storage is read only");
        } else {
            throw new HotdogedException("External storage is not available");
        }
    }

    public static void updateServerSyncTime(Context context, Uri serverUri) throws HotdogedException {
        try {
            ContentValues cv = new ContentValues();
            Calendar cal = Calendar.getInstance();
            long t = cal.getTimeInMillis() / 1000;
            cv.put("last_sync", Long.valueOf(t));
            context.getContentResolver().update(serverUri, cv, null, null);
            Log.d("updateServerSyncTime", "Server update time updated to: " + t);
        } catch (Exception e) {
            throw new HotdogedException(e);
        }
    }

    /* WARN: Code restructure failed: missing block: B:12:0x00da, code lost:
    
        if (r8.size() != 0) goto L24;
     */
    /* WARN: Code restructure failed: missing block: B:13:0x00dc, code lost:
    
        return null;
     */
    /* WARN: Code restructure failed: missing block: B:30:?, code lost:
    
        return (android.net.Uri[]) r8.toArray(new android.net.Uri[0]);
     */
    /* WARN: Code restructure failed: missing block: B:4:0x0057, code lost:
    
        if (r6.moveToFirst() != false) goto L5;
     */
    /* WARN: Code restructure failed: missing block: B:5:0x0059, code lost:
    
        r10 = r6.getLong(1);
        r9 = android.net.Uri.parse("content://com.pushkin.hotdoged.provider/" + r6.getString(2) + "/servers/" + r6.getInt(0));
        r8.add(r9);
        android.util.Log.d("getScheduledServersForCategory", "Server " + r6.getInt(0) + " scheduled for time " + r10 + " (in " + (r12 - r10) + " seconds)");
     */
    /* WARN: Code restructure failed: missing block: B:6:0x00c9, code lost:
    
        if (r6.moveToNext() != false) goto L29;
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public static android.net.Uri[] getScheduledServersForCategory(android.content.Context r14, java.lang.String r15) throws com.pushkin.hotdoged.export.HotdogedException {
        /*
            java.lang.String r0 = "content://com.pushkin.hotdoged.provider/servers"
            android.net.Uri r1 = android.net.Uri.parse(r0)
            r6 = 0
            java.util.ArrayList r8 = new java.util.ArrayList
            r8.<init>()
            java.util.Calendar r0 = java.util.Calendar.getInstance()     // Catch: java.lang.Exception -> Lde java.lang.Throwable -> Le5
            long r2 = r0.getTimeInMillis()     // Catch: java.lang.Exception -> Lde java.lang.Throwable -> Le5
            r4 = 1000(0x3e8, double:4.94E-321)
            long r12 = r2 / r4
            android.content.ContentResolver r0 = r14.getContentResolver()     // Catch: java.lang.Exception -> Lde java.lang.Throwable -> Le5
            r2 = 3
            java.lang.String[] r2 = new java.lang.String[r2]     // Catch: java.lang.Exception -> Lde java.lang.Throwable -> Le5
            r3 = 0
            java.lang.String r4 = "_id"
            r2[r3] = r4     // Catch: java.lang.Exception -> Lde java.lang.Throwable -> Le5
            r3 = 1
            java.lang.String r4 = "last_sync + schedule_time * 60 as t"
            r2[r3] = r4     // Catch: java.lang.Exception -> Lde java.lang.Throwable -> Le5
            r3 = 2
            java.lang.String r4 = "category_name"
            r2[r3] = r4     // Catch: java.lang.Exception -> Lde java.lang.Throwable -> Le5
            java.lang.StringBuilder r3 = new java.lang.StringBuilder     // Catch: java.lang.Exception -> Lde java.lang.Throwable -> Le5
            r3.<init>()     // Catch: java.lang.Exception -> Lde java.lang.Throwable -> Le5
            java.lang.String r4 = "(t <= "
            java.lang.StringBuilder r3 = r3.append(r4)     // Catch: java.lang.Exception -> Lde java.lang.Throwable -> Le5
            java.lang.StringBuilder r3 = r3.append(r12)     // Catch: java.lang.Exception -> Lde java.lang.Throwable -> Le5
            java.lang.String r4 = " or t is null) and _id is not null and category_name = ? and (schedule_time is not null and schedule_time > 0) and server_active == 1"
            java.lang.StringBuilder r3 = r3.append(r4)     // Catch: java.lang.Exception -> Lde java.lang.Throwable -> Le5
            java.lang.String r3 = r3.toString()     // Catch: java.lang.Exception -> Lde java.lang.Throwable -> Le5
            r4 = 1
            java.lang.String[] r4 = new java.lang.String[r4]     // Catch: java.lang.Exception -> Lde java.lang.Throwable -> Le5
            r5 = 0
            r4[r5] = r15     // Catch: java.lang.Exception -> Lde java.lang.Throwable -> Le5
            java.lang.String r5 = "t"
            android.database.Cursor r6 = r0.query(r1, r2, r3, r4, r5)     // Catch: java.lang.Exception -> Lde java.lang.Throwable -> Le5
            boolean r0 = r6.moveToFirst()     // Catch: java.lang.Exception -> Lde java.lang.Throwable -> Le5
            if (r0 == 0) goto Lcb
        L59:
            r0 = 1
            long r10 = r6.getLong(r0)     // Catch: java.lang.Exception -> Lde java.lang.Throwable -> Le5
            java.lang.StringBuilder r0 = new java.lang.StringBuilder     // Catch: java.lang.Exception -> Lde java.lang.Throwable -> Le5
            r0.<init>()     // Catch: java.lang.Exception -> Lde java.lang.Throwable -> Le5
            java.lang.String r2 = "content://com.pushkin.hotdoged.provider/"
            java.lang.StringBuilder r0 = r0.append(r2)     // Catch: java.lang.Exception -> Lde java.lang.Throwable -> Le5
            r2 = 2
            java.lang.String r2 = r6.getString(r2)     // Catch: java.lang.Exception -> Lde java.lang.Throwable -> Le5
            java.lang.StringBuilder r0 = r0.append(r2)     // Catch: java.lang.Exception -> Lde java.lang.Throwable -> Le5
            java.lang.String r2 = "/servers/"
            java.lang.StringBuilder r0 = r0.append(r2)     // Catch: java.lang.Exception -> Lde java.lang.Throwable -> Le5
            r2 = 0
            int r2 = r6.getInt(r2)     // Catch: java.lang.Exception -> Lde java.lang.Throwable -> Le5
            java.lang.StringBuilder r0 = r0.append(r2)     // Catch: java.lang.Exception -> Lde java.lang.Throwable -> Le5
            java.lang.String r0 = r0.toString()     // Catch: java.lang.Exception -> Lde java.lang.Throwable -> Le5
            android.net.Uri r9 = android.net.Uri.parse(r0)     // Catch: java.lang.Exception -> Lde java.lang.Throwable -> Le5
            r8.add(r9)     // Catch: java.lang.Exception -> Lde java.lang.Throwable -> Le5
            java.lang.String r0 = "getScheduledServersForCategory"
            java.lang.StringBuilder r2 = new java.lang.StringBuilder     // Catch: java.lang.Exception -> Lde java.lang.Throwable -> Le5
            r2.<init>()     // Catch: java.lang.Exception -> Lde java.lang.Throwable -> Le5
            java.lang.String r3 = "Server "
            java.lang.StringBuilder r2 = r2.append(r3)     // Catch: java.lang.Exception -> Lde java.lang.Throwable -> Le5
            r3 = 0
            int r3 = r6.getInt(r3)     // Catch: java.lang.Exception -> Lde java.lang.Throwable -> Le5
            java.lang.StringBuilder r2 = r2.append(r3)     // Catch: java.lang.Exception -> Lde java.lang.Throwable -> Le5
            java.lang.String r3 = " scheduled for time "
            java.lang.StringBuilder r2 = r2.append(r3)     // Catch: java.lang.Exception -> Lde java.lang.Throwable -> Le5
            java.lang.StringBuilder r2 = r2.append(r10)     // Catch: java.lang.Exception -> Lde java.lang.Throwable -> Le5
            java.lang.String r3 = " (in "
            java.lang.StringBuilder r2 = r2.append(r3)     // Catch: java.lang.Exception -> Lde java.lang.Throwable -> Le5
            long r4 = r12 - r10
            java.lang.StringBuilder r2 = r2.append(r4)     // Catch: java.lang.Exception -> Lde java.lang.Throwable -> Le5
            java.lang.String r3 = " seconds)"
            java.lang.StringBuilder r2 = r2.append(r3)     // Catch: java.lang.Exception -> Lde java.lang.Throwable -> Le5
            java.lang.String r2 = r2.toString()     // Catch: java.lang.Exception -> Lde java.lang.Throwable -> Le5
            android.util.Log.d(r0, r2)     // Catch: java.lang.Exception -> Lde java.lang.Throwable -> Le5
            boolean r0 = r6.moveToNext()     // Catch: java.lang.Exception -> Lde java.lang.Throwable -> Le5
            if (r0 != 0) goto L59
        Lcb:
            if (r6 == 0) goto Ld6
            boolean r0 = r6.isClosed()
            if (r0 != 0) goto Ld6
            r6.close()
        Ld6:
            int r0 = r8.size()
            if (r0 != 0) goto Lf2
            r0 = 0
        Ldd:
            return r0
        Lde:
            r7 = move-exception
            com.pushkin.hotdoged.export.HotdogedException r0 = new com.pushkin.hotdoged.export.HotdogedException     // Catch: java.lang.Throwable -> Le5
            r0.<init>(r7)     // Catch: java.lang.Throwable -> Le5
            throw r0     // Catch: java.lang.Throwable -> Le5
        Le5:
            r0 = move-exception
            if (r6 == 0) goto Lf1
            boolean r2 = r6.isClosed()
            if (r2 != 0) goto Lf1
            r6.close()
        Lf1:
            throw r0
        Lf2:
            r0 = 0
            android.net.Uri[] r0 = new android.net.Uri[r0]
            java.lang.Object[] r0 = r8.toArray(r0)
            android.net.Uri[] r0 = (android.net.Uri[]) r0
            goto Ldd
        */
        throw new UnsupportedOperationException("Method not decompiled: com.pushkin.hotdoged.export.Utils.getScheduledServersForCategory(android.content.Context, java.lang.String):android.net.Uri[]");
    }

    public static boolean deleteRecursive(File path) throws FileNotFoundException {
        if (!path.exists()) {
            throw new FileNotFoundException(path.getAbsolutePath());
        }
        boolean ret = true;
        if (path.isDirectory()) {
            for (File f : path.listFiles()) {
                ret = ret && deleteRecursive(f);
            }
        }
        return ret && path.delete();
    }

    public static void copyFile(String srcFileName, String dstFileName) throws HotdogedException {
        try {
            FileChannel src = new FileInputStream(srcFileName).getChannel();
            FileChannel dst = new FileOutputStream(dstFileName).getChannel();
            dst.transferFrom(src, 0L, src.size());
            src.close();
            dst.close();
        } catch (Exception e) {
            throw new HotdogedException(e);
        }
    }

    public static void changeGroupForMessage(Context context, String categoryName, long itemId, long groupId) throws HotdogedException {
        Uri itemUri = Uri.parse("content://com.pushkin.hotdoged.provider/" + categoryName + "/items/" + itemId);
        try {
            ContentValues cv = new ContentValues();
            cv.put("group_id", Long.valueOf(groupId));
            context.getContentResolver().update(itemUri, cv, null, null);
            Log.d("changeGroupForMessage", "Message " + itemUri.toString() + " moved to group " + groupId);
        } catch (Exception e) {
            Log.e("changeGroupForMessage", "Message " + itemUri.toString() + " NOT moved to group " + groupId + ": " + e.getMessage());
            throw new HotdogedException(e);
        }
    }

    /* WARN: Code restructure failed: missing block: B:12:0x0047, code lost:
    
        return (java.lang.String[]) r8.toArray(new java.lang.String[0]);
     */
    /* WARN: Code restructure failed: missing block: B:4:0x0024, code lost:
    
        if (r6.moveToFirst() != false) goto L5;
     */
    /* WARN: Code restructure failed: missing block: B:5:0x0026, code lost:
    
        r8.add(r6.getString(0));
     */
    /* WARN: Code restructure failed: missing block: B:6:0x0032, code lost:
    
        if (r6.moveToNext() != false) goto L26;
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public static java.lang.String[] getCategoryNames(android.content.Context r10) throws com.pushkin.hotdoged.export.HotdogedException {
        /*
            r9 = 0
            java.util.ArrayList r8 = new java.util.ArrayList
            r8.<init>()
            r6 = 0
            java.lang.String r0 = "content://com.pushkin.hotdoged.provider/Categories"
            android.net.Uri r1 = android.net.Uri.parse(r0)     // Catch: java.lang.Exception -> L48 java.lang.Throwable -> L6b
            android.content.ContentResolver r0 = r10.getContentResolver()     // Catch: java.lang.Exception -> L48 java.lang.Throwable -> L6b
            r2 = 1
            java.lang.String[] r2 = new java.lang.String[r2]     // Catch: java.lang.Exception -> L48 java.lang.Throwable -> L6b
            r3 = 0
            java.lang.String r4 = "name"
            r2[r3] = r4     // Catch: java.lang.Exception -> L48 java.lang.Throwable -> L6b
            r3 = 0
            r4 = 0
            r5 = 0
            android.database.Cursor r6 = r0.query(r1, r2, r3, r4, r5)     // Catch: java.lang.Exception -> L48 java.lang.Throwable -> L6b
            boolean r0 = r6.moveToFirst()     // Catch: java.lang.Exception -> L48 java.lang.Throwable -> L6b
            if (r0 == 0) goto L34
        L26:
            r0 = 0
            java.lang.String r0 = r6.getString(r0)     // Catch: java.lang.Exception -> L48 java.lang.Throwable -> L6b
            r8.add(r0)     // Catch: java.lang.Exception -> L48 java.lang.Throwable -> L6b
            boolean r0 = r6.moveToNext()     // Catch: java.lang.Exception -> L48 java.lang.Throwable -> L6b
            if (r0 != 0) goto L26
        L34:
            if (r6 == 0) goto L3f
            boolean r0 = r6.isClosed()
            if (r0 != 0) goto L3f
            r6.close()
        L3f:
            java.lang.String[] r0 = new java.lang.String[r9]
            java.lang.Object[] r0 = r8.toArray(r0)
            java.lang.String[] r0 = (java.lang.String[]) r0
            return r0
        L48:
            r7 = move-exception
            java.io.PrintStream r0 = java.lang.System.err     // Catch: java.lang.Throwable -> L6b
            r7.printStackTrace(r0)     // Catch: java.lang.Throwable -> L6b
            com.pushkin.hotdoged.export.HotdogedException r0 = new com.pushkin.hotdoged.export.HotdogedException     // Catch: java.lang.Throwable -> L6b
            java.lang.StringBuilder r2 = new java.lang.StringBuilder     // Catch: java.lang.Throwable -> L6b
            r2.<init>()     // Catch: java.lang.Throwable -> L6b
            java.lang.String r3 = "Error getting categories list: "
            java.lang.StringBuilder r2 = r2.append(r3)     // Catch: java.lang.Throwable -> L6b
            java.lang.String r3 = r7.getMessage()     // Catch: java.lang.Throwable -> L6b
            java.lang.StringBuilder r2 = r2.append(r3)     // Catch: java.lang.Throwable -> L6b
            java.lang.String r2 = r2.toString()     // Catch: java.lang.Throwable -> L6b
            r0.<init>(r2)     // Catch: java.lang.Throwable -> L6b
            throw r0     // Catch: java.lang.Throwable -> L6b
        L6b:
            r0 = move-exception
            if (r6 == 0) goto L77
            boolean r2 = r6.isClosed()
            if (r2 != 0) goto L77
            r6.close()
        L77:
            throw r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.pushkin.hotdoged.export.Utils.getCategoryNames(android.content.Context):java.lang.String[]");
    }

    public static String postHttpRequest(String url, List<NameValuePair> args) throws UnsupportedEncodingException, IOException, ClientProtocolException, HotdogedException {
        return postHttpRequest(url, args, null, null, false);
    }

    public static String postHttpRequest(String url, List<NameValuePair> args, List<FileAttachment> attachments, ContentType contentType, boolean fileNamesToAnsi) throws UnsupportedEncodingException, IOException, ClientProtocolException, HotdogedException {
        HttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(url);
        if (attachments == null) {
            UrlEncodedFormEntity urlEntity = new UrlEncodedFormEntity(args, "UTF-8");
            httpPost.setEntity(urlEntity);
        } else {
            MultipartEntityBuilder reqEntity = MultipartEntityBuilder.create();
            reqEntity.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
            httpPost.addHeader("Content-Type", "multipart/form-data");
            for (NameValuePair pair : args) {
                reqEntity.addTextBody(pair.getName(), pair.getValue(), ContentType.DEFAULT_TEXT);
            }
            for (FileAttachment attachment : attachments) {
                File file = new File(attachment.getLocalFilePath());
                String fileName = fileNamesToAnsi ? utf82ansi(attachment.getRemoteFileName()) : attachment.getRemoteFileName();
                reqEntity.addBinaryBody(attachment.getParam(), file, contentType, fileName);
            }
            httpPost.setEntity(reqEntity.build());
        }
        HttpResponse httpResponse = httpClient.execute(httpPost);
        if (httpResponse.getStatusLine().getStatusCode() != 200) {
            throw new HotdogedException("HTTP error received: " + httpResponse.getStatusLine().getStatusCode() + ", " + httpResponse.getStatusLine().getReasonPhrase());
        }
        InputStream inputStream = httpResponse.getEntity().getContent();
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        StringBuilder stringBuilder = new StringBuilder();
        while (true) {
            String bufferedStrChunk = bufferedReader.readLine();
            if (bufferedStrChunk == null) {
                String serverResponse = stringBuilder.toString();
                return serverResponse;
            }
            stringBuilder.append((stringBuilder.length() == 0 ? "" : "\n") + bufferedStrChunk);
        }
    }

    public static String utf82ansi(String utf8String) {
        if (utf8String == null) {
            return null;
        }
        String rc = "";
        for (char c : utf8String.toCharArray()) {
            if ((c >= '-' && c <= '9') || ((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z'))) {
                rc = rc + c;
            } else {
                rc = rc + 'x';
            }
        }
        return rc;
    }

    public static String getHttpRequest(String url) throws UnsupportedEncodingException, IOException, ClientProtocolException, HotdogedException {
        HttpClient httpClient = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet(url);
        HttpResponse httpResponse = httpClient.execute(httpGet);
        if (httpResponse.getStatusLine().getStatusCode() != 200) {
            throw new HotdogedException("HTTP error received: " + httpResponse.getStatusLine().getStatusCode() + ", " + httpResponse.getStatusLine().getReasonPhrase());
        }
        InputStream inputStream = httpResponse.getEntity().getContent();
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        StringBuilder stringBuilder = new StringBuilder();
        while (true) {
            String bufferedStrChunk = bufferedReader.readLine();
            if (bufferedStrChunk == null) {
                String serverResponse = stringBuilder.toString();
                return serverResponse;
            }
            stringBuilder.append((stringBuilder.length() == 0 ? "" : "\n") + bufferedStrChunk);
        }
    }

    public static Intent createExplicitFromImplicitIntent(Context context, Intent implicitIntent) {
        PackageManager pm = context.getPackageManager();
        List<ResolveInfo> resolveInfo = pm.queryIntentServices(implicitIntent, 0);
        if (resolveInfo == null || resolveInfo.size() != 1) {
            return null;
        }
        ResolveInfo serviceInfo = resolveInfo.get(0);
        String packageName = serviceInfo.serviceInfo.packageName;
        String className = serviceInfo.serviceInfo.name;
        ComponentName component = new ComponentName(packageName, className);
        Intent explicitIntent = new Intent(implicitIntent);
        explicitIntent.setComponent(component);
        return explicitIntent;
    }

    public static String getCategoryNameForServerId(Context context, int serverId) throws HotdogedException {
        Uri serverUri = Uri.parse("content://com.pushkin.hotdoged.provider/servers/" + serverId);
        Cursor cursor = context.getContentResolver().query(serverUri, new String[]{"category_id"}, null, null, null);
        if (cursor.moveToFirst()) {
            int categoryId = cursor.getInt(0);
            cursor.close();
            return getCategoryNameById(context, categoryId);
        }
        throw new HotdogedException("Could not get category name for server " + serverUri);
    }
}
