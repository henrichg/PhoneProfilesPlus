package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
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
    private final TextView textViewAccountType;

    private Contact contact;

    private final Context context;

    ContactsMultiSelectDialogPreferenceViewHolderX(View itemView, Context context)
    {
        super(itemView);

        this.context = context;

        imageViewPhoto = itemView.findViewById(R.id.contacts_multiselect_pref_dlg_item_icon);
        textViewDisplayName = itemView.findViewById(R.id.contacts_multiselect_pref_dlg_item_display_name);
        textViewPhoneNumber = itemView.findViewById(R.id.contacts_multiselect_pref_dlg_item_phone_number);
        checkBox = itemView.findViewById(R.id.contacts_multiselect_pref_dlg_item_checkbox);
        textViewAccountType = itemView.findViewById(R.id.contacts_multiselect_pref_dlg_item_account_type);

        // If CheckBox is toggled, update the Contact it is tagged with.
        checkBox.setOnClickListener(v -> {
            CheckBox cb = (CheckBox) v;
            Contact contact = (Contact) cb.getTag();
            contact.checked = cb.isChecked();
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
        if (contact.phoneId != 0) {
            textViewPhoneNumber.setVisibility(View.VISIBLE);
            textViewPhoneNumber.setText(contact.phoneNumber);
            textViewAccountType.setVisibility(View.VISIBLE);

            boolean found = false;
            PackageManager packageManager = context.getPackageManager();
            try {
                ApplicationInfo applicationInfo = packageManager.getApplicationInfo(contact.accountType, 0);
                if (applicationInfo != null) {
                    contact.accountType = packageManager.getApplicationLabel(applicationInfo).toString();
                    found = true;
                }
            } catch (Exception ignored) {}
//            Log.e("ContactsMultiSelectDialogPreferenceViewHolderX.bindContact", "found="+found);
            if (!found) {
                if (contact.accountType.equals("com.osp.app.signin"))
                    contact.accountType = context.getString(R.string.contact_account_type_samsung_account);
                if (contact.accountType.equals("com.google"))
                    contact.accountType = context.getString(R.string.contact_account_type_google_account);
                if (contact.accountType.equals("vnd.sec.contact.sim"))
                    contact.accountType = context.getString(R.string.contact_account_type_sim_card);
                if (contact.accountType.equals("vnd.sec.contact.phone"))
                    contact.accountType = context.getString(R.string.contact_account_type_phone_application);
                if (contact.accountType.equals("org.thoughtcrime.securesms"))
                    contact.accountType = "Signal";
                if (contact.accountType.equals("com.google.android.apps.tachyon"))
                    contact.accountType = "Duo";
                if (contact.accountType.equals("com.whatsapp"))
                    contact.accountType = "WhatsApp";
            }
            textViewAccountType.setText(contact.accountType);
        }
        else {
            textViewPhoneNumber.setVisibility(View.GONE);
            textViewPhoneNumber.setText(R.string.empty_string);
            textViewAccountType.setVisibility(View.GONE);
            textViewAccountType.setText(R.string.empty_string);
        }

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
