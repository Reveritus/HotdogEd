package com.pushkin.ftn;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import com.pushkin.ftn.Main;
import com.pushkin.hotdoged.export.Constants;
import com.pushkin.hotdoged.export.HotdogedException;
import com.sun.mail.imap.IMAPStore;

public class Echoarea {
    private static final String TAG = "Echoarea";
    private String description;
    private Long id;
    private String name;
    private Long readlevel;
    private Uri uri;
    private Long writelevel;

    public Echoarea(String name) throws HotdogedException {
        boolean isClosed;
        Main.SystemInfo.getLogger().log(TAG, "Requested echo with name " + name);
        this.name = name;
        Cursor cursor = null;
        try {
            try {
                Uri groupsUri = Uri.parse("content://com.pushkin.hotdoged.provider/ftn/servers/" + Main.info.getLinkId() + "/groups");
                cursor = Main.SystemInfo.getContext().getContentResolver().query(groupsUri, null, "name = ?", new String[]{name}, null);
                if (cursor.moveToFirst()) {
                    this.id = Long.valueOf(cursor.getLong(cursor.getColumnIndex(Constants.INTENT_EXTRA_DBID)));
                    this.uri = Uri.withAppendedPath(groupsUri, String.valueOf(this.id));
                    cursor.getString(cursor.getColumnIndex(IMAPStore.ID_NAME));
                    this.description = cursor.getString(cursor.getColumnIndex(Constants.INTENT_EXTRA_DESCRIPTION));
                    if (cursor != null) {
                        if (!isClosed) {
                            return;
                        }
                        return;
                    }
                    return;
                }
                throw new HotdogedException("Group not found: " + name);
            } catch (Exception e) {
                throw new HotdogedException("Error retrieving info for group " + name + ": " + e.getMessage());
            }
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
    }

    public Echoarea() {
        Main.SystemInfo.getLogger().log(TAG, "Requested empty echoarea");
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getWritelevel() {
        return this.writelevel;
    }

    public void setWritelevel(Long writelevel) {
        this.writelevel = writelevel;
    }

    public Long getReadlevel() {
        return this.readlevel;
    }

    public void setReadlevel(Long readlevel) {
        this.readlevel = readlevel;
    }

    public int hashCode() {
        int result = (this.id == null ? 0 : this.id.hashCode()) + 31;
        return (result * 31) + (this.name != null ? this.name.hashCode() : 0);
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj != null && getClass() == obj.getClass()) {
            Echoarea other = (Echoarea) obj;
            if (this.id == null) {
                if (other.id != null) {
                    return false;
                }
            } else if (!this.id.equals(other.id)) {
                return false;
            }
            return this.name == null ? other.name == null : this.name.equals(other.name);
        }
        return false;
    }

    public void create() throws HotdogedException {
        ContentValues cv = new ContentValues();
        cv.put(Constants.INTENT_EXTRA_DBID, this.id);
        cv.put(IMAPStore.ID_NAME, this.name);
        cv.put(Constants.INTENT_EXTRA_DESCRIPTION, this.description);
        cv.put("grouptype_id", (Integer) 20);
        cv.put(Constants.INTENT_EXTRA_PURGEREAD, (Integer) 0);
        try {
            Uri groupsUri = Uri.parse("content://com.pushkin.hotdoged.provider/ftn/servers/" + Main.info.getLinkId() + "/groups");
            this.uri = Main.SystemInfo.getContext().getContentResolver().insert(groupsUri, cv);
            this.id = Long.valueOf(this.uri.getLastPathSegment(), 10);
        } catch (Exception e) {
            throw new HotdogedException("Error creating echoarea " + this.name + ": " + e.getMessage());
        }
    }

    public synchronized Uri getUri() {
        return this.uri;
    }
}
