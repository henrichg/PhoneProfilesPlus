package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
class PanelWidgetFactory implements RemoteViewsService.RemoteViewsFactory {

    private DataWrapper dataWrapper;

    private final Context context;
    //private int appWidgetId;
    //private List<Profile> profileList = new ArrayList<>();

    PanelWidgetFactory(Context context,
                       @SuppressWarnings("unused") Intent intent) {
        this.context = context;
        /*appWidgetId=intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                                       AppWidgetManager.INVALID_APPWIDGET_ID);*/
    }

    public void onCreate() {
        //Log.e("PanelWidgetFactory.onCreate", "xxxx");
    }

    public void onDestroy() {
        /*if (dataWrapper != null)
            dataWrapper.invalidateDataWrapper();
        dataWrapper = null;*/
    }

    public int getCount() {
//        PPApplicationStatic.logE("[SYNCHRONIZED] PanelWidgetFactory.getCount", "PPApplication.panelWidgetDatasetChangedMutex");
        synchronized (PPApplication.panelWidgetDatasetChangedMutex) {
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
//            PPApplicationStatic.logE("[SYNCHRONIZED] PanelWidgetFactory.getItem", "PPApplication.panelWidgetDatasetChangedMutex");
            synchronized (PPApplication.panelWidgetDatasetChangedMutex) {
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
//        PPApplicationStatic.logE("[SYNCHRONIZED] PanelWidgetFactory.getViewAt", "PPApplication.panelWidgetDatasetChangedMutex");
        synchronized (PPApplication.panelWidgetDatasetChangedMutex) {
            Context appContext = context.getApplicationContext();
            LocaleHelper.setApplicationLocale(appContext);

            RemoteViews row;

            String applicationWidgetPanelLightnessT;
            boolean applicationWidgetPanelHeader;
            //boolean applicationWidgetPanelPrefIndicator;
            boolean applicationWidgetPanelChangeColorsByNightMode;
            String applicationWidgetPanelIconColor;
            boolean applicationWidgetPanelUseDynamicColors;
            boolean applicationWidgetPanelBackgroundType;
            String applicationWidgetPanelLightnessB;
            String applicationWidgetPanelBackgroundColor;
            String applicationWidgetPanelBackgroundColorNightModeOff;
            String applicationWidgetPanelBackgroundColorNightModeOn;

//            PPApplicationStatic.logE("[SYNCHRONIZED] ProfilePanelWidgetFactory.getViewAt", "PPApplication.applicationPreferencesMutex");
            synchronized (PPApplication.applicationPreferencesMutex) {
                applicationWidgetPanelLightnessT = ApplicationPreferences.applicationWidgetPanelLightnessT;
                applicationWidgetPanelHeader = ApplicationPreferences.applicationWidgetPanelHeader;
                //applicationWidgetPanelPrefIndicator = ApplicationPreferences.applicationWidgetPanelPrefIndicator;

                if (Build.VERSION.SDK_INT < 30) {
                    applicationWidgetPanelChangeColorsByNightMode = false;
                    //applicationWidgetPanelPrefIndicatorUseDynamicColor = false;
                }
                else
                    applicationWidgetPanelChangeColorsByNightMode = ApplicationPreferences.applicationWidgetPanelChangeColorsByNightMode;

                applicationWidgetPanelIconColor = ApplicationPreferences.applicationWidgetPanelIconColor;
                applicationWidgetPanelUseDynamicColors = ApplicationPreferences.applicationWidgetPanelUseDynamicColors;
                applicationWidgetPanelBackgroundType = ApplicationPreferences.applicationWidgetPanelBackgroundType;
                applicationWidgetPanelLightnessB = ApplicationPreferences.applicationWidgetPanelLightnessB;
                applicationWidgetPanelBackgroundColor = ApplicationPreferences.applicationWidgetPanelBackgroundColor;
                applicationWidgetPanelBackgroundColorNightModeOff = ApplicationPreferences.applicationWidgetPanelBackgroundColorNightModeOff;
                applicationWidgetPanelBackgroundColorNightModeOn = ApplicationPreferences.applicationWidgetPanelBackgroundColorNightModeOn;

                if (Build.VERSION.SDK_INT >= 30) {
                    if (Build.VERSION.SDK_INT < 31)
                        applicationWidgetPanelUseDynamicColors = false;
                    if (//PPApplication.isPixelLauncherDefault(context) ||
                            (applicationWidgetPanelChangeColorsByNightMode &&
                                    (!applicationWidgetPanelUseDynamicColors))) {
                        boolean nightModeOn = GlobalGUIRoutines.isNightModeEnabled(appContext);
                        //int nightModeFlags =
                        //        context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
                        //switch (nightModeFlags) {
                        if (nightModeOn) {
                            //case Configuration.UI_MODE_NIGHT_YES:

                            //applicationWidgetPanelLightnessT = GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_87; // lightness of text = white
                            applicationWidgetPanelBackgroundType = true; // background type = color
                            applicationWidgetPanelBackgroundColor = String.valueOf(ColorChooserPreference.parseValue(applicationWidgetPanelBackgroundColorNightModeOn)); // color of background
                            //break;
                        } else {
                            //case Configuration.UI_MODE_NIGHT_NO:
                            //case Configuration.UI_MODE_NIGHT_UNDEFINED:

                            //applicationWidgetPanelLightnessT = GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_12; // lightness of text = black
                            applicationWidgetPanelBackgroundType = true; // background type = color
                            applicationWidgetPanelBackgroundColor = String.valueOf(ColorChooserPreference.parseValue(applicationWidgetPanelBackgroundColorNightModeOff)); // color of background
                            //break;
                        }
                    }
                }
            }

            if (!((Build.VERSION.SDK_INT >= 31) && applicationWidgetPanelChangeColorsByNightMode &&
                    applicationWidgetPanelIconColor.equals("0") && applicationWidgetPanelUseDynamicColors))
                row = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_panel_listitem);
            else
                row = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_panel_listitem_dn);

            Profile profile = getItem(position);

            if (profile != null) {
//                PPApplicationStatic.logE("[SYNCHRONIZED] PanelWidgetFactory.getViewAt", "PPApplication.applicationPreferencesMutex");

                Bitmap bitmap = null;
                if (applicationWidgetPanelIconColor.equals("0")) {
                    if (profile.getIsIconResourceID()) {
                        if (applicationWidgetPanelChangeColorsByNightMode ||
                                ((!applicationWidgetPanelBackgroundType) &&
                                        (Integer.parseInt(applicationWidgetPanelLightnessB) <= 25)) ||
                                (applicationWidgetPanelBackgroundType &&
                                        (ColorUtils.calculateLuminance(Integer.parseInt(applicationWidgetPanelBackgroundColor)) < 0.23)))
                            bitmap = profile.increaseProfileIconBrightnessForContext(appContext, profile._iconBitmap);
                    } else
                        bitmap = profile._iconBitmap;
                }
                if (profile.getIsIconResourceID()) {
                    if (bitmap != null)
                        row.setImageViewBitmap(R.id.widget_panel_item_profile_icon, bitmap);
                    else {
                        if (profile._iconBitmap != null)
                            row.setImageViewBitmap(R.id.widget_panel_item_profile_icon, profile._iconBitmap);
                        else
                            row.setImageViewResource(R.id.widget_panel_item_profile_icon,
                                    /*context.getResources().getIdentifier(profile.getIconIdentifier(), "drawable", context.PPApplication.PACKAGE_NAME));*/
                                    ProfileStatic.getIconResource(profile.getIconIdentifier()));
                    }
                } else {
                    if (bitmap != null)
                        row.setImageViewBitmap(R.id.widget_panel_item_profile_icon, bitmap);
                    else
                        row.setImageViewBitmap(R.id.widget_panel_item_profile_icon, profile._iconBitmap);
                }
                int red = 0xFF;
                int green;
                int blue;
                switch (applicationWidgetPanelLightnessT) {
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
                if (!applicationWidgetPanelHeader) {
                    if (profile._checked) {
                        row.setTextViewTextSize(R.id.widget_panel_item_profile_name, TypedValue.COMPLEX_UNIT_DIP, 15);

                        if (!((Build.VERSION.SDK_INT >= 31) && applicationWidgetPanelChangeColorsByNightMode &&
                                applicationWidgetPanelIconColor.equals("0") && applicationWidgetPanelUseDynamicColors))
                            row.setTextColor(R.id.widget_panel_item_profile_name, Color.argb(0xFF, red, green, blue));
                        else {
                            // must be removed android:textColor in layout
                            int color = GlobalGUIRoutines.getDynamicColor(R.attr.colorOnBackground, appContext);
                            if (color != 0) {
                                row.setTextColor(R.id.widget_panel_item_profile_name, color);
                            }
                        }
                    } else {
                        row.setTextViewTextSize(R.id.widget_panel_item_profile_name, TypedValue.COMPLEX_UNIT_DIP, 15);

                        if (!((Build.VERSION.SDK_INT >= 31) && applicationWidgetPanelChangeColorsByNightMode &&
                                applicationWidgetPanelIconColor.equals("0") && applicationWidgetPanelUseDynamicColors))
                            row.setTextColor(R.id.widget_panel_item_profile_name, Color.argb(0xCC, red, green, blue));
                        else {
                            // must be removed android:textColor in layout
                            int color = GlobalGUIRoutines.getDynamicColor(R.attr.colorOnBackground, appContext);
                            if (color != 0) {
                                row.setTextColor(R.id.widget_panel_item_profile_name,
                                        Color.argb(0xCC, Color.red(color), Color.green(color), Color.blue(color)));
                            }
                        }
                    }
                } else {
                    row.setTextViewTextSize(R.id.widget_panel_item_profile_name, TypedValue.COMPLEX_UNIT_DIP, 15);

                    if (!((Build.VERSION.SDK_INT >= 31) && applicationWidgetPanelChangeColorsByNightMode &&
                            applicationWidgetPanelIconColor.equals("0") && applicationWidgetPanelUseDynamicColors))
                        row.setTextColor(R.id.widget_panel_item_profile_name, Color.argb(0xFF, red, green, blue));
                    else {
                        // must be removed android:textColor in layout
                        int color = GlobalGUIRoutines.getDynamicColor(R.attr.colorOnBackground, appContext);
                        if (color != 0) {
                            row.setTextColor(R.id.widget_panel_item_profile_name, color);
                        }
                    }
                }
                if ((!applicationWidgetPanelHeader) && (profile._checked)) {
                    // hm, interesting, how to set bold style for RemoteView text ;-)
                    Spannable profileName = DataWrapperStatic.getProfileNameWithManualIndicator(profile, false, "", true, true, true, true, dataWrapper);
                    Spannable sb = new SpannableString(profileName);
                    sb.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, profileName.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    row.setTextViewText(R.id.widget_panel_item_profile_name, sb);
                } else {
                    Spannable profileName = profile.getProfileNameWithDuration("", "", true, false, appContext);
                    row.setTextViewText(R.id.widget_panel_item_profile_name, profileName);
                }
            /*if (!applicationWidgetPanelGridLayout) {
                if (applicationWidgetPanelPrefIndicator) {
                    if (profile._preferencesIndicator != null)
                        row.setImageViewBitmap(R.id.widget_panel_profile_pref_indicator, profile._preferencesIndicator);
                    else
                        row.setImageViewResource(R.id.widget_panel_profile_pref_indicator, R.drawable.ic_empty);
                }
                else
                    row.setImageViewResource(R.id.widget_panel_profile_pref_indicator, R.drawable.ic_empty);
            }*/

                Intent i = new Intent();
                Bundle extras = new Bundle();

                if (EventStatic.getGlobalEventsRunning(appContext) && (position == 0))
                    extras.putLong(PPApplication.EXTRA_PROFILE_ID, Profile.RESTART_EVENTS_PROFILE_ID);
                else
                    extras.putLong(PPApplication.EXTRA_PROFILE_ID, profile._id);
                extras.putInt(PPApplication.EXTRA_STARTUP_SOURCE, PPApplication.STARTUP_SOURCE_SHORTCUT);
                i.putExtras(extras);
                row.setOnClickFillInIntent(R.id.widget_panel_item, i);

            }

            return (row);
        }
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
                                                  String applicationWidgetPanelIconLightness,
                                                  String applicationWidgetPanelIconColor,
                                                  boolean applicationWidgetPanelCustomIconLightness)
    {
        int monochromeValue = 0xFF;
        switch (applicationWidgetPanelIconLightness) {
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

        if (local) {
            return new DataWrapper(context.getApplicationContext(), applicationWidgetPanelIconColor.equals("1"),
                    monochromeValue, applicationWidgetPanelCustomIconLightness,
                    DataWrapper.IT_FOR_WIDGET, 0, 0f);
        }
        else {
            if (dataWrapper == null) {
                dataWrapper = new DataWrapper(context.getApplicationContext(), applicationWidgetPanelIconColor.equals("1"),
                        monochromeValue, applicationWidgetPanelCustomIconLightness,
                        DataWrapper.IT_FOR_WIDGET, 0, 0f);
            } else {
                dataWrapper.setParameters(applicationWidgetPanelIconColor.equals("1"),
                        monochromeValue, applicationWidgetPanelCustomIconLightness,
                        DataWrapper.IT_FOR_WIDGET, 0, 0f);
            }
            return dataWrapper;
        }
    }

    public void onDataSetChanged() {
        String applicationWidgetPanelIconColor;
        String applicationWidgetPanelIconLightness;
        boolean applicationWidgetPanelCustomIconLightness;
        boolean applicationWidgetPanelHeader;
        boolean applicationWidgetPanelChangeColorsByNightMode;
        boolean applicationWidgetPanelIconLightnessChangeByNightMode;

//        PPApplicationStatic.logE("[SYNCHRONIZED] PanelWidgetFactory.onDataSetChanged", "PPApplication.applicationPreferencesMutex");
        synchronized (PPApplication.applicationPreferencesMutex) {
            applicationWidgetPanelIconLightness = ApplicationPreferences.applicationWidgetPanelIconLightness;
            applicationWidgetPanelIconColor = ApplicationPreferences.applicationWidgetPanelIconColor;
            applicationWidgetPanelCustomIconLightness = ApplicationPreferences.applicationWidgetPanelCustomIconLightness;
            applicationWidgetPanelHeader = ApplicationPreferences.applicationWidgetPanelHeader;
            applicationWidgetPanelIconLightnessChangeByNightMode = ApplicationPreferences.applicationWidgetPanelIconLightnessChangeByNightMode;

            if (Build.VERSION.SDK_INT < 30)
                applicationWidgetPanelChangeColorsByNightMode = false;
            else
                applicationWidgetPanelChangeColorsByNightMode = ApplicationPreferences.applicationWidgetPanelChangeColorsByNightMode;

            if (Build.VERSION.SDK_INT >= 30) {
                if (applicationWidgetPanelChangeColorsByNightMode) {
                    boolean nightModeOn = GlobalGUIRoutines.isNightModeEnabled(context.getApplicationContext());
                    //int nightModeFlags =
                    //        context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
                    //switch (nightModeFlags) {
                    if (nightModeOn) {
                        //case Configuration.UI_MODE_NIGHT_YES:

                        //applicationWidgetPanelIconLightness = GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_75;
                        if (applicationWidgetPanelIconLightnessChangeByNightMode) {
                            switch (applicationWidgetPanelIconLightness) {
                                case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_0:
                                    applicationWidgetPanelIconLightness = GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_100;
                                    break;
                                case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_12:
                                    applicationWidgetPanelIconLightness = GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_87;
                                    break;
                                case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_25:
                                    applicationWidgetPanelIconLightness = GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_75;
                                    break;
                                case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_37:
                                    applicationWidgetPanelIconLightness = GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_62;
                                    break;
                            }
                            SharedPreferences.Editor editor = ApplicationPreferences.getEditor(context.getApplicationContext());
                            editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_PANEL_ICON_LIGHTNESS, applicationWidgetPanelIconLightness);
                            editor.apply();
                            ApplicationPreferences.applicationWidgetIconLightness = applicationWidgetPanelIconLightness;
                        }
                    } else {
                        //case Configuration.UI_MODE_NIGHT_NO:
                        //case Configuration.UI_MODE_NIGHT_UNDEFINED:

                        //applicationWidgetIconLightness = GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_62;
                        if (applicationWidgetPanelIconLightnessChangeByNightMode) {
                            switch (applicationWidgetPanelIconLightness) {
                                case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_62:
                                    applicationWidgetPanelIconLightness = GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_37;
                                    break;
                                case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_75:
                                    applicationWidgetPanelIconLightness = GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_25;
                                    break;
                                case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_87:
                                    applicationWidgetPanelIconLightness = GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_12;
                                    break;
                                case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_100:
                                    applicationWidgetPanelIconLightness = GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_0;
                                    break;
                            }
                            SharedPreferences.Editor editor = ApplicationPreferences.getEditor(context.getApplicationContext());
                            editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_PANEL_ICON_LIGHTNESS, applicationWidgetPanelIconLightness);
                            editor.apply();
                            ApplicationPreferences.applicationWidgetPanelIconLightness = applicationWidgetPanelIconLightness;
                        }
                    }
                }
            }
        }

        //Log.e("PanelWidgetFactory.onDataSetChanged", "applicationWidgetListHeader="+applicationWidgetListHeader);

        DataWrapper _dataWrapper = createProfilesDataWrapper(true,
                applicationWidgetPanelIconLightness,
                applicationWidgetPanelIconColor,
                applicationWidgetPanelCustomIconLightness);

        //List<Profile> newProfileList = _dataWrapper.getNewProfileList(true, applicationWidgetListPrefIndicator);
        List<Profile> newProfileList = _dataWrapper.getNewProfileList(false, false);

        _dataWrapper.getEventTimelineList(true);

        if (!applicationWidgetPanelHeader)
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

        newProfileList.sort(new PanelWidgetFactory.ProfileComparator());

        Profile restartEvents = null;
        if (/*(!applicationWidgetPanelHeader) &&*/
                EventStatic.getGlobalEventsRunning(context)) {
            //restartEvents = DataWrapper.getNonInitializedProfile(context.getString(R.string.menu_restart_events), "ic_profile_restart_events|1|0|0", 0);
            restartEvents = DataWrapperStatic.getNonInitializedProfile(context.getString(R.string.menu_restart_events),
                    StringConstants.PROFILE_ICON_RESTART_EVENTS+"|1|1|"+ApplicationPreferences.applicationRestartEventsIconColor, 0);

            restartEvents._showInActivator = true;
            newProfileList.add(0, restartEvents);
        }
        _dataWrapper.invalidateDataWrapper();

//        PPApplicationStatic.logE("[SYNCHRONIZED] PanelWidgetFactory.onDataSetChanged", "PPApplication.profileListWidgetDatasetChangedMutex");
        synchronized (PPApplication.panelWidgetDatasetChangedMutex) {

            if (dataWrapper != null)
                dataWrapper.invalidateDataWrapper();
            createProfilesDataWrapper(false,
                    applicationWidgetPanelIconLightness,
                    applicationWidgetPanelIconColor,
                    applicationWidgetPanelCustomIconLightness);
            //if (dataWrapper != null) {
            //dataWrapper.invalidateProfileList();
            if (restartEvents != null)
                dataWrapper.generateProfileIcon(restartEvents, true, false);
            dataWrapper.setProfileList(newProfileList);
            //profileList = newProfileList;
            //}

        }

        //Log.e("PanelWidgetFactory.onDataSetChanged", "END");
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
