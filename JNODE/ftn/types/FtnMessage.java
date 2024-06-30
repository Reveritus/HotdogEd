package jnode.ftn.types;

import com.pushkin.charset.CharsetFabric;
import com.pushkin.ftn.Main;
import com.pushkin.hotdoged.export.HotdogedException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import jnode.ftn.FtnTools;
import jnode.ftn.exception.LastMessageException;

public class FtnMessage {
    public static final int ATTR_ARQ = 16384;
    public static final int ATTR_CRASH = 2;
    public static final int ATTR_FILEATT = 16;
    public static final int ATTR_FILEREQ = 2048;
    public static final int ATTR_FIUPRQ = 32768;
    public static final int ATTR_HOFOPICKUP = 512;
    public static final int ATTR_INTRANS = 32;
    public static final int ATTR_ISRR = 8192;
    public static final int ATTR_KILLSENT = 128;
    public static final int ATTR_LOCAL = 256;
    public static final int ATTR_ORPHAN = 64;
    public static final int ATTR_PVT = 1;
    public static final int ATTR_RECD = 4;
    public static final int ATTR_RRQ = 4096;
    public static final int ATTR_SEND = 8;
    private static DateFormat format = new SimpleDateFormat("dd MMM yy  HH:mm:ss", Locale.US);
    private String area;
    private int attribute;
    private String codePage;
    private Date date;
    private FtnAddress fromAddr;
    private String fromName;
    private boolean isNetmail;
    private String msgid;
    private String subject;
    private String text;
    private FtnAddress toAddr;
    private String toName;
    private List<Ftn2D> seenby = new ArrayList();
    private List<Ftn2D> path = new ArrayList();

    public String getFromName() {
        return this.fromName;
    }

    public void setFromName(String fromName) {
        this.fromName = fromName;
    }

    public String getToName() {
        return this.toName;
    }

    public void setToName(String toName) {
        this.toName = toName;
    }

    public FtnAddress getFromAddr() {
        return this.fromAddr;
    }

    public void setFromAddr(FtnAddress fromAddr) {
        this.fromAddr = fromAddr;
    }

    public FtnAddress getToAddr() {
        return this.toAddr;
    }

    public void setToAddr(FtnAddress toAddr) {
        this.toAddr = toAddr;
    }

    public String getArea() {
        return this.area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getSubject() {
        return this.subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getText() {
        return this.text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Date getDate() {
        return this.date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public List<Ftn2D> getSeenby() {
        return this.seenby;
    }

    public void setSeenby(List<Ftn2D> seenby) {
        this.seenby = seenby;
    }

    public List<Ftn2D> getPath() {
        return this.path;
    }

    public void setPath(List<Ftn2D> path) {
        this.path = path;
    }

    public String getMsgid() {
        return this.msgid;
    }

    public boolean isNetmail() {
        return this.isNetmail;
    }

    public void setNetmail(boolean isNetmail) {
        this.isNetmail = isNetmail;
    }

    public int getAttribute() {
        return this.attribute;
    }

    public void setAttribute(int attribute) {
        this.attribute = attribute;
    }

    public byte[] pack() throws HotdogedException {
        byte[] bytes;
        if (this.codePage == null) {
            throw new HotdogedException("Codepage for message " + this.area + ":" + this.msgid + " not specified.");
        }
        FtnTools.codePage = this.codePage;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream os = new DataOutputStream(bos);
        try {
            os.write(new byte[]{2, 0});
            os.writeShort(FtnTools.revShort(this.fromAddr.getNode()));
            os.writeShort(FtnTools.revShort(this.toAddr.getNode()));
            os.writeShort(FtnTools.revShort(this.fromAddr.getNet()));
            os.writeShort(FtnTools.revShort(this.toAddr.getNet()));
            if (this.isNetmail) {
                os.writeShort(FtnTools.revShort((short) this.attribute));
            } else {
                os.write(new byte[]{0, 0});
            }
            os.write(new byte[]{0, 0});
            os.write(FtnTools.substr(format.format(this.date), 19));
            os.write(0);
            os.write(FtnTools.substr(this.toName, 35));
            os.write(0);
            os.write(FtnTools.substr(this.fromName, 35));
            os.write(0);
            os.write(FtnTools.substr(this.subject, 71));
            os.write(0);
            if (!this.isNetmail) {
                os.writeBytes(String.format("AREA:%s\r", this.area));
            } else {
                os.writeBytes(String.format("\u0001INTL %s %s\r", this.toAddr.intl(), this.fromAddr.intl()));
                os.writeBytes(this.fromAddr.fmpt());
                os.writeBytes(this.toAddr.topt());
            }
            StringBuilder sb = new StringBuilder();
            sb.append(this.text);
            if (sb.charAt(sb.length() - 1) != '\n') {
                sb.append('\n');
            }
            if (!this.isNetmail) {
                sb.append(FtnTools.writeSeenBy(this.seenby));
                sb.append(FtnTools.writePath(this.path));
            }
            String s = null;
            try {
                s = sb.toString().replaceAll("\n", "\r");
                bytes = s.getBytes(this.codePage);
            } catch (UnsupportedEncodingException e) {
                try {
                    bytes = CharsetFabric.getCharset(this.codePage).toByteArray(s);
                } catch (UnsupportedEncodingException e1) {
                    Main.SystemInfo.getLogger().l5(e1.getMessage());
                    try {
                        bytes = s.getBytes("LATIN-1");
                    } catch (UnsupportedEncodingException e2) {
                        throw new HotdogedException(e2);
                    }
                }
            }
            os.write(bytes);
            os.write(0);
            os.close();
        } catch (IOException e3) {
        }
        return bos.toByteArray();
    }

    public void unpack(byte[] data) throws LastMessageException, HotdogedException {
        try {
            InputStream is = new ByteArrayInputStream(data);
            unpack(is);
            is.close();
        } catch (IOException e) {
        }
    }

    public void unpack(InputStream iz) throws LastMessageException, HotdogedException {
        DataInputStream is = new DataInputStream(iz);
        FtnTools.codePage = Main.info.getLinkPreferredCodePage();
        this.fromAddr = new FtnAddress();
        this.toAddr = new FtnAddress();
        try {
            try {
                try {
                    try {
                        if (is.read() == 2 && is.read() == 0) {
                            this.fromAddr.setNode(FtnTools.revShort(is.readShort()));
                            this.toAddr.setNode(FtnTools.revShort(is.readShort()));
                            this.fromAddr.setNet(FtnTools.revShort(is.readShort()));
                            this.toAddr.setNet(FtnTools.revShort(is.readShort()));
                            this.attribute = FtnTools.revShort(is.readShort());
                            is.skip(2L);
                            this.date = format.parse(FtnTools.readUntillNull(is));
                            this.toName = FtnTools.readUntillNull(is);
                            this.fromName = FtnTools.readUntillNull(is);
                            this.subject = FtnTools.readUntillNull(is);
                            String[] lines = FtnTools.readUntillNull(is).replaceAll("\n", "").split("\r");
                            StringBuilder builder = new StringBuilder();
                            int linenum = 0;
                            boolean eofKluges = false;
                            boolean preOrigin = false;
                            boolean afterOrigin = false;
                            Pattern netmail = Pattern.compile("^\u0001(INTL|FMPT|TOPT) (.*)$");
                            Pattern origin = Pattern.compile("^ \\* Origin: ([\\S\\t ]*)$");
                            Pattern msgid = Pattern.compile("^\u0001MSGID: (.*)$");
                            Pattern tzutc = Pattern.compile("^\u0001TZUTC: (\\d+)$");
                            Pattern chrs = Pattern.compile("^\u0001CHRS: (.*)$");
                            StringBuilder seenby = new StringBuilder();
                            StringBuilder path = new StringBuilder();
                            for (String line : lines) {
                                linenum++;
                                if (linenum == 1) {
                                    if (line.matches("^AREA:\\S+$")) {
                                        this.isNetmail = false;
                                        this.area = line.replaceFirst("^AREA:", "");
                                    } else {
                                        this.isNetmail = true;
                                    }
                                }
                                if (!eofKluges && linenum > 1 && !line.matches("^\u0001.*$")) {
                                    eofKluges = true;
                                }
                                if (!eofKluges) {
                                    Matcher m = msgid.matcher(line);
                                    if (m.matches()) {
                                        this.msgid = m.group(1).toUpperCase();
                                    }
                                    Matcher m2 = chrs.matcher(line);
                                    if (m2.matches()) {
                                        this.codePage = FtnTools.chrs2codepage(m2.group(1).toUpperCase());
                                        if (!this.codePage.equalsIgnoreCase(Main.info.getLinkPreferredCodePage())) {
                                            Main.SystemInfo.getLogger().l5("Codepage in message " + this.codePage + " != link codepage " + Main.info.getLinkPreferredCodePage());
                                        }
                                    }
                                    tzutc.matcher(line);
                                    if (this.isNetmail) {
                                        Matcher m3 = netmail.matcher(line);
                                        if (m3.matches()) {
                                            String kluge = m3.group(1);
                                            String arg = m3.group(2);
                                            if (kluge.equals("INTL")) {
                                                String[] tmp = arg.split(" ");
                                                this.toAddr = new FtnAddress(tmp[0]);
                                                this.fromAddr = new FtnAddress(tmp[1]);
                                            } else if (kluge.equals("TOPT")) {
                                                this.toAddr.setPoint(new Integer(arg).intValue());
                                            } else if (kluge.equals("FMPT")) {
                                                this.fromAddr.setPoint(new Integer(arg).intValue());
                                            }
                                        }
                                    }
                                    builder.append(line);
                                    builder.append('\n');
                                } else if (preOrigin && !this.isNetmail) {
                                    if (line.startsWith("SEEN-BY: ")) {
                                        afterOrigin = true;
                                    } else {
                                        preOrigin = false;
                                    }
                                    if (afterOrigin) {
                                        if (line.startsWith("SEEN-BY: ")) {
                                            seenby.append(line);
                                            seenby.append('\n');
                                        } else if (line.startsWith("\u0001PATH: ")) {
                                            path.append(line);
                                            path.append('\n');
                                        }
                                    }
                                } else {
                                    if (!this.isNetmail) {
                                        Matcher m4 = origin.matcher(line);
                                        if (m4.matches()) {
                                            Pattern f = Pattern.compile("([1-5]?:?\\d{1,5}/\\d{1,5}(\\.\\d{1,5})?)");
                                            preOrigin = true;
                                            String orig = m4.group(1);
                                            Matcher fm = f.matcher(orig);
                                            String ftnAddr = "";
                                            while (fm.find()) {
                                                ftnAddr = fm.group(1);
                                            }
                                            this.fromAddr = new FtnAddress(ftnAddr);
                                        }
                                    }
                                    builder.append(line);
                                    builder.append('\n');
                                }
                            }
                            this.seenby = FtnTools.readSeenBy(seenby.toString());
                            this.path = FtnTools.readPath(path.toString());
                            this.text = builder.toString();
                            return;
                        }
                        throw new LastMessageException("2.0 is not out version");
                    } catch (IOException e) {
                        throw new LastMessageException(e);
                    }
                } catch (LastMessageException e2) {
                    throw new LastMessageException(e2);
                }
            } catch (ParseException e3) {
                throw new LastMessageException(e3);
            }
        } finally {
            FtnTools.codePage = null;
        }
    }

    public String toString() {
        return String.format("Message %s -> %s\nFrom: %s\nTo: %s\nDate: %s\nSubject: %s\nArea: %s\n-------------\n%s\n-------------\n", this.fromAddr, this.toAddr, this.fromName, this.toName, this.date, this.subject, this.area, this.text);
    }

    public String getCodePage() {
        return this.codePage;
    }

    public void setCodePage(String codePage) {
        this.codePage = codePage;
    }

    public synchronized void setMsgid(String msgid) {
        this.msgid = msgid;
    }
}
