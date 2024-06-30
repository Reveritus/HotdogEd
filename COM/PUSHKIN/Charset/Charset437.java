package com.pushkin.charset;

import kotlin.text.Typography;

public class Charset437 extends AbstractCharset {
    private static final byte CHAR_UNKNOWN = 63;
    private static final char[] CP437_SYM = {199, 252, 233, 226, 228, 224, 229, 231, 234, 235, 232, 239, 238, 236, 196, 197, 201, 230, 198, 244, 246, 242, 251, 249, 255, 214, 220, Typography.cent, Typography.pound, 165, 8359, 402, 225, 237, 243, 250, 241, 209, 170, 186, 191, 8976, 172, Typography.half, 188, 161, Typography.leftGuillemete, Typography.rightGuillemete, 9617, 9618, 9619, 9474, 9508, 9569, 9570, 9558, 9557, 9571, 9553, 9559, 9565, 9564, 9563, 9488, 9492, 9524, 9516, 9500, 9472, 9532, 9566, 9567, 9562, 9556, 9577, 9574, 9568, 9552, 9580, 9575, 9576, 9572, 9573, 9561, 9560, 9554, 9555, 9579, 9578, 9496, 9484, 9608, 9604, 9612, 9616, 9600, 945, 223, 915, 960, 931, 963, 181, 964, 934, 920, 937, 948, 8734, 966, 949, 8745, 8801, Typography.plusMinus, Typography.greaterOrEqual, Typography.lessOrEqual, 8992, 8993, 247, Typography.almostEqual, Typography.degree, 8729, Typography.middleDot, 8730, 8319, 178, 9632, Typography.nbsp};

    @Override // com.pushkin.charset.AbstractCharset
    protected byte char2byte(char ch2) {
        if (ch2 < 128) {
            return (byte) ch2;
        }
        for (int i = 0; i < CP437_SYM.length; i++) {
            if (CP437_SYM[i] == ch2) {
                return (byte) (i + 128);
            }
        }
        return CHAR_UNKNOWN;
    }

    @Override // com.pushkin.charset.AbstractCharset
    protected char byte2char(byte b) {
        short sh;
        if (b < 0) {
            sh = (short) (b + 256);
        } else {
            sh = b;
        }
        if (sh < 128) {
            return (char) b;
        }
        return CP437_SYM[sh - 128];
    }
}
