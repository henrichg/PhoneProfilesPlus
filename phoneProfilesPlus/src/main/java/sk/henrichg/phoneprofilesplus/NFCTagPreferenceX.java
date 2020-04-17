package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.TypedArray;
import android.nfc.NfcAdapter;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AlertDialog;
import androidx.preference.DialogPreference;

public class NFCTagPreferenceX extends DialogPreference {

    NFCTagPreferenceFragmentX fragment;

    String value;
    private String defaultValue;
    private boolean savedInstanceState;

    List<NFCTag> nfcTagList;

    private final Context context;

    //static final int RESULT_NFC_TAG_READ_EDITOR = 3500;
    static final int RESULT_NFC_TAG_WRITE = 3501;

    //private static final String PREF_SHOW_HELP = "nfc_tag_pref_show_help";

    public NFCTagPreferenceX(Context context, AttributeSet attrs) {
        super(context, attrs);
        
        this.context = context;

        nfcTagList = new ArrayList<>();
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
        String[] splits = value.split("\\|");
        for (String _tag : splits) {
            if (_tag.isEmpty()) {
                setSummary(R.string.applications_multiselect_summary_text_not_selected);
            }
            else
            if (splits.length == 1) {
                setSummary(_tag);
            }
            else {
                String selectedNfcTags = context.getString(R.string.applications_multiselect_summary_text_selected);
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
        String[] splits = value.split("\\|");
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

    @SuppressWarnings("StringConcatenationInLoop")
    void removeNfcTag(String tagName) {
        String[] splits = value.split("\\|");
        value = "";
        for (String tag : splits) {
            if (!tag.isEmpty()) {
                if (!tag.equals(tagName)) {
                    if (!value.isEmpty())
                        value = value + "|";
                    value = value + tag;
                }
            }
        }
    }

    boolean isNfcTagSelected(String tagName) {
        String[] splits = value.split("\\|");
        for (String tag : splits) {
            if (tag.equals(tagName))
                return true;
        }
        return false;
    }

    @SuppressLint("StaticFieldLeak")
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

        NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(context);
        if (!nfcAdapter.isEnabled()) {
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
            dialogBuilder.setTitle(R.string.nfc_tag_pref_dlg_menu_writeToNfcTag);
            dialogBuilder.setMessage(R.string.nfc_tag_pref_dlg_writeToNfcTag_nfcNotEnabled);
            dialogBuilder.setPositiveButton(android.R.string.ok, null);
            AlertDialog dialog = dialogBuilder.create();

            dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialog) {
                    Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
                    if (positive != null) positive.setAllCaps(false);
                    Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
                    if (negative != null) negative.setAllCaps(false);
                }
            });

            if (!((Activity)context).isFinishing())
                dialog.show();
            return;
        }

        Intent nfcTagIntent = new Intent(context.getApplicationContext(), NFCTagWriteActivity.class);
        nfcTagIntent.putExtra(NFCTagWriteActivity.EXTRA_TAG_NAME, tag);
        nfcTagIntent.putExtra(NFCTagWriteActivity.EXTRA_TAG_DB_ID, id);
        ((Activity)context).startActivityForResult(nfcTagIntent, RESULT_NFC_TAG_WRITE);
    }

    void setNFCTagFromEditor(String tagName,
                             @SuppressWarnings("SameParameterValue") String tagUid,
                             long tagDbId) {
        addNfcTag(tagName);
        NFCTag tag = new NFCTag(tagDbId, tagName, tagUid);
        if (tagDbId == 0)
            DatabaseHandler.getInstance(context).addNFCTag(tag);
        else
            DatabaseHandler.getInstance(context).updateNFCTag(tag);
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

        final NFCTagPreferenceX.SavedState myState = new NFCTagPreferenceX.SavedState(superState);
        myState.value = value;
        myState.defaultValue = defaultValue;

        return myState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state)
    {
        //if (dataWrapper == null)
        //    dataWrapper = new DataWrapper(prefContext, false, 0, false);

        if (!state.getClass().equals(NFCTagPreferenceX.SavedState.class)) {
            // Didn't save state for us in onSaveInstanceState
            super.onRestoreInstanceState(state);
            return;
        }

        // restore instance state
        NFCTagPreferenceX.SavedState myState = (NFCTagPreferenceX.SavedState)state;
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
        public static final Creator<NFCTagPreferenceX.SavedState> CREATOR =
                new Creator<NFCTagPreferenceX.SavedState>() {
                    public NFCTagPreferenceX.SavedState createFromParcel(Parcel in)
                    {
                        return new NFCTagPreferenceX.SavedState(in);
                    }
                    public NFCTagPreferenceX.SavedState[] newArray(int size)
                    {
                        return new NFCTagPreferenceX.SavedState[size];
                    }

                };

    }

}