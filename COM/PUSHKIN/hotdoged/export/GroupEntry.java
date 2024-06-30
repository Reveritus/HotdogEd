package com.pushkin.hotdoged.export;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import com.sun.mail.imap.IMAPStore;
import java.util.HashMap;

public class GroupEntry {
    private static final HashMap<Long, GroupEntry> groupsCache = new HashMap<>();
    private int _id;
    private String codepage;
    private final Context context;
    private String custom_headers;
    private String description;
    private int filter_id;
    private final Uri groupUri;
    private int grouptype_id;
    private boolean include_special;
    private Long index;
    private boolean invisible;
    private int keep_msg_amount_per_group;
    private int keep_msg_days_per_group;
    private int last_downloaded;
    private int last_notified;
    private int last_read;
    private String name;
    private int new_msgs;
    private int notify;
    private boolean purge_read;
    private long purged;
    private boolean scoring_disabled;
    private int server_id;
    private String server_quoting;
    private String signature;
    private int template_id;
    private int unread;
    private String user_address;
    private String user_name;

    public boolean getScoring_disabled() {
        return this.scoring_disabled;
    }

    public void setScoring_disabled(boolean scoring_disabled) {
        this.scoring_disabled = scoring_disabled;
    }

    public synchronized int getNotify() {
        return this.notify;
    }

    public synchronized void setNotify(int notify) {
        this.notify = notify;
    }

    public synchronized int getLast_notified() {
        return this.last_notified;
    }

    public synchronized void setLast_notified(int last_notified) {
        this.last_notified = last_notified;
    }

    public GroupEntry(Context context, int groupId) throws HotdogedException {
        this(context, getUriById(context, groupId));
    }

    public static Uri getUriById(Context context, int groupId) throws HotdogedException {
        Uri groupUri = Uri.parse("content://com.pushkin.hotdoged.provider/groups/" + groupId);
        Cursor cursor = null;
        try {
            try {
                cursor = context.getContentResolver().query(groupUri, new String[]{Constants.INTENT_EXTRA_DBID, "server_id"}, null, null, null);
                if (!cursor.moveToFirst()) {
                    throw new HotdogedException("Group with ID " + groupUri.getLastPathSegment() + " not found");
                }
                int serverId = cursor.getInt(1);
                String categoryName = Utils.getCategoryNameForServerId(context, serverId);
                Uri uri = Uri.parse("content://com.pushkin.hotdoged.provider/" + categoryName + "/servers/" + serverId + "/groups/" + groupId);
                return uri;
            } catch (Exception e) {
                throw new HotdogedException("Error fetching group info: " + e.getMessage());
            }
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
    }

    public GroupEntry(Context context, Uri groupUri) throws HotdogedException {
        this.context = context;
        this.groupUri = groupUri;
        if (groupUri == null) {
            throw new HotdogedException("Bad URI: " + groupUri);
        }
        this._id = Integer.parseInt(groupUri.getLastPathSegment(), 10);
        this.server_id = Integer.parseInt(groupUri.getPathSegments().get(2), 10);
        this.index = Long.valueOf((this.server_id * 100000) + this._id);
        GroupEntry cached = groupsCache.get(this.index);
        if (cached != null) {
            this.grouptype_id = cached.getGrouptype_id();
            this.template_id = cached.getTemplate_id();
            this.name = cached.getName();
            this.description = cached.getDescription();
            this.codepage = cached.getCodepage();
            this.filter_id = cached.getFilter_id();
            this.keep_msg_amount_per_group = cached.getKeep_msg_amount_per_group();
            this.keep_msg_days_per_group = cached.getKeep_msg_days_per_group();
            this.user_name = cached.getUser_name();
            this.user_address = cached.getUser_address();
            this.server_quoting = cached.getServer_quoting();
            this.new_msgs = cached.getNew_msgs();
            this.last_read = cached.getLast_read();
            this.last_downloaded = cached.getLast_downloaded();
            this.signature = cached.getSignature();
            this.purge_read = cached.isPurge_read();
            this.custom_headers = cached.getCustom_headers();
            this.unread = cached.getUnread();
            this.purged = cached.getPurged();
            this.notify = cached.getNotify();
            this.include_special = cached.isInclude_special();
            this.invisible = cached.isInvisible();
            this.last_notified = cached.getLast_notified();
            this.scoring_disabled = cached.getScoring_disabled();
            return;
        }
        Cursor cursor = null;
        try {
            Cursor cursor2 = this.context.getContentResolver().query(groupUri, null, null, null, null);
            if (!cursor2.moveToFirst()) {
                throw new HotdogedException("Group with ID " + groupUri.getLastPathSegment() + " not found");
            }
            this.grouptype_id = cursor2.getInt(cursor2.getColumnIndex("grouptype_id"));
            this.template_id = cursor2.getInt(cursor2.getColumnIndex("template_id"));
            this.name = cursor2.getString(cursor2.getColumnIndex(IMAPStore.ID_NAME));
            this.description = cursor2.getString(cursor2.getColumnIndex(Constants.INTENT_EXTRA_DESCRIPTION));
            this.codepage = cursor2.getString(cursor2.getColumnIndex(Constants.INTENT_EXTRA_SERVERCODEPAGE));
            this.filter_id = cursor2.getInt(cursor2.getColumnIndex("filter_id"));
            this.keep_msg_amount_per_group = cursor2.getInt(cursor2.getColumnIndex(Constants.INTENT_EXTRA_KEEPMSGAMOUNTPERGROUP));
            this.keep_msg_days_per_group = cursor2.getInt(cursor2.getColumnIndex(Constants.INTENT_EXTRA_KEEPMSGDAYSPERGROUP));
            this.user_name = cursor2.getString(cursor2.getColumnIndex(Constants.INTENT_EXTRA_NAME));
            this.user_address = cursor2.getString(cursor2.getColumnIndex(Constants.INTENT_EXTRA_ADDRESS));
            this.server_quoting = cursor2.getString(cursor2.getColumnIndex(Constants.INTENT_EXTRA_SERVERQUOTING));
            this.new_msgs = cursor2.getInt(cursor2.getColumnIndex(Constants.INTENT_EXTRA_NEWMSGS));
            this.last_read = cursor2.getInt(cursor2.getColumnIndex("last_read"));
            this.last_downloaded = cursor2.getInt(cursor2.getColumnIndex("last_downloaded"));
            this.signature = cursor2.getString(cursor2.getColumnIndex(Constants.INTENT_EXTRA_SIGNATURE));
            this.purge_read = cursor2.getInt(cursor2.getColumnIndex(Constants.INTENT_EXTRA_PURGEREAD)) == 1;
            this.custom_headers = cursor2.getString(cursor2.getColumnIndex(Constants.INTENT_EXTRA_CUSTOMHEADERS));
            this.unread = cursor2.getInt(cursor2.getColumnIndex("unread"));
            this.purged = cursor2.getLong(cursor2.getColumnIndex("purged"));
            this.notify = cursor2.getInt(cursor2.getColumnIndex("notify"));
            this.include_special = cursor2.getInt(cursor2.getColumnIndex("include_special")) == 1;
            this.invisible = cursor2.getInt(cursor2.getColumnIndex("invisible")) == 1;
            this.scoring_disabled = cursor2.getInt(cursor2.getColumnIndex("scoring_disabled")) == 1;
            this.last_notified = cursor2.getInt(cursor2.getColumnIndex("last_notified"));
            cursor2.close();
            groupsCache.put(this.index, this);
        } catch (Exception e) {
            try {
                cursor.close();
            } catch (Exception e2) {
            }
            throw new HotdogedException("Error fetching group info: " + e.getMessage());
        }
    }

    public synchronized String getName() {
        return this.name;
    }

    public synchronized void setName(String name) {
        this.name = name;
    }

    public synchronized String getDescription() {
        return this.description;
    }

    public synchronized void setDescription(String description) {
        this.description = description;
    }

    public synchronized String getCodepage() {
        return this.codepage;
    }

    public synchronized void setCodepage(String codepage) {
        this.codepage = codepage;
    }

    public synchronized int getFilter_id() {
        return this.filter_id;
    }

    public synchronized void setFilter_id(int filter_id) {
        this.filter_id = filter_id;
    }

    public synchronized int getKeep_msg_amount_per_group() {
        return this.keep_msg_amount_per_group;
    }

    public synchronized void setKeep_msg_amount_per_group(int keep_msg_amount_per_group) {
        this.keep_msg_amount_per_group = keep_msg_amount_per_group;
    }

    public synchronized int getKeep_msg_days_per_group() {
        return this.keep_msg_days_per_group;
    }

    public synchronized void setKeep_msg_days_per_group(int keep_msg_days_per_group) {
        this.keep_msg_days_per_group = keep_msg_days_per_group;
    }

    public synchronized Uri getGroupUri() {
        return this.groupUri;
    }

    public synchronized int getServer_id() {
        return this.server_id;
    }

    public synchronized int getGrouptype_id() {
        return this.grouptype_id;
    }

    public synchronized int get_id() {
        return this._id;
    }

    public Uri getServerUri() {
        return Uri.parse("content://com.pushkin.hotdoged.provider/" + this.groupUri.getPathSegments().get(0) + "/servers/" + getServer_id());
    }

    public synchronized int getNew_msgs() {
        return this.new_msgs;
    }

    public synchronized void setNew_msgs(int new_msgs) {
        this.new_msgs = new_msgs;
    }

    public synchronized String getUser_name() {
        return this.user_name;
    }

    public synchronized void setUser_name(String user_name) {
        this.user_name = user_name;
    }

    public synchronized String getUser_address() {
        return this.user_address;
    }

    public synchronized void setUser_address(String user_address) {
        this.user_address = user_address;
    }

    public synchronized String getServer_quoting() {
        return this.server_quoting;
    }

    public synchronized void setServer_quoting(String server_quoting) {
        this.server_quoting = server_quoting;
    }

    public synchronized int getLast_read() {
        return this.last_read;
    }

    public synchronized void setLast_read(int last_read) {
        this.last_read = last_read;
    }

    public synchronized String getSignature() {
        return this.signature;
    }

    public synchronized void setSignature(String signature) {
        this.signature = signature;
    }

    public synchronized int getLast_downloaded() {
        return this.last_downloaded;
    }

    public synchronized void setLast_downloaded(int last_downloaded) {
        this.last_downloaded = last_downloaded;
    }

    public synchronized boolean isPurge_read() {
        return this.purge_read;
    }

    public synchronized void setPurge_read(boolean purge_read) {
        this.purge_read = purge_read;
    }

    public synchronized int getTemplate_id() {
        return this.template_id;
    }

    public synchronized void setTemplate_id(int template_id) {
        this.template_id = template_id;
    }

    public synchronized String getCustom_headers() {
        return this.custom_headers;
    }

    public synchronized void setCustom_headers(String custom_headers) {
        this.custom_headers = custom_headers;
    }

    public synchronized int getUnread() {
        return this.unread;
    }

    public synchronized void setUnread(int unread) {
        this.unread = unread;
    }

    public synchronized long getPurged() {
        return this.purged;
    }

    public synchronized void setPurged(long purged) {
        this.purged = purged;
    }

    public static void clearCache() {
        groupsCache.clear();
        Log.d("GroupEntry", "Cache cleared");
    }

    public boolean isInclude_special() {
        return this.include_special;
    }

    public void setInclude_special(boolean include_special) {
        this.include_special = include_special;
    }

    public boolean isInvisible() {
        return this.invisible;
    }

    public void setInvisible(boolean invisible) {
        this.invisible = invisible;
    }
}
