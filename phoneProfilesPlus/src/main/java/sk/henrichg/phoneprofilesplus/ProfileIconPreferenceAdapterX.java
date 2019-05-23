package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import java.util.Map;

class ProfileIconPreferenceAdapterX extends BaseAdapter {

    private final Context context;
    private final LayoutInflater inflater;

    private final ProfileIconPreferenceX preference;
    /*private String imageIdentifier;
    private boolean isImageResourceID;
    private boolean useCustomColor;
    private int customColor;*/

    ProfileIconPreferenceAdapterX(ProfileIconPreferenceX preference, Context context/*, String imageIdentifier, boolean isImageResourceID, boolean useCustomColor, int customColor*/)
    {
        this.preference = preference;
        this.context = context;

        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
          //int position;
        }

    @SuppressLint("PrivateResource")
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;

        View vi = convertView;
        if (convertView == null)
        {
            vi = inflater.inflate(R.layout.profileicon_preference_gridview_item, parent, false);
            holder = new ViewHolder();
            holder.icon = vi.findViewById(R.id.profileicon_preference_gridview_item_icon);
            vi.setTag(holder);
        }
        else
        {
            holder = (ViewHolder)vi.getTag();
        }

        //String iconResName = context.getResources().getResourceEntryName(Profile.profileIconId[position]);
        String iconResName = ProfileIconPreferenceAdapterX.getImageResourceName(position);
        if (iconResName.equals(preference.imageIdentifier) && preference.isImageResourceID) {
            //if (Build.VERSION.SDK_INT >= 21)
                holder.icon.setBackgroundColor(GlobalGUIRoutines.getThemeColorControlHighlight(context));
            /*else {
                if (ApplicationPreferences.applicationTheme(context, true).equals("dark"))
                    holder.icon.setBackgroundResource(R.drawable.abc_list_selector_background_transition_holo_dark);
                else
                    holder.icon.setBackgroundResource(R.drawable.abc_list_selector_background_transition_holo_light);
            }*/
        }
        else
            holder.icon.setBackgroundResource(0);

        int iconRes = Profile.profileIconId[position];
        if (iconResName.equals(preference.imageIdentifier) && preference.isImageResourceID && preference.useCustomColor) {
            Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), iconRes);
            bitmap = BitmapManipulator.recolorBitmap(bitmap, preference.customColor/*, context*/);
            holder.icon.setImageBitmap(bitmap);
        }
        else
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

    static int getImageResourcePosition(String imageIdentifier/*, Context context*/) {
        /*for (int pos = 0; pos < Profile.profileIconId.length; pos++) {
            String resName = context.getResources().getResourceEntryName(Profile.profileIconId[pos]);
            if (resName.equals(imageIdentifier))
                return pos;
        }*/
        if (Profile.profileIconIdMap.get(imageIdentifier) != null) {
            int iconResource = Profile.getIconResource(imageIdentifier);
            for (int pos = 0; pos < Profile.profileIconId.length; pos++) {
                if (Profile.profileIconId[pos] == iconResource)
                    return pos;
            }
        }
        return 0;
    }

    static String getImageResourceName(int position) {
        int iconResource = Profile.profileIconId[position];
        for(Map.Entry entry: Profile.profileIconIdMap.entrySet()){
            if (entry.getValue().equals(iconResource)) {
                return entry.getKey().toString();
            }
        }
        return "ic_profile_default";
    }

    void setCustomColor(/*boolean newUseCustomColor, int newCustomColor*/) {
        //preference.useCustomColor = newUseCustomColor;
        //preference.customColor = newCustomColor;
        notifyDataSetChanged();
    }

    static int getIconColor(String imageIdentifier/*, Context context*/) {
        return Profile.profileIconColor[getImageResourcePosition(imageIdentifier/*, context*/)];
    }

}
