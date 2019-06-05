package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import androidx.preference.DialogPreference;

public class BluetoothNamePreferenceX extends DialogPreference {

    BluetoothNamePreferenceFragmentX fragment;

    private final Context context;

    String value;
    private String defaultValue;
    private boolean savedInstanceState;

    List<BluetoothDeviceData> bluetoothList;
    final List<BluetoothDeviceData> customBluetoothList;

    static boolean forceRegister = false;

    public BluetoothNamePreferenceX(Context context, AttributeSet attrs) {
        super(context, attrs);

        this.context = context;

        bluetoothList = new ArrayList<>();
        customBluetoothList = new ArrayList<>();
    }

    @Override
    protected Object onGetDefaultValue(TypedArray ta, int index)
    {
        super.onGetDefaultValue(ta, index);
        return ta.getString(index);
    }

    @Override
    protected void onSetInitialValue(Object defaultValue) {
        value = getPersistedString((String)defaultValue);
        this.defaultValue = (String)defaultValue;
        setSummary();
    }

    void addBluetoothName(String bluetoothName) {
        String[] splits = value.split("\\|");
        boolean found = false;
        for (String _bluetoothName : splits) {
            if (_bluetoothName.equals(bluetoothName))
                found = true;
        }
        if (!found) {
            if (!value.isEmpty())
                value = value + "|";
            value = value + bluetoothName;
        }
    }

    @SuppressWarnings("StringConcatenationInLoop")
    void removeBluetoothName(String bluetoothName) {
        String[] splits = value.split("\\|");
        value = "";
        for (String _bluetoothName : splits) {
            if (!_bluetoothName.isEmpty()) {
                if (!_bluetoothName.equals(bluetoothName)) {
                    if (!value.isEmpty())
                        value = value + "|";
                    value = value + _bluetoothName;
                }
            }
        }
    }

    boolean isBluetoothNameSelected(String bluetoothName) {
        String[] splits = value.split("\\|");
        for (String _bluetoothName : splits) {
            if (_bluetoothName.equals(bluetoothName))
                return true;
        }
        return false;
    }

    void setLocationEnableStatus() {
        if (fragment != null)
            fragment.setLocationEnableStatus();
    }

    @SuppressWarnings("SameParameterValue")
    void refreshListView(boolean forRescan, final String scrollToBTName)
    {
        if (fragment != null)
            fragment.refreshListView(forRescan, scrollToBTName);
    }

    void showEditMenu(View view)
    {
        if (fragment != null)
            fragment.showEditMenu(view);
    }

    private void setSummary() {
        String[] splits = value.split("\\|");
        for (String _bluetoothName : splits) {
            if (_bluetoothName.isEmpty()) {
                setSummary(R.string.applications_multiselect_summary_text_not_selected);
            } else if (splits.length == 1) {
                switch (value) {
                    case EventPreferencesBluetooth.ALL_BLUETOOTH_NAMES_VALUE:
                        setSummary(R.string.bluetooth_name_pref_dlg_all_bt_names_chb);
                        break;
                    case EventPreferencesBluetooth.CONFIGURED_BLUETOOTH_NAMES_VALUE:
                        setSummary(R.string.bluetooth_name_pref_dlg_configured_bt_names_chb);
                        break;
                    default:
                        setSummary(_bluetoothName);
                        break;
                }
            } else {
                String selectedBluetoothNames = context.getString(R.string.applications_multiselect_summary_text_selected);
                selectedBluetoothNames = selectedBluetoothNames + " " + splits.length;
                setSummary(selectedBluetoothNames);
                break;
            }
        }
    }

    void persistValue() {
        if (shouldPersist()) {
            if (callChangeListener(value))
            {
                setSummary();
                persistString(value);
            }
        }
    }

    void resetSummary() {
        if (!savedInstanceState) {
            value = getPersistedString(defaultValue);
            setSummary();
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

        final BluetoothNamePreferenceX.SavedState myState = new BluetoothNamePreferenceX.SavedState(superState);
        myState.value = value;
        myState.defaultValue = defaultValue;

        return myState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state)
    {
        //if (dataWrapper == null)
        //    dataWrapper = new DataWrapper(prefContext, false, 0, false);

        if (!state.getClass().equals(BluetoothNamePreferenceX.SavedState.class)) {
            // Didn't save state for us in onSaveInstanceState
            super.onRestoreInstanceState(state);
            return;
        }

        // restore instance state
        BluetoothNamePreferenceX.SavedState myState = (BluetoothNamePreferenceX.SavedState)state;
        super.onRestoreInstanceState(myState.getSuperState());
        value = myState.value;
        defaultValue = myState.defaultValue;

        setSummary();
        //notifyChanged();
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

        @SuppressWarnings("unused")
        public static final Creator<BluetoothNamePreferenceX.SavedState> CREATOR =
                new Creator<BluetoothNamePreferenceX.SavedState>() {
                    public BluetoothNamePreferenceX.SavedState createFromParcel(Parcel in)
                    {
                        return new BluetoothNamePreferenceX.SavedState(in);
                    }
                    public BluetoothNamePreferenceX.SavedState[] newArray(int size)
                    {
                        return new BluetoothNamePreferenceX.SavedState[size];
                    }

                };

    }

}