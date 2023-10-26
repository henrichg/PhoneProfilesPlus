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
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.View;
import android.widget.RemoteViews;

import androidx.core.graphics.ColorUtils;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class OneRowWidgetProvider extends AppWidgetProvider {

    static final String ACTION_REFRESH_ONEROWWIDGET = PPApplication.PACKAGE_NAME + ".ACTION_REFRESH_ONEROWWIDGET";

    public void onUpdate(Context context, AppWidgetManager appWidgetManager, final int[] appWidgetIds)
    {
//        PPApplicationStatic.logE("[IN_LISTENER] OneRowWidgetProvider.onUpdate", "xxx");
        //super.onUpdate(context, appWidgetManager, appWidgetIds);
        if (appWidgetIds.length > 0) {
            final Context appContext = context;
            LocaleHelper.setApplicationLocale(appContext);
            //PPApplication.startHandlerThreadWidget();
            //final Handler __handler = new Handler(PPApplication.handlerThreadWidget.getLooper());
            //__handler.post(new PPHandlerThreadRunnable(context, appWidgetManager) {
            //__handler.post(() -> {
            Runnable runnable = () -> {
//                    PPApplicationStatic.logE("[IN_EXECUTOR] PPApplication.startHandlerThreadWidget", "START run - from=OneRowWidgetProvider.onUpdate");

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
                           /*Profile _profile, DataWrapper _dataWrapper,*/ int[] appWidgetIds)
    {
        String applicationWidgetOneRowIconLightness;
        String applicationWidgetOneRowIconColor;
        boolean applicationWidgetOneRowCustomIconLightness;
        boolean applicationWidgetOneRowPrefIndicator;
        String applicationWidgetOneRowPrefIndicatorLightness;
        boolean applicationWidgetOneRowBackgroundType;
        String applicationWidgetOneRowBackgroundColor;
        String applicationWidgetOneRowLightnessB;
        String applicationWidgetOneRowBackground;
        boolean applicationWidgetOneRowShowBorder;
        String applicationWidgetOneRowLightnessBorder;
        boolean applicationWidgetOneRowRoundedCorners;
        String applicationWidgetOneRowLightnessT;
        int applicationWidgetOneRowRoundedCornersRadius;
        String applicationWidgetOneRowLayoutHeight;
        //boolean applicationWidgetOneRowHigherLayout;
        boolean applicationWidgetOneRowChangeColorsByNightMode;
        boolean applicationWidgetOneRowUseDynamicColors;
        String applicationWidgetOneRowBackgroundColorNightModeOff;
        String applicationWidgetOneRowBackgroundColorNightModeOn;
        boolean applicationWidgetOneRowFillBackground;

        synchronized (PPApplication.applicationPreferencesMutex) {

            applicationWidgetOneRowIconLightness = ApplicationPreferences.applicationWidgetOneRowIconLightness;
            applicationWidgetOneRowIconColor = ApplicationPreferences.applicationWidgetOneRowIconColor;
            applicationWidgetOneRowCustomIconLightness = ApplicationPreferences.applicationWidgetOneRowCustomIconLightness;
            applicationWidgetOneRowPrefIndicator = ApplicationPreferences.applicationWidgetOneRowPrefIndicator;
            applicationWidgetOneRowPrefIndicatorLightness = ApplicationPreferences.applicationWidgetOneRowPrefIndicatorLightness;
            applicationWidgetOneRowBackgroundType = ApplicationPreferences.applicationWidgetOneRowBackgroundType;
            applicationWidgetOneRowBackgroundColor = ApplicationPreferences.applicationWidgetOneRowBackgroundColor;
            applicationWidgetOneRowLightnessB = ApplicationPreferences.applicationWidgetOneRowLightnessB;
            applicationWidgetOneRowBackground = ApplicationPreferences.applicationWidgetOneRowBackground;
            applicationWidgetOneRowShowBorder = ApplicationPreferences.applicationWidgetOneRowShowBorder;
            applicationWidgetOneRowLightnessBorder = ApplicationPreferences.applicationWidgetOneRowLightnessBorder;
            applicationWidgetOneRowLightnessT = ApplicationPreferences.applicationWidgetOneRowLightnessT;
            applicationWidgetOneRowRoundedCorners = ApplicationPreferences.applicationWidgetOneRowRoundedCorners;
            applicationWidgetOneRowRoundedCornersRadius = ApplicationPreferences.applicationWidgetOneRowRoundedCornersRadius;
            applicationWidgetOneRowFillBackground = ApplicationPreferences.applicationWidgetOneRowFillBackground;

            // "Rounded corners" parameter is removed, is forced to true
            if (!applicationWidgetOneRowRoundedCorners) {
                //applicationWidgetOneRowRoundedCorners = true;
                applicationWidgetOneRowRoundedCornersRadius = 1;
            }
            applicationWidgetOneRowLayoutHeight = ApplicationPreferences.applicationWidgetOneRowLayoutHeight;
            //applicationWidgetOneRowHigherLayout = ApplicationPreferences.applicationWidgetOneRowHigherLayout;

            if (Build.VERSION.SDK_INT < 30)
                applicationWidgetOneRowChangeColorsByNightMode = false;
            else
                applicationWidgetOneRowChangeColorsByNightMode = ApplicationPreferences.applicationWidgetOneRowChangeColorsByNightMode;

            applicationWidgetOneRowUseDynamicColors = ApplicationPreferences.applicationWidgetOneRowUseDynamicColors;
            applicationWidgetOneRowBackgroundColorNightModeOff = ApplicationPreferences.applicationWidgetOneRowBackgroundColorNightModeOff;
            applicationWidgetOneRowBackgroundColorNightModeOn = ApplicationPreferences.applicationWidgetOneRowBackgroundColorNightModeOn;

            if (Build.VERSION.SDK_INT >= 30) {
                if (PPApplicationStatic.isPixelLauncherDefault(context) ||
                        PPApplicationStatic.isOneUILauncherDefault(context) ||
                        PPApplicationStatic.isMIUILauncherDefault(context)) {
                    ApplicationPreferences.applicationWidgetOneRowRoundedCorners = true;
                    ApplicationPreferences.applicationWidgetOneRowRoundedCornersRadius = 15;
                    //ApplicationPreferences.applicationWidgetChangeColorsByNightMode = true;
                    SharedPreferences.Editor editor = ApplicationPreferences.getEditor(context);
                    editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_ROUNDED_CORNERS,
                            ApplicationPreferences.applicationWidgetOneRowRoundedCorners);
                    editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_ROUNDED_CORNERS_RADIUS,
                            String.valueOf(ApplicationPreferences.applicationWidgetOneRowRoundedCornersRadius));
                    //editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_CHANGE_COLOR_BY_NIGHT_MODE,
                    //        ApplicationPreferences.applicationWidgetChangeColorsByNightMode);
                    editor.apply();
                    //applicationWidgetOneRowRoundedCorners = ApplicationPreferences.applicationWidgetOneRowRoundedCorners;
                    applicationWidgetOneRowRoundedCornersRadius = ApplicationPreferences.applicationWidgetOneRowRoundedCornersRadius;
                    //applicationWidgetChangeColorsByNightMode = ApplicationPreferences.applicationWidgetChangeColorsByNightMode;
                }
                if (Build.VERSION.SDK_INT < 31)
                    applicationWidgetOneRowUseDynamicColors = false;
                if (//PPApplication.isPixelLauncherDefault(context) ||
                        (applicationWidgetOneRowChangeColorsByNightMode &&
                         (!applicationWidgetOneRowUseDynamicColors))) {
                    boolean nightModeOn = GlobalGUIRoutines.isNightModeEnabled(context.getApplicationContext());
                    //int nightModeFlags =
                    //        context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
                    //switch (nightModeFlags) {
                    //noinspection IfStatementWithIdenticalBranches
                    if (nightModeOn) {
                        //case Configuration.UI_MODE_NIGHT_YES:

                        //applicationWidgetOneRowBackground = GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_100; // fully opaque
                        applicationWidgetOneRowBackgroundType = true; // background type = color
                        applicationWidgetOneRowBackgroundColor = String.valueOf(ColorChooserPreference.parseValue(applicationWidgetOneRowBackgroundColorNightModeOn)); // color of background
                        //applicationWidgetOneRowShowBorder = false; // do not show border
                        applicationWidgetOneRowLightnessBorder = GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_100;
                        applicationWidgetOneRowLightnessT = GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_87; // lightness of text = white
                        //applicationWidgetOneRowIconColor = "0"; // icon type = colorful
                        applicationWidgetOneRowIconLightness = GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_75;
                        //applicationWidgetOneRowPrefIndicatorLightness = GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_62; // lightness of preference indicators
                        //break;
                    } else {
                        //case Configuration.UI_MODE_NIGHT_NO:
                        //case Configuration.UI_MODE_NIGHT_UNDEFINED:

                        //applicationWidgetOneRowBackground = GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_100; // fully opaque
                        applicationWidgetOneRowBackgroundType = true; // background type = color
                        applicationWidgetOneRowBackgroundColor = String.valueOf(ColorChooserPreference.parseValue(applicationWidgetOneRowBackgroundColorNightModeOff)); // color of background
                        //applicationWidgetOneRowShowBorder = false; // do not show border
                        applicationWidgetOneRowLightnessBorder = "0";
                        applicationWidgetOneRowLightnessT = GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_12; // lightness of text = black
                        //applicationWidgetOneRowIconColor = "0"; // icon type = colorful
                        applicationWidgetOneRowIconLightness = GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_62;
                        //applicationWidgetOneRowPrefIndicatorLightness = GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_50; // lightness of preference indicators
                        //break;
                    }
                }
            }
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
                //noinspection ConstantConditions
                monochromeValue = 0xFF;
                break;
        }

        float prefIndicatorLightnessValue = 0f;
        int prefIndicatorMonochromeValue = 0x00;
        switch (applicationWidgetOneRowPrefIndicatorLightness) {
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
        if (applicationWidgetOneRowChangeColorsByNightMode &&
            applicationWidgetOneRowIconColor.equals("0")) {
            if ((Build.VERSION.SDK_INT >= 31) && applicationWidgetOneRowUseDynamicColors)
                indicatorType = DataWrapper.IT_FOR_WIDGET_DYNAMIC_COLORS;
            else
                indicatorType = DataWrapper.IT_FOR_WIDGET_NATIVE_BACKGROUND;
        }
        else
        if (applicationWidgetOneRowBackgroundType) {
            if (ColorUtils.calculateLuminance(Integer.parseInt(applicationWidgetOneRowBackgroundColor)) < 0.23)
                indicatorType = DataWrapper.IT_FOR_WIDGET_DARK_BACKGROUND;
            else
                indicatorType = DataWrapper.IT_FOR_WIDGET_LIGHT_BACKGROUND;
        } else {
            if (Integer.parseInt(applicationWidgetOneRowLightnessB) <= 37)
                indicatorType = DataWrapper.IT_FOR_WIDGET_DARK_BACKGROUND;
            else
                indicatorType = DataWrapper.IT_FOR_WIDGET_LIGHT_BACKGROUND;
        }

        DataWrapper dataWrapper = new DataWrapper(context.getApplicationContext(),
                    applicationWidgetOneRowIconColor.equals("1"), monochromeValue,
                    applicationWidgetOneRowCustomIconLightness,
                    indicatorType, prefIndicatorMonochromeValue, prefIndicatorLightnessValue);

        Profile profile;
        //boolean fullyStarted = PPApplication.applicationFullyStarted;
        //if ((!fullyStarted) /*|| applicationPackageReplaced*/)
        //    profile = null;
        //else
            profile = dataWrapper.getActivatedProfile(true, applicationWidgetOneRowPrefIndicator);

        //try {
            // set background
            int redBackground = 0x00;
            int greenBackground;
            int blueBackground;
            if (applicationWidgetOneRowBackgroundType) {
                int bgColor = Integer.parseInt(applicationWidgetOneRowBackgroundColor);
                redBackground = Color.red(bgColor);
                greenBackground = Color.green(bgColor);
                blueBackground = Color.blue(bgColor);
            } else {
                switch (applicationWidgetOneRowLightnessB) {
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
            switch (applicationWidgetOneRowBackground) {
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
            if (applicationWidgetOneRowShowBorder) {
                switch (applicationWidgetOneRowLightnessBorder) {
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
            switch (applicationWidgetOneRowLightnessT) {
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
                profile._icon = StringConstants.PROFILE_ICON_DEFAULT + "|1|0|0";

                profile.generateIconBitmap(context.getApplicationContext(),
                        applicationWidgetOneRowIconColor.equals("1"),
                        monochromeValue,
                        applicationWidgetOneRowCustomIconLightness);
                isIconResourceID = profile.getIsIconResourceID();
                iconIdentifier = profile.getIconIdentifier();
                profileName = new SpannableString(profile._name);
            }

            // get all OneRowWidgetProvider widgets in launcher
            //ComponentName thisWidget = new ComponentName(context, OneRowWidgetProvider.class);
            //int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);

            for (int widgetId : appWidgetIds) {

                Bundle options = appWidgetManager.getAppWidgetOptions(widgetId);
                int maxHeight = GlobalGUIRoutines.dpToPx(options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT));
//                Log.e("OneRowWidgetProvider._onUpdate", "maxHeight="+maxHeight);
                //int maxHeight = GlobalGUIRoutines.dpToPx(options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT));
                //Log.e("OneRowWidgetProvider._onUpdate", "maxHeight="+maxHeight);

                //bundle.putInt(PPApplication.BUNDLE_WIDGET_TYPE, PPApplication.WIDGET_TYPE_ONE_ROW);
                //appWidgetManager.updateAppWidgetOptions(widgetId, bundle);

//                AppWidgetProviderInfo info = appWidgetManager.getAppWidgetInfo(widgetId);

                RemoteViews remoteViews;

                float configuredHeight;
                if (!((Build.VERSION.SDK_INT >= 31) && applicationWidgetOneRowChangeColorsByNightMode &&
                        applicationWidgetOneRowIconColor.equals("0") && applicationWidgetOneRowUseDynamicColors)) {
                    if (applicationWidgetOneRowLayoutHeight.equals("0")) {
                        configuredHeight = context.getResources().getDimension(R.dimen.one_row_widget_height);
//                        Log.e("OneRowWidgetProvider._onUpdate", "configuredHeight="+configuredHeight);
                        if (maxHeight < configuredHeight)
                            applicationWidgetOneRowFillBackground = true;
                        if (applicationWidgetOneRowPrefIndicator) {
                            if (applicationWidgetOneRowFillBackground)
                                remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_one_row_fill);
                            else
                                remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_one_row);
                        }
                        else {
                            if (applicationWidgetOneRowFillBackground)
                                remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_one_row_fill_no_indicator);
                            else
                                remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_one_row_no_indicator);
                        }
                    } else if (applicationWidgetOneRowLayoutHeight.equals("1")) {
                        configuredHeight = context.getResources().getDimension(R.dimen.one_row_widget_height_higher);
//                        Log.e("OneRowWidgetProvider._onUpdate", "configuredHeight="+configuredHeight);
                        if (maxHeight < configuredHeight)
                            applicationWidgetOneRowFillBackground = true;
                        if (applicationWidgetOneRowPrefIndicator) {
                            if (applicationWidgetOneRowFillBackground)
                                remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_one_row_higher_fill);
                            else
                                remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_one_row_higher);
                        }
                        else {
                            if (applicationWidgetOneRowFillBackground)
                                remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_one_row_higher_fill_no_indicator);
                            else
                                remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_one_row_higher_no_indicator);
                        }
                    } else {
                        configuredHeight = context.getResources().getDimension(R.dimen.one_row_widget_height_highest);
//                        Log.e("OneRowWidgetProvider._onUpdate", "configuredHeight="+configuredHeight);
                        if (maxHeight < configuredHeight)
                            applicationWidgetOneRowFillBackground = true;
                        if (applicationWidgetOneRowPrefIndicator) {
                            if (applicationWidgetOneRowFillBackground)
                                remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_one_row_highest_fill);
                            else
                                remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_one_row_highest);
                        }
                        else {
                            if (applicationWidgetOneRowFillBackground)
                                remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_one_row_highest_fill_no_indicator);
                            else
                                remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_one_row_highest_no_indicator);
                        }
                    }
                } else {
                    if (applicationWidgetOneRowLayoutHeight.equals("0")) {
                        configuredHeight = context.getResources().getDimension(R.dimen.one_row_widget_height);
//                        Log.e("OneRowWidgetProvider._onUpdate", "configuredHeight="+configuredHeight);
                        if (maxHeight < configuredHeight)
                            applicationWidgetOneRowFillBackground = true;
                        if (applicationWidgetOneRowPrefIndicator) {
                            if (applicationWidgetOneRowFillBackground)
                                remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_one_row_fill_dn);
                            else
                                remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_one_row_dn);
                        }
                        else {
                            if (applicationWidgetOneRowFillBackground)
                                remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_one_row_fill_no_indicator_dn);
                            else
                                remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_one_row_no_indicator_dn);
                        }
                    } else if (applicationWidgetOneRowLayoutHeight.equals("1")) {
                        configuredHeight = context.getResources().getDimension(R.dimen.one_row_widget_height_higher);
//                        Log.e("OneRowWidgetProvider._onUpdate", "configuredHeight="+configuredHeight);
                        if (maxHeight < configuredHeight)
                            applicationWidgetOneRowFillBackground = true;
                        if (applicationWidgetOneRowPrefIndicator) {
                            if (applicationWidgetOneRowFillBackground)
                                remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_one_row_higher_fill_dn);
                            else
                                remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_one_row_higher_dn);
                        }
                        else {
                            if (applicationWidgetOneRowFillBackground)
                                remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_one_row_higher_fill_no_indicator_dn);
                            else
                                remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_one_row_higher_no_indicator_dn);
                        }
                    } else {
                        configuredHeight = context.getResources().getDimension(R.dimen.one_row_widget_height_highest);
//                        Log.e("OneRowWidgetProvider._onUpdate", "configuredHeight="+configuredHeight);
                        if (maxHeight < configuredHeight)
                            applicationWidgetOneRowFillBackground = true;
                        if (applicationWidgetOneRowPrefIndicator) {
                            if (applicationWidgetOneRowFillBackground)
                                remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_one_row_highest_fill_dn);
                            else
                                remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_one_row_highest_dn);
                        }
                        else {
                            if (applicationWidgetOneRowFillBackground)
                                remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_one_row_highest_fill_no_indicator_dn);
                            else
                                remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_one_row_highest_no_indicator_dn);
                        }
                    }
                }

                int roundedBackground = 0;
                int roundedBorder = 0;
                if (PPApplicationStatic.isPixelLauncherDefault(context)) {
                    roundedBackground = R.drawable.rounded_widget_background_pixel_launcher;
                    roundedBorder = R.drawable.rounded_widget_border_pixel_launcher;
                } else if (PPApplicationStatic.isOneUILauncherDefault(context)) {
                    roundedBackground = R.drawable.rounded_widget_background_oneui_launcher;
                    roundedBorder = R.drawable.rounded_widget_border_oneui_launcher;
                } else if (PPApplicationStatic.isMIUILauncherDefault(context)) {
                    roundedBackground = R.drawable.rounded_widget_background_miui_launcher;
                    roundedBorder = R.drawable.rounded_widget_border_miui_launcher;
                } else {
                    switch (applicationWidgetOneRowRoundedCornersRadius) {
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
                //remoteViews.setViewVisibility(R.id.widget_one_row_not_rounded_border, View.GONE);
                if (applicationWidgetOneRowShowBorder) {
                    remoteViews.setViewVisibility(R.id.widget_one_row_rounded_border, VISIBLE);
                }
                else {
                    remoteViews.setViewVisibility(R.id.widget_one_row_rounded_border, View.GONE);
                }
                remoteViews.setInt(R.id.widget_one_row_root, "setBackgroundColor", 0x00000000);

                if (!((Build.VERSION.SDK_INT >= 31) && applicationWidgetOneRowChangeColorsByNightMode &&
                        applicationWidgetOneRowIconColor.equals("0") && applicationWidgetOneRowUseDynamicColors)) {
                    //remoteViews.setInt(R.id.widget_one_row_background, "setColorFilter", Color.argb(0xFF, 0, 0, 0));
                    remoteViews.setInt(R.id.widget_one_row_background, "setColorFilter", Color.argb(0xFF, redBackground, greenBackground, blueBackground));
                }

                remoteViews.setInt(R.id.widget_one_row_background, "setImageAlpha", alphaBackground);

                if (applicationWidgetOneRowShowBorder) {
                    if (!((Build.VERSION.SDK_INT >= 31) && applicationWidgetOneRowChangeColorsByNightMode &&
                            applicationWidgetOneRowIconColor.equals("0") && applicationWidgetOneRowUseDynamicColors))
                        remoteViews.setInt(R.id.widget_one_row_rounded_border, "setColorFilter", Color.argb(0xFF, redBorder, greenBorder, blueBorder));
                }

                Bitmap bitmap = null;
                if (applicationWidgetOneRowIconColor.equals("0")) {
                    if (applicationWidgetOneRowChangeColorsByNightMode ||
                       ((!applicationWidgetOneRowBackgroundType) &&
                           (Integer.parseInt(applicationWidgetOneRowLightnessB) <= 25)) ||
                       (applicationWidgetOneRowBackgroundType &&
                           (ColorUtils.calculateLuminance(Integer.parseInt(applicationWidgetOneRowBackgroundColor)) < 0.23)))
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

                if (!((Build.VERSION.SDK_INT >= 31) && applicationWidgetOneRowChangeColorsByNightMode &&
                        applicationWidgetOneRowIconColor.equals("0") && applicationWidgetOneRowUseDynamicColors)) {
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
                if (applicationWidgetOneRowPrefIndicator) {
                    if (profile._preferencesIndicator == null)
                        //remoteViews.setImageViewResource(R.id.widget_one_row_header_profile_pref_indicator, R.drawable.ic_empty);
                        remoteViews.setViewVisibility(R.id.widget_one_row_header_profile_pref_indicator, GONE);
                    else {
                        remoteViews.setImageViewBitmap(R.id.widget_one_row_header_profile_pref_indicator, profile._preferencesIndicator);
                        remoteViews.setViewVisibility(R.id.widget_one_row_header_profile_pref_indicator, VISIBLE);
                    }
                }

                if (!((Build.VERSION.SDK_INT >= 31) && applicationWidgetOneRowChangeColorsByNightMode &&
                        applicationWidgetOneRowIconColor.equals("0") && applicationWidgetOneRowUseDynamicColors)) {
                    //if (Event.getGlobalEventsRunning() && PPApplicationStatic.getApplicationStarted(true)) {
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

                //if (Event.getGlobalEventsRunning() && PPApplicationStatic.getApplicationStarted(true)) {
                //remoteViews.setViewVisibility(R.id.widget_one_row_header_restart_events, VISIBLE);
                Intent intentRE = new Intent(context, RestartEventsFromGUIActivity.class);
                PendingIntent pIntentRE = PendingIntent.getActivity(context, 2, intentRE, PendingIntent.FLAG_UPDATE_CURRENT);
                remoteViews.setOnClickPendingIntent(R.id.widget_one_row_header_restart_events_click, pIntentRE);
                //} else
                //    remoteViews.setViewVisibility(R.id.widget_one_row_header_restart_events_click, View.GONE);

                Intent intent = GlobalGUIRoutines.getIntentForStartupSource(context, PPApplication.STARTUP_SOURCE_WIDGET);
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
                    PPApplicationStatic.recordException(e);
                }
            }
        //} catch (Exception ee) {
        //    PPApplicationStatic.recordException(ee);
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
//        PPApplicationStatic.logE("[IN_BROADCAST] OneRowWidgetProvider.onReceive", "action="+action);

        if ((action != null) &&
                (action.equalsIgnoreCase(ACTION_REFRESH_ONEROWWIDGET))) {
            AppWidgetManager manager = AppWidgetManager.getInstance(appContext);
            if (manager != null) {
                final int[] ids = manager.getAppWidgetIds(new ComponentName(appContext, OneRowWidgetProvider.class));
                if ((ids != null) && (ids.length > 0)) {
                    final AppWidgetManager appWidgetManager = manager;
                    //PPApplication.startHandlerThreadWidget();
                    //final Handler __handler = new Handler(PPApplication.handlerThreadWidget.getLooper());
                    //__handler.post(new PPHandlerThreadRunnable(context, manager) {
                    //__handler.post(() -> {
                    Runnable runnable = () -> {
//                            PPApplicationStatic.logE("[IN_EXECUTOR] PPApplication.startHandlerThreadWidget", "START run - from=OneRowWidgetProvider.onReceive");

                        //Context appContext= appContextWeakRef.get();
                        //AppWidgetManager appWidgetManager = appWidgetManagerWeakRef.get();

                        //if ((appContext != null) && (appWidgetManager != null)) {
                            _onUpdate(appContext, appWidgetManager, ids);
                        //}
                    }; //);
                    PPApplicationStatic.createDelayedGuiExecutor();
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
*/
    @Override
    public void onAppWidgetOptionsChanged (Context context,
                                           AppWidgetManager appWidgetManager,
                                           int appWidgetId,
                                           Bundle newOptions) {
//        PPApplicationStatic.logE("[LOCAL_BROADCAST_CALL] OneRowWidgetProvider.onAppWidgetOptionsChanged", "xxx");
        Intent intent3 = new Intent(ACTION_REFRESH_ONEROWWIDGET);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent3);
    }

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

//        PPApplicationStatic.logE("[LOCAL_BROADCAST_CALL] OneRowWidgetProvider.updateWidgets", "xxx");
        Intent intent3 = new Intent(ACTION_REFRESH_ONEROWWIDGET);
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
