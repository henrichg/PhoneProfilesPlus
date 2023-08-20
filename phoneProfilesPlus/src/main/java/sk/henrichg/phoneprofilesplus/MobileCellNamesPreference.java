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

public class MobileCellNamesPreference extends DialogPreference {

    MobileCellNamesPreferenceFragment fragment;

    String value;
    private String defaultValue;
    private boolean savedInstanceState;

    private final Context prefContext;

    List<String> cellNamesList;

    //private PersistValueAsyncTask persistValueAsyncTask = null;

    //static final String ACTION_MOBILE_CELLS_PREF_REFRESH_LISTVIEW_BROADCAST_RECEIVER = PPApplication.PACKAGE_NAME + ".MobileCellsPreference_refreshListView";

    public MobileCellNamesPreference(Context prefContext, AttributeSet attrs) {
        super(prefContext, attrs);
        
        this.prefContext = prefContext;

        cellNamesList = new ArrayList<>();
    }

    @Override
    protected Object onGetDefaultValue(@NonNull TypedArray ta, int index)
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

    void setSummary() {
        if (value.isEmpty())
            setSummary(R.string.applications_multiselect_summary_text_not_selected);
        else {
            String[] splits = value.split(StringConstants.STR_SPLIT_REGEX);
            String selectedCells = prefContext.getString(R.string.applications_multiselect_summary_text_selected);
            selectedCells = selectedCells + " " + splits.length;
            setSummary(selectedCells);
        }
    }

    void addCellName(String cellName) {
        String[] splits = value.split(StringConstants.STR_SPLIT_REGEX);
        boolean found = false;
        for (String cell : splits) {
            if (!cell.isEmpty()) {
                if (cell.equals(cellName)) {
                    found = true;
                    break;
                }
            }
        }
        if (!found) {
            if (!value.isEmpty())
                value = value + "|";
            value = value + cellName;
        }
    }

    void setValue(String _value) {
        value = _value;
    }

    void removeCellName(String cellName) {
        String[] splits = value.split(StringConstants.STR_SPLIT_REGEX);
        value = "";
        StringBuilder _value = new StringBuilder();
        for (String cell : splits) {
            if (!cell.isEmpty()) {
                if (!cell.equals(cellName)) {
                    //if (!value.isEmpty())
                    //    value = value + "|";
                    //value = value + cell;
                    if (_value.length() > 0)
                        _value.append("|");
                    _value.append(cell);
                }
            }
        }
        value = _value.toString();
    }

    boolean isCellSelected(String cellName) {
        String[] splits = value.split(StringConstants.STR_SPLIT_REGEX);
        for (String cell : splits) {
            if (cell.equals(cellName))
                return true;
        }
        return false;
    }

    void setLocationEnableStatus() {
        if (fragment != null)
            fragment.setLocationEnableStatus();
    }

    void refreshListView(/*final boolean forRescan*/)
    {
        if (fragment != null)
            fragment.refreshListView(/*forRescan*/);
    }

    void persistValue() {
        if (shouldPersist()) {
            if (callChangeListener(value)) {
                persistString(value);
                setSummary();
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

    /*
    void setCellNameText(String text) {
        if (fragment != null)
            fragment.setCellNameText(text);
    }

    String getCellNameText() {
        if (fragment != null)
            return  fragment.getCellNameText();
        else
            return null;
    }
    */

    @Override
    protected Parcelable onSaveInstanceState()
    {
        savedInstanceState = true;

        final Parcelable superState = super.onSaveInstanceState();
        /*if (isPersistent()) {
            return superState;
        }*/

        final MobileCellNamesPreference.SavedState myState = new MobileCellNamesPreference.SavedState(superState);
        myState.value = value;
        myState.defaultValue = defaultValue;

        return myState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state)
    {
        //if (dataWrapper == null)
        //    dataWrapper = new DataWrapper(prefContext, false, 0, false);

        if ((state == null) || (!state.getClass().equals(MobileCellNamesPreference.SavedState.class))) {
            // Didn't save state for us in onSaveInstanceState
            super.onRestoreInstanceState(state);
            return;
        }

        // restore instance state
        MobileCellNamesPreference.SavedState myState = (MobileCellNamesPreference.SavedState)state;
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

        public static final Creator<MobileCellNamesPreference.SavedState> CREATOR =
                new Creator<MobileCellNamesPreference.SavedState>() {
                    public MobileCellNamesPreference.SavedState createFromParcel(Parcel in)
                    {
                        return new MobileCellNamesPreference.SavedState(in);
                    }
                    public MobileCellNamesPreference.SavedState[] newArray(int size)
                    {
                        return new MobileCellNamesPreference.SavedState[size];
                    }

                };

    }

/*
    private static class PersistValueAsyncTask extends AsyncTask<Void, Integer, Void> {

        private final WeakReference<MobileCellsPreference> preferenceWeakRef;
        private final WeakReference<Context> prefContextWeakRef;
        private List<MobileCellsData> _cellsList;

        public PersistValueAsyncTask(MobileCellsPreference preference,
                                     Context prefContext) {
            this.preferenceWeakRef = new WeakReference<>(preference);
            this.prefContextWeakRef = new WeakReference<>(prefContext);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            MobileCellsPreference preference = preferenceWeakRef.get();
            if (preference != null) {
                _cellsList = new ArrayList<>();
                _cellsList.addAll(preference.cellsList);
            }
        }

        @Override
        protected Void doInBackground(Void... voids) {
            MobileCellsPreference preference = preferenceWeakRef.get();
            Context prefContext = prefContextWeakRef.get();
            if ((preference != null) && (prefContext != null)) {
                DatabaseHandler db = DatabaseHandler.getInstance(prefContext.getApplicationContext());
                db.saveMobileCellsList(_cellsList, false, false);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            MobileCellsPreference preference = preferenceWeakRef.get();
            if (preference != null) {
                preference.persistString(preference.value);
                preference.setSummary();

                //if ((preference.persistValueAsyncTask != null) &&
                //    getEventsAsyncTask.getStatus().equals(AsyncTask.Status.RUNNING)) {
                //    getEventsAsyncTask.cancel(true);
                //}
                preference.persistValueAsyncTask = null;
            }
        }

    }
*/

}