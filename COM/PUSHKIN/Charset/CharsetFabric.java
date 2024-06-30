package com.pushkin.charset;

import java.io.UnsupportedEncodingException;

public class CharsetFabric {
    public static final AbstractCharset getCharset(String charsetName) throws UnsupportedEncodingException {
        if (charsetName == null) {
            throw new UnsupportedEncodingException("Charset is null");
        }
        if (charsetName.equalsIgnoreCase("cp866")) {
            return new Charset866();
        }
        if (charsetName.equalsIgnoreCase("cp437")) {
            return new Charset437();
        }
        if (charsetName.equalsIgnoreCase("cp1251")) {
            return new Charset1251();
        }
        throw new UnsupportedEncodingException("Charset " + charsetName + " not supported");
    }
}
