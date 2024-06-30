package com.pushkin.ftn;

import com.pushkin.hotdoged.export.HotdogedException;

public class Subscription {
    private Echoarea area;
    private Link link;

    public Subscription(Echoarea area, Link link) throws HotdogedException {
        this.area = area;
        this.link = link;
    }

    public Subscription() {
    }

    public Link getLink() {
        return this.link;
    }

    public void setLink(Link link) {
        this.link = link;
    }

    public Echoarea getArea() {
        return this.area;
    }

    public void setArea(Echoarea area) {
        this.area = area;
    }

    public void save() {
        System.out.println("Subscription for " + this.link.toString() + " to " + this.area.getName() + " saved");
    }
}
