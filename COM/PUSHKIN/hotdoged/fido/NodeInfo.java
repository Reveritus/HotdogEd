package com.pushkin.hotdoged.fido;

import android.text.TextUtils;
import ch.boye.httpclientandroidlib.HttpHost;
import jnode.ftn.types.FtnAddress;

public class NodeInfo implements Comparable<NodeInfo> {
    private FtnAddress address;
    private String areas;
    private String areasFull;
    private String city;
    private String country;
    private String email;
    private String ftnAddress;
    private String ipaddress;
    private String note;
    private String pntRequestUrl;
    private String protocol;
    private String requestBy;
    private String sysop;
    private String system;
    private String yourCity;
    private String yourCountry;

    public int hashCode() {
        int result = (this.ipaddress == null ? 0 : this.ipaddress.hashCode()) + 31;
        return result;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj != null && getClass() == obj.getClass()) {
            NodeInfo other = (NodeInfo) obj;
            return this.ipaddress == null ? other.ipaddress == null : this.ipaddress.equals(other.ipaddress);
        }
        return false;
    }

    public NodeInfo(String ftnAddress, String requestBy, String country, String city, String system, String sysop, String email, String protocol, String ipaddress, String pntRequestUrl, String areas, String areasFull, String yourCountry, String yourCity, String note) {
        this.ftnAddress = ftnAddress;
        this.requestBy = requestBy;
        this.country = country;
        this.city = city;
        this.system = system;
        this.sysop = sysop;
        this.yourCountry = yourCountry;
        this.yourCity = yourCity;
        setNote(note);
        setEmail(email);
        this.protocol = protocol;
        this.ipaddress = ipaddress;
        setPntRequestUrl(pntRequestUrl);
        this.areas = areas;
        setAreasFull(areasFull);
        this.address = new FtnAddress(ftnAddress);
    }

    public String toString() {
        return "NodeInfo " + getPriority(this) + " [ftnAddress=" + this.ftnAddress + ", requestBy=" + this.requestBy + ", country=" + this.country + ", city=" + this.city + ", system=" + this.system + ", sysop=" + this.sysop + ", protocol=" + this.protocol + ", ipaddress=" + this.ipaddress + ", pntRequestUrl=" + this.pntRequestUrl + ", areas=" + this.areas + ", areasfull=" + getAreasFull() + ", email=" + this.email + "]";
    }

    public String getCountry() {
        return this.country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCity() {
        return this.city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getSystem() {
        return this.system;
    }

    public void setSystem(String system) {
        this.system = system;
    }

    public String getSysop() {
        return this.sysop;
    }

    public void setSysop(String sysop) {
        this.sysop = sysop;
    }

    public String getProtocol() {
        return this.protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getIpaddress() {
        return this.ipaddress;
    }

    public void setIpaddress(String ipaddress) {
        this.ipaddress = ipaddress;
    }

    public String getAreas() {
        return this.areas;
    }

    public void setAreas(String areas) {
        this.areas = areas;
    }

    public String getFtnAddress() {
        return this.ftnAddress;
    }

    public void setFtnAddress(String ftnaddress) {
        this.ftnAddress = ftnaddress;
    }

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPntRequestUrl() {
        return this.pntRequestUrl;
    }

    public void setPntRequestUrl(String pntRequestUrl) {
        this.pntRequestUrl = pntRequestUrl;
    }

    public String getRequestBy() {
        return this.requestBy;
    }

    public void setRequestBy(String requestBy) {
        this.requestBy = requestBy;
    }

    public String getAreasFull() {
        return this.areasFull;
    }

    public void setAreasFull(String areasFull) {
        this.areasFull = areasFull;
    }

    public static int getPriority(NodeInfo nodeInfo) {
        int priority = 0;
        String yourCountry = nodeInfo.getYourCountry();
        String yourCity = nodeInfo.getYourCity();
        if (nodeInfo.getAreasFull() != null || nodeInfo.getAreas() != null) {
            priority = 0 + 5;
        }
        if (nodeInfo.requestBy.equalsIgnoreCase("email")) {
            priority += 2;
        }
        if (nodeInfo.requestBy.equalsIgnoreCase(HttpHost.DEFAULT_SCHEME_NAME)) {
            priority += 3;
        }
        if (yourCountry != null && nodeInfo.getCountry() != null && yourCountry.trim().equalsIgnoreCase(nodeInfo.getCountry().trim())) {
            int priority2 = priority + 10;
            if (yourCity != null && nodeInfo.getCity() != null && yourCity.trim().equalsIgnoreCase(nodeInfo.getCity().trim())) {
                return priority2 + 10;
            }
            return priority2;
        }
        return priority;
    }

    @Override // java.lang.Comparable
    public int compareTo(NodeInfo another) {
        if (this == another) {
            return 0;
        }
        int diff = getPriority(another) - getPriority(this);
        if (diff == 0) {
            return this.address.compareTo(another.address);
        }
        return diff;
    }

    public String getPreferredAreasUrl() {
        return !TextUtils.isEmpty(this.areasFull) ? this.areasFull : this.areas;
    }

    public String getYourCountry() {
        return this.yourCountry;
    }

    public void setYourCountry(String yourCountry) {
        this.yourCountry = yourCountry;
    }

    public String getYourCity() {
        return this.yourCity;
    }

    public void setYourCity(String yourCity) {
        this.yourCity = yourCity;
    }

    public String getNote() {
        return this.note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
