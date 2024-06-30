package jnode.event;

public class FileReceivingEvent implements IEvent {
    private final String fileName;
    private final long fileSize;

    public FileReceivingEvent(String fileName, long fileSize) {
        this.fileName = fileName;
        this.fileSize = fileSize;
    }

    @Override // jnode.event.IEvent
    public String getEvent() {
        return "Getting " + this.fileName + " (" + this.fileSize + " b)";
    }

    public String getFileName() {
        return this.fileName;
    }

    public long getFileSize() {
        return this.fileSize;
    }
}
