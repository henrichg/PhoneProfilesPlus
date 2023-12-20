package sk.henrichg.phoneprofilesplus;

import android.view.View;
import android.widget.CheckBox;
//import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.imageview.ShapeableImageView;

class ContactsMultiSelectDialogPreferenceViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    private final boolean withountNumbers;

    private final ShapeableImageView imageViewPhoto;
    private final TextView textViewDisplayName;
    private final TextView textViewPhoneNumber;
    private final CheckBox checkBox;
    private final TextView textViewAccountType;

    private Contact contact;

    ContactsMultiSelectDialogPreferenceViewHolder(View itemView, boolean withoutNumbers)
    {
        super(itemView);

        this.withountNumbers = withoutNumbers;
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
//            PPApplicationStatic.logE("[CONTACTS_DIALOG] ContactsMultiSelectDialogPreferenceViewHolder.onClick", "checkbox click - contact.contactId="+contact.contactId);
//            PPApplicationStatic.logE("[CONTACTS_DIALOG] ContactsMultiSelectDialogPreferenceViewHolder.onClick", "checkbox click - contact.name="+contact.name);
//            PPApplicationStatic.logE("[CONTACTS_DIALOG] ContactsMultiSelectDialogPreferenceViewHolder.onClick", "checkbox click - contact.checked="+contact.checked);
        });

        itemView.setOnClickListener(this);
    }

    void bindContact(Contact contact) {
        this.contact = contact;

        // Display Contact data
        if (contact.photoId != 0)
            imageViewPhoto.setImageURI(contact.photoUri);
        else
            imageViewPhoto.setImageResource(R.drawable.ic_contacts_multiselect_dialog_preference_no_photo);
        textViewDisplayName.setText(contact.name);

        if ((contact.phoneId != 0) && (!withountNumbers)) {
            textViewPhoneNumber.setVisibility(View.VISIBLE);
            textViewPhoneNumber.setText(contact.phoneNumber);
        }
        else {
            textViewPhoneNumber.setVisibility(View.GONE);
            textViewPhoneNumber.setText("");
        }

        textViewAccountType.setText(contact.displayedAccountType);

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
//        PPApplicationStatic.logE("[CONTACTS_DIALOG] ContactsMultiSelectDialogPreferenceViewHolder.onClick", "item click - contact.contactId="+contact.contactId);
//        PPApplicationStatic.logE("[CONTACTS_DIALOG] ContactsMultiSelectDialogPreferenceViewHolder.onClick", "item click - contact.name="+contact.name);
//        PPApplicationStatic.logE("[CONTACTS_DIALOG] ContactsMultiSelectDialogPreferenceViewHolder.onClick", "item click - contact.checked="+contact.checked);
    }

}
