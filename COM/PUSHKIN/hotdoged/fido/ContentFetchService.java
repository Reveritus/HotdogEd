package com.pushkin.hotdoged.fido;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.support.v7.app.NotificationCompat;
import android.widget.Toast;
import com.pushkin.ftn.Link;
import com.pushkin.ftn.Main;
import com.pushkin.hotdoged.export.Constants;
import com.pushkin.hotdoged.export.GroupEntry;
import com.pushkin.hotdoged.export.HotdogedException;
import com.pushkin.hotdoged.export.ItemEntry;
import com.pushkin.hotdoged.export.ServerEntry;
import com.pushkin.hotdoged.export.Utils;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import jnode.event.FileReceivingEvent;
import jnode.event.FileSendingEvent;
import jnode.event.FileTossingEvent;
import jnode.event.FtnMessageReceivedEvent;
import jnode.event.IEvent;
import jnode.event.IEventHandler;
import jnode.event.Notifier;
import jnode.event.UnknownFileEvent;
import jnode.protocol.binkp.BinkpConnector;
import jnode.protocol.io.Connector;

public class ContentFetchService extends Service {
    public static final String CATEGORY_NAME = "ftn";
    public static final String NDL = "115200,TCP,BINKP";
    private static final String NETMAIL = "NETMAIL";
    private static final int NOTIF_ID = 2;
    private static final String TAG = "ContentFetchService";
    private Intent intent;
    protected MessageHandler messageHandler;
    private NotificationCompat.Builder notification;
    private NotificationManager notificationManager;
    private String object;
    private PendingIntent pendingIntent;
    private int purgePeriod;
    private Service service;
    private int syncType;
    public static final String DEFAULT_TEARLINE = Main.SystemInfo.getPID();
    private static ExecutorService es = Executors.newFixedThreadPool(1);
    private boolean isSyncing = false;
    private HashMap<String, Integer> messageCount = new HashMap<>();
    private ArrayList<String> unknownFiles = new ArrayList<>();

    /* INFO: Access modifiers changed from: private */
    public static class MessageHandler extends Handler {
        private Context context;

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            Toast.makeText(this.context, (String) msg.obj, 1).show();
        }

        public MessageHandler(Context context) {
            this.context = context;
        }
    }

    private class SyncTask implements Runnable {
        private final Context context;
        private final String object;
        private final int startId;
        private final int syncType;

        public SyncTask(Context context, int syncType, String object, int startId) {
            this.context = context;
            this.syncType = syncType;
            this.object = object;
            this.startId = startId;
            Main.info = null;
        }

        private void runSyncServer(String object) throws HotdogedException {
            Main.SystemInfo.getLogger().log(ContentFetchService.TAG, "Fetching server: " + object);
            Uri serverUri = Uri.parse(object);
            Uri groupsUri = Uri.withAppendedPath(serverUri, "groups");
            Cursor cursor = ContentFetchService.this.getContentResolver().query(groupsUri, null, "grouptype_id <> ?", new String[]{String.valueOf(10)}, "grouptype_id desc");
            if (cursor.moveToFirst()) {
                ServerEntry serverEntry = new ServerEntry(this.context, serverUri);
                Main.SystemInfo.getLogger().log(ContentFetchService.TAG, "Server domain: " + serverEntry.getDomain());
                if (serverEntry.isServer_active()) {
                    ContentFetchService.this.startForeground(2, ContentFetchService.this.notification.setContentTitle("Fetching server " + serverEntry.getServer_name()).setContentText("Please, wait for a while").build());
                    while (true) {
                        try {
                            try {
                                int groupId = cursor.getInt(cursor.getColumnIndex(Constants.INTENT_EXTRA_DBID));
                                Uri groupUri = Uri.withAppendedPath(groupsUri, String.valueOf(groupId));
                                GroupEntry groupEntry = new GroupEntry(ContentFetchService.this.getContext(), groupUri);
                                if (groupEntry.getGrouptype_id() != 3) {
                                    ContentFetchService.this.purgeGroup(serverEntry, groupEntry, ContentFetchService.this.purgePeriod);
                                }
                                if (ContentFetchService.this.isNeedsStop()) {
                                    Main.SystemInfo.getLogger().log(ContentFetchService.TAG, "Stop requested.");
                                    break;
                                } else if (!cursor.moveToNext()) {
                                    break;
                                }
                            } catch (Exception e) {
                                String errMsg = e.getMessage() != null ? e.getMessage() : "Unknown error occured";
                                Main.SystemInfo.getLogger().log(ContentFetchService.TAG, errMsg);
                                throw new HotdogedException("Ошибка: " + errMsg);
                            }
                        } finally {
                            cursor.close();
                        }
                    }
                    synchronized (ContentFetchService.class) {
                        ContentFetchService.this.pollNode(serverEntry);
                    }
                    Utils.updateServerSyncTime(this.context, serverUri);
                }
            }
        }

        private void runSyncCategory(String object) throws HotdogedException {
            Main.SystemInfo.getLogger().log(ContentFetchService.TAG, "Fetching category: " + object);
            Uri categoryUri = Uri.parse(object);
            Uri serverUri = Uri.withAppendedPath(categoryUri, "servers");
            ContentFetchService.this.startForeground(2, ContentFetchService.this.notification.setContentTitle("Fetching category " + categoryUri.toString()).setContentText("Please, wait for a while").build());
            Cursor cursor = ContentFetchService.this.getContentResolver().query(serverUri, null, null, null, null);
            if (!cursor.moveToFirst()) {
                cursor.close();
                ContentFetchService.this.stopService(ContentFetchService.this.intent);
                return;
            }
            while (true) {
                int serverId = cursor.getInt(cursor.getColumnIndex(Constants.INTENT_EXTRA_DBID));
                runSyncServer(Uri.withAppendedPath(serverUri, String.valueOf(serverId)).toString());
                if (ContentFetchService.this.isNeedsStop()) {
                    Main.SystemInfo.getLogger().log(ContentFetchService.TAG, "Stop requested.");
                    break;
                } else if (!cursor.moveToNext()) {
                    break;
                }
            }
            cursor.close();
        }

        @Override // java.lang.Runnable
        public void run() {
            Main.SystemInfo.getLogger().log(ContentFetchService.TAG, "Running with startid = " + this.startId);
            PowerManager.WakeLock wakeLock = null;
            try {
                try {
                    PowerManager mgr = (PowerManager) this.context.getSystemService("power");
                    wakeLock = mgr.newWakeLock(1, "Hotdoged FTN WakeLock");
                    wakeLock.acquire();
                    Main.SystemInfo.getLogger().log(ContentFetchService.TAG, "WakeLock acquired");
                    GroupEntry.clearCache();
                    ItemEntry.clearCache();
                    switch (this.syncType) {
                        case 2:
                            Main.SystemInfo.getEventsArray().clear();
                            runSyncServer(this.object);
                            Utils.notifyContentUpdated(this.context, ContentFetchService.CATEGORY_NAME, null, null, -1);
                            break;
                        case 3:
                            Main.SystemInfo.getEventsArray().clear();
                            runSyncCategory(this.object);
                            Utils.notifyContentUpdated(this.context, ContentFetchService.CATEGORY_NAME, null, null, -1);
                            break;
                        case 4:
                        case 5:
                        default:
                            Main.SystemInfo.getLogger().log(ContentFetchService.TAG, "Unknown synchronization type: " + this.syncType);
                            break;
                        case 6:
                            Main.SystemInfo.getEventsArray().clear();
                            runSyncScheduled();
                            Utils.notifyContentUpdated(this.context, ContentFetchService.CATEGORY_NAME, null, null, -1);
                            break;
                    }
                    ContentFetchService.this.stopForeground(true);
                    wakeLock.release();
                    Main.SystemInfo.getLogger().log(ContentFetchService.TAG, "WakeLock released");
                    ContentFetchService.this.stopService(ContentFetchService.this.intent);
                } catch (HotdogedException e) {
                    Intent notificationIntent = new Intent(ContentFetchService.this.getBaseContext(), LogActivity.class);
                    notificationIntent.putExtra("data", Main.SystemInfo.getEventsArray());
                    PendingIntent pendingIntent = PendingIntent.getActivity(this.context, 0, notificationIntent, 268435456);
                    ContentFetchService.this.startForeground(2, ContentFetchService.this.notification.setContentTitle("Ошибка синхронизации. Нажми для подробностей.").setContentText(e.getMessage()).setDefaults(17).setOngoing(false).setSmallIcon(17301624).setContentIntent(pendingIntent).build());
                    ContentFetchService.this.stopForeground(false);
                    ContentFetchService.this.stopForeground(false);
                    ContentFetchService.this.setSyncing(false);
                    wakeLock.release();
                    Main.SystemInfo.getLogger().log(ContentFetchService.TAG, "WakeLock released");
                }
            } catch (Throwable th) {
                wakeLock.release();
                Main.SystemInfo.getLogger().log(ContentFetchService.TAG, "WakeLock released");
                throw th;
            }
        }

        private void runSyncScheduled() throws HotdogedException {
            Uri[] servers = Utils.getScheduledServersForCategory(this.context, ContentFetchService.CATEGORY_NAME);
            if (servers != null) {
                HotdogedException error = null;
                for (Uri server : servers) {
                    try {
                        runSyncServer(server.toString());
                    } catch (HotdogedException e) {
                        error = new HotdogedException(e);
                    }
                }
                if (error != null) {
                    throw new HotdogedException(error);
                }
            }
        }
    }

    public synchronized boolean isSyncing() {
        return this.isSyncing;
    }

    public static String getStationName(Context context, ServerEntry serverEntry) {
        return (serverEntry == null || serverEntry.getServer_description() == null || serverEntry.getServer_description().trim().length() == 0) ? context.getString(R.string.default_system_name) : serverEntry.getServer_description();
    }

    public static String getLocation() {
        return Constants.DEFAULT_ORIGIN;
    }

    private class TransferFileHandler implements IEventHandler {
        private TransferFileHandler() {
        }

        @Override // jnode.event.IEventHandler
        public void handle(IEvent event) {
            Main.SystemInfo.getLogger().log(ContentFetchService.TAG, event.getEvent());
            ContentFetchService.this.updateNotification(Main.info.getBossAddress() + ": " + event.getEvent(), "Нажми, чтобы остановить");
        }
    }

    public class FileTossingHandler implements IEventHandler {
        public FileTossingHandler() {
        }

        @Override // jnode.event.IEventHandler
        public void handle(IEvent event) {
            Main.SystemInfo.getLogger().log(ContentFetchService.TAG, event.getEvent());
            ContentFetchService.this.updateNotification(Main.info.getBossAddress() + ": " + event.getEvent(), "Нажми, чтобы остановить");
        }
    }

    private class MessageReceivedHandler implements IEventHandler {
        private MessageReceivedHandler() {
        }

        @Override // jnode.event.IEventHandler
        public void handle(IEvent event) {
            Main.SystemInfo.getLogger().log(ContentFetchService.TAG, event.getEvent());
            String groupName = ((FtnMessageReceivedEvent) event).getFtnMessage().getArea();
            if (groupName == null) {
                groupName = ContentFetchService.NETMAIL;
            }
            if (ContentFetchService.this.messageCount.containsKey(groupName)) {
                int cnt = ((Integer) ContentFetchService.this.messageCount.get(groupName)).intValue();
                ContentFetchService.this.messageCount.put(groupName, Integer.valueOf(cnt + 1));
                return;
            }
            ContentFetchService.this.messageCount.put(groupName, 1);
        }
    }

    public class UnknownFileHandler implements IEventHandler {
        public UnknownFileHandler() {
        }

        @Override // jnode.event.IEventHandler
        public void handle(IEvent event) {
            UnknownFileEvent ufEvent = (UnknownFileEvent) event;
            String fileName = ufEvent.getFileName();
            ContentFetchService.this.unknownFiles.add(fileName);
        }
    }

    public void pollNode(ServerEntry serverEntry) throws HotdogedException {
        updateNotification("Опрос " + serverEntry.getServer_name(), "Нажми, чтобы остановить");
        try {
            Main.info = new Main.SystemInfo(this, serverEntry.getUser_name(), getLocation(), getStationName(this, serverEntry), serverEntry.getUser_address(), serverEntry.getServer_name(), NDL, DEFAULT_TEARLINE, serverEntry);
            Link link = new Link(Long.valueOf(serverEntry.get_id()));
            Main.info.setLink(link);
            Main.SystemInfo.getLogger().log(TAG, "Опрос " + Main.info.getBossAddress() + Main.info.getDomain());
            this.messageCount.clear();
            this.unknownFiles.clear();
            BinkpConnector binkpConnector = new BinkpConnector();
            Connector connector = new Connector(binkpConnector);
            if (link != null) {
                Main.SystemInfo.getLogger().log(TAG, String.format("Outgoing to %s (%s:%d)", link.getLinkAddress(), link.getProtocolHost(), link.getProtocolPort()));
                connector.connect(link);
                Main.SystemInfo.getLogger().log(TAG, "Соединение завершено с " + link.getLinkAddress());
            }
            binkpConnector.reset();
            Main.SystemInfo.getLogger().log(TAG, "Статистика мессаг:\n" + this.messageCount);
            updateGroupsStats(serverEntry);
            showUnknownFiles();
        } catch (Exception e) {
            throw new HotdogedException("Исключение при опросе ноды " + serverEntry.getServer_name() + ": " + e.getMessage());
        }
    }

    private void showUnknownFiles() {
        if (!this.unknownFiles.isEmpty()) {
            String message = "HotdogEd FTN: unknown file(s) moved to external storage:\n" + getFileList(this.unknownFiles);
            this.messageHandler.sendMessage(this.messageHandler.obtainMessage(0, message));
        }
    }

    private String getFileList(ArrayList<String> files) {
        String rc = "";
        if (files != null && !files.isEmpty()) {
            Iterator<String> it = files.iterator();
            while (it.hasNext()) {
                String fileName = it.next();
                rc = rc + (rc.length() > 0 ? ", " : "") + fileName;
            }
        }
        return rc;
    }

    private void updateGroupsStats(ServerEntry serverEntry) throws HotdogedException {
        Uri groupUri;
        GroupEntry.clearCache();
        Uri groupsUri = Uri.withAppendedPath(serverEntry.getServerUri(), "groups");
        for (String groupName : this.messageCount.keySet()) {
            if (groupName.equals(NETMAIL)) {
                groupUri = Uri.withAppendedPath(groupsUri, String.valueOf(Utils.getSpecialGroupIdForServer(this, 1, serverEntry.getServerUri())));
            } else {
                groupUri = Uri.withAppendedPath(groupsUri, String.valueOf(Utils.getGroupIdByName(this, serverEntry.getServerUri(), groupName.toLowerCase())));
            }
            GroupEntry groupEntry = new GroupEntry(this, groupUri);
            int newMsgs = this.messageCount.get(groupName).intValue() + groupEntry.getNew_msgs();
            if (newMsgs > 0) {
                if (newMsgs > groupEntry.getUnread()) {
                    newMsgs = groupEntry.getUnread();
                }
                Utils.setNewMsgs(this, groupUri, newMsgs, false);
            }
        }
        GroupEntry.clearCache();
    }

    public Context getContext() {
        return this;
    }

    /* INFO: Access modifiers changed from: private */
    public void purgeGroup(ServerEntry serverEntry, GroupEntry groupEntry, int purgePeriod) throws HotdogedException {
        if (purgePeriod == -1) {
            Main.SystemInfo.getLogger().log(TAG, "No purging scheduled for group " + groupEntry.getName());
            return;
        }
        if (purgePeriod > 0) {
            long currentTime = Calendar.getInstance().getTimeInMillis() / 1000;
            long timeToPurge = groupEntry.getPurged() + (purgePeriod * 60 * 60);
            if (currentTime < timeToPurge) {
                Main.SystemInfo.getLogger().log(TAG, "Not yet purging group " + groupEntry.getName() + ", purge scheduled for " + Utils.date2NntpDate(timeToPurge));
                return;
            } else {
                Utils.setPurgeTimestamp(this, groupEntry.getGroupUri(), currentTime);
                Main.SystemInfo.getLogger().log(TAG, "Purging group " + groupEntry.getName());
            }
        }
        updateNotification("Purging " + groupEntry.getName(), "Нажми, чтобы остановить синхронизацию");
        boolean purgeRead = groupEntry.isPurge_read();
        int keepArticles = serverEntry.getKeep_msg_amount_per_group();
        if (groupEntry.getKeep_msg_amount_per_group() > 0) {
            keepArticles = groupEntry.getKeep_msg_amount_per_group();
        }
        int toDeleteArticles = Utils.getArticleCount(this, groupEntry) - keepArticles;
        int keepDays = serverEntry.getKeep_msg_days_per_group();
        if (groupEntry.getKeep_msg_days_per_group() > 0) {
            keepDays = groupEntry.getKeep_msg_days_per_group();
        }
        Calendar cal = Calendar.getInstance();
        long purgeDate = (cal.getTimeInMillis() / 1000) - (((keepDays * 24) * 60) * 60);
        if (purgeRead) {
            getContentResolver().delete(Uri.withAppendedPath(groupEntry.getGroupUri(), "items"), "read = 1 and (starred <> 1 or starred is null)", null);
        }
        getContentResolver().delete(Uri.withAppendedPath(groupEntry.getGroupUri(), "items"), "date < " + purgeDate + " and (starred <> 1 or starred is null)", null);
        if (toDeleteArticles > 0) {
            getContentResolver().delete(Uri.withAppendedPath(groupEntry.getGroupUri(), "items"), "_id in (select _id from items_ftn where group_id=" + groupEntry.get_id() + " and (starred <> 1 or starred is null) order by _id limit " + toDeleteArticles + ")", null);
        }
        Main.SystemInfo.getLogger().log(TAG, "Group " + groupEntry.getName() + " purged OK, purge read = " + purgeRead + ", keep articles = " + keepArticles + ", keep days = " + keepDays);
    }

    public synchronized void updateNotification(String notificationTitle, String notificationText) {
        if (this.pendingIntent == null) {
            Intent notificationIntent = new Intent(StartBCReceiver.SYNC_INTENT_NAME);
            notificationIntent.putExtra(Constants.INTENT_EXTRA_SYNCTYPE, 4);
            this.pendingIntent = PendingIntent.getService(this.service, 0, notificationIntent, 268435456);
        }
        startForeground(2, this.notification.setContentTitle(notificationTitle).setContentText(notificationText).setContentIntent(this.pendingIntent).build());
    }

    public synchronized void setSyncing(boolean isSyncing) {
        this.isSyncing = isSyncing;
    }

    @Override // android.app.Service
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override // android.app.Service
    public void onCreate() {
        super.onCreate();
        Main.SystemInfo.getLogger().log(TAG, "onCreate()");
        this.service = this;
        this.messageHandler = new MessageHandler(getContext());
        Intent notificationIntent = new Intent(StartBCReceiver.SYNC_INTENT_NAME);
        notificationIntent.putExtra(Constants.INTENT_EXTRA_SYNCTYPE, 4);
        this.pendingIntent = PendingIntent.getService(this.service, 0, notificationIntent, 268435456);
        Notifier.INSTANSE.register(FileSendingEvent.class, new TransferFileHandler());
        Notifier.INSTANSE.register(FileReceivingEvent.class, new TransferFileHandler());
        Notifier.INSTANSE.register(FtnMessageReceivedEvent.class, new MessageReceivedHandler());
        Notifier.INSTANSE.register(FileTossingEvent.class, new FileTossingHandler());
        Notifier.INSTANSE.register(UnknownFileEvent.class, new UnknownFileHandler());
        this.notificationManager = (NotificationManager) getSystemService("notification");
        this.notification = new NotificationCompat.Builder(this);
        this.notification.setAutoCancel(false);
        this.notification.setContentTitle("FTN HotdogEd Provider");
        this.notification.setSmallIcon(17301599);
        this.notification.setContentIntent(this.pendingIntent);
        this.notification.setOngoing(true);
    }

    @Override // android.app.Service
    public void onDestroy() {
        super.onDestroy();
        Main.SystemInfo.getLogger().log(TAG, "onDestroy()");
    }

    @Override // android.app.Service
    public int onStartCommand(Intent intent, int flags, int startId) {
        Main.SystemInfo.getLogger().log(TAG, "onStartCommand(): " + intent + ", " + flags + ", " + startId);
        if (intent == null) {
            return 2;
        }
        this.intent = intent;
        this.syncType = intent.getIntExtra(Constants.INTENT_EXTRA_SYNCTYPE, -1);
        this.object = intent.getStringExtra("uri");
        this.purgePeriod = intent.getIntExtra(Constants.INTENT_EXTRA_PURGEPERIOD, -1);
        Main.SystemInfo.getLogger().log(TAG, "purgePeriod set to " + this.purgePeriod);
        if (isSyncing()) {
            if (this.syncType == 4) {
                Main.SystemInfo.getLogger().log(TAG, "Stopping requested.");
                setNeedsStop(true);
                return 3;
            }
            Main.SystemInfo.getLogger().log(TAG, "Synchronization already active. Exiting.");
            stopSelf(1);
            return 3;
        } else if (this.syncType == 4) {
            Main.SystemInfo.getLogger().log(TAG, "Stopping requested.");
            stopService(intent);
            return 3;
        } else {
            setSyncing(true);
            startForeground(2, this.notification.setContentTitle("Hotdoged is synchronizing").setContentText("Please, wait for a while").build());
            Main.SystemInfo.getLogger().log(TAG, "Service started: [" + this.syncType + "] " + this.object);
            SyncTask task = new SyncTask(this, this.syncType, this.object, startId);
            es.execute(task);
            return 1;
        }
    }

    public synchronized boolean isNeedsStop() {
        boolean z;
        if (Main.info != null) {
            z = Main.info.needsStop;
        }
        return z;
    }

    public synchronized void setNeedsStop(boolean needsStop) {
        if (Main.info != null) {
            Main.info.needsStop = needsStop;
        }
    }
}
