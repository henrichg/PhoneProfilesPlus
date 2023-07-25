package sk.henrichg.phoneprofilesplus;

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
import android.text.Spannable;
import android.text.SpannableString;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import androidx.core.graphics.ColorUtils;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class IconWidgetProvider extends AppWidgetProvider {

    static final String ACTION_REFRESH_ICONWIDGET = PPApplication.PACKAGE_NAME + ".ACTION_REFRESH_ICONWIDGET";

    public void onUpdate(Context context, final AppWidgetManager appWidgetManager, final int[] appWidgetIds)
    {
        //super.onUpdate(context, appWidgetManager, appWidgetIds);
//        PPApplicationStatic.logE("[IN_LISTENER] IconWidgetProvider.onUpdate", "xxx");
        if (appWidgetIds.length > 0) {
            final Context appContext = context;
            LocaleHelper.setApplicationLocale(appContext);
            //PPApplication.startHandlerThreadWidget();
            //final Handler __handler = new Handler(PPApplication.handlerThreadWidget.getLooper());
            //__handler.post(new PPHandlerThreadRunnable(context, appWidgetManager) {
            //__handler.post(() -> {
            Runnable runnable = () -> {
//                    PPApplicationStatic.logE("[IN_EXECUTOR] PPApplication.startHandlerThreadWidget", "START run - from=IconWidgetProvider.onUpdate");

                //Context appContext= appContextWeakRef.get();
                //AppWidgetManager appWidgetManager = appWidgetManagerWeakRef.get();

                //if ((appContext != null) && (appWidgetManager != null)) {
                    _onUpdate(appContext, appWidgetManager, appWidgetIds);
                //}
            }; //);
            PPApplicationStatic.createDelayedGuiExecutor();
            PPApplication.delayedGuiExecutor.submit(runnable);
        }
    }

    private static void _onUpdate(Context context, AppWidgetManager appWidgetManager,
                          /*Profile _profile, DataWrapper _dataWrapper,*/ int[] appWidgetIds) {

        String applicationWidgetIconLightness;
        String applicationWidgetIconColor;
        boolean applicationWidgetIconCustomIconLightness;
        boolean applicationWidgetIconHideProfileName;
        boolean applicationWidgetIconBackgroundType;
        String applicationWidgetIconBackgroundColor;
        String applicationWidgetIconLightnessB;
        String applicationWidgetIconBackground;
        boolean applicationWidgetIconShowBorder;
        String applicationWidgetIconLightnessBorder;
        boolean applicationWidgetIconRoundedCorners;
        String applicationWidgetIconLightnessT;
        boolean applicationWidgetIconShowProfileDuration;
        int applicationWidgetIconRoundedCornersRadius;
        boolean applicationWidgetIconChangeColorsByNightMode;
        boolean applicationWidgetIconUseDynamicColors;
        String applicationWidgetIconBackgroundColorNightModeOff;
        String applicationWidgetIconBackgroundColorNightModeOn;
        String applicationWidgetIconLayoutHeight;
        boolean applicationWidgetIconFillBackground;
        boolean applicationWidgetIconFillBackgroundHeight;
        boolean applicationWidgetIconFillBackgroundWidth;

        synchronized (PPApplication.applicationPreferencesMutex) {

            applicationWidgetIconLightness = ApplicationPreferences.applicationWidgetIconLightness;
            applicationWidgetIconColor = ApplicationPreferences.applicationWidgetIconColor;
            applicationWidgetIconCustomIconLightness = ApplicationPreferences.applicationWidgetIconCustomIconLightness;
            applicationWidgetIconHideProfileName = ApplicationPreferences.applicationWidgetIconHideProfileName;
            applicationWidgetIconBackgroundType = ApplicationPreferences.applicationWidgetIconBackgroundType;
            applicationWidgetIconBackgroundColor = ApplicationPreferences.applicationWidgetIconBackgroundColor;
            applicationWidgetIconLightnessB = ApplicationPreferences.applicationWidgetIconLightnessB;
            applicationWidgetIconBackground = ApplicationPreferences.applicationWidgetIconBackground;
            applicationWidgetIconShowBorder = ApplicationPreferences.applicationWidgetIconShowBorder;
            applicationWidgetIconLightnessBorder = ApplicationPreferences.applicationWidgetIconLightnessBorder;
            applicationWidgetIconLightnessT = ApplicationPreferences.applicationWidgetIconLightnessT;
            applicationWidgetIconShowProfileDuration = ApplicationPreferences.applicationWidgetIconShowProfileDuration;
            applicationWidgetIconRoundedCorners = ApplicationPreferences.applicationWidgetIconRoundedCorners;
            applicationWidgetIconRoundedCornersRadius = ApplicationPreferences.applicationWidgetIconRoundedCornersRadius;

            if (Build.VERSION.SDK_INT < 30)
                applicationWidgetIconChangeColorsByNightMode = false;
            else
                applicationWidgetIconChangeColorsByNightMode = ApplicationPreferences.applicationWidgetIconChangeColorsByNightMode;

            applicationWidgetIconUseDynamicColors = ApplicationPreferences.applicationWidgetIconUseDynamicColors;
            applicationWidgetIconBackgroundColorNightModeOff = ApplicationPreferences.applicationWidgetIconBackgroundColorNightModeOff;
            applicationWidgetIconBackgroundColorNightModeOn = ApplicationPreferences.applicationWidgetIconBackgroundColorNightModeOn;
            applicationWidgetIconLayoutHeight = ApplicationPreferences.applicationWidgetIconLayoutHeight;
            applicationWidgetIconFillBackground = ApplicationPreferences.applicationWidgetIconFillBackground;
            applicationWidgetIconFillBackgroundHeight = applicationWidgetIconFillBackground;
            applicationWidgetIconFillBackgroundWidth = applicationWidgetIconFillBackground;

            // "Rounded corners" parameter is removed, is forced to true
            if (!applicationWidgetIconRoundedCorners) {
                //applicationWidgetIconRoundedCorners = true;
                applicationWidgetIconRoundedCornersRadius = 1;
            }

            if (Build.VERSION.SDK_INT >= 30) {
                if (PPApplicationStatic.isPixelLauncherDefault(context) ||
                        PPApplicationStatic.isOneUILauncherDefault(context) ||
                        PPApplicationStatic.isMIUILauncherDefault(context)) {
                    ApplicationPreferences.applicationWidgetIconRoundedCorners = true;
                    ApplicationPreferences.applicationWidgetIconRoundedCornersRadius = 15;
                    //ApplicationPreferences.applicationWidgetChangeColorsByNightMode = true;
                    SharedPreferences.Editor editor = ApplicationPreferences.getEditor(context);
                    editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_ROUNDED_CORNERS,
                            ApplicationPreferences.applicationWidgetIconRoundedCorners);
                    editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_ROUNDED_CORNERS_RADIUS,
                            String.valueOf(ApplicationPreferences.applicationWidgetIconRoundedCornersRadius));
                    //editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_CHANGE_COLOR_BY_NIGHT_MODE,
                    //        ApplicationPreferences.applicationWidgetChangeColorsByNightMode);
                    editor.apply();
                    //applicationWidgetIconRoundedCorners = ApplicationPreferences.applicationWidgetIconRoundedCorners;
                    applicationWidgetIconRoundedCornersRadius = ApplicationPreferences.applicationWidgetIconRoundedCornersRadius;
                    //applicationWidgetChangeColorsByNightMode = ApplicationPreferences.applicationWidgetChangeColorsByNightMode;
                }
                if (Build.VERSION.SDK_INT < 31)
                    applicationWidgetIconUseDynamicColors = false;
                if ((/*PPApplication.isPixelLauncherDefault(context) ||*/
                        applicationWidgetIconChangeColorsByNightMode &&
                        (!applicationWidgetIconUseDynamicColors))) {
                    boolean nightModeOn = GlobalGUIRoutines.isNightModeEnabled(context.getApplicationContext());
                    //int nightModeFlags =
                    //        context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
                    //switch (nightModeFlags) {
                    //noinspection IfStatementWithIdenticalBranches
                    if (nightModeOn) {
                        //case Configuration.UI_MODE_NIGHT_YES:

                        //applicationWidgetIconBackground = GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_100; // fully opaque
                        applicationWidgetIconBackgroundType = true; // background type = color
                        applicationWidgetIconBackgroundColor = String.valueOf(ColorChooserPreference.parseValue(applicationWidgetIconBackgroundColorNightModeOn)); // color of background
                        //applicationWidgetIconShowBorder = false; // do not show border
                        applicationWidgetIconLightnessBorder = GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_100;
                        applicationWidgetIconLightnessT = GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_87; // lightness of text = white
                        //applicationWidgetIconColor = "0"; // icon type = colorful
                        applicationWidgetIconLightness = GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_75;
                        //break;
                    } else {
                        //case Configuration.UI_MODE_NIGHT_NO:
                        //case Configuration.UI_MODE_NIGHT_UNDEFINED:

                        //applicationWidgetIconBackground = GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_100; // fully opaque
                        applicationWidgetIconBackgroundType = true; // background type = color
                        applicationWidgetIconBackgroundColor = String.valueOf(ColorChooserPreference.parseValue(applicationWidgetIconBackgroundColorNightModeOff)); // color of background
                        //applicationWidgetIconShowBorder = false; // do not show border
                        applicationWidgetIconLightnessBorder = "0";
                        applicationWidgetIconLightnessT = GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_12; // lightness of text = black
                        //applicationWidgetIconColor = "0"; // icon type = colorful
                        applicationWidgetIconLightness = GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_62;
                        //break;
                    }
                }
            }
        }

        int monochromeValue = 0xFF;
        switch (applicationWidgetIconLightness) {
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

        //DataWrapper dataWrapper = _dataWrapper;
        //Profile profile = _profile;
        //if (dataWrapper == null) {
        DataWrapper dataWrapper = new DataWrapper(context.getApplicationContext(),
                    applicationWidgetIconColor.equals("1"),
                    monochromeValue,
                    applicationWidgetIconCustomIconLightness,
                    DataWrapper.IT_FOR_WIDGET, 0, 0f);

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
            if (applicationWidgetIconBackgroundType) {
                int bgColor = Integer.parseInt(applicationWidgetIconBackgroundColor);
                redBackground = Color.red(bgColor);
                greenBackground = Color.green(bgColor);
                blueBackground = Color.blue(bgColor);
            } else {
                switch (applicationWidgetIconLightnessB) {
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
            switch (applicationWidgetIconBackground) {
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
            if (applicationWidgetIconShowBorder) {
                switch (applicationWidgetIconLightnessBorder) {
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
            switch (applicationWidgetIconLightnessT) {
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

            boolean isIconResourceID;
            String iconIdentifier;
            Spannable profileName;
            if (profile != null) {
                isIconResourceID = profile.getIsIconResourceID();
                iconIdentifier = profile.getIconIdentifier();
                if (applicationWidgetIconShowProfileDuration)
                    profileName = DataWrapperStatic.getProfileNameWithManualIndicator(profile, false, "", true, true, true, dataWrapper);
                else
                    profileName = DataWrapperStatic.getProfileNameWithManualIndicator(profile, false, "", false, true, false, dataWrapper);
            } else {
                // create empty profile and set icon resource
                profile = new Profile();
                profile._name = context.getString(R.string.profiles_header_profile_name_no_activated);
                profile._icon = Profile.PROFILE_ICON_DEFAULT + "|1|0|0";

                profile.generateIconBitmap(context.getApplicationContext(),
                        applicationWidgetIconColor.equals("1"), monochromeValue,
                        applicationWidgetIconCustomIconLightness);
                isIconResourceID = profile.getIsIconResourceID();
                iconIdentifier = profile.getIconIdentifier();
                profileName = new SpannableString(profile._name);
            }

            // get all IconWidgetProvider widgets in launcher

            // prepare view for widget update
            for (int widgetId : appWidgetIds)
            {
                Bundle options = appWidgetManager.getAppWidgetOptions(widgetId);
                int maxHeight = GlobalGUIRoutines.dpToPx(options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT));
                int maxWidth = GlobalGUIRoutines.dpToPx(options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH));
//                Log.e("IconWidgetProvider._onUpdate", "maxHeight="+maxHeight);
//                Log.e("IconWidgetProvider._onUpdate", "maxWidth="+maxWidth);

                /*
                int minHeight = GlobalGUIRoutines.dpToPx(options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT));
                int minWidth = GlobalGUIRoutines.dpToPx(options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH));
                Log.e("IconWidgetProvider._onUpdate", "minHeight="+minHeight);
                Log.e("IconWidgetProvider._onUpdate", "minWidth="+minWidth);
                */

                //bundle.putInt(PPApplication.BUNDLE_WIDGET_TYPE, PPApplication.WIDGET_TYPE_ICON);
                //appWidgetManager.updateAppWidgetOptions(widgetId, bundle);

                RemoteViews remoteViews;

                float configuredHeight;
                if (!((Build.VERSION.SDK_INT >= 31) && applicationWidgetIconChangeColorsByNightMode &&
                        applicationWidgetIconColor.equals("0") && applicationWidgetIconUseDynamicColors)) {
                    if (applicationWidgetIconLayoutHeight.equals("0")) {
                        configuredHeight = context.getResources().getDimension(R.dimen.icon_widget_height);
                        //configuredHeight = GlobalGUIRoutines.getRawDimensionInDp(context.getResources(), R.dimen.icon_widget_height);
//                        Log.e("IconWidgetProvider._onUpdate", "configuredHeight="+configuredHeight);
                        if ((!applicationWidgetIconBackground.equals(GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_0)) ||
                                applicationWidgetIconShowBorder) {
//                            Log.e("IconWidgetProvider._onUpdate", "**** fill ****");
                            if ((maxHeight < configuredHeight))
                                applicationWidgetIconFillBackgroundHeight = true;
                            if ((maxWidth < configuredHeight))
                                applicationWidgetIconFillBackgroundWidth = true;
                        }
                        if (applicationWidgetIconHideProfileName) {
                            if (applicationWidgetIconFillBackground ||
                                    (applicationWidgetIconFillBackgroundHeight &&
                                            applicationWidgetIconFillBackgroundWidth))
                                remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_icon_fill_no_profile_name);
                            else
                            if (applicationWidgetIconFillBackgroundHeight)
                                remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_icon_fill_height_no_profile_name);
                            else
                            if (applicationWidgetIconFillBackgroundWidth)
                                remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_icon_fill_width_no_profile_name);
                            else
                                remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_icon_no_profile_name);
                        } else {
                            if ((profile._endOfActivationType == 0) &&
                                    (profile._duration > 0) &&
                                    (applicationWidgetIconShowProfileDuration)) {
                                if (applicationWidgetIconFillBackground ||
                                        (applicationWidgetIconFillBackgroundHeight &&
                                                applicationWidgetIconFillBackgroundWidth))
                                    remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_icon_fill);
                                else
                                if (applicationWidgetIconFillBackgroundHeight)
                                    remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_icon_fill_height);
                                else
                                if (applicationWidgetIconFillBackgroundWidth)
                                    remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_icon_fill_width);
                                else
                                    remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_icon);
                            } else if ((profile._endOfActivationType == 1) &&
                                    (applicationWidgetIconShowProfileDuration)) {
                                if (applicationWidgetIconFillBackground ||
                                        (applicationWidgetIconFillBackgroundHeight &&
                                                applicationWidgetIconFillBackgroundWidth))
                                    remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_icon_fill);
                                else
                                if (applicationWidgetIconFillBackgroundHeight)
                                    remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_icon_fill_height);
                                else
                                if (applicationWidgetIconFillBackgroundWidth)
                                    remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_icon_fill_width);
                                else
                                    remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_icon);
                            } else {
                                if (applicationWidgetIconFillBackground ||
                                        (applicationWidgetIconFillBackgroundHeight &&
                                                applicationWidgetIconFillBackgroundWidth))
                                    remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_icon_fill_one_line_text);
                                else
                                if (applicationWidgetIconFillBackgroundHeight)
                                    remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_icon_fill_height_one_line_text);
                                else
                                if (applicationWidgetIconFillBackgroundWidth)
                                    remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_icon_fill_width_one_line_text);
                                else
                                    remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_icon_one_line_text);
                            }
                        }
                    } else if (applicationWidgetIconLayoutHeight.equals("1")) {
                        configuredHeight = context.getResources().getDimension(R.dimen.icon_widget_height_higher);
                        //configuredHeight = GlobalGUIRoutines.getRawDimensionInDp(context.getResources(), R.dimen.icon_widget_height_higher);
//                        Log.e("IconWidgetProvider._onUpdate", "configuredHeight="+configuredHeight);
                        if ((!applicationWidgetIconBackground.equals(GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_0)) ||
                                applicationWidgetIconShowBorder) {
//                            Log.e("IconWidgetProvider._onUpdate", "**** fill ****");
                            if ((maxHeight < configuredHeight))
                                applicationWidgetIconFillBackgroundHeight = true;
                            if ((maxWidth < configuredHeight))
                                applicationWidgetIconFillBackgroundWidth = true;
                        }
                        if (applicationWidgetIconHideProfileName) {
                            if (applicationWidgetIconFillBackground ||
                                    (applicationWidgetIconFillBackgroundHeight &&
                                            applicationWidgetIconFillBackgroundWidth))
                                remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_icon_higher_fill_no_profile_name);
                            else
                            if (applicationWidgetIconFillBackgroundHeight)
                                remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_icon_higher_fill_height_no_profile_name);
                            else
                            if (applicationWidgetIconFillBackgroundWidth)
                                remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_icon_higher_fill_width_no_profile_name);
                            else
                                remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_icon_higher_no_profile_name);
                        } else {
                            if ((profile._endOfActivationType == 0) &&
                                    (profile._duration > 0) &&
                                    (applicationWidgetIconShowProfileDuration)) {
                                if (applicationWidgetIconFillBackground ||
                                        (applicationWidgetIconFillBackgroundHeight &&
                                                applicationWidgetIconFillBackgroundWidth))
                                    remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_icon_higher_fill);
                                else
                                if (applicationWidgetIconFillBackgroundHeight)
                                    remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_icon_higher_fill_height);
                                else
                                if (applicationWidgetIconFillBackgroundWidth)
                                    remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_icon_higher_fill_width);
                                else
                                    remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_icon_higher);
                            } else if ((profile._endOfActivationType == 1) &&
                                    (applicationWidgetIconShowProfileDuration)) {
                                if (applicationWidgetIconFillBackground ||
                                        (applicationWidgetIconFillBackgroundHeight &&
                                                applicationWidgetIconFillBackgroundWidth))
                                    remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_icon_higher_fill);
                                else
                                if (applicationWidgetIconFillBackgroundHeight)
                                    remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_icon_higher_fill_height);
                                else
                                if (applicationWidgetIconFillBackgroundWidth)
                                    remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_icon_higher_fill_width);
                                else
                                    remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_icon_higher);
                            } else {
                                if (applicationWidgetIconFillBackground ||
                                        (applicationWidgetIconFillBackgroundHeight &&
                                                applicationWidgetIconFillBackgroundWidth))
                                    remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_icon_higher_fill_one_line_text);
                                else
                                if (applicationWidgetIconFillBackgroundHeight)
                                    remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_icon_higher_fill_height_one_line_text);
                                else
                                if (applicationWidgetIconFillBackgroundWidth)
                                    remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_icon_higher_fill_width_one_line_text);
                                else
                                    remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_icon_higher_one_line_text);
                            }
                        }
                    } else if (applicationWidgetIconLayoutHeight.equals("2")) {
                        configuredHeight = context.getResources().getDimension(R.dimen.icon_widget_height_highest);
                        //configuredHeight = GlobalGUIRoutines.getRawDimensionInDp(context.getResources(), R.dimen.icon_widget_height_highest);
//                        Log.e("IconWidgetProvider._onUpdate", "configuredHeight="+configuredHeight);
                        if ((!applicationWidgetIconBackground.equals(GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_0)) ||
                                applicationWidgetIconShowBorder) {
//                            Log.e("IconWidgetProvider._onUpdate", "**** fill ****");
                            if ((maxHeight < configuredHeight))
                                applicationWidgetIconFillBackgroundHeight = true;
                            if ((maxWidth < configuredHeight))
                                applicationWidgetIconFillBackgroundWidth = true;
                        }
                        if (applicationWidgetIconHideProfileName) {
                            if (applicationWidgetIconFillBackground ||
                                    (applicationWidgetIconFillBackgroundHeight &&
                                            applicationWidgetIconFillBackgroundWidth))
                                remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_icon_highest_fill_no_profile_name);
                            else
                            if (applicationWidgetIconFillBackgroundHeight)
                                remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_icon_highest_fill_height_no_profile_name);
                            else
                            if (applicationWidgetIconFillBackgroundWidth)
                                remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_icon_highest_fill_width_no_profile_name);
                            else
                                remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_icon_highest_no_profile_name);
                        } else {
                            if ((profile._endOfActivationType == 0) &&
                                    (profile._duration > 0) &&
                                    (applicationWidgetIconShowProfileDuration)) {
                                if (applicationWidgetIconFillBackground ||
                                        (applicationWidgetIconFillBackgroundHeight &&
                                                applicationWidgetIconFillBackgroundWidth))
                                    remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_icon_highest_fill);
                                else
                                if (applicationWidgetIconFillBackgroundHeight)
                                    remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_icon_highest_fill_height);
                                else
                                if (applicationWidgetIconFillBackgroundWidth)
                                    remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_icon_highest_fill_width);
                                else
                                    remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_icon_highest);
                            } else if ((profile._endOfActivationType == 1) &&
                                    (applicationWidgetIconShowProfileDuration)) {
                                if (applicationWidgetIconFillBackground ||
                                        (applicationWidgetIconFillBackgroundHeight &&
                                                applicationWidgetIconFillBackgroundWidth))
                                    remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_icon_highest_fill);
                                else
                                if (applicationWidgetIconFillBackgroundHeight)
                                    remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_icon_highest_fill_height);
                                else
                                if (applicationWidgetIconFillBackgroundWidth)
                                    remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_icon_highest_fill_width);
                                else
                                    remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_icon_highest);
                            } else {
                                if (applicationWidgetIconFillBackground ||
                                        (applicationWidgetIconFillBackgroundHeight &&
                                                applicationWidgetIconFillBackgroundWidth))
                                    remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_icon_highest_fill_one_line_text);
                                else
                                if (applicationWidgetIconFillBackgroundHeight)
                                    remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_icon_highest_fill_height_one_line_text);
                                else
                                if (applicationWidgetIconFillBackgroundWidth)
                                    remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_icon_highest_fill_width_one_line_text);
                                else
                                    remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_icon_highest_one_line_text);
                            }
                        }
                    } else  {
                        configuredHeight = context.getResources().getDimension(R.dimen.icon_widget_height_more_compact);
                        //configuredHeight = GlobalGUIRoutines.getRawDimensionInDp(context.getResources(), R.dimen.icon_widget_height_highest);
//                        Log.e("IconWidgetProvider._onUpdate", "configuredHeight="+configuredHeight);
                        if ((!applicationWidgetIconBackground.equals(GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_0)) ||
                                applicationWidgetIconShowBorder) {
//                            Log.e("IconWidgetProvider._onUpdate", "**** fill ****");
                            if ((maxHeight < configuredHeight))
                                applicationWidgetIconFillBackgroundHeight = true;
                            if ((maxWidth < configuredHeight))
                                applicationWidgetIconFillBackgroundWidth = true;
                        }
                        if (applicationWidgetIconHideProfileName) {
                            if (applicationWidgetIconFillBackground ||
                                    (applicationWidgetIconFillBackgroundHeight &&
                                            applicationWidgetIconFillBackgroundWidth))
                                remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_icon_more_compact_fill_no_profile_name);
                            else
                            if (applicationWidgetIconFillBackgroundHeight)
                                remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_icon_more_compact_fill_height_no_profile_name);
                            else
                            if (applicationWidgetIconFillBackgroundWidth)
                                remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_icon_more_compact_fill_width_no_profile_name);
                            else
                                remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_icon_more_compact_no_profile_name);
                        } else {
                            if ((profile._endOfActivationType == 0) &&
                                    (profile._duration > 0) &&
                                    (applicationWidgetIconShowProfileDuration)) {
                                if (applicationWidgetIconFillBackground ||
                                        (applicationWidgetIconFillBackgroundHeight &&
                                                applicationWidgetIconFillBackgroundWidth))
                                    remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_icon_more_compact_fill);
                                else
                                if (applicationWidgetIconFillBackgroundHeight)
                                    remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_icon_more_compact_fill_height);
                                else
                                if (applicationWidgetIconFillBackgroundWidth)
                                    remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_icon_more_compact_fill_width);
                                else
                                    remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_icon_more_compact);
                            } else if ((profile._endOfActivationType == 1) &&
                                    (applicationWidgetIconShowProfileDuration)) {
                                if (applicationWidgetIconFillBackground ||
                                        (applicationWidgetIconFillBackgroundHeight &&
                                                applicationWidgetIconFillBackgroundWidth))
                                    remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_icon_more_compact_fill);
                                else
                                if (applicationWidgetIconFillBackgroundHeight)
                                    remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_icon_more_compact_fill_height);
                                else
                                if (applicationWidgetIconFillBackgroundWidth)
                                    remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_icon_more_compact_fill_width);
                                else
                                    remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_icon_more_compact);
                            } else {
                                if (applicationWidgetIconFillBackground ||
                                        (applicationWidgetIconFillBackgroundHeight &&
                                                applicationWidgetIconFillBackgroundWidth))
                                    remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_icon_more_compact_fill_one_line_text);
                                else
                                if (applicationWidgetIconFillBackgroundHeight)
                                    remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_icon_more_compact_fill_height_one_line_text);
                                else
                                if (applicationWidgetIconFillBackgroundWidth)
                                    remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_icon_more_compact_fill_width_one_line_text);
                                else
                                    remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_icon_more_compact_one_line_text);
                            }
                        }
                    }
                } else {
//                    Log.e("IconWidgetProvider._onUpdate", "**** applicationWidgetIconLayoutHeight="+applicationWidgetIconLayoutHeight);
                    if (applicationWidgetIconLayoutHeight.equals("0")) {
                        configuredHeight = context.getResources().getDimension(R.dimen.icon_widget_height);
                        //configuredHeight = GlobalGUIRoutines.getRawDimensionInDp(context.getResources(), R.dimen.icon_widget_height);
//                        Log.e("IconWidgetProvider._onUpdate", "configuredHeight="+configuredHeight);
                        if ((!applicationWidgetIconBackground.equals(GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_0)) ||
                                applicationWidgetIconShowBorder) {
//                            Log.e("IconWidgetProvider._onUpdate", "**** fill ****");
                            if ((maxHeight < configuredHeight))
                                applicationWidgetIconFillBackgroundHeight = true;
                            if ((maxWidth < configuredHeight))
                                applicationWidgetIconFillBackgroundWidth = true;
                        }
                        if (applicationWidgetIconHideProfileName) {
                            if (applicationWidgetIconFillBackground ||
                                    (applicationWidgetIconFillBackgroundHeight &&
                                            applicationWidgetIconFillBackgroundWidth))
                                remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_icon_fill_no_profile_name_dn);
                            else
                            if (applicationWidgetIconFillBackgroundHeight)
                                remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_icon_fill_height_no_profile_name_dn);
                            else
                            if (applicationWidgetIconFillBackgroundWidth)
                                remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_icon_fill_width_no_profile_name_dn);
                            else
                                remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_icon_no_profile_name_dn);
                        } else {
                            if ((profile._endOfActivationType == 0) &&
                                    (profile._duration > 0) &&
                                    (applicationWidgetIconShowProfileDuration)) {
                                if (applicationWidgetIconFillBackground ||
                                        (applicationWidgetIconFillBackgroundHeight &&
                                                applicationWidgetIconFillBackgroundWidth))
                                    remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_icon_fill_dn);
                                else
                                if (applicationWidgetIconFillBackgroundHeight)
                                    remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_icon_fill_height_dn);
                                else
                                if (applicationWidgetIconFillBackgroundWidth)
                                    remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_icon_fill_width_dn);
                                else
                                    remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_icon_dn);
                            } else if ((profile._endOfActivationType == 1) &&
                                    (applicationWidgetIconShowProfileDuration)) {
                                if (applicationWidgetIconFillBackground ||
                                        (applicationWidgetIconFillBackgroundHeight &&
                                                applicationWidgetIconFillBackgroundWidth))
                                    remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_icon_fill_dn);
                                else
                                if (applicationWidgetIconFillBackgroundHeight)
                                    remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_icon_fill_height_dn);
                                else
                                if (applicationWidgetIconFillBackgroundWidth)
                                    remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_icon_fill_width_dn);
                                else
                                    remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_icon_dn);
                            } else {
                                if (applicationWidgetIconFillBackground ||
                                        (applicationWidgetIconFillBackgroundHeight &&
                                                applicationWidgetIconFillBackgroundWidth))
                                    remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_icon_fill_one_line_text_dn);
                                else
                                if (applicationWidgetIconFillBackgroundHeight)
                                    remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_icon_fill_height_one_line_text_dn);
                                else
                                if (applicationWidgetIconFillBackgroundWidth)
                                    remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_icon_fill_width_one_line_text_dn);
                                else
                                    remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_icon_one_line_text_dn);
                            }
                        }
                    } else if (applicationWidgetIconLayoutHeight.equals("1")) {
                        configuredHeight = context.getResources().getDimension(R.dimen.icon_widget_height_higher);
                        //configuredHeight = GlobalGUIRoutines.getRawDimensionInDp(context.getResources(), R.dimen.icon_widget_height_higher);
//                        Log.e("IconWidgetProvider._onUpdate", "configuredHeight="+configuredHeight);
                        if ((!applicationWidgetIconBackground.equals(GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_0)) ||
                                applicationWidgetIconShowBorder) {
//                            Log.e("IconWidgetProvider._onUpdate", "**** fill ****");
                            if ((maxHeight < configuredHeight))
                                applicationWidgetIconFillBackgroundHeight = true;
                            if ((maxWidth < configuredHeight))
                                applicationWidgetIconFillBackgroundWidth = true;
                        }
                        if (applicationWidgetIconHideProfileName) {
                            if (applicationWidgetIconFillBackground ||
                                    (applicationWidgetIconFillBackgroundHeight &&
                                            applicationWidgetIconFillBackgroundWidth))
                                remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_icon_higher_fill_no_profile_name_dn);
                            else
                            if (applicationWidgetIconFillBackgroundHeight)
                                remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_icon_higher_fill_height_no_profile_name_dn);
                            else
                            if (applicationWidgetIconFillBackgroundWidth)
                                remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_icon_higher_fill_width_no_profile_name_dn);
                            else
                                remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_icon_higher_no_profile_name_dn);
                        } else {
//                            Log.e("IconWidgetProvider._onUpdate", "**** _endOfActivationType="+profile._endOfActivationType);
                            if ((profile._endOfActivationType == 0) &&
                                    (profile._duration > 0) &&
                                    (applicationWidgetIconShowProfileDuration)) {
                                if (applicationWidgetIconFillBackground ||
                                        (applicationWidgetIconFillBackgroundHeight &&
                                                applicationWidgetIconFillBackgroundWidth))
                                    remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_icon_higher_fill_dn);
                                else
                                if (applicationWidgetIconFillBackgroundHeight)
                                    remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_icon_higher_fill_height_dn);
                                else
                                if (applicationWidgetIconFillBackgroundWidth)
                                    remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_icon_higher_fill_width_dn);
                                else
                                    remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_icon_higher_dn);
                            } else if ((profile._endOfActivationType == 1) &&
                                    (applicationWidgetIconShowProfileDuration)) {
                                if (applicationWidgetIconFillBackground ||
                                        (applicationWidgetIconFillBackgroundHeight &&
                                                applicationWidgetIconFillBackgroundWidth))
                                    remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_icon_higher_fill_dn);
                                else
                                if (applicationWidgetIconFillBackgroundHeight)
                                    remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_icon_higher_fill_height_dn);
                                else
                                if (applicationWidgetIconFillBackgroundWidth)
                                    remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_icon_higher_fill_width_dn);
                                else
                                    remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_icon_higher_dn);
                            } else {
                                if (applicationWidgetIconFillBackground ||
                                        (applicationWidgetIconFillBackgroundHeight &&
                                                applicationWidgetIconFillBackgroundWidth)) {
                                    remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_icon_higher_fill_one_line_text_dn);
//                                    Log.e("IconWidgetProvider._onUpdate", "**** layout 1 ****");
                                }
                                else
                                if (applicationWidgetIconFillBackgroundHeight) {
                                    remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_icon_higher_fill_height_one_line_text_dn);
//                                    Log.e("IconWidgetProvider._onUpdate", "**** layout 2 ****");
                                }
                                else
                                if (applicationWidgetIconFillBackgroundWidth) {
                                    remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_icon_higher_fill_width_one_line_text_dn);
//                                    Log.e("IconWidgetProvider._onUpdate", "**** layout 3 ****");
                                }
                                else {
                                    remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_icon_higher_one_line_text_dn);
//                                    Log.e("IconWidgetProvider._onUpdate", "**** layout 4 ****");
                                }
                            }
                        }
                    } else if (applicationWidgetIconLayoutHeight.equals("2")) {
                        configuredHeight = context.getResources().getDimension(R.dimen.icon_widget_height_highest);
                        //configuredHeight = GlobalGUIRoutines.getRawDimensionInDp(context.getResources(), R.dimen.icon_widget_height_highest);
//                        Log.e("IconWidgetProvider._onUpdate", "configuredHeight="+configuredHeight);
                        if ((!applicationWidgetIconBackground.equals(GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_0)) ||
                                applicationWidgetIconShowBorder) {
//                            Log.e("IconWidgetProvider._onUpdate", "**** fill ****");
                            if ((maxHeight < configuredHeight))
                                applicationWidgetIconFillBackgroundHeight = true;
                            if ((maxWidth < configuredHeight))
                                applicationWidgetIconFillBackgroundWidth = true;
                        }
                        if (applicationWidgetIconHideProfileName) {
                            if (applicationWidgetIconFillBackground ||
                                    (applicationWidgetIconFillBackgroundHeight &&
                                            applicationWidgetIconFillBackgroundWidth))
                                remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_icon_highest_fill_no_profile_name_dn);
                            else
                            if (applicationWidgetIconFillBackgroundHeight)
                                remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_icon_highest_fill_height_no_profile_name_dn);
                            else
                            if (applicationWidgetIconFillBackgroundWidth)
                                remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_icon_highest_fill_width_no_profile_name_dn);
                            else
                                remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_icon_highest_no_profile_name_dn);
                        } else {
                            // profile._endOfActivationType - 0 = duration, 1 = end time
                            if ((profile._endOfActivationType == 0) &&
                                    (profile._duration > 0) &&
                                    (applicationWidgetIconShowProfileDuration)) {
                                if (applicationWidgetIconFillBackground ||
                                        (applicationWidgetIconFillBackgroundHeight &&
                                                applicationWidgetIconFillBackgroundWidth))
                                    remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_icon_highest_fill_dn);
                                else
                                if (applicationWidgetIconFillBackgroundHeight)
                                    remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_icon_highest_fill_height_dn);
                                else
                                if (applicationWidgetIconFillBackgroundWidth)
                                    remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_icon_highest_fill_width_dn);
                                else
                                    remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_icon_highest_dn);
                            } else if ((profile._endOfActivationType == 1) &&
                                    (applicationWidgetIconShowProfileDuration)) {
                                if (applicationWidgetIconFillBackground ||
                                        (applicationWidgetIconFillBackgroundHeight &&
                                                applicationWidgetIconFillBackgroundWidth))
                                    remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_icon_highest_fill_dn);
                                else
                                if (applicationWidgetIconFillBackgroundHeight)
                                    remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_icon_highest_fill_height_dn);
                                else
                                if (applicationWidgetIconFillBackgroundWidth)
                                    remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_icon_highest_fill_width_dn);
                                else
                                    remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_icon_highest_dn);
                            } else {
                                if (applicationWidgetIconFillBackground ||
                                        (applicationWidgetIconFillBackgroundHeight &&
                                                applicationWidgetIconFillBackgroundWidth))
                                    remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_icon_highest_fill_one_line_text_dn);
                                else
                                if (applicationWidgetIconFillBackgroundHeight)
                                    remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_icon_highest_fill_height_one_line_text_dn);
                                else
                                if (applicationWidgetIconFillBackgroundWidth)
                                    remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_icon_highest_fill_width_one_line_text_dn);
                                else
                                    remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_icon_highest_one_line_text_dn);
                            }
                        }
                    } else {
                        configuredHeight = context.getResources().getDimension(R.dimen.icon_widget_height_more_compact);
                        //configuredHeight = GlobalGUIRoutines.getRawDimensionInDp(context.getResources(), R.dimen.icon_widget_height_highest);
//                        Log.e("IconWidgetProvider._onUpdate", "configuredHeight="+configuredHeight);
                        if ((!applicationWidgetIconBackground.equals(GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_0)) ||
                                applicationWidgetIconShowBorder) {
//                            Log.e("IconWidgetProvider._onUpdate", "**** fill ****");
                            if ((maxHeight < configuredHeight))
                                applicationWidgetIconFillBackgroundHeight = true;
                            if ((maxWidth < configuredHeight))
                                applicationWidgetIconFillBackgroundWidth = true;
                        }
                        if (applicationWidgetIconHideProfileName) {
                            if (applicationWidgetIconFillBackground ||
                                    (applicationWidgetIconFillBackgroundHeight &&
                                            applicationWidgetIconFillBackgroundWidth))
                                remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_icon_more_compact_fill_no_profile_name_dn);
                            else
                            if (applicationWidgetIconFillBackgroundHeight)
                                remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_icon_more_compact_fill_height_no_profile_name_dn);
                            else
                            if (applicationWidgetIconFillBackgroundWidth)
                                remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_icon_more_compact_fill_width_no_profile_name_dn);
                            else
                                remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_icon_more_compact_no_profile_name_dn);
                        } else {
                            // profile._endOfActivationType - 0 = duration, 1 = end time
                            if ((profile._endOfActivationType == 0) &&
                                    (profile._duration > 0) &&
                                    (applicationWidgetIconShowProfileDuration)) {
                                if (applicationWidgetIconFillBackground ||
                                        (applicationWidgetIconFillBackgroundHeight &&
                                                applicationWidgetIconFillBackgroundWidth))
                                    remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_icon_more_compact_fill_dn);
                                else
                                if (applicationWidgetIconFillBackgroundHeight)
                                    remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_icon_more_compact_fill_height_dn);
                                else
                                if (applicationWidgetIconFillBackgroundWidth)
                                    remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_icon_more_compact_fill_width_dn);
                                else
                                    remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_icon_more_compact_dn);
                            } else if ((profile._endOfActivationType == 1) &&
                                    (applicationWidgetIconShowProfileDuration)) {
                                if (applicationWidgetIconFillBackground ||
                                        (applicationWidgetIconFillBackgroundHeight &&
                                                applicationWidgetIconFillBackgroundWidth))
                                    remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_icon_more_compact_fill_dn);
                                else
                                if (applicationWidgetIconFillBackgroundHeight)
                                    remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_icon_more_compact_fill_height_dn);
                                else
                                if (applicationWidgetIconFillBackgroundWidth)
                                    remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_icon_more_compact_fill_width_dn);
                                else
                                    remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_icon_more_compact_dn);
                            } else {
                                if (applicationWidgetIconFillBackground ||
                                        (applicationWidgetIconFillBackgroundHeight &&
                                                applicationWidgetIconFillBackgroundWidth))
                                    remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_icon_more_compact_fill_one_line_text_dn);
                                else
                                if (applicationWidgetIconFillBackgroundHeight)
                                    remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_icon_more_compact_fill_height_one_line_text_dn);
                                else
                                if (applicationWidgetIconFillBackgroundWidth)
                                    remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_icon_more_compact_fill_width_one_line_text_dn);
                                else
                                    remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_icon_more_compact_one_line_text_dn);
                            }
                        }
                    }
                }

                int roundedBackground = 0;
                int roundedBorder = 0;
                if (PPApplicationStatic.isPixelLauncherDefault(context)) {
                    roundedBackground = R.drawable.rounded_widget_background_pixel_launcher;
                    roundedBorder = R.drawable.rounded_widget_border_pixel_launcher;
                }
                else
                if (PPApplicationStatic.isOneUILauncherDefault(context)) {
                    roundedBackground = R.drawable.rounded_widget_background_oneui_launcher;
                    roundedBorder = R.drawable.rounded_widget_border_oneui_launcher;
                } else if (PPApplicationStatic.isMIUILauncherDefault(context)) {
                    roundedBackground = R.drawable.rounded_widget_background_miui_launcher;
                    roundedBorder = R.drawable.rounded_widget_border_miui_launcher;
                } else {
                    switch (applicationWidgetIconRoundedCornersRadius) {
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
                    remoteViews.setImageViewResource(R.id.widget_icon_background, roundedBackground);
                else
                    remoteViews.setImageViewResource(R.id.widget_icon_background, R.drawable.ic_empty);
                if (roundedBorder != 0)
                    remoteViews.setImageViewResource(R.id.widget_icon_rounded_border, roundedBorder);
                else
                    remoteViews.setImageViewResource(R.id.widget_icon_rounded_border, R.drawable.ic_empty);

                //if (applicationWidgetIconRoundedCorners) {
                remoteViews.setViewVisibility(R.id.widget_icon_background, View.VISIBLE);
                //remoteViews.setViewVisibility(R.id.widget_icon_not_rounded_border, View.GONE);
                if (applicationWidgetIconShowBorder)
                    remoteViews.setViewVisibility(R.id.widget_icon_rounded_border, View.VISIBLE);
                else
                    remoteViews.setViewVisibility(R.id.widget_icon_rounded_border, View.GONE);
                remoteViews.setInt(R.id.widget_icon_root, "setBackgroundColor", 0x00000000);

                if (!((Build.VERSION.SDK_INT >= 31) && applicationWidgetIconChangeColorsByNightMode &&
                        applicationWidgetIconColor.equals("0") && applicationWidgetIconUseDynamicColors))
                    remoteViews.setInt(R.id.widget_icon_background, "setColorFilter", Color.argb(0xFF, redBackground, greenBackground, blueBackground));

                remoteViews.setInt(R.id.widget_icon_background, "setImageAlpha", alphaBackground);

                if (applicationWidgetIconShowBorder) {
                    if (!((Build.VERSION.SDK_INT >= 31) && applicationWidgetIconChangeColorsByNightMode &&
                            applicationWidgetIconColor.equals("0") && applicationWidgetIconUseDynamicColors))
                        remoteViews.setInt(R.id.widget_icon_rounded_border, "setColorFilter", Color.argb(0xFF, redBorder, greenBorder, blueBorder));
                }
                /*} else {
                    remoteViews.setViewVisibility(R.id.widget_icon_background, View.GONE);
                    remoteViews.setViewVisibility(R.id.widget_icon_rounded_border, View.GONE);
                    if (applicationWidgetIconShowBorder)
                        remoteViews.setViewVisibility(R.id.widget_icon_not_rounded_border, View.VISIBLE);
                    else
                        remoteViews.setViewVisibility(R.id.widget_icon_not_rounded_border, View.GONE);
                    remoteViews.setInt(R.id.widget_icon_root, "setBackgroundColor", Color.argb(alphaBackground, redBackground, greenBackground, blueBackground));
                    if (applicationWidgetIconShowBorder)
                        remoteViews.setInt(R.id.widget_icon_not_rounded_border, "setColorFilter", Color.argb(0xFF, redBorder, greenBorder, blueBorder));
                }*/

                Bitmap bitmap = null;
                if (applicationWidgetIconColor.equals("0")) {
                    if (applicationWidgetIconChangeColorsByNightMode ||
                        ((!applicationWidgetIconBackgroundType) &&
                             (Integer.parseInt(applicationWidgetIconLightnessB) <= 25)) ||
                         (applicationWidgetIconBackgroundType &&
                             (ColorUtils.calculateLuminance(Integer.parseInt(applicationWidgetIconBackgroundColor)) < 0.23)))
                        bitmap = profile.increaseProfileIconBrightnessForContext(context, profile._iconBitmap);
                }
                if (isIconResourceID) {
                    if (bitmap != null)
                        remoteViews.setImageViewBitmap(R.id.icon_widget_icon, bitmap);
                    else {
                        if (profile._iconBitmap != null)
                            remoteViews.setImageViewBitmap(R.id.icon_widget_icon, profile._iconBitmap);
                        else {
                            //int iconResource = context.getResources().getIdentifier(iconIdentifier, "drawable", context.PPApplication.PACKAGE_NAME);
                            int iconResource = ProfileStatic.getIconResource(iconIdentifier);
                            remoteViews.setImageViewResource(R.id.icon_widget_icon, iconResource);
                        }
                    }
                } else {
                    if (bitmap != null)
                        remoteViews.setImageViewBitmap(R.id.icon_widget_icon, bitmap);
                    else {
                        remoteViews.setImageViewBitmap(R.id.icon_widget_icon, profile._iconBitmap);
                    }
                }

                if (!((Build.VERSION.SDK_INT >= 31) && applicationWidgetIconChangeColorsByNightMode &&
                        applicationWidgetIconColor.equals("0") && applicationWidgetIconUseDynamicColors))
                    remoteViews.setTextColor(R.id.icon_widget_name, Color.argb(0xFF, redText, greenText, blueText));
                else {
                    // must be removed android:textColor in layout
                    int color = GlobalGUIRoutines.getDynamicColor(R.attr.colorOnBackground, context);
                    if (color != 0) {
                        remoteViews.setTextColor(R.id.icon_widget_name, color);
                    }
                }

                if (!applicationWidgetIconHideProfileName)
                    remoteViews.setTextViewText(R.id.icon_widget_name, profileName);

                Intent intent = GlobalGUIRoutines.getIntentForStartupSource(context, PPApplication.STARTUP_SOURCE_WIDGET);
                // clear all opened activities
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra(PPApplication.EXTRA_STARTUP_SOURCE, PPApplication.STARTUP_SOURCE_WIDGET);
                PendingIntent pendingIntent = PendingIntent.getActivity(context, 100, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                //remoteViews.setOnClickPendingIntent(R.id.icon_widget_icon, pendingIntent);
                //remoteViews.setOnClickPendingIntent(R.id.icon_widget_name, pendingIntent);
                remoteViews.setOnClickPendingIntent(R.id.icon_widget_relLa1, pendingIntent);

                // widget update
                try {
                    appWidgetManager.updateAppWidget(widgetId, remoteViews);
                    //ComponentName thisWidget = new ComponentName(context, IconWidgetProvider.class);
                    //appWidgetManager.updateAppWidget(thisWidget, remoteViews);
                } catch (Exception e) {
                    PPApplicationStatic.recordException(e);
                }
            }
        //} catch (Exception e) {
            //Log.e("IconWidgetProvider._onUpdate", Log.getStackTraceString(e));
        //}

        dataWrapper.invalidateDataWrapper();
    }

    @Override
    public void onReceive(Context context, final Intent intent) {
        final Context appContext = context;
        LocaleHelper.setApplicationLocale(appContext);

        super.onReceive(appContext, intent); // calls onUpdate, is required for widget
//        PPApplicationStatic.logE("[IN_BROADCAST] IconWidgetProvider.onReceive", "xxx");

        final String action = intent.getAction();

        if ((action != null) &&
                (action.equalsIgnoreCase(ACTION_REFRESH_ICONWIDGET))) {
            AppWidgetManager manager = AppWidgetManager.getInstance(appContext);
            if (manager != null) {
                final int[] ids = manager.getAppWidgetIds(new ComponentName(appContext, IconWidgetProvider.class));
                if ((ids != null) && (ids.length > 0)) {
                    final AppWidgetManager appWidgetManager = manager;
                    //PPApplication.startHandlerThreadWidget();
                    //final Handler __handler = new Handler(PPApplication.handlerThreadWidget.getLooper());
                    //__handler.post(new PPHandlerThreadRunnable(context, manager) {
                    //__handler.post(() -> {
                    Runnable runnable = () -> {
//                            PPApplicationStatic.logE("[IN_EXECUTOR] PPApplication.startHandlerThreadWidget", "START run - from=IconWidgetProvider.onReceive");

                        //Context appContext= appContextWeakRef.get();
                        //AppWidgetManager appWidgetManager = appWidgetManagerWeakRef.get();

                        //if ((appContext != null) && (appWidgetManager != null)) {
                            _onUpdate(appContext, appWidgetManager, ids);
//                        This not working. This uses one row profie list provider. Why???
//                        Intent updateIntent = new Intent();
//                        updateIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
//                        updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
//                        context.sendBroadcast(updateIntent);
                        //}
                    }; //);
                    PPApplicationStatic.createDelayedGuiExecutor();
                    PPApplication.delayedGuiExecutor.submit(runnable);
                }
            }
        }
    }

    @Override
    public void onAppWidgetOptionsChanged (Context context,
                                           AppWidgetManager appWidgetManager,
                                           int appWidgetId,
                                           Bundle newOptions) {
        Intent intent3 = new Intent(ACTION_REFRESH_ICONWIDGET);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent3);
    }

    static void updateWidgets(Context context/*, boolean refresh*/) {
        /*String applicationWidgetIconLightness;
        String applicationWidgetIconColor;
        boolean applicationWidgetIconCustomIconLightness;
        synchronized (PPApplication.applicationPreferencesMutex) {
            applicationWidgetIconLightness = ApplicationPreferences.applicationWidgetIconLightness;
            applicationWidgetIconColor = ApplicationPreferences.applicationWidgetIconColor;
            applicationWidgetIconCustomIconLightness = ApplicationPreferences.applicationWidgetIconCustomIconLightness;
        }
        int monochromeValue = 0xFF;
        switch (applicationWidgetIconLightness) {
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
                applicationWidgetIconColor.equals("1"),
                monochromeValue,
                applicationWidgetIconCustomIconLightness);
        Profile profile = dataWrapper.getActivatedProfile(true, false);
        */

        /*DataWrapper dataWrapper = new DataWrapper(context.getApplicationContext(), false, 0, false);
        Profile profile = dataWrapper.getActivatedProfile(false, false);

        String pName;

        if (profile != null)
            pName = DataWrapper.getProfileNameWithManualIndicatorAsString(profile, false, "", true, false, false, dataWrapper);
        else
            pName = context.getString(R.string.profiles_header_profile_name_no_activated);

        if (!refresh) {
            String pNameWidget = PPApplication.prefWidgetProfileName1;

            if (!pNameWidget.isEmpty()) {
                if (pName.equals(pNameWidget)) {
                    return;
                }
            }
        }

        PPApplication.setWidgetProfileName(context.getApplicationContext(), 1, pName);*/

//        PPApplicationStatic.logE("[LOCAL_BROADCAST_CALL] IconWidgetProvider.updateWidgets", "xxx");
        Intent intent3 = new Intent(ACTION_REFRESH_ICONWIDGET);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent3);

        //Intent intent = new Intent(context, IconWidgetProvider.class);
        //intent.setAction(ACTION_REFRESH_ICONWIDGET);
        //context.sendBroadcast(intent);

        /*AppWidgetManager manager = AppWidgetManager.getInstance(context.getApplicationContext());
        if (manager != null) {
            int[] ids = manager.getAppWidgetIds(new ComponentName(context, IconWidgetProvider.class));
            if ((ids != null) && (ids.length > 0))
                _onUpdate(context, manager, profile, dataWrapper, ids);
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
