package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

class ProfileIconPreferenceAdapter extends BaseAdapter {

    private final Context context;
    //private final LayoutInflater inflater;

    private final ProfileIconPreference preference;
    /*private String imageIdentifier;
    private boolean isImageResourceID;
    private boolean useCustomColor;
    private int customColor;*/

    ProfileIconPreferenceAdapter(ProfileIconPreference preference, Context context/*, String imageIdentifier, boolean isImageResourceID, boolean useCustomColor, int customColor*/)
    {
        this.preference = preference;
        this.context = context;

        //inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public int getCount() {
        return Profile.profileIconId.length;
    }

    public Object getItem(int position) {
        return Profile.profileIconId[position];
    }

    public long getItemId(int position) {
        return position;
    }

    static class ViewHolder {
          ImageView icon;
        View mark;
          //int position;
        }

    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;

        View vi = convertView;
        if (convertView == null)
        {
            vi = LayoutInflater.from(context).inflate(R.layout.profileicon_preference_gridview_item, parent, false);
            holder = new ViewHolder();
            holder.icon = vi.findViewById(R.id.profileicon_preference_gridview_item_icon);
            holder.mark = vi.findViewById(R.id.profileicon_preference_gridview_item_mark);
            vi.setTag(holder);
        }
        else
        {
            holder = (ViewHolder)vi.getTag();
        }

        //String iconResName = context.getResources().getResourceEntryName(Profile.profileIconId[position]);
        String iconResName = ProfileStatic.getImageResourceName(position);
        if (iconResName.equals(preference.imageIdentifier) && preference.isImageResourceID) {
            //if (Build.VERSION.SDK_INT >= 21)
            //holder.icon.setBackgroundColor(GlobalGUIRoutines.getThemeColorControlHighlight(context));
            //    holder.icon.setBackgroundResource(R.drawable.profile_icon_background);
            /*else {
                if (ApplicationPreferences.applicationTheme(context, true).equals("dark"))
                    holder.icon.setBackgroundResource(R.drawable.abc_list_selector_background_transition_holo_dark);
                else
                    holder.icon.setBackgroundResource(R.drawable.abc_list_selector_background_transition_holo_light);
            }*/
            holder.mark.setVisibility(View.VISIBLE);
        } else {
            //holder.icon.setBackgroundResource(0);
            holder.mark.setVisibility(View.INVISIBLE);
        }

        int iconRes = Profile.profileIconId[position];
        /*if (iconResName.equals(preference.imageIdentifier) && preference.isImageResourceID && preference.useCustomColor) {
            //Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), iconRes);
            Bitmap bitmap = BitmapManipulator.getBitmapFromResource(iconRes, true, context);
            bitmap = BitmapManipulator.recolorBitmap(bitmap, preference.customColor);
            holder.icon.setImageBitmap(bitmap);
        }
        else*/
            holder.icon.setImageResource(iconRes);

        return vi;
    }

    void imageIdentifierAndTypeChanged(/*String imageIdentifier, boolean isImageResourceID*/) {
        /*if (!preference.imageIdentifier.equals(imageIdentifier)) {
            preference.useCustomColor = false;
            preference.customColor = 0;
        }
        preference.imageIdentifier = imageIdentifier;
        preference.isImageResourceID = isImageResourceID;*/
        notifyDataSetChanged();
    }

    void setCustomColor(/*boolean newUseCustomColor, int newCustomColor*/) {
        //preference.useCustomColor = newUseCustomColor;
        //preference.customColor = newCustomColor;
        notifyDataSetChanged();
    }

}
