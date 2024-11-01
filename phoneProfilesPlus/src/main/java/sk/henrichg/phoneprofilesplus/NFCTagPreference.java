package sk.henrichg.phoneprofilesplus;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.nfc.NfcAdapter;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.preference.DialogPreference;

import java.util.ArrayList;
import java.util.List;

public class NFCTagPreference extends DialogPreference {

    NFCTagPreferenceFragment fragment;

    String value;
    private String defaultValue;
    private boolean savedInstanceState;

    List<NFCTag> nfcTagList;

    private final Context prefContext;

    //static final int RESULT_NFC_TAG_READ_EDITOR = 3500;
    static final int RESULT_NFC_TAG_WRITE = 3501;

    //private static final String PREF_SHOW_HELP = "nfc_tag_pref_show_help";

    public NFCTagPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        
        this.prefContext = context;

        nfcTagList = new ArrayList<>();
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

    private void setSummary() {
        String[] splits = value.split(StringConstants.STR_SPLIT_REGEX);
        for (String _tag : splits) {
            if (_tag.isEmpty()) {
                setSummary(R.string.applications_multiselect_summary_text_not_selected);
            }
            else
            if (splits.length == 1) {
                setSummary(_tag);
            }
            else {
                String selectedNfcTags = prefContext.getString(R.string.applications_multiselect_summary_text_selected);
                selectedNfcTags = selectedNfcTags + " " + splits.length;
                setSummary(selectedNfcTags);
                break;
            }
        }
        //GlobalGUIRoutines.setPreferenceTitleStyle(preference, false, true, false, false);
    }

    /*public String getNfcTags()
    {
        return value;
    }*/

    void addNfcTag(String tagName) {
        String[] splits = value.split(StringConstants.STR_SPLIT_REGEX);
        boolean found = false;
        for (String tag : splits) {
            if (tag.equals(tagName)) {
                found = true;
                break;
            }
        }
        if (!found) {
            if (!value.isEmpty())
                value = value + "|";
            value = value + tagName;
        }
    }

    void removeNfcTag(String tagName) {
        String[] splits = value.split(StringConstants.STR_SPLIT_REGEX);
        value = "";
        StringBuilder _value = new StringBuilder();
        for (String tag : splits) {
            if (!tag.isEmpty()) {
                if (!tag.equals(tagName)) {
                    //if (!value.isEmpty())
                    //    value = value + "|";
                    //value = value + tag;
                    if (_value.length() > 0)
                        _value.append("|");
                    _value.append(tag);
                }
            }
        }
        value = _value.toString();
    }

    boolean isNfcTagSelected(String tagName) {
        String[] splits = value.split(StringConstants.STR_SPLIT_REGEX);
        for (String tag : splits) {
            if (tag.equals(tagName))
                return true;
        }
        return false;
    }

    private void refreshListView(/*boolean forRescan, */final String scrollToTag)
    {
        if (fragment != null)
            fragment.refreshListView(scrollToTag);
    }

    void showEditMenu(View view)
    {
        if (fragment != null)
            fragment.showEditMenu(view);
    }

    void writeToNFCTag(long id, String tag) {

        NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(prefContext);
        if (!nfcAdapter.isEnabled()) {
            if (fragment != null) {
                PPAlertDialog dialog = new PPAlertDialog(
                        prefContext.getString(R.string.nfc_tag_pref_dlg_menu_writeToNfcTag),
                        prefContext.getString(R.string.nfc_tag_pref_dlg_writeToNfcTag_nfcNotEnabled),
                        prefContext.getString(android.R.string.ok),
                        null,
                        null, null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        true, true,
                        false, false,
                        false,
                        false,
                        fragment.getActivity()
                );

                if (fragment.getActivity() != null)
                    if (!fragment.getActivity().isFinishing())
                        dialog.show();
            }
            return;
        }

        Intent nfcTagIntent = new Intent(prefContext.getApplicationContext(), NFCTagWriteActivity.class);
        nfcTagIntent.putExtra(NFCTagWriteActivity.EXTRA_TAG_NAME, tag);
        nfcTagIntent.putExtra(NFCTagWriteActivity.EXTRA_TAG_DB_ID, id);
        ((Activity) prefContext).startActivityForResult(nfcTagIntent, RESULT_NFC_TAG_WRITE);
    }

    void setNFCTagFromEditor(String tagName,
                             @SuppressWarnings("SameParameterValue") String tagUid,
                             long tagDbId) {
        addNfcTag(tagName);
        NFCTag tag = new NFCTag(tagDbId, tagName, tagUid);
        if (tagDbId == 0)
            DatabaseHandler.getInstance(prefContext).addNFCTag(tag);
        else
            DatabaseHandler.getInstance(prefContext).updateNFCTag(tag);
        refreshListView(tagName);
    }

    void persistValue() {
        if (shouldPersist()) {
            if (callChangeListener(value))
            {
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

    @Override
    protected Parcelable onSaveInstanceState()
    {
        savedInstanceState = true;

        final Parcelable superState = super.onSaveInstanceState();
        /*if (isPersistent()) {
            return superState;
        }*/

        final NFCTagPreference.SavedState myState = new NFCTagPreference.SavedState(superState);
        myState.value = value;
        myState.defaultValue = defaultValue;

        return myState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state)
    {
        //if (dataWrapper == null)
        //    dataWrapper = new DataWrapper(prefContext, false, 0, false);

        if ((state == null) || (!state.getClass().equals(NFCTagPreference.SavedState.class))) {
            // Didn't save state for us in onSaveInstanceState
            super.onRestoreInstanceState(state);
            return;
        }

        // restore instance state
        NFCTagPreference.SavedState myState = (NFCTagPreference.SavedState)state;
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

        public static final Creator<NFCTagPreference.SavedState> CREATOR =
                new Creator<>() {
                    public NFCTagPreference.SavedState createFromParcel(Parcel in)
                    {
                        return new NFCTagPreference.SavedState(in);
                    }
                    public NFCTagPreference.SavedState[] newArray(int size)
                    {
                        return new NFCTagPreference.SavedState[size];
                    }

                };

    }

}