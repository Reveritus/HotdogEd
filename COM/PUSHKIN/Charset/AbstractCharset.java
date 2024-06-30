package com.pushkin.charset;

public abstract class AbstractCharset {
    protected abstract char byte2char(byte b);

    protected abstract byte char2byte(char c);

    public byte[] toByteArray(String text) {
        if (text == null) {
            return null;
        }
        byte[] result = new byte[text.length()];
        for (int i = 0; i < text.length(); i++) {
            result[i] = char2byte(text.charAt(i));
        }
        return result;
    }

    public String toString(byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        char[] chars = new char[bytes.length];
        for (int i = 0; i < bytes.length; i++) {
            chars[i] = byte2char(bytes[i]);
        }
        String result = new String(chars);
        return result;
    }

    public static String printBytes(byte[] bytes) {
        String rc = "";
        for (int i : bytes) {
            StringBuilder append = new StringBuilder().append(rc);
            if (i < 0) {
                i += 256;
            }
            rc = append.append(String.valueOf(i)).append(", ").toString();
        }
        return rc;
    }
}
