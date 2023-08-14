package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.preference.DialogPreference;
import androidx.preference.PreferenceViewHolder;

public class RestartEventsIconColorChooserPreference extends DialogPreference {

    RestartEventsIconColorChooserPreferenceFragment fragment;

    private ImageView imageView;

    String value;

    final Context context;

    final int[] mColors;
    final int defaultColor;

    public RestartEventsIconColorChooserPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        this.context = context;

        //noinspection resource
        final TypedArray ta = context.getResources().obtainTypedArray(R.array.colorChooserDialog_colors);
        mColors = new int[ta.length()];
        for (int i = 0; i < ta.length(); i++) {
            mColors[i] = ta.getColor(i, 0);
        }
        ta.recycle();

        this.defaultColor = 0xff1ea0df;

        setWidgetLayoutResource(R.layout.preference_widget_restart_events_icon_color_chooser_preference); // resource na layout custom preference - TextView-ImageView

        setPositiveButtonText(null);
    }

    @Override
    public void onBindViewHolder(@NonNull PreferenceViewHolder holder)
    {
        super.onBindViewHolder(holder);

        imageView = (ImageView)holder.findViewById(R.id.restart_events_icon_pref_imageview);

        setColorInWidget();
    }

    void setBackgroundCompat(View view, Drawable d) {
        view.setBackground(d);
    }

    private void setColorInWidget() {

        int color = Integer.parseInt(value);

        DataWrapper dataWrapper = new DataWrapper(context.getApplicationContext(), false, 0, false, 0, 0, 0f);
        Profile restartEvents = DataWrapperStatic.getNonInitializedProfile(dataWrapper.context.getString(R.string.menu_restart_events),
                StringConstants.PROFILE_ICON_RESTART_EVENTS+"|1|1|"+color, 0);
        restartEvents.generateIconBitmap(dataWrapper.context, false, 0, false);
        dataWrapper.invalidateDataWrapper();

        Bitmap bitmap = restartEvents.increaseProfileIconBrightnessForContext(context, restartEvents._iconBitmap);
        if (bitmap != null)
            restartEvents._iconBitmap = bitmap;

        imageView.setImageBitmap(restartEvents._iconBitmap);

/*
        Drawable selector = createSelector(color);
        int[][] states = new int[][]{
                new int[]{-android.R.attr.state_pressed},
                new int[]{android.R.attr.state_pressed}
        };
        int[] colors = new int[]{
                shiftColor(color),
                color
        };
        ColorStateList rippleColors = new ColorStateList(states, colors);
        setBackgroundCompat(widgetLayout, new RippleDrawable(rippleColors, selector, null));

//        Handler handler = new Handler(context.getMainLooper());
//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                setSummary(R.string.empty_string);
//            }
//        }, 200);
 */
    }

    @Override
    protected Object onGetDefaultValue(@NonNull TypedArray ta, int index)
    {
        super.onGetDefaultValue(ta, index);
        return ta.getString(index);
    }

    @Override
    protected void onSetInitialValue(Object defaultValue) {
        value = getPersistedString((String) defaultValue);
        //setSummaryCCHP(value);
    }

    void persistValue() {
        if (callChangeListener(value))
        {
            persistString(value);
            setColorInWidget();
            //setSummaryCCHP(value);
        }
    }

    /*
    private void setSummaryCCHP(String value)
    {
        int color = Integer.parseInt(value);
        setSummary(ChromaUtil.getFormattedColorString(color, false));
    }
    */

    int shiftColor(int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] *= 0.9f; // value component
        return Color.HSVToColor(hsv);
    }

    Drawable createSelector(int color) {
        /*int position = -1;
        for (int i = 0; i < mColors.length; i++) {
            if (mColors[i] == color) {
                position = i;
                break;
            }
        }*/

        String applicationTheme = ApplicationPreferences.applicationTheme(context, true);
        //String applicationTheme; // = "white";// = ApplicationPreferences.applicationTheme(context, true);
        //if (GlobalGUIRoutines.isNightModeEnabled(context.getApplicationContext()))
        //    applicationTheme = "dark";
        //else
        //    applicationTheme = "white";
//        int nightModeFlags =
//                context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
//        switch (nightModeFlags) {
//            case Configuration.UI_MODE_NIGHT_YES:
//                applicationTheme = "dark";
//                break;
//            case Configuration.UI_MODE_NIGHT_NO:
//            case Configuration.UI_MODE_NIGHT_UNDEFINED:
//                applicationTheme = "white";
//                break;
//        }

        final String COLOR1 = "#6E6E6E";
        final String COLOR2 = "#AEAEAE";

        GradientDrawable coloredCircle = new GradientDrawable();
        coloredCircle.setColor(color);
        coloredCircle.setShape(GradientDrawable.OVAL);
        if (applicationTheme.equals(ApplicationPreferences.PREF_APPLICATION_THEME_VALUE_WHITE)) {
            //if (position == 2) // dark gray color
            //    coloredCircle.setStroke(2, Color.parseColor("#6E6E6E"));
            //else
            coloredCircle.setStroke(1, Color.parseColor(COLOR1));
        }
        else {
            //if (position == 0) // white color
            //    coloredCircle.setStroke(2, Color.parseColor("#AEAEAE"));
            //else
                coloredCircle.setStroke(1, Color.parseColor(COLOR1));
        }

        GradientDrawable darkerCircle = new GradientDrawable();
        darkerCircle.setColor(shiftColor(color));
        darkerCircle.setShape(GradientDrawable.OVAL);
        if (applicationTheme.equals(ApplicationPreferences.PREF_APPLICATION_THEME_VALUE_WHITE)) {
            //if (position == 2) // dark gray color
            //    coloredCircle.setStroke(2, Color.parseColor("#6E6E6E"));
            //else
                coloredCircle.setStroke(2, Color.parseColor(COLOR1));
        }
        else {
            //if (position == 0) // white color
            //    darkerCircle.setStroke(2, Color.parseColor("#AEAEAE"));
            //else
                darkerCircle.setStroke(2, Color.parseColor(COLOR2));
        }

        StateListDrawable stateListDrawable = new StateListDrawable();
        stateListDrawable.addState(new int[]{-android.R.attr.state_pressed}, coloredCircle);
        stateListDrawable.addState(new int[]{android.R.attr.state_pressed}, darkerCircle);
        return stateListDrawable;
    }


    @Override
    protected Parcelable onSaveInstanceState()
    {
        final Parcelable superState = super.onSaveInstanceState();
        /*if (isPersistent()) {
            return superState;
        }*/

        final RestartEventsIconColorChooserPreference.SavedState myState = new RestartEventsIconColorChooserPreference.SavedState(superState);
        myState.value = value;
        return myState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state)
    {
        if ((state == null) || (!state.getClass().equals(RestartEventsIconColorChooserPreference.SavedState.class))) {
            // Didn't save state for us in onSaveInstanceState
            super.onRestoreInstanceState(state);
            return;
        }

        // restore instance state
        RestartEventsIconColorChooserPreference.SavedState myState = (RestartEventsIconColorChooserPreference.SavedState)state;
        super.onRestoreInstanceState(myState.getSuperState());
        value = myState.value;
        //setSummaryCCHP(value);
    }

    // SavedState class
    private static class SavedState extends BaseSavedState
    {
        String value;

        SavedState(Parcel source)
        {
            super(source);

            // restore profileId
            value = source.readString();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags)
        {
            super.writeToParcel(dest, flags);

            // save profileId
            dest.writeString(value);
        }

        SavedState(Parcelable superState)
        {
            super(superState);
        }

        public static final Creator<RestartEventsIconColorChooserPreference.SavedState> CREATOR =
                new Creator<RestartEventsIconColorChooserPreference.SavedState>() {
                    public RestartEventsIconColorChooserPreference.SavedState createFromParcel(Parcel in)
                    {
                        return new RestartEventsIconColorChooserPreference.SavedState(in);
                    }
                    public RestartEventsIconColorChooserPreference.SavedState[] newArray(int size)
                    {
                        return new RestartEventsIconColorChooserPreference.SavedState[size];
                    }

                };

    }

}