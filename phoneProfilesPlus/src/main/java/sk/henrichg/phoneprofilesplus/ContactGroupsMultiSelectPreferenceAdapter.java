package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

class ContactGroupsMultiSelectPreferenceAdapter extends BaseAdapter
{
    private final LayoutInflater inflater;
    private final Context context;
    private final ContactGroupsMultiSelectDialogPreference preference;

    ContactGroupsMultiSelectPreferenceAdapter(Context context, ContactGroupsMultiSelectDialogPreference preference)
    {
        // Cache the LayoutInflate to avoid asking for a new one each time.
        inflater = LayoutInflater.from(context);
        this.context = context;
        this.preference = preference;
    }

    public int getCount() {
        if (preference.contactGroupList != null)
            return preference.contactGroupList.size();
        else
            return 0;
    }

    public Object getItem(int position) {
        if (preference.contactGroupList != null)
            return preference.contactGroupList.get(position);
        else
            return null;
    }

    public long getItemId(int position) {
        return position;
    }
    
    @SuppressLint("SetTextI18n")
    public View getView(int position, View convertView, ViewGroup parent)
    {
        // The child views in each row.
        TextView textViewDisplayName;
        CheckBox checkBox;
        TextView textViewAccountType;

        // Create a new row view
        if (convertView == null)
        {
            convertView = inflater.inflate(R.layout.listitem_contact_groups_multiselect_preference, parent, false);

            // Find the child views.
            textViewDisplayName = convertView.findViewById(R.id.contact_groups_multiselect_pref_dlg_item_display_name);
            checkBox = convertView.findViewById(R.id.contact_groups_multiselect_pref_dlg_item_checkbox);
            textViewAccountType = convertView.findViewById(R.id.contact_groups_multiselect_pref_dlg_item_account_type);

            // Optimization: Tag the row with it's child views, so we don't
            // have to
            // call findViewById() later when we reuse the row.
            convertView.setTag(new ContactGroupViewHolder(textViewDisplayName, checkBox, textViewAccountType));

            // If CheckBox is toggled, update the ContactGroup it is tagged with.
            //noinspection DataFlowIssue
            checkBox.setOnClickListener(v -> {
                CheckBox cb = (CheckBox) v;
                ContactGroup contactGroup = (ContactGroup) cb.getTag();
                if (contactGroup != null)
                    contactGroup.checked = cb.isChecked();
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
            textViewAccountType = viewHolder.textViewAccountType;
        }

        // Contact group to display
        ContactGroup contactGroup = null;
        if (preference.contactGroupList != null)
            contactGroup = preference.contactGroupList.get(position);

        if (contactGroup != null) {
            // Tag the CheckBox with the ContactGroup it is displaying, so that we
            // can
            // access the ContactGroup in onClick() when the CheckBox is toggled.
            checkBox.setTag(contactGroup);

            // Display ContactGroup data
            if (contactGroup.count == 0)
                //noinspection DataFlowIssue
                textViewDisplayName.setTextColor(convertView.getContext().getColor(R.color.preferenceSummaryTextColor));
            else
                //noinspection DataFlowIssue
                textViewDisplayName.setTextColor(convertView.getContext().getColor(R.color.activityNormalTextColor));
            //noinspection DataFlowIssue
            textViewDisplayName.setText(contactGroup.name + " (" + contactGroup.count + ")");

            boolean found = false;
            String accountType = "";
            PackageManager packageManager = context.getPackageManager();
            try {
                ApplicationInfo applicationInfo = packageManager.getApplicationInfo(contactGroup.accountType, PackageManager.MATCH_ALL);
                //if (applicationInfo != null) {
                    accountType = packageManager.getApplicationLabel(applicationInfo).toString();
                    found = true;
                //}
            } catch (Exception ignored) {}
            if (!found) {
                if (contactGroup.accountType != null)
                    accountType = ContactsCache.getAccountName(contactGroup.accountType, context);
            }
            if (accountType.isEmpty())
                accountType = contactGroup.accountType;
            contactGroup.displayedAccountType = accountType;

            if (contactGroup.count == 0)
                //noinspection DataFlowIssue
                textViewAccountType.setTextColor(convertView.getContext().getColor(R.color.preferenceSummaryTextColor));
            else
                //noinspection DataFlowIssue
                textViewAccountType.setTextColor(convertView.getContext().getColor(R.color.activityNormalTextColor));
            //noinspection DataFlowIssue
            textViewAccountType.setText(contactGroup.displayedAccountType);

            checkBox.setChecked(contactGroup.checked);
        }

        return convertView;
    }

}
