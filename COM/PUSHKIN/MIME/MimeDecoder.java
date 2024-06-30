package com.pushkin.mime;

import java.io.UnsupportedEncodingException;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.ApacheBase64;
import org.apache.commons.codec.net.QuotedPrintableCodec;

public class MimeDecoder {
    private String decodedString;
    private String encodedString;

    public static class DecodeResult {
        private static final int B_ENCODING = 2;
        private static final int PLAIN_TEXT = 0;
        private static final int Q_ENCODING = 1;
        private final String codePage;
        private final int encoding;
        private final String result;

        public int getEncoding() {
            return this.encoding;
        }

        public String getCodePage() {
            return this.codePage;
        }

        public String getResult() {
            return this.result;
        }

        public DecodeResult(int encoding, String codePage, String result) {
            this.encoding = encoding;
            this.codePage = codePage;
            this.result = result;
        }

        public DecodeResult(String result) {
            this.result = result;
            this.encoding = 0;
            this.codePage = null;
        }
    }

    public String getDecodedString() {
        return this.decodedString;
    }

    public MimeDecoder() {
        this.decodedString = null;
        this.encodedString = null;
    }

    public MimeDecoder(String encodedString) throws MIMEException {
        this.decodedString = null;
        this.encodedString = null;
        this.encodedString = encodedString;
        this.decodedString = decode();
    }

    public static String decode(String encodedString) throws MIMEException {
        if (encodedString == null) {
            throw new MIMEException("Bad argument: null");
        }
        String[] parts = encodedString.split("=\\?");
        String result = "";
        DecodeResult decodeResult = null;
        if (parts.length > 0) {
            for (String part : parts) {
                if (decodeResult != null && decodeResult.getEncoding() == 2) {
                    part = part.replaceAll("[=\\r\\n ]", "");
                }
                decodeResult = decodePart(part);
                result = result + decodeResult.getResult();
            }
            return result;
        }
        return encodedString;
    }

    private String decode() throws MIMEException {
        return decode(this.encodedString);
    }

    private static DecodeResult decodePart(String part) throws MIMEException {
        int rEncoding;
        String result;
        String codePage = part.replaceFirst("^(.+?)\\?[BQbq]\\?.*", "$1");
        if (codePage == null || codePage.length() == 0) {
            return new DecodeResult(part);
        }
        String codePage2 = codePage.trim().toUpperCase();
        String encoding = part.replaceFirst("^.+?\\?([BQbq])\\?.*(\\?=)?.*?", "$1").trim();
        if (encoding == null || encoding.length() != 1) {
            return new DecodeResult(part);
        }
        switch (encoding.toUpperCase().charAt(0)) {
            case 'B':
                rEncoding = 2;
                String encValue = part.replaceFirst("^.+?\\?[Bb]\\?(.+)(\\?=)?(.*?)$", "$1$3");
                result = decodeB64(encValue, codePage2);
                break;
            case 'Q':
                rEncoding = 1;
                String encValue2 = part.replaceFirst("^.+?\\?[Qq]\\?(.+)\\?=(.*?) ?", "$1$2");
                result = decodeQP(encValue2, codePage2);
                break;
            default:
                return new DecodeResult(part);
        }
        return new DecodeResult(rEncoding, codePage2, result);
    }

    public static String decodeB64(String encValue, String codePage) throws MIMEException {
        String encValue2 = encValue.replaceAll("\\r\\n", "");
        ApacheBase64 base64 = new ApacheBase64();
        byte[] b = base64.decode(encValue2);
        try {
            String result = new String(b, codePage);
            return result;
        } catch (UnsupportedEncodingException e) {
            throw new MIMEException("Unsupported encoding: " + e.getMessage());
        }
    }

    public static String decodeQP(String encValue, String codePage) throws MIMEException {
        QuotedPrintableCodec qpCodec = new QuotedPrintableCodec();
        try {
            return qpCodec.decode(encValue.replaceAll("_", " "), codePage);
        } catch (UnsupportedEncodingException e) {
            throw new MIMEException("Unsupported encoding: " + e.getMessage());
        } catch (DecoderException e2) {
            throw new MIMEException("QP decode failure: " + e2.getMessage());
        }
    }
}
