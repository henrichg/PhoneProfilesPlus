package com.kunzisoft.androidclearchroma.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.AppCompatImageView;

import com.kunzisoft.androidclearchroma.IndicatorMode;
import com.kunzisoft.androidclearchroma.R;
import com.kunzisoft.androidclearchroma.colormode.Channel;
import com.kunzisoft.androidclearchroma.colormode.ColorMode;
import com.kunzisoft.androidclearchroma.listener.OnColorChangedListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Chroma color view to show a color view with channels
 * @author JJamet
 */
public class ChromaColorView extends RelativeLayout {

    private @ColorInt
    int currentColor = Color.GRAY;
    private ColorMode colorMode = ColorMode.RGB;
    private IndicatorMode indicatorMode = IndicatorMode.DECIMAL;

    private AppCompatImageView colorView;

    private OnColorChangedListener mOnColorChangedListener;

    public ChromaColorView(Context context) {
        super(context);
        init(context, null);
    }

    public ChromaColorView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public ChromaColorView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public ChromaColorView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    private void init(Context context, @Nullable AttributeSet attributeSet) {

        TypedArray a = getContext().obtainStyledAttributes(attributeSet, R.styleable.ChromaColorView);
        try {
            currentColor = a.getColor(R.styleable.ChromaPreference_chromaInitialColor, currentColor);

            colorMode = ColorMode.values()[
                    a.getInt(R.styleable.ChromaPreference_chromaColorMode,
                            colorMode.ordinal())];

            indicatorMode = IndicatorMode.values()[
                    a.getInt(R.styleable.ChromaPreference_chromaIndicatorMode,
                            indicatorMode.ordinal())];
        }
        finally {
            a.recycle();
        }

        View root = inflate(context, R.layout.acch_chroma_color, this);

        colorView = root.findViewById(R.id.color_view);
        createView();
    }

    private void createView() {
        Drawable colorViewDrawable = new ColorDrawable(currentColor);
        colorView.setImageDrawable(colorViewDrawable);

        ViewGroup channelContainer = findViewById(R.id.channel_container);
        channelContainer.removeAllViews();

        List<Channel> channels = colorMode.getColorMode().getChannels();
        final List<ChannelView> channelViews = new ArrayList<>();
        for (Channel channel : channels) {
            ChannelView channelView = new ChannelView(getContext());

            // Assign the progress from the color
            channel.setProgress(channel.getExtractor().extract(currentColor));
            if(channel.getProgress() < channel.getMin() || channel.getProgress() > channel.getMax()) {
                throw new IllegalArgumentException(
                        "Initial progress " + channel.getProgress()
                                + " for channel: " + channel.getClass().getSimpleName()
                                + " must be between " + channel.getMin() + " and " + channel.getMax());
            }

            channelView.setChannel(channel, indicatorMode);
            channelViews.add(channelView);
        }

        ChannelView.OnProgressChangedListener seekBarChangeListener = new ChannelView.OnProgressChangedListener() {
            @Override
            public void onProgressChanged() {
                List<Channel> channels = new ArrayList<>();
                for (ChannelView chan : channelViews) {
                    channels.add(chan.getChannel());
                }
                currentColor = colorMode.getColorMode().evaluateColor(channels);
                // Listener for color selected in real time
                if (mOnColorChangedListener != null)
                    mOnColorChangedListener.onColorChanged(currentColor);

                // Change view for visibility of color
                Drawable colorViewDrawable = new ColorDrawable(currentColor);
                colorView.setImageDrawable(colorViewDrawable);
            }
        };

        for (ChannelView c : channelViews) {
            channelContainer.addView(c);
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) c.getLayoutParams();
            params.topMargin =
                    getResources().getDimensionPixelSize(R.dimen.acch_channel_view_margin_top);
            params.bottomMargin =
                    getResources().getDimensionPixelSize(R.dimen.acch_channel_view_margin_bottom);

            c.registerListener(seekBarChangeListener);
        }
    }

    @Override
    public void invalidate() {
        createView();
        super.invalidate();
    }

    public void setOnColorChangedListener(OnColorChangedListener listener) {
        mOnColorChangedListener = listener;
    }

    public int getCurrentColor() {
        return currentColor;
    }

    public void setCurrentColor(int currentColor) {
        this.currentColor = currentColor;
        invalidate();
    }

    public ColorMode getColorMode() {
        return colorMode;
    }

    public void setColorMode(@NonNull ColorMode colorMode) {
        this.colorMode = colorMode;
        invalidate();
    }

    public IndicatorMode getIndicatorMode() {
        return indicatorMode;
    }

    public void setIndicatorMode(@NonNull IndicatorMode indicatorMode) {
        this.indicatorMode = indicatorMode;
        invalidate();
    }
}
