package com.pushkin.ftn;

public class EchomailAwaiting {
    private Link link;
    private Echomail mail;

    public Link getLink() {
        return this.link;
    }

    public void setLink(Link link) {
        this.link = link;
    }

    public Echomail getMail() {
        return this.mail;
    }

    public void setMail(Echomail mail) {
        this.mail = mail;
    }

    public EchomailAwaiting() {
    }

    public EchomailAwaiting(Link link, Echomail mail) {
        this.link = link;
        this.mail = mail;
    }
}
