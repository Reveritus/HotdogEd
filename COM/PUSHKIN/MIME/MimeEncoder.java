package com.pushkin.mime;

import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.binary.ApacheBase64;
import org.apache.commons.codec.net.QCodec;

public class MimeEncoder {
    private String charset;
    private String encodedString;
    private String sourceString;

    public enum Encoding {
        Base64,
        QP
    }

    public String getEncodedString() {
        return this.encodedString;
    }

    public MimeEncoder() {
        this.encodedString = null;
        this.sourceString = null;
        this.charset = null;
    }

    public MimeEncoder(String sourceString, Encoding encoding, String charset) throws MIMEException {
        this.encodedString = null;
        this.sourceString = null;
        this.charset = null;
        this.encodedString = sourceString;
        this.charset = charset;
        switch (encoding) {
            case Base64:
                this.encodedString = encodeB64(sourceString);
                return;
            case QP:
                this.encodedString = encodeQP(sourceString, charset);
                return;
            default:
                return;
        }
    }

    public static String encodeB64(String sourceString) {
        String sourceString2 = sourceString.replaceAll("\\r\\n", "");
        ApacheBase64 base64 = new ApacheBase64();
        byte[] b = sourceString2.getBytes();
        String result = base64.encodeAsString(b);
        return result;
    }

    public static String encodeQP(String sourceString, String charset) throws MIMEException {
        String sourceString2 = sourceString.replaceAll("\\r\\n", "");
        QCodec qCodec = new QCodec();
        try {
            String result = qCodec.encode(sourceString2, charset);
            return result.replace(" ", "_");
        } catch (EncoderException e) {
            throw new MIMEException(e);
        }
    }

    public String getSourceString() {
        return this.sourceString;
    }

    public void setSourceString(String sourceString) {
        this.sourceString = sourceString;
    }

    public String getCharset() {
        return this.charset;
    }

    public static boolean needsEncoding(String value) {
        if (value == null) {
            return false;
        }
        return value.matches(".*[^0-9a-zA-Z ,.?\\-\\.\\^\\(\\)\\+\\<\\>@']+.*");
    }
}
