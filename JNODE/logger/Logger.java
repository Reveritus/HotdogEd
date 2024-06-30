package jnode.logger;

import android.util.Log;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public final class Logger {
    private static final String LOG_FORMAT = "%s [%08d] %s %s";
    public static final int LOG_L1 = 1;
    public static final int LOG_L2 = 2;
    public static final int LOG_L3 = 3;
    public static final int LOG_L4 = 4;
    public static final int LOG_L5 = 5;
    private static ArrayList<String> eventArray;
    private String className;
    public static int Loglevel = 5;
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("HH:mm:ss");

    public static Logger getLogger(Class<?> clazz, ArrayList<String> eventArray2) {
        eventArray = eventArray2;
        String className = clazz.getSimpleName();
        StringBuilder b = new StringBuilder(20);
        b.append(className);
        for (int i = b.length(); i < 20; i++) {
            b.append(' ');
        }
        return new Logger(b.toString());
    }

    private Logger(String className) {
        this.className = className;
    }

    private void log(int _type, String log) {
        if (Loglevel >= _type) {
            String logString = String.format(LOG_FORMAT, DATE_FORMAT.format(new Date()), Long.valueOf(Thread.currentThread().getId()), this.className, log);
            Log.i(this.className, logString);
            if (eventArray != null) {
                synchronized (Logger.class) {
                    eventArray.add(logString);
                }
            }
        }
    }

    private String th2s(Throwable e) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(bos);
        e.printStackTrace(ps);
        ps.close();
        try {
            bos.close();
        } catch (IOException e2) {
        }
        return bos.toString();
    }

    public void log(String tag, String log) {
        l5(tag + ": " + log);
    }

    public void l5(String log) {
        log(5, log);
    }

    public void l4(String log) {
        log(4, log);
    }

    public void l3(String log) {
        log(3, log);
    }

    public void l2(String log) {
        log(2, log);
    }

    public void l1(String log) {
        log(1, log);
    }

    public void l5(String log, Throwable e) {
        log(1, log + ": " + th2s(e));
    }

    public void l4(String log, Throwable e) {
        log(1, log + ": " + th2s(e));
    }

    public void l3(String log, Throwable e) {
        log(1, log + ": " + th2s(e));
    }

    public void l2(String log, Throwable e) {
        log(1, log + ": " + th2s(e));
    }

    public void l1(String log, Throwable e) {
        log(1, log + ": " + th2s(e));
    }
}
