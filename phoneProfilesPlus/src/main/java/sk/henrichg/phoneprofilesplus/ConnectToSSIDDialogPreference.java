package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.preference.DialogPreference;

import java.util.ArrayList;
import java.util.List;

public class ConnectToSSIDDialogPreference extends DialogPreference {

    ConnectToSSIDDialogPreferenceFragment fragment;

    private final Context context;

    String value = "";
    private String defaultValue;
    private boolean savedInstanceState;

    //final int disableSharedProfile;

    List<WifiSSIDData> ssidList;

    public ConnectToSSIDDialogPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        /*TypedArray typedArray = context.obtainStyledAttributes(attrs,
                R.styleable.ConnectToSSIDDialogPreference);

        disableSharedProfile = typedArray.getInteger(
                R.styleable.ConnectToSSIDDialogPreference_cDisableSharedProfile, 0);*/

        this.context = context;

        //setWidgetLayoutResource(R.layout.widget_applications_preference); // resource na layout custom preference - TextView-ImageView

        //typedArray.recycle();

        ssidList = new ArrayList<>();
    }

    @Override
    protected Object onGetDefaultValue(@NonNull TypedArray ta, int index)
    {
        super.onGetDefaultValue(ta, index);
        return ta.getString(index);
    }

    @Override
    protected void onSetInitialValue(Object defaultValue)
    {
        value = getPersistedString((String) defaultValue);
        this.defaultValue = (String)defaultValue;

        setSummaryCTSDP();
    }

    private void setSummaryCTSDP()
    {
        String prefSummary = "[" + StringConstants.CHAR_HARD_SPACE + context.getString(R.string.connect_to_ssid_pref_dlg_summary_text_just_any) + StringConstants.CHAR_HARD_SPACE + "]";
        //if (!value.isEmpty() && value.equals(Profile.CONNECTTOSSID_SHAREDPROFILE))
        //    prefSummary = context.getString(R.string.array_pref_default_profile);
        //else
        if (!value.isEmpty() && !value.equals(StringConstants.CONNECTTOSSID_JUSTANY)) {
            prefSummary = value.replace("\"", "");
        }
        setSummary(prefSummary);
    }

    void persistValue() {
        persistString(value);
        setSummaryCTSDP();
    }

    void resetSummary() {
        if (!savedInstanceState) {
            value = getPersistedString(defaultValue);
            setSummaryCTSDP();
        }
        savedInstanceState = false;
    }

    @Override
    protected Parcelable onSaveInstanceState()
    {
        savedInstanceState = true;

        final Parcelable superState = super.onSaveInstanceState();
        /*if (isPersistent()) {
            return superState;
        }*/

        final ConnectToSSIDDialogPreference.SavedState myState = new ConnectToSSIDDialogPreference.SavedState(superState);
        myState.value = value;
        myState.defaultValue = defaultValue;
        return myState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state)
    {
        if ((state == null) || (!state.getClass().equals(ConnectToSSIDDialogPreference.SavedState.class))) {
            // Didn't save state for us in onSaveInstanceState
            super.onRestoreInstanceState(state);
            setSummaryCTSDP();
            return;
        }

        // restore instance state
        ConnectToSSIDDialogPreference.SavedState myState = (ConnectToSSIDDialogPreference.SavedState)state;
        super.onRestoreInstanceState(myState.getSuperState());
        value = myState.value;
        defaultValue = myState.defaultValue;

        setSummaryCTSDP();
    }

    void refreshListView() {
        if (fragment != null)
            fragment.refreshListView();
    }

    // SavedState class
    private static class SavedState extends BaseSavedState
    {
        String value;
        String defaultValue;

        SavedState(Parcel source)
        {
            super(source);

            value = source.readString();
            defaultValue = source.readString();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags)
        {
            super.writeToParcel(dest, flags);

            dest.writeString(value);
            dest.writeString(defaultValue);
        }

        SavedState(Parcelable superState)
        {
            super(superState);
        }

        public static final Creator<ConnectToSSIDDialogPreference.SavedState> CREATOR =
                new Creator<>() {
                    public ConnectToSSIDDialogPreference.SavedState createFromParcel(Parcel in)
                    {
                        return new ConnectToSSIDDialogPreference.SavedState(in);
                    }
                    public ConnectToSSIDDialogPreference.SavedState[] newArray(int size)
                    {
                        return new ConnectToSSIDDialogPreference.SavedState[size];
                    }

                };

    }

}
