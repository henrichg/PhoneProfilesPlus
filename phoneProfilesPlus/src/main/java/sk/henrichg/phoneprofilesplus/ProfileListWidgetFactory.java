package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.util.TypedValue;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import androidx.core.graphics.ColorUtils;

import java.util.Comparator;
import java.util.List;

class ProfileListWidgetFactory implements RemoteViewsService.RemoteViewsFactory {

    private DataWrapper dataWrapper;

    private final Context context;
    //private int appWidgetId;
    //private List<Profile> profileList = new ArrayList<>();

    ProfileListWidgetFactory(Context context, @SuppressWarnings("unused") Intent intent) {
        this.context = context;
        /*appWidgetId=intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                                       AppWidgetManager.INVALID_APPWIDGET_ID);*/
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
        if (dataWrapper != null) {
            //if (dataWrapper.profileList != null) {
                for (Profile profile : dataWrapper.profileList) {
                    if (profile._showInActivator)
                        ++count;
                }
            //}
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
            if (dataWrapper != null) {
                int pos = -1;
                for (Profile profile : dataWrapper.profileList) {
                    if (profile._showInActivator)
                        ++pos;

                    if (pos == position) {
                        _profile = profile;
                        break;
                    }
                }
            }
            return _profile;
        }
    }

    public RemoteViews getViewAt(int position) {
        RemoteViews row;

        boolean applicationWidgetListGridLayout;
        String applicationWidgetListLightnessT;
        boolean applicationWidgetListHeader;
        boolean applicationWidgetListPrefIndicator;
        boolean applicationWidgetListChangeColorsByNightMode;
        String applicationWidgetListIconColor;
        boolean applicationWidgetListUseDynamicColors;
        boolean applicationWidgetListBackgroundType;
        String applicationWidgetListLightnessB;
        String applicationWidgetListBackgroundColor;
        String applicationWidgetListBackgroundColorNightModeOff;
        String applicationWidgetListBackgroundColorNightModeOn;

        synchronized (PPApplication.applicationPreferencesMutex) {
            applicationWidgetListGridLayout = ApplicationPreferences.applicationWidgetListGridLayout;
            applicationWidgetListLightnessT = ApplicationPreferences.applicationWidgetListLightnessT;
            applicationWidgetListHeader = ApplicationPreferences.applicationWidgetListHeader;
            applicationWidgetListPrefIndicator = ApplicationPreferences.applicationWidgetListPrefIndicator;
            applicationWidgetListChangeColorsByNightMode = ApplicationPreferences.applicationWidgetListChangeColorsByNightMode;
            applicationWidgetListIconColor = ApplicationPreferences.applicationWidgetListIconColor;
            applicationWidgetListUseDynamicColors = ApplicationPreferences.applicationWidgetListUseDynamicColors;
            applicationWidgetListBackgroundType = ApplicationPreferences.applicationWidgetListBackgroundType;
            applicationWidgetListLightnessB = ApplicationPreferences.applicationWidgetListLightnessB;
            applicationWidgetListBackgroundColor = ApplicationPreferences.applicationWidgetListBackgroundColor;
            applicationWidgetListBackgroundColorNightModeOff = ApplicationPreferences.applicationWidgetListBackgroundColorNightModeOff;
            applicationWidgetListBackgroundColorNightModeOn = ApplicationPreferences.applicationWidgetListBackgroundColorNightModeOn;

            if (Build.VERSION.SDK_INT >= 30) {
                if (Build.VERSION.SDK_INT < 31)
                    applicationWidgetListUseDynamicColors = false;
                if (//PPApplication.isPixelLauncherDefault(context) ||
                        (applicationWidgetListChangeColorsByNightMode &&
                        (!applicationWidgetListUseDynamicColors))) {
                    int nightModeFlags =
                            context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
                    switch (nightModeFlags) {
                        case Configuration.UI_MODE_NIGHT_YES:
                            applicationWidgetListLightnessT = GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_87; // lightness of text = white
                            applicationWidgetListBackgroundType = true; // background type = color
                            applicationWidgetListBackgroundColor = String.valueOf(ColorChooserPreference.parseValue(applicationWidgetListBackgroundColorNightModeOn)); // color of background
                            break;
                        case Configuration.UI_MODE_NIGHT_NO:
                        case Configuration.UI_MODE_NIGHT_UNDEFINED:
                            applicationWidgetListLightnessT = GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_12; // lightness of text = black
                            applicationWidgetListBackgroundType = true; // background type = color
                            applicationWidgetListBackgroundColor = String.valueOf(ColorChooserPreference.parseValue(applicationWidgetListBackgroundColorNightModeOff)); // color of background
                            break;
                    }
                }
            }
        }

        if (!((Build.VERSION.SDK_INT >= 31) && applicationWidgetListChangeColorsByNightMode &&
                applicationWidgetListIconColor.equals("0") && applicationWidgetListUseDynamicColors)) {
            if (!applicationWidgetListGridLayout)
                row = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.profile_list_widget_item);
            else
                row = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.profile_grid_widget_item);
        } else {
            if (!applicationWidgetListGridLayout)
                row = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.profile_list_widget_item_dn);
            else
                row = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.profile_grid_widget_item_dn);
        }
    
        Profile profile = getItem(position);

        if (profile != null) {
            Bitmap bitmap = null;
            if (applicationWidgetListIconColor.equals("0")) {
                if (applicationWidgetListChangeColorsByNightMode ||
                    ((!applicationWidgetListBackgroundType) &&
                        (Integer.parseInt(applicationWidgetListLightnessB) <= 25)) ||
                    (applicationWidgetListBackgroundType &&
                        (ColorUtils.calculateLuminance(Integer.parseInt(applicationWidgetListBackgroundColor)) < 0.23)))
                    bitmap = profile.increaseProfileIconBrightnessForContext(context, profile._iconBitmap);
            }
            if (profile.getIsIconResourceID()) {
                if (bitmap != null)
                    row.setImageViewBitmap(R.id.widget_profile_list_item_profile_icon, bitmap);
                else {
                    if (profile._iconBitmap != null)
                        row.setImageViewBitmap(R.id.widget_profile_list_item_profile_icon, profile._iconBitmap);
                    else
                        row.setImageViewResource(R.id.widget_profile_list_item_profile_icon,
                            /*context.getResources().getIdentifier(profile.getIconIdentifier(), "drawable", context.PPApplication.PACKAGE_NAME)*/
                                ProfileStatic.getIconResource(profile.getIconIdentifier()));
                }
            } else {
                if (bitmap != null)
                    row.setImageViewBitmap(R.id.widget_profile_list_item_profile_icon, bitmap);
                else
                    row.setImageViewBitmap(R.id.widget_profile_list_item_profile_icon, profile._iconBitmap);
            }
            int red = 0xFF;
            int green;
            int blue;
            switch (applicationWidgetListLightnessT) {
                case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_0:
                    red = 0x00;
                    break;
                case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_12:
                    red = 0x20;
                    break;
                case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_25:
                    red = 0x40;
                    break;
                case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_37:
                    red = 0x60;
                    break;
                case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_50:
                    red = 0x80;
                    break;
                case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_62:
                    red = 0xA0;
                    break;
                case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_75:
                    red = 0xC0;
                    break;
                case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_87:
                    red = 0xE0;
                    break;
                case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_100:
                    //noinspection ConstantConditions
                    red = 0xFF;
                    break;
            }
            green = red;
            blue = red;
            if (!applicationWidgetListHeader) {
                if (profile._checked) {
                    row.setTextViewTextSize(R.id.widget_profile_list_item_profile_name, TypedValue.COMPLEX_UNIT_DIP, 16);

                    if (!((Build.VERSION.SDK_INT >= 31) && applicationWidgetListChangeColorsByNightMode &&
                            applicationWidgetListIconColor.equals("0") && applicationWidgetListUseDynamicColors))
                        row.setTextColor(R.id.widget_profile_list_item_profile_name, Color.argb(0xFF, red, green, blue));
                    else {
                        // must be removed android:textColor in layout
                        int color = GlobalGUIRoutines.getDynamicColor(R.attr.colorOnBackground, context);
                        if (color != 0) {
                            row.setTextColor(R.id.widget_profile_list_item_profile_name, color);
                        }
                    }
                } else {
                    row.setTextViewTextSize(R.id.widget_profile_list_item_profile_name, TypedValue.COMPLEX_UNIT_DIP, 15);

                    if (!((Build.VERSION.SDK_INT >= 31) && applicationWidgetListChangeColorsByNightMode &&
                            applicationWidgetListIconColor.equals("0") && applicationWidgetListUseDynamicColors))
                        row.setTextColor(R.id.widget_profile_list_item_profile_name, Color.argb(0xCC, red, green, blue));
                    else {
                        // must be removed android:textColor in layout
                        int color = GlobalGUIRoutines.getDynamicColor(R.attr.colorOnBackground, context);
                        if (color != 0) {
                            row.setTextColor(R.id.widget_profile_list_item_profile_name,
                                    Color.argb(0xCC, Color.red(color), Color.green(color), Color.blue(color)));
                        }
                    }
                }
            } else {
                if (!((Build.VERSION.SDK_INT >= 31) && applicationWidgetListChangeColorsByNightMode &&
                        applicationWidgetListIconColor.equals("0") && applicationWidgetListUseDynamicColors))
                    row.setTextColor(R.id.widget_profile_list_item_profile_name, Color.argb(0xFF, red, green, blue));
                else {
                    // must be removed android:textColor in layout
                    int color = GlobalGUIRoutines.getDynamicColor(R.attr.colorOnBackground, context);
                    if (color != 0) {
                        row.setTextColor(R.id.widget_profile_list_item_profile_name, color);
                    }
                }
            }
            if ((!applicationWidgetListHeader) && (profile._checked)) {
                // hm, interesting, how to set bold style for RemoteView text ;-)
                Spannable profileName = DataWrapperStatic.getProfileNameWithManualIndicator(profile, !applicationWidgetListGridLayout,
                                            "", true, true, applicationWidgetListGridLayout, dataWrapper);
                Spannable sb = new SpannableString(profileName);
                sb.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, profileName.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                row.setTextViewText(R.id.widget_profile_list_item_profile_name, sb);
            } else {
                Spannable profileName = profile.getProfileNameWithDuration("", "",
                        true/*applicationWidgetListGridLayout*/, applicationWidgetListGridLayout, context.getApplicationContext());
                row.setTextViewText(R.id.widget_profile_list_item_profile_name, profileName);
            }
            if (!applicationWidgetListGridLayout) {
                if (applicationWidgetListPrefIndicator) {
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

            if ((!applicationWidgetListHeader) &&
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
        return 1;
    }

    public long getItemId(int position) {
        return position;
    }

    public boolean hasStableIds() {
        return false;
    }

    private DataWrapper createProfilesDataWrapper(boolean local,
                                                  String applicationWidgetListIconLightness,
                                                  String applicationWidgetListIconColor,
                                                  boolean applicationWidgetListCustomIconLightness,
                                                  String applicationWidgetListPrefIndicatorLightness,
                                                  boolean applicationWidgetListChangeColorsByNightMode,
                                                  boolean applicationWidgetListUseDynamicColors,
                                                  boolean applicationWidgetListBackgroundType,
                                                  String applicationWidgetListBackgroundColor,
                                                  String applicationWidgetListBackground)
    {
        int monochromeValue = 0xFF;
        switch (applicationWidgetListIconLightness) {
            case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_0:
                monochromeValue = 0x00;
                break;
            case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_12:
                monochromeValue = 0x20;
                break;
            case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_25:
                monochromeValue = 0x40;
                break;
            case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_37:
                monochromeValue = 0x60;
                break;
            case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_50:
                monochromeValue = 0x80;
                break;
            case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_62:
                monochromeValue = 0xA0;
                break;
            case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_75:
                monochromeValue = 0xC0;
                break;
            case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_87:
                monochromeValue = 0xE0;
                break;
            case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_100:
                //noinspection ConstantConditions
                monochromeValue = 0xFF;
                break;
        }

        float prefIndicatorLightnessValue = 0f;
        int prefIndicatorMonochromeValue = 0x00;
        switch (applicationWidgetListPrefIndicatorLightness) {
            case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_0:
                prefIndicatorLightnessValue = -128f;
                //noinspection ConstantConditions
                prefIndicatorMonochromeValue = 0x00;
                break;
            case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_12:
                prefIndicatorLightnessValue = -96f;
                prefIndicatorMonochromeValue = 0x20;
                break;
            case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_25:
                prefIndicatorLightnessValue = -64f;
                prefIndicatorMonochromeValue = 0x40;
                break;
            case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_37:
                prefIndicatorLightnessValue = -32f;
                prefIndicatorMonochromeValue = 0x60;
                break;
            case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_50:
                prefIndicatorLightnessValue = 0f;
                prefIndicatorMonochromeValue = 0x80;
                break;
            case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_62:
                prefIndicatorLightnessValue = 32f;
                prefIndicatorMonochromeValue = 0xA0;
                break;
            case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_75:
                prefIndicatorLightnessValue = 64f;
                prefIndicatorMonochromeValue = 0xC0;
                break;
            case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_87:
                prefIndicatorLightnessValue = 96f;
                prefIndicatorMonochromeValue = 0xE0;
                break;
            case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_100:
                prefIndicatorLightnessValue = 128f;
                prefIndicatorMonochromeValue = 0xFF;
                break;
        }

        int indicatorType;// = DataWrapper.IT_FOR_WIDGET;
        if (applicationWidgetListChangeColorsByNightMode &&
                applicationWidgetListIconColor.equals("0")) {
            if ((Build.VERSION.SDK_INT >= 31) && applicationWidgetListUseDynamicColors)
                indicatorType = DataWrapper.IT_FOR_WIDGET_DYNAMIC_COLORS;
            else
                indicatorType = DataWrapper.IT_FOR_WIDGET_NATIVE_BACKGROUND;
        }
        else
        if (applicationWidgetListBackgroundType) {
            if (ColorUtils.calculateLuminance(Integer.parseInt(applicationWidgetListBackgroundColor)) < 0.23)
                indicatorType = DataWrapper.IT_FOR_WIDGET_DARK_BACKGROUND;
            else
                indicatorType = DataWrapper.IT_FOR_WIDGET_LIGHT_BACKGROUND;
        } else {
            if (Integer.parseInt(applicationWidgetListBackground) <= 37)
                indicatorType = DataWrapper.IT_FOR_WIDGET_DARK_BACKGROUND;
            else
                indicatorType = DataWrapper.IT_FOR_WIDGET_LIGHT_BACKGROUND;
        }

        if (local) {
            return new DataWrapper(context.getApplicationContext(), applicationWidgetListIconColor.equals("1"),
                    monochromeValue, applicationWidgetListCustomIconLightness,
                    indicatorType, prefIndicatorMonochromeValue, prefIndicatorLightnessValue);
        }
        else {
            if (dataWrapper == null) {
                dataWrapper = new DataWrapper(context.getApplicationContext(), applicationWidgetListIconColor.equals("1"),
                        monochromeValue, applicationWidgetListCustomIconLightness,
                        indicatorType, prefIndicatorMonochromeValue, prefIndicatorLightnessValue);
            } else {
                dataWrapper.setParameters(applicationWidgetListIconColor.equals("1"),
                        monochromeValue, applicationWidgetListCustomIconLightness,
                        indicatorType, prefIndicatorMonochromeValue, prefIndicatorLightnessValue);
            }
            return dataWrapper;
        }
    }

    /*
        Called when notifyDataSetChanged() is triggered on the remote adapter. This allows a RemoteViewsFactory to
        respond to data changes by updating any internal references.

        Note: expensive tasks can be safely performed synchronously within this method. In the interim,
        the old data will be displayed within the widget.
    */
    public void onDataSetChanged() {
        String applicationWidgetListIconColor;
        String applicationWidgetListIconLightness;
        boolean applicationWidgetListCustomIconLightness;
        boolean applicationWidgetListPrefIndicator;
        String applicationWidgetListPrefIndicatorLightness;
        boolean applicationWidgetListHeader;
        boolean applicationWidgetListChangeColorsByNightMode;
        boolean applicationWidgetListUseDynamicColors;
        boolean applicationWidgetListBackgroundType;
        String applicationWidgetListBackgroundColor;
        String applicationWidgetListBackground;
        String applicationWidgetListBackgroundColorNightModeOff;
        String applicationWidgetListBackgroundColorNightModeOn;

        synchronized (PPApplication.applicationPreferencesMutex) {
            applicationWidgetListIconLightness = ApplicationPreferences.applicationWidgetListIconLightness;
            applicationWidgetListIconColor = ApplicationPreferences.applicationWidgetListIconColor;
            applicationWidgetListCustomIconLightness = ApplicationPreferences.applicationWidgetListCustomIconLightness;
            applicationWidgetListPrefIndicator = ApplicationPreferences.applicationWidgetListPrefIndicator;
            applicationWidgetListPrefIndicatorLightness = ApplicationPreferences.applicationWidgetListPrefIndicatorLightness;
            applicationWidgetListHeader = ApplicationPreferences.applicationWidgetListHeader;
            applicationWidgetListChangeColorsByNightMode = ApplicationPreferences.applicationWidgetListChangeColorsByNightMode;
            applicationWidgetListUseDynamicColors = ApplicationPreferences.applicationWidgetListUseDynamicColors;
            applicationWidgetListBackgroundType = ApplicationPreferences.applicationWidgetListBackgroundType;
            applicationWidgetListBackgroundColor = ApplicationPreferences.applicationWidgetListBackgroundColor;
            applicationWidgetListBackground = ApplicationPreferences.applicationWidgetListBackground;
            applicationWidgetListBackgroundColorNightModeOff = ApplicationPreferences.applicationWidgetListBackgroundColorNightModeOff;
            applicationWidgetListBackgroundColorNightModeOn = ApplicationPreferences.applicationWidgetListBackgroundColorNightModeOn;

            if (Build.VERSION.SDK_INT >= 30) {
                if (Build.VERSION.SDK_INT < 31)
                    applicationWidgetListUseDynamicColors = false;
                if (applicationWidgetListChangeColorsByNightMode &&
                        (!applicationWidgetListUseDynamicColors)) {
                    int nightModeFlags =
                            context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
                    switch (nightModeFlags) {
                        case Configuration.UI_MODE_NIGHT_YES:
                            //applicationWidgetListIconColor = "0"; // icon type = colorful
                            applicationWidgetListIconLightness = GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_75;
                            applicationWidgetListBackgroundColor = String.valueOf(ColorChooserPreference.parseValue(applicationWidgetListBackgroundColorNightModeOn)); // color of background
                            //applicationWidgetListPrefIndicatorLightness = GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_62; // lightness of preference indicators
                            break;
                        case Configuration.UI_MODE_NIGHT_NO:
                        case Configuration.UI_MODE_NIGHT_UNDEFINED:
                            //applicationWidgetListIconColor = "0"; // icon type = colorful
                            applicationWidgetListIconLightness = GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_62;
                            applicationWidgetListBackgroundColor = String.valueOf(ColorChooserPreference.parseValue(applicationWidgetListBackgroundColorNightModeOff)); // color of background
                            //applicationWidgetListPrefIndicatorLightness = GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_50; // lightness of preference indicators
                            break;
                    }
                }
            }
        }

        DataWrapper _dataWrapper = createProfilesDataWrapper(true,
                                                applicationWidgetListIconLightness,
                                                applicationWidgetListIconColor,
                                                applicationWidgetListCustomIconLightness,
                                                applicationWidgetListPrefIndicatorLightness,
                                                applicationWidgetListChangeColorsByNightMode,
                                                applicationWidgetListUseDynamicColors,
                                                applicationWidgetListBackgroundType,
                                                applicationWidgetListBackgroundColor,
                                                applicationWidgetListBackground);

        List<Profile> newProfileList = _dataWrapper.getNewProfileList(true,
                                                        applicationWidgetListPrefIndicator);
        _dataWrapper.getEventTimelineList(true);

        if (!applicationWidgetListHeader)
        {
            // show activated profile in list if is not showed in activator
            Profile profile = _dataWrapper.getActivatedProfile(newProfileList);
            if ((profile != null) && (!profile._showInActivator))
            {
                profile._showInActivator = true;
                profile._porder = -1;
            }
        }
        newProfileList.sort(new ProfileComparator());

        Profile restartEvents = null;
        if ((!applicationWidgetListHeader) &&
                Event.getGlobalEventsRunning()) {
            //restartEvents = DataWrapper.getNonInitializedProfile(context.getString(R.string.menu_restart_events), "ic_profile_restart_events|1|0|0", 0);
            restartEvents = DataWrapperStatic.getNonInitializedProfile(context.getString(R.string.menu_restart_events),
                    "ic_profile_restart_events|1|1|"+ApplicationPreferences.applicationRestartEventsIconColor, 0);
            restartEvents._showInActivator = true;
            newProfileList.add(0, restartEvents);
        }
        _dataWrapper.invalidateDataWrapper();

        if (dataWrapper != null)
            dataWrapper.invalidateDataWrapper();
        createProfilesDataWrapper(false,
                                    applicationWidgetListIconLightness,
                                    applicationWidgetListIconColor,
                                    applicationWidgetListCustomIconLightness,
                                    applicationWidgetListPrefIndicatorLightness,
                                    applicationWidgetListChangeColorsByNightMode,
                                    applicationWidgetListUseDynamicColors,
                                    applicationWidgetListBackgroundType,
                                    applicationWidgetListBackgroundColor,
                                    applicationWidgetListBackground);
        //if (dataWrapper != null) {
            //dataWrapper.invalidateProfileList();
            if (restartEvents != null)
                dataWrapper.generateProfileIcon(restartEvents, true, false);
            dataWrapper.setProfileList(newProfileList);
            //profileList = newProfileList;
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
