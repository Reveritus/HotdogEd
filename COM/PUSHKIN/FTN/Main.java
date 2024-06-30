package com.pushkin.ftn;

import android.content.Context;
import android.text.TextUtils;
import com.pushkin.hotdoged.export.HotdogedException;
import com.pushkin.hotdoged.export.ServerEntry;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import jnode.ftn.types.FtnAddress;
import jnode.logger.Logger;

public class Main {
    private static final String DEFAULT_ORIGIN = "Android device, Milky Way";
    private static final int MAX_ORIGIN_LENGTH = 79;
    private static final String NDL = "115200,TCP,BINKP";
    private static String inbound;
    public static SystemInfo info;

    public static class SystemInfo {
        private static Context context;
        private static final ArrayList<String> eventsArray = new ArrayList<>();
        private static final Logger logger = Logger.getLogger(SystemInfo.class, eventsArray);
        private final String NDL;
        private final FtnAddress address;
        private final FtnAddress bossAddress;
        private String domain;
        private Link link;
        private final String location;
        private String origin;
        private ServerEntry serverEntry;
        private final String stationName;
        private final String sysop;
        private final String tearline;
        private final String version = "jNode/Android";
        public volatile boolean needsStop = false;

        public SystemInfo(Context context2, String sysop, String location, String stationName, String ourAddr, String bossAddr, String NDL, String tearline, ServerEntry serverEntry) throws HotdogedException {
            context = context2;
            this.sysop = sysop;
            this.location = location;
            this.stationName = stationName;
            this.NDL = NDL;
            this.tearline = tearline;
            this.origin = serverEntry.getOrigin();
            if (TextUtils.isEmpty(this.origin)) {
                this.origin = Main.DEFAULT_ORIGIN;
            }
            int originMaxLength = (75 - ourAddr.length()) - 11;
            if (this.origin.length() > originMaxLength) {
                this.origin = this.origin.substring(0, originMaxLength);
            }
            this.serverEntry = serverEntry;
            this.address = new FtnAddress(ourAddr);
            this.bossAddress = new FtnAddress(bossAddr);
            this.domain = TextUtils.isEmpty(serverEntry.getDomain()) ? "@fidonet" : "@" + serverEntry.getDomain();
            createInbound();
        }

        public static ArrayList<String> getEventsArray() {
            return eventsArray;
        }

        public static File createTempFile(String name) throws HotdogedException {
            File f = context.getFileStreamPath(name);
            try {
                f.createNewFile();
                return f;
            } catch (IOException e) {
                throw new HotdogedException("Failed to create temp file " + f.getAbsolutePath());
            }
        }

        private void createInbound() throws HotdogedException {
            File inboundDir = context.getDir("inbound", 0);
            if (inboundDir.mkdirs() || inboundDir.isDirectory()) {
                String unused = Main.inbound = inboundDir.getAbsolutePath() + getUniqueSubdir(this.address);
                File inboundDir2 = new File(Main.inbound);
                if (!inboundDir2.mkdirs() && !inboundDir2.isDirectory()) {
                    throw new HotdogedException("Failed to create inbound dir " + Main.inbound);
                }
            } else {
                String unused2 = Main.inbound = "/tmp";
            }
            getLogger().log("createInbound", "Inbound set to: " + Main.inbound);
        }

        private String getUniqueSubdir(FtnAddress address) {
            return address != null ? ((int) address.getZone()) + "." + ((int) address.getNet()) + "." + ((int) address.getNode()) + "." + ((int) address.getPoint()) : String.valueOf(Math.round(Math.random() * 1000.0d));
        }

        public String getSysop() {
            return this.sysop;
        }

        public String getNDL() {
            return this.NDL;
        }

        public String getLocation() {
            return this.location;
        }

        public String getStationName() {
            return this.stationName;
        }

        public FtnAddress getAddress() {
            return this.address;
        }

        public String getVersion() {
            return "jNode/Android";
        }

        public FtnAddress getBossAddress() {
            return this.bossAddress;
        }

        public static String getPID() {
            return "ХотДог/2.14.5/Android";
        }

        public static Context getContext() {
            return context;
        }

        public long getLinkId() throws HotdogedException {
            if (this.link == null) {
                throw new HotdogedException("Link not set!");
            }
            return this.link.getId().longValue();
        }

        public String getTearline() {
            return this.tearline;
        }

        public String getOrigin() {
            return this.origin;
        }

        public static Logger getLogger() {
            return logger;
        }

        public String getLinkPreferredCodePage() {
            return this.serverEntry.getServer_codepage();
        }

        public ServerEntry getServerEntry() {
            return this.serverEntry;
        }

        public void setServerEntry(ServerEntry serverEntry) {
            this.serverEntry = serverEntry;
        }

        public Link getLink() {
            return this.link;
        }

        public void setLink(Link link) {
            this.link = link;
        }

        public String getDomain() {
            return this.domain;
        }

        public void setDomain(String domain) {
            this.domain = domain;
        }
    }

    public static String getInbound() {
        return inbound;
    }
}
