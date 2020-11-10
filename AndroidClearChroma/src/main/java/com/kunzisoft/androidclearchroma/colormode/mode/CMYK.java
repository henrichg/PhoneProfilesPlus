package com.kunzisoft.androidclearchroma.colormode.mode;

import android.graphics.Color;

import com.kunzisoft.androidclearchroma.R;
import com.kunzisoft.androidclearchroma.colormode.Channel;

import java.util.ArrayList;
import java.util.List;

/**
 * Class for managed Cyan-Magenta-Yellow-Black color mode <br />
 * Each value of channel is between 0 and 100
 * @author JJamet
 */
public class CMYK implements AbstractColorMode {

    protected int MAX_VALUE;

    public CMYK() {
        MAX_VALUE = 100;
    }
    
    private double fraction() {
        return 255 / (double) MAX_VALUE;
    }

    @Override
    public List<Channel> getChannels() {
        List<Channel> list = new ArrayList<>();

        list.add(new Channel(R.string.acch_channel_cyan, 0, MAX_VALUE, color -> {
            // C = (1-R'-K) / (1-K)
            return (int) (MAX_VALUE * ((MAX_VALUE - (Color.red(color)/fraction()) - black(color) ) / (MAX_VALUE - black(color))));
        }));

        list.add(new Channel(R.string.acch_channel_magenta, 0, MAX_VALUE, color -> {
            // M = (1-G'-K) / (1-K)
            return (int) (MAX_VALUE * ((MAX_VALUE - (Color.green(color)/fraction()) - black(color) ) / (MAX_VALUE - black(color))));
        }));

        list.add(new Channel(R.string.acch_channel_yellow, 0, MAX_VALUE, color -> {
            // M = (1-Y'-K) / (1-K)
            return (int) (MAX_VALUE * ((MAX_VALUE - (Color.blue(color)/fraction()) - black(color) ) / (MAX_VALUE - black(color))));
        }));

        list.add(new Channel(R.string.acch_channel_black, 0, MAX_VALUE, color -> (int) black(color)));

        return list;
    }

    // K = 1-max(R', G', B')
    private double black(int color) {
        return MAX_VALUE - Math.max(Math.max(Color.red(color)/fraction(), Color.green(color)/fraction()), Color.blue(color)/fraction());
    }

    @Override
    public int evaluateColor(List<Channel> channels) {
        return Color.rgb(
                convertToRGB(channels.get(0), channels.get(3)),
                convertToRGB(channels.get(1), channels.get(3)),
                convertToRGB(channels.get(2), channels.get(3)));
    }

    private int convertToRGB(Channel colorChan, Channel blackChan) {
        return (int)((255 - colorChan.getProgress() * fraction()) * (255 - blackChan.getProgress() * fraction())) / 255;
    }
}
