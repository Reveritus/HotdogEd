package com.pushkin.charset;

import kotlin.text.Typography;

public class Charset866 extends AbstractCharset {
    private static final byte CHAR_UNKNOWN = 63;
    private static final char[] CP866_SYM = {1040, 1041, 1042, 1043, 1044, 1045, 1046, 1047, 1048, 1049, 1050, 1051, 1052, 1053, 1054, 1055, 1056, 1057, 1058, 1059, 1060, 1061, 1062, 1063, 1064, 1065, 1066, 1067, 1068, 1069, 1070, 1071, 1072, 1073, 1074, 1075, 1076, 1077, 1078, 1079, 1080, 1081, 1082, 1083, 1084, 1085, 1086, 1087, 9617, 9618, 9619, 9474, 9508, 9569, 9570, 9558, 9557, 9571, 9553, 9559, 9565, 9564, 9563, 9488, 9492, 9524, 9516, 9500, 9472, 9532, 9566, 9567, 9562, 9556, 9577, 9574, 9568, 9552, 9580, 9575, 9576, 9572, 9573, 9561, 9560, 9554, 9555, 9579, 9578, 9496, 9484, 9608, 9604, 9612, 9616, 9600, 1088, 1089, 1090, 1091, 1092, 1093, 1094, 1095, 1096, 1097, 1098, 1099, 1100, 1101, 1102, 1103, 1025, 1105, 1028, 1108, 1031, 1111, 1038, 1118, Typography.degree, 8729, Typography.middleDot, 8730, 8470, 164, 9632, Typography.nbsp};

    @Override // com.pushkin.charset.AbstractCharset
    protected byte char2byte(char ch2) {
        if (ch2 < 128) {
            return (byte) ch2;
        }
        for (int i = 0; i < CP866_SYM.length; i++) {
            if (CP866_SYM[i] == ch2) {
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
        return CP866_SYM[sh - 128];
    }
}
