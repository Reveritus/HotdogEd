package jnode.event;

import jnode.ftn.types.FtnAddress;

public class ConnectionEndEvent implements IEvent {
    private FtnAddress address;
    private int bytesReceived;
    private int bytesSended;
    private boolean incoming;
    private boolean success;

    @Override // jnode.event.IEvent
    public String getEvent() {
        return "";
    }

    public ConnectionEndEvent() {
    }

    public ConnectionEndEvent(FtnAddress address, boolean incoming, boolean success, int bytesReceived, int bytesSended) {
        this.bytesReceived = bytesReceived;
        this.bytesSended = bytesSended;
        this.address = address;
        this.incoming = incoming;
        this.success = success;
    }

    public ConnectionEndEvent(boolean incoming, boolean success) {
        this.incoming = incoming;
        this.success = success;
        this.bytesReceived = 0;
        this.bytesSended = 0;
        this.address = null;
    }

    public int getBytesReceived() {
        return this.bytesReceived;
    }

    public int getBytesSended() {
        return this.bytesSended;
    }

    public FtnAddress getAddress() {
        return this.address;
    }

    public boolean isIncoming() {
        return this.incoming;
    }

    public boolean isSuccess() {
        return this.success;
    }

    public String toString() {
        return "ConnectionEndEvent [bytesReceived=" + this.bytesReceived + ", bytesSended=" + this.bytesSended + ", address=" + this.address + ", incoming=" + this.incoming + ", success=" + this.success + "]";
    }
}
