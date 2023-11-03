package sk.henrichg.phoneprofilesplus;

import android.content.ContentUris;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.ContactsContract;
import android.telephony.PhoneNumberUtils;
import android.util.AttributeSet;

import androidx.preference.DialogPreference;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ContactsMultiSelectDialogPreference extends DialogPreference
{
    ContactsMultiSelectDialogPreferenceFragment fragment;

    private final Context _context;

    final boolean withoutNumbers;

    String value = "";
    private String defaultValue;
    private boolean savedInstanceState;

    List<Contact> contactList;
    List<Contact> checkedContactList;

    //String filterNameFromContactsFilterDialog;
    ContactFilter contactsFilter;

    public ContactsMultiSelectDialogPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        _context = context;

        //noinspection resource
        TypedArray locationGeofenceType = context.obtainStyledAttributes(attrs,
                R.styleable.PPContactsMultiSelectDialogPreference, 0, 0);
        withoutNumbers = locationGeofenceType.getBoolean(R.styleable.PPContactsMultiSelectDialogPreference_withoutNumbers, false);

        locationGeofenceType.recycle();

        contactsFilter = new ContactFilter();
        contactsFilter.data = StringConstants.CONTACTS_FILTER_DATA_ALL;
        contactsFilter.displayName = context.getString(R.string.contacts_filter_dialog_item_show_all);

        //if (PhoneProfilesService.getContactsCache() == null)
        //    PhoneProfilesService.createContactsCache();

    }

    @Override
    protected void onSetInitialValue(Object defaultValue)
    {
        // Get the persistent value
        value = getPersistedString((String)defaultValue);
//        PPApplicationStatic.logE("[CONTACTS_DIALOG] ContactsMultiSelectDialogPreference.onSetInitialValue", "value="+value);
        this.defaultValue = (String)defaultValue;
        //getValueCMSDP(); // toto cita z cache, je tam blokoanie mutexom
        setSummaryCMSDP(); // toto cita z databazy, ak je len jedne kontakt nastaveny
    }

    void refreshListView(@SuppressWarnings("SameParameterValue") final boolean notForUnselect) {
        if (fragment != null)
            fragment.refreshListView(notForUnselect);
    }

    void setContactsFilter(ContactFilter filter) {
        contactsFilter = filter;
        if (fragment != null)
            fragment.setContactsFilter(filter);
    }

    /*
    void setFilterNameText(String text) {
        filterNameFromContactsFilterDialog = text;
    }

    String getFilterNameText() {
        //if (fragment != null) {
        //    return fragment.getCellNameText();
        //}
        //else {
        //    return null;
        //}
        return filterNameFromContactsFilterDialog;
    }
    */

    void getValueCMSDP()
    {
        // change checked state by value
        ContactsCache contactsCache = PPApplicationStatic.getContactsCache();
        if (contactsCache == null)
            return;

        synchronized (PPApplication.contactsCacheMutex) {
            List<Contact>  localContactList = contactsCache.getList(/*withoutNumbers*/);
            if (localContactList != null) {
//                PPApplicationStatic.logE("[CONTACTS_DIALOG] ContactsMultiSelectDialogPreference.getValueCMSDP", "localContactList.size()="+localContactList.size());

                // save checked contacts
                if (contactList != null) {
                    checkedContactList = new ArrayList<>();
                    for (Contact contact : contactList) {
                        if (contact.checked)
                            checkedContactList.add(contact);
                    }
                }

                contactList = new ArrayList<>();

                // add all checked
                if (checkedContactList != null)
                    contactList.addAll(checkedContactList);

                // add not in checked and only filtered
                if (!withoutNumbers) {
                    for (Contact localContact : localContactList) {
                        if (localContact.phoneId != 0) {
//                        if (localContact.accountType.equals("com.viber.voip")) {
//                            Log.e("ContactsMultiSelectDialogPreference.getValueCMSDP", "localContact.name=" + localContact.name);
//                            Log.e("ContactsMultiSelectDialogPreference.getValueCMSDP", "localContact.contactId=" + localContact.contactId);
//                            Log.e("ContactsMultiSelectDialogPreference.getValueCMSDP", "localContact.phoneId=" + localContact.phoneId);
//                            Log.e("ContactsMultiSelectDialogPreference.getValueCMSDP", "localContact.phoneNumber=" + localContact.phoneNumber);
//                        }
                            boolean found = false;
                            for (Contact contact : contactList) {
                                if ((contact.contactId == localContact.contactId) &&
                                        (contact.phoneId == localContact.phoneId) &&
                                        (contact.accountType.equals(localContact.accountType))) {
                                    found = true;
                                    break;
                                }
                            }
                            if (!found) {
                                if (contactsFilter.data.equals(StringConstants.CONTACTS_FILTER_DATA_ALL)
                                        || localContact.accountType.equals(contactsFilter.data)) {
                                    contactList.add(localContact);
                                }
                            }
                        }
                    }
                } else {
                    for (Contact localContact : localContactList) {
                        boolean found = false;
                        for (Contact contact : contactList) {
                            if ((contact.contactId == localContact.contactId)  &&
                                    (contact.accountType.equals(localContact.accountType))) {
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            if (contactsFilter.data.equals(StringConstants.CONTACTS_FILTER_DATA_ALL)
                                    || localContact.accountType.equals(contactsFilter.data)) {
                                contactList.add(localContact);
                            }
                        }
                    }
                }

                String[] splits = value.split(StringConstants.STR_SPLIT_REGEX);

                // add from value not found in contact cache
                for (String split : splits) {
                    try {
                        boolean found = false;

                        String[] splits2 = split.split(StringConstants.STR_SPLIT_CONTACTS_REGEX);
                        String contactName = splits2[0];
                        String contactPhoneNumber = splits2[1];
                        String contactAccountType = splits2[2];

                        for (Contact contact : contactList) {
                            if (withoutNumbers) {
                                if (contact.name.equals(contactName) &&
                                        contact.accountType.equals(contactAccountType)) {
                                    found = true;
                                    break;
                                }
                            } else {
                                if (contact.name.equals(contactName) &&
                                        PhoneNumberUtils.compare(contact.phoneNumber, contactPhoneNumber) &&
                                        contact.accountType.equals(contactAccountType)) {
                                    found = true;
                                    break;
                                }
                            }
                        }

                        if (!found) {
//                            PPApplicationStatic.logE("[CONTACTS_DIALOG] ContactsMultiSelectDialogPreference.getValueCMSDP",
//                                    "not found in contactList split=" + split);

                            Contact aContact = new Contact();
                            aContact.contactId = -1000;
                            aContact.name = contactName;
                            aContact.phoneId = 0;
                            aContact.phoneNumber = contactPhoneNumber;
                            aContact.photoId = 0;
                            aContact.accountType = contactAccountType; //accountType;
                            aContact.accountName = contactAccountType;
                            //_oneContactIdList.add(aContact);
                            contactList.add(aContact);

//                            PPApplicationStatic.logE("[CONTACTS_DIALOG] ContactsMultiSelectDialogPreference.getValueCMSDP",
//                                    "not founded split added into contactList");
                        }

                    } catch (Exception e) {
                        //PPApplicationStatic.recordException(e);
                    }
                }

//                PPApplicationStatic.logE("[CONTACTS_DIALOG] ContactsMultiSelectDialogPreference.getValueCMSDP", "contactList.size()="+contactList.size());
//                PPApplicationStatic.logE("[CONTACTS_DIALOG] ContactsMultiSelectDialogPreference.getValueCMSDP", "value="+value);
                for (Contact contact : contactList) {
                    if (withoutNumbers || (contact.phoneId != 0)) {

                        // set checked for contacts in value
                        contact.checked = false;
                        for (String split : splits) {
                            try {
                                String[] splits2 = split.split(StringConstants.STR_SPLIT_CONTACTS_REGEX);
                                String contactName = splits2[0];
                                String contactPhoneNumber = splits2[1];
                                String contactAccountType = splits2[2];
                                if (withoutNumbers) {
                                    if (contact.name.equals(contactName) &&
                                            contact.accountType.equals(contactAccountType)) {
                                        contact.checked = true;
//                                        PPApplicationStatic.logE("[CONTACTS_DIALOG] ContactsMultiSelectDialogPreference.getValueCMSDP",
//                                                "checked split=" + split);
                                    }
                                } else {
                                    if (contact.name.equals(contactName) &&
                                            PhoneNumberUtils.compare(contact.phoneNumber, contactPhoneNumber) &&
                                            contact.accountType.equals(contactAccountType)) {
                                        contact.checked = true;
//                                        PPApplicationStatic.logE("[CONTACTS_DIALOG] ContactsMultiSelectDialogPreference.getValueCMSDP",
//                                                "checked split=" + split);
                                    }
                                }
                            } catch (Exception e) {
                                //PPApplicationStatic.recordException(e);
                            }
                        }
                        if (checkedContactList != null) {
                            for (Contact checkedContact : checkedContactList) {
                                try {
                                    String contactName = checkedContact.name;
                                    String contactPhoneNumber = checkedContact.phoneNumber;
                                    String contactAccountType = checkedContact.accountType;
                                    if (withoutNumbers) {
                                        if (contact.name.equals(contactName) &&
                                                contact.accountType.equals(contactAccountType)) {
                                            contact.checked = true;
                                        }
                                    } else {
                                        if (contact.name.equals(contactName) &&
                                                PhoneNumberUtils.compare(contact.phoneNumber, contactPhoneNumber) &&
                                                contact.accountType.equals(contactAccountType)) {
                                            contact.checked = true;
                                        }
                                    }
                                } catch (Exception e) {
                                    //PPApplicationStatic.recordException(e);
                                }
                            }
                        }
                    }

                    contact.photoUri = getPhotoUri(contact.contactId);

                    boolean found = false;
                    String accountType = "";
                    PackageManager packageManager = _context.getPackageManager();
                    try {
                        ApplicationInfo applicationInfo = packageManager.getApplicationInfo(contact.accountType, PackageManager.MATCH_ALL);
                        if (applicationInfo != null) {
                            accountType = packageManager.getApplicationLabel(applicationInfo).toString();
                            found = true;
                        }
                    } catch (Exception ignored) {}
                    if (!found) {
                        if (contact.accountType.equals("com.osp.app.signin"))
                            accountType = _context.getString(R.string.contact_account_type_samsung_account);
                        if (contact.accountType.equals("com.google"))
                            accountType = _context.getString(R.string.contact_account_type_google_account);
                        if (contact.accountType.equals("vnd.sec.contact.sim"))
                            accountType = _context.getString(R.string.contact_account_type_sim_card);
                        if (contact.accountType.equals("vnd.sec.contact.sim2"))
                            accountType = _context.getString(R.string.contact_account_type_sim_card);
                        if (contact.accountType.equals("vnd.sec.contact.phone"))
                            accountType = _context.getString(R.string.contact_account_type_phone_application);
                        if (contact.accountType.equals("org.thoughtcrime.securesms"))
                            accountType = "Signal";
                        if (contact.accountType.equals("com.google.android.apps.tachyon"))
                            accountType = "Duo";
                        if (contact.accountType.equals("com.whatsapp"))
                            accountType = "WhatsApp";
                    }
                    if ((!accountType.isEmpty()) &&
                            (!contact.accountType.equals("vnd.sec.contact.sim")) &&
                            (!contact.accountType.equals("vnd.sec.contact.sim2")) &&
                            (!contact.accountType.equals("vnd.sec.contact.phone")) &&
                            (!contact.accountName.equals(accountType)))
                        accountType = accountType + StringConstants.CHAR_NEW_LINE+"  - " + contact.accountName;
                    if (accountType.isEmpty())
                        accountType = contact.accountType;
                    contact.displayedAccountType = accountType;
                }

                contactList.sort(new ContactsComparator());

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

//                PPApplicationStatic.logE("[CONTACTS_DIALOG] ContactsMultiSelectDialogPreference.getValueCMSDP", "after move chcecked up: contactList.size()="+contactList.size());
            } //else
//                PPApplicationStatic.logE("[CONTACTS_DIALOG] ContactsMultiSelectDialogPreference.getValueCMSDP", "!!! localContactList=null");
        }
    }

    static String getSummary(String value, boolean withoutNumbers, Context context) {
        String summary = context.getString(R.string.contacts_multiselect_summary_text_not_selected);
        if (!value.isEmpty()) {
            String[] splits = value.split(StringConstants.STR_SPLIT_REGEX);
            if (splits.length == 1) {
                String[] splits2 = splits[0].split(StringConstants.STR_SPLIT_CONTACTS_REGEX);
                if (splits2.length == 3) {
                    summary = splits2[0];
                    if (!withoutNumbers)
                        summary = summary + StringConstants.CHAR_NEW_LINE + splits2[1];
                }
            } else
                summary = context.getString(R.string.contacts_multiselect_summary_text_selected) + StringConstants.STR_COLON_WITH_SPACE + splits.length;
        }
        return summary;
    }

    private void setSummaryCMSDP()
    {
        setSummary(getSummary(value, withoutNumbers, _context));
    }

    private void getValue() {
        // fill with strings of contacts separated with |
        value = "";
        StringBuilder _value = new StringBuilder();
        if (contactList != null)
        {
            for (Contact contact : contactList)
            {
                if (contact.checked)
                {
                    if (_value.length() > 0)
                        _value.append("|");
                    _value.append(contact.name)
                            .append(StringConstants.STR_SPLIT_CONTACTS_REGEX)
                            .append(contact.phoneNumber)
                            .append(StringConstants.STR_SPLIT_CONTACTS_REGEX)
                            .append(contact.accountType);
                }
            }
        }
        value = _value.toString();
    }

    void persistValue() {
        if (shouldPersist())
        {
            getValue();
//            PPApplicationStatic.logE("[CONTACTS_DIALOG] ContactsMultiSelectDialogPreference.persistValue", "value="+value);
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

        final ContactsMultiSelectDialogPreference.SavedState myState = new ContactsMultiSelectDialogPreference.SavedState(superState);
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

        if ((state == null) || (!state.getClass().equals(ContactsMultiSelectDialogPreference.SavedState.class))) {
            // Didn't save state for us in onSaveInstanceState
            super.onRestoreInstanceState(state);
            setSummaryCMSDP();
            return;
        }

        // restore instance state
        ContactsMultiSelectDialogPreference.SavedState myState = (ContactsMultiSelectDialogPreference.SavedState)state;
        super.onRestoreInstanceState(myState.getSuperState());
        value = myState.value;
        defaultValue = myState.defaultValue;

        //getValueCMSDP();
        setSummaryCMSDP();
        refreshListView(true);
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

        public static final Creator<ContactsMultiSelectDialogPreference.SavedState> CREATOR =
                new Creator<ContactsMultiSelectDialogPreference.SavedState>() {
                    public ContactsMultiSelectDialogPreference.SavedState createFromParcel(Parcel in)
                    {
                        return new ContactsMultiSelectDialogPreference.SavedState(in);
                    }
                    public ContactsMultiSelectDialogPreference.SavedState[] newArray(int size)
                    {
                        return new ContactsMultiSelectDialogPreference.SavedState[size];
                    }

                };

    }

    /**
     * @return the photo URI
     */
    private Uri getPhotoUri(long contactId)
    {
    /*    try {
            Cursor cur = context.getContentResolver().query(ContactsContract.Data.CONTENT_URI, null,
                            ContactsContract.Data.CONTACT_ID + "=" + photoId + " AND "
                            + ContactsContract.Data.MIMETYPE + "='"
                            + ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE + "'", null,
                            null);
            if (cur != null)
            {
                if (!cur.moveToFirst())
                {
                    return null; // no photo
                }
            }
            else
                return null; // error in cursor process
        } catch (Exception e) {
            return null;
        }
        */
        try {
            Uri person = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId);
            return Uri.withAppendedPath(person, ContactsContract.Contacts.Photo.CONTENT_DIRECTORY);
        } catch (Exception e) {
            return null;
        }
    }

    private static class ContactsComparator implements Comparator<Contact> {

        public int compare(Contact lhs, Contact rhs) {
            if (PPApplication.collator != null)
                return PPApplication.collator.compare(lhs.name, rhs.name);
            else
                return 0;
        }
    }

}
