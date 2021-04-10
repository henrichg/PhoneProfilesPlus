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

class ContactGroupsMultiSelectPreferenceAdapterX extends BaseAdapter
{
    private final LayoutInflater inflater;
    private final Context context;

    ContactGroupsMultiSelectPreferenceAdapterX(Context context)
    {
        // Cache the LayoutInflate to avoid asking for a new one each time.
        inflater = LayoutInflater.from(context);
        this.context = context;
    }

    public int getCount() {
        ContactGroupsCache contactGroupsCache = PhoneProfilesService.getContactGroupsCache();
        if (contactGroupsCache != null)
            return contactGroupsCache.getLength();
        else
            return 0;
    }

    public Object getItem(int position) {
        ContactGroupsCache contactGroupsCache = PhoneProfilesService.getContactGroupsCache();
        if (contactGroupsCache != null)
            return contactGroupsCache.getContactGroup(position);
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
            convertView = inflater.inflate(R.layout.contact_groups_multiselect_preference_list_item, parent, false);

            // Find the child views.
            textViewDisplayName = convertView.findViewById(R.id.contact_groups_multiselect_pref_dlg_item_display_name);
            checkBox = convertView.findViewById(R.id.contact_groups_multiselect_pref_dlg_item_checkbox);
            textViewAccountType = convertView.findViewById(R.id.contact_groups_multiselect_pref_dlg_item_account_type);

            // Optimization: Tag the row with it's child views, so we don't
            // have to
            // call findViewById() later when we reuse the row.
            convertView.setTag(new ContactGroupViewHolder(textViewDisplayName, checkBox, textViewAccountType));

            // If CheckBox is toggled, update the ContactGroup it is tagged with.
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

        ContactGroupsCache contactGroupsCache = PhoneProfilesService.getContactGroupsCache();
        if (contactGroupsCache != null) {
            // Contact group to display
            ContactGroup contactGroup = contactGroupsCache.getContactGroup(position);
            //System.out.println(String.valueOf(position));

            // Tag the CheckBox with the ContactGroup it is displaying, so that we
            // can
            // access the ContactGroup in onClick() when the CheckBox is toggled.
            checkBox.setTag(contactGroup);

            if (contactGroup != null) {
                // Display ContactGroup data
                textViewDisplayName.setText(contactGroup.name + " (" + contactGroup.count + ")");

//                PPApplication.logE("ContactGroupsMultiSelectPreferenceAdapterX.getView", "contactGroup.accountType="+contactGroup.accountType);

                boolean found = false;
                PackageManager packageManager = context.getPackageManager();
                try {
                    ApplicationInfo applicationInfo = packageManager.getApplicationInfo(contactGroup.accountType, 0);
                    if (applicationInfo != null) {
                        contactGroup.accountType = packageManager.getApplicationLabel(applicationInfo).toString();
                        found = true;
                    }
                } catch (Exception ignored) {}
//                PPApplication.logE("ContactGroupsMultiSelectPreferenceAdapterX.getView", "found="+found);
                if (!found) {
                    if (contactGroup.accountType.equals("com.osp.app.signin"))
                        contactGroup.accountType = context.getString(R.string.contact_account_type_samsung_account);
                    if (contactGroup.accountType.equals("com.google"))
                        contactGroup.accountType = context.getString(R.string.contact_account_type_google_account);
                    if (contactGroup.accountType.equals("vnd.sec.contact.sim"))
                        contactGroup.accountType = context.getString(R.string.contact_account_type_sim_card);
                    if (contactGroup.accountType.equals("vnd.sec.contact.sim2"))
                        contactGroup.accountType = context.getString(R.string.contact_account_type_sim_card);
                    if (contactGroup.accountType.equals("vnd.sec.contact.phone"))
                        contactGroup.accountType = context.getString(R.string.contact_account_type_phone_application);
                    if (contactGroup.accountType.equals("org.thoughtcrime.securesms"))
                        contactGroup.accountType = "Signal";
                    if (contactGroup.accountType.equals("com.google.android.apps.tachyon"))
                        contactGroup.accountType = "Duo";
                    if (contactGroup.accountType.equals("com.whatsapp"))
                        contactGroup.accountType = "WhatsApp";
                }
                textViewAccountType.setText(contactGroup.accountType);

                checkBox.setChecked(contactGroup.checked);
            }
            else {
                textViewDisplayName.setText(context.getString(R.string.empty_string));
                checkBox.setChecked(false);
                textViewAccountType.setText(context.getString(R.string.empty_string));
            }
        }

        return convertView;
    }

}
