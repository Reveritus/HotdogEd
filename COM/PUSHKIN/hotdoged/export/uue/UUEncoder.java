package com.pushkin.hotdoged.export.uue;

import com.pushkin.hotdoged.export.HotdogedException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.mail.MessagingException;
import javax.mail.internet.MimeUtility;
import org.jetbrains.annotations.NotNull;

public class UUEncoder {
    private String fileName;

    public UUEncoder(String fileName) {
        this.fileName = fileName;
    }

    public void encode(@NotNull InputStream is, @NotNull OutputStream os) throws HotdogedException {
        try {
            OutputStream encodedOs = MimeUtility.encode(os, "uuencode", this.fileName);
            byte[] buf = new byte[1024];
            while (true) {
                int len = is.read(buf, 0, buf.length);
                if (len > 0) {
                    encodedOs.write(buf, 0, len);
                } else {
                    encodedOs.flush();
                    return;
                }
            }
        } catch (IOException e) {
            throw new HotdogedException("Error copying buffers: " + e.getMessage());
        } catch (MessagingException e2) {
            throw new HotdogedException("Error uuencoding: " + e2.getMessage());
        }
    }
}
