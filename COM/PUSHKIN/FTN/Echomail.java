package com.pushkin.ftn;

import android.content.ContentValues;
import android.net.Uri;
import com.pushkin.ftn.Main;
import com.pushkin.hotdoged.export.HotdogedException;
import com.pushkin.hotdoged.export.Utils;
import com.sun.mail.imap.IMAPStore;
import java.util.Date;

public class Echomail {
    private static final String TAG = "Echomail";
    private Echoarea area;
    private String codePage;
    private Date date;
    private String fromFTN;
    private String fromName;
    private Long id = new Long(-1);
    private String msgId;
    private String path;
    private String seenBy;
    private String subject;
    private String text;
    private String toName;

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Echoarea getArea() {
        return this.area;
    }

    public void setArea(Echoarea area) {
        this.area = area;
    }

    public String getFromName() {
        return this.fromName;
    }

    public void setFromName(String fromName) {
        this.fromName = fromName;
    }

    public String getToName() {
        return this.toName;
    }

    public void setToName(String toName) {
        this.toName = toName;
    }

    public String getFromFTN() {
        return this.fromFTN;
    }

    public void setFromFTN(String fromFTN) {
        this.fromFTN = fromFTN;
    }

    public String getSubject() {
        return this.subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getText() {
        return this.text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getSeenBy() {
        return this.seenBy;
    }

    public void setSeenBy(String seenBy) {
        this.seenBy = seenBy;
    }

    public String getPath() {
        return this.path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Date getDate() {
        return this.date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public void save() throws HotdogedException {
        ContentValues cv = new ContentValues();
        try {
            String msgId = Utils.extractFTNHeader(this.text, "MSGID");
            if (msgId == null) {
                throw new HotdogedException("Not saving echomail #" + getId() + " due to empty MSGID");
            }
            cv.put("Message_ID", msgId);
            String replyTo = Utils.extractFTNHeader(this.text, "REPLY");
            if (replyTo != null) {
                cv.put("reply_to", replyTo);
            }
            cv.put("from_name", this.fromName);
            cv.put("from_addr", this.fromFTN);
            cv.put("to_name", this.toName);
            cv.put("subject", this.subject);
            cv.put(IMAPStore.ID_DATE, Long.valueOf(this.date.getTime() / 1000));
            cv.put("article", this.text);
            cv.put("read", (Integer) 0);
            long groupId = this.area.getId().longValue();
            Uri itemsUri = Uri.parse("content://com.pushkin.hotdoged.provider/ftn/servers/" + Main.info.getLinkId() + "/groups/" + groupId + "/items");
            Main.SystemInfo.getContext().getContentResolver().insert(itemsUri, cv);
            Main.SystemInfo.getLogger().log(TAG, "Echomail '" + getSubject() + "' saved");
        } catch (Exception e) {
            throw new HotdogedException("Error saving echomail: " + e.getMessage());
        }
    }

    public void moveFromOutbound() throws HotdogedException {
        try {
            Uri itemUri = Uri.parse("content://com.pushkin.hotdoged.provider/ftn/items/" + getId());
            Uri serverUri = Uri.parse("content://com.pushkin.hotdoged.provider/ftn/servers/" + Main.info.getLinkId());
            int groupId = Utils.getGroupIdByName(Main.SystemInfo.getContext(), serverUri, getArea().getName());
            ContentValues cv = new ContentValues();
            cv.put("group_id", Integer.valueOf(groupId));
            cv.put("Message_ID", getMsgId());
            cv.put("read", (Integer) 1);
            int cnt = Main.SystemInfo.getContext().getContentResolver().update(itemUri, cv, null, null);
            if (cnt > 0) {
                Main.SystemInfo.getLogger().log(TAG, "Echomail " + itemUri.toString() + " moved to group " + getArea().getName());
                Link.moveMessageToSent(serverUri, itemUri);
                return;
            }
            throw new HotdogedException("Echomail " + itemUri.toString() + " NOT moved to group " + getArea().getName());
        } catch (Exception e) {
            throw new HotdogedException(e.getMessage());
        }
    }

    public String getMsgId() {
        return this.msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }

    public String getCodePage() {
        return this.codePage;
    }

    public void setCodePage(String codePage) {
        this.codePage = codePage;
    }
}
