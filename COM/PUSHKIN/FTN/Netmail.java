package com.pushkin.ftn;

import android.content.ContentValues;
import android.net.Uri;
import com.pushkin.ftn.Main;
import com.pushkin.hotdoged.export.Constants;
import com.pushkin.hotdoged.export.HotdogedException;
import com.pushkin.hotdoged.export.Utils;
import com.sun.mail.imap.IMAPStore;
import java.util.Date;

public class Netmail {
    private static final String TAG = "Netmail";
    private int attr;
    private String codePage;
    private Date date;
    private String fromFTN;
    private String fromName;
    private Long id;
    private String msgId;
    private Link routeVia;
    private boolean send;
    private String subject;
    private String text;
    private String toFTN;
    private String toName;

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getToFTN() {
        return this.toFTN;
    }

    public void setToFTN(String toFTN) {
        this.toFTN = toFTN;
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

    public Date getDate() {
        return this.date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Link getRouteVia() {
        return this.routeVia;
    }

    public void setRouteVia(Link routeVia) {
        this.routeVia = routeVia;
    }

    public boolean isSend() {
        return this.send;
    }

    public void setSend(boolean send) {
        this.send = send;
    }

    public int getAttr() {
        return this.attr;
    }

    public void setAttr(int attr) {
        this.attr = attr;
    }

    public String toString() {
        return "Netmail [id=" + this.id + ", fromName=" + this.fromName + ", toName=" + this.toName + ", fromFTN=" + this.fromFTN + ", toFTN=" + this.toFTN + ", subject=" + this.subject + ", text=" + this.text + ", date=" + this.date + ", routeVia=" + this.routeVia + "]";
    }

    public void save() throws HotdogedException {
        save(1);
    }

    public void save(int groupType) throws HotdogedException {
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
            cv.put("to_addr", this.toFTN);
            cv.put("subject", this.subject);
            cv.put(IMAPStore.ID_DATE, Long.valueOf(this.date.getTime() / 1000));
            cv.put("article", this.text);
            cv.put("read", (Integer) 0);
            cv.put(Constants.INTENT_EXTRA_ADD_INFO_01, "NETMAIL");
            Uri serverUri = Uri.parse("content://com.pushkin.hotdoged.provider/ftn/servers/" + Main.info.getLinkId());
            long groupId = Utils.getSpecialGroupIdForServer(Main.SystemInfo.getContext(), groupType, serverUri);
            Uri itemsUri = Uri.withAppendedPath(serverUri, "groups/" + groupId + "/items");
            Main.SystemInfo.getContext().getContentResolver().insert(itemsUri, cv);
            Main.SystemInfo.getLogger().log(TAG, "Netmail " + msgId + " saved");
        } catch (Exception e) {
            throw new HotdogedException("Error saving netmail: " + e.getMessage());
        }
    }

    public void moveFromOutbound() throws HotdogedException {
        try {
            Uri itemUri = Uri.parse("content://com.pushkin.hotdoged.provider/ftn/items/" + getId());
            Uri serverUri = Uri.parse("content://com.pushkin.hotdoged.provider/ftn/servers/" + Main.info.getLinkId());
            int groupId = Utils.getSpecialGroupIdForServer(Main.SystemInfo.getContext(), 1, serverUri);
            ContentValues cv = new ContentValues();
            cv.put("group_id", Integer.valueOf(groupId));
            cv.put("Message_ID", getMsgId());
            cv.put("read", (Integer) 1);
            int cnt = Main.SystemInfo.getContext().getContentResolver().update(itemUri, cv, null, null);
            if (cnt > 0) {
                Main.SystemInfo.getLogger().log(TAG, "Netmail " + itemUri.toString() + " moved from outbound");
                Link.moveMessageToSent(serverUri, itemUri);
                return;
            }
            throw new HotdogedException("Netmail " + itemUri.toString() + " NOT moved to outbound");
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
