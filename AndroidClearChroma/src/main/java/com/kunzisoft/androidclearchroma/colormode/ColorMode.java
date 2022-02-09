package com.kunzisoft.androidclearchroma.colormode;

import com.kunzisoft.androidclearchroma.colormode.mode.ARGB;
import com.kunzisoft.androidclearchroma.colormode.mode.AbstractColorMode;
import com.kunzisoft.androidclearchroma.colormode.mode.CMYK;
import com.kunzisoft.androidclearchroma.colormode.mode.CMYK255;
import com.kunzisoft.androidclearchroma.colormode.mode.HSL;
import com.kunzisoft.androidclearchroma.colormode.mode.HSV;
import com.kunzisoft.androidclearchroma.colormode.mode.RGB;

/**
 * Enumeration of each color mode
 * @author JJamet
 */
public enum ColorMode {
    RGB(0), HSV(1), ARGB(2), CMYK(3), CMYK255(4), HSL(5);

    private final int i;

    /**
     * Construct color mode with an unique ID
     * @param id Id of mode
     */
    ColorMode(int id) {
        i = id;
    }

    /**
     * Get unique ID of mode
     * @return ID
     */
    @SuppressWarnings("unused")
    public int getId() {
        return i;
    }

    /**
     * Get color mode object link to mode
     * @return Color mode object
     */
    public AbstractColorMode getColorMode() {
        switch (this) {
            case RGB:
            default:
                return new RGB();
            case HSV:
                return new HSV();
            case ARGB:
                return new ARGB();
            case CMYK:
                return new CMYK();
            case CMYK255:
                return new CMYK255();
            case HSL:
                return new HSL();
        }
    }

    /**
     * Retrieves the color mode from id.
     * @param id Unique ID
     * @return Color mode
     */
    @SuppressWarnings("unused")
    public static ColorMode getColorModeFromId(int id) {
        switch (id) {
            case(0):
            default:
                return RGB;
            case(1):
                return HSV;
            case(2):
                return ARGB;
            case(3):
                return CMYK;
            case(4):
                return CMYK255;
            case(5):
                return HSL;
        }
    }
}
