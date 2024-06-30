package jnode.event;

import com.pushkin.ftn.Link;

public class NewEchoareaEvent implements IEvent {
    private String text;

    public NewEchoareaEvent(String name, Link link) {
        this.text = "Echoarea " + name + " created by " + (link == null ? "local system" : link.getLinkAddress()) + "\n";
    }

    @Override // jnode.event.IEvent
    public String getEvent() {
        return this.text;
    }
}
