package com.pushkin.ftn;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import com.pushkin.ftn.Main;
import com.pushkin.hotdoged.export.Constants;
import com.pushkin.hotdoged.export.HotdogedException;
import com.pushkin.hotdoged.export.ServerEntry;
import com.pushkin.hotdoged.export.Utils;
import com.pushkin.hotdoged.fido.ConfigureServerActivity;
import jnode.ftn.types.FtnAddress;

public class Link {
    private static final String BOSS_NODE = "Boss node";
    private static final int DEFAULT_PORT = 24554;
    private static final String TAG = "Link";
    private Long id;
    private String linkAddress;
    private String linkName;
    private Uri linkUri;
    private boolean packEchomail;
    private String paketPassword;
    private String protocolHost;
    private String protocolPassword;
    private Integer protocolPort;
    private ServerEntry serverEntry;

    public Link(FtnAddress ftnAddress) throws HotdogedException {
        boolean isClosed;
        this.packEchomail = true;
        Main.SystemInfo.getLogger().log(TAG, "Link requested with address '" + ftnAddress + "'");
        this.linkUri = Uri.parse("content://com.pushkin.hotdoged.provider/ftn/servers");
        Cursor cursor = null;
        try {
            try {
                cursor = Main.SystemInfo.getContext().getContentResolver().query(this.linkUri, null, "server_name = ?", new String[]{ftnAddress.toString()}, null);
                if (cursor.moveToFirst()) {
                    this.serverEntry = new ServerEntry(Main.SystemInfo.getContext(), Uri.withAppendedPath(this.linkUri, cursor.getString(cursor.getColumnIndex(Constants.INTENT_EXTRA_DBID))));
                    fillDataFromCursor(cursor);
                    if (cursor != null) {
                        if (!isClosed) {
                            return;
                        }
                        return;
                    }
                    return;
                }
                throw new HotdogedException("No link with address " + ftnAddress);
            } catch (Exception e) {
                throw new HotdogedException("Error retrieving link with address " + ftnAddress + ": " + e.getMessage());
            }
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
    }

    public Link(Long id) throws HotdogedException {
        boolean isClosed;
        this.packEchomail = true;
        Main.SystemInfo.getLogger().log(TAG, "Link requested with id " + id);
        this.linkUri = Uri.parse("content://com.pushkin.hotdoged.provider/ftn/servers/" + id);
        Cursor cursor = null;
        try {
            try {
                this.serverEntry = new ServerEntry(Main.SystemInfo.getContext(), this.linkUri);
                cursor = Main.SystemInfo.getContext().getContentResolver().query(this.linkUri, null, null, null, null);
                if (cursor.moveToFirst()) {
                    fillDataFromCursor(cursor);
                    if (cursor != null) {
                        if (!isClosed) {
                            return;
                        }
                        return;
                    }
                    return;
                }
                throw new HotdogedException("No link with id " + id);
            } catch (Exception e) {
                throw new HotdogedException("Error retrieving link with id " + id + ": " + e.getMessage());
            }
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
    }

    private void fillDataFromCursor(Cursor cursor) {
        this.linkAddress = cursor.getString(cursor.getColumnIndex(Constants.INTENT_EXTRA_SERVERNAME));
        this.linkName = BOSS_NODE;
        String ip = cursor.getString(cursor.getColumnIndex(Constants.INTENT_EXTRA_SERVERIP));
        this.protocolHost = getHostFromAddr(ip);
        this.protocolPort = Integer.valueOf(getPortFromAddr(ip));
        this.packEchomail = cursor.getInt(cursor.getColumnIndex(Constants.INTENT_EXTRA_ADD_INT_01)) != 1;
        Log.d(TAG, "Pack echomail for link " + this.linkAddress + ": " + this.packEchomail);
        this.id = Long.valueOf(cursor.getLong(cursor.getColumnIndex(Constants.INTENT_EXTRA_DBID)));
        this.paketPassword = ConfigureServerActivity.getLocalServerPassword(Main.SystemInfo.getContext(), String.valueOf(this.id));
        this.protocolPassword = this.paketPassword;
    }

    private String getHostFromAddr(String address) {
        if (address == null) {
            return null;
        }
        return address.replaceFirst("^(.*?)(:\\d+)?$", "$1"); 
/*
		return address.replaceFirst("^(.*?)(:\\w+)?$", "$1");
		*/
		}

    private int getPortFromAddr(String address) {
        if (address == null) {
            return -1;
        }
        try {
        	
            String port = address.replaceFirst("^(.*?)(:(\\d+))?$", "$3");
/*
			String port = address.replaceFirst("^(.*?)(:({2}\\d+))?$", "$3");
*/            
			return (port == null || port.length() == 0) ? DEFAULT_PORT : Integer.valueOf(port, 10).intValue();
        } catch (Exception e) {
            return DEFAULT_PORT;
        }
    }

    public Link(String sFtn) throws HotdogedException {
        this(new FtnAddress(sFtn));
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLinkName() {
        return this.linkName;
    }

    public void setLinkName(String linkName) {
        this.linkName = linkName;
    }

    public String getLinkAddress() {
        return this.linkAddress;
    }

    public void setLinkAddress(String linkAddress) {
        this.linkAddress = linkAddress;
    }

    public String getPaketPassword() {
        return this.paketPassword;
    }

    public void setPaketPassword(String paketPassword) {
        this.paketPassword = paketPassword;
    }

    public String getProtocolPassword() {
        return this.protocolPassword;
    }

    public void setProtocolPassword(String protocolPassword) {
        this.protocolPassword = protocolPassword;
    }

    public String getProtocolHost() {
        return this.protocolHost;
    }

    public void setProtocolHost(String protocolHost) {
        this.protocolHost = protocolHost;
    }

    public Integer getProtocolPort() {
        return this.protocolPort;
    }

    public void setProtocolPort(Integer protocolPort) {
        this.protocolPort = protocolPort;
    }

    public String toString() {
        return "Link [id=" + this.id + ", linkName=" + this.linkName + ", linkAddress=" + this.linkAddress + ", paketPassword=" + this.paketPassword + ", protocolPassword=" + this.protocolPassword + ", protocolHost=" + this.protocolHost + ", protocolPort=" + this.protocolPort + "]";
    }

    public int hashCode() {
        int result = (this.id == null ? 0 : this.id.hashCode()) + 31;
        return (result * 31) + (this.linkAddress != null ? this.linkAddress.hashCode() : 0);
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj != null && getClass() == obj.getClass()) {
            Link other = (Link) obj;
            if (this.id == null) {
                if (other.id != null) {
                    return false;
                }
            } else if (!this.id.equals(other.id)) {
                return false;
            }
            return this.linkAddress == null ? other.linkAddress == null : this.linkAddress.equals(other.linkAddress);
        }
        return false;
    }

    /* WARN: Code restructure failed: missing block: B:10:0x022d, code lost:
    
        r2 = "";
     */
    /* WARN: Code restructure failed: missing block: B:11:0x022f, code lost:
    
        r18.setText(r4.append(r2).toString());
        r18.setMsgId(r20);
        r21.add(r18);
     */
    /* WARN: Code restructure failed: missing block: B:12:0x024e, code lost:
    
        if (r11.moveToNext() != false) goto L35;
     */
    /* WARN: Code restructure failed: missing block: B:17:0x025b, code lost:
    
        return r21;
     */
    /* WARN: Code restructure failed: missing block: B:18:0x025c, code lost:
    
        r2 = "\n" + r25;
     */
    /* WARN: Code restructure failed: missing block: B:4:0x00ca, code lost:
    
        if (r11.moveToFirst() != false) goto L5;
     */
    /* WARN: Code restructure failed: missing block: B:5:0x00cc, code lost:
    
        r18 = new com.pushkin.ftn.Netmail();
        r18.setCodePage(r10);
        r8 = java.util.Calendar.getInstance();
        r8.setTimeInMillis(r11.getLong(r11.getColumnIndex(com.sun.mail.imap.IMAPStore.ID_DATE)) * 1000);
        r12 = r8.getTime();
        r18.setDate(r12);
        r18.setId(java.lang.Long.valueOf(r11.getLong(r11.getColumnIndex(com.pushkin.hotdoged.export.Constants.INTENT_EXTRA_DBID))));
        r18.setFromName(r11.getString(r11.getColumnIndex("from_name")));
        r18.setFromFTN(r11.getString(r11.getColumnIndex("from_addr")));
        r18.setToName(r11.getString(r11.getColumnIndex("to_name")));
        r18.setToFTN(r11.getString(r11.getColumnIndex("to_addr")));
        r18.setSubject(r11.getString(r11.getColumnIndex("subject")));
        r19 = r11.getString(r11.getColumnIndex("article"));
        r20 = com.pushkin.ftn.Main.info.getAddress().toString() + " " + jnode.ftn.FtnTools.generate8d();
        r14 = java.lang.String.format("\u0001MSGID: %s\n\u0001PID: %s\n\u0001TID: %s\n\u0001CHRS: %s", r20, com.pushkin.ftn.Main.SystemInfo.getPID(), com.pushkin.ftn.Main.info.getVersion(), r9);
        r25 = null;
     */
    /* WARN: Code restructure failed: missing block: B:6:0x01ab, code lost:
    
        if (isNetmailToRobots(r18.getToName()) != false) goto L8;
     */
    /* WARN: Code restructure failed: missing block: B:7:0x01ad, code lost:
    
        r25 = "--- " + com.pushkin.ftn.Main.info.getTearline() + "\n * Origin: " + com.pushkin.ftn.Main.info.getOrigin() + " (" + com.pushkin.ftn.Main.info.getAddress().toString() + ")";
        com.pushkin.ftn.Main.SystemInfo.getLogger().log(com.pushkin.ftn.Link.TAG, "Netmail " + r18.getId() + " is addressed to robots. Don't add origin and teraline.");
     */
    /* WARN: Code restructure failed: missing block: B:8:0x0216, code lost:
    
        r4 = new java.lang.StringBuilder().append(r14).append("\n").append(r19);
     */
    /* WARN: Code restructure failed: missing block: B:9:0x022b, code lost:
    
        if (r25 != null) goto L18;
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public java.util.List<com.pushkin.ftn.Netmail> getUnsentNetmail() {
        /*
            Method dump skipped, instructions count: 698
            To view this dump add '--comments-level debug' option
        */
        throw new UnsupportedOperationException("Method not decompiled: com.pushkin.ftn.Link.getUnsentNetmail():java.util.List");
    }

    private boolean isNetmailToRobots(String toName) {
        if (toName == null) {
            return false;
        }
        return toName.equalsIgnoreCase("areafix") || toName.equalsIgnoreCase("filefix");
    }

    /* WARN: Code restructure failed: missing block: B:10:0x0081, code lost:
    
        if (r12.moveToNext() != false) goto L33;
     */
    /* WARN: Code restructure failed: missing block: B:15:0x008e, code lost:
    
        return r16;
     */
    /* WARN: Code restructure failed: missing block: B:16:0x008f, code lost:
    
        r21 = new com.pushkin.ftn.Echomail();
        r8 = new com.pushkin.ftn.Echoarea();
        r8.setName(r12.getString(r12.getColumnIndex(com.pushkin.hotdoged.export.Constants.INTENT_EXTRA_ADD_INFO_01)));
        r21.setArea(r8);
        r19 = com.pushkin.hotdoged.export.Utils.getGroupIdByName(com.pushkin.ftn.Main.SystemInfo.getContext(), r26, r14);
        r20 = android.net.Uri.withAppendedPath(r26, "groups/" + r19);
        r18 = new com.pushkin.hotdoged.export.GroupEntry(com.pushkin.ftn.Main.SystemInfo.getContext(), r20);
        r11 = com.pushkin.hotdoged.export.Utils.getPreferredCodePage(r28.serverEntry, r18);
        r21.setCodePage(r11);
        r10 = jnode.ftn.FtnTools.codepage2chrs(r11);
        com.pushkin.ftn.Main.SystemInfo.getLogger().log(com.pushkin.ftn.Link.TAG, "Codepage set to " + r11 + ", CHRS: " + r10);
        r9 = java.util.Calendar.getInstance();
        r9.setTimeInMillis(r12.getLong(r12.getColumnIndex(com.sun.mail.imap.IMAPStore.ID_DATE)) * 1000);
        r13 = r9.getTime();
        r21.setDate(r13);
        r21.setId(java.lang.Long.valueOf(r12.getLong(r12.getColumnIndex(com.pushkin.hotdoged.export.Constants.INTENT_EXTRA_DBID))));
        r21.setFromName(r12.getString(r12.getColumnIndex("from_name")));
        r21.setFromFTN(r12.getString(r12.getColumnIndex("from_addr")));
        r21.setToName(r12.getString(r12.getColumnIndex("to_name")));
        r21.setSubject(r12.getString(r12.getColumnIndex("subject")));
        r21.setPath("");
        r21.setSeenBy("");
        r22 = r12.getString(r12.getColumnIndex("article"));
        r23 = com.pushkin.ftn.Main.info.getAddress().toString() + " " + jnode.ftn.FtnTools.generate8d();
        r17 = java.lang.String.format("\u0001MSGID: %s\n\u0001PID: %s\n\u0001TID: %s\n\u0001CHRS: %s", r23, com.pushkin.ftn.Main.SystemInfo.getPID(), com.pushkin.ftn.Main.info.getVersion(), r10);
        r27 = "--- " + com.pushkin.ftn.Main.info.getTearline() + "\n * Origin: " + com.pushkin.ftn.Main.info.getOrigin() + " (" + com.pushkin.ftn.Main.info.getAddress().toString() + ")";
        r21.setText(r17 + "\n" + r22 + "\n" + r27);
        r21.setMsgId(r23);
        r16.add(new com.pushkin.ftn.EchomailAwaiting(r28, r21));
     */
    /* WARN: Code restructure failed: missing block: B:4:0x0067, code lost:
    
        if (r12.moveToFirst() != false) goto L5;
     */
    /* WARN: Code restructure failed: missing block: B:5:0x0069, code lost:
    
        r14 = r12.getString(r12.getColumnIndex(com.pushkin.hotdoged.export.Constants.INTENT_EXTRA_ADD_INFO_01));
     */
    /* WARN: Code restructure failed: missing block: B:6:0x0073, code lost:
    
        if (r14 == null) goto L9;
     */
    /* WARN: Code restructure failed: missing block: B:8:0x007b, code lost:
    
        if (r14.equals("NETMAIL") == false) goto L16;
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public java.util.List<com.pushkin.ftn.EchomailAwaiting> getAwaitingEchomail() {
        /*
            Method dump skipped, instructions count: 685
            To view this dump add '--comments-level debug' option
        */
        throw new UnsupportedOperationException("Method not decompiled: com.pushkin.ftn.Link.getAwaitingEchomail():java.util.List");
    }

    public String getLinkOptionValue(String optionName) {
        if (optionName.equalsIgnoreCase(LinkOption.BOOLEAN_IGNORE_PKTPWD) || optionName.equalsIgnoreCase(LinkOption.BOOLEAN_AUTOCREATE_AREA)) {
            return "TRUE";
        }
        if (optionName.equalsIgnoreCase(LinkOption.BOOLEAN_PACK_NETMAIL)) {
            return "FALSE";
        }
        if (optionName.equalsIgnoreCase(LinkOption.BOOLEAN_PACK_ECHOMAIL)) {
            return this.packEchomail ? "TRUE" : "FALSE";
        }
        return null;
    }

    public static void moveMessageToSent(Uri linkUri, Uri itemUri) throws HotdogedException {
        ContentValues cv = new ContentValues();
        Cursor cursor = Main.SystemInfo.getContext().getContentResolver().query(itemUri, null, null, null, null);
        if (cursor.moveToFirst()) {
            for (int field = 0; field < cursor.getColumnCount(); field++) {
                cv.put(cursor.getColumnNames()[field], cursor.getString(field));
            }
            int sentGroupId = Utils.getSpecialGroupIdForServer(Main.SystemInfo.getContext(), 6, linkUri);
            Uri itemUri2 = Uri.withAppendedPath(linkUri, "groups/" + sentGroupId + "/items");
            cv.remove(Constants.INTENT_EXTRA_DBID);
            cv.remove("group_id");
            cv.put("read", (Integer) 1);
            cursor.close();
            Main.SystemInfo.getContext().getContentResolver().insert(itemUri2, cv);
            Main.SystemInfo.getLogger().log(TAG, "Echomail copied to sent");
            return;
        }
        Main.SystemInfo.getLogger().log(TAG, "Echomail NOT copied to sent");
    }
}
