package com.kunzisoft.androidclearchroma;

/**
 * Utility class for pre-configured methods
 * @author Pavel Sikun
 */
public class ChromaUtil {

    private ChromaUtil() {
    }

    /**
     * Get the string link to a int color
     * @param color IntColor
     * @param showAlpha true if we want alpha channel
     * @return String associated with the color
     */
    public static String getFormattedColorString(int color, boolean showAlpha) {
        if(showAlpha) {
            return String.format("#%08X", color);
        }
        else {
            return String.format("#%06X", 0xFFFFFF & color);
        }
    }
}
