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

public class MobileCellsPreferenceX extends DialogPreference {

    MobileCellsPreferenceFragmentX fragment;

    String value;
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
        value = getPersistedString(value);
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
        if (shouldPersist()) {
            //Log.d("MobileCellsPreference.onPositive", "1");
            if (callChangeListener(value)) {
                //Log.d("MobileCellsPreference.onPositive", "2");
                DatabaseHandler db = DatabaseHandler.getInstance(context);
                db.saveMobileCellsList(cellsList, false, false);
                persistString(value);
            }
        }
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
        if (fragment != null)
            fragment.setCellFilterText(text);
    }



    @Override
    protected Parcelable onSaveInstanceState()
    {
        final Parcelable superState = super.onSaveInstanceState();
        /*if (isPersistent()) {
            return superState;
        }*/

        final MobileCellsPreferenceX.SavedState myState = new MobileCellsPreferenceX.SavedState(superState);
        myState.value = value;

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

        //notifyChanged();
    }

    // SavedState class
    private static class SavedState extends BaseSavedState
    {
        String value;

        SavedState(Parcel source)
        {
            super(source);

            value = source.readString();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags)
        {
            super.writeToParcel(dest, flags);

            dest.writeString(value);
        }

        SavedState(Parcelable superState)
        {
            super(superState);
        }

        @SuppressWarnings("unused")
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