package jnode.ftn.types;

import com.pushkin.ftn.Main;
import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FtnAddress implements Serializable, Comparable<FtnAddress> {
    private static final String TAG = "FtnAddress";
    private static final long serialVersionUID = 1;
    protected int net;
    protected int node;
    protected int point;
    protected int zone;

    public FtnAddress(String addr) {
        Pattern p = Pattern.compile("^(\\d{1,3})?:?(\\d{1,5})/(\\d{1,5})\\.?(\\d{1,5})?@?(\\S+)?$");
        Matcher m = p.matcher(addr);
        if (m.matches()) {
            if (m.group(1) != null && m.group(1).length() > 0) {
                this.zone = new Integer(m.group(1)).intValue();
                if (this.zone == 0) {
                    this.zone = Main.info.getAddress().zone;
                    Main.SystemInfo.getLogger().log(TAG, "Zone for address " + addr + " set to " + this.zone + ", reason: it was 0");
                }
            } else {
                this.zone = Main.info.getAddress().zone;
                Main.SystemInfo.getLogger().log(TAG, "Zone for address " + addr + " set to " + this.zone + ", reason: it was not set");
            }
            this.net = new Integer(m.group(2)).intValue();
            this.node = new Integer(m.group(3)).intValue();
            if (m.group(4) != null && m.group(4).length() > 0) {
                this.point = new Integer(m.group(4)).intValue();
                return;
            } else {
                this.point = 0;
                return;
            }
        }
        throw new NumberFormatException();
    }

    public FtnAddress() {
        this.zone = Main.info.getAddress().zone;
        this.net = 0;
        this.node = 0;
        this.point = 0;
    }

    public String toString() {
        return this.point > 0 ? String.format("%d:%d/%d.%d", Integer.valueOf(this.zone), Integer.valueOf(this.net), Integer.valueOf(this.node), Integer.valueOf(this.point)) : String.format("%d:%d/%d", Integer.valueOf(this.zone), Integer.valueOf(this.net), Integer.valueOf(this.node));
    }

    public String intl() {
        return String.format("%d:%d/%d", Integer.valueOf(this.zone), Integer.valueOf(this.net), Integer.valueOf(this.node));
    }

    public String topt() {
        return this.point != 0 ? String.format("\u0001TOPT %d\r", Integer.valueOf(this.point)) : "";
    }

    public String fmpt() {
        return this.point != 0 ? String.format("\u0001FMPT %d\r", Integer.valueOf(this.point)) : "";
    }

    public short getZone() {
        return (short) this.zone;
    }

    public short getNet() {
        return (short) this.net;
    }

    public short getNode() {
        return (short) this.node;
    }

    public short getPoint() {
        return (short) this.point;
    }

    public void setZone(int zone) {
        if (zone != 0) {
            this.zone = zone;
            return;
        }
        this.zone = Main.info.getAddress().zone;
        Main.SystemInfo.getLogger().log("FtnAddress.setZone", "Zone for address " + toString() + " set to " + this.zone + ", reason: it was 0");
    }

    public void setNet(int net) {
        this.net = net;
    }

    public void setNode(int node) {
        this.node = node;
    }

    public void setPoint(int point) {
        this.point = point;
    }

    public int hashCode() {
        int result = this.net + 31;
        return (((((result * 31) + this.node) * 31) + this.point) * 31) + this.zone;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj != null && (obj instanceof FtnAddress)) {
            FtnAddress other = (FtnAddress) obj;
            return this.net == other.net && this.node == other.node && this.point == other.point && this.zone == other.zone;
        }
        return false;
    }

    public boolean isPointOf(FtnAddress boss) {
        return boss.zone == this.zone && boss.net == this.net && boss.node == this.node;
    }

    public FtnAddress clone() {
        FtnAddress n = new FtnAddress();
        n.zone = this.zone;
        n.net = this.net;
        n.node = this.node;
        n.point = this.point;
        return n;
    }

    @Override // java.lang.Comparable
    public int compareTo(FtnAddress another) {
        if (this.zone != another.getZone()) {
            return this.zone - another.getZone();
        }
        if (this.net != another.getNet()) {
            return this.net - another.getNet();
        }
        if (this.node != another.getNode()) {
            return this.node - another.getNode();
        }
        if (this.point != another.getPoint()) {
            return this.point - another.getPoint();
        }
        return 0;
    }
}
