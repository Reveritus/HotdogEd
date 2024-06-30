package jnode.protocol.binkp;

import android.support.v4.internal.view.SupportMenu;
import ch.boye.httpclientandroidlib.cookie.ClientCookie;
import com.pushkin.ftn.Link;
import com.pushkin.ftn.Main;
import com.pushkin.hotdoged.export.HotdogedException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import jnode.event.FileReceivingEvent;
import jnode.event.FileSendingEvent;
import jnode.event.Notifier;
import jnode.logger.Logger;
import jnode.protocol.io.Connector;
import jnode.protocol.io.Frame;
import jnode.protocol.io.Message;
import jnode.protocol.io.ProtocolConnector;
import org.apache.commons.codec.digest.MessageDigestAlgorithms;

public class BinkpConnector implements ProtocolConnector {
    private static final int STATE_END = 16;
    private static final int STATE_ERR = 32;
    private static final int STATE_TRANSFER = 8;
    private static final int STATE_WAITADDR = 1;
    private static final int STATE_WAITOK = 2;
    private static final int STATE_WAITPWD = 4;
    private static final DateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.US);
    private static final Logger logger = Logger.getLogger(BinkpConnector.class, Main.SystemInfo.getEventsArray());
    private boolean binkp1;
    private int connectionState;
    private Connector connector;
    private String cramText;
    private Message currentMessage;
    private int currentMessageBytesLeft;
    private long currentMessageTimestamp;
    private OutputStream currentOutputStream;
    private File currentTempFile;
    private List<Frame> frames;
    private boolean leob;
    private Link link;
    private String password;
    private boolean recv;
    private boolean recvfile;
    private boolean reob;
    private boolean secure;
    private boolean send;
    private boolean sendfile;
    private List<Message> sent;
    private int totalin;
    private int totalout;
    private boolean useCram = false;
    private String cramAlgo = "";
    private boolean incoming = false;

    @Override // jnode.protocol.io.ProtocolConnector
    public void reset() {
        this.sent = new ArrayList();
        this.frames = new ArrayList();
        this.useCram = false;
        this.connector = null;
        this.link = null;
        this.totalin = 0;
        this.totalout = 0;
        this.connectionState = 1;
        this.recvfile = false;
        this.sendfile = false;
        this.reob = false;
        this.leob = false;
        this.binkp1 = false;
        this.recv = false;
        this.send = false;
        this.secure = true;
    }

    private void greet() {
        this.frames.add(new BinkpFrame(BinkpCommand.M_NUL, "SYS " + Main.info.getStationName()));
        this.frames.add(new BinkpFrame(BinkpCommand.M_NUL, "ZYZ " + Main.info.getSysop()));
        this.frames.add(new BinkpFrame(BinkpCommand.M_NUL, "LOC " + Main.info.getLocation()));
        this.frames.add(new BinkpFrame(BinkpCommand.M_NUL, "NDL " + Main.info.getNDL()));
        this.frames.add(new BinkpFrame(BinkpCommand.M_NUL, "VER " + Main.info.getVersion() + " binkp/1.1"));
        this.frames.add(new BinkpFrame(BinkpCommand.M_NUL, "TIME " + format.format(new Date())));
        this.frames.add(new BinkpFrame(BinkpCommand.M_ADR, Main.info.getAddress().toString() + Main.info.getDomain()));
    }

    private void error(String err) throws HotdogedException {
        logger.l3("lerror: " + err);
        this.frames.add(new BinkpFrame(BinkpCommand.M_ERR, err));
        this.connectionState = 32;
        throw new HotdogedException(err);
    }

    @Override // jnode.protocol.io.ProtocolConnector
    public void initOutgoing(Connector connector) {
        this.connector = connector;
        this.incoming = false;
        greet();
    }

    @Override // jnode.protocol.io.ProtocolConnector
    public void initIncoming(Connector connector) {
        this.connector = connector;
        this.incoming = true;
        try {
            MessageDigest md = MessageDigest.getInstance(MessageDigestAlgorithms.MD5);
            md.update(String.format("%d%d", Long.valueOf(System.currentTimeMillis()), Long.valueOf(System.nanoTime())).getBytes());
            byte[] digest = md.digest();
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < 16; i++) {
                builder.append(String.format("%02x", Byte.valueOf(digest[i])));
            }
            this.cramText = builder.toString();
            this.cramAlgo = MessageDigestAlgorithms.MD5;
            this.frames.add(new BinkpFrame(BinkpCommand.M_NUL, String.format("OPT CRAM-MD5-%s", this.cramText)));
        } catch (NoSuchAlgorithmException e) {
            this.cramText = null;
        }
        greet();
    }

    private BinkpCommand getCommand(int command) {
        for (BinkpCommand c : BinkpCommand.values()) {
            if (c.getCmd() == command) {
                return c;
            }
        }
        return null;
    }

    private BinkpFrame recv(InputStream in) {
        try {
            if (in.available() <= 2) {
                return null;
            }
            int hw = in.read();
            int lw = in.read();
            boolean command = false;
            if ((hw >> 7) == 1) {
                command = true;
            }
            int len = (((hw << 9) | (lw << 1)) & SupportMenu.USER_MASK) >> 1;
            if (len <= 0) {
                return null;
            }
            if (command) {
                int cmd = in.read();
                String arg = null;
                if (len > 1) {
                    byte[] data = new byte[len - 1];
                    in.read(data);
                    if (data[data.length - 1] == 0) {
                        byte[] datawonull = new byte[data.length - 1];
                        for (int i = 0; i < data.length - 1; i++) {
                            datawonull[i] = data[i];
                        }
                        arg = new String(datawonull);
                    } else {
                        arg = new String(data);
                    }
                }
                BinkpFrame ret = new BinkpFrame(getCommand(cmd), arg);
                return ret;
            }
            byte[] data2 = new byte[len];
            int i2 = 0;
            while (i2 < len) {
                i2 += in.read(data2, i2, len - i2);
            }
            BinkpFrame ret2 = new BinkpFrame(data2);
            return ret2;
        } catch (IOException e) {
            logger.l2("Frame receiving error", e);
            return null;
        }
    }

    @Override // jnode.protocol.io.ProtocolConnector
    public void avalible(InputStream is) throws HotdogedException {
        InputStream iz;
        String str;
        if (!Main.info.needsStop) {
            Pattern cram = Pattern.compile("^CRAM-([-A-Z0-9]+)-([a-f0-9]+)$");
            BinkpFrame frame = recv(is);
            if (frame != null) {
                logger.l5("Received frame: " + frame.toString());
                if (this.connectionState < 8 && !frame.isCommand()) {
                    error("Unknown frame");
                } else if (frame.isCommand() && frame.getCommand().equals(BinkpCommand.M_ERR)) {
                    error(new String(frame.getData()));
                } else if (frame.isCommand() && frame.getCommand().equals(BinkpCommand.M_BSY)) {
                    logger.l3("Remote is busy");
                    this.connectionState = 16;
                } else if (this.connectionState == 1) {
                    if (frame.getCommand().equals(BinkpCommand.M_NUL)) {
                        logger.l4(frame.getArg());
                        String[] args = frame.getArg().split(" ");
                        if (args[0].equals("OPT")) {
                            for (int i = 1; i < args.length; i++) {
                                Matcher md = cram.matcher(args[i]);
                                if (md.matches()) {
                                    String[] algos = md.group(1).split("/");
                                    for (String algo : algos) {
                                        try {
                                            MessageDigest.getInstance(algo);
                                            this.useCram = true;
                                            this.cramText = md.group(2);
                                            this.cramAlgo = md.group(1);
                                            logger.l4("Remote requires MD-mode (" + algo + ")");
                                            break;
                                        } catch (NoSuchAlgorithmException e) {
                                        }
                                    }
                                    if (!this.useCram) {
                                        logger.l4("Remote requires MD-mode for unknown algo");
                                    }
                                }
                            }
                        } else if (args[0].equals("VER")) {
                            if (frame.getArg().matches("^.* binkp/1\\.1$")) {
                                this.binkp1 = true;
                                logger.l4("Protocol version 1.1");
                                return;
                            }
                            this.binkp1 = false;
                            logger.l4("Protocol version 1.0");
                        }
                    } else if (frame.getCommand().equals(BinkpCommand.M_ADR)) {
                        logger.l4(frame.getArg());
                        boolean authorized = false;
                        if (frame.getArg() != null) {
                            Pattern ftn = Pattern.compile("([^\\S]*([1-5]:\\d{1,5}/\\d{1,5}\\.?\\d{0,5})(@fido[a-z]*)?)", 2);
                            Matcher m = ftn.matcher(frame.getArg());
                            while (m.find()) {
                                String sFtn = m.group(2);
                                try {
                                    this.link = new Link(sFtn);
                                    authorized = true;
                                    this.secure = true;
                                    break;
                                } catch (HotdogedException e2) {
                                    logger.l2("HotdogedException", e2);
                                }
                            }
                            if (!authorized) {
                                error("Not authorized");
                            }
                            if (authorized) {
                                if (this.secure) {
                                    str = this.link.getProtocolPassword() != null ? this.link.getProtocolPassword() : "-";
                                } else {
                                    str = "-";
                                }
                                this.password = str;
                                if (!this.incoming) {
                                    this.frames.add(new BinkpFrame(BinkpCommand.M_PWD, getPassword()));
                                }
                                this.connectionState = this.incoming ? 4 : 2;
                                return;
                            }
                            error("unknown m_addr");
                            return;
                        }
                        error("Bad M_ADDR command");
                    }
                } else if (this.connectionState == 2) {
                    if (frame.getCommand().equals(BinkpCommand.M_OK)) {
                        if (!this.password.equals("-")) {
                            logger.l4("(C) Secure session (" + (this.useCram ? "cram" : "plain") + ")");
                        } else {
                            logger.l4("(C) Unsecure session");
                        }
                        this.connector.setLink(this.link);
                        this.connectionState = 8;
                    } else if (frame.getCommand().equals(BinkpCommand.M_NUL)) {
                        logger.l5(frame.getArg());
                    } else {
                        error("Unknown frame ok");
                    }
                } else if (this.connectionState == 4) {
                    if (frame.getCommand().equals(BinkpCommand.M_PWD)) {
                        boolean auth = false;
                        if (frame.getArg() != null) {
                            if (this.secure) {
                                String pw = frame.getArg();
                                if (pw.matches("^CRAM-" + this.cramAlgo + "-.*")) {
                                    this.useCram = true;
                                    if (getPassword().equals(pw)) {
                                        auth = true;
                                    }
                                } else if (pw.equals(this.password)) {
                                    auth = true;
                                }
                            } else {
                                auth = true;
                            }
                        }
                        if (auth) {
                            this.frames.add(new BinkpFrame(BinkpCommand.M_OK, this.password.equals("-") ? "insecure" : ClientCookie.SECURE_ATTR));
                            if (!this.password.equals("-")) {
                                logger.l4("(S) Secure session (" + (this.useCram ? "cram" : "plain") + ")");
                            } else {
                                logger.l4("(S) Unsecure session");
                            }
                            if (this.secure) {
                                this.connector.setLink(this.link);
                            }
                            this.connectionState = 8;
                            return;
                        }
                        error("Bad pwd");
                        throw new HotdogedException("Bad password for link " + this.link.getLinkAddress());
                    } else if (frame.getCommand().equals(BinkpCommand.M_NUL)) {
                        logger.l5(frame.getArg());
                    } else {
                        logger.l5("(OK) Unknown frame " + frame.toString());
                    }
                } else if (this.connectionState == 8) {
                    if (frame.isCommand()) {
                        String arg = frame.getArg();
                        if (frame.getCommand().equals(BinkpCommand.M_FILE)) {
                            Pattern[] p = {Pattern.compile("^(\\S+) (\\d+) (\\d+) 0$"), Pattern.compile("^(\\S+ \\d+ \\d+) -1$")};
                            Matcher m2 = p[1].matcher(arg);
                            if (m2.matches()) {
                                this.frames.add(new BinkpFrame(BinkpCommand.M_GET, m2.group(1) + " 0"));
                                return;
                            }
                            Matcher m3 = p[0].matcher(arg);
                            if (m3.matches()) {
                                this.currentMessage = new Message(m3.group(1), new Integer(m3.group(2)).intValue());
                                this.currentMessageTimestamp = new Long(m3.group(3)).longValue();
                                this.currentMessageBytesLeft = (int) this.currentMessage.getMessageLength();
                                try {
                                    this.currentTempFile = Main.SystemInfo.createTempFile(this.currentMessage.getMessageName());
                                    this.currentOutputStream = new FileOutputStream(this.currentTempFile);
                                    logger.l5("Receiving to tempfile " + this.currentTempFile.getAbsolutePath());
                                } catch (HotdogedException e3) {
                                    Main.SystemInfo.getLogger().log("avalible", "Temp file creation failed. " + e3.getMessage());
                                    e3.printStackTrace();
                                } catch (IOException e4) {
                                    this.currentTempFile = null;
                                    this.currentOutputStream = new ByteArrayOutputStream(this.currentMessageBytesLeft);
                                    Main.SystemInfo.getLogger().log("avalible", "Temp file creation failed. " + e4.getMessage());
                                }
                                this.recvfile = true;
                                logger.l3(String.format("Receiving file: %s (%d)", this.currentMessage.getMessageName(), Long.valueOf(this.currentMessage.getMessageLength())));
                                Notifier.INSTANSE.notify(new FileReceivingEvent(this.currentMessage.getMessageName(), this.currentMessage.getMessageLength()));
                            }
                        } else if (frame.getCommand().equals(BinkpCommand.M_EOB)) {
                            this.reob = true;
                        } else if (frame.getCommand().equals(BinkpCommand.M_GOT) && this.sendfile) {
                            Matcher m4 = Pattern.compile("^(\\S+) (\\d+) (\\d+)$").matcher(frame.getArg());
                            if (m4.matches()) {
                                String messageName = m4.group(1);
                                int len = Integer.valueOf(m4.group(2)).intValue();
                                logger.l3(String.format("Sent file: %s (%d)", messageName, Integer.valueOf(len)));
                                this.totalout += len;
                                this.sendfile = false;
                                this.send = true;
                                for (Message mess : this.sent) {
                                    if (mess.getMessageName().equals(messageName)) {
                                        mess.delete();
                                        return;
                                    }
                                }
                            }
                        } else {
                            logger.l4("(TRANSFER) Unknown frame " + frame.toString());
                        }
                    } else if (this.recvfile) {
                        byte[] data = frame.getData();
                        int len2 = data.length;
                        try {
                            if (this.currentMessageBytesLeft >= len2) {
                                this.currentOutputStream.write(data);
                                this.currentMessageBytesLeft -= len2;
                            } else {
                                this.currentOutputStream.write(data, 0, this.currentMessageBytesLeft);
                                this.currentMessageBytesLeft = 0;
                            }
                            if (this.currentMessageBytesLeft == 0) {
                                this.currentOutputStream.close();
                                if (this.currentTempFile != null) {
                                    iz = new FileInputStream(this.currentTempFile);
                                    this.currentTempFile.delete();
                                } else {
                                    ByteArrayOutputStream bos = (ByteArrayOutputStream) this.currentOutputStream;
                                    iz = new ByteArrayInputStream(bos.toByteArray());
                                }
                                this.currentOutputStream = null;
                                this.currentTempFile = null;
                                this.currentMessage.setInputStream(iz);
                                Frame m_got = new BinkpFrame(BinkpCommand.M_GOT, String.format("%s %d %d", this.currentMessage.getMessageName(), Long.valueOf(this.currentMessage.getMessageLength()), Long.valueOf(this.currentMessageTimestamp)));
                                Frame m_skip = new BinkpFrame(BinkpCommand.M_SKIP, String.format("%s %d %d", this.currentMessage.getMessageName(), Long.valueOf(this.currentMessage.getMessageLength()), Long.valueOf(this.currentMessageTimestamp)));
                                logger.l3(String.format("Received file: %s (%d)", this.currentMessage.getMessageName(), Long.valueOf(this.currentMessage.getMessageLength())));
                                this.currentMessage.setSecure(this.secure);
                                if (this.connector.onReceived(this.currentMessage) == 0) {
                                    this.frames.add(m_got);
                                } else {
                                    logger.l4("Tossing failed, sending m_skip");
                                    this.frames.add(m_skip);
                                }
                                this.totalin = (int) (this.totalin + this.currentMessage.getMessageLength());
                                this.currentMessage = null;
                                this.recvfile = false;
                                this.recv = true;
                            }
                        } catch (IOException e5) {
                            error("recv error " + e5.getMessage());
                        }
                    }
                }
            }
        }
    }

    @Override // jnode.protocol.io.ProtocolConnector
    public Frame[] getFrames() {
        Frame[] frames = (Frame[]) this.frames.toArray(new Frame[0]);
        this.frames.clear();
        return frames;
    }

    @Override // jnode.protocol.io.ProtocolConnector
    public boolean canSend() {
        return (this.connectionState != 8 || this.leob || this.sendfile) ? false : true;
    }

    @Override // jnode.protocol.io.ProtocolConnector
    public boolean closed() {
        if (this.reob && this.leob && this.connectionState == 8) {
            if ((this.recv || this.send) && this.binkp1) {
                this.leob = false;
                this.reob = false;
                this.recv = false;
                this.send = false;
                if (this.secure) {
                    logger.l4("Restarting transfer");
                    this.connector.setLink(this.link);
                }
            } else {
                this.connectionState = 16;
            }
        }
        if (this.connectionState == 32) {
            if (this.link != null) {
                logger.l3(String.format("Done with errors, Sb/Rb: %d/%d (%s)", Integer.valueOf(this.totalout), Integer.valueOf(this.totalin), this.link.getLinkAddress()));
                return true;
            }
            logger.l3("Done with errors");
            return true;
        } else if (this.connectionState == 16) {
            logger.l3(String.format("Done, Sb/Rb: %d/%d (%s)", Integer.valueOf(this.totalout), Integer.valueOf(this.totalin), this.link.getLinkAddress()));
            return true;
        } else {
            return false;
        }
    }

    @Override // jnode.protocol.io.ProtocolConnector
    public void eob() {
        if (!this.sendfile) {
            this.leob = true;
            this.frames.add(new BinkpFrame(BinkpCommand.M_EOB));
        }
    }

    @Override // jnode.protocol.io.ProtocolConnector
    public void send(Message message) {
        byte[] buf;
        this.sent.add(message);
        this.sendfile = true;
        Notifier.INSTANSE.notify(new FileSendingEvent(message.getMessageName(), message.getMessageLength()));
        this.frames.add(new BinkpFrame(BinkpCommand.M_FILE, String.format("%s %d %d 0", message.getMessageName(), Long.valueOf(message.getMessageLength()), Long.valueOf(System.currentTimeMillis() / 1000))));
        logger.l3(String.format("Sending file: %s (%d)", message.getMessageName(), Long.valueOf(message.getMessageLength())));
        while (true) {
            try {
                int avalible = message.getInputStream().available();
                if (avalible > 0) {
                    if (avalible > 32767) {
                        buf = new byte[32767];
                    } else {
                        buf = new byte[avalible];
                    }
                    message.getInputStream().read(buf);
                    this.frames.add(new BinkpFrame(buf));
                } else {
                    return;
                }
            } catch (IOException e) {
                logger.l1("Send error", e);
                return;
            }
        }
    }

    private static byte hex2decimal(String s) {
        String s2 = s.toUpperCase();
        byte val = 0;
        for (int i = 0; i < s2.length(); i++) {
            char c = s2.charAt(i);
            int d = "0123456789ABCDEF".indexOf(c);
            val = (byte) ((val * 16) + d);
        }
        return val;
    }

    private String getPassword() {
        if (this.password.equals("-") || !this.useCram) {
            return this.password;
        }
        try {
            MessageDigest md = MessageDigest.getInstance(this.cramAlgo);
            byte[] text = new byte[this.cramText.length() / 2];
            byte[] key = this.password.getBytes();
            byte[] k_ipad = new byte[64];
            byte[] k_opad = new byte[64];
            for (int i = 0; i < this.cramText.length(); i += 2) {
                text[i / 2] = hex2decimal(this.cramText.substring(i, i + 2));
            }
            for (int i2 = 0; i2 < key.length; i2++) {
                k_ipad[i2] = key[i2];
                k_opad[i2] = key[i2];
            }
            for (int i3 = 0; i3 < 64; i3++) {
                k_ipad[i3] = (byte) (k_ipad[i3] ^ 54);
                k_opad[i3] = (byte) (k_opad[i3] ^ 92);
            }
            md.update(k_ipad);
            md.update(text);
            byte[] digest = md.digest();
            md.update(k_opad);
            md.update(digest);
            byte[] digest2 = md.digest();
            StringBuilder builder = new StringBuilder();
            builder.append("CRAM-" + this.cramAlgo + "-");
            for (int i4 = 0; i4 < 16; i4++) {
                builder.append(String.format("%02x", Byte.valueOf(digest2[i4])));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException e) {
            return this.password;
        }
    }

    @Override // jnode.protocol.io.ProtocolConnector
    public boolean getIncoming() {
        return this.incoming;
    }

    @Override // jnode.protocol.io.ProtocolConnector
    public boolean getSuccess() {
        return this.connectionState != 32;
    }

    @Override // jnode.protocol.io.ProtocolConnector
    public int getBytesReceived() {
        return this.totalin;
    }

    @Override // jnode.protocol.io.ProtocolConnector
    public int getBytesSent() {
        return this.totalout;
    }
}
