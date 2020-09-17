package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.AsyncTask;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.preference.DialogPreference;

import java.util.ArrayList;
import java.util.List;

public class MobileCellsPreferenceX extends DialogPreference {

    MobileCellsPreferenceFragmentX fragment;

    String value;
    private String defaultValue;
    String cellFilter;
    int sortCellsBy = 0;
    private boolean savedInstanceState;

    List<MobileCellsData> cellsList;
    List<MobileCellsData> filteredCellsList;

    MobileCellsData registeredCellData;
    boolean registeredCellInTable;
    boolean registeredCellInValue;

    private final Context context;

    static boolean forceStart;

    public MobileCellsPreferenceX(Context context, AttributeSet attrs) {
        super(context, attrs);
        
        this.context = context;
        
        cellsList = new ArrayList<>();
        filteredCellsList = new ArrayList<>();
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

    private void setSummary() {
        /*if (!ApplicationPreferences.applicationEventMobileCellEnableScannig(context.getApplicationContext())) {
            preference.setSummary(context.getResources().getString(R.string.profile_preferences_device_not_allowed)+
                    ": "+context.getResources().getString(R.string.preference_not_allowed_reason_not_enabled_scanning));
        }
        else {*/
        if (value.isEmpty())
            setSummary(R.string.applications_multiselect_summary_text_not_selected);
        else {
            String[] splits = value.split("\\|");
            String selectedCells = context.getString(R.string.applications_multiselect_summary_text_selected);
            selectedCells = selectedCells + " " + splits.length;
            setSummary(selectedCells);
        }
        //}
        //GlobalGUIRoutines.setPreferenceTitleStyle(preference, false, true, false, false);
    }

    void addCellId(int cellId) {
        String[] splits = value.split("\\|");
        String sCellId = Integer.toString(cellId);
        boolean found = false;
        for (String cell : splits) {
            if (!cell.isEmpty()) {
                if (cell.equals(sCellId)) {
                    found = true;
                    break;
                }
            }
        }
        if (!found) {
            if (!value.isEmpty())
                value = value + "|";
            value = value + sCellId;
        }
    }

    @SuppressWarnings("StringConcatenationInLoop")
    void removeCellId(int cellId) {
        String[] splits = value.split("\\|");
        String sCellId = Integer.toString(cellId);
        value = "";
        for (String cell : splits) {
            if (!cell.isEmpty()) {
                if (!cell.equals(sCellId)) {
                    if (!value.isEmpty())
                        value = value + "|";
                    value = value + cell;
                }
            }
        }
    }

    boolean isCellSelected(int cellId) {
        String[] splits = value.split("\\|");
        String sCellId = Integer.toString(cellId);
        for (String cell : splits) {
            if (cell.equals(sCellId))
                return true;
        }
        return false;
    }

    void setLocationEnableStatus() {
        if (fragment != null)
            fragment.setLocationEnableStatus();
    }

    @SuppressWarnings("SameParameterValue")
    void refreshListView(final boolean forRescan, final int renameCellId)
    {
        if (fragment != null)
            fragment.refreshListView(forRescan, renameCellId);
    }

    void showEditMenu(View view)
    {
        if (fragment != null)
            fragment.showEditMenu(view);
    }

    void persistValue() {
        Log.e("MobileCellsPreference.onPositive", "xxx");
        if (shouldPersist()) {
            Log.e("MobileCellsPreference.onPositive", "shouldPersist");
            if (callChangeListener(value)) {
                Log.e("MobileCellsPreference.onPositive", "callChangeListener=true");
                new AsyncTask<Void, Integer, Void>() {
                    List<MobileCellsData> _cellsList;

                    @Override
                    protected void onPreExecute() {
                        super.onPreExecute();
                        Log.e("MobileCellsPreference.onPositive", "onPreExecute cellList.size()="+cellsList.size());
                        _cellsList = new ArrayList<>();
                        _cellsList.addAll(cellsList);
                    }

                    @Override
                    protected Void doInBackground(Void... voids) {
                        Log.e("MobileCellsPreference.onPositive", "doInBackground");
                        DatabaseHandler db = DatabaseHandler.getInstance(context);
                        db.saveMobileCellsList(_cellsList, false, false);
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void result) {
                        super.onPostExecute(result);
                        Log.e("MobileCellsPreference.onPositive", "onPostExecute value="+value);
                        persistString(value);
                        setSummary();
                    }
                }.execute();
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

    void setCellFilterText(String text) {
        cellFilter = text;
        if (fragment != null)
            fragment.setCellFilterText(text);
    }


    @Override
    protected Parcelable onSaveInstanceState()
    {
        savedInstanceState = true;

        final Parcelable superState = super.onSaveInstanceState();
        /*if (isPersistent()) {
            return superState;
        }*/

        final MobileCellsPreferenceX.SavedState myState = new MobileCellsPreferenceX.SavedState(superState);
        myState.value = value;
        myState.defaultValue = defaultValue;
        myState.cellFilter = cellFilter;
        myState.sortCellsBy = sortCellsBy;

        return myState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state)
    {
        //if (dataWrapper == null)
        //    dataWrapper = new DataWrapper(prefContext, false, 0, false);

        if (!state.getClass().equals(MobileCellsPreferenceX.SavedState.class)) {
            // Didn't save state for us in onSaveInstanceState
            super.onRestoreInstanceState(state);
            return;
        }

        // restore instance state
        MobileCellsPreferenceX.SavedState myState = (MobileCellsPreferenceX.SavedState)state;
        super.onRestoreInstanceState(myState.getSuperState());
        value = myState.value;
        defaultValue = myState.defaultValue;
        cellFilter = myState.cellFilter;
        sortCellsBy = myState.sortCellsBy;

        setSummary();

        //notifyChanged();
    }

    // SavedState class
    private static class SavedState extends BaseSavedState
    {
        String value;
        String defaultValue;
        String cellFilter;
        int sortCellsBy;

        SavedState(Parcel source)
        {
            super(source);

            value = source.readString();
            defaultValue = source.readString();
            cellFilter = source.readString();
            sortCellsBy = source.readInt();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags)
        {
            super.writeToParcel(dest, flags);

            dest.writeString(value);
            dest.writeString(defaultValue);
            dest.writeString(cellFilter);
            dest.writeInt(sortCellsBy);
        }

        SavedState(Parcelable superState)
        {
            super(superState);
        }

        public static final Creator<MobileCellsPreferenceX.SavedState> CREATOR =
                new Creator<MobileCellsPreferenceX.SavedState>() {
                    public MobileCellsPreferenceX.SavedState createFromParcel(Parcel in)
                    {
                        return new MobileCellsPreferenceX.SavedState(in);
                    }
                    public MobileCellsPreferenceX.SavedState[] newArray(int size)
                    {
                        return new MobileCellsPreferenceX.SavedState[size];
                    }

                };

    }

}