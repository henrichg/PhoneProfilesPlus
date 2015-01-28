package sk.henrichg.phoneprofilesplus;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;

public class ContactsCache {

	private ArrayList<Contact> contactList;
	private boolean cached;
	private boolean cancelled;
	
	public ContactsCache()
	{
		contactList = new ArrayList<Contact>();
		cached = false;
	}
	
	public void getContactList(Context context)
	{
		if (cached) return;
		
		cancelled = false;
		
		contactList.clear();
		
		String[] projection = new String[] { ContactsContract.Contacts.HAS_PHONE_NUMBER, 
				 ContactsContract.Contacts._ID, 
				 ContactsContract.Contacts.DISPLAY_NAME,
			     ContactsContract.Contacts.PHOTO_ID };
		String selection = ContactsContract.Contacts.HAS_PHONE_NUMBER + "='1'";
		String order = ContactsContract.Contacts.DISPLAY_NAME + " ASC";
		
		Cursor mCursor = context.getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, projection, selection, null, order);
		
		while (mCursor.moveToNext()) 
		{
			//try{
				long contactId = mCursor.getLong(mCursor.getColumnIndex(ContactsContract.Contacts._ID)); 
				String name = mCursor.getString(mCursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
				//String hasPhone = mCursor.getString(mCursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));
				String photoId = mCursor.getString(mCursor.getColumnIndex(ContactsContract.Contacts.PHOTO_ID));
				if (Integer.parseInt(mCursor.getString(mCursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) 
				{
					Cursor phones = context.getContentResolver().query( ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = "+ contactId, null, null);
					while (phones.moveToNext()) 
					{ 
						long phoneId = phones.getLong(phones.getColumnIndex( ContactsContract.CommonDataKinds.Phone._ID));
						String phoneNumber = phones.getString(phones.getColumnIndex( ContactsContract.CommonDataKinds.Phone.NUMBER));
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
						
						if (cancelled)
							break;
					} 
					phones.close(); 
				}
			//}catch(Exception e){}
				
			if (cancelled)
				break;
		}
		mCursor.close();
		
		if(cancelled)
			return;
		
		cached = true;
	}
	
	public int getLength()
	{
		if (cached) 
			return contactList.size();
		else
			return 0;
	}

	public List<Contact> getList()
	{
		if (cached) 
			return contactList;
		else
			return null;
	}
	
	public Contact getContact(int position)
	{
		if (cached) 
			return contactList.get(position);
		else
			return null;
	}
	
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

	public void clearCache(boolean nullList)
	{
		contactList.clear();
		if (nullList)
			contactList = null;
		cached = false;
	}
	
	public boolean isCached()
	{
		return cached;
	}
	
	public void cancelCaching()
	{
		cancelled = true;
	}
	
}
