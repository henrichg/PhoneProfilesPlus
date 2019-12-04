package com.kunzisoft.androidclearchroma.view;

import android.content.Context;
import android.os.Build;
import androidx.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.kunzisoft.androidclearchroma.IndicatorMode;
import com.kunzisoft.androidclearchroma.R;
import com.kunzisoft.androidclearchroma.colormode.Channel;

/**
 * Channel view to show a color channel
 * @author JJamet, Pavel Sikun
 */
public class ChannelView extends RelativeLayout {

    private TextView label;
    private TextView progressView;
    private SeekBar seekbar;

    private Channel channel;
    private IndicatorMode indicatorMode;

    private OnProgressChangedListener listener;

    public ChannelView(Context context) {
        super(context);
        init(context);
    }

    public ChannelView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ChannelView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public ChannelView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(Context context) {
        View rootView = inflate(context, R.layout.acch_channel_row, this);
        label = rootView.findViewById(R.id.label);
        progressView = rootView.findViewById(R.id.progress_text);
        seekbar = rootView.findViewById(R.id.seekbar);
    }

    private void bindViews() {
        label.setText(getContext().getString(channel.getNameResourceId()));

        setProgress(progressView, channel.getProgress());

        seekbar.setMax(channel.getMax());
        seekbar.setProgress(channel.getProgress());

        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                channel.setProgress(progress);
                setProgress(progressView, progress);
                if(listener != null) {
                    listener.onProgressChanged();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });
    }

    private void setProgress(TextView view, int progress) {
        view.setText(indicatorMode == IndicatorMode.HEX
                ? Integer.toHexString(progress)
                : String.valueOf(progress));
    }

    /**
     * Attach a change listener to the channel view
     * @param listener Listener to attach
     */
    public void registerListener(OnProgressChangedListener listener) {
        this.listener = listener;
    }

    /**
     * Set a new channel to the view, the IndicatorMode.DECIMAL is used
     * @param channel Channel to assign
     */
    public void setChannel(Channel channel) {
        setChannel(channel, IndicatorMode.DECIMAL);
    }

    /**
     * Set a new channel to the view with a specific IndicatorMode
     * @param channel Channel to assign
     * @param indicatorMode Indicator to use
     */
    public void setChannel(Channel channel, IndicatorMode indicatorMode) {
        this.channel = channel;
        this.indicatorMode = indicatorMode;

        bindViews();
    }

    public Channel getChannel() {
        return channel;
    }

    public IndicatorMode getIndicatorMode() {
        return indicatorMode;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        listener = null;
    }

    public interface OnProgressChangedListener {
        void onProgressChanged();
    }
}
