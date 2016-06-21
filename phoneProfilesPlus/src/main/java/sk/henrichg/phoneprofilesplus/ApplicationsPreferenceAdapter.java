package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

public class ApplicationsPreferenceAdapter extends BaseAdapter
{
    private LayoutInflater inflater;
    //private Context context;

    private ApplicationsDialogPreference preference;

    public ApplicationsPreferenceAdapter(Context context, ApplicationsDialogPreference preference)
    {
        // Cache the LayoutInflate to avoid asking for a new one each time.
        inflater = LayoutInflater.from(context);
        //this.context = context;

        this.preference = preference;

    }

    public int getCount() {
        return preference.applicationsList.size();
    }

    public Object getItem(int position) {
        return preference.applicationsList.get(position);
    }

    public long getItemId(int position) {
        return position;
    }
    
    public View getView(int position, View convertView, ViewGroup parent)
    {
        // Application to display
        Application application = preference.applicationsList.get(position);
        //System.out.println(String.valueOf(position));

        // The child views in each row.
        ImageView imageViewIcon;
        TextView textViewAppName;
        TextView textViewAppType;
        ImageView imageViewMenu;

        // Create a new row view
        if (convertView == null)
        {
            convertView = inflater.inflate(R.layout.applications_preference_list_item, parent, false);

            // Find the child views.
            imageViewIcon = (ImageView) convertView.findViewById(R.id.applications_pref_dlg_item_icon);
            textViewAppName = (TextView) convertView.findViewById(R.id.applications_pref_dlg_item_app_name);
            textViewAppType = (TextView) convertView.findViewById(R.id.applications_pref_dlg_item_app_type);
            imageViewMenu = (ImageView) convertView.findViewById(R.id.applications_pref_dlg_item_edit_menu);

            // Optimization: Tag the row with it's child views, so we don't
            // have to
            // call findViewById() later when we reuse the row.
            convertView.setTag(new ApplicationViewHolder(imageViewIcon, textViewAppName, textViewAppType,
                                        null, imageViewMenu));
        }
        // Reuse existing row view
        else
        {
            // Because we use a ViewHolder, we avoid having to call
            // findViewById().
            ApplicationViewHolder viewHolder = (ApplicationViewHolder) convertView.getTag();
            imageViewIcon = viewHolder.imageViewIcon;
            textViewAppName = viewHolder.textViewAppName;
            textViewAppType = viewHolder.textViewAppType;
            imageViewMenu = viewHolder.imageViewMenu;
        }

        // Display Application data
        imageViewIcon.setImageDrawable(application.icon);
        textViewAppName.setText(application.appLabel);
        if (application.shortcut)
            textViewAppType.setText(R.string.applications_preference_applicationType_shortcut);
        else
            textViewAppType.setText(R.string.applications_preference_applicationType_application);

        imageViewMenu.setTag(position);
        final ImageView itemEditMenu = imageViewMenu;
        imageViewMenu.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                preference.showEditMenu(itemEditMenu);
            }
        });

        return convertView;
    }

}
