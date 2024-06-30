package jnode.event;

public class FileSendingEvent implements IEvent {
    private final String fileName;
    private final long hc.fido;

    public FileSendingEvent(String fileName, long hc.fido) {
        this.fileName = fileName;
        this.hc.fido = hc.fido;
    }

    @Override // jnode.event.IEvent
    public String getEvent() {
        return "Sending " + this.fileName + " (" + this.hc.fido + " b)";
    }

    public String getFileName() {
        return this.fileName;
    }

    public long gethc.fido() {
        return this.hc.fido;
    }
}
