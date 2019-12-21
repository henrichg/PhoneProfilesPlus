package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

class ContactsMultiSelectDialogPreferenceViewHolderX extends RecyclerView.ViewHolder implements View.OnClickListener {

    private final ImageView imageViewPhoto;
    private final TextView textViewDisplayName;
    private final TextView textViewPhoneNumber;
    private final CheckBox checkBox;

    private Contact contact;

    ContactsMultiSelectDialogPreferenceViewHolderX(View itemView)
    {
        super(itemView);

        imageViewPhoto = itemView.findViewById(R.id.contacts_multiselect_pref_dlg_item_icon);
        textViewDisplayName = itemView.findViewById(R.id.contacts_multiselect_pref_dlg_item_display_name);
        textViewPhoneNumber = itemView.findViewById(R.id.contacts_multiselect_pref_dlg_item_phone_number);
        checkBox = itemView.findViewById(R.id.contacts_multiselect_pref_dlg_item_checkbox);

        // If CheckBox is toggled, update the Contact it is tagged with.
        checkBox.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                CheckBox cb = (CheckBox) v;
                Contact contact = (Contact) cb.getTag();
                contact.checked = cb.isChecked();
            }
        });

        itemView.setOnClickListener(this);
    }

    @SuppressLint("SetTextI18n")
    void bindContact(Contact contact) {
        this.contact = contact;

        // Display Contact data
        if (contact.photoId != 0)
            imageViewPhoto.setImageURI(getPhotoUri(contact.contactId));
        else
            imageViewPhoto.setImageResource(R.drawable.ic_contacts_multiselect_dialog_preference_no_photo);
        textViewDisplayName.setText(contact.name);
        if (contact.phoneId != 0)
            textViewPhoneNumber.setText(contact.phoneNumber);
        else
            textViewPhoneNumber.setText(R.string.empty_string);

        // Tag the CheckBox with the Contact it is displaying, so that we
        // can
        // access the Contact in onClick() when the CheckBox is toggled.
        checkBox.setTag(contact);

        checkBox.setChecked(contact.checked);
    }

    @Override
    public void onClick(View v) {
        contact.toggleChecked();
        checkBox.setChecked(contact.checked);
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
        Uri person = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId);
        return Uri.withAppendedPath(person, ContactsContract.Contacts.Photo.CONTENT_DIRECTORY);
    }

}
