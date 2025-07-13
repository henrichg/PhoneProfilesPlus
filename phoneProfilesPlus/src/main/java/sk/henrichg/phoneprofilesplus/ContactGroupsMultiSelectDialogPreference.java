package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.ContactsContract;
import android.util.AttributeSet;

import androidx.preference.DialogPreference;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ContactGroupsMultiSelectDialogPreference extends DialogPreference
{
    ContactGroupsMultiSelectDialogPreferenceFragment fragment;

    private final Context _context;
    String value = "";
    private String defaultValue;
    private boolean savedInstanceState;

    List<ContactGroup> contactGroupList;

    public ContactGroupsMultiSelectDialogPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        _context = context;

        //if (EditorActivity.getContactGroupsCache() == null)
        //    EditorActivity.createContactGroupsCache();

    }

    @Override
    protected void onSetInitialValue(Object defaultValue)
    {
        value = getPersistedString((String)defaultValue);
        this.defaultValue = (String)defaultValue;

        //getValueCMSDP();
        setSummaryCMSDP();
    }

    /** @noinspection SameParameterValue*/
    void refreshListView(@SuppressWarnings("SameParameterValue") final boolean forUnselect,
                         final boolean forceRefresh) {
        if (fragment != null)
            fragment.refreshListView(forUnselect, forceRefresh);
    }

    void getValueCMSDP()
    {
        // change checked state by value
//        PPApplicationStatic.logE("[CONTACTS_CACHE] ContactGroupsMultiSelectDialogPreference.getValueCMSDP", "PPApplicationStatic.getContactGroupsCache()");
        ContactGroupsCache contactGroupsCache = PPApplicationStatic.getContactGroupsCache();
        if (contactGroupsCache == null)
            return;

//        PPApplicationStatic.logE("[SYNCHRONIZED] ContactGroupsMultiSelectDialogPreference.getValueCMSDP", "PPApplication.contactsCacheMutex");
        synchronized (PPApplication.contactsCacheMutex) {
//            PPApplicationStatic.logE("[CONTACTS_CACHE] ContactGroupsMultiSelectDialogPreference.getValueCMSDP", "contactGroupsCache.getList()");
            List<ContactGroup> localContactGroupList = contactGroupsCache.getList();
            if (localContactGroupList != null) {
                contactGroupList = new ArrayList<>();
                contactGroupList.addAll(localContactGroupList);

                String[] splits = value.split(StringConstants.STR_SPLIT_REGEX);
                for (ContactGroup contactGroup : contactGroupList) {
                    contactGroup.checked = false;
                    for (String split : splits) {
                        try {
                            long groupId = Long.parseLong(split);
                            if (contactGroup.groupId == groupId)
                                contactGroup.checked = true;
                        } catch (Exception e) {
                            //PPApplicationStatic.recordException(e);
                        }
                    }
                }

                contactGroupList.sort(new ContactGroupsComparator());

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

    static String getSummary(String value, Context context) {
        String summary = context.getString(R.string.contacts_multiselect_summary_text_not_selected);
        if (Permissions.checkContacts(context)) {
            if ((value != null) && (!value.isEmpty())) {
                String[] splits = value.split(StringConstants.STR_SPLIT_REGEX);
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
                            summary = mCursor.getString(mCursor.getColumnIndexOrThrow(ContactsContract.Groups.TITLE));
                            summary = ContactGroupsCache.translateContactGroup(summary, context);
                            //break;
                        }
                        mCursor.close();
                    }
                    if (!found)
                        summary = context.getString(R.string.contacts_multiselect_summary_text_selected) + StringConstants.STR_COLON_WITH_SPACE + splits.length;
                } else
                    summary = context.getString(R.string.contacts_multiselect_summary_text_selected) + StringConstants.STR_COLON_WITH_SPACE + splits.length;
            }
        }
        return summary;
    }

    private void setSummaryCMSDP()
    {
        setSummary(getSummary(value, _context));
    }

    private void getValue() {
        // fill with strings of contact groups separated with |
        value = "";
        StringBuilder _value = new StringBuilder();
        if (contactGroupList != null) {
            for (ContactGroup contactGroup : contactGroupList) {
                if (contactGroup.checked) {
                    //if (!value.isEmpty())
                    //    value = value + "|";
                    //value = value + contactGroup.groupId;
                    if (_value.length() > 0)
                        _value.append("|");
                    _value.append(contactGroup.groupId);
                }
            }
        }
        value = _value.toString();
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

        final ContactGroupsMultiSelectDialogPreference.SavedState myState = new ContactGroupsMultiSelectDialogPreference.SavedState(superState);
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

        if ((state == null) || (!state.getClass().equals(ContactGroupsMultiSelectDialogPreference.SavedState.class))) {
            // Didn't save state for us in onSaveInstanceState
            super.onRestoreInstanceState(state);
            setSummaryCMSDP();
            return;
        }

        // restore instance state
        ContactGroupsMultiSelectDialogPreference.SavedState myState = (ContactGroupsMultiSelectDialogPreference.SavedState)state;
        super.onRestoreInstanceState(myState.getSuperState());
        value = myState.value;
        defaultValue = myState.defaultValue;

        //getValueCMSDP();
        setSummaryCMSDP();
        refreshListView(false, false);
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

        public static final Creator<ContactGroupsMultiSelectDialogPreference.SavedState> CREATOR =
                new Creator<>() {
                    public ContactGroupsMultiSelectDialogPreference.SavedState createFromParcel(Parcel in)
                    {
                        return new ContactGroupsMultiSelectDialogPreference.SavedState(in);
                    }
                    public ContactGroupsMultiSelectDialogPreference.SavedState[] newArray(int size)
                    {
                        return new ContactGroupsMultiSelectDialogPreference.SavedState[size];
                    }

                };

    }

    private static class ContactGroupsComparator implements Comparator<ContactGroup> {

        public int compare(ContactGroup lhs, ContactGroup rhs) {
            if (PPApplication.collator != null)
                return PPApplication.collator.compare(lhs.name, rhs.name);
            else
                return 0;
        }
    }

}
