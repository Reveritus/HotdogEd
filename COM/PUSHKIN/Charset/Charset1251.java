package com.pushkin.charset;

import kotlin.text.Typography;

public class Charset1251 extends AbstractCharset {
    private static final byte CHAR_UNKNOWN = 63;
    private static final char[] CP1251_SYM = {0, 1, 2, 3, 4, 5, 6, 7, '\b', '\t', '\n', 11, '\f', '\r', 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, ' ', '!', Typography.quote, '#', Typography.dollar, '%', Typography.amp, '\'', '(', ')', '*', '+', ',', '-', '.', '/', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', ':', ';', Typography.less, '=', Typography.greater, '?', '@', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '[', '\\', ']', '^', '_', '`', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '{', '|', '}', '~', 127, 1026, 1027, Typography.lowSingleQuote, 1107, Typography.lowDoubleQuote, Typography.ellipsis, Typography.dagger, Typography.doubleDagger, Typography.euro, 8240, 1033, 8249, 1034, 1036, 1035, 1039, 1106, Typography.leftSingleQuote, Typography.rightSingleQuote, Typography.leftDoubleQuote, Typography.rightDoubleQuote, Typography.bullet, Typography.ndash, Typography.mdash, ' ', Typography.tm, 1113, 8250, 1114, 1116, 1115, 1119, Typography.nbsp, 1038, 1118, 1032, 164, 1168, 166, Typography.section, 1025, Typography.copyright, 1028, Typography.leftGuillemete, 172, 173, Typography.registered, 1031, Typography.degree, Typography.plusMinus, 1030, 1110, 1169, 181, Typography.paragraph, Typography.middleDot, 1105, 8470, 1108, Typography.rightGuillemete, 1112, 1029, 1109, 1111, 1040, 1041, 1042, 1043, 1044, 1045, 1046, 1047, 1048, 1049, 1050, 1051, 1052, 1053, 1054, 1055, 1056, 1057, 1058, 1059, 1060, 1061, 1062, 1063, 1064, 1065, 1066, 1067, 1068, 1069, 1070, 1071, 1072, 1073, 1074, 1075, 1076, 1077, 1078, 1079, 1080, 1081, 1082, 1083, 1084, 1085, 1086, 1087, 1088, 1089, 1090, 1091, 1092, 1093, 1094, 1095, 1096, 1097, 1098, 1099, 1100, 1101, 1102, 1103};

    @Override // com.pushkin.charset.AbstractCharset
    protected byte char2byte(char ch2) {
        if (ch2 < 128) {
            return (byte) ch2;
        }
        for (int i = 0; i < CP1251_SYM.length; i++) {
            if (CP1251_SYM[i] == ch2) {
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
        return CP1251_SYM[sh - 128];
    }
}
