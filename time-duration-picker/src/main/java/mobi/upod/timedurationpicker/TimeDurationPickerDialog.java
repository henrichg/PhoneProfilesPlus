package mobi.upod.timedurationpicker;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;

/**
 * A simple dialog containing a {@link TimeDurationPicker}.
 *
 * See the <a href="https://developer.android.com/guide/topics/ui/controls/pickers.html">Pickers</a> guide in the
 * android documentation of how to use this. You can derive your own fragment from {@link TimeDurationPickerDialogFragment}.
 *
 * @see TimeDurationPicker
 * @see TimeDurationPickerDialogFragment
 */
public class TimeDurationPickerDialog extends AlertDialog implements DialogInterface.OnClickListener {
    private static final String DURATION = "duration";
    private final TimeDurationPicker durationInputView;
    private final OnDurationSetListener durationSetListener;

    /**
     * The callback used to indicate the user is done entering the duration.
     */
    public interface OnDurationSetListener {
        /**
         * Called when the user leaves the dialog using the OK button.
         * @param view the picker view.
         * @param duration the duration that was entered.
         */
        void onDurationSet(TimeDurationPicker view, long duration);
    }

    /**
     * Creates a time duration picker dialog.
     * @param context the context for the dialog
     * @param listener the listener to be informed about entered duration
     * @param duration the initial duration to be shown in the dialog
     */
    @SuppressWarnings("WeakerAccess")
    public TimeDurationPickerDialog(Context context, OnDurationSetListener listener, long duration) {
        super(context);
        durationSetListener = listener;

        final LayoutInflater inflater = LayoutInflater.from(context);
        @SuppressLint("InflateParams")
        final View view = inflater.inflate(R.layout.time_duration_picker_dialog, null);
        setView(view);
        setButton(BUTTON_POSITIVE, context.getString(android.R.string.ok), this);
        setButton(BUTTON_NEGATIVE, context.getString(android.R.string.cancel), this);

        durationInputView = (TimeDurationPicker) view;
        durationInputView.setDuration(duration);
    }

    /**
     * Creates a time duration picker dialog.
     * @param context the context for the dialog
     * @param listener the listener to be informed about entered duration
     * @param duration the initial duration to be shown in the dialog
     * @param timeUnits the units of time to display
     */
    public TimeDurationPickerDialog(Context context, OnDurationSetListener listener, long duration, int timeUnits) {

        this(context,listener,duration);
        durationInputView.setTimeUnits(timeUnits);
    }

    /**
     * Gets the current entered duration.
     * @return the current duration in milliseconds.
     */
    @SuppressWarnings("unused")
    public TimeDurationPicker getDurationInput() {
        return durationInputView;
    }

    /**
     * Sets the duration to be shown in the dialog.
     * @param duration duration in milliseconds.
     */
    public void setDuration(long duration) {
        durationInputView.setDuration(duration);
    }

    //
    // internal stuff
    //

    @Override
    public void onClick(DialogInterface dialog, int which) {
        switch (which) {
            case BUTTON_POSITIVE:
                if (durationSetListener != null) {
                    durationSetListener.onDurationSet(durationInputView, durationInputView.getDuration());
                }
                break;
            case BUTTON_NEGATIVE:
                cancel();
                break;
        }
    }

    @NonNull
    @Override
    public Bundle onSaveInstanceState() {
        final Bundle state = super.onSaveInstanceState();
        state.putLong(DURATION, durationInputView.getDuration());
        return state;
    }

    @Override
    public void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        final long duration = savedInstanceState.getLong(DURATION);
        durationInputView.setDuration(duration);
    }
}
