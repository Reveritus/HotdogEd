package jnode.event;

import jnode.ftn.types.FtnMessage;

public class FtnMessageReceivedEvent implements IEvent {
    private final FtnMessage message;

    public FtnMessageReceivedEvent(FtnMessage message) {
        this.message = message;
    }

    @Override // jnode.event.IEvent
    public String getEvent() {
        return "Received message " + this.message.getMsgid() + " to group " + this.message.getArea();
    }

    public FtnMessage getFtnMessage() {
        return this.message;
    }
}
