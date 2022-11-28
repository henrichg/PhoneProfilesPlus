package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

class ConfiguredProfilePreferencesAdapter extends BaseAdapter
{
    private final ConfiguredProfilePreferencesDialogPreference preference;
    private final Context context;

    private final LayoutInflater inflater;

    ConfiguredProfilePreferencesAdapter(Context context, ConfiguredProfilePreferencesDialogPreference preference)
    {
        this.preference = preference;
        this.context = context;

        // Cache the LayoutInflate to avoid asking for a new one each time.
        inflater = LayoutInflater.from(context);
    }

    public int getCount() {
        return preference.preferencesList.size();
    }

    public Object getItem(int position) {
        return preference.preferencesList.get(position);
    }

    public long getItemId(int position) {
        return position;
    }
    
    static class ViewHolder {
        ImageView preferenceIcon;
        ImageView preferenceIcon2;
        TextView preferenceString;
        TextView preferenceDescription;
        //int position;
    }

    public View getView(final int position, View convertView, ViewGroup parent)
    {
        ConfiguredProfilePreferencesData configuredPreferences = preference.preferencesList.get(position);

        ViewHolder holder;
        
        View vi = convertView;
        if (convertView == null)
        {
            vi = inflater.inflate(R.layout.configured_profile_preferences_list_item, parent, false);
            holder = new ViewHolder();
            holder.preferenceIcon = vi.findViewById(R.id.configured_profile_preferences_preference_icon);
            holder.preferenceIcon2 = vi.findViewById(R.id.configured_profile_preferences_preference_icon2);
            holder.preferenceString = vi.findViewById(R.id.configured_profile_preferences_preference_string);
            holder.preferenceDescription = vi.findViewById(R.id.configured_profile_preferences_preference_decription);
            vi.setTag(holder);
        } else {
            holder = (ViewHolder) vi.getTag();
        }

        //String applicationTheme = ApplicationPreferences.applicationTheme(context.getApplicationContext(), true);
        //boolean nightModeOn = GlobalGUIRoutines.isNightModeEnabled(context.getApplicationContext());
//                (context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK)
//                                    == Configuration.UI_MODE_NIGHT_YES;
        String applicationTheme = ApplicationPreferences.applicationTheme(context, true);
        boolean nightModeOn = !applicationTheme.equals("white");

        if (configuredPreferences.preferenceIcon == 0) {
            holder.preferenceIcon.setVisibility(View.GONE);
        } else {
            Paint paint = new Paint();

            float brightness;
            if (/*applicationTheme.equals("dark")*/nightModeOn) {
                //if (disabled)
                //    paint.setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(context, R.color.profileindicatorColorDisabled_dark), PorterDuff.Mode.SRC_ATOP));
                //else
                paint.setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(context, R.color.profileindicatorColor_dark), PorterDuff.Mode.SRC_ATOP));
                brightness = 64f;
            } else {
                //if (disabled)
                //    paint.setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(context, R.color.profileindicatorColorDisabled_light), PorterDuff.Mode.SRC_ATOP));
                //else
                paint.setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(context, R.color.profileindicatorColor_light), PorterDuff.Mode.SRC_ATOP));
                brightness = 50f;
            }

            if (configuredPreferences.preferenceIconDisabled) {
                ColorMatrix saturationCM = new ColorMatrix();
                saturationCM.setSaturation(0);
                paint.setColorFilter(new ColorMatrixColorFilter(saturationCM));
                //paint.setAlpha(ProfilePreferencesIndicator.DISABLED_ALPHA);
            }

            Bitmap preferenceBitmap = BitmapFactory.decodeResource(context.getResources(), configuredPreferences.preferenceIcon);

            Bitmap bitmapResult = Bitmap.createBitmap(preferenceBitmap.getWidth(), preferenceBitmap.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas _canvas = new Canvas(bitmapResult);
            _canvas.drawBitmap(preferenceBitmap, 0, 0, paint);

            if (configuredPreferences.preferenceIconDisabled) {
                bitmapResult = BitmapManipulator.setBitmapBrightness(bitmapResult, brightness);
            }

            holder.preferenceIcon.setImageBitmap(bitmapResult);

            /*
            //holder.preferenceIcon.setImageResource(configuredPreferences.preferenceIcon);

            if (applicationTheme.equals("dark")) {
                //if (configuredPreferences.preferenceIconDisabled)
                //    holder.preferenceIcon.setColorFilter(ContextCompat.getColor(context, R.color.profileindicatorColorDisabled_dark), PorterDuff.Mode.SRC_ATOP);
                //else
                    holder.preferenceIcon.setColorFilter(ContextCompat.getColor(context, R.color.profileindicatorColor_dark), PorterDuff.Mode.SRC_ATOP);
            } else {
                if (configuredPreferences.preferenceIconDisabled)
                    holder.preferenceIcon.setColorFilter(ContextCompat.getColor(context, R.color.profileindicatorColorDisabled_light), PorterDuff.Mode.SRC_ATOP);
                else
                    holder.preferenceIcon.setColorFilter(ContextCompat.getColor(context, R.color.profileindicatorColor_light), PorterDuff.Mode.SRC_ATOP);
            }
            */

            holder.preferenceIcon.setVisibility(View.VISIBLE);
        }
        if (configuredPreferences.preferenceIcon2 == 0) {
            holder.preferenceIcon2.setVisibility(View.GONE);
        }
        else {
            Paint paint = new Paint();

            float brightness;
            if (/*applicationTheme.equals("dark")*/nightModeOn) {
                //if (disabled)
                //    paint.setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(context, R.color.profileindicatorColorDisabled_dark), PorterDuff.Mode.SRC_ATOP));
                //else
                paint.setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(context, R.color.profileindicatorColor_dark), PorterDuff.Mode.SRC_ATOP));
                brightness = 64f;
            } else {
                //if (disabled)
                //    paint.setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(context, R.color.profileindicatorColorDisabled_light), PorterDuff.Mode.SRC_ATOP));
                //else
                paint.setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(context, R.color.profileindicatorColor_light), PorterDuff.Mode.SRC_ATOP));
                brightness = 40f;
            }

            if (configuredPreferences.preferenceIcon2Disabled) {
                ColorMatrix saturationCM = new ColorMatrix();
                saturationCM.setSaturation(0);
                paint.setColorFilter(new ColorMatrixColorFilter(saturationCM));
                //paint.setAlpha(ProfilePreferencesIndicator.DISABLED_ALPHA);
            }

            Bitmap preferenceBitmap = BitmapFactory.decodeResource(context.getResources(), configuredPreferences.preferenceIcon2);

            Bitmap bitmapResult = Bitmap.createBitmap(preferenceBitmap.getWidth(), preferenceBitmap.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas _canvas = new Canvas(bitmapResult);
            _canvas.drawBitmap(preferenceBitmap, 0, 0, paint);

            if (configuredPreferences.preferenceIcon2Disabled) {
                bitmapResult = BitmapManipulator.setBitmapBrightness(bitmapResult, brightness);
            }

            holder.preferenceIcon2.setImageBitmap(bitmapResult);

            /*
            holder.preferenceIcon2.setImageResource(configuredPreferences.preferenceIcon2);

            if (applicationTheme.equals("dark")) {
                if (configuredPreferences.preferenceIcon2Disabled)
                    holder.preferenceIcon2.setColorFilter(ContextCompat.getColor(context, R.color.profileindicatorColorDisabled_dark), PorterDuff.Mode.SRC_ATOP);
                else
                    holder.preferenceIcon2.setColorFilter(ContextCompat.getColor(context, R.color.profileindicatorColor_dark), PorterDuff.Mode.SRC_ATOP);
            } else {
                if (configuredPreferences.preferenceIcon2Disabled)
                    holder.preferenceIcon2.setColorFilter(ContextCompat.getColor(context, R.color.profileindicatorColorDisabled_light), PorterDuff.Mode.SRC_ATOP);
                else
                    holder.preferenceIcon2.setColorFilter(ContextCompat.getColor(context, R.color.profileindicatorColor_light), PorterDuff.Mode.SRC_ATOP);
            }
            */

            holder.preferenceIcon2.setVisibility(View.VISIBLE);
        }

        if (configuredPreferences.preferenceIcon == 0) {
            holder.preferenceString.setVisibility(View.GONE);
        }
        else {
            holder.preferenceString.setText(configuredPreferences.preferenceString);
            holder.preferenceString.setVisibility(View.VISIBLE);
        }
        holder.preferenceDescription.setText(configuredPreferences.preferenceDecription);

        return vi;
    }

}
