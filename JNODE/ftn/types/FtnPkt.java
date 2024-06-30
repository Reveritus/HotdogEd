package jnode.ftn.types;

import com.pushkin.hotdoged.export.HotdogedException;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import jnode.ftn.FtnTools;
import jnode.ftn.exception.LastMessageException;

public class FtnPkt {
    private static DateFormat format = new SimpleDateFormat("yyyy MM dd HH mm ss", Locale.US);
    private boolean close;
    private Date date;
    private FtnAddress fromAddr;
    private InputStream is;
    private String password;
    private FtnAddress toAddr;

    public FtnAddress getFromAddr() {
        return this.fromAddr;
    }

    public FtnAddress getToAddr() {
        return this.toAddr;
    }

    public String getPassword() {
        return this.password;
    }

    public Date getDate() {
        return this.date;
    }

    public static DateFormat getFormat() {
        return format;
    }

    public FtnPkt() {
    }

    public FtnPkt(FtnAddress fromAddr, FtnAddress toAddr, String password, Date date) {
        this.fromAddr = fromAddr;
        this.toAddr = toAddr;
        this.password = password;
        this.date = date;
    }

    public byte[] pack() throws HotdogedException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream os = new DataOutputStream(bos);
        FtnTools.codePage = FtnTools.DEFAULT_FTN_CHARSET;
        try {
            os.writeShort(FtnTools.revShort(this.fromAddr.getNode()));
            os.writeShort(FtnTools.revShort(this.toAddr.getNode()));
            String date = format.format(this.date);
            int n = 0;
            for (String d : date.split(" ")) {
                short s = new Short(d).shortValue();
                if (n == 1) {
                    s = (short) (s - 1);
                }
                os.writeShort(FtnTools.revShort(s));
                n++;
            }
            os.write(new byte[]{0, 0, 2, 0});
            os.writeShort(FtnTools.revShort(this.fromAddr.getNet()));
            os.writeShort(FtnTools.revShort(this.toAddr.getNet()));
            os.write(new byte[]{-1, 0});
            os.write(FtnTools.substr(this.password, 8));
            for (int i = this.password.length(); i < 8; i++) {
                os.write(0);
            }
            os.writeShort(FtnTools.revShort(this.fromAddr.getZone()));
            os.writeShort(FtnTools.revShort(this.toAddr.getZone()));
            os.write(new byte[]{0, 0, 0, 1, 19, 4, 1, 0});
            os.writeShort(FtnTools.revShort(this.fromAddr.getZone()));
            os.writeShort(FtnTools.revShort(this.toAddr.getZone()));
            os.writeShort(FtnTools.revShort(this.fromAddr.getPoint()));
            os.writeShort(FtnTools.revShort(this.toAddr.getPoint()));
            os.write(new byte[]{0, 0, 0, 0});
            os.close();
        } catch (IOException e) {
        }
        return bos.toByteArray();
    }

    public void write(OutputStream fos) throws HotdogedException {
        DataOutputStream os = new DataOutputStream(fos);
        try {
            os.writeShort(FtnTools.revShort(this.fromAddr.getNode()));
            os.writeShort(FtnTools.revShort(this.toAddr.getNode()));
            String date = format.format(this.date);
            int n = 0;
            for (String d : date.split(" ")) {
                short s = new Short(d).shortValue();
                if (n == 1) {
                    s = (short) (s - 1);
                }
                os.writeShort(FtnTools.revShort(s));
                n++;
            }
            os.write(new byte[]{0, 0, 2, 0});
            os.writeShort(FtnTools.revShort(this.fromAddr.getNet()));
            os.writeShort(FtnTools.revShort(this.toAddr.getNet()));
            os.write(new byte[]{-1, 0});
            os.write(FtnTools.substr(this.password, 8));
            for (int i = this.password.length(); i < 8; i++) {
                os.write(0);
            }
            os.writeShort(FtnTools.revShort(this.fromAddr.getZone()));
            os.writeShort(FtnTools.revShort(this.toAddr.getZone()));
            os.write(new byte[]{0, 0, 0, 1, 19, 4, 1, 0});
            os.writeShort(FtnTools.revShort(this.fromAddr.getZone()));
            os.writeShort(FtnTools.revShort(this.toAddr.getZone()));
            os.writeShort(FtnTools.revShort(this.fromAddr.getPoint()));
            os.writeShort(FtnTools.revShort(this.toAddr.getPoint()));
            os.write(new byte[]{0, 0, 0, 0});
        } catch (IOException e) {
        }
    }

    public void finalz(OutputStream fos) {
        try {
            fos.write(new byte[]{0, 0});
            fos.close();
        } catch (IOException e) {
        }
    }

    public byte[] finalz() {
        return new byte[]{0, 0};
    }

    public void unpack(InputStream iz) {
        unpack(iz, true);
    }

    public void unpack(InputStream iz, boolean close) {
        this.is = iz;
        this.close = close;
        DataInputStream is = new DataInputStream(iz);
        this.fromAddr = new FtnAddress();
        this.toAddr = new FtnAddress();
        try {
            this.fromAddr.setNode(FtnTools.revShort(is.readShort()));
            this.toAddr.setNode(FtnTools.revShort(is.readShort()));
            short[] date = new short[6];
            for (int i = 0; i < 6; i++) {
                date[i] = FtnTools.revShort(is.readShort());
            }
            try {
                Calendar calendar = Calendar.getInstance();
                calendar.set(date[0], date[1], date[2], date[3], date[4], date[5]);
                this.date = calendar.getTime();
            } catch (Exception e) {
                this.date = new Date(0L);
            }
            is.skip(4L);
            this.fromAddr.setNet(FtnTools.revShort(is.readShort()));
            this.toAddr.setNet(FtnTools.revShort(is.readShort()));
            is.skip(2L);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            for (int i2 = 0; i2 < 8; i2++) {
                int c = is.read();
                if (c != 0) {
                    bos.write(c);
                }
            }
            bos.close();
            this.password = new String(bos.toByteArray());
            is.skip(12L);
            this.fromAddr.setZone(FtnTools.revShort(is.readShort()));
            this.toAddr.setZone(FtnTools.revShort(is.readShort()));
            this.fromAddr.setPoint(FtnTools.revShort(is.readShort()));
            this.toAddr.setPoint(FtnTools.revShort(is.readShort()));
            is.skip(4L);
        } catch (IOException e2) {
        }
    }

    public FtnMessage getNextMessage() throws HotdogedException {
        try {
            FtnMessage mess = new FtnMessage();
            mess.unpack(this.is);
            return mess;
        } catch (LastMessageException e) {
            if (this.close) {
                try {
                    this.is.close();
                } catch (IOException e2) {
                }
            }
            return null;
        }
    }

    public String toString() {
        return String.format("PKT From: %s\nPKT To: %s\nPKT Date: %s\nPKT Password: %s\n", this.fromAddr, this.toAddr, this.date, this.password);
    }
}
