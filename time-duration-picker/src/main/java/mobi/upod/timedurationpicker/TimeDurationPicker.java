package mobi.upod.timedurationpicker;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.widget.TooltipCompat;

/**
 * Control that allows the user to easily input a time duration made up of hours, minutes and seconds, like known from
 * the Lollipop stock timer app.
 *
 * @noinspection EmptyMethod
 */
@SuppressWarnings({"FieldCanBeLocal", "ConstantConditions"})
public class TimeDurationPicker extends FrameLayout {

    public static final int HH_MM_SS = 0;
    public static final int HH_MM = 1;
    public static final int MM_SS = 2;

    private int timeUnits = HH_MM_SS;

    private final TimeDurationString input;
    private final View displayRow;
    private final View durationView;
    private final TextView hoursView;
    private final TextView minutesView;
    private final TextView secondsView;
    private final TextView[] displayViews;
    private final TextView[] unitLabelViews;
    private final ImageButton backspaceButton;
    private final ImageButton clearButton;
    private final View separatorView;
    private final View buttonDividerView;
    private final View numPad;
    private final Button[] numPadButtons;
    private final Button numPadMeasureButton;
    //private OnDurationChangedListener changeListener = null;
    //private final TextView secondsLabel;
    private final TextView hoursLabel;
    private final TextView minutesLabel;

    /*
     * Implement this interface and set it using #setOnDurationChangeListener to get informed about input changes.
    interface OnDurationChangedListener {
         * Called whenever the input (the displayed duration string) changes.
         * param view the view that fired the event
         * param duration the new duration in milli seconds
        void onDurationChanged(TimeDurationPicker view, long duration);
    }
    */

    public TimeDurationPicker(Context context) {
        this(context, null);
    }

    public TimeDurationPicker(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.timeDurationPickerStyle);
    }

    public TimeDurationPicker(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        inflate(context, R.layout.time_duration_picker, this);

        //
        // find views
        //
        displayRow = findViewById(R.id.displayRow);
        durationView = findViewById(R.id.duration);
        hoursView = findViewById(R.id.hours);
        minutesView = findViewById(R.id.minutes);
        secondsView = findViewById(R.id.seconds);
        displayViews = new TextView[] { hoursView, minutesView, secondsView };

        hoursLabel = findViewById(R.id.hoursLabel);
        minutesLabel = findViewById(R.id.minutesLabel);
        //secondsLabel = findViewById(R.id.secondsLabel);
        unitLabelViews = new TextView[] { hoursLabel, minutesLabel/*, secondsLabel*/ };

        backspaceButton = findViewById(R.id.backspace);
        TooltipCompat.setTooltipText(backspaceButton, context.getString(R.string.backspace_button_tooltip));
        clearButton = findViewById(R.id.clear);
        TooltipCompat.setTooltipText(clearButton, context.getString(R.string.clear_button_tooltip));

        separatorView = findViewById(R.id.separator);
        buttonDividerView = findViewById(R.id.duration_dlg_buttonsDivider);

        numPad = findViewById(R.id.numPad);
        numPadMeasureButton = findViewById(R.id.numPadMeasure);
        numPadButtons = new Button[] {
                findViewById(R.id.numPad1), findViewById(R.id.numPad2), findViewById(R.id.numPad3),
                findViewById(R.id.numPad4), findViewById(R.id.numPad5), findViewById(R.id.numPad6),
                findViewById(R.id.numPad7), findViewById(R.id.numPad8), findViewById(R.id.numPad9),
                findViewById(R.id.numPad0), findViewById(R.id.numPad00)
        };

        input = new TimeDurationString(this);

        //
        // apply style
        //
        final TypedArray attributes = context.getTheme().obtainStyledAttributes(attrs, R.styleable.TimeDurationPicker, defStyleAttr, 0);
        try {
            applyPadding(attributes, R.styleable.TimeDurationPicker_numPadButtonPadding, numPadButtons);

            applyTextAppearance(/*context,*/ attributes, R.styleable.TimeDurationPicker_textAppearanceDisplay, displayViews);
            applyTextAppearance(/*context,*/ attributes, R.styleable.TimeDurationPicker_textAppearanceButton, numPadButtons);
            applyTextAppearance(/*context,*/ attributes, R.styleable.TimeDurationPicker_textAppearanceUnit, unitLabelViews);

            applyIcon(attributes, R.styleable.TimeDurationPicker_backspaceIcon, backspaceButton);
            applyIcon(attributes, R.styleable.TimeDurationPicker_clearIcon, clearButton);

            applyBackgroundColor(attributes, R.styleable.TimeDurationPicker_displaySeparatorColor, separatorView);
            applyBackgroundColor(attributes, R.styleable.TimeDurationPicker_buttonsSeparatorColor, buttonDividerView);
            //applyBackgroundColor(attributes, R.styleable.TimeDurationPicker_durationDisplayBackground, displayRow);

            applyUnits(attributes, R.styleable.TimeDurationPicker_timeUnits);
        } finally {
            attributes.recycle();
        }

        //
        // init actions
        //

        updateUnits();

        backspaceButton.setOnClickListener(v -> onBackspace());
        clearButton.setOnClickListener(v -> onClear());

        final OnClickListener numberClickListener = v -> onNumberClick(((Button) v).getText());
        for (Button button : numPadButtons) {
            button.setOnClickListener(numberClickListener);
        }

        //
        // init default value
        //
        updateHoursMinutesSeconds();
    }

    private void updateUnits() {
        hoursView.setVisibility(timeUnits == HH_MM_SS || timeUnits == HH_MM ? View.VISIBLE : View.GONE);
        hoursLabel.setVisibility(timeUnits == HH_MM_SS || timeUnits == HH_MM ? View.VISIBLE : View.GONE);
        secondsView.setVisibility(timeUnits == HH_MM_SS || timeUnits == MM_SS ? View.VISIBLE : View.GONE);
        //secondsLabel.setVisibility(timeUnits == HH_MM_SS || timeUnits == MM_SS ? View.VISIBLE : View.GONE);
        minutesLabel.setVisibility(timeUnits == HH_MM_SS || timeUnits == MM_SS ? View.VISIBLE : View.GONE);

        input.updateTimeUnits(timeUnits);
    }

    private void applyUnits(TypedArray attrs, int attributeIndex) {
        if (attrs.hasValue(attributeIndex)) {
            timeUnits = attrs.getInt(attributeIndex, 0);
        }
    }

    //
    // public interface
    //

    /**
     * Gets the current duration entered by the user.
     * @return the duration entered by the user in milliseconds.
     */
    public long getDuration() {
        return input.getDuration();
    }

    /**
     * Sets the current duration.
     * @param millis the duration in milliseconds
     */
    public void setDuration(long millis) {
        input.setDuration(millis);
        updateHoursMinutesSeconds();
    }

    /**
     * Sets time units to use
     * @param timeUnits One of {@link #HH_MM_SS}, {@link #HH_MM}, {@link #MM_SS}.
     */

    public void setTimeUnits(int timeUnits) {
        this.timeUnits = timeUnits;
        updateUnits();
    }

    /*
    /*
     * Sets a listener to be informed of updates to the entered duration.
     * @param listener the listener to be informed or {@code null} if no one should be informed.
    public void setOnDurationChangeListener(OnDurationChangedListener listener) {
        changeListener = listener;
    }
    */

    /**
     * Sets the text appearance for the entered duration (the large numbers in the upper area).
     * @param resId resource id of the style describing the text appearance.
     */
    public void setDisplayTextAppearance(int resId) {
        applyTextAppearance(/*getContext(),*/ resId, displayViews);
    }

    /**
     * Sets the text appearance for the small unit labels ("h", "m", "s") in the upper display area.
     * @param resId resource id of the style describing the text appearance.
     */
    public void setUnitTextAppearance(int resId) {
        applyTextAppearance(/*getContext(),*/ resId, unitLabelViews);
    }

    /*
     * Sets the text appearance for the number pad buttons.
     * @param resId resource id of the style describing the text appearance.
    public void setButtonTextAppearance(int resId) {
        applyTextAppearance(resId, numPadButtons);
    }
    */

    /**
     * Sets the icon to be shown on the backspace button.
     * @param icon backspace drawable
     */
    public void setBackspaceIcon(Drawable icon) {
        backspaceButton.setImageDrawable(icon);
    }

    /**
     * Sets the icon for the clear button.
     * @param icon clear drawable
     */
    public void setClearIcon(Drawable icon) {
        clearButton.setImageDrawable(icon);
    }

    /**
     * Sets the color of the separator line between the duration display and the number pad.
     * @param color color value
     */
    public void setDisplaySeparatorColor(int color) {
        separatorView.setBackgroundColor(color);
    }
    public void setButtonsSeparatorColor(int color) {
        buttonDividerView.setBackgroundColor(color);
    }

    /**
     * Sets the background color of the upper duration display area.
     * @param color color value
     */
    public void setDurationDisplayBackgroundColor(int color) {
        displayRow.setBackgroundColor(color);
    }

    /*
     * Sets the padding to be used for the number pad buttons.
     * @param padding padding in pixels
    public void setNumPadButtonPadding(int padding) {
        applyPadding(padding, numPadButtons);
    }
    */

    //
    // style helpers
    //

    private void applyPadding(TypedArray attrs, int attributeIndex, final View[] targetViews) {
        final int padding = attrs.getDimensionPixelSize(attributeIndex, -1);
        if (padding > -1) {
            applyPadding(padding, targetViews);
        }
    }

    private void applyPadding(int padding, final View[] targetViews) {
        for (View view : targetViews) view.setPadding(padding, padding, padding, padding);
    }

    private void applyTextAppearance(/*Context context,*/ TypedArray attrs, int attributeIndex, final TextView[] targetViews) {
        final int id = attrs.getResourceId(attributeIndex, 0);
        if (id != 0) {
            applyTextAppearance(/*context,*/ id, targetViews);
        }
    }

    private void applyTextAppearance(/*Context context,*/ int resId, final TextView[] targetViews) {
        for (TextView view : targetViews) view.setTextAppearance(resId);
    }

    private void applyIcon(TypedArray attrs, int attributeIndex, ImageView targetView) {
        final Drawable icon = attrs.getDrawable(attributeIndex);
        if (icon != null) {
            targetView.setImageDrawable(icon);
        }
    }

    private void applyBackgroundColor(TypedArray attrs, int attributeIndex, View targetView) {
        if (attrs.hasValue(attributeIndex)) {
            final int color = attrs.getColor(attributeIndex, 0);
            targetView.setBackgroundColor(color);
        }
    }

    /*
    private void applyLeftMargin(int margin, View... targetViews) {
        for (View view : targetViews) {
            final LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) view.getLayoutParams();

            boolean isLeftToRight = TextUtils.getLayoutDirectionFromLocale(Locale.getDefault())==View.LAYOUT_DIRECTION_LTR;
            if (isLeftToRight)
                params.setMargins(margin, params.topMargin, params.rightMargin, params.bottomMargin);
            else
                params.setMargins(params.leftMargin, params.topMargin, margin, params.bottomMargin);

            view.setLayoutParams(params);
        }
    }
    */

    //
    // event helpers
    //

    private void onBackspace() {
        input.popDigit();
        updateHoursMinutesSeconds();
    }

    private void onClear() {
        input.clear();
        updateHoursMinutesSeconds();
    }

    private void onNumberClick(final CharSequence digits) {
        input.pushNumber(digits);
        updateHoursMinutesSeconds();
    }

    private void updateHoursMinutesSeconds() {
        hoursView.setText(input.getHoursString());
        minutesView.setText(input.getMinutesString());
        secondsView.setText(input.getSecondsString());
        //fireDurationChangeListener();
    }

    /*
    private void fireDurationChangeListener() {
        if (changeListener != null) {
            changeListener.onDurationChanged(this, input.getDuration());
        }
    }
    */

    //
    // layouting
    //

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int touchableSize = getContext().getResources().getDimensionPixelSize(R.dimen.touchable);
        final int dummyMeasureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);

        /*
        // set spacing between units
        hoursView.measure(dummyMeasureSpec, dummyMeasureSpec);
        final TextView unitLabelView = unitLabelViews[2];
        unitLabelView.measure(dummyMeasureSpec, dummyMeasureSpec);
        final int unitSpacing = Math.max(hoursView.getMeasuredWidth() / 3, (int) (1.2f * unitLabelView.getMeasuredWidth()));
        applyLeftMargin(unitSpacing, minutesView, secondsView);
        */

        // calculate size for display row
        durationView.measure(dummyMeasureSpec, dummyMeasureSpec);
        final int minDisplayWidth = durationView.getMeasuredWidth() + 2 * touchableSize;
        final int minDisplayHeight = Math.max(durationView.getMeasuredHeight(), touchableSize);

        // calculate size for num pad
        numPadMeasureButton.measure(dummyMeasureSpec, dummyMeasureSpec);
        final int minNumPadButtonSize = Math.max(Math.max(numPadMeasureButton.getMeasuredHeight(), numPadMeasureButton.getMeasuredWidth()), touchableSize);
        final int minNumPadWidth = 3 * minNumPadButtonSize;
        final int minNumPadHeight = 4 * minNumPadButtonSize;

        // calculate overall size
        final int minWidth = Math.max(minDisplayWidth, minNumPadWidth);
        final int minHeight = minDisplayHeight + minNumPadHeight;

        // respect measure spec
        final int availableWidth = MeasureSpec.getSize(widthMeasureSpec);
        final int availableHeight = MeasureSpec.getSize(heightMeasureSpec);
        final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        final int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        final int preferredWidth = widthMode == MeasureSpec.EXACTLY ? availableWidth : minWidth;
        final int preferredHeight = heightMode == MeasureSpec.EXACTLY ? availableHeight : minHeight;

        // measure the display
        final int displayRowWidth = Math.max(minDisplayWidth, preferredWidth);
        displayRow.measure(MeasureSpec.makeMeasureSpec(displayRowWidth, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(minDisplayHeight, MeasureSpec.EXACTLY));

        // measure the numPad
        // if we have more space available, we can try to grow the num pad
        final int numPadWidth = Math.max(minNumPadWidth, displayRowWidth);
        final int numPadHeight = Math.max(minNumPadHeight, preferredHeight - minDisplayHeight);
        numPad.measure(MeasureSpec.makeMeasureSpec(numPadWidth, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(numPadHeight, MeasureSpec.EXACTLY));

        // forward calculated size to super implementation
        final int width = Math.max(displayRowWidth, numPadWidth);
        final int height = minDisplayHeight + numPadHeight;
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        final int width = right - left;

        // layout display row
        final int displayRowWidth = displayRow.getMeasuredWidth();
        final int displayRowHeight = displayRow.getMeasuredHeight();
        final int displayRowX = (width - displayRowWidth) / 2;
        displayRow.layout(displayRowX, 0, displayRowX + displayRowWidth, displayRowHeight);

        // layout num pad
        final int numPadWidth = numPad.getMeasuredWidth();
        final int numPadHeight = numPad.getMeasuredHeight();
        final int numPadX = (width - numPadWidth) / 2;
        numPad.layout(numPadX, displayRowHeight, numPadX + numPadWidth, displayRowHeight + numPadHeight);
    }

    //
    // state handling
    //

    @Override
    protected Parcelable onSaveInstanceState() {
        return new SavedState(super.onSaveInstanceState(), input.getInputString());
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (!(state instanceof SavedState))
            throw new IllegalArgumentException("Expected state of class " + SavedState.class.getName() + " but received state of class " + state.getClass().getName());

        final SavedState savedStated = (SavedState) state;
        super.onRestoreInstanceState(savedStated.getSuperState());
        input.clear();
        input.pushNumber(savedStated.durationInput);
        updateHoursMinutesSeconds();
    }

    /**
     * Encapsulates the digit input logic and text to duration conversion logic.
     */
    private static class TimeDurationString {
        private final TimeDurationPicker timeDurationPicker;
        private int timeUnits;
        private int maxDigits = 6;
        private long duration = 0;
        private final StringBuilder input = new StringBuilder(maxDigits);

        TimeDurationString(TimeDurationPicker picker) {
            timeDurationPicker = picker;
            padWithZeros();
            //enableNumPadButtons();
        }

        private void updateTimeUnits(int timeUnits) {
            this.timeUnits = timeUnits;
            setMaxDigits(timeUnits);
//            Log.e("TimeDurationPicker.updateTimeUnits", "enableNumPadButtons");
//            removeLeadingZeros();
//            enableNumPadButtons();
//            padWithZeros();
        }

        private void setMaxDigits(int timeUnits) {
            if (timeUnits == TimeDurationPicker.HH_MM_SS)
                maxDigits = 6;
            else
                maxDigits = 4;
            setDuration(duration);
        }

        void pushNumber(final CharSequence digits) {
            for (int i = 0; i < digits.length(); ++i)
                pushDigit(digits.charAt(i));
        }

        void pushDigit(final char digit) {
            if (!Character.isDigit(digit))
                throw new IllegalArgumentException("Only numbers are allowed");

            removeLeadingZeros();
            if (input.length() < maxDigits && (input.length() > 0 || digit != '0')) {
                input.append(digit);
            }
            //Log.e("TimeDurationPicker.pushDigit", "enableNumPadButtons");
            enableNumPadButtons();
            padWithZeros();
        }

        void popDigit() {
            if (input.length() > 0)
                input.deleteCharAt(input.length() - 1);
            //Log.e("TimeDurationPicker.pushDigit", "popDigit");
            enableNumPadButtons();
            padWithZeros();
        }

        void clear() {
            input.setLength(0);
            //Log.e("TimeDurationPicker.pushDigit", "clear");
            enableNumPadButtons();
            padWithZeros();
        }

        String getHoursString() {
            return timeUnits == HH_MM_SS || timeUnits == HH_MM ? input.substring(0, 2) : "00";
        }

        String getMinutesString() {
            if (timeUnits == HH_MM_SS || timeUnits == HH_MM) return input.substring(2, 4);
            else if (timeUnits == MM_SS) return input.substring(0, 2);
            else return "00";
        }

        String getSecondsString() {
            if (timeUnits == HH_MM_SS) return input.substring(4, 6);
            else if (timeUnits == MM_SS) return input.substring(2, 4);
            else return "00";
        }

        String getInputString() {
            return input.toString();
        }

        long getDuration() {
            final int hours = Integer.parseInt(getHoursString());
            final int minutes = Integer.parseInt(getMinutesString());
            final int seconds = Integer.parseInt(getSecondsString());
            return TimeDurationUtil.durationOf(hours, minutes, seconds);
        }

        void setDuration(long millis) {
            duration = millis;
            setDuration(
                TimeDurationUtil.hoursOf(millis),
                timeUnits == MM_SS ? TimeDurationUtil.minutesOf(millis) : TimeDurationUtil.minutesInHourOf(millis),
                TimeDurationUtil.secondsInMinuteOf(millis));
            //Log.e("TimeDurationPicker.setDuration", "enableNumPadButtons");
            removeLeadingZeros();
            enableNumPadButtons();
            padWithZeros();
        }

        private void setDuration(long hours, long minutes, long seconds) {
            if (hours > 99 || minutes > 99)
                setDurationString("99", "99", "99");
            else
                setDurationString(stringFragment(hours), stringFragment(minutes), stringFragment(seconds));
        }

        private void setDurationString(String hours, String minutes, String seconds) {
            input.setLength(0);
            if (timeUnits == HH_MM || timeUnits == HH_MM_SS)
                input.append(hours);
            input.append(minutes);
            if (timeUnits == HH_MM_SS || timeUnits == MM_SS)
                input.append(seconds);
        }

        private void removeLeadingZeros() {
            while (input.length() > 0 && input.charAt(0) == '0')
                input.deleteCharAt(0);
        }

        private void padWithZeros() {
            while (input.length() < maxDigits)
                input.insert(0, '0');
        }

        private String stringFragment(long value) {
            return (value < 10 ? "0" : "") + value;
        }

        private void enableNumPadButtons() {
            boolean enable = input.length() < maxDigits;
            for (Button numPadButton : timeDurationPicker.numPadButtons) {
                numPadButton.setEnabled(enable);
            }
        }

    }

    /**
     * User interface state that is stored by this view for implementing
     * {@link View#onSaveInstanceState}.
     */
    public static class SavedState extends BaseSavedState {
        final String durationInput;

        SavedState(Parcelable superState, String durationInput) {
            super(superState);
            this.durationInput = durationInput;
        }

        public SavedState(Parcel source) {
            super(source);
            durationInput = source.readString();
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeString(durationInput);
        }
        
        public static final Parcelable.Creator<TimeDurationPicker.SavedState> CREATOR
                = new Parcelable.Creator<>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }
}
