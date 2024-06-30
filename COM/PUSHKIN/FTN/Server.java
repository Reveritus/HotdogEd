package com.pushkin.ftn;

import android.content.Context;
import com.pushkin.ftn.Main;
import com.pushkin.hotdoged.export.HotdogedException;
import com.pushkin.hotdoged.fido.ContentFetchService;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.Inet4Address;
import java.net.ServerSocket;
import java.net.Socket;
import jnode.ftn.types.FtnAddress;
import jnode.logger.Logger;
import jnode.protocol.binkp.BinkpConnector;
import jnode.protocol.io.Connector;
import jnode.protocol.io.exception.ProtocolException;

public class Server extends Thread {
    private static final Logger logger = Main.SystemInfo.getLogger();
    private Context context;
    private FtnAddress ftnAddress;
    private String host;
    private int port;
    public boolean needsStop = false;
    private int errors = 0;

    @Override // java.lang.Thread
    public String toString() {
        return "Server [host=" + this.host + ", port=" + this.port + ", ftnAddress=" + this.ftnAddress + "]";
    }

    public int hashCode() {
        int result = (this.ftnAddress == null ? 0 : this.ftnAddress.hashCode()) + 31;
        return result;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj != null && getClass() == obj.getClass()) {
            Server other = (Server) obj;
            return this.ftnAddress == null ? other.ftnAddress == null : this.ftnAddress.equals(other.ftnAddress);
        }
        return false;
    }

    private static class ServerClient extends Thread {
        private static final Logger logger = Main.SystemInfo.getLogger();
        private Context context;
        private Socket socket;

        public ServerClient(Context context, Socket socket) {
            this.context = context;
            this.socket = socket;
        }

        @Override // java.lang.Thread, java.lang.Runnable
        public void run() {
            try {
                try {
                    try {
                        synchronized (ContentFetchService.class) {
                            logger.l3(String.format("Incoming connection from %s:%d", this.socket.getInetAddress().getHostAddress(), Integer.valueOf(this.socket.getPort())));
                            Connector connector = new Connector(new BinkpConnector());
                            connector.accept(this.socket);
                        }
                    } catch (ProtocolException e) {
                        logger.l2("Connector initialization failed: " + e.getMessage());
                        try {
                            this.socket.close();
                        } catch (IOException e2) {
                        }
                    }
                } catch (HotdogedException e3) {
                    e3.printStackTrace();
                    logger.l2("Error while establishing inbound connection: " + e3.getMessage());
                    try {
                        this.socket.close();
                    } catch (IOException e4) {
                    }
                }
            } finally {
                try {
                    this.socket.close();
                } catch (IOException e5) {
                }
            }
        }
    }

    public Server(Context context, String host, int port, FtnAddress ftnAddress) {
        this.context = context;
        this.host = host;
        this.port = port;
        this.ftnAddress = ftnAddress;
    }

    public void stopServer() {
        this.needsStop = true;
    }

    @Override // java.lang.Thread, java.lang.Runnable
    public void run() {
        Socket clientSocket;
        logger.l4("Server listens on " + this.host + ":" + this.port);
        try {
            ServerSocket socket = new ServerSocket(this.port, 0, this.host == null ? null : Inet4Address.getByName(this.host));
            while (!socket.isClosed() && socket.isBound()) {
                this.needsStop = false;
                socket.setSoTimeout(5000);
                try {
                    clientSocket = socket.accept();
                } catch (InterruptedIOException e) {
                    if (this.needsStop) {
                        break;
                    }
                }
                if (this.needsStop) {
                    break;
                }
                new ServerClient(this.context, clientSocket).start();
            }
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e2) {
            logger.l2("Server error: " + e2.getMessage());
        }
        if (!this.needsStop) {
            this.errors++;
            if (this.errors < 10) {
                logger.l3("Server crashed, restart");
                run();
                return;
            }
            logger.l2("Server crashed 10 times, leave");
            return;
        }
        logger.l2("Server stop requested");
    }
}
