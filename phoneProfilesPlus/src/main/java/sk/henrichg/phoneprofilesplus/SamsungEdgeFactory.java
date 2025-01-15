package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.util.TypedValue;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import androidx.core.graphics.ColorUtils;

import java.util.Comparator;
import java.util.List;

/** @noinspection ExtractMethodRecommender*/
class SamsungEdgeFactory implements RemoteViewsService.RemoteViewsFactory {

    private DataWrapper dataWrapper;

    private final Context context;
    //private int appWidgetId;
    //private List<Profile> profileList = new ArrayList<>();

    SamsungEdgeFactory(Context context,
                       @SuppressWarnings("unused") Intent intent) {
        this.context = context;
        /*appWidgetId=intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                                       AppWidgetManager.INVALID_APPWIDGET_ID);*/
    }
  
    private DataWrapper createProfilesDataWrapper(boolean local)
    {
        String applicationSamsungEdgeIconColor;
        String applicationSamsungEdgeIconLightness;
        boolean applicationSamsungEdgeCustomIconLightness;
        boolean applicationSamsungEdgeChangeColorsByNightMode;
        //String applicationSamsungEdgeBackgroundColorNightModeOff;
        //String applicationSamsungEdgeBackgroundColorNightModeOn;

//        PPApplicationStatic.logE("[SYNCHRONIZED] SamsungEdgeFactory.createProfilesDataWrapper", "PPApplication.applicationPreferencesMutex");
        synchronized (PPApplication.applicationPreferencesMutex) {
            applicationSamsungEdgeIconColor = ApplicationPreferences.applicationSamsungEdgeIconColor;
            applicationSamsungEdgeIconLightness = ApplicationPreferences.applicationSamsungEdgeIconLightness;
            applicationSamsungEdgeCustomIconLightness = ApplicationPreferences.applicationSamsungEdgeCustomIconLightness;

            if (Build.VERSION.SDK_INT < 30)
                applicationSamsungEdgeChangeColorsByNightMode = false;
            else
                applicationSamsungEdgeChangeColorsByNightMode = ApplicationPreferences.applicationSamsungEdgeChangeColorsByNightMode;

            //applicationSamsungEdgeBackgroundColorNightModeOff = ApplicationPreferences.applicationSamsungEdgeBackgroundColorNightModeOff;
            //applicationSamsungEdgeBackgroundColorNightModeOn = ApplicationPreferences.applicationSamsungEdgeBackgroundColorNightModeOn;

            if (Build.VERSION.SDK_INT >= 30) {
                if (applicationSamsungEdgeChangeColorsByNightMode) {
                    if (GlobalGUIRoutines.isNightModeEnabled(context.getApplicationContext()))
                        applicationSamsungEdgeIconLightness = GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_75;
                    else
                        applicationSamsungEdgeIconLightness = GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_62;
                    //int nightModeFlags =
                    //        context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
                    //switch (nightModeFlags) {
                    //    case Configuration.UI_MODE_NIGHT_YES:
                    //        applicationSamsungEdgeIconLightness = GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_75;
                    //        break;
                    //    case Configuration.UI_MODE_NIGHT_NO:
                    //    case Configuration.UI_MODE_NIGHT_UNDEFINED:
                    //        applicationSamsungEdgeIconLightness = GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_62;
                    //        break;
                    //}
                }
            }
        }

        int monochromeValue = 0xFF;
        if (applicationSamsungEdgeIconLightness.equals(GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_0)) monochromeValue = 0x00;
        if (applicationSamsungEdgeIconLightness.equals(GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_12)) monochromeValue = 0x20;
        if (applicationSamsungEdgeIconLightness.equals(GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_25)) monochromeValue = 0x40;
        if (applicationSamsungEdgeIconLightness.equals(GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_37)) monochromeValue = 0x60;
        if (applicationSamsungEdgeIconLightness.equals(GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_50)) monochromeValue = 0x80;
        if (applicationSamsungEdgeIconLightness.equals(GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_62)) monochromeValue = 0xA0;
        if (applicationSamsungEdgeIconLightness.equals(GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_75)) monochromeValue = 0xC0;
        if (applicationSamsungEdgeIconLightness.equals(GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_87)) monochromeValue = 0xE0;
        //if (applicationSamsungEdgeIconLightness.equals(GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_100)) monochromeValue = 0xFF;

        if (local) {
            return new DataWrapper(context.getApplicationContext(), applicationSamsungEdgeIconColor.equals("1"),
                    monochromeValue, applicationSamsungEdgeCustomIconLightness,
                    DataWrapper.IT_FOR_WIDGET, 0, 0f);
        }
        else {
            if (dataWrapper == null) {
                dataWrapper = new DataWrapper(context.getApplicationContext(), applicationSamsungEdgeIconColor.equals("1"),
                        monochromeValue, applicationSamsungEdgeCustomIconLightness,
                        DataWrapper.IT_FOR_WIDGET, 0, 0f);
            } else {
                dataWrapper.setParameters(applicationSamsungEdgeIconColor.equals("1"),
                        monochromeValue, applicationSamsungEdgeCustomIconLightness,
                        DataWrapper.IT_FOR_WIDGET, 0, 0f);
            }
            return dataWrapper;
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
//        PPApplicationStatic.logE("[SYNCHRONIZED] SamsungEdgeFactory.getCount", "PPApplication.samsungEdgeDatasetChangedMutex");
        synchronized (PPApplication.samsungEdgeDatasetChangedMutex) {
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
    }

    private Profile getItem(int position)
    {
        if (getCount() == 0)
            return null;
        else
        {
//            PPApplicationStatic.logE("[SYNCHRONIZED] SamsungEdgeFactory.getItem", "PPApplication.samsungEdgeDatasetChangedMutex");
            synchronized (PPApplication.samsungEdgeDatasetChangedMutex) {
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
    }

    public RemoteViews getViewAt(int position) {
//        PPApplicationStatic.logE("[SYNCHRONIZED] SamsungEdgeFactory.getViewAt", "PPApplication.samsungEdgeDatasetChangedMutex");
        synchronized (PPApplication.samsungEdgeDatasetChangedMutex) {
            Context appContext = context.getApplicationContext();
            LocaleHelper.setApplicationLocale(appContext);

            RemoteViews row;
            //if (!applicationSamsungEdgeGridLayout)
            //    row=new RemoteViews(context.PPApplication.PACKAGE_NAME, R.layout.samsung_edge_widget_item);
            //else
            row = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.samsung_edge_listitem);

            Profile profile = getItem(position);

            if (profile != null) {
                String applicationSamsungEdgeLightnessT;
                boolean applicationSamsungEdgeHeader;
                boolean applicationSamsungEdgeChangeColorsByNightMode;
                String applicationSamsungEdgeIconColor;
                boolean applicationSamsungEdgeBackgroundType;
                String applicationSamsungEdgeLightnessB;
                String applicationSamsungEdgeBackgroundColor;
                String applicationSamsungEdgeBackgroundColorNightModeOff;
                String applicationSamsungEdgeBackgroundColorNightModeOn;

//                PPApplicationStatic.logE("[SYNCHRONIZED] SamsungEdgeFactory.getViewAt", "PPApplication.applicationPreferencesMutex");
                synchronized (PPApplication.applicationPreferencesMutex) {
                    applicationSamsungEdgeLightnessT = ApplicationPreferences.applicationSamsungEdgeLightnessT;
                    applicationSamsungEdgeHeader = ApplicationPreferences.applicationSamsungEdgeHeader;

                    if (Build.VERSION.SDK_INT < 30)
                        applicationSamsungEdgeChangeColorsByNightMode = false;
                    else
                        applicationSamsungEdgeChangeColorsByNightMode = ApplicationPreferences.applicationSamsungEdgeChangeColorsByNightMode;

                    applicationSamsungEdgeIconColor = ApplicationPreferences.applicationSamsungEdgeIconColor;
                    applicationSamsungEdgeBackgroundType = ApplicationPreferences.applicationSamsungEdgeBackgroundType;
                    applicationSamsungEdgeLightnessB = ApplicationPreferences.applicationSamsungEdgeLightnessB;
                    applicationSamsungEdgeBackgroundColor = ApplicationPreferences.applicationSamsungEdgeBackgroundColor;
                    applicationSamsungEdgeBackgroundColorNightModeOff = ApplicationPreferences.applicationSamsungEdgeBackgroundColorNightModeOff;
                    applicationSamsungEdgeBackgroundColorNightModeOn = ApplicationPreferences.applicationSamsungEdgeBackgroundColorNightModeOn;

                    if (Build.VERSION.SDK_INT >= 30) {
                        if (applicationSamsungEdgeChangeColorsByNightMode) {
                            boolean nightModeOn = GlobalGUIRoutines.isNightModeEnabled(appContext);
                            //int nightModeFlags =
                            //        context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
                            //switch (nightModeFlags) {
                            if (nightModeOn) {
                                //case Configuration.UI_MODE_NIGHT_YES:
                                //applicationSamsungEdgeLightnessT = GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_87; // lightness of text = white
                                applicationSamsungEdgeBackgroundType = true; // background type = color
                                applicationSamsungEdgeBackgroundColor = String.valueOf(ColorChooserPreference.parseValue(applicationSamsungEdgeBackgroundColorNightModeOn)); // color of background
                                //applicationSamsungEdgeLightnessB = GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_12;  // lighting  of backgroud = 12%
                                //break;
                            } else {
                                //case Configuration.UI_MODE_NIGHT_NO:
                                //case Configuration.UI_MODE_NIGHT_UNDEFINED:
                                //applicationSamsungEdgeLightnessT = GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_12; // lightness of text = black
                                applicationSamsungEdgeBackgroundType = true; // background type = not color
                                applicationSamsungEdgeBackgroundColor = String.valueOf(ColorChooserPreference.parseValue(applicationSamsungEdgeBackgroundColorNightModeOff)); // color of background
                                //applicationSamsungEdgeLightnessB = GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_87; // lighting  of backgroud = 87%
                                //break;
                            }
                        }
                    }
                }

                Bitmap bitmap = null;
                if (applicationSamsungEdgeIconColor.equals("0")) {
                    if (profile.getIsIconResourceID()) {
                        if (applicationSamsungEdgeChangeColorsByNightMode ||
                                ((!applicationSamsungEdgeBackgroundType) &&
                                        (Integer.parseInt(applicationSamsungEdgeLightnessB) <= 25)) ||
                                (applicationSamsungEdgeBackgroundType &&
                                        (ColorUtils.calculateLuminance(Integer.parseInt(applicationSamsungEdgeBackgroundColor)) < 0.23)))
                            bitmap = profile.increaseProfileIconBrightnessForContext(appContext, profile._iconBitmap);
                    } else
                        bitmap = profile._iconBitmap;
                }
                if (profile.getIsIconResourceID()) {
                    if (bitmap != null)
                        row.setImageViewBitmap(R.id.widget_samsung_edge_item_profile_icon, bitmap);
                    else {
                        if (profile._iconBitmap != null)
                            row.setImageViewBitmap(R.id.widget_samsung_edge_item_profile_icon, profile._iconBitmap);
                        else
                            row.setImageViewResource(R.id.widget_samsung_edge_item_profile_icon,
                                    /*context.getResources().getIdentifier(profile.getIconIdentifier(), "drawable", context.PPApplication.PACKAGE_NAME));*/
                                    ProfileStatic.getIconResource(profile.getIconIdentifier()));
                    }
                } else {
                    if (bitmap != null)
                        row.setImageViewBitmap(R.id.widget_samsung_edge_item_profile_icon, bitmap);
                    else
                        row.setImageViewBitmap(R.id.widget_samsung_edge_item_profile_icon, profile._iconBitmap);
                }
                int red = 0xFF;
                int green;
                int blue;
                switch (applicationSamsungEdgeLightnessT) {
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
                if (!applicationSamsungEdgeHeader) {
                    if (profile._checked) {
                        row.setTextViewTextSize(R.id.widget_samsung_edge_item_profile_name, TypedValue.COMPLEX_UNIT_DIP, 15);

                        //if (PPApplication.applicationWidgetListIconColor.equals("1"))
                        row.setTextColor(R.id.widget_samsung_edge_item_profile_name, Color.argb(0xFF, red, green, blue));
                        //else
                        //	row.setTextColor(R.id.widget_samsung_edge_item_profile_name, Color.parseColor("#33b5e5"));
                    } else {
                        row.setTextViewTextSize(R.id.widget_samsung_edge_item_profile_name, TypedValue.COMPLEX_UNIT_DIP, 15);

                        //if (PPApplication.applicationWidgetListIconColor.equals("1"))
                        row.setTextColor(R.id.widget_samsung_edge_item_profile_name, Color.argb(0xCC, red, green, blue));
                        //else
                        //	row.setTextColor(R.id.widget_samsung_edge_item_profile_name, Color.argb(0xFF, red, green, blue));
                    }
                } else {
                    row.setTextViewTextSize(R.id.widget_samsung_edge_item_profile_name, TypedValue.COMPLEX_UNIT_DIP, 15);

                    row.setTextColor(R.id.widget_samsung_edge_item_profile_name, Color.argb(0xFF, red, green, blue));
                }
                if ((!applicationSamsungEdgeHeader) && (profile._checked)) {
                    // hm, interesting, how to set bold style for RemoteView text ;-)
                    Spannable profileName = DataWrapperStatic.getProfileNameWithManualIndicator(profile, false, "", true, true, true, dataWrapper);
                    Spannable sb = new SpannableString(profileName);
                    sb.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, profileName.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    row.setTextViewText(R.id.widget_samsung_edge_item_profile_name, sb);
                } else {
                    Spannable profileName = profile.getProfileNameWithDuration("", "", true, false, appContext);
                    row.setTextViewText(R.id.widget_samsung_edge_item_profile_name, profileName);
                }
            /*if (!applicationSamsungEdgeGridLayout) {
                if (applicationSamsungEdgePrefIndicator) {
                    if (profile._preferencesIndicator != null)
                        row.setImageViewBitmap(R.id.widget_samsung_edge_profile_pref_indicator, profile._preferencesIndicator);
                    else
                        row.setImageViewResource(R.id.widget_samsung_edge_profile_pref_indicator, R.drawable.ic_empty);
                }
                else
                    row.setImageViewResource(R.id.widget_samsung_edge_profile_pref_indicator, R.drawable.ic_empty);
            }*/

                Intent i = new Intent();
                Bundle extras = new Bundle();

                if (EventStatic.getGlobalEventsRunning(appContext) && (position == 0))
                    extras.putLong(PPApplication.EXTRA_PROFILE_ID, Profile.RESTART_EVENTS_PROFILE_ID);
                else
                    extras.putLong(PPApplication.EXTRA_PROFILE_ID, profile._id);
                extras.putInt(PPApplication.EXTRA_STARTUP_SOURCE, PPApplication.STARTUP_SOURCE_SHORTCUT);
                i.putExtras(extras);
                row.setOnClickFillInIntent(R.id.widget_samsung_edge_item, i);

            }

            return (row);
        }
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
        return false;
    }

    public void onDataSetChanged() {
        DataWrapper _dataWrapper = createProfilesDataWrapper(true);

        //List<Profile> newProfileList = _dataWrapper.getNewProfileList(true, false);
        List<Profile> newProfileList = _dataWrapper.getNewProfileList(false, false);

        _dataWrapper.getEventTimelineList(true);

        boolean applicationSamsungEdgeHeader;
//        PPApplicationStatic.logE("[SYNCHRONIZED] SamsungEdgeFactory.onDataSetChanged", "PPApplication.applicationPreferencesMutex");
        synchronized (PPApplication.applicationPreferencesMutex) {
            applicationSamsungEdgeHeader = ApplicationPreferences.applicationSamsungEdgeHeader;
        }

        if (!applicationSamsungEdgeHeader)
        {
            // show activated profile in list if is not showed in activator
            Profile profile = _dataWrapper.getActivatedProfile(newProfileList);
            if ((profile != null) && (!profile._showInActivator))
            {
                profile._showInActivator = true;
                profile._porder = -1;
            }
        }

        for (Profile profile : newProfileList) {
            if (profile._showInActivator) {
                _dataWrapper.generateProfileIcon(profile, true, false);
            }
        }

        newProfileList.sort(new ProfileComparator());

        Profile restartEvents = null;
        if (EventStatic.getGlobalEventsRunning(context)) {
            //restartEvents = DataWrapper.getNonInitializedProfile(context.getString(R.string.menu_restart_events), "ic_profile_restart_events|1|0|0", 0);
            restartEvents = DataWrapperStatic.getNonInitializedProfile(context.getString(R.string.menu_restart_events),
                    StringConstants.PROFILE_ICON_RESTART_EVENTS+"|1|1|"+ApplicationPreferences.applicationRestartEventsIconColor, 0);
            restartEvents._showInActivator = true;
            newProfileList.add(0, restartEvents);
        }
        _dataWrapper.invalidateDataWrapper();

//        PPApplicationStatic.logE("[SYNCHRONIZED] SamsungEdgeFactory.onDataSetChanged", "PPApplication.samsungEdgeDatasetChangedMutex");
        synchronized (PPApplication.samsungEdgeDatasetChangedMutex) {

            if (dataWrapper != null)
                dataWrapper.invalidateDataWrapper();
            createProfilesDataWrapper(false);
            //dataWrapper.invalidateProfileList();
            if (restartEvents != null)
                dataWrapper.generateProfileIcon(restartEvents, true, false);
            dataWrapper.setProfileList(newProfileList);
            //profileList = newProfileList;
        }
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
