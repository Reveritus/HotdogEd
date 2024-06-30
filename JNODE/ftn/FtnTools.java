package jnode.ftn;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.util.Log;
import ch.boye.httpclientandroidlib.protocol.HTTP;
import com.pushkin.charset.CharsetFabric;
import com.pushkin.ftn.Echoarea;
import com.pushkin.ftn.Echomail;
import com.pushkin.ftn.Link;
import com.pushkin.ftn.LinkOption;
import com.pushkin.ftn.Main;
import com.pushkin.ftn.Netmail;
import com.pushkin.ftn.Route;
import com.pushkin.ftn.Subscription;
import com.pushkin.hotdoged.export.HotdogedException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import jnode.event.NewEchoareaEvent;
import jnode.event.Notifier;
import jnode.ftn.types.Ftn2D;
import jnode.ftn.types.FtnAddress;
import jnode.ftn.types.FtnMessage;
import jnode.ftn.types.FtnPkt;
import jnode.logger.Logger;
import jnode.protocol.io.Message;

@SuppressLint({"DefaultLocale"})
public final class FtnTools {
    public static final String ARCMAIL_REGEX = "^.+\\.(mo|tu|we|th|fr|sa|su)[0-9a-z]$";
    public static final String DEFAULT_FTN_CHRS = "CP866 2";
    public static final String LO_REGEX = "^[0-9a-f]{8}\\..?lo$";
    private static final String PATH = "\u0001PATH:";
    public static final String PKT_REGEX = "^.+\\.pkt$";
    private static final String SEEN_BY = "SEEN-BY:";
    private static final String TAG = "FtnTools";
    public static String codePage;
    public static String DEFAULT_FTN_CHARSET = "CP866";
    private static final String ROUTE_VIA = "\u0001Via %s " + Main.info.getVersion() + " %s";
    public static final DateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.US);
    private static final Logger logger = Logger.getLogger(FtnTools.class, Main.SystemInfo.getEventsArray());

    public static class Ftn2DComparator implements Comparator<Ftn2D> {
        @Override // java.util.Comparator
        public int compare(Ftn2D o1, Ftn2D o2) {
            return o1.getNet() == o2.getNet() ? o1.getNode() - o2.getNode() : o1.getNet() - o2.getNet();
        }
    }

    public static class Ftn4DComparator implements Comparator<FtnAddress> {
        @Override // java.util.Comparator
        public int compare(FtnAddress o1, FtnAddress o2) {
            if (o1.getZone() == o2.getZone()) {
                if (o1.getNet() == o2.getNet()) {
                    if (o1.getNode() == o2.getNode()) {
                        if (o1.getPoint() == o2.getPoint()) {
                            return 0;
                        }
                        return o1.getPoint() - o2.getPoint();
                    }
                    return o1.getNode() - o2.getNode();
                }
                return o1.getNet() - o2.getNet();
            }
            return o1.getZone() - o2.getZone();
        }
    }

    public static String generate8d() {
        byte[] digest = new byte[4];
        for (int i = 0; i < 4; i++) {
            long a = Math.round(2.147483647E9d * Math.random());
            long b = Math.round((-2.147483648E9d) * Math.random());
            long c = a ^ b;
            byte d = (byte) ((c >> 12) & 255);
            digest[i] = d;
        }
        return String.format("%02x%02x%02x%02x", Byte.valueOf(digest[0]), Byte.valueOf(digest[1]), Byte.valueOf(digest[2]), Byte.valueOf(digest[3]));
    }

    public static String generateTic() {
        char[] chars = {'a', 'b', 'c', 'd', 'e', 'f', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};
        StringBuilder sb = new StringBuilder(11);
        sb.append("jt");
        for (int i = 0; i < 6; i++) {
            sb.append(chars[(int) Math.round(((double) (chars.length - 1)) * Math.random())]);
        }
        sb.append(".tic");
        return sb.toString();
    }

    public static short revShort(short v) {
        return (short) (((short) (((short) (v >> 8)) & 255)) | ((short) (v << 8)));
    }

    public static byte[] substr(String s, int len) throws HotdogedException {
        byte[] bytes;
        if (codePage == null) {
            throw new HotdogedException("[substr] Codepage not specified.");
        }
        try {
            bytes = s.getBytes(codePage);
        } catch (UnsupportedEncodingException e) {
            try {
                bytes = CharsetFabric.getCharset(codePage).toByteArray(s);
            } catch (UnsupportedEncodingException e1) {
                logger.l5(e1.getMessage());
                try {
                    bytes = s.getBytes("LATIN-1");
                } catch (UnsupportedEncodingException e2) {
                    throw new HotdogedException(e2);
                }
            }
        }
        if (bytes.length > len) {
            return ByteBuffer.wrap(bytes, 0, len).array();
        }
        return bytes;
    }

    public static String readUntillNull(InputStream is) throws HotdogedException {
        if (codePage == null) {
            throw new HotdogedException("[readUntillNull] Codepage not specified.");
        }
        ByteArrayOutputStream bos = new ByteArrayOutputStream(1);
        long len = 0;
        while (true) {
            try {
                int b = is.read();
                if (b != 0) {
                    bos.write(b);
                    len++;
                }
            } catch (IOException e) {
                Log.d(TAG, "Caugth IO exception: " + e.getMessage());
            }
            try {
                String s = new String(bos.toByteArray(), codePage);
                return s;
            } catch (UnsupportedEncodingException e2) {
                try {
                    String s2 = CharsetFabric.getCharset(codePage).toString(bos.toByteArray());
                    return s2;
                } catch (UnsupportedEncodingException e3) {
                    try {
                        String s3 = new String(bos.toByteArray(), "LATIN-1");
                        return s3;
                    } catch (UnsupportedEncodingException e22) {
                        throw new HotdogedException(e22);
                    }
                }
            }
        }
    }

    public static List<Ftn2D> readSeenBy(String seenByLines) {
        int node;
        List<Ftn2D> seen = new ArrayList<>();
        String[] seenBy = seenByLines.split("[ \n]");
        int net = 0;
        for (String parts : seenBy) {
            if (parts != null && parts.length() >= 1 && !parts.equals(SEEN_BY)) {
                String[] part = parts.split("/");
                if (part.length == 2) {
                    net = Integer.valueOf(part[0]).intValue();
                    node = Integer.valueOf(part[1]).intValue();
                } else {
                    node = Integer.valueOf(part[0]).intValue();
                }
                seen.add(new Ftn2D(net, node));
            }
        }
        return seen;
    }

    public static String writeSeenBy(List<Ftn2D> seenby) {
        logger.l5("WriteSeenBy: " + seenby);
        StringBuilder ret = new StringBuilder();
        Collections.sort(seenby, new Ftn2DComparator());
        int net = 0;
        int linelen = 0;
        for (Ftn2D ftn : seenby) {
            if (linelen >= 72) {
                linelen = 0;
                net = 0;
                ret.append("\n");
            }
            if (linelen == 0) {
                ret.append(SEEN_BY);
                linelen += SEEN_BY.length();
            }
            if (net != ftn.getNet()) {
                net = ftn.getNet();
                String app = String.format(" %d/%d", Integer.valueOf(ftn.getNet()), Integer.valueOf(ftn.getNode()));
                ret.append(app);
                linelen += app.length();
            } else {
                String app2 = String.format(" %d", Integer.valueOf(ftn.getNode()));
                ret.append(app2);
                linelen += app2.length();
            }
        }
        if (ret.length() == 0) {
            return "";
        }
        if (ret.charAt(ret.length() - 1) != '\n') {
            ret.append('\n');
        }
        return ret.toString();
    }

    public static List<Ftn2D> readPath(String seenByLines) {
        int node;
        List<Ftn2D> seen = new ArrayList<>();
        String[] seenBy = seenByLines.split("[ \n]");
        int net = 0;
        for (String parts : seenBy) {
            if (parts != null && parts.length() >= 1 && !parts.equals(PATH)) {
                String[] part = parts.split("/");
                if (part.length == 2) {
                    net = Integer.valueOf(part[0]).intValue();
                    node = Integer.valueOf(part[1]).intValue();
                } else {
                    node = Integer.valueOf(part[0]).intValue();
                }
                seen.add(new Ftn2D(net, node));
            }
        }
        return seen;
    }

    public static String writePath(List<Ftn2D> path) {
        logger.l5("WritePath: " + path);
        StringBuilder ret = new StringBuilder();
        int net = 0;
        int linelen = 0;
        for (Ftn2D ftn : path) {
            if (linelen >= 72) {
                linelen = 0;
                net = 0;
                ret.append("\n");
            }
            if (linelen == 0) {
                ret.append(PATH);
                linelen += PATH.length();
            }
            if (net != ftn.getNet()) {
                net = ftn.getNet();
                String app = String.format(" %d/%d", Integer.valueOf(ftn.getNet()), Integer.valueOf(ftn.getNode()));
                ret.append(app);
                linelen += app.length();
            } else {
                String app2 = String.format(" %d", Integer.valueOf(ftn.getNode()));
                ret.append(app2);
                linelen += app2.length();
            }
        }
        if (ret.length() == 0) {
            return "";
        }
        if (ret.charAt(ret.length() - 1) != '\n') {
            ret.append('\n');
        }
        return ret.toString();
    }

    public static List<Ftn2D> read2D(String list2d) {
        List<Ftn2D> ret = new ArrayList<>();
        for (String l2d : list2d.split(" ")) {
            String[] part = l2d.split("/");
            try {
                ret.add(new Ftn2D(Integer.valueOf(part[0]).intValue(), Integer.valueOf(part[1]).intValue()));
            } catch (RuntimeException e) {
            }
        }
        return ret;
    }

    public static List<FtnAddress> read4D(String list2d) {
        List<FtnAddress> ret = new ArrayList<>();
        for (String l2d : list2d.split(" ")) {
            try {
                ret.add(new FtnAddress(l2d));
            } catch (RuntimeException e) {
            }
        }
        return ret;
    }

    public static String write2D(List<Ftn2D> list, boolean sort) {
        StringBuilder ret = new StringBuilder();
        if (sort) {
            Collections.sort(list, new Ftn2DComparator());
        }
        boolean flag = false;
        for (Ftn2D d : list) {
            if (flag) {
                ret.append(" ");
            } else {
                flag = true;
            }
            ret.append(String.format("%d/%d", Integer.valueOf(d.getNet()), Integer.valueOf(d.getNode())));
        }
        return ret.toString();
    }

    public static String write4D(List<FtnAddress> list) {
        StringBuilder ret = new StringBuilder();
        boolean flag = false;
        for (FtnAddress d : list) {
            if (flag) {
                ret.append(" ");
            } else {
                flag = true;
            }
            ret.append(d.toString());
        }
        return ret.toString();
    }

    private static String getOption(Link link, String option) {
        LinkOption opt = new LinkOption(link, option.toLowerCase());
        if (opt == null) {
            return "";
        }
        String value = opt.getValue();
        return value;
    }

    public static String getOptionString(Link link, String option) {
        return getOption(link, option);
    }

    public static boolean getOptionBooleanDefFalse(Link link, String option) {
        String s = getOption(link, option);
        return s.equalsIgnoreCase("TRUE") || s.equalsIgnoreCase("ON");
    }

    public static boolean getOptionBooleanDefTrue(Link link, String option) {
        String s = getOption(link, option);
        return (s.equalsIgnoreCase("FALSE") || s.equalsIgnoreCase("OFF")) ? false : true;
    }

    public static long getOptionLong(Link link, String option) {
        String s = getOption(link, option);
        try {
            long ret = Long.valueOf(s).longValue();
            return ret;
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    public static String[] getOptionStringArray(Link link, String option) {
        String s = getOption(link, option);
        return s.split(" ");
    }

    public static FtnMessage netmailToFtnMessage(Netmail mail) {
        FtnMessage message = new FtnMessage();
        message.setNetmail(true);
        message.setFromName(mail.getFromName());
        message.setToName(mail.getToName());
        message.setFromAddr(new FtnAddress(mail.getFromFTN()));
        message.setToAddr(new FtnAddress(mail.getToFTN()));
        message.setDate(mail.getDate());
        message.setSubject(mail.getSubject());
        message.setAttribute(mail.getAttr());
        message.setCodePage(mail.getCodePage());
        StringBuilder text = new StringBuilder();
        text.append(mail.getText());
        if (text.charAt(text.length() - 1) != '\n') {
            text.append('\n');
        }
        text.append(String.format(ROUTE_VIA, Main.info.getAddress().toString(), format.format(new Date())));
        message.setText(text.toString());
        return message;
    }

    public static void unpack(Message message) throws IOException {
        char c;
        byte[] buf;
        String filename = message.getMessageName().toLowerCase();
        if (filename.matches(PKT_REGEX)) {
            File out = createInboundFile(message.isSecure());
            FileOutputStream fos = new FileOutputStream(out);
            InputStream is = message.getInputStream();
            while (true) {
                int len = is.available();
                if (len > 0) {
                    if (len > 1024) {
                        buf = new byte[1024];
                    } else {
                        buf = new byte[len];
                    }
                    is.read(buf);
                    fos.write(buf);
                } else {
                    is.close();
                    fos.close();
                    return;
                }
            }
        } else if (filename.matches(ARCMAIL_REGEX)) {
            ZipInputStream zis = new ZipInputStream(message.getInputStream());
            while (true) {
                ZipEntry ze = zis.getNextEntry();
                if (ze != null) {
                    if (ze.getName().toLowerCase().matches(PKT_REGEX)) {
                        File out2 = null;
                        FileOutputStream fos2 = null;
                        byte[] buf2 = new byte[1024];
                        while (true) {
                            int len2 = zis.read(buf2);
                            if (len2 == -1) {
                                break;
                            }
                            if (out2 == null) {
                                out2 = createInboundFile(message.isSecure());
                                fos2 = new FileOutputStream(out2);
                            }
                            fos2.write(buf2, 0, len2);
                        }
                        if (fos2 != null) {
                            fos2.close();
                        }
                    }
                } else {
                    zis.close();
                    return;
                }
            }
        } else if (message.isSecure()) {
            String filename2 = filename.replaceAll("^[\\./\\\\]+", "_");
            File f = new File(Main.getInbound() + File.separator + filename2);
            boolean ninetoa = false;
            boolean ztonull = false;
            boolean underll = false;
            while (f.exists()) {
                if ((ninetoa && ztonull) || underll) {
                    logger.l2("All possible files exists. Please delete something before continue");
                    break;
                }
                char[] array = filename2.toCharArray();
                char c2 = array[array.length - 1];
                if ((c2 >= '0' && c2 <= '8') || (c2 >= 'a' && c2 <= 'y')) {
                    c = (char) (c2 + 1);
                } else if (c2 == '9') {
                    c = 'a';
                    ninetoa = true;
                } else if (c2 == 'z') {
                    c = '0';
                    ztonull = true;
                } else {
                    c = '_';
                    underll = true;
                }
                array[array.length - 1] = c;
                filename2 = new String(array);
                f = new File(Main.getInbound() + File.separator + filename2);
            }
            FileOutputStream fos3 = new FileOutputStream(f);
            while (message.getInputStream().available() > 0) {
                byte[] block = new byte[1024];
                fos3.write(block, 0, message.getInputStream().read(block));
            }
            fos3.close();
            logger.l3("File saved " + f.getAbsolutePath() + " (" + f.length() + ")");
        } else {
            logger.l2("File rejected via unsecure " + filename);
        }
    }

    public static boolean completeMask(Route route, FtnMessage message) {
        boolean ok = true;
        String[] regexp = {route.getFromAddr(), route.getToAddr(), route.getFromName(), route.getToName(), route.getSubject()};
        String[] check = {message.getFromAddr().toString(), message.getToAddr().toString(), message.getFromName(), message.getToName(), message.getSubject()};
        for (int i = 0; i < 5; i++) {
            if (regexp[i] != null && !regexp[i].equals("*")) {
                logger.l5("Checks " + check[i] + " via regexp " + regexp[i]);
                if (check[i] == null || !check[i].matches(regexp[i])) {
                    ok = false;
                }
            }
        }
        return ok;
    }

    public static Link getRouting(FtnMessage message) {
        Link routeVia;
        if (message.getToAddr() != null && message.getToAddr().equals(Main.info.getAddress())) {
            Main.SystemInfo.getLogger().log("getRouting", "Routing messsage '" + message.getSubject() + "' to ourself");
            return null;
        }
        try {
            routeVia = new Link(Main.info.getBossAddress());
        } catch (HotdogedException e) {
            e = e;
        }
        try {
            Main.SystemInfo.getLogger().log("getRouting", "Routing messsage '" + message.getSubject() + "' via " + routeVia);
            return routeVia;
        } catch (HotdogedException e2) {
            e = e2;
            logger.l2("HotdogedException", e);
            return null;
        }
    }

    private static File createOutboundFile(Link link) {
        String template = "out_" + link.getId() + ".%d";
        int i = 0;
        File f = new File(Main.getInbound() + File.separator + String.format(template, 0));
        while (f.exists()) {
            i++;
            f = new File(Main.getInbound() + File.separator + String.format(template, Integer.valueOf(i)));
        }
        return f;
    }

    private static File createInboundFile(boolean secure) {
        String template = (secure ? "s" : "u") + "inb%d.pkt";
        int i = 0;
        File f = new File(Main.getInbound() + File.separator + String.format(template, 0));
        while (f.exists()) {
            i++;
            f = new File(Main.getInbound() + File.separator + String.format(template, Integer.valueOf(i)));
        }
        return f;
    }

    private static File createZipFile(FtnPkt header, Link link, List<FtnMessage> messages) throws IOException, HotdogedException {
        File np = createOutboundFile(link);
        FileOutputStream out = new FileOutputStream(np);
        ZipOutputStream zos = new ZipOutputStream(out);
        zos.setMethod(8);
        ZipEntry ze = new ZipEntry(String.format("%s.pkt", generate8d()));
        ze.setMethod(8);
        CRC32 crc32 = new CRC32();
        zos.putNextEntry(ze);
        byte[] data = header.pack();
        int len = 0 + data.length;
        crc32.update(data);
        zos.write(data);
        for (FtnMessage m : messages) {
            codePage = m.getCodePage();
            byte[] data2 = m.pack();
            len += data2.length;
            crc32.update(data2);
            zos.write(data2);
        }
        byte[] data3 = header.finalz();
        int len2 = len + data3.length;
        crc32.update(data3);
        zos.write(data3);
        ze.setSize(len2);
        ze.setCrc(crc32.getValue());
        zos.close();
        out.close();
        return np;
    }

    public static String generateEchoBundle() {
        String suffix = "";
        switch (Calendar.getInstance().get(7)) {
            case 1:
                suffix = "su";
                break;
            case 2:
                suffix = "mo";
                break;
            case 3:
                suffix = "tu";
                break;
            case 4:
                suffix = "we";
                break;
            case 5:
                suffix = "th";
                break;
            case 6:
                suffix = "fr";
                break;
            case 7:
                suffix = "sa";
                break;
        }
        int d = (int) (Math.random() * 9.0d);
        return generate8d() + "." + suffix + d;
    }

    public static List<Message> pack(List<FtnMessage> messages, Link link) {
        boolean packNetmail = getOptionBooleanDefFalse(link, LinkOption.BOOLEAN_PACK_NETMAIL);
        boolean packEchomail = getOptionBooleanDefTrue(link, LinkOption.BOOLEAN_PACK_ECHOMAIL);
        List<Message> ret = new ArrayList<>();
        List<FtnMessage> packedEchomail = new ArrayList<>();
        List<FtnMessage> unpackedEchomail = new ArrayList<>();
        List<FtnMessage> packedNetmail = new ArrayList<>();
        List<FtnMessage> unpackedNetmail = new ArrayList<>();
        FtnAddress to = new FtnAddress(link.getLinkAddress());
        String password = link.getPaketPassword();
        FtnPkt header = new FtnPkt(Main.info.getAddress(), to, password, new Date());
        for (FtnMessage message : messages) {
            if (message.isNetmail()) {
                if (packNetmail) {
                    packedNetmail.add(message);
                } else {
                    unpackedNetmail.add(message);
                }
            } else if (packEchomail) {
                packedEchomail.add(message);
            } else {
                unpackedEchomail.add(message);
            }
        }
        if (!packedNetmail.isEmpty()) {
            try {
                Message m = new Message(createZipFile(header, link, packedNetmail));
                m.setMessageName(generateEchoBundle());
                ret.add(m);
            } catch (Exception e) {
                logger.l1("Error while writing netmail to link #" + link.getId(), e);
            }
        }
        if (!packedEchomail.isEmpty()) {
            try {
                Message m2 = new Message(createZipFile(header, link, packedEchomail));
                m2.setMessageName(generateEchoBundle());
                ret.add(m2);
            } catch (Exception e2) {
                logger.l1("Error while writing echomail to link #" + link.getId(), e2);
            }
        }
        if (!unpackedNetmail.isEmpty()) {
            try {
                File out = createOutboundFile(link);
                FileOutputStream fos = new FileOutputStream(out);
                fos.write(header.pack());
                for (FtnMessage m3 : unpackedNetmail) {
                    codePage = m3.getCodePage();
                    fos.write(m3.pack());
                }
                fos.write(header.finalz());
                fos.close();
                Message m4 = new Message(out);
                m4.setMessageName(generate8d() + ".pkt");
                ret.add(m4);
            } catch (Exception e3) {
                logger.l1("Error while writing netmail to link #" + link.getId(), e3);
            }
        }
        if (!unpackedEchomail.isEmpty()) {
            try {
                File out2 = createOutboundFile(link);
                FileOutputStream fos2 = new FileOutputStream(out2);
                fos2.write(header.pack());
                for (FtnMessage m5 : unpackedEchomail) {
                    codePage = m5.getCodePage();
                    fos2.write(m5.pack());
                }
                fos2.write(header.finalz());
                fos2.close();
                Message m6 = new Message(out2);
                m6.setMessageName(generate8d() + ".pkt");
                ret.add(m6);
            } catch (Exception e4) {
                logger.l1("Error while writing netmail to link #" + link.getId(), e4);
            }
        }
        return ret;
    }

    public static void moveToBad(FtnPkt pkt) throws HotdogedException {
        ByteArrayInputStream bis = new ByteArrayInputStream(pkt.pack());
        Message message = new Message(String.format("%s_%d.pkt", generate8d(), Long.valueOf(new Date().getTime() / 1000)), bis.available());
        message.setInputStream(bis);
        try {
            unpack(message);
        } catch (IOException e) {
        }
    }

    public static Echoarea getAreaByName(String name, Link link) {
        Echoarea ret = null;
        String name2 = name.toLowerCase();
        try {
            Echoarea ret2 = new Echoarea(name2);
            ret = ret2;
        } catch (HotdogedException e) {
            Main.SystemInfo.getLogger().log(TAG, e.getMessage());
        }
        if (ret == null) {
            if (link == null || getOptionBooleanDefFalse(link, LinkOption.BOOLEAN_AUTOCREATE_AREA)) {
                Echoarea ret3 = new Echoarea();
                ret3.setName(name2);
                ret3.setDescription("");
                ret3.setReadlevel(0L);
                ret3.setWritelevel(0L);
                try {
                    ret3.create();
                    logger.l3("Echoarea " + name2.toUpperCase() + " created");
                } catch (HotdogedException e2) {
                    Main.SystemInfo.getLogger().log(TAG, "Autocreation of echoarea " + name2 + " failed: " + e2.getMessage());
                }
                if (link != null) {
                    Subscription sub = new Subscription();
                    sub.setArea(ret3);
                    sub.setLink(link);
                    sub.save();
                }
                Notifier.INSTANSE.notify(new NewEchoareaEvent(name2, link));
                return ret3;
            }
            return ret;
        }
        Subscription sub2 = null;
        try {
            Subscription sub3 = new Subscription(ret, link);
            sub2 = sub3;
        } catch (HotdogedException e3) {
            e3.printStackTrace();
        }
        if (link != null && sub2 == null) {
            return null;
        }
        return ret;
    }

    public static boolean isADupe(Echoarea area, String msgid) {
        boolean z;
        if (area == null || msgid == null) {
            return false;
        }
        Cursor cursor = null;
        try {
            try {
                cursor = Main.SystemInfo.getContext().getContentResolver().query(Uri.withAppendedPath(area.getUri(), "items"), null, "Message_ID = " + DatabaseUtils.sqlEscapeString(msgid), null, null);
                if (cursor.moveToFirst()) {
                    Main.SystemInfo.getLogger().log(TAG, "Dupe detected: " + area.getName() + ": " + msgid);
                    z = true;
                    if (cursor != null && !cursor.isClosed()) {
                        cursor.close();
                    }
                } else {
                    if (cursor != null && !cursor.isClosed()) {
                        cursor.close();
                    }
                    z = false;
                }
            } catch (Exception e) {
                Log.e(TAG, "Error catching dupes: " + e.getMessage());
                if (cursor != null && !cursor.isClosed()) {
                    cursor.close();
                }
                z = false;
            }
            return z;
        } catch (Throwable th) {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
            throw th;
        }
    }

    public static boolean isNetmailMustBeDropped(FtnMessage netmail, boolean secure) {
        return (netmail.getFromAddr().equals(Main.info.getAddress()) || netmail.getToAddr().equals(Main.info.getAddress())) ? false : true;
    }

    public static void writeReply(FtnMessage fmsg, String subject, String text) {
        Netmail netmail = new Netmail();
        netmail.setFromFTN(Main.info.getAddress().toString());
        netmail.setFromName(Main.info.getStationName());
        netmail.setToFTN(fmsg.getFromAddr().toString());
        netmail.setToName(fmsg.getFromName());
        netmail.setSubject(subject);
        netmail.setDate(new Date());
        StringBuilder sb = new StringBuilder();
        sb.append(String.format(fmsg.getFromAddr().topt() + Main.info.getAddress().fmpt() + "\u0001REPLY: %s\n\u0001MSGID: %s %s\n\u0001PID: %s\n\u0001TID: %s\nHello, %s!\n\n", fmsg.getMsgid(), Main.info.getAddress().toString(), generate8d(), Main.info.getVersion(), Main.SystemInfo.getPID(), netmail.getToName()));
        sb.append(text);
        sb.append("\n\n========== Original message ==========\n");
        sb.append("From: " + fmsg.getFromName() + " (" + fmsg.getFromAddr() + ")\n");
        sb.append("To: " + fmsg.getToName() + " (" + fmsg.getToAddr() + ")\n");
        sb.append("Date: " + fmsg.getDate() + "\n");
        sb.append("Subject: " + fmsg.getSubject() + "\n");
        if (fmsg.getText() != null) {
            sb.append(fmsg.getText().replaceAll("\u0001", "@").replaceAll("---", "+++").replaceAll(" \\* Origin:", " + Origin:"));
        }
        sb.append("========== Original message ==========\n\n--- " + Main.info.getVersion() + "\n");
        netmail.setText(sb.toString());
        FtnMessage ret = new FtnMessage();
        ret.setCodePage(fmsg.getCodePage());
        ret.setFromAddr(new FtnAddress(Main.info.getAddress().toString()));
        ret.setToAddr(fmsg.getFromAddr());
        Link routeVia = getRouting(ret);
        if (routeVia == null) {
            logger.l2("Routing for reply not found" + fmsg.getMsgid());
            return;
        }
        netmail.setRouteVia(routeVia);
        try {
            netmail.save();
        } catch (HotdogedException e) {
            Main.SystemInfo.getLogger().log(TAG, "Netmail " + netmail.toString() + " could not be saved: " + e.getMessage());
        }
        logger.l4("Netmail #" + netmail.getId() + " created");
        if (getOptionBooleanDefTrue(routeVia, LinkOption.BOOLEAN_CRASH_NETMAIL)) {
            System.out.println("Need to create poll to " + routeVia);
        }
    }

    public static void writeEchomail(Echoarea area, String subject, String text) {
        Echomail mail = new Echomail();
        mail.setFromFTN(Main.info.getAddress().toString());
        mail.setFromName(Main.info.getStationName());
        mail.setArea(area);
        mail.setDate(new Date());
        mail.setPath("");
        mail.setSeenBy("");
        mail.setToName("All");
        mail.setSubject(subject);
        StringBuilder b = new StringBuilder();
        b.append(String.format("\u0001MSGID: %s %s\n\u0001PID: %s\n\u0001TID: %s\n\n", Main.info.getAddress().toString(), generate8d(), Main.SystemInfo.getPID(), Main.info.getVersion()));
        b.append(text);
        b.append("\n--- " + Main.info.getStationName() + "\n");
        b.append(" * Origin: " + Main.info.getVersion() + " (" + Main.info.getAddress().toString() + ")\n");
        mail.setText(b.toString());
        try {
            mail.save();
        } catch (HotdogedException e) {
            Main.SystemInfo.getLogger().log(TAG, "Error writing echomail: " + e.getMessage());
        }
    }

    public static String chrs2codepage(String chrs) {
        if (chrs == null || chrs.length() == 0) {
            return DEFAULT_FTN_CHARSET;
        }
        Pattern p = Pattern.compile("^(.*?)( (.*))?$");
        Matcher m = p.matcher(chrs);
        if (m.matches()) {
            String charset = m.group(1);
            if (isCharsetUnknown(charset)) {
                return DEFAULT_FTN_CHARSET;
            }
            if (charset.equalsIgnoreCase("CP850")) {
                return "LATIN-1";
            }
            if (charset.equalsIgnoreCase("+7_FIDO")) {
                return "CP866";
            }
            return charset.toUpperCase();
        }
        return DEFAULT_FTN_CHARSET;
    }

    private static boolean isCharsetUnknown(String charset) {
        return charset.equalsIgnoreCase("IBMPC") || charset.equalsIgnoreCase("ISO-10") || charset.equalsIgnoreCase("CP865") || charset.equalsIgnoreCase("MAC") || charset.equalsIgnoreCase(HTTP.ASCII) || charset.equalsIgnoreCase("DUTCH") || charset.equalsIgnoreCase("FINNISH") || charset.equalsIgnoreCase("FRENCH") || charset.equalsIgnoreCase("CANADIAN") || charset.equalsIgnoreCase("GERMAN") || charset.equalsIgnoreCase("ITALIAN") || charset.equalsIgnoreCase("NORWEIG") || charset.equalsIgnoreCase("PORTU") || charset.equalsIgnoreCase("SPANISH") || charset.equalsIgnoreCase("SWEDISH") || charset.equalsIgnoreCase("SWISS") || charset.equalsIgnoreCase("UK");
    }

    public static String codepage2chrs(String codePage2) {
        if (codePage2 == null || codePage2.length() == 0) {
            return DEFAULT_FTN_CHRS;
        }
        String level = getChrsLevel(codePage2);
        return codePage2.toUpperCase() + (level != null ? " " + level : "");
    }

    public static String getChrsLevel(String chrs) {
        if (chrs == null || chrs.length() == 0) {
            return null;
        }
        if (chrs.equalsIgnoreCase("CP866") || chrs.equalsIgnoreCase("LATIN-1")) {
            return "2";
        }
        if (chrs.equalsIgnoreCase("UTF-8")) {
            return "4";
        }
        return null;
    }
}
