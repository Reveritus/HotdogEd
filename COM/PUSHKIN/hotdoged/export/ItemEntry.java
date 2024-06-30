package com.pushkin.hotdoged.export;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import com.sun.mail.imap.IMAPStore;

public class ItemEntry {
    private static final String TAG = "ItemEntry";
    private long _id;
    private String add_info_01;
    private String article_id;
    private final String category_name;
    private final Context context;
    private long date;
    private String from_addr;
    private String from_name;
    private int group_id;
    private String in_reply_to;
    private final Uri itemUri;
    private String message_id;
    private boolean read;
    private String ref;
    private String reply_to;
    private boolean starred;
    private String subject;
    private String to_addr;
    private String to_name;
    private long tree;

    public ItemEntry(Context context, Cursor cursor, Uri itemUri) throws HotdogedException {
        this.group_id = -1;
        this.context = context;
        this.itemUri = itemUri;
        if (itemUri == null) {
            throw new HotdogedException("Bad URI: " + itemUri);
        }
        if (cursor == null || cursor.isClosed()) {
            throw new HotdogedException("Bad cursor state");
        }
        this.category_name = itemUri.getPathSegments().get(0);
        this._id = cursor.getLong(cursor.getColumnIndex(Constants.INTENT_EXTRA_DBID));
        this.group_id = cursor.getInt(cursor.getColumnIndex("group_id"));
        fillFieldsFromCursor(cursor);
    }

    public ItemEntry(Context context, Uri itemUri) throws HotdogedException {
        this.group_id = -1;
        this.context = context;
        this.itemUri = itemUri;
        if (itemUri == null) {
            throw new HotdogedException("Bad URI: " + itemUri);
        }
        this.category_name = itemUri.getPathSegments().get(0);
        this._id = Integer.parseInt(itemUri.getLastPathSegment(), 10);
        Cursor cursor = null;
        try {
            Cursor cursor2 = this.context.getContentResolver().query(itemUri, null, null, null, null);
            if (!cursor2.moveToFirst()) {
                if (!cursor2.isClosed()) {
                    cursor2.close();
                }
                Uri newItemUri = itemUri2StraightId(context, itemUri);
                Log.d(TAG, "Item URI straight " + itemUri + " -> " + newItemUri);
                cursor2 = this.context.getContentResolver().query(newItemUri, null, null, null, null);
                if (!cursor2.moveToFirst()) {
                    throw new HotdogedException("Item with ID " + itemUri.getLastPathSegment() + " not found");
                }
                this.group_id = cursor2.getInt(cursor2.getColumnIndex("group_id"));
                Log.d(TAG, "Item URI normalized " + newItemUri + " -> " + straighId2itemUri(context, newItemUri, this.group_id));
            }
            if (this.group_id < 0) {
                this.group_id = cursor2.getInt(cursor2.getColumnIndex("group_id"));
            }
            fillFieldsFromCursor(cursor2);
            cursor2.close();
        } catch (Exception e) {
            try {
                cursor.close();
            } catch (Exception e2) {
            }
            throw new HotdogedException("Error fetching item info: " + e.getMessage());
        }
    }

    private void fillFieldsFromCursor(Cursor cursor) {
        this.from_name = cursor.getString(cursor.getColumnIndex("from_name"));
        this.to_name = cursor.getString(cursor.getColumnIndex("to_name"));
        this.subject = cursor.getString(cursor.getColumnIndex("subject"));
        this.date = cursor.getLong(cursor.getColumnIndex(IMAPStore.ID_DATE));
        this.message_id = cursor.getString(cursor.getColumnIndex("Message_ID"));
        if (this.category_name.equalsIgnoreCase("NNTP")) {
            this.in_reply_to = cursor.getString(cursor.getColumnIndex("IN_REPLY_TO"));
            this.ref = cursor.getString(cursor.getColumnIndex("REF"));
            this.article_id = cursor.getString(cursor.getColumnIndex("article_id"));
        } else if (this.category_name.equalsIgnoreCase("FTN")) {
            this.from_addr = cursor.getString(cursor.getColumnIndex("from_addr"));
            this.to_addr = cursor.getString(cursor.getColumnIndex("to_addr"));
            this.reply_to = cursor.getString(cursor.getColumnIndex("reply_to"));
        }
        this.read = cursor.getInt(cursor.getColumnIndex("read")) == 1;
        this.starred = cursor.getInt(cursor.getColumnIndex("starred")) == 1;
        this.add_info_01 = cursor.getString(cursor.getColumnIndex(Constants.INTENT_EXTRA_ADD_INFO_01));
        this.tree = cursor.getLong(cursor.getColumnIndex("tree"));
    }

    public static Uri straighId2itemUri(Context context, Uri itemUri, int groupId) throws HotdogedException {
        Uri groupUri = Uri.parse("content://com.pushkin.hotdoged.provider/groups/" + groupId);
        Cursor cursor = null;
        try {
            try {
                cursor = context.getContentResolver().query(groupUri, new String[]{"server_id"}, null, null, null);
                if (!cursor.moveToFirst()) {
                    throw new HotdogedException("Group not found: " + groupUri.toString());
                }
                int serverId = cursor.getInt(0);
                String categoryName = itemUri.getPathSegments().get(0);
                Uri uri = Uri.parse("content://com.pushkin.hotdoged.provider/" + categoryName + "/servers/" + serverId + "/groups/" + groupId + "/items/" + itemUri.getLastPathSegment());
                return uri;
            } catch (Exception e) {
                throw new HotdogedException(e);
            }
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
    }

    public static Uri itemUri2StraightId(Context context, Uri itemUri) {
        String categoryName = itemUri.getPathSegments().get(0);
        String itemId = itemUri.getLastPathSegment();
        Uri absoluteMessageUri = Uri.parse("content://com.pushkin.hotdoged.provider/" + categoryName + "/items/" + itemId);
        return absoluteMessageUri;
    }

    public synchronized String getArticle_id() {
        return this.article_id;
    }

    public synchronized void setArticle_id(String article_id) {
        this.article_id = article_id;
    }

    public synchronized String getFrom_name() {
        return this.from_name;
    }

    public synchronized void setFrom_name(String from_name) {
        this.from_name = from_name;
    }

    public synchronized String getSubject() {
        return this.subject;
    }

    public synchronized void setSubject(String subject) {
        this.subject = subject;
    }

    public synchronized long getDate() {
        return this.date;
    }

    public synchronized void setDate(long date) {
        this.date = date;
    }

    public synchronized String getRef() {
        return this.ref;
    }

    public synchronized void setRef(String ref) {
        this.ref = ref;
    }

    public synchronized boolean isRead() {
        return this.read;
    }

    public synchronized void setRead(boolean read) {
        this.read = read;
    }

    public synchronized boolean isStarred() {
        return this.starred;
    }

    public synchronized void setStarred(boolean starred) {
        this.starred = starred;
    }

    public synchronized Uri getItemUri() {
        return this.itemUri;
    }

    public synchronized String getCategory_name() {
        return this.category_name;
    }

    public synchronized long get_id() {
        return this._id;
    }

    public synchronized int getGroup_id() {
        return this.group_id;
    }

    public synchronized String getTo_name() {
        return this.to_name;
    }

    public synchronized void setTo_name(String to_name) {
        this.to_name = to_name;
    }

    public synchronized String getMessage_id() {
        return this.message_id;
    }

    public synchronized void setMessage_id(String message_id) {
        this.message_id = message_id;
    }

    public synchronized String getIn_reply_to() {
        return this.in_reply_to;
    }

    public synchronized void setIn_reply_to(String in_reply_to) {
        this.in_reply_to = in_reply_to;
    }

    public static void clearCache() {
    }

    public synchronized String getFrom_addr() {
        return this.from_addr;
    }

    public synchronized void setFrom_addr(String from_addr) {
        this.from_addr = from_addr;
    }

    public synchronized String getTo_addr() {
        return this.to_addr;
    }

    public synchronized void setTo_addr(String to_addr) {
        this.to_addr = to_addr;
    }

    public synchronized String getAdd_info_01() {
        return this.add_info_01;
    }

    public synchronized void setAdd_info_01(String add_info_01) {
        this.add_info_01 = add_info_01;
    }

    public synchronized String getReply_to() {
        return this.reply_to;
    }

    public synchronized void setReply_to(String reply_to) {
        this.reply_to = reply_to;
    }

    public long getTree() {
        return this.tree;
    }

    public void setTree(long tree) {
        this.tree = tree;
    }
}
