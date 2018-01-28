package sk.henrichg.phoneprofilesplus;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.l4digital.fastscroll.FastScroller;

class ContactsMultiSelectPreferenceAdapter extends RecyclerView.Adapter<ContactsMultiSelectDialogPreferenceViewHolder>
                                                implements ItemTouchHelperAdapter, FastScroller.SectionIndexer
{
    private final ContactsMultiSelectDialogPreference preference;

    ContactsMultiSelectPreferenceAdapter(ContactsMultiSelectDialogPreference preference)
    {
        this.preference = preference;
    }

    @Override
    public ContactsMultiSelectDialogPreferenceViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.contacts_multiselect_preference_list_item, parent, false);
        return new ContactsMultiSelectDialogPreferenceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ContactsMultiSelectDialogPreferenceViewHolder holder, int position) {
        // Contact to display
        Contact contact = preference.contactList.get(position);
        //System.out.println(String.valueOf(position));

        holder.bindContact(contact);
    }

    @Override
    public String getSectionText(int position) {
        Contact contact = preference.contactList.get(position);
        if (contact.checked)
            return "*";
        else
            return contact.name.substring(0, 1);
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        return false;
    }

    @Override
    public void onItemDismiss(int position) {

    }

    public Object getItem(int position) {
        return EditorProfilesActivity.getContactsCache().getContact(position);
    }

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
