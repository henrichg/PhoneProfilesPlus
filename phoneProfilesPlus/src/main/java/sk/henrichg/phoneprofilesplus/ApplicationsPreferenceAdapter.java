package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.preference.Preference;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.CharacterStyle;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
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
    private Context context;
    private DataWrapper dataWrapper;

    private ApplicationsDialogPreference preference;

    public ApplicationsPreferenceAdapter(Context context, ApplicationsDialogPreference preference)
    {
        // Cache the LayoutInflate to avoid asking for a new one each time.
        inflater = LayoutInflater.from(context);
        this.context = context;

        this.preference = preference;
        dataWrapper = new DataWrapper(context, false, false, 0);
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

    public static void setTextStyle(TextView textView, boolean errorColor)
    {
        if (textView != null) {
            CharSequence title = textView.getText();
            Spannable sbt = new SpannableString(title);
            Object spansToRemove[] = sbt.getSpans(0, title.length(), Object.class);
            for (Object span : spansToRemove) {
                if (span instanceof CharacterStyle)
                    sbt.removeSpan(span);
            }
            if (errorColor) {
                sbt.setSpan(new ForegroundColorSpan(Color.RED), 0, title.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                textView.setText(sbt);
            } else {
                textView.setText(sbt);
            }
        }
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
        String text = application.appLabel;
        if (application.shortcutId > 0) {
            Shortcut shortcut = dataWrapper.getDatabaseHandler().getShortcut(application.shortcutId);
            if (shortcut != null)
                text = shortcut._name;
        }
        textViewAppName.setText(text);
        setTextStyle(textViewAppName, application.shortcut && (application.shortcutId == 0));
        if (application.shortcut)
            textViewAppType.setText("- "+context.getString(R.string.applications_preference_applicationType_shortcut));
        else
            textViewAppType.setText("- "+context.getString(R.string.applications_preference_applicationType_application));
        setTextStyle(textViewAppType, application.shortcut && (application.shortcutId == 0));

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
