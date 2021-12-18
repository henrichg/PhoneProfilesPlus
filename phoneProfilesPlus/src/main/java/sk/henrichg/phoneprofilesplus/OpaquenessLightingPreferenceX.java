package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.widget.ImageView;

import androidx.preference.DialogPreference;
import androidx.preference.PreferenceViewHolder;

public class OpaquenessLightingPreferenceX extends DialogPreference {

    OpaquenessLightingPreferenceFragmentX fragment;

    String value;
    final boolean showLighting;

    private final Context prefContext;

    final int[] opaquenessValues = { 0, 12, 25, 37, 50, 62, 75, 87, 100 };
    final int[] opaquenessNames = {
            R.string.array_pref_0_percent,
            R.string.array_pref_12_percent,
            R.string.array_pref_25_percent,
            R.string.array_pref_37_percent,
            R.string.array_pref_50_percent,
            R.string.array_pref_62_percent,
            R.string.array_pref_75_percent,
            R.string.array_pref_87_percent,
            R.string.array_pref_100_percent
    };
    final int[] opaquenessIconResIds = {
            R.drawable.ic_opaqueness_0,
            R.drawable.ic_opaqueness_13,
            R.drawable.ic_opaqueness_25,
            R.drawable.ic_opaqueness_38,
            R.drawable.ic_opaqueness_50,
            R.drawable.ic_opaqueness_63,
            R.drawable.ic_opaqueness_75,
            R.drawable.ic_opaqueness_88,
            R.drawable.ic_opaqueness_100
    };

    final int[] lightingValues = { 0, 12, 25, 37, 50, 62, 75, 87, 100 };
    final int[] lightingNames = {
            R.string.array_pref_0_percent,
            R.string.array_pref_12_percent,
            R.string.array_pref_25_percent,
            R.string.array_pref_37_percent,
            R.string.array_pref_50_percent,
            R.string.array_pref_62_percent,
            R.string.array_pref_75_percent,
            R.string.array_pref_87_percent,
            R.string.array_pref_100_percent
    };
    final int[] lightingIconResIds = {
            R.drawable.ic_lighting_0,
            R.drawable.ic_lighting_13,
            R.drawable.ic_lighting_25,
            R.drawable.ic_lighting_38,
            R.drawable.ic_lighting_50,
            R.drawable.ic_lighting_63,
            R.drawable.ic_lighting_75,
            R.drawable.ic_lighting_88,
            R.drawable.ic_lighting_100
    };

    public OpaquenessLightingPreferenceX(Context context, AttributeSet attrs)
    {
        super(context, attrs);

        //PPApplication.logE("ProfilePreferenceX.ProfilePreferenceX", "xxx");

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.OpaquenessLightingPference);

        showLighting = typedArray.getBoolean(R.styleable.OpaquenessLightingPference_showLighting, false);

        value = "0";
        prefContext = context;
        //preferenceTitle = getTitle();

        setWidgetLayoutResource(R.layout.preference_widget_opaqueness_lighting_preference); // resource na layout custom preference - TextView-ImageView

        typedArray.recycle();

        setPositiveButtonText(null);

    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder)
    {
        super.onBindViewHolder(holder);

        //preferenceTitleView = view.findViewById(R.id.applications_pref_label);  // resource na title
        //preferenceTitleView.setText(preferenceTitle);

        ImageView opaquenessLightingIcon = (ImageView) holder.findViewById(R.id.opaqueness_lighting_icon);

        if (opaquenessLightingIcon != null)
        {
            int position = getPosition(value);
            int iconResId = R.drawable.ic_empty;
            if (position != -1) {
                try {
                    if (showLighting)
                        iconResId = lightingIconResIds[position];
                    else
                        iconResId = opaquenessIconResIds[position];
                } catch (Exception ignored) {
                }
            }
            opaquenessLightingIcon.setImageResource(iconResId); // icon resource

            Handler handler = new Handler(prefContext.getMainLooper());
            handler.postDelayed(() -> {
//                    PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", "START run - from=ProfilePreferenceX.onBindViewHolder");
                int _position = getPosition(value);
                if (_position != -1) {
                    String summary;
                    if (showLighting)
                        summary = prefContext.getString(lightingNames[_position]);
                    else
                        summary = prefContext.getString(opaquenessNames[_position]);
                    setSummary(summary);
                }
            }, 200);

        }
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index)
    {
        super.onGetDefaultValue(a, index);
        return a.getString(index);
    }

    @Override
    protected void onSetInitialValue(Object defaultValue)
    {
        String value;
        try {
            value = getPersistedString((String) defaultValue);
        } catch  (Exception e) {
            value = (String) defaultValue;
        }
        this.value = value;
    }

    /*
    @Override
    protected void onPrepareForRemoval()
    {
        super.onPrepareForRemoval();
        //dataWrapper.invalidateDataWrapper();
        //dataWrapper = null;
    }
    */

    /*
    public String getProfileId()
    {
        return profileId;
    }
    */

    void setValue(String newValue)
    {
        if (!callChangeListener(newValue)) {
            // no save new value
            return;
        }

        value = newValue;

        // set summary
        int position = getPosition(value);
        if (position != -1) {
            String summary;
            if (showLighting)
                summary = prefContext.getString(lightingNames[position]);
            else
                summary = prefContext.getString(opaquenessNames[position]);
            setSummary(summary);
        }

        // save to preferences
        persistString(newValue);

        // and notify
        notifyChanged();

    }

    int getPosition(String value) {
        int position = -1;
        if (showLighting) {
            for (int _value : lightingValues) {
                String sValue = String.valueOf(_value);
                ++position;
                if (sValue.equals(value))
                    break;
            }
        }
        else {
            for (int _value : opaquenessValues) {
                String sValue = String.valueOf(_value);
                ++position;
                if (sValue.equals(value))
                    break;
            }
        }
        return position;
    }

    @Override
    protected Parcelable onSaveInstanceState()
    {
        final Parcelable superState = super.onSaveInstanceState();
        /*if (isPersistent()) {
            return superState;
        }*/

        final SavedState myState = new SavedState(superState);
        myState.value = value;
        return myState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state)
    {
        //if (dataWrapper == null)
        //    dataWrapper = new DataWrapper(prefContext, false, 0, false);

        if (!state.getClass().equals(SavedState.class)) {
            // Didn't save state for us in onSaveInstanceState
            super.onRestoreInstanceState(state);
            int position = getPosition(value);
            if (position != -1) {
                String summary;
                if (showLighting)
                    summary = prefContext.getString(lightingNames[position]);
                else
                    summary = prefContext.getString(opaquenessNames[position]);
                setSummary(summary);
            }
            return;
        }

        // restore instance state
        SavedState myState = (SavedState)state;
        super.onRestoreInstanceState(myState.getSuperState());
        value = myState.value;
        /*addNoActivateItem = myState.addNoActivateItem;
        noActivateAsDoNotApply = myState.noActivateAsDoNotApply;
        showDuration = myState.showDuration;*/

        int position = getPosition(value);
        if (position != -1) {
            String summary;
            if (showLighting)
                summary = prefContext.getString(lightingNames[position]);
            else
                summary = prefContext.getString(opaquenessNames[position]);
            setSummary(summary);
        }
        //notifyChanged();
    }

    // SavedState class
    private static class SavedState extends BaseSavedState
    {
        String value;

        SavedState(Parcel source)
        {
            super(source);

            // restore value
            value = source.readString();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags)
        {
            super.writeToParcel(dest, flags);

            // save value
            dest.writeString(value);
        }

        SavedState(Parcelable superState)
        {
            super(superState);
        }

        public static final Creator<SavedState> CREATOR =
                new Creator<OpaquenessLightingPreferenceX.SavedState>() {
            public OpaquenessLightingPreferenceX.SavedState createFromParcel(Parcel in)
            {
                return new OpaquenessLightingPreferenceX.SavedState(in);
            }
            public OpaquenessLightingPreferenceX.SavedState[] newArray(int size)
            {
                return new OpaquenessLightingPreferenceX.SavedState[size];
            }

        };

    }

}
