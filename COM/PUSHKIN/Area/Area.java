package com.pushkin.area;

import android.text.TextUtils;
import com.pushkin.hotdoged.export.HotdogedException;
import java.text.DateFormat;
import java.util.Calendar;

public class Area {
    public static final int SS_NO = 0;
    public static final int SS_TBS = 2;
    public static final int SS_TBU = 3;
    public static final int SS_YES = 1;
    private String description;
    private int itemCount;
    private long lastMessageTimestamp;
    private String name;
    private int subscription = 0;
    private long dbId = -1;

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

    public int getItemCount() {
        return this.itemCount;
    }

    public void setItemCount(int itemCount) {
        this.itemCount = itemCount;
    }

    public long getLastMessageTimestamp() {
        return this.lastMessageTimestamp;
    }

    public void setLastMessageTimestamp(long lastMessageTimestamp) {
        this.lastMessageTimestamp = lastMessageTimestamp;
    }

    public Area(String name, long lastMessageTimestamp, int itemCount, String description) {
        this.name = name;
        this.lastMessageTimestamp = lastMessageTimestamp;
        this.itemCount = itemCount;
        this.description = description;
    }

    public Area() {
    }

    public String formatTimestamp() throws HotdogedException {
        return formatTimestamp(getLastMessageTimestamp());
    }

    public static String formatTimestamp(long timestamp) throws HotdogedException {
        DateFormat df = DateFormat.getDateTimeInstance(3, 3);
        try {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(1000 * timestamp);
            String result = df.format(cal.getTime());
            return result;
        } catch (Exception e) {
            throw new HotdogedException("Error formatting date: " + timestamp + ": " + e.getMessage());
        }
    }

    public int getSubscription() {
        return this.subscription;
    }

    public void setSubscription(int subscription) {
        this.subscription = subscription;
    }

    public String toString() {
        return "Area [name=" + this.name + ", lastMessageTimestamp=" + this.lastMessageTimestamp + ", itemCount=" + this.itemCount + ", description=" + this.description + ", subscription=" + this.subscription + "]";
    }

    public static void mergeArea(Area areaDst, Area areaSrc) throws HotdogedException {
        if (areaDst == null || areaSrc == null) {
            throw new HotdogedException("Either of merged areas can not be null");
        }
        if (areaDst.getItemCount() <= 0) {
            areaDst.setItemCount(areaSrc.getItemCount());
        }
        if (areaDst.getLastMessageTimestamp() <= 0) {
            areaDst.setLastMessageTimestamp(areaSrc.getLastMessageTimestamp());
        }
        if (TextUtils.isEmpty(areaDst.getDescription())) {
            areaDst.setDescription(areaSrc.getDescription());
        }
    }

    public long getDbId() {
        return this.dbId;
    }

    public void setDbId(long dbId) {
        this.dbId = dbId;
    }
}
