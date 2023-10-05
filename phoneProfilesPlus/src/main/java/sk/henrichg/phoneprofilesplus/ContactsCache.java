package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.telephony.PhoneNumberUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

class ContactsCache {

    private final ArrayList<Contact> contactList;
    //private final ArrayList<Contact> contactListWithoutNumber;
    private boolean cached;
    private boolean caching;
    //private boolean cancelled;

    ContactsCache()
    {
        contactList = new ArrayList<>();
        //contactListWithoutNumber = new ArrayList<>();
        cached = false;
        caching = false;
    }

    void getContactList(Context context, boolean fixEvents, boolean forceCache)
    {
        if ((cached || caching) && (!forceCache)) return;

        caching = true;

        ArrayList<Contact> _contactList = new ArrayList<>();
        ArrayList<ContactsInEvent> _contactInEventsCall = new ArrayList<>();
        ArrayList<ContactsInEvent> _contactInEventsSMS = new ArrayList<>();
        ArrayList<ContactsInEvent> _contactInEventsNotification = new ArrayList<>();
        ArrayList<Contact> _oldContactList = new ArrayList<>();

        DataWrapper dataWrapper = null;

        try {
            if (Permissions.checkContacts(context)) {

                if (fixEvents && (contactList.size() != 0)) {
//                    Log.e("ContactsCache.getContactList", "contactList.size() != 0");

                    dataWrapper = new DataWrapper(context.getApplicationContext(), false, 0, false, 0, 0, 0f);
                    dataWrapper.fillEventList();

                    // fill array with events, which uses contact cache
                    for (Event _event : dataWrapper.eventList) {
                        if (_event._eventPreferencesCall._enabled) {
                            ContactsInEvent contactsInEvent = new ContactsInEvent();
                            contactsInEvent.event = _event;
                            contactsInEvent.contacts = _event._eventPreferencesCall._contacts;
                            //contactsInEvent.sensorType = EventsHandler.SENSOR_TYPE_PHONE_CALL;
                            _contactInEventsCall.add(contactsInEvent);
                        }
                    }
                    for (Event _event : dataWrapper.eventList) {
                        if (_event._eventPreferencesSMS._enabled) {
                            ContactsInEvent contactsInEvent = new ContactsInEvent();
                            contactsInEvent.event = _event;
                            contactsInEvent.contacts = _event._eventPreferencesSMS._contacts;
                            //contactsInEvent.sensorType = EventsHandler.SENSOR_TYPE_SMS;
                            _contactInEventsSMS.add(contactsInEvent);
                        }
                    }
                    for (Event _event : dataWrapper.eventList) {
                        if (_event._eventPreferencesNotification._enabled) {
                            ContactsInEvent contactsInEvent = new ContactsInEvent();
                            contactsInEvent.event = _event;
                            contactsInEvent.contacts = _event._eventPreferencesNotification._contacts;
                            //contactsInEvent.sensorType = EventsHandler.SENSOR_TYPE_NOTIFICATION;
                            _contactInEventsNotification.add(contactsInEvent);
                        }
                    }
//                    Log.e("ContactsCache.getContactList", "_contactInEventsCall.size()="+_contactInEventsCall.size());
//                    Log.e("ContactsCache.getContactList", "_contactInEventsSMS.size()="+_contactInEventsSMS.size());
//                    Log.e("ContactsCache.getContactList", "_contactInEventsNotification.size()="+_contactInEventsNotification.size());
                } //else
//                    Log.e("ContactsCache.getContactList", "contactList.size() == 0");

                long contactId = 0;
                String name = null;
                String photoId = "0";
                //int hasPhone = 0;

                String[] projection = new String[]{
                        ContactsContract.RawContacts.CONTACT_ID,
                        ContactsContract.RawContacts.ACCOUNT_TYPE,
                        ContactsContract.RawContacts.ACCOUNT_NAME
                };
                Cursor rawCursor = context.getContentResolver().query(ContactsContract.RawContacts.CONTENT_URI, projection, null /*selection*/, null, ContactsContract.RawContacts.CONTACT_ID + " ASC");
                if (rawCursor != null) {
                    while (rawCursor.moveToNext()) {
                        long _contactId = rawCursor.getLong(0);
                        String rawAccountType = rawCursor.getString(1);
                        //rawAccountType = removeLeadingChar(rawAccountType, '\'');
                        //rawAccountType = removeTrailingChar(rawAccountType, '\'');
                        String rawAccountName = rawCursor.getString(2);

                        if (contactId != _contactId) {
                            // contactId cahnged

                            contactId = _contactId;
                            //_oneContactIdList = new ArrayList<>();

                            projection = new String[]{
                                    //ContactsContract.Contacts.HAS_PHONE_NUMBER,
                                    //ContactsContract.Contacts._ID,
                                    ContactsContract.Contacts.DISPLAY_NAME,
                                    ContactsContract.Contacts.PHOTO_ID
                            };

                            Cursor mCursor = context.getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, projection, ContactsContract.Contacts._ID + " = " + contactId, null, null);
                            if (mCursor != null) {
                                if (mCursor.moveToFirst()) {
                                    name = mCursor.getString(mCursor.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME));
                                    photoId = mCursor.getString(mCursor.getColumnIndexOrThrow(ContactsContract.Contacts.PHOTO_ID));
                                    //hasPhone = Integer.parseInt(mCursor.getString(mCursor.getColumnIndexOrThrow(ContactsContract.Contacts.HAS_PHONE_NUMBER)));
                                }
                                else
                                    name = null;
                                mCursor.close();
                            }
                            else
                                name = null;
                        }

                        if ((name != null) && (rawAccountType != null)) {
                            //if (hasPhone > 0) {
                                projection = new String[]{
                                        ContactsContract.CommonDataKinds.Phone._ID,
                                        ContactsContract.CommonDataKinds.Phone.NUMBER//,
                                        //ContactsContract.CommonDataKinds.Phone.ACCOUNT_TYPE_AND_DATA_SET
                                };
                                Cursor phones = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, projection,
                                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=" + contactId + " AND " +
                                        ContactsContract.CommonDataKinds.Phone.ACCOUNT_TYPE_AND_DATA_SET + "=\"" + rawAccountType + "\"",
                                        null, null);
                                if (phones != null) {
                                    if (phones.getCount() > 0) {
                                        while (phones.moveToNext()) {
                                            //String accountType = phones.getString(phones.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.ACCOUNT_TYPE_AND_DATA_SET));
                                            //accountType = removeLeadingChar(accountType, '\'');
                                            //accountType = removeTrailingChar(accountType, '\'');

                                            //if (accountType.equals(rawAccountType)) {
                                            long phoneId = phones.getLong(phones.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone._ID));
                                            String phoneNumber = phones.getString(phones.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER));

                                            Contact aContact = new Contact();
                                            aContact.contactId = contactId;
                                            aContact.name = name;
                                            aContact.phoneId = phoneId;
                                            aContact.phoneNumber = phoneNumber;
                                            try {
                                                aContact.photoId = Long.parseLong(photoId);
                                            } catch (Exception e) {
                                                aContact.photoId = 0;
                                            }
                                            aContact.accountType = rawAccountType; //accountType;
                                            aContact.accountName = rawAccountName;
                                            //_oneContactIdList.add(aContact);
                                            _contactList.add(aContact);
                                            //}
                                        }
                                    }
                                    else {
                                        Contact aContact = new Contact();
                                        aContact.contactId = contactId;
                                        aContact.name = name;
                                        aContact.phoneId = 0;
                                        aContact.phoneNumber = "";
                                        aContact.accountType = rawAccountType;
                                        aContact.accountName = rawAccountName;
                                        try {
                                            aContact.photoId = Long.parseLong(photoId);
                                        } catch (Exception e) {
                                            aContact.photoId = 0;
                                        }
                                        //_oneContactIdList.add(aContact);
                                        _contactList.add(aContact);
                                    }
                                    phones.close();
                                }
                            //}
                            /*else {
                                Contact aContact = new Contact();
                                aContact.contactId = contactId;
                                aContact.name = name;
                                aContact.phoneId = 0;
                                aContact.phoneNumber = "";
                                aContact.accountType = rawAccountType;
                                try {
                                    aContact.photoId = Long.parseLong(photoId);
                                } catch (Exception e) {
                                    aContact.photoId = 0;
                                }
                                //_oneContactIdList.add(aContact);
                                _contactList.add(aContact);
                            }*/
                        }

                    }
                    rawCursor.close();
                }

                _contactList.sort(new ContactsComparator());
                synchronized (PPApplication.contactsCacheMutex) {

                    if (fixEvents && (contactList.size() != 0)) {
//                        Log.e("ContactsCache.getContactList", "contactList.size() != 0");

                        // do copy of old contactList
                        for (Contact _contact : contactList) {
                            Contact dContact = new Contact();
                            dContact.contactId = _contact.contactId;
                            dContact.name = _contact.name;
                            dContact.phoneId = _contact.phoneId;
                            dContact.phoneNumber = _contact.phoneNumber;
                            dContact.photoId = _contact.photoId;
                            dContact.accountType = _contact.accountType;
                            dContact.accountName = _contact.accountName;
                            _oldContactList.add(dContact);
                        }
//                        Log.e("ContactsCache.getContactList", "_oldContactList.size()="+_oldContactList.size());
                    } //else
//                        Log.e("ContactsCache.getContactList", "contactList.size() == 0");

                    updateContacts(_contactList/*, false*/);
                    //updateContacts(_contactListWithoutNumber, true);

                    if (fixEvents && (_oldContactList.size() != 0)) {
//                        Log.e("ContactsCache.getContactList", "_oldContactList.size() != 0");

                        for (ContactsInEvent contactsInEvent : _contactInEventsCall) {
                            // for each contactsInEvent for call sensor
//                            Log.e("ContactsCache.getContactList", "(1) contactsInEvent.event._eventPreferencesCall._contacts="+contactsInEvent.event._eventPreferencesCall._contacts);
                            contactsInEvent.event._eventPreferencesCall._contacts =
                                        covertOldContactToNewContact(contactsInEvent, _oldContactList);
//                            Log.e("ContactsCache.getContactList", "(2) contactsInEvent.event._eventPreferencesCall._contacts="+contactsInEvent.event._eventPreferencesCall._contacts);
                        }
                        for (ContactsInEvent contactsInEvent : _contactInEventsSMS) {
                            // for each contactsInEvent for sms sensor
//                            Log.e("ContactsCache.getContactList", "(1) contactsInEvent.event._eventPreferencesSMS._contacts="+contactsInEvent.event._eventPreferencesSMS._contacts);
                            contactsInEvent.event._eventPreferencesSMS._contacts =
                                    covertOldContactToNewContact(contactsInEvent, _oldContactList);
//                            Log.e("ContactsCache.getContactList", "(2) contactsInEvent.event._eventPreferencesSMS._contacts="+contactsInEvent.event._eventPreferencesSMS._contacts);
                        }
                        for (ContactsInEvent contactsInEvent : _contactInEventsNotification) {
                            // for each contactsInEvent for notification sensor
//                            Log.e("ContactsCache.getContactList", "(1) contactsInEvent.event._eventPreferencesNotification._contacts="+contactsInEvent.event._eventPreferencesNotification._contacts);
                            contactsInEvent.event._eventPreferencesNotification._contacts =
                                    covertOldContactToNewContact(contactsInEvent, _oldContactList);
//                            Log.e("ContactsCache.getContactList", "(2) contactsInEvent.event._eventPreferencesNotification._contacts="+contactsInEvent.event._eventPreferencesNotification._contacts);
                        }
                    } //else
//                        Log.e("ContactsCache.getContactList", "_oldContactList.size() == 0");
                }

                cached = true;
            }
        } catch (SecurityException e) {
            //Log.e("ContactsCache.getContactList", Log.getStackTraceString(e));
            //PPApplicationStatic.recordException(e);

            _contactList.clear();
            //_contactListWithoutNumber.clear();
            synchronized (PPApplication.contactsCacheMutex) {
                updateContacts(_contactList/*, false*/);
                //updateContacts(_contactListWithoutNumber, true);
            }

            cached = false;
        } catch (Exception e) {
            //Log.e("ContactsCache.getContactList", Log.getStackTraceString(e));
            PPApplicationStatic.recordException(e);

            _contactList.clear();
            //_contactListWithoutNumber.clear();
            synchronized (PPApplication.contactsCacheMutex) {
                updateContacts(_contactList/*, false*/);
                //updateContacts(_contactListWithoutNumber, true);
            }

            cached = false;
        }

        if (dataWrapper != null)
            dataWrapper.invalidateDataWrapper();

        caching = false;
    }

/*
    private String removeLeadingChar(String s, char ch) {
        int index;
        for (index = 0; index < s.length(); index++) {
            if (s.charAt(index) != ch) {
                break;
            }
        }
        return s.substring(index);
    }

    private String removeTrailingChar(String s, char ch) {
        int index;
        for (index = s.length() - 1; index >= 0; index--) {
            if (s.charAt(index) != ch) {
                break;
            }
        }
        return s.substring(0, index + 1);
    }
*/

/*
    void getContactList(Context context)
    {
        if (cached || caching) return;

        caching = true;
        //cancelled = false;

        ArrayList<Contact> _contactList = new ArrayList<>();
        //ArrayList<Contact> _contactListWithoutNumber = new ArrayList<>();

        try {
            if (Permissions.checkContacts(context)) {
                String[] projection = new String[]{
                        ContactsContract.Contacts.HAS_PHONE_NUMBER,
                        ContactsContract.Contacts._ID,
                        ContactsContract.Contacts.DISPLAY_NAME,
                        ContactsContract.Contacts.PHOTO_ID};
                //String selection = ContactsContract.Contacts.HAS_PHONE_NUMBER + "='1'";
                //String order = ContactsContract.Contacts.DISPLAY_NAME + " ASC";

                Cursor mCursor = context.getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, projection, null, null, null);

                if (mCursor != null) {
                    while (mCursor.moveToNext()) {
                        //try{
                        long contactId = mCursor.getLong(mCursor.getColumnIndexOrThrow(ContactsContract.Contacts._ID));
                        String name = mCursor.getString(mCursor.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME));
                        if (name != null) {
//                            String hasPhone = mCursor.getString(mCursor.getColumnIndexOrThrow(ContactsContract.Contacts.HAS_PHONE_NUMBER));
                            String photoId = mCursor.getString(mCursor.getColumnIndexOrThrow(ContactsContract.Contacts.PHOTO_ID));
                            if (Integer.parseInt(mCursor.getString(mCursor.getColumnIndexOrThrow(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                                projection = new String[]{
                                        ContactsContract.CommonDataKinds.Phone._ID,
                                        ContactsContract.CommonDataKinds.Phone.NUMBER,
                                        ContactsContract.CommonDataKinds.Phone.ACCOUNT_TYPE_AND_DATA_SET
                                };
                                Cursor phones = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, projection, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + contactId, null, null);
                                if (phones != null) {
                                    while (phones.moveToNext()) {
                                        long phoneId = phones.getLong(phones.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone._ID));
                                        String phoneNumber = phones.getString(phones.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER));
                                        String accountType = phones.getString(phones.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.ACCOUNT_TYPE_AND_DATA_SET));

                                        Contact aContact = new Contact();
                                        aContact.contactId = contactId;
                                        aContact.name = name;
                                        aContact.phoneId = phoneId;
                                        aContact.phoneNumber = phoneNumber;
                                        try {
                                            aContact.photoId = Long.parseLong(photoId);
                                        } catch (Exception e) {
                                            aContact.photoId = 0;
                                        }
                                        aContact.accountType = accountType;
                                        _contactList.add(aContact);

                                        //if (cancelled)
                                        //    break;
                                    }
                                    phones.close();
                                }
                            } else {
                                Contact aContact = new Contact();
                                aContact.contactId = contactId;
                                aContact.name = name;
                                aContact.phoneId = 0;
                                aContact.phoneNumber = "";
                                aContact.accountType = "";
                                try {
                                    aContact.photoId = Long.parseLong(photoId);
                                } catch (Exception e) {
                                    aContact.photoId = 0;
                                }
                                _contactList.add(aContact);
                            }
                        }

                        //}catch(Exception e){}

                        //if (cancelled)
                        //    break;
                    }
                    mCursor.close();
                }

                //if (cancelled)
                //    return;

                Collections.sort(_contactList, new ContactsComparator());

                cached = true;
            }
        } catch (SecurityException e) {
            Log.e("ContactsCache.getContactList", Log.getStackTraceString(e));
            //PPApplicationStatic.recordException(e);

            _contactList.clear();
            //_contactListWithoutNumber.clear();

            cached = false;
        } catch (Exception e) {
            Log.e("ContactsCache.getContactList", Log.getStackTraceString(e));
            PPApplicationStatic.recordException(e);

            _contactList.clear();
            //_contactListWithoutNumber.clear();

            cached = false;
        }

        //if (cached) {
        synchronized (PPApplication.contactsCacheMutex) {
            updateContacts(_contactList);
            //updateContacts(_contactListWithoutNumber, true);
        }
        //}

        caching = false;
    }
*/
/*
    void getContactListX(Context context)
    {
        if (cached || caching) return;

        caching = true;
        //cancelled = false;

        ArrayList<Contact> _contactList = new ArrayList<>();
        //ArrayList<Contact> _contactListWithoutNumber = new ArrayList<>();

        try {
            if (Permissions.checkContacts(context)) {

                Map<Long, List<String>> phones = new HashMap<>();

                String[] projection = new String[]{
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                        ContactsContract.CommonDataKinds.Phone.NUMBER,
                        ContactsContract.CommonDataKinds.Phone._ID,
                        ContactsContract.CommonDataKinds.Phone.ACCOUNT_TYPE_AND_DATA_SET
                };
                String selection = //ContactsContract.Contacts.HAS_PHONE_NUMBER + "=1" + " AND " +
                        "(" +
                        //ContactsContract.CommonDataKinds.Phone.ACCOUNT_TYPE_AND_DATA_SET + "<>'vnd.sec.contact.phone' AND " +
                        //ContactsContract.CommonDataKinds.Phone.ACCOUNT_TYPE_AND_DATA_SET + "<>'vnd.sec.contact.sim' AND " +
                        ContactsContract.CommonDataKinds.Phone.ACCOUNT_TYPE_AND_DATA_SET + "<>'com.google.android.apps.tachyon' AND " +
                        ContactsContract.CommonDataKinds.Phone.ACCOUNT_TYPE_AND_DATA_SET + "<>'org.thoughtcrime.securesms'" +
                        ")"
                        ;

                Cursor mCursor = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, projection, selection, null, null);

                if (mCursor != null) {
                    while (mCursor.moveToNext()) {
                        long contactId = mCursor.getLong(0);
                        String phoneNumber = mCursor.getString(1);
                        long phoneId = mCursor.getLong(2);
                        List<String> list;
                        if (phones.containsKey(contactId)) {
                            list = phones.get(contactId);
                        } else {
                            list = new ArrayList<>();
                            phones.put(contactId, list);
                        }
                        if (list != null)
                            list.add(phoneId+"|"+phoneNumber);

                        //if (cancelled)
                        //    break;
                    }
                    mCursor.close();
                }

                //if (cancelled)
                //    return;

                projection = new String[]{
                        ContactsContract.Contacts._ID,
                        ContactsContract.Contacts.DISPLAY_NAME,
                        ContactsContract.CommonDataKinds.Phone.PHOTO_ID
                };

                mCursor = context.getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, projection, null, null, null);

                if (mCursor != null) {
                    while (mCursor.moveToNext()) {
                        long contactId = mCursor.getLong(0);
                        String name = mCursor.getString(1);
                        String photo = mCursor.getString(2);
                        List<String> contactPhones = phones.get(contactId);
                        if (contactPhones != null) {
                            for (String phone : contactPhones) {
                                String[] splits = phone.split(StringConstants.STR_SPLIT_REGEX);
                                Contact aContact = new Contact();
                                aContact.contactId = contactId;
                                aContact.name = name;
                                aContact.phoneId = Long.parseLong(splits[0]);
                                aContact.phoneNumber = splits[1];
                                try {
                                    aContact.photoId = Long.parseLong(photo);
                                } catch (Exception e) {
                                    aContact.photoId = 0;
                                }
                                _contactList.add(aContact);
                            }
                        }

                        //if (cancelled)
                        //    break;
                    }

                    mCursor.close();
                }

                //if (cancelled)
                //    return;

                Collections.sort(_contactList, new ContactsComparator());

                cached = true;
            }
        } catch (SecurityException e) {
            //Log.e("ContactsCache.getContactListX", Log.getStackTraceString(e));
            //PPApplicationStatic.recordException(e);

            _contactList.clear();
            //_contactListWithoutNumber.clear();

            cached = false;
        } catch (Exception e) {
            //Log.e("ContactsCache.getContactListX", Log.getStackTraceString(e));
            PPApplicationStatic.recordException(e);

            _contactList.clear();
            //_contactListWithoutNumber.clear();

            cached = false;
        }

        //if (cached) {
        synchronized (PPApplication.contactsCacheMutex) {
            updateContacts(_contactList);
            //updateContacts(_contactListWithoutNumber, true);
        }
        //}

        caching = false;
    }
*/

    void updateContacts(List<Contact> _contactList/*, boolean withoutNumber*/) {
        /*if (withoutNumber) {
            contactListWithoutNumber.clear();
            contactListWithoutNumber.addAll(_contactList);
        }
        else {*/
            contactList.clear();
            contactList.addAll(_contactList);
        //}
    }

    List<Contact> getList(/*boolean withoutNumber*/)
    {
        synchronized (PPApplication.contactsCacheMutex) {
            if (cached) {
            /*if (withoutNumber)
                return contactListWithoutNumber;
            else*/
                //return contactList;

                ArrayList<Contact> copyOfList = new ArrayList<>();
                for (Contact contact : contactList) {
                    Contact copOfContact = new Contact();
                    copOfContact.contactId = contact.contactId;

                    if (contact.groups != null) {
                        copOfContact.groups = new ArrayList<>();
                        for (Long group : contact.groups) {
                            long _group = group;
                            copOfContact.groups.add(_group);
                        }
                    } else
                        copOfContact.groups = null;

                    copOfContact.name = contact.name;
                    copOfContact.phoneId = contact.phoneId;
                    copOfContact.phoneNumber = contact.phoneNumber;
                    copOfContact.photoId = contact.photoId;
                    copOfContact.accountType = contact.accountType;
                    copOfContact.accountName = contact.accountName;
                    copyOfList.add(copOfContact);
                }
                return copyOfList;

            } else
                return null;
        }
    }

    void clearCache()
    {
        synchronized (PPApplication.contactsCacheMutex) {
            contactList.clear();
            //contactListWithoutNumber.clear();
            cached = false;
            caching = false;
        }
    }

    boolean getCaching() {
        return caching;
    }

    private String covertOldContactToNewContact(ContactsInEvent contactsInEvent, List<Contact> _oldContactList) {
        if (contactsInEvent.contacts == null)
            return "";

        StringBuilder newContacts = new StringBuilder();

        String[] splits = contactsInEvent.contacts.split(StringConstants.STR_SPLIT_REGEX);
        for (String split : splits) {
            // for each contact in contactsInEvent.contacts
            String[] splits2 = split.split("#");

            if (splits2[0].isEmpty())
                continue;
            if (splits2[1].isEmpty())
                continue;

            long _contactId = Long.parseLong(splits2[0]);
            long _phoneId = Long.parseLong(splits2[1]);

//            Log.e("ContactsCache.covertOldContactToNewContact", "_contactId="+_contactId);
//            Log.e("ContactsCache.covertOldContactToNewContact", "_phoneId="+_phoneId);

            boolean foundInNew = false;
            // search one contact from contactsInEvent.contacts
            for (Contact oldContact : _oldContactList) {
                boolean foundInOld = false;
                if (_phoneId != 0) {
                    if ((oldContact.contactId == _contactId) && (oldContact.phoneId == _phoneId))
                        foundInOld = true;
                } else {
                    if (oldContact.contactId == _contactId)
                        foundInOld = true;
                }
//                Log.e("ContactsCache.covertOldContactToNewContact", "foundInOld="+foundInOld);
                if (foundInOld) {
                    // found contact in old list

                    // search it in new list
                    for (Contact newContact : contactList) {
                        // search these fields in new contactList
                        if (newContact.name.equals(oldContact.name) &&
                                PhoneNumberUtils.compare(newContact.phoneNumber, oldContact.phoneNumber) &&
                                newContact.accountType.equals(oldContact.accountType)) {
                            foundInNew = true;
//                            Log.e("ContactsCache.covertOldContactToNewContact", "newContact.contactId="+newContact.contactId);
//                            Log.e("ContactsCache.covertOldContactToNewContact", "newContact.phoneId="+newContact.phoneId);
                            // update contact to new contact in event
                            if (newContacts.length() > 0)
                                newContacts.append("|");
                            newContacts.append(newContact.contactId).append("#").append(newContact.phoneId);
                            break;
                        }
                    }
                    break;
                }
            }
//            Log.e("ContactsCache.covertOldContactToNewContact", "foundInNew="+foundInNew);
            if (!foundInNew) {
                // get back old contact
                if (newContacts.length() > 0)
                    newContacts.append("|");
                newContacts.append(split);
            }
        }

        return newContacts.toString();
    }

    private static class ContactsComparator implements Comparator<Contact> {

        public int compare(Contact lhs, Contact rhs) {
            if (PPApplication.collator != null)
                return PPApplication.collator.compare(lhs.name, rhs.name);
            else
                return 0;
        }
    }

    private static class ContactsInEvent {
        Event event = null;
        String contacts = null;
        //int sensorType = -1;
    }

}
