package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

class ContactsMultiSelectPreferenceAdapterX extends RecyclerView.Adapter<ContactsMultiSelectDialogPreferenceViewHolderX>
                                                implements FastScrollRecyclerView.SectionedAdapter
{
    private final Context context;

    private final ContactsMultiSelectDialogPreferenceX preference;

    ContactsMultiSelectPreferenceAdapterX(Context context, ContactsMultiSelectDialogPreferenceX preference)
    {
        this.context = context;
        this.preference = preference;
    }

    @NonNull
    @Override
    public ContactsMultiSelectDialogPreferenceViewHolderX onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.contacts_multiselect_preference_list_item, parent, false);
        return new ContactsMultiSelectDialogPreferenceViewHolderX(view, context);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactsMultiSelectDialogPreferenceViewHolderX holder, int position) {
        // Contact to display
        Contact contact = preference.contactList.get(position);
        //System.out.println(String.valueOf(position));

        holder.bindContact(contact);
    }

    @NonNull
    @Override
    public String getSectionName(int position) {
        Contact contact = preference.contactList.get(position);
        if (contact.checked)
            return "*";
        else
            return contact.name.substring(0, 1);
    }

    /*
    public Object getItem(int position) {
        return EditorProfilesActivity.getContactsCache().getContact(position);
    }
    */

    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        if (preference.contactList == null)
            return 0;
        else
            return preference.contactList.size();
    }

}
