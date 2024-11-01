package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;

import java.util.ArrayList;
import java.util.List;

class ContactsCache {

    private final ArrayList<Contact> contactList;
    //private final ArrayList<Contact> contactListWithoutNumber;
    private volatile boolean cached;
    private volatile boolean caching;
    //private boolean cancelled;

    ContactsCache()
    {
        contactList = new ArrayList<>();
        //contactListWithoutNumber = new ArrayList<>();
        cached = false;
        caching = false;
    }

    void getContactList(Context context, boolean fixEvents)
    {
        //if ((cached || caching) && (!forceCache)) return;

        caching = true;

        ArrayList<Contact> _contactList = new ArrayList<>();
        //ArrayList<ContactsInEvent> _contactInEventsCall = new ArrayList<>();
        //ArrayList<ContactsInEvent> _contactInEventsSMS = new ArrayList<>();
        //ArrayList<ContactsInEvent> _contactInEventsNotification = new ArrayList<>();
        //ArrayList<Contact> _oldContactList = new ArrayList<>();

        //DataWrapper dataWrapper = null;

        try {
            if (Permissions.checkContacts(context)) {

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

                            // get name for contacyt id
                            // all phones in contactid will be with the same name
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
                            try {
                                projection = new String[]{
                                        ContactsContract.CommonDataKinds.Phone._ID,
                                        ContactsContract.CommonDataKinds.Phone.NUMBER//,
                                        //ContactsContract.CommonDataKinds.Phone.ACCOUNT_TYPE_AND_DATA_SET
                                };
                                Cursor phones = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, projection,
                                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=" + contactId + " AND " +
                                                ContactsContract.CommonDataKinds.Phone.ACCOUNT_TYPE_AND_DATA_SET + "='" + rawAccountType + "'",
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
                                    } else {
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
                            } catch (Exception ignored) {
                                // example of crash when contact is from WhatsApp:
                                // android.database.sqlite.SQLiteException: unrecognized token: ""whatsapp" (code 1 SQLITE_ERROR): ,
                                // while compiling: SELECT _id, data1 FROM view_data data LEFT OUTER JOIN (SELECT 0 as STAT_DATA_ID,0 as x_times_used, 0 as x_last_time_used,
                                // 0 as times_used, 0 as last_time_used where 0) as data_usage_stat ON (STAT_DATA_ID=data._id) WHERE (1 AND mimetype_id=5 AND (1=1)) AND
                                // (contact_id=135200 AND account_type_and_data_set="whatsapp e")
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

//                Log.e("ContactsCache.getContactList", "(1) xxxxxx");

                //_contactList.sort(new ContactsComparator());
//                PPApplicationStatic.logE("[SYNCHRONIZED] ContactsCache.getContactList", "(1) PPApplication.contactsCacheMutex");
                synchronized (PPApplication.contactsCacheMutex) {
//                    Log.e("ContactsCache.getContactList", "(1.1) xxxxxx");

                    updateContacts(_contactList/*, false*/);
                    //updateContacts(_contactListWithoutNumber, true);

                    if (fixEvents) {
                        Context appContext = context.getApplicationContext();

                        List<Event> eventList = DatabaseHandler.getInstance(appContext).getAllEvents();

                        for (Event event : eventList) {
                            boolean dataChanged = false;

                            if (!event._eventPreferencesCall._contacts.isEmpty()) {
                                String[] splits = event._eventPreferencesCall._contacts.split(StringConstants.STR_SPLIT_REGEX);
                                String _split = splits[0];
                                String[] _splits2 = _split.split("#");
                                boolean oldData = false;
                                try {
//                                    if (event._name.equals("Белый лист"))
//                                        Log.e("PhoneProfilesService.doForPackageReplaced", "_splits2[0]="+_splits2[0]);
                                    //noinspection unused
                                    long l = Long.parseLong(_splits2[0]);
                                    oldData = true;
                                } catch (Exception ignored) {
                                }
//                                if (event._name.equals("Белый лист"))
//                                    Log.e("PhoneProfilesService.doForPackageReplaced", "oldData="+oldData);
                                if (oldData) {
                                    StringBuilder newContacts = new StringBuilder();
                                    for (String split : splits) {
                                        String[] splits2 = split.split("#");
                                        contactId = Long.parseLong(splits2[0]);
                                        long phoneId = Long.parseLong(splits2[1]);

                                        boolean found = false;
                                        for (Contact contact : contactList) {
                                            if (phoneId != 0) {
                                                if ((contact.contactId == contactId) && (contact.phoneId == phoneId))
                                                    found = true;
                                            } else {
                                                if (contact.contactId == contactId)
                                                    found = true;
                                            }
                                            if (found) {
                                                if (newContacts.length() > 0)
                                                    newContacts.append("|");
                                                newContacts
                                                        .append(contact.name)
                                                        .append(StringConstants.STR_SPLIT_CONTACTS_REGEX)
                                                        .append(contact.phoneNumber)
                                                        .append(StringConstants.STR_SPLIT_CONTACTS_REGEX)
                                                        .append(contact.accountType);
                                                break;
                                            }
                                        }
                                    }
                                    event._eventPreferencesCall._contacts = newContacts.toString();
                                    dataChanged = true;
                                }
                            }

                            if (!event._eventPreferencesSMS._contacts.isEmpty()) {
                                String[] splits = event._eventPreferencesSMS._contacts.split(StringConstants.STR_SPLIT_REGEX);
                                String _split = splits[0];
                                String[] _splits2 = _split.split("#");
                                boolean oldData = false;
                                try {
                                    //noinspection unused
                                    long l = Long.parseLong(_splits2[0]);
                                    oldData = true;
                                } catch (Exception ignored) {
                                }
                                if (oldData) {
                                    StringBuilder newContacts = new StringBuilder();
                                    for (String split : splits) {
                                        String[] splits2 = split.split("#");
                                        if (splits2.length != 3) {
                                            // old data
                                            splits2 = split.split("#");
                                            if (splits2.length != 2)
                                                continue;
                                            contactId = Long.parseLong(splits2[0]);
                                            long phoneId = Long.parseLong(splits2[1]);

                                            boolean found = false;
                                            for (Contact contact : contactList) {
                                                if (phoneId != 0) {
                                                    if ((contact.contactId == contactId) && (contact.phoneId == phoneId))
                                                        found = true;
                                                } else {
                                                    if (contact.contactId == contactId)
                                                        found = true;
                                                }
                                                if (found) {
                                                    if (newContacts.length() > 0)
                                                        newContacts.append("|");
                                                    newContacts
                                                            .append(contact.name)
                                                            .append(StringConstants.STR_SPLIT_CONTACTS_REGEX)
                                                            .append(contact.phoneNumber)
                                                            .append(StringConstants.STR_SPLIT_CONTACTS_REGEX)
                                                            .append(contact.accountType);
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                    event._eventPreferencesSMS._contacts = newContacts.toString();
                                    dataChanged = true;
                                }
                            }

                            if (!event._eventPreferencesNotification._contacts.isEmpty()) {
                                String[] splits = event._eventPreferencesNotification._contacts.split(StringConstants.STR_SPLIT_REGEX);
                                String _split = splits[0];
                                String[] _splits2 = _split.split("#");
                                boolean oldData = false;
                                try {
                                    //noinspection unused
                                    long l = Long.parseLong(_splits2[0]);
                                    oldData = true;
                                } catch (Exception ignored) {
                                }
                                if (oldData) {
                                    StringBuilder newContacts = new StringBuilder();
                                    for (String split : splits) {
                                        String[] splits2 = split.split("#");
                                        if (splits2.length != 3) {
                                            // old data
                                            splits2 = split.split("#");
                                            if (splits2.length != 2)
                                                continue;
                                            contactId = Long.parseLong(splits2[0]);
                                            long phoneId = Long.parseLong(splits2[1]);

                                            boolean found = false;
                                            for (Contact contact : contactList) {
                                                if (phoneId != 0) {
                                                    if ((contact.contactId == contactId) && (contact.phoneId == phoneId))
                                                        found = true;
                                                } else {
                                                    if (contact.contactId == contactId)
                                                        found = true;
                                                }
                                                if (found) {
                                                    if (newContacts.length() > 0)
                                                        newContacts.append("|");
                                                    newContacts
                                                            .append(contact.name)
                                                            .append(StringConstants.STR_SPLIT_CONTACTS_REGEX)
                                                            .append(contact.phoneNumber)
                                                            .append(StringConstants.STR_SPLIT_CONTACTS_REGEX)
                                                            .append(contact.accountType);
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                    event._eventPreferencesNotification._contacts = newContacts.toString();
                                    dataChanged = true;
                                }
                            }

                            if (!event._eventPreferencesCallScreening._contacts.isEmpty()) {
                                String[] splits = event._eventPreferencesCallScreening._contacts.split(StringConstants.STR_SPLIT_REGEX);
                                String _split = splits[0];
                                String[] _splits2 = _split.split("#");
                                boolean oldData = false;
                                try {
                                    //noinspection unused
                                    long l = Long.parseLong(_splits2[0]);
                                    oldData = true;
                                } catch (Exception ignored) {
                                }
                                if (oldData) {
                                    StringBuilder newContacts = new StringBuilder();
                                    for (String split : splits) {
                                        String[] splits2 = split.split("#");
                                        if (splits2.length != 3) {
                                            // old data
                                            splits2 = split.split("#");
                                            if (splits2.length != 2)
                                                continue;
                                            contactId = Long.parseLong(splits2[0]);
                                            long phoneId = Long.parseLong(splits2[1]);

                                            boolean found = false;
                                            for (Contact contact : contactList) {
                                                if (phoneId != 0) {
                                                    if ((contact.contactId == contactId) && (contact.phoneId == phoneId))
                                                        found = true;
                                                } else {
                                                    if (contact.contactId == contactId)
                                                        found = true;
                                                }
                                                if (found) {
                                                    if (newContacts.length() > 0)
                                                        newContacts.append("|");
                                                    newContacts
                                                            .append(contact.name)
                                                            .append(StringConstants.STR_SPLIT_CONTACTS_REGEX)
                                                            .append(contact.phoneNumber)
                                                            .append(StringConstants.STR_SPLIT_CONTACTS_REGEX)
                                                            .append(contact.accountType);
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                    event._eventPreferencesCallScreening._contacts = newContacts.toString();
                                    dataChanged = true;
                                }
                            }

                            if (dataChanged)
                                DatabaseHandler.getInstance(appContext).updateEvent(event);
                        }
                    }
                }

//                Log.e("ContactsCache.getContactList", "(2) xxxxxx");

                _contactList.clear();

                cached = true;
            }
        } catch (SecurityException e) {
            //Log.e("ContactsCache.getContactList", Log.getStackTraceString(e));
            //PPApplicationStatic.recordException(e);

            _contactList.clear();
            //_contactListWithoutNumber.clear();
//            PPApplicationStatic.logE("[SYNCHRONIZED] ContactsCache.getContactList", "(2) PPApplication.contactsCacheMutex");
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
//            PPApplicationStatic.logE("[SYNCHRONIZED] ContactsCache.getContactList", "(3) PPApplication.contactsCacheMutex");
            synchronized (PPApplication.contactsCacheMutex) {
                updateContacts(_contactList/*, false*/);
                //updateContacts(_contactListWithoutNumber, true);
            }

            cached = false;
        }

        //if (dataWrapper != null)
        //    dataWrapper.invalidateDataWrapper();

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
//        PPApplicationStatic.logE("[SYNCHRONIZED] ContactsCache.getList", "PPApplication.contactsCacheMutex");
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
//        PPApplicationStatic.logE("[SYNCHRONIZED] ContactsCache.clearCache", "PPApplication.contactsCacheMutex");
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

/*
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

            PPApplicationStatic.logE("[CONTACTS_CACHE] ContactsCache.covertOldContactToNewContact", "_contactId="+_contactId);
            PPApplicationStatic.logE("[CONTACTS_CACHE] ContactsCache.covertOldContactToNewContact", "_phoneId="+_phoneId);

            boolean foundInNew = false;
            boolean foundInOld = false;
            // search one contact from contactsInEvent.contacts
            for (Contact oldContact : _oldContactList) {
                if (_phoneId != 0) {
                    if ((oldContact.contactId == _contactId) && (oldContact.phoneId == _phoneId))
                        foundInOld = true;
                } else {
                    if (oldContact.contactId == _contactId)
                        foundInOld = true;
                }
                if (foundInOld) {
                    // found contact in old list
                    // search it in new list
                    for (Contact newContact : contactList) {
                        // search these fields in new contactList
                        if (newContact.name.equals(oldContact.name) &&
                                PhoneNumberUtils.compare(newContact.phoneNumber, oldContact.phoneNumber) &&
                                newContact.accountType.equals(oldContact.accountType)) {
                            foundInNew = true;
                            PPApplicationStatic.logE("[CONTACTS_CACHE] ContactsCache.covertOldContactToNewContact", "newContact.contactId="+newContact.contactId);
                            PPApplicationStatic.logE("[CONTACTS_CACHE] ContactsCache.covertOldContactToNewContact", "newContact.phoneId="+newContact.phoneId);
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
            PPApplicationStatic.logE("[CONTACTS_CACHE] ContactsCache.covertOldContactToNewContact", "foundInOld="+foundInOld);
            PPApplicationStatic.logE("[CONTACTS_CACHE] ContactsCache.covertOldContactToNewContact", "foundInNew="+foundInNew);
            //if (foundInOld) {
                // get back old contact
            //    if (newContacts.length() > 0)
            //        newContacts.append("|");
            //    newContacts.append(split);
            //}
        }

        return newContacts.toString();
    }
*/

    /*
    private static class ContactsComparator implements Comparator<Contact> {

        public int compare(Contact lhs, Contact rhs) {
            if (PPApplication.collator != null)
                return PPApplication.collator.compare(lhs.name, rhs.name);
            else
                return 0;
        }
    }
    */

    /*
    private static class ContactsInEvent {
        Event event = null;
        String contacts = null;
        //int sensorType = -1;
    }
    */

    static String getAccountName(String accountType, Context context) {
        String accountName = "";

        if (accountType.equals("com.osp.app.signin"))
            accountName = context.getString(R.string.contact_account_type_samsung_account);
        if (accountType.equals("com.google"))
            accountName = context.getString(R.string.contact_account_type_google_account);
        if (accountType.equals("vnd.sec.contact.sim"))
            accountName = context.getString(R.string.contact_account_type_sim_card);
        if (accountType.equals("vnd.sec.contact.sim2"))
            accountName = context.getString(R.string.contact_account_type_sim_card);
        if (accountType.equals("vnd.sec.contact.phone"))
            accountName = context.getString(R.string.contact_account_type_phone_application);
        if (accountType.equals("org.thoughtcrime.securesms"))
            accountName = "Signal";
        if (accountType.equals("com.google.android.apps.tachyon"))
            accountName = "Duo";
        if (accountType.equals("com.whatsapp"))
            accountName = "WhatsApp";

        return accountName;
    }

}
