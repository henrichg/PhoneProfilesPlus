package sk.henrichg.phoneprofilesplus;

import android.content.ContentUris;
import android.content.Context;
import android.net.Uri;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

public class ContactsMultiselectPreferenceAdapter extends BaseAdapter 
{
    private LayoutInflater inflater;
    //private Context context;

    public ContactsMultiselectPreferenceAdapter(Context context) 
    {
        // Cache the LayoutInflate to avoid asking for a new one each time.
        inflater = LayoutInflater.from(context);
        //this.context = context; 
    }

	public int getCount() {
		return EditorProfilesActivity.getContactsCache().getLength();
	}

	public Object getItem(int position) {
		return EditorProfilesActivity.getContactsCache().getContact(position);
	}

	public long getItemId(int position) {
		return position;
	}
    
    public View getView(int position, View convertView, ViewGroup parent)
    {
		ContactsCache contactsCahce = EditorProfilesActivity.getContactsCache();
    	
        // Contact to display
        Contact contact = contactsCahce.getContact(position);
        //System.out.println(String.valueOf(position));

        // The child views in each row.
        ImageView imageViewPhoto;
        TextView textViewDisplayName;
        TextView textViewPhoneNumber;
        CheckBox checkBox;

        // Create a new row view
        if (convertView == null)
        {
            convertView = inflater.inflate(R.layout.contacts_multiselect_preference_list_item, parent, false);

            // Find the child views.
            imageViewPhoto = (ImageView) convertView.findViewById(R.id.contacts_multiselect_pref_dlg_item_icon);
            textViewDisplayName = (TextView) convertView.findViewById(R.id.contacts_multiselect_pref_dlg_item_display_name);
            textViewPhoneNumber = (TextView) convertView.findViewById(R.id.contacts_multiselect_pref_dlg_item_phone_number);
            checkBox = (CheckBox) convertView.findViewById(R.id.contacts_multiselect_pref_dlg_item_checkbox);

            // Optimization: Tag the row with it's child views, so we don't
            // have to
            // call findViewById() later when we reuse the row.
            convertView.setTag(new ContactViewHolder(imageViewPhoto, textViewDisplayName, textViewPhoneNumber, checkBox));

            // If CheckBox is toggled, update the Contact it is tagged with.
            checkBox.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    CheckBox cb = (CheckBox) v;
                    Contact contact = (Contact) cb.getTag();
                    contact.checked = cb.isChecked();
                }
            });
        }
        // Reuse existing row view
        else
        {
            // Because we use a ViewHolder, we avoid having to call
            // findViewById().
            ContactViewHolder viewHolder = (ContactViewHolder) convertView.getTag();
            imageViewPhoto = viewHolder.imageViewPhoto;
            textViewDisplayName = viewHolder.textViewDisplayName;
            textViewPhoneNumber = viewHolder.textViewPhoneNumber;
            checkBox = viewHolder.checkBox;
        }

        // Tag the CheckBox with the Contact it is displaying, so that we
        // can
        // access the Contact in onClick() when the CheckBox is toggled.
        checkBox.setTag(contact);

        // Display Contact data
        if (contact.photoId != 0)
        	imageViewPhoto.setImageURI(getPhotoUri(contact.contactId));
        else
        	imageViewPhoto.setImageResource(R.drawable.ic_contacts_multiselect_dialog_preference_no_photo);
        textViewDisplayName.setText(contact.name);
        textViewPhoneNumber.setText(contact.phoneNumber);
        
        checkBox.setChecked(contact.checked);

        return convertView;
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
	        e.printStackTrace();
	        return null;
	    }
	    */
	    Uri person = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId);
	    return Uri.withAppendedPath(person, ContactsContract.Contacts.Photo.CONTENT_DIRECTORY);
	}

}
