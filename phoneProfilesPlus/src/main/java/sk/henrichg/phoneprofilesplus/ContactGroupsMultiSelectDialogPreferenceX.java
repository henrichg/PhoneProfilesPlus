package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.ContactsContract;
import android.util.AttributeSet;

import com.crashlytics.android.Crashlytics;

import java.util.List;

import androidx.preference.DialogPreference;

public class ContactGroupsMultiSelectDialogPreferenceX extends DialogPreference
{
    ContactGroupsMultiSelectDialogPreferenceFragmentX fragment;

    private final Context _context;
    String value = "";
    private String defaultValue;
    private boolean savedInstanceState;

    public ContactGroupsMultiSelectDialogPreferenceX(Context context, AttributeSet attrs) {
        super(context, attrs);

        _context = context;

        //if (EditorProfilesActivity.getContactGroupsCache() == null)
        //    EditorProfilesActivity.createContactGroupsCache();

    }

    @Override
    protected void onSetInitialValue(Object defaultValue)
    {
        value = getPersistedString((String)defaultValue);
        this.defaultValue = (String)defaultValue;

        getValueCMSDP();
        setSummaryCMSDP();
    }

    @SuppressWarnings("SameParameterValue")
    void refreshListView(final boolean notForUnselect) {
        if (fragment != null)
            fragment.refreshListView(notForUnselect);
    }

    void getValueCMSDP()
    {
        // change checked state by value
        ContactGroupsCache contactGroupsCache = PhoneProfilesService.getContactGroupsCache();
        if (contactGroupsCache != null) {
            synchronized (PPApplication.contactGroupsCacheMutex) {
                List<ContactGroup> contactGroupList = contactGroupsCache.getList();
                if (contactGroupList != null) {
                    String[] splits = value.split("\\|");
                    for (ContactGroup contactGroup : contactGroupList) {
                        contactGroup.checked = false;
                        for (String split : splits) {
                            try {
                                long groupId = Long.parseLong(split);
                                if (contactGroup.groupId == groupId)
                                    contactGroup.checked = true;
                            } catch (Exception e) {
                                //Crashlytics.logException(e);
                            }
                        }
                    }
                    // move checked on top
                    int i = 0;
                    int ich = 0;
                    while (i < contactGroupList.size()) {
                        ContactGroup contactGroup = contactGroupList.get(i);
                        if (contactGroup.checked) {
                            contactGroupList.remove(i);
                            contactGroupList.add(ich, contactGroup);
                            ich++;
                        }
                        i++;
                    }
                }
            }
        }
    }

    static String getSummary(String value, Context context) {
        String summary = context.getString(R.string.contacts_multiselect_summary_text_not_selected);
        if (Permissions.checkContacts(context)) {
            if (!value.isEmpty()) {
                String[] splits = value.split("\\|");
                if (splits.length == 1) {
                    boolean found = false;
                    String[] projection = new String[]{
                            ContactsContract.Groups._ID,
                            ContactsContract.Groups.TITLE};
                    String selection = ContactsContract.Groups._ID + "=" + splits[0];
                    Cursor mCursor = context.getContentResolver().query(ContactsContract.Groups.CONTENT_SUMMARY_URI, projection, selection, null, null);

                    if (mCursor != null) {
                        //while (mCursor.moveToNext()) {
                        if (mCursor.moveToFirst()) {
                            found = true;
                            summary = mCursor.getString(mCursor.getColumnIndex(ContactsContract.Groups.TITLE));
                            //break;
                        }
                        mCursor.close();
                    }
                    if (!found)
                        summary = context.getString(R.string.contacts_multiselect_summary_text_selected) + ": " + splits.length;
                } else
                    summary = context.getString(R.string.contacts_multiselect_summary_text_selected) + ": " + splits.length;
            }
        }
        return summary;
    }

    private void setSummaryCMSDP()
    {
        setSummary(getSummary(value, _context));
    }

    @SuppressWarnings("StringConcatenationInLoop")
    private void getValue() {
        // fill with strings of contact groups separated with |
        value = "";
        ContactGroupsCache contactGroupsCache = PhoneProfilesService.getContactGroupsCache();
        if (contactGroupsCache != null) {
            synchronized (PPApplication.contactGroupsCacheMutex) {
                List<ContactGroup> contactGroupList = contactGroupsCache.getList();
                if (contactGroupList != null) {
                    for (ContactGroup contactGroup : contactGroupList) {
                        if (contactGroup.checked) {
                            if (!value.isEmpty())
                                value = value + "|";
                            value = value + contactGroup.groupId;
                        }
                    }
                }
            }
        }
    }

    void persistValue() {
        if (shouldPersist())
        {
            getValue();
            persistString(value);

            setSummaryCMSDP();
        }
    }

    void resetSummary() {
        if (!savedInstanceState) {
            value = getPersistedString(defaultValue);
            setSummaryCMSDP();
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

        final ContactGroupsMultiSelectDialogPreferenceX.SavedState myState = new ContactGroupsMultiSelectDialogPreferenceX.SavedState(superState);
        getValue();
        myState.value = value;
        myState.defaultValue = defaultValue;

        return myState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state)
    {
        //if (dataWrapper == null)
        //    dataWrapper = new DataWrapper(prefContext, false, 0, false);

        if (!state.getClass().equals(ContactGroupsMultiSelectDialogPreferenceX.SavedState.class)) {
            // Didn't save state for us in onSaveInstanceState
            super.onRestoreInstanceState(state);
            setSummaryCMSDP();
            return;
        }

        // restore instance state
        ContactGroupsMultiSelectDialogPreferenceX.SavedState myState = (ContactGroupsMultiSelectDialogPreferenceX.SavedState)state;
        super.onRestoreInstanceState(myState.getSuperState());
        value = myState.value;
        defaultValue = myState.defaultValue;

        getValueCMSDP();
        setSummaryCMSDP();
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
        public static final Creator<ContactGroupsMultiSelectDialogPreferenceX.SavedState> CREATOR =
                new Creator<ContactGroupsMultiSelectDialogPreferenceX.SavedState>() {
                    public ContactGroupsMultiSelectDialogPreferenceX.SavedState createFromParcel(Parcel in)
                    {
                        return new ContactGroupsMultiSelectDialogPreferenceX.SavedState(in);
                    }
                    public ContactGroupsMultiSelectDialogPreferenceX.SavedState[] newArray(int size)
                    {
                        return new ContactGroupsMultiSelectDialogPreferenceX.SavedState[size];
                    }

                };

    }

}
