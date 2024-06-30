package jnode.event;

public class FileTossingEvent implements IEvent {
    private final String fileName;

    public FileTossingEvent(String fileName) {
        this.fileName = fileName;
    }

    @Override // jnode.event.IEvent
    public String getEvent() {
        return "Распаковка " + this.fileName;
    }

    public String getFileName() {
        return this.fileName;
    }
}
