package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.util.TypedValue;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

class ProfileListWidgetFactory implements RemoteViewsService.RemoteViewsFactory {

    private DataWrapper dataWrapper;

    private final Context context;
    //private int appWidgetId;
    private List<Profile> profileList = new ArrayList<>();

    ProfileListWidgetFactory(Context context, @SuppressWarnings("unused") Intent intent) {
        this.context = context;
        /*appWidgetId=intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                                       AppWidgetManager.INVALID_APPWIDGET_ID);*/
    }
  
    private void createProfilesDataWrapper()
    {
        /*String applicationWidgetListIconLightness = ApplicationPreferences.applicationWidgetListIconLightness;
        String applicationWidgetListIconColor = ApplicationPreferences.applicationWidgetListIconColor;
        boolean applicationWidgetListCustomIconLightness = ApplicationPreferences.applicationWidgetListCustomIconLightness;*/

        int monochromeValue = 0xFF;
        switch (ApplicationPreferences.applicationWidgetListIconLightness) {
            case "0":
                monochromeValue = 0x00;
                break;
            case "12":
                monochromeValue = 0x20;
                break;
            case "25":
                monochromeValue = 0x40;
                break;
            case "37":
                monochromeValue = 0x60;
                break;
            case "50":
                monochromeValue = 0x80;
                break;
            case "62":
                monochromeValue = 0xA0;
                break;
            case "75":
                monochromeValue = 0xC0;
                break;
            case "87":
                monochromeValue = 0xE0;
                break;
            case "100":
                monochromeValue = 0xFF;
                break;
        }

        if (dataWrapper == null)
        {
            dataWrapper = new DataWrapper(context.getApplicationContext(), ApplicationPreferences.applicationWidgetListIconColor.equals("1"),
                                            monochromeValue, ApplicationPreferences.applicationWidgetListCustomIconLightness);
        }
        else
        {
            dataWrapper.setParameters(ApplicationPreferences.applicationWidgetListIconColor.equals("1"),
                                        monochromeValue, ApplicationPreferences.applicationWidgetListCustomIconLightness);
        }
    }

    public void onCreate() {
    }

    public void onDestroy() {
        /*if (dataWrapper != null)
            dataWrapper.invalidateDataWrapper();
        dataWrapper = null;*/
    }

    public int getCount() {
        int count = 0;
        if (profileList != null) {
            for (Profile profile : profileList) {
                if (profile._showInActivator)
                    ++count;
            }
        }
        return count;
    }

    private Profile getItem(int position)
    {
        if (getCount() == 0)
            return null;
        else
        {
            Profile _profile = null;

            int pos = -1;
            for (Profile profile : profileList)
            {
                if (profile._showInActivator)
                    ++pos;

                if (pos == position)
                {
                    _profile = profile;
                    break;
                }
            }

            return _profile;
        }
    }

    public RemoteViews getViewAt(int position) {
        RemoteViews row;
        if (!ApplicationPreferences.applicationWidgetListGridLayout)
            row=new RemoteViews(context.getPackageName(), R.layout.profile_list_widget_item);
        else
            row=new RemoteViews(context.getPackageName(), R.layout.profile_grid_widget_item);
    
        Profile profile = getItem(position);

        if (profile != null) {
            /*String applicationWidgetListLightnessT = ApplicationPreferences.applicationWidgetListLightnessT;
            boolean applicationWidgetListHeader= ApplicationPreferences.applicationWidgetListHeader;
            boolean applicationWidgetListGridLayout = ApplicationPreferences.applicationWidgetListGridLayout;
            boolean applicationWidgetListPrefIndicator = ApplicationPreferences.applicationWidgetListPrefIndicator;*/

            if (profile.getIsIconResourceID()) {
                if (profile._iconBitmap != null)
                    row.setImageViewBitmap(R.id.widget_profile_list_item_profile_icon, profile._iconBitmap);
                else {
                    row.setImageViewResource(R.id.widget_profile_list_item_profile_icon,
                            /*context.getResources().getIdentifier(profile.getIconIdentifier(), "drawable", context.getPackageName())*/
                            Profile.getIconResource(profile.getIconIdentifier()));
                }
            } else {
                row.setImageViewBitmap(R.id.widget_profile_list_item_profile_icon, profile._iconBitmap);
            }
            int red = 0xFF;
            int green;
            int blue;
            switch (ApplicationPreferences.applicationWidgetListLightnessT) {
                case "0":
                    red = 0x00;
                    break;
                case "12":
                    red = 0x20;
                    break;
                case "25":
                    red = 0x40;
                    break;
                case "37":
                    red = 0x60;
                    break;
                case "50":
                    red = 0x80;
                    break;
                case "62":
                    red = 0xA0;
                    break;
                case "75":
                    red = 0xC0;
                    break;
                case "87":
                    red = 0xE0;
                    break;
                case "100":
                    red = 0xFF;
                    break;
            }
            green = red;
            blue = red;
            if (!ApplicationPreferences.applicationWidgetListHeader) {
                if (profile._checked) {
                    row.setTextViewTextSize(R.id.widget_profile_list_item_profile_name, TypedValue.COMPLEX_UNIT_SP, 16);

                    //if (PPApplication.applicationWidgetListIconColor.equals("1"))
                    row.setTextColor(R.id.widget_profile_list_item_profile_name, Color.argb(0xFF, red, green, blue));
                    //else
                    //	row.setTextColor(R.id.widget_profile_list_item_profile_name, Color.parseColor("#33b5e5"));
                } else {
                    row.setTextViewTextSize(R.id.widget_profile_list_item_profile_name, TypedValue.COMPLEX_UNIT_SP, 15);

                    //if (PPApplication.applicationWidgetListIconColor.equals("1"))
                    row.setTextColor(R.id.widget_profile_list_item_profile_name, Color.argb(0xCC, red, green, blue));
                    //else
                    //	row.setTextColor(R.id.widget_profile_list_item_profile_name, Color.argb(0xFF, red, green, blue));
                }
            } else {
                row.setTextColor(R.id.widget_profile_list_item_profile_name, Color.argb(0xFF, red, green, blue));
            }
            if ((!ApplicationPreferences.applicationWidgetListHeader) && (profile._checked)) {
                // hm, interesting, how to set bold style for RemoteView text ;-)
                Spannable profileName = DataWrapper.getProfileNameWithManualIndicator(profile, !ApplicationPreferences.applicationWidgetListGridLayout,
                                            "", true, true, true, dataWrapper);
                Spannable sb = new SpannableString(profileName);
                sb.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, profileName.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                row.setTextViewText(R.id.widget_profile_list_item_profile_name, sb);
            } else {
                Spannable profileName = profile.getProfileNameWithDuration("", "",
                        true/*applicationWidgetListGridLayout*/, true, context);
                row.setTextViewText(R.id.widget_profile_list_item_profile_name, profileName);
            }
            if (!ApplicationPreferences.applicationWidgetListGridLayout) {
                if (ApplicationPreferences.applicationWidgetListPrefIndicator) {
                    if (profile._preferencesIndicator != null)
                        row.setImageViewBitmap(R.id.widget_profile_list_profile_pref_indicator, profile._preferencesIndicator);
                    else
                        row.setImageViewResource(R.id.widget_profile_list_profile_pref_indicator, R.drawable.ic_empty);
                    row.setViewVisibility(R.id.widget_profile_list_profile_pref_indicator, View.VISIBLE);
                }
                else
                    //row.setImageViewResource(R.id.widget_profile_list_profile_pref_indicator, R.drawable.ic_empty);
                    row.setViewVisibility(R.id.widget_profile_list_profile_pref_indicator, View.GONE);
            }

            Intent i = new Intent();
            Bundle extras = new Bundle();

            if ((!ApplicationPreferences.applicationWidgetListHeader) &&
                Event.getGlobalEventsRunning() && (position == 0))
                extras.putLong(PPApplication.EXTRA_PROFILE_ID, Profile.RESTART_EVENTS_PROFILE_ID);
            else
                extras.putLong(PPApplication.EXTRA_PROFILE_ID, profile._id);
            extras.putInt(PPApplication.EXTRA_STARTUP_SOURCE, PPApplication.STARTUP_SOURCE_SHORTCUT);
            i.putExtras(extras);
            row.setOnClickFillInIntent(R.id.widget_profile_list_item, i);

        }

        return(row);
    }

    public RemoteViews getLoadingView() {
        return(null);
    }
  
    public int getViewTypeCount() {
        return(1);
    }

    public long getItemId(int position) {
        return(position);
    }

    public boolean hasStableIds() {
        return(true);
    }

    /*
        Called when notifyDataSetChanged() is triggered on the remote adapter. This allows a RemoteViewsFactory to
        respond to data changes by updating any internal references.

        Note: expensive tasks can be safely performed synchronously within this method. In the interim,
        the old data will be displayed within the widget.
    */
    public void onDataSetChanged() {
        createProfilesDataWrapper();

        /*boolean applicationWidgetListPrefIndicator = ApplicationPreferences.applicationWidgetListPrefIndicator;
        boolean applicationWidgetListHeader = ApplicationPreferences.applicationWidgetListHeader;*/

        List<Profile> newProfileList = dataWrapper.getNewProfileList(true,
                ApplicationPreferences.applicationWidgetListPrefIndicator);
        dataWrapper.getEventTimelineList(true);

        if (!ApplicationPreferences.applicationWidgetListHeader)
        {
            // show activated profile in list if is not showed in activator
            Profile profile = dataWrapper.getActivatedProfile(newProfileList);
            if ((profile != null) && (!profile._showInActivator))
            {
                profile._showInActivator = true;
                profile._porder = -1;
            }
        }
        Collections.sort(newProfileList, new ProfileComparator());

        if ((!ApplicationPreferences.applicationWidgetListHeader) &&
                Event.getGlobalEventsRunning()) {
            Profile restartEvents = DataWrapper.getNonInitializedProfile(context.getString(R.string.menu_restart_events), "ic_list_item_events_restart_color|1|0|0", 0);
            restartEvents._showInActivator = true;
            newProfileList.add(0, restartEvents);
        }

        //if (dataWrapper != null) {
            //dataWrapper.invalidateProfileList();
            dataWrapper.setProfileList(newProfileList);
            profileList = newProfileList;
        //}
    }

    private static class ProfileComparator implements Comparator<Profile> {

        public int compare(Profile lhs, Profile rhs) {
            int res = 0;
            if ((lhs != null) && (rhs != null))
                res = lhs._porder - rhs._porder;
            return res;
        }
    }

}
