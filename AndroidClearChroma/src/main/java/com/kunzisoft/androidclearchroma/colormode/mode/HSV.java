package com.kunzisoft.androidclearchroma.colormode.mode;

import android.graphics.Color;

import com.kunzisoft.androidclearchroma.R;
import com.kunzisoft.androidclearchroma.colormode.Channel;

import java.util.ArrayList;
import java.util.List;

/**
 * Class for managed Hue-Saturation-Value color mode
 * @author Pavel Sikun
 */
@SuppressWarnings("unused")
public class HSV implements AbstractColorMode {

    private float[] colorToHSV(int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        return hsv;
    }

    @Override
    public List<Channel> getChannels() {
        List<Channel> list = new ArrayList<>();

        list.add(new Channel(R.string.acch_channel_hue, 0, 360, new Channel.ColorExtractor() {
            @Override
            public int extract(int color) {
                return (int) colorToHSV(color)[0];
            }
        }));

        list.add(new Channel(R.string.acch_channel_saturation, 0, 100, new Channel.ColorExtractor() {
            @Override
            public int extract(int color) {
                return (int) (colorToHSV(color)[1] * 100);
            }
        }));

        list.add(new Channel(R.string.acch_channel_value, 0, 100, new Channel.ColorExtractor() {
            @Override
            public int extract(int color) {
                return (int) (colorToHSV(color)[2] * 100);
            }
        }));

        return list;
    }

    @Override
    public int evaluateColor(List<Channel> channels) {
        return Color.HSVToColor(new float[]{
                ((float) channels.get(0).getProgress()),
                ((float) channels.get(1).getProgress()) / 100,
                ((float) channels.get(2).getProgress()) / 100
        });
    }
}
