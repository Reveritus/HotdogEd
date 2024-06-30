package com.pushkin.hotdoged.export.uue;

import com.pushkin.hotdoged.export.HotdogedException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.mail.MessagingException;
import javax.mail.internet.MimeUtility;
import org.jetbrains.annotations.NotNull;

public class UUDecoder {
    public void decodeBuffer(@NotNull InputStream is, @NotNull OutputStream os) throws HotdogedException {
        byte[] buf = new byte[1024];
        try {
            InputStream decodedIs = MimeUtility.decode(is, "uuencode");
            while (true) {
                int len = decodedIs.read(buf, 0, buf.length);
                if (len > 0) {
                    os.write(buf, 0, len);
                } else {
                    os.flush();
                    return;
                }
            }
        } catch (IOException e) {
            throw new HotdogedException("Error copying buffers: " + e.getMessage());
        } catch (MessagingException e2) {
            throw new HotdogedException("Error in uudecode: " + e2.getMessage());
        }
    }
}
