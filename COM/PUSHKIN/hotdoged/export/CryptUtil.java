package com.pushkin.hotdoged.export;

import android.util.Base64;
import java.security.Key;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class CryptUtil {
    private static final String ALGORITHM = "DES";
    private static final byte[] keyValue = "F189B5BE".getBytes();

    public static String encrypt(String valueToEnc) throws Exception {
        if (valueToEnc == null) {
            valueToEnc = "";
        }
        Key key = generateKey();
        Cipher c = Cipher.getInstance(ALGORITHM);
        c.init(1, key);
        byte[] encValue = c.doFinal(valueToEnc.getBytes());
        String encryptedValue = Base64.encodeToString(encValue, 0);
        return encryptedValue;
    }

    public static String decrypt(String encryptedValue) throws Exception {
        if (encryptedValue == null) {
            return "";
        }
        Key key = generateKey();
        Cipher c = Cipher.getInstance(ALGORITHM);
        c.init(2, key);
        byte[] decordedValue = Base64.decode(encryptedValue, 0);
        byte[] decValue = c.doFinal(decordedValue);
        return new String(decValue);
    }

    private static Key generateKey() throws Exception {
        Key key = new SecretKeySpec(keyValue, ALGORITHM);
        return key;
    }
}
