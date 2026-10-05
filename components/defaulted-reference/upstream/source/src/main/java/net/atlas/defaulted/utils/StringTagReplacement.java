package net.atlas.defaulted.utils;

//? >=1.21.5
import net.minecraft.nbt.StringTag;

//? <1.21.5
//import java.util.HexFormat;

public class StringTagReplacement {
    //? <1.21.5
    //private static final HexFormat HEX_ESCAPE = HexFormat.of().withUpperCase();
    public static void quoteAndEscape(String string, StringBuilder stringBuilder) {
        //? >=1.21.5 {
        StringTag.quoteAndEscape(string, stringBuilder);
        //?} <1.21.5 {
        /*int length = stringBuilder.length();
        stringBuilder.append(' ');
        char c = 0;

        for(int index = 0; index < string.length(); ++index) {
            char charAt = string.charAt(index);
            if (charAt == '\\') {
                stringBuilder.append("\\\\");
            } else if (charAt != '"' && charAt != '\'') {
                String controlCharacters = escapeControlCharacters(charAt);
                if (controlCharacters != null) {
                    stringBuilder.append('\\');
                    stringBuilder.append(controlCharacters);
                } else {
                    stringBuilder.append(charAt);
                }
            } else {
                if (c == 0) {
                    c = (char)(charAt == '"' ? 39 : 34);
                }

                if (c == charAt) {
                    stringBuilder.append('\\');
                }

                stringBuilder.append(charAt);
            }
        }

        if (c == 0) {
            c = '"';
        }

        stringBuilder.setCharAt(length, c);
        stringBuilder.append(c);
        *///?}
    }

    //? <1.21.5 {
    /*public static String escapeControlCharacters(char c) {
        return switch (c) {
            case '\b' -> "b";
            case '\t' -> "t";
            case '\n' -> "n";
            case '\f' -> "f";
            case '\r' -> "r";
            default -> c < ' ' ? "x" + HEX_ESCAPE.toHexDigits((byte) c) : null;
        };
    }
    *///?}
}
