package com.pushkin.hotdoged.fido;

import android.content.Context;
import android.util.Log;
import com.pushkin.ftn.Server;
import com.pushkin.hotdoged.export.HotdogedException;
import java.util.ArrayList;
import jnode.ftn.types.FtnAddress;

public class IncomingConnectionsManager extends ArrayList<Server> {
    private static final String TAG = "IncomingConnectionsManager";
    private static final long serialVersionUID = 569555284472592822L;

    @Override // java.util.ArrayList, java.util.AbstractList, java.util.List
    public void add(int index, Server object) {
        if (!contains(object)) {
            super.add(index, (int) object);
        }
    }

    @Override // java.util.ArrayList, java.util.AbstractList, java.util.AbstractCollection, java.util.Collection, java.util.List
    public boolean add(Server object) {
        if (contains(object)) {
            return true;
        }
        return super.add((IncomingConnectionsManager) object);
    }

    public Server add(Context context, String host, int port, FtnAddress ftnAddress, boolean autoStart) throws HotdogedException {
        try {
            get(ftnAddress);
            throw new HotdogedException("TCP-server for address " + ftnAddress + " already running");
        } catch (HotdogedException e) {
            Server server = new Server(context, host, port, ftnAddress);
            add(server);
            Log.d(TAG, "TCP-server for address " + ftnAddress + " added.");
            if (autoStart) {
                server.start();
                Log.d(TAG, "TCP-server for address " + ftnAddress + " started.");
            }
            return server;
        }
    }

    public void stopServer(FtnAddress ftnAddress, boolean remove) throws HotdogedException {
        Server server = get(ftnAddress);
        if (server != null) {
            if (server.isAlive()) {
                server.needsStop = true;
                Log.d(TAG, "TCP-server stop request sent: " + ftnAddress);
            } else {
                Log.e(TAG, "TCP-server for address " + ftnAddress + " NOT running");
            }
            if (remove) {
                remove(server);
                Log.d(TAG, "TCP-server removed from pool: " + ftnAddress);
                return;
            }
            return;
        }
        Log.d(TAG, "TCP-server is null for address " + ftnAddress);
    }

    private Server get(FtnAddress ftnAddress) throws HotdogedException {
        Server server = new Server(null, null, 0, ftnAddress);
        int i = indexOf(server);
        if (i < 0) {
            throw new HotdogedException("TCP-server not found for address " + ftnAddress);
        }
        Server server2 = get(i);
        return server2;
    }
}
