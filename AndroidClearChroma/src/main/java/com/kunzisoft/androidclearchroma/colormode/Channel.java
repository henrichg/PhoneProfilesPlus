package com.kunzisoft.androidclearchroma.colormode;

/**
 * Channel for manipulate data of element in color mode
 * @author Pavel Sikun
 */
public final class Channel {

    private final int nameResourceId;
    private final int min;
    private final int max;
    private final ColorExtractor extractor;

    private int progress = 0;

    public Channel(int nameResourceId, int min, int max, ColorExtractor extractor) {
        this.nameResourceId = nameResourceId;
        this.min = min;
        this.max = max;
        this.extractor = extractor;
    }

    public Channel(int nameResourceId, int min, int max, int progress, ColorExtractor extractor) {
        this.nameResourceId = nameResourceId;
        this.min = min;
        this.max = max;
        this.extractor = extractor;
        this.progress = progress;
    }

    /**
     * Get resource id of channel <br />
     * Used to name the channel of color mode.
     * @return NameId
     */
    public int getNameResourceId() {
        return nameResourceId;
    }

    /**
     * Return the minimum channel value.
     * @return Minimum
     */
    public int getMin() {
        return min;
    }

    /**
     * Return the maximum channel value.
     * @return Maximum
     */
    public int getMax() {
        return max;
    }

    /**
     * Get color extractor
     * @return Extractor
     */
    public ColorExtractor getExtractor() {
        return extractor;
    }

    /**
     * Get current progress of channel
     * @return Current progress
     */
    public int getProgress() {
        return progress;
    }

    /**
     * Assign current progress of channel
     * @param progress Between minimum and maximum
     */
    public void setProgress(int progress) {
        this.progress = progress;
    }

    /**
     * Must be implemented for extract color in a color mode
     */
    public interface ColorExtractor {

        /**
         * Extract current color
         * @param color Color to extracted
         * @return IntColor
         */
        int extract(int color);
    }
}
