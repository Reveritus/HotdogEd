package jnode.event;

public class UnknownFileEvent implements IEvent {
    private final String fileName;

    public UnknownFileEvent(String fileName) {
        this.fileName = fileName;
    }

    @Override // jnode.event.IEvent
    public String getEvent() {
        return "Unknown file received: " + this.fileName;
    }

    public String getFileName() {
        return this.fileName;
    }
}
