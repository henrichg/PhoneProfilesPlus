package com.kunzisoft.androidclearchroma.colormode.mode;

import android.graphics.Color;
import com.kunzisoft.androidclearchroma.R;
import com.kunzisoft.androidclearchroma.colormode.Channel;

import java.util.ArrayList;
import java.util.List;

/**
 * Class for managed Hue-Saturation-Lightness color mode
 * @author Pavel Sikun
 */
public class HSL implements AbstractColorMode {

//    thx @xpansive!
//    function hsv2hsl(a,b,c){return[a,b*c/((a=(2-b)*c)<1?a:2-a),a/2]}
//    function hsl2hsv(a,b,c){b*=c<.5?c:1-c;return[a,2*b/(c+b),c+b]}

    private float[] color2hsl(int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        return hsv2hsl(hsv);
    }

    private float[] hsv2hsl(float[] hsv) {
        float a = hsv[0];
        float b = hsv[1];
        float c = hsv[2];

        return new float[] { a, b * c / ((a = (2 - b) * c) < 1 ? a : 2), a/2 };
    }

    private float[] hsl2hsv(float[] hsl) {
        float a = hsl[0];
        float b = hsl[1];
        float c = hsl[2];

        b *= c < 0.5 ? c : 1 - c;
        if(b == 0) {
            b = 0.001f;
        }

        return new float[] { a, 2 * b / (c + b), c + b };
    }

    @Override
    public List<Channel> getChannels() {
        List<Channel> list = new ArrayList<>();

        list.add(new Channel(R.string.acch_channel_hue, 0, 360, new Channel.ColorExtractor() {
            @Override
            public int extract(int color) {
                return (int) color2hsl(color)[0];
            }
        }));

        list.add(new Channel(R.string.acch_channel_saturation, 0, 100, new Channel.ColorExtractor() {
            @Override
            public int extract(int color) {
                return 100 - (int) (color2hsl(color)[1] * 100);
            }
        }));

        list.add(new Channel(R.string.acch_channel_lightness, 0, 100, new Channel.ColorExtractor() {
            @Override
            public int extract(int color) {
                return (int) (color2hsl(color)[2] * 100);
            }
        }));

        return list;
    }

    @Override
    public int evaluateColor(List<Channel> channels) {

        float[] hsv = hsl2hsv(new float[]{
                ((float) channels.get(0).getProgress()),
                ((float) channels.get(1).getProgress()) / 100,
                ((float) channels.get(2).getProgress()) / 100
        });

        return Color.HSVToColor(hsv);
    }
}
