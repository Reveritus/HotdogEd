package jnode.protocol.io;

import com.pushkin.hotdoged.export.HotdogedException;
import java.io.InputStream;

public interface ProtocolConnector {
    void avalible(InputStream inputStream) throws HotdogedException;

    boolean canSend();

    boolean closed();

    void eob();

    int getBytesReceived();

    int getBytesSent();

    Frame[] getFrames();

    boolean getIncoming();

    boolean getSuccess();

    void initIncoming(Connector connector);

    void initOutgoing(Connector connector);

    void reset();

    void send(Message message);
}
