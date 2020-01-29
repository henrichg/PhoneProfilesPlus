package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;

import java.util.ArrayList;
import java.util.List;

class ContactsCache {

    private final ArrayList<Contact> contactList;
    private final ArrayList<Contact> contactListWithoutNumber;
    private boolean cached;
    private boolean caching;
    //private boolean cancelled;

    ContactsCache()
    {
        contactList = new ArrayList<>();
        contactListWithoutNumber = new ArrayList<>();
        cached = false;
        caching = false;
    }

    void getContactList(Context context)
    {
        if (cached || caching) return;

        caching = true;
        //cancelled = false;

        contactList.clear();
        contactListWithoutNumber.clear();

        if (Permissions.checkContacts(context)) {
            String[] projection = new String[]{ContactsContract.Contacts.HAS_PHONE_NUMBER,
                    ContactsContract.Contacts._ID,
                    ContactsContract.Contacts.DISPLAY_NAME,
                    ContactsContract.Contacts.PHOTO_ID};
            String selection = ContactsContract.Contacts.HAS_PHONE_NUMBER + "='1'";
            String order = ContactsContract.Contacts.DISPLAY_NAME + " ASC";

            Cursor mCursor = context.getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, projection, selection, null, order);

            if (mCursor != null) {
                while (mCursor.moveToNext()) {
                    //try{
                    long contactId = mCursor.getLong(mCursor.getColumnIndex(ContactsContract.Contacts._ID));
                    String name = mCursor.getString(mCursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                    //String hasPhone = mCursor.getString(mCursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));
                    String photoId = mCursor.getString(mCursor.getColumnIndex(ContactsContract.Contacts.PHOTO_ID));
                    if (Integer.parseInt(mCursor.getString(mCursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                        Cursor phones = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + contactId, null, null);
                        if (phones != null) {
                            while (phones.moveToNext()) {
                                long phoneId = phones.getLong(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone._ID));
                                String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
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
                                contactList.add(aContact);

                                //if (cancelled)
                                //    break;
                            }
                            phones.close();
                        }
                    }
                    Contact aContact = new Contact();
                    aContact.contactId = contactId;
                    aContact.name = name;
                    aContact.phoneId = 0;
                    aContact.phoneNumber = "";
                    try {
                        aContact.photoId = Long.parseLong(photoId);
                    } catch (Exception e) {
                        aContact.photoId = 0;
                    }
                    contactListWithoutNumber.add(aContact);

                    //}catch(Exception e){}

                    //if (cancelled)
                    //    break;
                }
                mCursor.close();
            }

            //if (cancelled)
            //    return;

            cached = true;
        }

        caching = false;
    }

    /*
    public int getLength()
    {
        if (cached)
            return contactList.size();
        else
            return 0;
    }
    */

    List<Contact> getList(boolean withoutNumber)
    {
        if (cached) {
            if (withoutNumber)
                return contactListWithoutNumber;
            else
                return contactList;
        }
        else
            return null;
    }

    /*
    Contact getContact(int position)
    {
        if (cached)
            return contactList.get(position);
        else
            return null;
    }
    */

    /*
    public long getContactId(int position)
    {
        if (cached)
            return contactList.get(position).contactId;
        else
            return 0;
    }

    public String getContactDisplayName(int position)
    {
        if (cached)
            return contactList.get(position).name;
        else
            return "";
    }

    public long getPhoneId(int position)
    {
        if (cached)
            return contactList.get(position).phoneId;
        else
            return 0;
    }

    public String getContactPhoneNumber(int position)
    {
        if (cached)
            return contactList.get(position).phoneNumber;
        else
            return "";
    }

    public long getContactPhotoId(int position)
    {
        if (cached)
            return contactList.get(position).photoId;
        else
            return 0;
    }
    */

    void clearGroups() {
        for (Contact contact : contactList) {
            if (contact.groups != null) {
                contact.groups.clear();
                contact.groups = null;
            }
        }
        for (Contact contact : contactListWithoutNumber) {
            if (contact.groups != null) {
                contact.groups.clear();
                contact.groups = null;
            }
        }
    }

    void addGroup(long contactId, long contactGroupId, boolean withoutNumbers) {
        if (withoutNumbers) {
            for (Contact contact : contactListWithoutNumber) {
                boolean contactFound = false;

                if (contact.contactId == contactId) {
                    contactFound = true;

                    if (contact.groups == null)
                        contact.groups = new ArrayList<>();

                    // search group in contact
                    boolean groupFound = false;
                    for (long groupId : contact.groups) {
                        if (groupId == contactGroupId) {
                            groupFound = true;
                            break;
                        }
                    }
                    if (!groupFound)
                        // group not found, add it
                        contact.groups.add(contactGroupId);
                }

                if (contactFound)
                    break;
            }
        }
        else {
            /*if ((contactGroupId == 1) || (contactGroupId == 15) || (contactGroupId == 20)) {
                Log.e("ContactsCache.addGroup", "contactGroupId=" + contactGroupId);
                Log.e("ContactsCache.addGroup", "contactId=" + contactId);
            }*/
            for (Contact contact : contactList) {
                boolean contactFound = false;

                if (contact.contactId == contactId) {
                    contactFound = true;
                    /*if ((contactGroupId == 1) || (contactGroupId == 15) || (contactGroupId == 20)) {
                        Log.e("ContactsCache.addGroup", "contact found");
                        Log.e("ContactsCache.addGroup", "contact.phoneNumber="+contact.phoneNumber);
                    }*/

                    if (contact.groups == null)
                        contact.groups = new ArrayList<>();

                    // search group in contact
                    boolean groupFound = false;
                    for (long groupId : contact.groups) {
                        if (groupId == contactGroupId) {
                            groupFound = true;
                            /*if ((contactGroupId == 1) || (contactGroupId == 15) || (contactGroupId == 20)) {
                                Log.e("ContactsCache.addGroup", "group found");
                            }*/
                            break;
                        }
                    }
                    if (!groupFound) {
                        // group not found, add it
                        contact.groups.add(contactGroupId);
                        /*if ((contactGroupId == 1) || (contactGroupId == 15) || (contactGroupId == 20)) {
                            Log.e("ContactsCache.addGroup", "group added");
                            Log.e("ContactsCache.addGroup", "contact.groups.size()="+contact.groups.size());
                        }*/
                    }
                }

                if (contactFound)
                    break;
            }
        }
    }

    void clearCache(/*boolean nullList*/)
    {
        contactList.clear();
        contactListWithoutNumber.clear();
        /*if (nullList) {
            contactList = null;
            contactListWithoutNumber = null;
        }*/
        cached = false;
        caching = false;
    }

    boolean getCaching() {
        return caching;
    }

    /*
    void cancelCaching()
    {
        cancelled = true;
    }
    */
}
