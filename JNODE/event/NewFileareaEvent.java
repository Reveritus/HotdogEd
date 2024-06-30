package jnode.event;

import com.pushkin.ftn.Link;

public class NewFileareaEvent implements IEvent {
    private String text;

    public NewFileareaEvent(String name, Link link) {
        this.text = "Filearea " + name + " created by " + (link == null ? "local system" : link.getLinkAddress()) + "\n";
    }

    @Override // jnode.event.IEvent
    public String getEvent() {
        return this.text;
    }
}
