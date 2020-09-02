package com.kunzisoft.androidclearchroma;

/**
 * Indicator mode of color, can be Decimal or Hexadecimal
 * @author Pavel Sikun
 */
public enum IndicatorMode {
    DECIMAL(0), HEX(1);

    @SuppressWarnings("CanBeFinal")
    private int i;

    /**
     * Unique id of IndicatorMode
     * @param id unique ID
     */
    IndicatorMode(int id) {
        i=id;
    }

    /**
     * Get id of Indicator Mode
     * @return ID
     */
    @SuppressWarnings("unused")
    public int getId() {
        return i;
    }

    /**
     * Return mode from ID
     * @param id Unique ID
     * @return Mode
     */
    @SuppressWarnings("unused")
    public static IndicatorMode getIndicatorModeFromId(int id) {
        switch (id) {
            default:
            case 0:
                return DECIMAL;
            case 1:
                return HEX;
        }
    }
}
