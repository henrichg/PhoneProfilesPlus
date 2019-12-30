package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.ContactsContract;
import android.util.AttributeSet;
import java.util.List;

import androidx.preference.DialogPreference;

public class ContactsMultiSelectDialogPreferenceX extends DialogPreference
{
    ContactsMultiSelectDialogPreferenceFragmentX fragment;

    private final Context _context;

    private final boolean withoutNumbers;

    String value = "";
    private String defaultValue;
    private boolean savedInstanceState;

    List<Contact> contactList;

    public ContactsMultiSelectDialogPreferenceX(Context context, AttributeSet attrs) {
        super(context, attrs);

        _context = context;

        TypedArray locationGeofenceType = context.obtainStyledAttributes(attrs,
                R.styleable.ContactsMultiSelectDialogPreference, 0, 0);
        withoutNumbers = locationGeofenceType.getBoolean(R.styleable.ContactsMultiSelectDialogPreference_withoutNumbers, false);

        locationGeofenceType.recycle();

        //if (PhoneProfilesService.getContactsCache() == null)
        //    PhoneProfilesService.createContactsCache();

    }

    @Override
    protected void onSetInitialValue(Object defaultValue)
    {
        // Get the persistent value
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
        contactList = PhoneProfilesService.getContactsCache().getList(withoutNumbers);
        if (contactList != null)
        {
            String[] splits = value.split("\\|");
            for (Contact contact : contactList)
            {
                contact.checked = false;
                for (String split : splits) {
                    try {
                        String[] splits2 = split.split("#");
                        long contactId = Long.parseLong(splits2[0]);
                        long phoneId = Long.parseLong(splits2[1]);
                        if ((contact.contactId == contactId) && (contact.phoneId == phoneId))
                            contact.checked = true;
                    } catch (Exception ignored) {
                    }
                }
            }
            // move checked on top
            int i = 0;
            int ich = 0;
            while (i < contactList.size()) {
                Contact contact = contactList.get(i);
                if (contact.checked) {
                    contactList.remove(i);
                    contactList.add(ich, contact);
                    ich++;
                }
                i++;
            }
        }
    }

    static String getSummary(String value, boolean withoutNumbers, Context context) {
        String summary = context.getString(R.string.contacts_multiselect_summary_text_not_selected);
        if (Permissions.checkContacts(context)) {
            if (!value.isEmpty()) {
                String[] splits = value.split("\\|");
                if (splits.length == 1) {
                    boolean found = false;
                    String[] projection = new String[]{
                            ContactsContract.Contacts._ID,
                            ContactsContract.Contacts.DISPLAY_NAME,
                            ContactsContract.Contacts.PHOTO_ID};
                    String[] splits2 = splits[0].split("#");
                    String selection = ContactsContract.Contacts._ID + "=" + splits2[0];
                    Cursor mCursor = context.getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, projection, selection, null, null);

                    if (mCursor != null) {
                        while (mCursor.moveToNext()) {
                            selection = ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=" + splits2[0] + " AND " +
                                    ContactsContract.CommonDataKinds.Phone._ID + "=" + splits2[1];
                            Cursor phones = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, selection, null, null);
                            if (phones != null) {
                                //while (phones.moveToNext()) {
                                if (phones.moveToFirst()) {
                                    found = true;
                                    summary = mCursor.getString(mCursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                                    if (!withoutNumbers)
                                        summary = summary + "\n" + phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                                    //break;
                                }
                                phones.close();
                            }
                            if (found)
                                break;
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
        setSummary(getSummary(value, withoutNumbers, _context));
    }

    @SuppressWarnings("StringConcatenationInLoop")
    private void getValue() {
        // fill with strings of contacts separated with |
        value = "";
        if (contactList != null)
        {
            for (Contact contact : contactList)
            {
                if (contact.checked)
                {
                    if (!value.isEmpty())
                        value = value + "|";
                    value = value + contact.contactId + "#" + contact.phoneId;
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

        final ContactsMultiSelectDialogPreferenceX.SavedState myState = new ContactsMultiSelectDialogPreferenceX.SavedState(superState);
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

        if (!state.getClass().equals(ContactsMultiSelectDialogPreferenceX.SavedState.class)) {
            // Didn't save state for us in onSaveInstanceState
            super.onRestoreInstanceState(state);
            setSummaryCMSDP();
            return;
        }

        // restore instance state
        ContactsMultiSelectDialogPreferenceX.SavedState myState = (ContactsMultiSelectDialogPreferenceX.SavedState)state;
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
        public static final Creator<ContactsMultiSelectDialogPreferenceX.SavedState> CREATOR =
                new Creator<ContactsMultiSelectDialogPreferenceX.SavedState>() {
                    public ContactsMultiSelectDialogPreferenceX.SavedState createFromParcel(Parcel in)
                    {
                        return new ContactsMultiSelectDialogPreferenceX.SavedState(in);
                    }
                    public ContactsMultiSelectDialogPreferenceX.SavedState[] newArray(int size)
                    {
                        return new ContactsMultiSelectDialogPreferenceX.SavedState[size];
                    }

                };

    }

}
