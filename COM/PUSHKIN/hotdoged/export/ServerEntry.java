package com.pushkin.hotdoged.export;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

public class ServerEntry {
    private int _id;
    private String areasurl;
    private int category_id;
    private final Context context;
    private String custom_headers;
    private String domain;
    private int keep_msg_amount_per_group;
    private int keep_msg_days_per_group;
    private long last_sync;
    private String origin;
    private int outputheadersformat;
    private int schedule_time;
    private final Uri serverUri;
    private boolean server_active;
    private boolean server_auth_enable;
    private String server_codepage;
    private String server_description;
    private String server_ip;
    private String server_name;
    private String server_quoting;
    private String signature;
    private int template_id;
    private String user_address;
    private String user_name;

    public synchronized int getSchedule_time() {
        return this.schedule_time;
    }

    public synchronized void setSchedule_time(int schedule_time) {
        this.schedule_time = schedule_time;
    }

    public ServerEntry(Context context, Uri serverUri) throws HotdogedException {
        this.context = context;
        this.serverUri = serverUri;
        if (serverUri == null) {
            throw new HotdogedException("Bad URI: " + serverUri);
        }
        Cursor cursor = null;
        try {
            cursor = this.context.getContentResolver().query(serverUri, null, null, null, null);
            if (!cursor.moveToFirst()) {
                throw new HotdogedException("Server with ID " + serverUri.getLastPathSegment() + " not found");
            }
            this._id = cursor.getInt(cursor.getColumnIndex(Constants.INTENT_EXTRA_DBID));
            this.category_id = cursor.getInt(cursor.getColumnIndex("category_id"));
            this.template_id = cursor.getInt(cursor.getColumnIndex("template_id"));
            this.server_active = cursor.getInt(cursor.getColumnIndex(Constants.INTENT_EXTRA_SERVERACTIVE)) == 1;
            this.server_name = cursor.getString(cursor.getColumnIndex(Constants.INTENT_EXTRA_SERVERNAME));
            this.server_ip = cursor.getString(cursor.getColumnIndex(Constants.INTENT_EXTRA_SERVERIP));
            this.server_description = cursor.getString(cursor.getColumnIndex(Constants.INTENT_EXTRA_SERVERDESCRIPTION));
            this.server_codepage = cursor.getString(cursor.getColumnIndex(Constants.INTENT_EXTRA_SERVERCODEPAGE));
            this.server_auth_enable = cursor.getInt(cursor.getColumnIndex(Constants.INTENT_EXTRA_SERVERAUTHENABLE)) == 1;
            this.keep_msg_amount_per_group = cursor.getInt(cursor.getColumnIndex(Constants.INTENT_EXTRA_KEEPMSGAMOUNTPERGROUP));
            this.keep_msg_days_per_group = cursor.getInt(cursor.getColumnIndex(Constants.INTENT_EXTRA_KEEPMSGDAYSPERGROUP));
            this.user_name = cursor.getString(cursor.getColumnIndex(Constants.INTENT_EXTRA_NAME));
            this.user_address = cursor.getString(cursor.getColumnIndex(Constants.INTENT_EXTRA_ADDRESS));
            this.server_quoting = cursor.getString(cursor.getColumnIndex(Constants.INTENT_EXTRA_SERVERQUOTING));
            this.signature = cursor.getString(cursor.getColumnIndex(Constants.INTENT_EXTRA_SIGNATURE));
            this.custom_headers = cursor.getString(cursor.getColumnIndex(Constants.INTENT_EXTRA_CUSTOMHEADERS));
            this.origin = cursor.getString(cursor.getColumnIndex(Constants.INTENT_EXTRA_ORIGIN));
            this.schedule_time = cursor.getInt(cursor.getColumnIndex("schedule_time"));
            this.last_sync = cursor.getLong(cursor.getColumnIndex("last_sync"));
            this.areasurl = cursor.getString(cursor.getColumnIndex(Constants.INTENT_EXTRA_SERVERAREASURL));
            this.domain = cursor.getString(cursor.getColumnIndex("domain"));
            try {
                this.outputheadersformat = cursor.getInt(cursor.getColumnIndex(Constants.INTENT_EXTRA_OUTPUTHEADERSFORMAT));
            } catch (Exception e) {
                this.outputheadersformat = 0;
            }
            cursor.close();
        } catch (Exception e2) {
            try {
                cursor.close();
            } catch (Exception e3) {
            }
            throw new HotdogedException("Error fetching server info: " + e2.getMessage());
        }
    }

    public Context getContext() {
        return this.context;
    }

    public Uri getServerUri() {
        return this.serverUri;
    }

    public synchronized int get_id() {
        return this._id;
    }

    public synchronized void set_id(int _id) {
        this._id = _id;
    }

    public synchronized int getCategory_id() {
        return this.category_id;
    }

    public synchronized void setCategory_id(int category_id) {
        this.category_id = category_id;
    }

    public synchronized boolean isServer_active() {
        return this.server_active;
    }

    public synchronized void setServer_active(boolean server_active) {
        this.server_active = server_active;
    }

    public synchronized String getServer_name() {
        return this.server_name;
    }

    public synchronized void setServer_name(String server_name) {
        this.server_name = server_name;
    }

    public synchronized String getServer_ip() {
        return this.server_ip;
    }

    public synchronized void setServer_ip(String server_ip) {
        this.server_ip = server_ip;
    }

    public synchronized String getServer_description() {
        return this.server_description;
    }

    public synchronized void setServer_description(String server_description) {
        this.server_description = server_description;
    }

    public synchronized String getServer_codepage() {
        return this.server_codepage;
    }

    public synchronized void setServer_codepage(String server_codepage) {
        this.server_codepage = server_codepage;
    }

    public synchronized boolean isServer_auth_enable() {
        return this.server_auth_enable;
    }

    public synchronized void setServer_auth_enable(boolean server_auth_enable) {
        this.server_auth_enable = server_auth_enable;
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

    public synchronized String getSignature() {
        return this.signature;
    }

    public synchronized void setSignature(String signature) {
        this.signature = signature;
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

    public synchronized int getOutputheadersformat() {
        return this.outputheadersformat;
    }

    public synchronized void setOutputheadersformat(int outputheadersformat) {
        this.outputheadersformat = outputheadersformat;
    }

    public synchronized String getOrigin() {
        return this.origin;
    }

    public synchronized void setOrigin(String origin) {
        this.origin = origin;
    }

    public synchronized long getLast_sync() {
        return this.last_sync;
    }

    public synchronized void setLast_sync(long last_sync) {
        this.last_sync = last_sync;
    }

    public synchronized String getAreasurl() {
        return this.areasurl;
    }

    public synchronized void setAreasurl(String areasurl) {
        this.areasurl = areasurl;
    }

    public synchronized String getDomain() {
        return this.domain;
    }

    public synchronized void setDomain(String domain) {
        this.domain = domain;
    }
}
