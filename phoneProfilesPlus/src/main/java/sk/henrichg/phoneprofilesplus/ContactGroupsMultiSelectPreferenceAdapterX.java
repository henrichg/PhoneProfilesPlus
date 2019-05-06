package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

class ContactGroupsMultiSelectPreferenceAdapterX extends BaseAdapter
{
    private final LayoutInflater inflater;
    //private Context context;

    ContactGroupsMultiSelectPreferenceAdapterX(Context context)
    {
        // Cache the LayoutInflate to avoid asking for a new one each time.
        inflater = LayoutInflater.from(context);
        //this.context = context; 
    }

    public int getCount() {
        return EditorProfilesActivity.getContactGroupsCache().getLength();
    }

    public Object getItem(int position) {
        return EditorProfilesActivity.getContactGroupsCache().getContactGroup(position);
    }

    public long getItemId(int position) {
        return position;
    }
    
    @SuppressLint("SetTextI18n")
    public View getView(int position, View convertView, ViewGroup parent)
    {
        ContactGroupsCache contactGroupsCache = EditorProfilesActivity.getContactGroupsCache();

        // Contact group to display
        ContactGroup contactGroup = contactGroupsCache.getContactGroup(position);
        //System.out.println(String.valueOf(position));

        // The child views in each row.
        TextView textViewDisplayName;
        CheckBox checkBox;

        // Create a new row view
        if (convertView == null)
        {
            convertView = inflater.inflate(R.layout.contact_groups_multiselect_preference_list_item, parent, false);

            // Find the child views.
            textViewDisplayName = convertView.findViewById(R.id.contact_groups_multiselect_pref_dlg_item_display_name);
            checkBox = convertView.findViewById(R.id.contact_groups_multiselect_pref_dlg_item_checkbox);

            // Optimization: Tag the row with it's child views, so we don't
            // have to
            // call findViewById() later when we reuse the row.
            convertView.setTag(new ContactGroupViewHolder(textViewDisplayName, checkBox));

            // If CheckBox is toggled, update the ContactGroup it is tagged with.
            checkBox.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    CheckBox cb = (CheckBox) v;
                    ContactGroup contactGroup = (ContactGroup) cb.getTag();
                    contactGroup.checked = cb.isChecked();
                }
            });
        }
        // Reuse existing row view
        else
        {
            // Because we use a ViewHolder, we avoid having to call
            // findViewById().
            ContactGroupViewHolder viewHolder = (ContactGroupViewHolder) convertView.getTag();
            textViewDisplayName = viewHolder.textViewDisplayName;
            checkBox = viewHolder.checkBox;
        }

        // Tag the CheckBox with the ContactGroup it is displaying, so that we
        // can
        // access the ContactGroup in onClick() when the CheckBox is toggled.
        checkBox.setTag(contactGroup);

        // Display ContactGroup data
        textViewDisplayName.setText(contactGroup.name + " (" + contactGroup.count + ")");

        checkBox.setChecked(contactGroup.checked);

        return convertView;
    }

}
