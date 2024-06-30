package com.pushkin.ftn;

public class Route {
    private String fromAddr;
    private String fromName;
    private Long nice;
    private Link routeVia;
    private String subject;
    private String toAddr;
    private String toName;

    public Long getNice() {
        return this.nice;
    }

    public void setNice(Long nice) {
        this.nice = nice;
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

    public String getFromAddr() {
        return this.fromAddr;
    }

    public void setFromAddr(String fromAddr) {
        this.fromAddr = fromAddr;
    }

    public String getToAddr() {
        return this.toAddr;
    }

    public void setToAddr(String toAddr) {
        this.toAddr = toAddr;
    }

    public String getSubject() {
        return this.subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public Link getRouteVia() {
        return this.routeVia;
    }

    public void setRouteVia(Link routeVia) {
        this.routeVia = routeVia;
    }
}
