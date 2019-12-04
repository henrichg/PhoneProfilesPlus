package com.kunzisoft.androidclearchroma.colormode.mode;

import com.kunzisoft.androidclearchroma.colormode.Channel;

import java.util.List;

/**
 * Class that will be derived for managing color modes
 */
public interface AbstractColorMode {
    /**
     * Return int color defined by each channel
     * @param channels List of color channels use in this mode
     * @return IntColor
     */
    int evaluateColor(List<Channel> channels);

    /**
     * Get each channel of color
     * @return List of channels
     */
    List<Channel> getChannels();
}
