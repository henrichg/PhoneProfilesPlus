package sk.henrichg.phoneprofilesplus;

import static android.view.View.VISIBLE;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.RemoteViews;

import androidx.core.graphics.ColorUtils;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.lang.ref.WeakReference;
import java.util.Comparator;
import java.util.List;

/** @noinspection ExtractMethodRecommender*/
public class OneRowProfileListWidgetProvider extends AppWidgetProvider {

    private static final int[] profileIconId = {
            R.id.widget_one_row_profile_list_profile_icon_1, R.id.widget_one_row_profile_list_profile_icon_2,
            R.id.widget_one_row_profile_list_profile_icon_3, R.id.widget_one_row_profile_list_profile_icon_4,
            R.id.widget_one_row_profile_list_profile_icon_5, R.id.widget_one_row_profile_list_profile_icon_6,
            R.id.widget_one_row_profile_list_profile_icon_7, R.id.widget_one_row_profile_list_profile_icon_8,
            R.id.widget_one_row_profile_list_profile_icon_9, R.id.widget_one_row_profile_list_profile_icon_10,
            R.id.widget_one_row_profile_list_profile_icon_11, R.id.widget_one_row_profile_list_profile_icon_12,
            R.id.widget_one_row_profile_list_profile_icon_13, R.id.widget_one_row_profile_list_profile_icon_14,
            R.id.widget_one_row_profile_list_profile_icon_15
    };

    private static final int[] profileMarkId = {
            R.id.widget_one_row_profile_list_profile_mark_1, R.id.widget_one_row_profile_list_profile_mark_2,
            R.id.widget_one_row_profile_list_profile_mark_3, R.id.widget_one_row_profile_list_profile_mark_4,
            R.id.widget_one_row_profile_list_profile_mark_5, R.id.widget_one_row_profile_list_profile_mark_6,
            R.id.widget_one_row_profile_list_profile_mark_7, R.id.widget_one_row_profile_list_profile_mark_8,
            R.id.widget_one_row_profile_list_profile_mark_9, R.id.widget_one_row_profile_list_profile_mark_10,
            R.id.widget_one_row_profile_list_profile_mark_11, R.id.widget_one_row_profile_list_profile_mark_12,
            R.id.widget_one_row_profile_list_profile_mark_13, R.id.widget_one_row_profile_list_profile_mark_14,
            R.id.widget_one_row_profile_list_profile_mark_15
    };

    private static final int[] profileRootId = {
            R.id.widget_one_row_profile_list_profile_icon_1_root, R.id.widget_one_row_profile_list_profile_icon_2_root,
            R.id.widget_one_row_profile_list_profile_icon_3_root, R.id.widget_one_row_profile_list_profile_icon_4_root,
            R.id.widget_one_row_profile_list_profile_icon_5_root, R.id.widget_one_row_profile_list_profile_icon_6_root,
            R.id.widget_one_row_profile_list_profile_icon_7_root, R.id.widget_one_row_profile_list_profile_icon_8_root,
            R.id.widget_one_row_profile_list_profile_icon_9_root, R.id.widget_one_row_profile_list_profile_icon_10_root,
            R.id.widget_one_row_profile_list_profile_icon_11_root, R.id.widget_one_row_profile_list_profile_icon_12_root,
            R.id.widget_one_row_profile_list_profile_icon_13_root, R.id.widget_one_row_profile_list_profile_icon_14_root,
            R.id.widget_one_row_profile_list_profile_icon_15_root
    };

    private static int displayedPage = 0;
    private static int profileCount = 0;

    private static final int MAX_PROFILE_COUNT = 15;

    static final String ACTION_REFRESH_ONEROWPROFILELISTWIDGET = PPApplication.PACKAGE_NAME + ".ACTION_REFRESH_ONEROWPROFILELISTWIDGET";
    private static final String ACTION_LEFT_ARROW_CLICK = PPApplication.PACKAGE_NAME + ".ACTION_LEFT_ARROW_CLICK";
    private static final String ACTION_RIGHT_ARROW_CLICK = PPApplication.PACKAGE_NAME + ".ACTION_RIGHT_ARROW_CLICK";
    private static final int PROFILE_ID_ACTIVATE_PROFILE_ID = 1000;

    public void onUpdate(Context context, AppWidgetManager _appWidgetManager, final int[] appWidgetIds)
    {
//        PPApplicationStatic.logE("[IN_LISTENER] OneRowWidgetProvider.onUpdate", "xxx");
        //super.onUpdate(context, appWidgetManager, appWidgetIds);
        if (appWidgetIds.length > 0) {
            final Context appContext = context.getApplicationContext();
            final WeakReference<AppWidgetManager> appWidgetManagerWeakRef = new WeakReference<>(_appWidgetManager);
            LocaleHelper.setApplicationLocale(appContext);
            Runnable runnable = () -> {
//                    PPApplicationStatic.logE("[IN_EXECUTOR] PPApplication.startHandlerThreadWidget", "START run - from=OneRowWidgetProvider.onUpdate");

                //Context appContext= appContextWeakRef.get();
                AppWidgetManager appWidgetManager = appWidgetManagerWeakRef.get();

                if (/*(appContext != null) &&*/ (appWidgetManager != null)) {
                    _onUpdate(appContext, appWidgetManager, appWidgetIds);
                }
            };
            PPApplicationStatic.createDelayedGuiExecutor();
            PPApplication.delayedGuiExecutor.submit(runnable);
        }
    }

    private static void _onUpdate(Context context, AppWidgetManager appWidgetManager,
                           /*Profile _profile, DataWrapper _dataWrapper,*/ int[] appWidgetIds)
    {
        String applicationWidgetOneRowProfileListIconLightness;
        String applicationWidgetOneRowProfileListIconColor;
        boolean applicationWidgetOneRowProfileListCustomIconLightness;

        String applicationWidgetOneRowProfileListArrowsMarkLightness;

        boolean applicationWidgetOneRowProfileListBackgroundType;
        String applicationWidgetOneRowProfileListBackgroundColor;
        String applicationWidgetOneRowProfileListLightnessB;
        String applicationWidgetOneRowProfileListBackground;
        boolean applicationWidgetOneRowProfileListShowBorder;
        String applicationWidgetOneRowProfileListLightnessBorder;
        boolean applicationWidgetOneRowProfileListRoundedCorners;
        int applicationWidgetOneRowProfileListRoundedCornersRadius;
        String applicationWidgetOneRowProfileListLayoutHeight;
        boolean applicationWidgetOneRowProfileListChangeColorsByNightMode;
        boolean applicationWidgetOneRowProfileListUseDynamicColors;
        String applicationWidgetOneRowProfileListBackgroundColorNightModeOff;
        String applicationWidgetOneRowProfileListBackgroundColorNightModeOn;
        int applicationWidgetOneRowProfileListNumberOfProfilesPerPage;
        boolean applicationWidgetOneRowProfileListFillBackground;
        boolean applicationWidgetOneRowProfileListLightnessBorderChangeByNightMode;

//        PPApplicationStatic.logE("[SYNCHRONIZED] OneRowProfileListWidgetProvider._onUpdate", "PPApplication.applicationPreferencesMutex");
        synchronized (PPApplication.applicationPreferencesMutex) {

            applicationWidgetOneRowProfileListIconLightness = ApplicationPreferences.applicationWidgetOneRowProfileListIconLightness;
            applicationWidgetOneRowProfileListIconColor = ApplicationPreferences.applicationWidgetOneRowProfileListIconColor;
            applicationWidgetOneRowProfileListCustomIconLightness = ApplicationPreferences.applicationWidgetOneRowProfileListCustomIconLightness;

            applicationWidgetOneRowProfileListArrowsMarkLightness = ApplicationPreferences.applicationWidgetOneRowProfileListArrowsMarkLightness;
            applicationWidgetOneRowProfileListNumberOfProfilesPerPage = ApplicationPreferences.applicationWidgetOneRowProfileListNumberOfProfilesPerPage;

            applicationWidgetOneRowProfileListBackgroundType = ApplicationPreferences.applicationWidgetOneRowProfileListBackgroundType;
            applicationWidgetOneRowProfileListBackgroundColor = ApplicationPreferences.applicationWidgetOneRowProfileListBackgroundColor;
            applicationWidgetOneRowProfileListLightnessB = ApplicationPreferences.applicationWidgetOneRowProfileListLightnessB;
            applicationWidgetOneRowProfileListBackground = ApplicationPreferences.applicationWidgetOneRowProfileListBackground;
            applicationWidgetOneRowProfileListShowBorder = ApplicationPreferences.applicationWidgetOneRowProfileListShowBorder;
            applicationWidgetOneRowProfileListLightnessBorder = ApplicationPreferences.applicationWidgetOneRowProfileListLightnessBorder;
            applicationWidgetOneRowProfileListRoundedCorners = ApplicationPreferences.applicationWidgetOneRowProfileListRoundedCorners;
            applicationWidgetOneRowProfileListRoundedCornersRadius = ApplicationPreferences.applicationWidgetOneRowProfileListRoundedCornersRadius;
            applicationWidgetOneRowProfileListLightnessBorderChangeByNightMode = ApplicationPreferences.applicationWidgetOneRowProfileListLightnessBorderChangeByNightMode;

            // "Rounded corners" parameter is removed, is forced to true
            if (!applicationWidgetOneRowProfileListRoundedCorners) {
                //applicationWidgetOneRowRoundedCorners = true;
                applicationWidgetOneRowProfileListRoundedCornersRadius = 1;
            }

            applicationWidgetOneRowProfileListLayoutHeight = ApplicationPreferences.applicationWidgetOneRowProfileListLayoutHeight;

            if (Build.VERSION.SDK_INT < 30)
                applicationWidgetOneRowProfileListChangeColorsByNightMode = false;
            else
                applicationWidgetOneRowProfileListChangeColorsByNightMode = ApplicationPreferences.applicationWidgetOneRowProfileListChangeColorsByNightMode;

            applicationWidgetOneRowProfileListUseDynamicColors = ApplicationPreferences.applicationWidgetOneRowProfileListUseDynamicColors;
            applicationWidgetOneRowProfileListBackgroundColorNightModeOff = ApplicationPreferences.applicationWidgetOneRowProfileListBackgroundColorNightModeOff;
            applicationWidgetOneRowProfileListBackgroundColorNightModeOn = ApplicationPreferences.applicationWidgetOneRowProfileListBackgroundColorNightModeOn;
            applicationWidgetOneRowProfileListFillBackground = ApplicationPreferences.applicationWidgetOneRowProfileListFillBackground;

            if (Build.VERSION.SDK_INT >= 30) {
                if (Build.VERSION.SDK_INT >= 31) {
                    if (PPApplicationStatic.isPixelLauncherDefault(context) ||
                            PPApplicationStatic.isOneUILauncherDefault(context) ||
                            PPApplicationStatic.isMIUILauncherDefault(context) ||
                            PPApplicationStatic.isSmartLauncherDefault(context)) {
                        ApplicationPreferences.applicationWidgetOneRowProfileListRoundedCorners = true;
                        ApplicationPreferences.applicationWidgetOneRowProfileListRoundedCornersRadius = 15;
                        //ApplicationPreferences.applicationWidgetChangeColorsByNightMode = true;
                        SharedPreferences.Editor editor = ApplicationPreferences.getEditor(context);
                        editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_PROFILE_LIST_ROUNDED_CORNERS,
                                ApplicationPreferences.applicationWidgetOneRowProfileListRoundedCorners);
                        editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_PROFILE_LIST_ROUNDED_CORNERS_RADIUS,
                                String.valueOf(ApplicationPreferences.applicationWidgetOneRowProfileListRoundedCornersRadius));
                        editor.apply();
                        //applicationWidgetOneRowProfileListRoundedCorners = ApplicationPreferences.applicationWidgetOneRowProfileListRoundedCorners;
                        applicationWidgetOneRowProfileListRoundedCornersRadius = ApplicationPreferences.applicationWidgetOneRowProfileListRoundedCornersRadius;
                    }
                }
                if (Build.VERSION.SDK_INT < 31)
                    applicationWidgetOneRowProfileListUseDynamicColors = false;
                if (//PPApplication.isPixelLauncherDefault(context) ||
                        (applicationWidgetOneRowProfileListChangeColorsByNightMode &&
                         (!applicationWidgetOneRowProfileListUseDynamicColors))) {
                    boolean nightModeOn = GlobalGUIRoutines.isNightModeEnabled(context.getApplicationContext());
                    //int nightModeFlags =
                    //        context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
                    //switch (nightModeFlags) {
                    //noinspection IfStatementWithIdenticalBranches
                    if (nightModeOn) {
                        //case Configuration.UI_MODE_NIGHT_YES:

                        //applicationWidgetOneRowProfileListBackground = GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_100; // fully opaque
                        applicationWidgetOneRowProfileListBackgroundType = true; // background type = color
                        applicationWidgetOneRowProfileListBackgroundColor = String.valueOf(ColorChooserPreference.parseValue(applicationWidgetOneRowProfileListBackgroundColorNightModeOn)); // color of background
                        //applicationWidgetOneRowProfileListShowBorder = false; // do not show border

                        //applicationWidgetOneRowProfileListLightnessBorder = GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_100;
                        if (applicationWidgetOneRowProfileListLightnessBorderChangeByNightMode) {
                            switch (applicationWidgetOneRowProfileListLightnessBorder) {
                                case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_0:
                                    applicationWidgetOneRowProfileListLightnessBorder = GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_100;
                                    break;
                                case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_12:
                                    applicationWidgetOneRowProfileListLightnessBorder = GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_87;
                                    break;
                                case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_25:
                                    applicationWidgetOneRowProfileListLightnessBorder = GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_75;
                                    break;
                                case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_37:
                                    applicationWidgetOneRowProfileListLightnessBorder = GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_62;
                                    break;
                            }
                            SharedPreferences.Editor editor = ApplicationPreferences.getEditor(context.getApplicationContext());
                            editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_PROFILE_LIST_LIGHTNESS_BORDER, applicationWidgetOneRowProfileListLightnessBorder);
                            editor.apply();
                            ApplicationPreferences.applicationWidgetOneRowProfileListLightnessBorder = applicationWidgetOneRowProfileListLightnessBorder;
                        }

                        applicationWidgetOneRowProfileListArrowsMarkLightness = GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_87; // lightness of arrows and mark = white

                        applicationWidgetOneRowProfileListIconColor = "0"; // icon type = colorful
                        applicationWidgetOneRowProfileListIconLightness = GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_75;
                        //break;
                    } else {
                        //case Configuration.UI_MODE_NIGHT_NO:
                        //case Configuration.UI_MODE_NIGHT_UNDEFINED:

                        //applicationWidgetOneRowProfileListBackground = GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_100; // fully opaque
                        applicationWidgetOneRowProfileListBackgroundType = true; // background type = color
                        applicationWidgetOneRowProfileListBackgroundColor = String.valueOf(ColorChooserPreference.parseValue(applicationWidgetOneRowProfileListBackgroundColorNightModeOff)); // color of background
                        //applicationWidgetOneRowProfileListShowBorder = false; // do not show border

                        //applicationWidgetOneRowProfileListLightnessBorder = "0";
                        if (applicationWidgetOneRowProfileListLightnessBorderChangeByNightMode) {
                            switch (applicationWidgetOneRowProfileListLightnessBorder) {
                                case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_62:
                                    applicationWidgetOneRowProfileListLightnessBorder = GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_37;
                                    break;
                                case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_75:
                                    applicationWidgetOneRowProfileListLightnessBorder = GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_25;
                                    break;
                                case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_87:
                                    applicationWidgetOneRowProfileListLightnessBorder = GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_12;
                                    break;
                                case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_100:
                                    applicationWidgetOneRowProfileListLightnessBorder = GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_0;
                                    break;
                            }
                            SharedPreferences.Editor editor = ApplicationPreferences.getEditor(context.getApplicationContext());
                            editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_PROFILE_LIST_LIGHTNESS_BORDER, applicationWidgetOneRowProfileListLightnessBorder);
                            editor.apply();
                            ApplicationPreferences.applicationWidgetOneRowProfileListLightnessBorder = applicationWidgetOneRowProfileListLightnessBorder;
                        }

                        applicationWidgetOneRowProfileListArrowsMarkLightness = GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_12; // lightness arrows and mark = black

                        applicationWidgetOneRowProfileListIconColor = "0"; // icon type = colorful
                        applicationWidgetOneRowProfileListIconLightness = GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_62;
                        //break;
                    }
                }
            }
        }

        int monochromeValue = 0xFF;
        switch (applicationWidgetOneRowProfileListIconLightness) {
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

        DataWrapper dataWrapper = new DataWrapper(context.getApplicationContext(),
                    applicationWidgetOneRowProfileListIconColor.equals("1"), monochromeValue,
                    applicationWidgetOneRowProfileListCustomIconLightness,
                    DataWrapper.IT_FOR_WIDGET, 0, 0);

        //List<Profile> newProfileList = dataWrapper.getNewProfileList(true, false);
        List<Profile> newProfileList = dataWrapper.getNewProfileList(false, false);

        // add activated profile, when has not enabled _showInActivator
        Profile activatedProfile = dataWrapper.getActivatedProfile(newProfileList);
        if ((activatedProfile != null) && (!activatedProfile._showInActivator))
        {
            activatedProfile._showInActivator = true;
            activatedProfile._porder = -1;
        }
        for (Profile profile : newProfileList) {
            if (profile._showInActivator) {
                dataWrapper.generateProfileIcon(profile, true, false);
            }
        }

        newProfileList.sort(new OneRowProfileListWidgetProvider.ProfileComparator());

        Profile restartEvents = null;
        if (EventStatic.getGlobalEventsRunning(context)) {
            //restartEvents = DataWrapper.getNonInitializedProfile(context.getString(R.string.menu_restart_events), "ic_profile_restart_events|1|0|0", 0);
            restartEvents = DataWrapperStatic.getNonInitializedProfile(context.getString(R.string.menu_restart_events),
                    StringConstants.PROFILE_ICON_RESTART_EVENTS+"|1|1|"+ApplicationPreferences.applicationRestartEventsIconColor, 0);
            restartEvents._showInActivator = true;
            restartEvents._id = Profile.RESTART_EVENTS_PROFILE_ID;
            newProfileList.add(0, restartEvents);
        }
        if (restartEvents != null)
            dataWrapper.generateProfileIcon(restartEvents, true, false);

        dataWrapper.setProfileList(newProfileList);

        profileCount = 0;
        for (Profile profile : dataWrapper.profileList) {
            if (profile._showInActivator)
                profileCount++;
        }
        if (profileCount > MAX_PROFILE_COUNT)
            profileCount = MAX_PROFILE_COUNT;

        // set background
        int redBackground = 0x00;
        int greenBackground;
        int blueBackground;
        if (applicationWidgetOneRowProfileListBackgroundType) {
            int bgColor = Integer.parseInt(applicationWidgetOneRowProfileListBackgroundColor);
            redBackground = Color.red(bgColor);
            greenBackground = Color.green(bgColor);
            blueBackground = Color.blue(bgColor);
        } else {
            switch (applicationWidgetOneRowProfileListLightnessB) {
                case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_0:
                    //noinspection ConstantConditions
                    redBackground = 0x00;
                    break;
                case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_12:
                    redBackground = 0x20;
                    break;
                case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_25:
                    redBackground = 0x40;
                    break;
                case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_37:
                    redBackground = 0x60;
                    break;
                case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_50:
                    redBackground = 0x80;
                    break;
                case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_62:
                    redBackground = 0xA0;
                    break;
                case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_75:
                    redBackground = 0xC0;
                    break;
                case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_87:
                    redBackground = 0xE0;
                    break;
                case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_100:
                    redBackground = 0xFF;
                    break;
            }
            greenBackground = redBackground;
            blueBackground = redBackground;
        }

        int alphaBackground = 0x40;
        switch (applicationWidgetOneRowProfileListBackground) {
            case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_0:
                alphaBackground = 0x00;
                break;
            case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_12:
                alphaBackground = 0x20;
                break;
            case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_25:
                //noinspection ConstantConditions
                alphaBackground = 0x40;
                break;
            case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_37:
                alphaBackground = 0x60;
                break;
            case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_50:
                alphaBackground = 0x80;
                break;
            case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_62:
                alphaBackground = 0xA0;
                break;
            case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_75:
                alphaBackground = 0xC0;
                break;
            case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_87:
                alphaBackground = 0xE0;
                break;
            case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_100:
                alphaBackground = 0xFF;
                break;
        }

        int redBorder = 0xFF;
        int greenBorder;
        int blueBorder;
        if (applicationWidgetOneRowProfileListShowBorder) {
            switch (applicationWidgetOneRowProfileListLightnessBorder) {
                case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_0:
                    redBorder = 0x00;
                    break;
                case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_12:
                    redBorder = 0x20;
                    break;
                case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_25:
                    redBorder = 0x40;
                    break;
                case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_37:
                    redBorder = 0x60;
                    break;
                case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_50:
                    redBorder = 0x80;
                    break;
                case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_62:
                    redBorder = 0xA0;
                    break;
                case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_75:
                    redBorder = 0xC0;
                    break;
                case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_87:
                    redBorder = 0xE0;
                    break;
                case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_100:
                    //noinspection ConstantConditions
                    redBorder = 0xFF;
                    break;
            }
        }
        greenBorder = redBorder;
        blueBorder = redBorder;

        int markRedColor = 0xFF;
        switch (applicationWidgetOneRowProfileListArrowsMarkLightness) {
            case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_0:
                markRedColor = 0x00;
                break;
            case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_12:
                markRedColor = 0x20;
                break;
            case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_25:
                markRedColor = 0x40;
                break;
            case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_37:
                markRedColor = 0x60;
                break;
            case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_50:
                markRedColor = 0x80;
                break;
            case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_62:
                markRedColor = 0xA0;
                break;
            case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_75:
                markRedColor = 0xC0;
                break;
            case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_87:
                markRedColor = 0xE0;
                break;
            case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_100:
                //noinspection ConstantConditions
                markRedColor = 0xFF;
                break;
        }
        int markGreenColor = markRedColor;
        int markBlueColor = markRedColor;

        int arrowsLightness = markRedColor;


        // get all OneRowWidgetProvider widgets in launcher
        //ComponentName thisWidget = new ComponentName(context, OneRowWidgetProvider.class);
        //int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);

        for (int widgetId : appWidgetIds) {

            Bundle options = appWidgetManager.getAppWidgetOptions(widgetId);
            int maxHeight = GlobalGUIRoutines.dpToPx(options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT));
            //Log.e("OneRowProfileListWidgetProvider._onUpdate", "maxHeight="+maxHeight);

            //bundle.putInt(PPApplication.BUNDLE_WIDGET_TYPE, PPApplication.WIDGET_TYPE_ONE_ROW);
            //appWidgetManager.updateAppWidgetOptions(widgetId, bundle);

//                AppWidgetProviderInfo info = appWidgetManager.getAppWidgetInfo(widgetId);

            RemoteViews remoteViews;

            float configuredHeight;
            if (!((Build.VERSION.SDK_INT >= 31) && applicationWidgetOneRowProfileListChangeColorsByNightMode &&
                    applicationWidgetOneRowProfileListIconColor.equals("0") && applicationWidgetOneRowProfileListUseDynamicColors)) {
                if (applicationWidgetOneRowProfileListLayoutHeight.equals("0")) {
                    configuredHeight = context.getResources().getDimension(R.dimen.one_row_widget_height);
//                        Log.e("OneRowProfileListWidgetProvider._onUpdate", "configuredHeight="+configuredHeight);
                    if (maxHeight < configuredHeight)
                        applicationWidgetOneRowProfileListFillBackground = true;
                    if (applicationWidgetOneRowProfileListFillBackground)
                        remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_one_row_profile_list_fill);
                    else
                        remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_one_row_profile_list);
                } else if (applicationWidgetOneRowProfileListLayoutHeight.equals("1")) {
                    configuredHeight = context.getResources().getDimension(R.dimen.one_row_widget_height_higher);
//                        Log.e("OneRowProfileListWidgetProvider._onUpdate", "configuredHeight="+configuredHeight);
                    if (maxHeight < configuredHeight)
                        applicationWidgetOneRowProfileListFillBackground = true;
                    if (applicationWidgetOneRowProfileListFillBackground)
                        remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_one_row_profile_list_higher_fill);
                    else
                        remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_one_row_profile_list_higher);
                } else {
                    configuredHeight = context.getResources().getDimension(R.dimen.one_row_widget_height_highest);
//                        Log.e("OneRowProfileListWidgetProvider._onUpdate", "configuredHeight="+configuredHeight);
                    if (maxHeight < configuredHeight)
                        applicationWidgetOneRowProfileListFillBackground = true;
                    if (applicationWidgetOneRowProfileListFillBackground)
                        remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_one_row_profile_list_highest_fill);
                    else
                        remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_one_row_profile_list_highest);
                }
            } else {
                if (applicationWidgetOneRowProfileListLayoutHeight.equals("0")) {
                    configuredHeight = context.getResources().getDimension(R.dimen.one_row_widget_height);
//                        Log.e("OneRowProfileListWidgetProvider._onUpdate", "configuredHeight="+configuredHeight);
                    if (maxHeight < configuredHeight)
                        applicationWidgetOneRowProfileListFillBackground = true;
                    if (applicationWidgetOneRowProfileListFillBackground)
                        remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_one_row_profile_list_fill_dn);
                    else
                        remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_one_row_profile_list_dn);
                } else if (applicationWidgetOneRowProfileListLayoutHeight.equals("1")) {
                    configuredHeight = context.getResources().getDimension(R.dimen.one_row_widget_height_higher);
//                        Log.e("OneRowProfileListWidgetProvider._onUpdate", "configuredHeight="+configuredHeight);
                    if (maxHeight < configuredHeight)
                        applicationWidgetOneRowProfileListFillBackground = true;
                    if (applicationWidgetOneRowProfileListFillBackground)
                        remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_one_row_profile_list_higher_fill_dn);
                    else
                        remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_one_row_profile_list_higher_dn);
                } else {
                    configuredHeight = context.getResources().getDimension(R.dimen.one_row_widget_height_highest);
//                        Log.e("OneRowProfileListWidgetProvider._onUpdate", "configuredHeight="+configuredHeight);
                    if (maxHeight < configuredHeight)
                        applicationWidgetOneRowProfileListFillBackground = true;
                    if (applicationWidgetOneRowProfileListFillBackground)
                        remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_one_row_profile_list_highest_fill_dn);
                    else
                        remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_one_row_profile_list_highest_dn);
                }
            }

            int roundedBackground = 0;
            int roundedBorder = 0;
            if ((Build.VERSION.SDK_INT >= 31) && PPApplicationStatic.isPixelLauncherDefault(context)) {
                roundedBackground = R.drawable.rounded_widget_background_pixel_launcher;
                roundedBorder = R.drawable.rounded_widget_border_pixel_launcher;
            } else
            if ((Build.VERSION.SDK_INT >= 31) && PPApplicationStatic.isOneUILauncherDefault(context)) {
                roundedBackground = R.drawable.rounded_widget_background_oneui_launcher;
                roundedBorder = R.drawable.rounded_widget_border_oneui_launcher;
            } else
            if ((Build.VERSION.SDK_INT >= 31) && PPApplicationStatic.isMIUILauncherDefault(context)) {
                roundedBackground = R.drawable.rounded_widget_background_miui_launcher;
                roundedBorder = R.drawable.rounded_widget_border_miui_launcher;
            } else
            if ((Build.VERSION.SDK_INT >= 31) && PPApplicationStatic.isSmartLauncherDefault(context)) {
                roundedBackground = R.drawable.rounded_widget_background_smart_launcher;
                roundedBorder = R.drawable.rounded_widget_border_smart_launcher;
            } else {
                switch (applicationWidgetOneRowProfileListRoundedCornersRadius) {
                    case 1:
                        roundedBackground = R.drawable.rounded_widget_background_1;
                        roundedBorder = R.drawable.rounded_widget_border_1;
                        break;
                    case 2:
                        roundedBackground = R.drawable.rounded_widget_background_2;
                        roundedBorder = R.drawable.rounded_widget_border_2;
                        break;
                    case 3:
                        roundedBackground = R.drawable.rounded_widget_background_3;
                        roundedBorder = R.drawable.rounded_widget_border_3;
                        break;
                    case 4:
                        roundedBackground = R.drawable.rounded_widget_background_4;
                        roundedBorder = R.drawable.rounded_widget_border_4;
                        break;
                    case 5:
                        roundedBackground = R.drawable.rounded_widget_background_5;
                        roundedBorder = R.drawable.rounded_widget_border_5;
                        break;
                    case 6:
                        roundedBackground = R.drawable.rounded_widget_background_6;
                        roundedBorder = R.drawable.rounded_widget_border_6;
                        break;
                    case 7:
                        roundedBackground = R.drawable.rounded_widget_background_7;
                        roundedBorder = R.drawable.rounded_widget_border_7;
                        break;
                    case 8:
                        roundedBackground = R.drawable.rounded_widget_background_8;
                        roundedBorder = R.drawable.rounded_widget_border_8;
                        break;
                    case 9:
                        roundedBackground = R.drawable.rounded_widget_background_9;
                        roundedBorder = R.drawable.rounded_widget_border_9;
                        break;
                    case 10:
                        roundedBackground = R.drawable.rounded_widget_background_10;
                        roundedBorder = R.drawable.rounded_widget_border_10;
                        break;
                    case 11:
                        roundedBackground = R.drawable.rounded_widget_background_11;
                        roundedBorder = R.drawable.rounded_widget_border_11;
                        break;
                    case 12:
                        roundedBackground = R.drawable.rounded_widget_background_12;
                        roundedBorder = R.drawable.rounded_widget_border_12;
                        break;
                    case 13:
                        roundedBackground = R.drawable.rounded_widget_background_13;
                        roundedBorder = R.drawable.rounded_widget_border_13;
                        break;
                    case 14:
                        roundedBackground = R.drawable.rounded_widget_background_14;
                        roundedBorder = R.drawable.rounded_widget_border_14;
                        break;
                    case 15:
                        roundedBackground = R.drawable.rounded_widget_background_15;
                        roundedBorder = R.drawable.rounded_widget_border_15;
                        break;
                }
            }
            if (roundedBackground != 0)
                remoteViews.setImageViewResource(R.id.widget_one_row_profile_list_background, roundedBackground);
            else
                remoteViews.setImageViewResource(R.id.widget_one_row_profile_list_background, R.drawable.ic_empty);
            if (roundedBorder != 0)
                remoteViews.setImageViewResource(R.id.widget_one_row_profile_list_rounded_border, roundedBorder);
            else
                remoteViews.setImageViewResource(R.id.widget_one_row_profile_list_rounded_border, R.drawable.ic_empty);

            remoteViews.setViewVisibility(R.id.widget_one_row_profile_list_background, VISIBLE);
            //remoteViews.setViewVisibility(R.id.widget_one_row_profile_list_not_rounded_border, View.GONE);
            if (applicationWidgetOneRowProfileListShowBorder) {
                remoteViews.setViewVisibility(R.id.widget_one_row_profile_list_rounded_border, VISIBLE);
            }
            else {
                remoteViews.setViewVisibility(R.id.widget_one_row_profile_list_rounded_border, View.GONE);
            }
            remoteViews.setInt(R.id.widget_one_row_profile_list_root, "setBackgroundColor", 0x00000000);

            if (!((Build.VERSION.SDK_INT >= 31) && applicationWidgetOneRowProfileListChangeColorsByNightMode &&
                    applicationWidgetOneRowProfileListIconColor.equals("0") && applicationWidgetOneRowProfileListUseDynamicColors)) {
                //remoteViews.setInt(R.id.widget_one_row_profile_list_background, "setColorFilter", Color.argb(0xFF, 0, 0, 0));
                remoteViews.setInt(R.id.widget_one_row_profile_list_background, "setColorFilter", Color.argb(0xFF, redBackground, greenBackground, blueBackground));
            }

            remoteViews.setInt(R.id.widget_one_row_profile_list_background, "setImageAlpha", alphaBackground);

            if (applicationWidgetOneRowProfileListShowBorder) {
                if (!((Build.VERSION.SDK_INT >= 31) && applicationWidgetOneRowProfileListChangeColorsByNightMode &&
                        applicationWidgetOneRowProfileListIconColor.equals("0") && applicationWidgetOneRowProfileListUseDynamicColors))
                    remoteViews.setInt(R.id.widget_one_row_profile_list_rounded_border, "setColorFilter", Color.argb(0xFF, redBorder, greenBorder, blueBorder));
            }

            int profileIdx = 0;
            int displayedProfileIdx = 0;
            int firstProfileIdxInPage = applicationWidgetOneRowProfileListNumberOfProfilesPerPage * displayedPage;
            //Log.e("OneRowProfileListWidgetProvider._onUpdate", "displayedPage="+displayedPage);
            //Log.e("OneRowProfileListWidgetProvider._onUpdate", "firstProfileInPage="+firstProfileInPage);
            for (Profile profile : dataWrapper.profileList) {
                if (profile._showInActivator) {
                    if (profileIdx >= firstProfileIdxInPage) {
                        setProfileIcon(profile,
                                profileIconId[displayedProfileIdx], profileMarkId[displayedProfileIdx], profileRootId[displayedProfileIdx],
                                applicationWidgetOneRowProfileListIconColor,
                                monochromeValue,
                                applicationWidgetOneRowProfileListCustomIconLightness,
                                applicationWidgetOneRowProfileListChangeColorsByNightMode,
                                applicationWidgetOneRowProfileListBackgroundType,
                                applicationWidgetOneRowProfileListLightnessB,
                                applicationWidgetOneRowProfileListBackgroundColor,
                                applicationWidgetOneRowProfileListUseDynamicColors,
                                markRedColor, markGreenColor, markBlueColor,
                                remoteViews, context);
                        //remoteViews.setViewVisibility(profileIconId[displayedProfileIdx], View.VISIBLE);
                        ++displayedProfileIdx;
                    }
                    profileIdx++;
                    if (displayedProfileIdx == MAX_PROFILE_COUNT)
                        break;
                }
            }
            // invisible all not used profile icons
            for (int i = displayedProfileIdx; i < MAX_PROFILE_COUNT; i++) {
                remoteViews.setViewVisibility(profileRootId[i], View.GONE);
                //remoteViews.setViewVisibility(profileIconId[i], View.INVISIBLE);
                //remoteViews.setViewVisibility(profileMarkId[i], View.INVISIBLE);
                remoteViews.setOnClickPendingIntent(profileRootId[i], null);
            }

            if (!((Build.VERSION.SDK_INT >= 31) && applicationWidgetOneRowProfileListChangeColorsByNightMode &&
                    applicationWidgetOneRowProfileListIconColor.equals("0") && applicationWidgetOneRowProfileListUseDynamicColors)) {
                //if (Event.getGlobalEventsRunning() && PPApplicationStatic.getApplicationStarted(true)) {
                // left arrow
                Bitmap bitmap = BitmapManipulator.getBitmapFromResource(R.drawable.ic_widget_profile_list_scroll_left, true, context);
                bitmap = BitmapManipulator.monochromeBitmap(bitmap, arrowsLightness);
                remoteViews.setImageViewBitmap(R.id.widget_one_row_profile_list_scroll_left_arrow, bitmap);
                // right arrow
                bitmap = BitmapManipulator.getBitmapFromResource(R.drawable.ic_widget_profile_list_scroll_right, true, context);
                bitmap = BitmapManipulator.monochromeBitmap(bitmap, arrowsLightness);
                remoteViews.setImageViewBitmap(R.id.widget_one_row_profile_list_scroll_right_arrow, bitmap);
                //}
            } else {
                // good, color of this is as in notification ;-)
                // but must be removed android:tint in layout
                int color = GlobalGUIRoutines.getDynamicColor(R.attr.colorSecondary, context);
                if (color != 0) {
                    // left arrow
                    Bitmap bitmap = BitmapManipulator.getBitmapFromResource(R.drawable.ic_widget_profile_list_scroll_left, true, context);
                    bitmap = BitmapManipulator.recolorBitmap(bitmap, color);
                    remoteViews.setImageViewBitmap(R.id.widget_one_row_profile_list_scroll_left_arrow, bitmap);
                    // right arrow
                    bitmap = BitmapManipulator.getBitmapFromResource(R.drawable.ic_widget_profile_list_scroll_right, true, context);
                    bitmap = BitmapManipulator.recolorBitmap(bitmap, color);
                    remoteViews.setImageViewBitmap(R.id.widget_one_row_profile_list_scroll_right_arrow, bitmap);
                }
            }
            //if (Event.getGlobalEventsRunning() && PPApplicationStatic.getApplicationStarted(true)) {
            // left arrow
            if (displayedPage > 0)
                remoteViews.setViewVisibility(R.id.widget_one_row_profile_list_scroll_left_arrow, VISIBLE);
            else
                remoteViews.setViewVisibility(R.id.widget_one_row_profile_list_scroll_left_arrow, View.GONE);
            Intent intentLeftArrow = new Intent(context, OneRowProfileListWidgetProvider.class);
            intentLeftArrow.setAction(ACTION_LEFT_ARROW_CLICK);
            PendingIntent pIntentLeftArrow = PendingIntent.getBroadcast(context, 2, intentLeftArrow, PendingIntent.FLAG_UPDATE_CURRENT);
            remoteViews.setOnClickPendingIntent(R.id.widget_one_row_profile_list_scroll_left_arrow, pIntentLeftArrow);
            // right arrow
            if ((displayedPage < profileCount / applicationWidgetOneRowProfileListNumberOfProfilesPerPage) &&
                    (profileCount > applicationWidgetOneRowProfileListNumberOfProfilesPerPage))
                remoteViews.setViewVisibility(R.id.widget_one_row_profile_list_scroll_right_arrow, VISIBLE);
            else
                remoteViews.setViewVisibility(R.id.widget_one_row_profile_list_scroll_right_arrow, View.GONE);
            Intent intentRightArrow = new Intent(context, OneRowProfileListWidgetProvider.class);
            intentRightArrow.setAction(ACTION_RIGHT_ARROW_CLICK);
            PendingIntent pIntentRightArrow = PendingIntent.getBroadcast(context, 3, intentRightArrow, PendingIntent.FLAG_UPDATE_CURRENT);
            remoteViews.setOnClickPendingIntent(R.id.widget_one_row_profile_list_scroll_right_arrow, pIntentRightArrow);

            // widget update
            try {
                appWidgetManager.updateAppWidget(widgetId, remoteViews);
                //ComponentName thisWidget = new ComponentName(context, OneRowWidgetProvider.class);
                //appWidgetManager.updateAppWidget(thisWidget, remoteViews);
                //appWidgetManager.partiallyUpdateAppWidget(appWidgetIds, remoteViews);
            } catch (Exception e) {
                PPApplicationStatic.recordException(e);
            }
        }

        /*if (profile != null) {
            profile.releaseIconBitmap();
            profile.releasePreferencesIndicator();
        }*/
        dataWrapper.invalidateDataWrapper();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final Context appContext = context.getApplicationContext();
        LocaleHelper.setApplicationLocale(appContext);

        super.onReceive(appContext, intent); // calls onUpdate, is required for widget

        String action = intent.getAction();
//        PPApplicationStatic.logE("[IN_BROADCAST] OneRowWidgetProvider.onReceive", "action="+action);

        if (action != null) {
            if (action.equalsIgnoreCase(ACTION_REFRESH_ONEROWPROFILELISTWIDGET)) {
                AppWidgetManager manager = AppWidgetManager.getInstance(appContext);
                if (manager != null) {
                    final int[] ids = manager.getAppWidgetIds(new ComponentName(appContext, OneRowProfileListWidgetProvider.class));
                    if ((ids != null) && (ids.length > 0)) {
                        final WeakReference<AppWidgetManager> appWidgetManagerWeakRef = new WeakReference<>(manager);
                        Runnable runnable = () -> {
//                            PPApplicationStatic.logE("[IN_EXECUTOR] PPApplication.startHandlerThreadWidget", "START run - from=OneRowWidgetProvider.onReceive");

                            //Context appContext= appContextWeakRef.get();
                            AppWidgetManager appWidgetManager = appWidgetManagerWeakRef.get();

                            if (/*(appContext != null) &&*/ (appWidgetManager != null)) {
                                _onUpdate(appContext, appWidgetManager, ids);
                            }
                        };
                        PPApplicationStatic.createDelayedGuiExecutor();
                        PPApplication.delayedGuiExecutor.submit(runnable);
                    }
                }
            }
            else
            if (action.equalsIgnoreCase(ACTION_RIGHT_ARROW_CLICK)) {
                if ((displayedPage < profileCount / ApplicationPreferences.applicationWidgetOneRowProfileListNumberOfProfilesPerPage) &&
                        (profileCount > ApplicationPreferences.applicationWidgetOneRowProfileListNumberOfProfilesPerPage)) {
                    ++displayedPage;
                    updateWidgets(appContext);
                }
            }
            else
            if (action.equalsIgnoreCase(ACTION_LEFT_ARROW_CLICK)) {
                if (displayedPage > 0) {
                    --displayedPage;
                    updateWidgets(appContext);
                }
            }
        }
    }

    @Override
    public void onAppWidgetOptionsChanged (Context context,
                                           AppWidgetManager appWidgetManager,
                                           int appWidgetId,
                                           Bundle newOptions) {
//        PPApplicationStatic.logE("[LOCAL_BROADCAST_CALL] OneRowProfileListWidgetProvider.onAppWidgetOptionsChanged", "xxx");
        Intent intent3 = new Intent(ACTION_REFRESH_ONEROWPROFILELISTWIDGET);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent3);
    }

    static void updateWidgets(Context context/*, boolean refresh*/) {
//        PPApplicationStatic.logE("[LOCAL_BROADCAST_CALL] OneRowProfileListWidgetProvider.updateWidgets", "xxx");
        Intent intent3 = new Intent(ACTION_REFRESH_ONEROWPROFILELISTWIDGET);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent3);
    }

/*    private static abstract class PPHandlerThreadRunnable implements Runnable {

        final WeakReference<Context> appContextWeakRef;
        final WeakReference<AppWidgetManager> appWidgetManagerWeakRef;

        PPHandlerThreadRunnable(Context appContext,
                                       AppWidgetManager appWidgetManager) {
            this.appContextWeakRef = new WeakReference<>(appContext);
            this.appWidgetManagerWeakRef = new WeakReference<>(appWidgetManager);
        }

    }*/

    private static void setProfileIcon(Profile profile,
                                       int imageViewId, int markViewId, int rootId,
                                       String applicationWidgetOneRowProfileListIconColor,
                                       int monochromeValue,
                                       boolean applicationWidgetOneRowProfileListCustomIconLightness,
                                       boolean applicationWidgetOneRowProfileListChangeColorsByNightMode,
                                       boolean applicationWidgetOneRowProfileListBackgroundType,
                                       String applicationWidgetOneRowProfileListLightnessB,
                                       String applicationWidgetOneRowProfileListBackgroundColor,
                                       boolean applicationWidgetOneRowProfileListUseDynamicColors,
                                       int markRedColor, int markGreenColor, int markBlueColor,
                                       RemoteViews remoteViews, Context context) {

        remoteViews.setViewVisibility(rootId, VISIBLE);

        boolean isIconResourceID;
        String iconIdentifier;
        //noinspection IfStatementWithIdenticalBranches
        if (profile != null) {
            isIconResourceID = profile.getIsIconResourceID();
            iconIdentifier = profile.getIconIdentifier();
        } else {
            // create empty profile and set icon resource
            profile = new Profile();
            profile._name = context.getString(R.string.profiles_header_profile_name_no_activated);
            profile._icon = StringConstants.PROFILE_ICON_DEFAULT + "|1|0|0";

            profile.generateIconBitmap(context.getApplicationContext(),
                    applicationWidgetOneRowProfileListIconColor.equals("1"),
                    monochromeValue,
                    applicationWidgetOneRowProfileListCustomIconLightness);
            isIconResourceID = profile.getIsIconResourceID();
            iconIdentifier = profile.getIconIdentifier();
        }

        Bitmap bitmap = null;
        if (applicationWidgetOneRowProfileListIconColor.equals("0")) {
            if (isIconResourceID) {
                if (applicationWidgetOneRowProfileListChangeColorsByNightMode ||
                        ((!applicationWidgetOneRowProfileListBackgroundType) &&
                                (Integer.parseInt(applicationWidgetOneRowProfileListLightnessB) <= 25)) ||
                        (applicationWidgetOneRowProfileListBackgroundType &&
                                (ColorUtils.calculateLuminance(Integer.parseInt(applicationWidgetOneRowProfileListBackgroundColor)) < 0.23)))
                    bitmap = profile.increaseProfileIconBrightnessForContext(context, profile._iconBitmap);
            } else
                bitmap = profile._iconBitmap;
        }
        if (isIconResourceID) {
            if (bitmap != null)
                remoteViews.setImageViewBitmap(imageViewId, bitmap);
            else {
                if (profile._iconBitmap != null)
                    remoteViews.setImageViewBitmap(imageViewId, profile._iconBitmap);
                else {
                    //remoteViews.setImageViewResource(imageViewId, 0);
                    //int iconResource = context.getResources().getIdentifier(iconIdentifier, "drawable", context.PPApplication.PACKAGE_NAME);
                    int iconResource = ProfileStatic.getIconResource(iconIdentifier);
                    remoteViews.setImageViewResource(imageViewId, iconResource);
                }
            }
        } else {
            if (bitmap != null)
                remoteViews.setImageViewBitmap(imageViewId, bitmap);
            else {
                remoteViews.setImageViewBitmap(imageViewId, profile._iconBitmap);
            }
        }

        if (profile._checked) {
            if (!((Build.VERSION.SDK_INT >= 31) && applicationWidgetOneRowProfileListChangeColorsByNightMode &&
                    applicationWidgetOneRowProfileListIconColor.equals("0") && applicationWidgetOneRowProfileListUseDynamicColors))
                remoteViews.setInt(markViewId, "setBackgroundColor", Color.argb(0xFF, markRedColor, markGreenColor, markBlueColor));
            else {
                int color = GlobalGUIRoutines.getDynamicColor(R.attr.colorSecondary, context);
                if (color != 0) {
                    bitmap = BitmapManipulator.getBitmapFromResource(R.drawable.ic_black, true, context);
                    bitmap = BitmapManipulator.recolorBitmap(bitmap, color);
                    remoteViews.setImageViewBitmap(markViewId, bitmap);
                }
            }
            remoteViews.setViewVisibility(markViewId, View.VISIBLE);
        } else {
            remoteViews.setViewVisibility(markViewId, View.INVISIBLE);
        }

        Intent clickIntent = new Intent(context, BackgroundActivateProfileActivity.class);
        if (EventStatic.getGlobalEventsRunning(context) && (profile._id == Profile.RESTART_EVENTS_PROFILE_ID))
            clickIntent.putExtra(PPApplication.EXTRA_PROFILE_ID, Profile.RESTART_EVENTS_PROFILE_ID);
        else
            clickIntent.putExtra(PPApplication.EXTRA_PROFILE_ID, profile._id);
        clickIntent.putExtra(PPApplication.EXTRA_STARTUP_SOURCE, PPApplication.STARTUP_SOURCE_WIDGET);
        PendingIntent clickPI=PendingIntent.getActivity(context, PROFILE_ID_ACTIVATE_PROFILE_ID + (int) profile._id, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(rootId, clickPI);
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
