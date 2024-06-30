package jnode.protocol.io;

import com.pushkin.ftn.Link;
import com.pushkin.ftn.Main;
import com.pushkin.hotdoged.export.HotdogedException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import jnode.event.ConnectionEndEvent;
import jnode.event.Notifier;
import jnode.ftn.tosser.FtnTosser;
import jnode.ftn.types.FtnAddress;
import jnode.logger.Logger;
import jnode.protocol.io.exception.ProtocolException;

/* 
Connector timeout 
*/
public class Connector {
    private static final int CONN_TIMEOUT = 60000;
    private static final Logger logger = Logger.getLogger(Connector.class, Main.SystemInfo.getEventsArray());
    private Socket clientSocket;
    private ProtocolConnector connector;
    private Link link;
    private int index = 0;
    private FtnTosser tosser = new FtnTosser();
    private List<Message> messages = new ArrayList();
    private List<Message> receivedMessages = new ArrayList();

    public Connector(ProtocolConnector connector) throws ProtocolException {
        this.connector = connector;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }

    public Link getLink() {
        return this.link;
    }

    public void setLink(Link link) {
        this.link = link;
        List<Message> messages = FtnTosser.getMessagesForLink(link);
        this.messages = messages;
        this.index = 0;
        logger.l4(String.format("Received %d messages for %s", Integer.valueOf(messages.size()), link.getLinkAddress()));
    }

    public int onReceived(Message message) {
        this.receivedMessages.add(message);
        return 0;
    }
/*
Client socket
*/
    private void doSocket(Socket clientSocket) throws HotdogedException {
        ConnectionEndEvent event;
        boolean success = true;
        long lastactive = System.currentTimeMillis();
        try {
            InputStream is = clientSocket.getInputStream();
            OutputStream os = clientSocket.getOutputStream();
            while (true) {
                if (clientSocket.isClosed() && Main.info != null && Main.info.needsStop) {
                    break;
                }
                try {
                    if (is.available() > 0) {
                        this.connector.avalible(is);
                        lastactive = System.currentTimeMillis();
                    }
                    Frame[] frames = this.connector.getFrames();
                    if (frames != null && frames.length > 0) {
                        for (Frame frame : frames) {
                            try {
                                logger.l5("Sent frame: " + frame);
                                os.write(frame.getBytes());
                                lastactive = System.currentTimeMillis();
                            } catch (IOException e) {
                                if (clientSocket != null) {
                                    try {
                                        clientSocket.close();
                                    } catch (IOException e2) {
                                    }
                                }
                            }
                        }
                    }
                    if (this.connector.canSend()) {
                        if (this.messages.size() > this.index) {
                            ProtocolConnector protocolConnector = this.connector;
                            List<Message> list = this.messages;
                            int i = this.index;
                            this.index = i + 1;
                            protocolConnector.send(list.get(i));
                        } else {
                            this.connector.eob();
                        }
                    } else if (this.connector.closed()) {
                        if (clientSocket != null) {
                            try {
                                clientSocket.close();
                            } catch (IOException e3) {
                            }
                        }
                    } else if (System.currentTimeMillis() - lastactive > 60000) {
                        logger.l3("Connection(1) timed out");
                        success = false;
                        if (clientSocket != null) {
                            try {
                                clientSocket.close();
                            } catch (IOException e4) {
                            }
                        }
                    }
                } catch (IOException e5) {
                }
            }
            if (Main.info != null && Main.info.needsStop) {
                Main.SystemInfo.getLogger().log("doSocket", "Connection stopped by user request");
            }
            if (this.link == null) {
                event = new ConnectionEndEvent(this.connector.getIncoming(), success && this.connector.getSuccess());
            } else {
                event = new ConnectionEndEvent(new FtnAddress(this.link.getLinkAddress()), this.connector.getIncoming(), success && this.connector.getSuccess(), this.connector.getBytesReceived(), this.connector.getBytesSent());
            }
            Notifier.INSTANSE.notify(event);
            this.messages = new ArrayList();
            this.index = 0;
            if (this.link != null) {
                FtnTosser tosser = new FtnTosser();
                for (Message message : this.receivedMessages) {
                    tosser.tossIncoming(message, this.link);
                }
                return;
            }
            Main.SystemInfo.getLogger().l1("Link is not set, cannot toss.");
        } catch (IOException e6) {
            if (clientSocket != null) {
                try {
                    clientSocket.close();
                } catch (IOException e7) {
                }
            }
        }
    }
/*
Connect
*/
    public void connect(Link link) throws ProtocolException, HotdogedException {
        if (link == null) {
            throw new ProtocolException("Link can not be null");
        }
        this.link = link;
        this.connector.reset();
        this.connector.initOutgoing(this);
        try {
            try {
                try {
                    SocketAddress soAddr = new InetSocketAddress(link.getProtocolHost(), link.getProtocolPort().intValue());
                    this.clientSocket = new Socket();
                    this.clientSocket.connect(soAddr, 30000);
                    doSocket(this.clientSocket);
                    this.tosser.end();
                } catch (SocketTimeoutException e) {
                    Notifier.INSTANSE.notify(new ConnectionEndEvent(new FtnAddress(link.getLinkAddress()), false, false, 0, 0));
                    throw new ProtocolException("Connection timeout");
                }
            } catch (UnknownHostException e2) {
                Notifier.INSTANSE.notify(new ConnectionEndEvent(new FtnAddress(link.getLinkAddress()), false, false, 0, 0));
                throw new ProtocolException("Unknown host: " + link.getProtocolHost());
            } catch (IOException e3) {
                Notifier.INSTANSE.notify(new ConnectionEndEvent(new FtnAddress(link.getLinkAddress()), false, false, 0, 0));
                throw new ProtocolException(e3.getLocalizedMessage());
            }
        } finally {
            try {
                if (this.clientSocket != null) {
                    this.clientSocket.close();
                }
            } catch (IOException e4) {
            }
        }
    }
/*
incoming socket
*/
    public void accept(Socket clientSocket) throws ProtocolException, HotdogedException {
        this.connector.reset();
        this.connector.initIncoming(this);
        doSocket(clientSocket);
        this.tosser.end();
    }
}
