package com.pushkin.hotdoged.export;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import com.sun.mail.imap.IMAPStore;
import java.util.Iterator;

public abstract class ABAbstractUpdater {
    private static final String TAG = "ABAbstractUpdater";
    protected String[] categories;
    private Context context;

    protected abstract Iterable<ABItem> getItems() throws HotdogedException;

    public Context getContext() {
        return this.context;
    }

    public static class ABItem {
        private static final long UNDEFINED = -1;
        public String add_info_01;
        public String address;
        public long category_id = -1;
        public long id;
        public String name;

        public String toString() {
            return "{" + this.name + ", " + this.address + "}: " + this.add_info_01;
        }
    }

    public ABAbstractUpdater(Context context) throws HotdogedException {
        this.context = context;
        this.categories = Utils.getCategoryNames(context);
    }

    public static boolean isEmpty(Context context) throws HotdogedException {
        Cursor cursor = null;
        try {
            try {
                Uri addresses = Uri.parse("content://com.pushkin.hotdoged.provider/addresses");
                cursor = context.getContentResolver().query(addresses, new String[]{IMAPStore.ID_NAME}, null, null, null);
                return !cursor.moveToFirst();
            } catch (Exception e) {
                e.printStackTrace(System.err);
                throw new HotdogedException("Error checking address book: " + e.getMessage());
            }
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
    }

    public void clearAddressBook() throws HotdogedException {
        for (String categoryName : this.categories) {
            Uri addresses = Uri.parse("content://com.pushkin.hotdoged.provider/" + categoryName + "/addresses");
            try {
                this.context.getContentResolver().delete(addresses, null, null);
            } catch (Exception e) {
                throw new HotdogedException(e);
            }
        }
    }

    public void updateAddressBook() throws HotdogedException {
        Iterator<ABItem> items = getItems().iterator();
        while (items.hasNext()) {
            addOrUpdateABItem(items.next());
        }
    }

    public void addOrUpdateABItem(ABItem item) throws HotdogedException {
        ABItem oldItem = findDBItem(item.name, item.address);
        if (oldItem != null) {
            Log.d(TAG, "Found item: " + item.toString());
            if ((oldItem.add_info_01 != null && !oldItem.add_info_01.equalsIgnoreCase(item.add_info_01)) || (oldItem.add_info_01 == null && item.add_info_01 != null)) {
                updateAddInfo01(oldItem.id, item.add_info_01);
                return;
            }
            return;
        }
        insertItem(item);
    }

    private void insertItem(ABItem item) throws HotdogedException {
        if (item == null) {
            throw new HotdogedException("Error inserting address book item: it is null");
        }
        if (item.category_id == -1) {
            throw new HotdogedException("Error inserting address book item: category is not defined for item " + item.toString());
        }
        Uri addresses = Uri.parse("content://com.pushkin.hotdoged.provider/addresses/");
        ContentValues cv = new ContentValues();
        cv.put("category_id", Long.valueOf(item.category_id));
        cv.put(IMAPStore.ID_ADDRESS, item.address);
        cv.put(IMAPStore.ID_NAME, item.name);
        cv.put(Constants.INTENT_EXTRA_ADD_INFO_01, item.add_info_01);
        try {
            this.context.getContentResolver().insert(addresses, cv);
            Log.d(TAG, "Inserted address book item: " + item.toString());
        } catch (Exception e) {
            throw new HotdogedException("Error inserting address book item: " + e.getMessage());
        }
    }

    private void updateAddInfo01(long id, String add_info_01) throws HotdogedException {
        Uri address = Uri.parse("content://com.pushkin.hotdoged.provider/addresses/" + id);
        ContentValues cv = new ContentValues();
        cv.put(Constants.INTENT_EXTRA_ADD_INFO_01, add_info_01);
        try {
            int count = this.context.getContentResolver().update(address, cv, null, null);
            Log.d(TAG, "Updated " + count + " item(s)");
        } catch (Exception e) {
            throw new HotdogedException("Error updating add_info_01 for address book item " + id + ": " + e.getMessage());
        }
    }

    public ABItem findDBItem(String name, String address) throws HotdogedException {
        Cursor cursor = null;
        try {
            try {
                Uri addresses = Uri.parse("content://com.pushkin.hotdoged.provider/addresses");
                cursor = this.context.getContentResolver().query(addresses, null, "address = ? and name = ?", new String[]{address, name}, null);
                if (cursor.moveToFirst()) {
                    ABItem rc = extractItemFromCursor(cursor);
                    return rc;
                }
                if (cursor != null && !cursor.isClosed()) {
                    cursor.close();
                }
                return null;
            } catch (Exception e) {
                e.printStackTrace(System.err);
                throw new HotdogedException("Error getting address book item(s): " + e.getMessage());
            }
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
    }

    public static ABItem findDBItem(Context context, long id) throws HotdogedException {
        Cursor cursor = null;
        try {
            try {
                Uri addresses = Uri.parse("content://com.pushkin.hotdoged.provider/addresses");
                cursor = context.getContentResolver().query(addresses, null, "_id = " + id, null, null);
                if (cursor.moveToFirst()) {
                    ABItem rc = extractItemFromCursor(cursor);
                    return rc;
                }
                if (cursor != null && !cursor.isClosed()) {
                    cursor.close();
                }
                return null;
            } catch (Exception e) {
                e.printStackTrace(System.err);
                throw new HotdogedException("Error getting address book item(s): " + e.getMessage());
            }
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
    }

    private static ABItem extractItemFromCursor(Cursor cursor) {
        ABItem rc = new ABItem();
        rc.id = cursor.getLong(cursor.getColumnIndex(Constants.INTENT_EXTRA_DBID));
        rc.category_id = cursor.getLong(cursor.getColumnIndex("category_id"));
        rc.address = cursor.getString(cursor.getColumnIndex(IMAPStore.ID_ADDRESS));
        rc.name = cursor.getString(cursor.getColumnIndex(IMAPStore.ID_NAME));
        rc.add_info_01 = cursor.getString(cursor.getColumnIndex(Constants.INTENT_EXTRA_ADD_INFO_01));
        return rc;
    }
}
