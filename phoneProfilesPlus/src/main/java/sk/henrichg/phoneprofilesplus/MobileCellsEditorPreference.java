package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.preference.DialogPreference;

import java.util.ArrayList;
import java.util.List;

public class MobileCellsEditorPreference extends DialogPreference {

    MobileCellsEditorPreferenceFragment fragment;

    String value;
    //private String defaultValue;
    String cellFilter;
    int sortCellsBy = 0;
    //private boolean savedInstanceState;

    String cellNameFromMobileCellNamesDialog;

    //List<MobileCellsData> cellsList;
    List<MobileCellsData> filteredCellsList;

    MobileCellsData registeredCellDataSIM1;
    boolean registeredCellInTableSIM1;
    //boolean registeredCellInValueSIM1;
    MobileCellsData registeredCellDataSIM2;
    boolean registeredCellInTableSIM2;
    //boolean registeredCellInValueSIM2;
    MobileCellsData registeredCellDataDefault;
    boolean registeredCellInTableDefault;
    //boolean registeredCellInValueDefault;

    //private final Context prefContext;

    //private PersistValueAsyncTask persistValueAsyncTask = null;

    static final String ACTION_MOBILE_CELLS_EDITOR_REFRESH_LISTVIEW_BROADCAST_RECEIVER = PPApplication.PACKAGE_NAME + ".MobileCellsEditorPreference_refreshListView";

    public MobileCellsEditorPreference(Context prefContext, AttributeSet attrs) {
        super(prefContext, attrs);
        
        //this.prefContext = prefContext;
        
        //cellsList = new ArrayList<>();
        filteredCellsList = new ArrayList<>();

        setNegativeButtonText(null);
    }

    @Override
    protected Object onGetDefaultValue(@NonNull TypedArray ta, int index)
    {
        super.onGetDefaultValue(ta, index);
        return ta.getString(index);
    }

    @Override
    protected void onSetInitialValue(Object defaultValue) {
        //value = getPersistedString((String)defaultValue);
        //this.defaultValue = (String)defaultValue;

        value = "";
        //setSummary();
    }

    /*
    private void setSummary() {
        if (value.isEmpty())
            setSummary(R.string.applications_multiselect_summary_text_not_selected);
        else {
            String[] splits = value.split(StringConstants.STR_SPLIT_REGEX);
            String selectedCells = prefContext.getString(R.string.applications_multiselect_summary_text_selected);
            selectedCells = selectedCells + " " + splits.length;
            setSummary(selectedCells);
        }
    }
    */

    void addCellId(int cellId, long cellIdLong) {
        String[] splits = value.split(StringConstants.STR_SPLIT_REGEX);
        String sCellId = Integer.toString(cellId);
        String sCellIdLong = Long.toString(cellIdLong);
        boolean found = false;
        for (String cell : splits) {
            if (!cell.isEmpty()) {
                if (cellId != Integer.MAX_VALUE) {
                    if (cell.equals(sCellId)) {
                        found = true;
                        break;
                    }
                } else if (cellIdLong != Long.MAX_VALUE) {
                    if (cell.equals(sCellIdLong)) {
                        found = true;
                        break;
                    }
                }
            }
        }
        if (!found) {
            if (!value.isEmpty())
                value = value + "|";
            if (cellId != Integer.MAX_VALUE)
                value = value + sCellId;
            else if (cellIdLong != Long.MAX_VALUE)
                value = value + sCellIdLong;
        }
    }

    void removeCellId(int cellId, long cellIdLong) {
        String[] splits = value.split(StringConstants.STR_SPLIT_REGEX);
        String sCellId = Integer.toString(cellId);
        String sCellIdLOng = Long.toString(cellIdLong);
        value = "";
        StringBuilder _value = new StringBuilder();
        for (String cell : splits) {
            if (!cell.isEmpty()) {
                if (cellId != Integer.MAX_VALUE) {
                    if (!cell.equals(sCellId)) {
                        //if (!value.isEmpty())
                        //    value = value + "|";
                        //value = value + cell;
                        if (_value.length() > 0)
                            _value.append("|");
                        _value.append(cell);
                    }
                } else if (cellIdLong != Long.MAX_VALUE) {
                    if (!cell.equals(sCellIdLOng)) {
                        //if (!value.isEmpty())
                        //    value = value + "|";
                        //value = value + cell;
                        if (_value.length() > 0)
                            _value.append("|");
                        _value.append(cell);
                    }
                }
            }
        }
        value = _value.toString();
    }

    boolean isCellSelected(int cellId, long cellIdLong) {
        String[] splits = value.split(StringConstants.STR_SPLIT_REGEX);
        String sCellId = Integer.toString(cellId);
        String sCellIdLong = Long.toString(cellIdLong);
        for (String cell : splits) {
            if (cellId != Integer.MAX_VALUE) {
                if (cell.equals(sCellId))
                    return true;
            } else if (cellIdLong != Long.MAX_VALUE) {
                if (cell.equals(sCellIdLong))
                    return true;
            }
        }
        return false;
    }

    void setLocationEnableStatus() {
        if (fragment != null)
            fragment.setLocationEnableStatus();
    }

    void refreshListView(final boolean forRescan, final boolean showProgress/*, final int renameCellId*/)
    {
        if (fragment != null)
            fragment.refreshListView(forRescan, showProgress/*, renameCellId*/);
    }

    void showEditMenu(View view)
    {
        if (fragment != null)
            fragment.showEditMenu(view);
    }

    /*
    void persistValue() {
        if (shouldPersist()) {
            if (callChangeListener(value)) {
                persistValueAsyncTask = new PersistValueAsyncTask(this, prefContext);
                persistValueAsyncTask.execute();
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
    */

    void setCellNameText(String text) {
        //if (fragment != null)
        //    fragment.setCellNameText(text);
        cellNameFromMobileCellNamesDialog = text;
    }

    String getCellNameText() {
        //if (fragment != null) {
        //    return fragment.getCellNameText();
        //}
        //else {
        //    return null;
        //}
        return cellNameFromMobileCellNamesDialog;
    }

    void setCellFilterText(String text) {
        cellFilter = text;
        if (fragment != null)
            fragment.setCellFilterText(text);
    }


    @Override
    protected Parcelable onSaveInstanceState()
    {
        //savedInstanceState = true;

        final Parcelable superState = super.onSaveInstanceState();
        /*if (isPersistent()) {
            return superState;
        }*/

        final MobileCellsEditorPreference.SavedState myState = new MobileCellsEditorPreference.SavedState(superState);
        myState.value = value;
        //myState.defaultValue = defaultValue;
        myState.cellFilter = cellFilter;
        myState.sortCellsBy = sortCellsBy;

        return myState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state)
    {
        //if (dataWrapper == null)
        //    dataWrapper = new DataWrapper(prefContext, false, 0, false);

        if ((state == null) || (!state.getClass().equals(MobileCellsEditorPreference.SavedState.class))) {
            // Didn't save state for us in onSaveInstanceState
            super.onRestoreInstanceState(state);
            return;
        }

        // restore instance state
        MobileCellsEditorPreference.SavedState myState = (MobileCellsEditorPreference.SavedState)state;
        super.onRestoreInstanceState(myState.getSuperState());
        value = myState.value;
        //defaultValue = myState.defaultValue;
        cellFilter = myState.cellFilter;
        sortCellsBy = myState.sortCellsBy;

        //setSummary();

        //notifyChanged();
    }

    // SavedState class
    private static class SavedState extends BaseSavedState
    {
        String value;
        //String defaultValue;
        String cellFilter;
        int sortCellsBy;

        SavedState(Parcel source)
        {
            super(source);

            value = source.readString();
            //defaultValue = source.readString();
            cellFilter = source.readString();
            sortCellsBy = source.readInt();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags)
        {
            super.writeToParcel(dest, flags);

            dest.writeString(value);
            //dest.writeString(defaultValue);
            dest.writeString(cellFilter);
            dest.writeInt(sortCellsBy);
        }

        SavedState(Parcelable superState)
        {
            super(superState);
        }

        public static final Creator<MobileCellsEditorPreference.SavedState> CREATOR =
                new Creator<>() {
                    public MobileCellsEditorPreference.SavedState createFromParcel(Parcel in)
                    {
                        return new MobileCellsEditorPreference.SavedState(in);
                    }
                    public MobileCellsEditorPreference.SavedState[] newArray(int size)
                    {
                        return new MobileCellsEditorPreference.SavedState[size];
                    }

                };

    }

/*
    private static class PersistValueAsyncTask extends AsyncTask<Void, Integer, Void> {

        private final WeakReference<MobileCellsEditorPreference> preferenceWeakRef;
        private final WeakReference<Context> prefContextWeakRef;
        private List<MobileCellsData> _cellsList;

        public PersistValueAsyncTask(MobileCellsEditorPreference preference,
                                     Context prefContext) {
            this.preferenceWeakRef = new WeakReference<>(preference);
            this.prefContextWeakRef = new WeakReference<>(prefContext);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            MobileCellsEditorPreference preference = preferenceWeakRef.get();
            if (preference != null) {
                _cellsList = new ArrayList<>();
                _cellsList.addAll(preference.cellsList);
            }
        }

        @Override
        protected Void doInBackground(Void... voids) {
            MobileCellsEditorPreference preference = preferenceWeakRef.get();
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
            MobileCellsEditorPreference preference = preferenceWeakRef.get();
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