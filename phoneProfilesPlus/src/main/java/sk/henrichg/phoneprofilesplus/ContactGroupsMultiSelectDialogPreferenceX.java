package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.util.AttributeSet;

import java.util.List;

import androidx.preference.DialogPreference;

public class ContactGroupsMultiSelectDialogPreferenceX extends DialogPreference
{
    ContactGroupsMultiSelectDialogPreferenceFragmentX fragment;

    private final Context _context;
    String value = "";

    public ContactGroupsMultiSelectDialogPreferenceX(Context context, AttributeSet attrs) {
        super(context, attrs);

        _context = context;

        if (EditorProfilesActivity.getContactGroupsCache() == null)
            EditorProfilesActivity.createContactGroupsCache();

    }

    @Override
    protected void onSetInitialValue(Object defaultValue)
    {
        value = getPersistedString(value);
        getValueCMSDP();
        setSummaryCMSDP();
    }

    void getValueCMSDP()
    {
        // change checked state by value
        List<ContactGroup> contactGroupList = EditorProfilesActivity.getContactGroupsCache().getList();
        if (contactGroupList != null)
        {
            String[] splits = value.split("\\|");
            for (ContactGroup contactGroup : contactGroupList)
            {
                contactGroup.checked = false;
                for (String split : splits) {
                    try {
                        long groupId = Long.parseLong(split);
                        if (contactGroup.groupId == groupId)
                            contactGroup.checked = true;
                    } catch (Exception ignored) {
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

    private void setSummaryCMSDP()
    {
        String prefVolumeDataSummary = _context.getString(R.string.contacts_multiselect_summary_text_not_selected);
        if (Permissions.checkContacts(_context)) {
            if (!value.isEmpty()) {
                String[] splits = value.split("\\|");
                if (splits.length == 1) {
                    boolean found = false;
                    String[] projection = new String[]{
                            ContactsContract.Groups._ID,
                            ContactsContract.Groups.TITLE};
                    String selection = ContactsContract.Groups._ID + "=" + splits[0];
                    Cursor mCursor = _context.getContentResolver().query(ContactsContract.Groups.CONTENT_SUMMARY_URI, projection, selection, null, null);

                    if (mCursor != null) {
                        //while (mCursor.moveToNext()) {
                        if (mCursor.moveToFirst()) {
                            found = true;
                            prefVolumeDataSummary = mCursor.getString(mCursor.getColumnIndex(ContactsContract.Groups.TITLE));
                            //break;
                        }
                        mCursor.close();
                    }
                    if (!found)
                        prefVolumeDataSummary = _context.getString(R.string.contacts_multiselect_summary_text_selected) + ": " + splits.length;
                } else
                    prefVolumeDataSummary = _context.getString(R.string.contacts_multiselect_summary_text_selected) + ": " + splits.length;
            }
        }
        setSummary(prefVolumeDataSummary);
    }

    @SuppressWarnings("StringConcatenationInLoop")
    void persistValue() {
        if (shouldPersist())
        {
            // fill with strings of contact groups separated with |
            value = "";
            List<ContactGroup> contactGroupList = EditorProfilesActivity.getContactGroupsCache().getList();
            if (contactGroupList != null)
            {
                for (ContactGroup contactGroup : contactGroupList)
                {
                    if (contactGroup.checked)
                    {
                        if (!value.isEmpty())
                            value = value + "|";
                        value = value + contactGroup.groupId;
                    }
                }
            }
            persistString(value);

            setSummaryCMSDP();
        }
    }

}
