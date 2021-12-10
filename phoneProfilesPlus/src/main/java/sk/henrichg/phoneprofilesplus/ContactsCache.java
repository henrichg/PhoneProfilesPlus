package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;

import java.util.ArrayList;
import java.util.Collections;
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

    @SuppressLint("Range")
    void getContactList(Context context)
    {
        if (cached || caching) return;

//        PPApplication.logE("[TEST BATTERY] ContactsCache.getContactList", "---- START");

        caching = true;

        ArrayList<Contact> _contactList = new ArrayList<>();

        try {
            if (Permissions.checkContacts(context)) {

                long contactId = 0;
                String name = null;
                String photoId = "0";
                //int hasPhone = 0;

                String[] projection = new String[]{
                        ContactsContract.RawContacts.CONTACT_ID,
                        ContactsContract.RawContacts.ACCOUNT_TYPE
                };
                Cursor rawCursor = context.getContentResolver().query(ContactsContract.RawContacts.CONTENT_URI, projection, null /*selection*/, null, ContactsContract.RawContacts.CONTACT_ID + " ASC");
                if (rawCursor != null) {
                    while (rawCursor.moveToNext()) {
                        long _contactId = rawCursor.getLong(0);
                        String rawAccountType = rawCursor.getString(1);

//                        PPApplication.logE("------- ContactsCache.getContactList", "_contactId=" + _contactId);
//                        PPApplication.logE("------- ContactsCache.getContactList", "contactId=" + contactId);
//                        PPApplication.logE("------- ContactsCache.getContactList", "rawAccountType=" + rawAccountType);

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
                                    name = mCursor.getString(mCursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                                    photoId = mCursor.getString(mCursor.getColumnIndex(ContactsContract.Contacts.PHOTO_ID));
                                    //hasPhone = Integer.parseInt(mCursor.getString(mCursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)));
                                }
                                else
                                    name = null;
                                mCursor.close();
                            }
                            else
                                name = null;
                        }

                        if (name != null) {
                            //if (hasPhone > 0) {
                                projection = new String[]{
                                        ContactsContract.CommonDataKinds.Phone._ID,
                                        ContactsContract.CommonDataKinds.Phone.NUMBER,
                                        ContactsContract.CommonDataKinds.Phone.ACCOUNT_TYPE_AND_DATA_SET
                                };
                                Cursor phones = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, projection,
                                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=" + contactId + " AND " +
                                        ContactsContract.CommonDataKinds.Phone.ACCOUNT_TYPE_AND_DATA_SET + "='" + rawAccountType + "'",
                                        null, null);
                                if (phones != null) {
                                    if (phones.getCount() > 0) {
                                        while (phones.moveToNext()) {
                                            String accountType = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.ACCOUNT_TYPE_AND_DATA_SET));

                                            //if (accountType.equals(rawAccountType)) {
                                            long phoneId = phones.getLong(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone._ID));
                                            String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

//                                            PPApplication.logE("------- ContactsCache.getContactList", "aContact.name=" + name);
//                                            PPApplication.logE("------- ContactsCache.getContactList", "aContact.phoneNumber=" + phoneNumber);
//                                            PPApplication.logE("------- ContactsCache.getContactList", "aContact.accountType=" + accountType);

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

                //noinspection Java8ListSort
                Collections.sort(_contactList, new ContactsComparator());

                cached = true;
            }
        } catch (SecurityException e) {
            //Log.e("ContactsCache.getContactList", Log.getStackTraceString(e));
            //PPApplication.recordException(e);

            _contactList.clear();
            //_contactListWithoutNumber.clear();

            cached = false;
        } catch (Exception e) {
            //Log.e("ContactsCache.getContactList", Log.getStackTraceString(e));
            PPApplication.recordException(e);

            _contactList.clear();
            //_contactListWithoutNumber.clear();

            cached = false;
        }

        //if (cached) {
            synchronized (PPApplication.contactsCacheMutex) {
                updateContacts(_contactList/*, false*/);
                //updateContacts(_contactListWithoutNumber, true);
            }
        //}

//        PPApplication.logE("[TEST BATTERY] ContactsCache.getContactList", "---- END");

        caching = false;
    }
/*
    void getContactList(Context context)
    {
        if (cached || caching) return;

//        PPApplication.logE("[TEST BATTERY] ContactsCache.getContactList", "---- START");

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
                        long contactId = mCursor.getLong(mCursor.getColumnIndex(ContactsContract.Contacts._ID));
                        String name = mCursor.getString(mCursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                        if (name != null) {
//                            String hasPhone = mCursor.getString(mCursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));
//                            PPApplication.logE("------- ContactsCache.getContactList", "hasPhone=" + hasPhone);
                            String photoId = mCursor.getString(mCursor.getColumnIndex(ContactsContract.Contacts.PHOTO_ID));
                            if (Integer.parseInt(mCursor.getString(mCursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                                projection = new String[]{
                                        ContactsContract.CommonDataKinds.Phone._ID,
                                        ContactsContract.CommonDataKinds.Phone.NUMBER,
                                        ContactsContract.CommonDataKinds.Phone.ACCOUNT_TYPE_AND_DATA_SET
                                };
                                Cursor phones = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, projection, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + contactId, null, null);
                                if (phones != null) {
                                    while (phones.moveToNext()) {
                                        long phoneId = phones.getLong(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone._ID));
                                        String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                                        String accountType = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.ACCOUNT_TYPE_AND_DATA_SET));

//                                        PPApplication.logE("------- ContactsCache.getContactList", "aContact.accountType=" + accountType);

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
            //PPApplication.recordException(e);

            _contactList.clear();
            //_contactListWithoutNumber.clear();

            cached = false;
        } catch (Exception e) {
            Log.e("ContactsCache.getContactList", Log.getStackTraceString(e));
            PPApplication.recordException(e);

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

        //PPApplication.logE("[TEST BATTERY] ContactsCache.getContactList", "---- END");

        caching = false;
    }
*/
/*
    void getContactListX(Context context)
    {
        if (cached || caching) return;

//        PPApplication.logE("[TEST BATTERY] ContactsCache.getContactList", "---- START");

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
//                        PPApplication.logE("[TEST BATTERY] ContactsCache.getContactList", "(1)");

//                        PPApplication.logE("------- ContactsCache.getContactListX", "accountType=" + mCursor.getString(mCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.ACCOUNT_TYPE_AND_DATA_SET)));

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

//                PPApplication.logE("[TEST BATTERY] ContactsCache.getContactList", "(2)");

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
//                        PPApplication.logE("[TEST BATTERY] ContactsCache.getContactList", "(3)");

                        long contactId = mCursor.getLong(0);
                        String name = mCursor.getString(1);
                        String photo = mCursor.getString(2);
                        List<String> contactPhones = phones.get(contactId);
                        if (contactPhones != null) {
                            for (String phone : contactPhones) {
                                String[] splits = phone.split("\\|");
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

//                PPApplication.logE("[TEST BATTERY] ContactsCache.getContactList", "(4)");
//                PPApplication.logE("ContactsCache.getContactListX", "_contactList.size()="+_contactList.size());

                //if (cancelled)
                //    return;

                Collections.sort(_contactList, new ContactsComparator());

                cached = true;
            }
        } catch (SecurityException e) {
            //Log.e("ContactsCache.getContactList", Log.getStackTraceString(e));
            //PPApplication.recordException(e);

            _contactList.clear();
            //_contactListWithoutNumber.clear();

            cached = false;
        } catch (Exception e) {
            //Log.e("ContactsCache.getContactList", Log.getStackTraceString(e));
            PPApplication.recordException(e);

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

//        PPApplication.logE("[TEST BATTERY] ContactsCache.getContactList", "---- END");

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
        if (cached) {
            /*if (withoutNumber)
                return contactListWithoutNumber;
            else*/
                return contactList;
        }
        else
            return null;
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

    private static class ContactsComparator implements Comparator<Contact> {

        public int compare(Contact lhs, Contact rhs) {
            if (PPApplication.collator != null)
                return PPApplication.collator.compare(lhs.name, rhs.name);
            else
                return 0;
        }
    }

}
