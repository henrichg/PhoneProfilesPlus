package sk.henrichg.phoneprofilesplus;

import static android.view.View.GONE;
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
import android.text.Spannable;
import android.text.SpannableString;
import android.view.View;
import android.widget.RemoteViews;

import androidx.core.graphics.ColorUtils;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class OneRowProfileListWidgetProvider extends AppWidgetProvider {

    static final String ACTION_REFRESH_ONEROWPROFILELISTWIDGET = PPApplication.PACKAGE_NAME + ".ACTION_REFRESH_ONEROWPROFILELISTWIDGET";

    public void onUpdate(Context context, AppWidgetManager appWidgetManager, final int[] appWidgetIds)
    {
//        PPApplication.logE("[IN_LISTENER] OneRowWidgetProvider.onUpdate", "xxx");
        //super.onUpdate(context, appWidgetManager, appWidgetIds);
        if (appWidgetIds.length > 0) {
            final Context appContext = context;
            LocaleHelper.setApplicationLocale(appContext);
            //PPApplication.startHandlerThreadWidget();
            //final Handler __handler = new Handler(PPApplication.handlerThreadWidget.getLooper());
            //__handler.post(new PPHandlerThreadRunnable(context, appWidgetManager) {
            //__handler.post(() -> {
            Runnable runnable = () -> {
//                    PPApplication.logE("[IN_EXECUTOR] PPApplication.startHandlerThreadWidget", "START run - from=OneRowWidgetProvider.onUpdate");

                //Context appContext= appContextWeakRef.get();
                //AppWidgetManager appWidgetManager = appWidgetManagerWeakRef.get();

                //if ((appContext != null) && (appWidgetManager != null)) {
                    _onUpdate(appContext, appWidgetManager, appWidgetIds);
                //}
            }; //);
            PPApplication.createDelayedGuiExecutor();
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

            // "Rounded corners" parameter is removed, is forced to true
            if (!applicationWidgetOneRowProfileListRoundedCorners) {
                //applicationWidgetOneRowRoundedCorners = true;
                applicationWidgetOneRowProfileListRoundedCornersRadius = 1;
            }

            applicationWidgetOneRowProfileListLayoutHeight = ApplicationPreferences.applicationWidgetOneRowProfileListLayoutHeight;
            applicationWidgetOneRowProfileListChangeColorsByNightMode = ApplicationPreferences.applicationWidgetOneRowProfileListChangeColorsByNightMode;
            applicationWidgetOneRowProfileListUseDynamicColors = ApplicationPreferences.applicationWidgetOneRowProfileListUseDynamicColors;
            applicationWidgetOneRowProfileListBackgroundColorNightModeOff = ApplicationPreferences.applicationWidgetOneRowProfileListBackgroundColorNightModeOff;
            applicationWidgetOneRowProfileListBackgroundColorNightModeOn = ApplicationPreferences.applicationWidgetOneRowProfileListBackgroundColorNightModeOn;

            if (Build.VERSION.SDK_INT >= 30) {
                if (PPApplication.isPixelLauncherDefault(context) ||
                        PPApplication.isOneUILauncherDefault(context)) {
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
                        applicationWidgetOneRowProfileListLightnessBorder = GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_100;
                        applicationWidgetOneRowProfileListArrowsMarkLightness = GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_87; // lightness of arrows and mark = white
                        //applicationWidgetOneRowProfileListIconColor = "0"; // icon type = colorful
                        applicationWidgetOneRowProfileListIconLightness = GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_75;
                        //break;
                    } else {
                        //case Configuration.UI_MODE_NIGHT_NO:
                        //case Configuration.UI_MODE_NIGHT_UNDEFINED:

                        //applicationWidgetOneRowProfileListBackground = GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_100; // fully opaque
                        applicationWidgetOneRowProfileListBackgroundType = true; // background type = color
                        applicationWidgetOneRowProfileListBackgroundColor = String.valueOf(ColorChooserPreference.parseValue(applicationWidgetOneRowProfileListBackgroundColorNightModeOff)); // color of background
                        //applicationWidgetOneRowProfileListShowBorder = false; // do not show border
                        applicationWidgetOneRowProfileListLightnessBorder = "0";
                        applicationWidgetOneRowProfileListArrowsMarkLightness = GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_12; // lightness arrows and mark = black
                        //applicationWidgetOneRowProfileListIconColor = "0"; // icon type = colorful
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

        float prefIndicatorLightnessValue = 0f;
        int prefIndicatorMonochromeValue = 0x00;
        switch (applicationWidgetOneRowProfileListArrowsMarkLightness) {
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
        if (applicationWidgetOneRowProfileListChangeColorsByNightMode &&
            applicationWidgetOneRowProfileListIconColor.equals("0")) {
            if ((Build.VERSION.SDK_INT >= 31) && applicationWidgetOneRowProfileListUseDynamicColors)
                indicatorType = DataWrapper.IT_FOR_WIDGET_DYNAMIC_COLORS;
            else
                indicatorType = DataWrapper.IT_FOR_WIDGET_NATIVE_BACKGROUND;
        }
        else
        if (applicationWidgetOneRowProfileListBackgroundType) {
            if (ColorUtils.calculateLuminance(Integer.parseInt(applicationWidgetOneRowProfileListBackgroundColor)) < 0.23)
                indicatorType = DataWrapper.IT_FOR_WIDGET_DARK_BACKGROUND;
            else
                indicatorType = DataWrapper.IT_FOR_WIDGET_LIGHT_BACKGROUND;
        } else {
            if (Integer.parseInt(applicationWidgetOneRowProfileListBackground) <= 37)
                indicatorType = DataWrapper.IT_FOR_WIDGET_DARK_BACKGROUND;
            else
                indicatorType = DataWrapper.IT_FOR_WIDGET_LIGHT_BACKGROUND;
        }


        DataWrapper dataWrapper = new DataWrapper(context.getApplicationContext(),
                    applicationWidgetOneRowProfileListIconColor.equals("1"), monochromeValue,
                    applicationWidgetOneRowProfileListCustomIconLightness,
                    indicatorType, prefIndicatorMonochromeValue, prefIndicatorLightnessValue);

        Profile profile;
        //boolean fullyStarted = PPApplication.applicationFullyStarted;
        //if ((!fullyStarted) /*|| applicationPackageReplaced*/)
        //    profile = null;
        //else
            profile = dataWrapper.getActivatedProfile(true, false);

        //try {
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

            int redText = 0xFF;
            switch (applicationWidgetOneRowProfileListArrowsMarkLightness) {
                case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_0:
                    redText = 0x00;
                    break;
                case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_12:
                    redText = 0x20;
                    break;
                case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_25:
                    redText = 0x40;
                    break;
                case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_37:
                    redText = 0x60;
                    break;
                case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_50:
                    redText = 0x80;
                    break;
                case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_62:
                    redText = 0xA0;
                    break;
                case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_75:
                    redText = 0xC0;
                    break;
                case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_87:
                    redText = 0xE0;
                    break;
                case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_100:
                    //noinspection ConstantConditions
                    redText = 0xFF;
                    break;
            }
            int greenText = redText;
            int blueText = redText;

            int restartEventsLightness = redText;

            boolean isIconResourceID;
            String iconIdentifier;
            Spannable profileName;
            if (profile != null) {
                isIconResourceID = profile.getIsIconResourceID();
                iconIdentifier = profile.getIconIdentifier();
                profileName = DataWrapperStatic.getProfileNameWithManualIndicator(profile, true, "", true, false, false, dataWrapper);
            } else {
                // create empty profile and set icon resource
                profile = new Profile();
                profile._name = context.getString(R.string.profiles_header_profile_name_no_activated);
                profile._icon = Profile.PROFILE_ICON_DEFAULT + "|1|0|0";

                profile.generateIconBitmap(context.getApplicationContext(),
                        applicationWidgetOneRowProfileListIconColor.equals("1"),
                        monochromeValue,
                        applicationWidgetOneRowProfileListCustomIconLightness);
                isIconResourceID = profile.getIsIconResourceID();
                iconIdentifier = profile.getIconIdentifier();
                profileName = new SpannableString(profile._name);
            }

            // get all OneRowWidgetProvider widgets in launcher
            //ComponentName thisWidget = new ComponentName(context, OneRowWidgetProvider.class);
            //int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);

            for (int widgetId : appWidgetIds) {

                //Bundle bundle = appWidgetManager.getAppWidgetOptions(widgetId);
                //bundle.putInt(PPApplication.BUNDLE_WIDGET_TYPE, PPApplication.WIDGET_TYPE_ONE_ROW);
                //appWidgetManager.updateAppWidgetOptions(widgetId, bundle);

//                AppWidgetProviderInfo info = appWidgetManager.getAppWidgetInfo(widgetId);

                RemoteViews remoteViews;

                if (!((Build.VERSION.SDK_INT >= 31) && applicationWidgetOneRowProfileListChangeColorsByNightMode &&
                        applicationWidgetOneRowProfileListIconColor.equals("0") && applicationWidgetOneRowProfileListUseDynamicColors)) {
                    if (applicationWidgetOneRowProfileListLayoutHeight.equals("0")) {
                        remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.one_row_widget);
                    } else if (applicationWidgetOneRowProfileListLayoutHeight.equals("1")) {
                        remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.one_row_higher_widget);
                    } else {
                        remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.one_row_highest_widget);
                    }
                } else {
                    if (applicationWidgetOneRowProfileListLayoutHeight.equals("0")) {
                        remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.one_row_widget_dn);
                    } else if (applicationWidgetOneRowProfileListLayoutHeight.equals("1")) {
                        remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.one_row_higher_widget_dn);
                    } else {
                        remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.one_row_highest_widget_dn);
                    }
                }

                int roundedBackground = 0;
                int roundedBorder = 0;
                if (PPApplication.isPixelLauncherDefault(context)) {
                    roundedBackground = R.drawable.rounded_widget_background_pixel_launcher;
                    roundedBorder = R.drawable.rounded_widget_border_pixel_launcher;
                } else if (PPApplication.isOneUILauncherDefault(context)) {
                    roundedBackground = R.drawable.rounded_widget_background_oneui_launcher;
                    roundedBorder = R.drawable.rounded_widget_border_oneui_launcher;
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
                    remoteViews.setImageViewResource(R.id.widget_one_row_background, roundedBackground);
                else
                    remoteViews.setImageViewResource(R.id.widget_one_row_background, R.drawable.ic_empty);
                if (roundedBorder != 0)
                    remoteViews.setImageViewResource(R.id.widget_one_row_rounded_border, roundedBorder);
                else
                    remoteViews.setImageViewResource(R.id.widget_one_row_rounded_border, R.drawable.ic_empty);

                remoteViews.setViewVisibility(R.id.widget_one_row_background, VISIBLE);
                remoteViews.setViewVisibility(R.id.widget_one_row_not_rounded_border, View.GONE);
                if (applicationWidgetOneRowProfileListShowBorder) {
                    remoteViews.setViewVisibility(R.id.widget_one_row_rounded_border, VISIBLE);
                }
                else {
                    remoteViews.setViewVisibility(R.id.widget_one_row_rounded_border, View.GONE);
                }
                remoteViews.setInt(R.id.widget_one_row_root, "setBackgroundColor", 0x00000000);

                if (!((Build.VERSION.SDK_INT >= 31) && applicationWidgetOneRowProfileListChangeColorsByNightMode &&
                        applicationWidgetOneRowProfileListIconColor.equals("0") && applicationWidgetOneRowProfileListUseDynamicColors)) {
                    //remoteViews.setInt(R.id.widget_one_row_background, "setColorFilter", Color.argb(0xFF, 0, 0, 0));
                    remoteViews.setInt(R.id.widget_one_row_background, "setColorFilter", Color.argb(0xFF, redBackground, greenBackground, blueBackground));
                }

                remoteViews.setInt(R.id.widget_one_row_background, "setImageAlpha", alphaBackground);

                if (applicationWidgetOneRowProfileListShowBorder) {
                    if (!((Build.VERSION.SDK_INT >= 31) && applicationWidgetOneRowProfileListChangeColorsByNightMode &&
                            applicationWidgetOneRowProfileListIconColor.equals("0") && applicationWidgetOneRowProfileListUseDynamicColors))
                        remoteViews.setInt(R.id.widget_one_row_rounded_border, "setColorFilter", Color.argb(0xFF, redBorder, greenBorder, blueBorder));
                }

                Bitmap bitmap = null;
                if (applicationWidgetOneRowProfileListIconColor.equals("0")) {
                    if (applicationWidgetOneRowProfileListChangeColorsByNightMode ||
                       ((!applicationWidgetOneRowProfileListBackgroundType) &&
                           (Integer.parseInt(applicationWidgetOneRowProfileListLightnessB) <= 25)) ||
                       (applicationWidgetOneRowProfileListBackgroundType &&
                           (ColorUtils.calculateLuminance(Integer.parseInt(applicationWidgetOneRowProfileListBackgroundColor)) < 0.23)))
                        bitmap = profile.increaseProfileIconBrightnessForContext(context, profile._iconBitmap);
                }
                if (isIconResourceID) {
                    if (bitmap != null)
                        remoteViews.setImageViewBitmap(R.id.widget_one_row_header_profile_icon, bitmap);
                    else {
                        if (profile._iconBitmap != null)
                            remoteViews.setImageViewBitmap(R.id.widget_one_row_header_profile_icon, profile._iconBitmap);
                        else {
                            //remoteViews.setImageViewResource(R.id.activate_profile_widget_icon, 0);
                            //int iconResource = context.getResources().getIdentifier(iconIdentifier, "drawable", context.PPApplication.PACKAGE_NAME);
                            int iconResource = ProfileStatic.getIconResource(iconIdentifier);
                            remoteViews.setImageViewResource(R.id.widget_one_row_header_profile_icon, iconResource);
                        }
                    }
                } else {
                    if (bitmap != null)
                        remoteViews.setImageViewBitmap(R.id.widget_one_row_header_profile_icon, bitmap);
                    else {
                        remoteViews.setImageViewBitmap(R.id.widget_one_row_header_profile_icon, profile._iconBitmap);
                    }
                }

                if (!((Build.VERSION.SDK_INT >= 31) && applicationWidgetOneRowProfileListChangeColorsByNightMode &&
                        applicationWidgetOneRowProfileListIconColor.equals("0") && applicationWidgetOneRowProfileListUseDynamicColors)) {
                    remoteViews.setTextColor(R.id.widget_one_row_header_profile_name, Color.argb(0xFF, redText, greenText, blueText));
                }
                else {
                    // must be removed android:textColor in layout
                    int color = GlobalGUIRoutines.getDynamicColor(R.attr.colorOnBackground, context);
                    if (color != 0) {
                        remoteViews.setTextColor(R.id.widget_one_row_header_profile_name, color);
                    }
                }

                remoteViews.setTextViewText(R.id.widget_one_row_header_profile_name, profileName);

                if (!((Build.VERSION.SDK_INT >= 31) && applicationWidgetOneRowProfileListChangeColorsByNightMode &&
                        applicationWidgetOneRowProfileListIconColor.equals("0") && applicationWidgetOneRowProfileListUseDynamicColors)) {
                    //if (Event.getGlobalEventsRunning() && PPApplication.getApplicationStarted(true)) {
                    bitmap = BitmapManipulator.getBitmapFromResource(R.drawable.ic_widget_restart_events, true, context);
                    bitmap = BitmapManipulator.monochromeBitmap(bitmap, restartEventsLightness);
                    remoteViews.setImageViewBitmap(R.id.widget_one_row_header_restart_events, bitmap);
                    //}
                } else {
                    // good, color of this is as in notification ;-)
                    // but must be removed android:tint in layout
                    int color = GlobalGUIRoutines.getDynamicColor(R.attr.colorSecondary, context);
                    if (color != 0) {
                        bitmap = BitmapManipulator.getBitmapFromResource(R.drawable.ic_widget_restart_events, true, context);
                        bitmap = BitmapManipulator.recolorBitmap(bitmap, color);
                        remoteViews.setImageViewBitmap(R.id.widget_one_row_header_restart_events, bitmap);
                    }
                }

                //if (Event.getGlobalEventsRunning() && PPApplication.getApplicationStarted(true)) {
                //remoteViews.setViewVisibility(R.id.widget_one_row_header_restart_events, VISIBLE);
                Intent intentRE = new Intent(context, RestartEventsFromGUIActivity.class);
                PendingIntent pIntentRE = PendingIntent.getActivity(context, 2, intentRE, PendingIntent.FLAG_UPDATE_CURRENT);
                remoteViews.setOnClickPendingIntent(R.id.widget_one_row_header_restart_events_click, pIntentRE);
                //} else
                //    remoteViews.setViewVisibility(R.id.widget_one_row_header_restart_events_click, View.GONE);

                // intent for start LauncherActivity on widget click
                Intent intent = new Intent(context, LauncherActivity.class);
                // clear all opened activities
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra(PPApplication.EXTRA_STARTUP_SOURCE, PPApplication.STARTUP_SOURCE_WIDGET);
                PendingIntent pendingIntent = PendingIntent.getActivity(context, 200, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                remoteViews.setOnClickPendingIntent(R.id.widget_one_row_header_profile_root, pendingIntent);

                // widget update
                try {
                    appWidgetManager.updateAppWidget(widgetId, remoteViews);
                    //ComponentName thisWidget = new ComponentName(context, OneRowWidgetProvider.class);
                    //appWidgetManager.updateAppWidget(thisWidget, remoteViews);
                    //appWidgetManager.partiallyUpdateAppWidget(appWidgetIds, remoteViews);
                } catch (Exception e) {
                    PPApplication.recordException(e);
                }
            }
        //} catch (Exception ee) {
        //    PPApplication.recordException(ee);
        //}

        /*if (profile != null) {
            profile.releaseIconBitmap();
            profile.releasePreferencesIndicator();
        }*/
        dataWrapper.invalidateDataWrapper();
    }

    @Override
    public void onReceive(Context context, final Intent intent) {
        final Context appContext = context;
        LocaleHelper.setApplicationLocale(appContext);

        super.onReceive(appContext, intent); // calls onUpdate, is required for widget

        String action = intent.getAction();
//        PPApplication.logE("[IN_BROADCAST] OneRowWidgetProvider.onReceive", "action="+action);

        if ((action != null) &&
                (action.equalsIgnoreCase(ACTION_REFRESH_ONEROWPROFILELISTWIDGET))) {
            AppWidgetManager manager = AppWidgetManager.getInstance(appContext);
            if (manager != null) {
                final int[] ids = manager.getAppWidgetIds(new ComponentName(appContext, OneRowProfileListWidgetProvider.class));
                if ((ids != null) && (ids.length > 0)) {
                    final AppWidgetManager appWidgetManager = manager;
                    //PPApplication.startHandlerThreadWidget();
                    //final Handler __handler = new Handler(PPApplication.handlerThreadWidget.getLooper());
                    //__handler.post(new PPHandlerThreadRunnable(context, manager) {
                    //__handler.post(() -> {
                    Runnable runnable = () -> {
//                            PPApplication.logE("[IN_EXECUTOR] PPApplication.startHandlerThreadWidget", "START run - from=OneRowWidgetProvider.onReceive");

                        //Context appContext= appContextWeakRef.get();
                        //AppWidgetManager appWidgetManager = appWidgetManagerWeakRef.get();

                        //if ((appContext != null) && (appWidgetManager != null)) {
                            _onUpdate(appContext, appWidgetManager, ids);
                        //}
                    }; //);
                    PPApplication.createDelayedGuiExecutor();
                    PPApplication.delayedGuiExecutor.submit(runnable);
                }
            }
        }
    }
/*
    @Override
    public void onDeleted (Context context, int[] appWidgetIds) {
    }

    @Override
    public void onDisabled (Context context) {
    }

    @Override
    public void onEnabled (Context context) {
    }

    @Override
    public void onRestored (Context context,
                            int[] oldWidgetIds,
                            int[] newWidgetIds) {
    }

    @Override
    public void onAppWidgetOptionsChanged (Context context,
                                           AppWidgetManager appWidgetManager,
                                           int appWidgetId,
                                           Bundle newOptions) {
    }
*/
    static void updateWidgets(Context context/*, boolean refresh*/) {
        /*String applicationWidgetOneRowIconLightness;
        String applicationWidgetOneRowIconColor;
        boolean applicationWidgetOneRowCustomIconLightness;
        boolean applicationWidgetOneRowPrefIndicator;
        synchronized (PPApplication.applicationPreferencesMutex) {
            applicationWidgetOneRowIconLightness = ApplicationPreferences.applicationWidgetOneRowIconLightness;
            applicationWidgetOneRowIconColor = ApplicationPreferences.applicationWidgetOneRowIconColor;
            applicationWidgetOneRowCustomIconLightness = ApplicationPreferences.applicationWidgetOneRowCustomIconLightness;
            applicationWidgetOneRowPrefIndicator = ApplicationPreferences.applicationWidgetOneRowPrefIndicator;
        }

        int monochromeValue = 0xFF;
        switch (applicationWidgetOneRowIconLightness) {
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
                monochromeValue = 0xFF;
                break;
        }

        DataWrapper dataWrapper = new DataWrapper(context.getApplicationContext(),
                applicationWidgetOneRowIconColor.equals("1"), monochromeValue,
                applicationWidgetOneRowCustomIconLightness);
        Profile profile = dataWrapper.getActivatedProfile(true, applicationWidgetOneRowPrefIndicator);
        */

        /*DataWrapper dataWrapper = new DataWrapper(context.getApplicationContext(), false, 0, false);
        Profile profile = dataWrapper.getActivatedProfile(false, false);

        String pName;

        if (profile != null)
            pName = DataWrapper.getProfileNameWithManualIndicatorAsString(profile, true, "", true, false, false, dataWrapper);
        else
            pName = context.getString(R.string.profiles_header_profile_name_no_activated);

        if (!refresh) {
            String pNameWidget = PPApplication.prefWidgetProfileName2;

            if (!pNameWidget.isEmpty()) {
                if (pName.equals(pNameWidget)) {
                    return;
                }
            }
        }

        PPApplication.setWidgetProfileName(context, 2, pName);*/

//        PPApplication.logE("[LOCAL_BROADCAST_CALL] OneRowWidgetProvider.updateWidgets", "xxx");
        Intent intent3 = new Intent(ACTION_REFRESH_ONEROWPROFILELISTWIDGET);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent3);

        //Intent intent3 = new Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        //context.sendBroadcast(intent3);

        //Intent intent = new Intent(context, OneRowWidgetProvider.class);
        //intent.setAction(ACTION_REFRESH_ONEROWWIDGET);
        //context.sendBroadcast(intent);

        /*AppWidgetManager manager = AppWidgetManager.getInstance(context.getApplicationContext());
        if (manager != null) {
            int[] ids = manager.getAppWidgetIds(new ComponentName(context, OneRowWidgetProvider.class));
            if ((ids != null) && (ids.length > 0))
                _onUpdate(context.getApplicationContext(), manager, profile, dataWrapper, ids);
        }*/
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

}
