package com.pushkin.hotdoged.export;

import android.os.Build;

public final class Constants {
    public static final String AUTHORITY = "com.pushkin.hotdoged.provider";
    public static final int CANNOT_UPDATE_PER_GROUP = 0;
    public static final int CANNOT_UPDATE_PER_SERVER = 0;
    public static final int CAN_UPDATE_PER_GROUP = 1;
    public static final int CAN_UPDATE_PER_SERVER = 1;
    public static final String DEFAULT_ORIGIN = "Android device";
    public static final int GROUPTYPE_DELETED = 7;
    public static final int GROUPTYPE_DRAFT = 3;
    public static final int GROUPTYPE_FILTER = 10;
    public static final int GROUPTYPE_GROUP = 20;
    public static final int GROUPTYPE_NETMAIL = 1;
    public static final int GROUPTYPE_OUTGOING = 5;
    public static final int GROUPTYPE_SENT = 6;
    public static final int HEADER_PT = 1;
    public static final int HEADER_QP = 0;
    public static final String INTENT_EXTRA_ADDSRVACTIVITY = "addsrvactivity";
    public static final String INTENT_EXTRA_CATEGORY = "category";
    public static final String INTENT_EXTRA_CFGACTIVITY = "cfgactivity";
    public static final String INTENT_EXTRA_CFGGRPACTIVITY = "cfggrpactivity";
    public static final String INTENT_EXTRA_CFGSRVACTIVITY = "cfgsrvactivity";
    public static final String INTENT_EXTRA_DBID = "_id";
    public static final String INTENT_EXTRA_DESCRIPTION = "description";
    public static final String INTENT_EXTRA_DOMAIN = "domain";
    public static final String INTENT_EXTRA_GROUP = "groupid";
    public static final String INTENT_EXTRA_NEWMSGS = "new_msgs";
    public static final String INTENT_EXTRA_PG_UPDATE = "pg_update";
    public static final String INTENT_EXTRA_PS_UPDATE = "ps_update";
    public static final String INTENT_EXTRA_PURGEPERIOD = "purgeperiod";
    public static final String INTENT_EXTRA_SERVER = "serverid";
    public static final String INTENT_EXTRA_SERVERLOGIN = "server_login";
    public static final String INTENT_EXTRA_SERVERPASSWORD = "server_password";
    public static final String INTENT_EXTRA_SUBSCRIBEACTIVITY = "subscribeactivity";
    public static final String INTENT_EXTRA_SYNCINTENT = "syncintent";
    public static final String INTENT_EXTRA_SYNCTYPE = "synctype";
    public static final String INTENT_EXTRA_VERSION = "version";
    public static final String INTENT_EXTRA_WRITABLE = "writable";
    public static final int MAX_SERVERS = 100000;
    public static final String ONCONTENTUPDATED_BROADCAST = "com.pushkin.hotdoged.contentupdated";
    public static final String ONSTART_BROADCAST = "com.pushkin.hotdoged.started";
    public static final String ONSTART_PENDING_INTENT = "com.pushkin.hotdoged.response";
    public static final String REQUEST_BACKUP_NOW = "com.pushkin.hotdoged.backup_now";
    public static final String REQUEST_RESTORE_NOW = "com.pushkin.hotdoged.restore_now";
    public static final int SYNC_CATEGORY = 3;
    public static final int SYNC_GROUP = 1;
    public static final int SYNC_INCOMING_FINISHED = 8;
    public static final int SYNC_INCOMING_STARTED = 7;
    public static final int SYNC_SCHEDULED = 6;
    public static final int SYNC_SEND = 5;
    public static final int SYNC_SERVER = 2;
    public static final int SYNC_STOP = 4;
    public static final int SYNC_UNKNOWN = -1;
    public static final String UPDATE_ADDRESSBOOK_BROADCAST = "com.pushkin.hotdoged.update_addressbook";
    public static final String VERSION = "2.14.5";
    public static final String USER_AGENT = "ХотДог/2.14.5 (Android; Google Android; rv:1) Hotdoged/" + Build.TIME + " HotdogEd/2.14.5";
    public static final String INTENT_EXTRA_SERVERACTIVE = "server_active";
    public static final String INTENT_EXTRA_SERVERNAME = "server_name";
    public static final String INTENT_EXTRA_SERVERIP = "server_ip";
    public static final String INTENT_EXTRA_SERVERDESCRIPTION = "server_description";
    public static final String INTENT_EXTRA_SERVERCODEPAGE = "server_codepage";
    public static final String INTENT_EXTRA_SERVERAUTHENABLE = "server_auth_enable";
    public static final String INTENT_EXTRA_KEEPMSGAMOUNTPERGROUP = "keep_msg_amount_per_group";
    public static final String INTENT_EXTRA_KEEPMSGDAYSPERGROUP = "keep_msg_days_per_group";
    public static final String INTENT_EXTRA_NAME = "user_name";
    public static final String INTENT_EXTRA_ADDRESS = "user_address";
    public static final String INTENT_EXTRA_SERVERQUOTING = "server_quoting";
    public static final String INTENT_EXTRA_SIGNATURE = "signature";
    public static final String INTENT_EXTRA_CUSTOMHEADERS = "custom_headers";
    public static final String INTENT_EXTRA_OUTPUTHEADERSFORMAT = "outputheadersformat";
    public static final String[] DB_SERVER_FIELDS = {INTENT_EXTRA_SERVERACTIVE, INTENT_EXTRA_SERVERNAME, INTENT_EXTRA_SERVERIP, INTENT_EXTRA_SERVERDESCRIPTION, INTENT_EXTRA_SERVERCODEPAGE, INTENT_EXTRA_SERVERAUTHENABLE, INTENT_EXTRA_KEEPMSGAMOUNTPERGROUP, INTENT_EXTRA_KEEPMSGDAYSPERGROUP, INTENT_EXTRA_NAME, INTENT_EXTRA_ADDRESS, INTENT_EXTRA_SERVERQUOTING, INTENT_EXTRA_SIGNATURE, INTENT_EXTRA_CUSTOMHEADERS, INTENT_EXTRA_OUTPUTHEADERSFORMAT};
    public static final String INTENT_EXTRA_PURGEREAD = "purge_read";
    public static final String[] DB_GROUP_FIELDS = {INTENT_EXTRA_SERVERCODEPAGE, INTENT_EXTRA_KEEPMSGAMOUNTPERGROUP, INTENT_EXTRA_KEEPMSGDAYSPERGROUP, INTENT_EXTRA_NAME, INTENT_EXTRA_ADDRESS, INTENT_EXTRA_SERVERQUOTING, INTENT_EXTRA_SIGNATURE, INTENT_EXTRA_PURGEREAD, INTENT_EXTRA_CUSTOMHEADERS};
    public static final String INTENT_EXTRA_SERVERAREASURL = "areasurl";
    public static final String INTENT_EXTRA_ORIGIN = "origin";
    public static final String INTENT_EXTRA_ADD_INT_01 = "add_int_01";
    public static final String INTENT_EXTRA_ADD_INT_02 = "add_int_02";
    public static final String INTENT_EXTRA_ADD_INFO_01 = "add_info_01";
    public static final String[] DB_FTN_FIELDS = {INTENT_EXTRA_SERVERACTIVE, INTENT_EXTRA_SERVERNAME, INTENT_EXTRA_SERVERIP, INTENT_EXTRA_SERVERAREASURL, INTENT_EXTRA_SERVERCODEPAGE, INTENT_EXTRA_KEEPMSGAMOUNTPERGROUP, INTENT_EXTRA_KEEPMSGDAYSPERGROUP, INTENT_EXTRA_NAME, INTENT_EXTRA_ADDRESS, INTENT_EXTRA_SERVERQUOTING, INTENT_EXTRA_SIGNATURE, INTENT_EXTRA_CUSTOMHEADERS, INTENT_EXTRA_ORIGIN, INTENT_EXTRA_SERVERDESCRIPTION, INTENT_EXTRA_ADD_INT_01, INTENT_EXTRA_ADD_INT_02, INTENT_EXTRA_ADD_INFO_01, "domain"};
    public static final String[] DB_FTN_GROUP_FIELDS = {INTENT_EXTRA_KEEPMSGAMOUNTPERGROUP, INTENT_EXTRA_KEEPMSGDAYSPERGROUP, INTENT_EXTRA_SERVERCODEPAGE, INTENT_EXTRA_NAME, INTENT_EXTRA_SERVERQUOTING, INTENT_EXTRA_SIGNATURE, INTENT_EXTRA_PURGEREAD, INTENT_EXTRA_CUSTOMHEADERS};
}