package de.in.dicom.tools.ui;

/**
 * Utility class for converting byte arrays to hexadecimal string
 * representations.
 *
 * @author TiJaWo68 in cooperation with Gemini 3 Flash using Antigravity
 */
public class HexUtils {

    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

    public static String toHexString(byte[] bytes) {
        if (bytes == null)
            return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            if (i > 0)
                sb.append(' ');
            int v = bytes[i] & 0xFF;
            sb.append(HEX_ARRAY[v >>> 4]);
            sb.append(HEX_ARRAY[v & 0x0F]);
        }
        return sb.toString();
    }

    public static String toHexString(byte[] bytes, int maxLength) {
        if (bytes == null)
            return "";
        int len = Math.min(bytes.length, maxLength);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len; i++) {
            if (i > 0)
                sb.append(' ');
            int v = bytes[i] & 0xFF;
            sb.append(HEX_ARRAY[v >>> 4]);
            sb.append(HEX_ARRAY[v & 0x0F]);
        }
        if (bytes.length > maxLength) {
            sb.append(" ...");
        }
        return sb.toString();
    }
}
