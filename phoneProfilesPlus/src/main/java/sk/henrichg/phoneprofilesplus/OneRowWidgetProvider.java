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
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import androidx.core.graphics.ColorUtils;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.lang.ref.WeakReference;
import java.util.concurrent.TimeUnit;

/** @noinspection ExtractMethodRecommender*/
public class OneRowWidgetProvider extends AppWidgetProvider {

    static final String ACTION_REFRESH_ONEROWWIDGET = PPApplication.PACKAGE_NAME + ".ACTION_REFRESH_ONEROWWIDGET";

    public void onUpdate(Context context, AppWidgetManager _appWidgetManager, final int[] appWidgetIds)
    {
//        PPApplicationStatic.logE("[IN_LISTENER] OneRowWidgetProvider.onUpdate", "xxx");
//        PPApplicationStatic.logE("[UPDATE_GUI] OneRowWidgetProvider.onUpdate", "xxxxxxxxxxx");

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
            //PPApplication.delayedGuiExecutor.submit(runnable);
            for (int appWidgetId : appWidgetIds) {
                boolean found = false;
                SheduledFutureWidgetData sheduledFutureWidgetData = null;
                for (SheduledFutureWidgetData futureWidgetData : PPApplication.scheduledFutureOneRowWidgetExecutor) {
                    if (futureWidgetData.appWidgetId == appWidgetId) {
                        sheduledFutureWidgetData = futureWidgetData;
                        found = true;
                        break;
                    }
                }
                if (found)
                    sheduledFutureWidgetData.scheduledFutures.cancel(true);
                else {
                    sheduledFutureWidgetData = new SheduledFutureWidgetData(appWidgetId, null);
                    PPApplication.scheduledFutureOneRowWidgetExecutor.add(sheduledFutureWidgetData);
                }
                sheduledFutureWidgetData.scheduledFutures =
                        PPApplication.delayedGuiExecutor.schedule(runnable, 5, TimeUnit.SECONDS);
            }
        }
    }

    /** @noinspection DataFlowIssue*/
    private static void _onUpdate(Context context, AppWidgetManager appWidgetManager,
                           /*Profile _profile, DataWrapper _dataWrapper,*/ int[] appWidgetIds)
    {
        String applicationWidgetOneRowLauncher;
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
        boolean applicationWidgetOneRowPrefIndicatorUseDynamicColor;
        boolean applicationWidgetOneRowLightnessTChangeByNightMode;
        boolean applicationWidgetOneRowLightnessBorderChangeByNightMode;
        boolean applicationWidgetOneRowIconLightnessChangeByNightMode;
        boolean applicationWidgetOneRowPrefIndicatorLightnessChangeByNightMode;

        int setRestartEventsLightness = 0;

//        PPApplicationStatic.logE("[SYNCHRONIZED] OneRowWidgetProvider._onUpdate", "PPApplication.applicationPreferencesMutex");
        synchronized (PPApplication.applicationPreferencesMutex) {

            applicationWidgetOneRowLauncher = ApplicationPreferences.applicationWidgetOneRowLauncher;
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
            applicationWidgetOneRowLightnessTChangeByNightMode = ApplicationPreferences.applicationWidgetOneRowLightnessTChangeByNightMode;
            applicationWidgetOneRowRoundedCorners = ApplicationPreferences.applicationWidgetOneRowRoundedCorners;
            applicationWidgetOneRowRoundedCornersRadius = ApplicationPreferences.applicationWidgetOneRowRoundedCornersRadius;
            applicationWidgetOneRowFillBackground = ApplicationPreferences.applicationWidgetOneRowFillBackground;
            applicationWidgetOneRowLightnessBorderChangeByNightMode = ApplicationPreferences.applicationWidgetOneRowLightnessBorderChangeByNightMode;
            applicationWidgetOneRowIconLightnessChangeByNightMode = ApplicationPreferences.applicationWidgetOneRowIconLightnessChangeByNightMode;
            applicationWidgetOneRowPrefIndicatorLightnessChangeByNightMode = ApplicationPreferences.applicationWidgetOneRowPrefIndicatorLightnessChangeByNightMode;

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
            applicationWidgetOneRowPrefIndicatorUseDynamicColor = ApplicationPreferences.applicationWidgetOneRowPrefIndicatorUseDynamicColor;
            //Log.e("OneRowWidgetProvider._onUpdate", "applicationWidgetOneRowPrefIndicatorUseDynamicColor="+applicationWidgetOneRowPrefIndicatorUseDynamicColor);

            if (Build.VERSION.SDK_INT >= 30) {
                if (Build.VERSION.SDK_INT >= 31) {
                    if (PPApplicationStatic.isPixelLauncherDefault(context) ||
                            PPApplicationStatic.isOneUILauncherDefault(context) ||
                            PPApplicationStatic.isMIUILauncherDefault(context)/* ||
                            PPApplicationStatic.isSmartLauncherDefault(context)*/) {
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
                }
                if (Build.VERSION.SDK_INT < 31) {
                    applicationWidgetOneRowUseDynamicColors = false;
                    applicationWidgetOneRowPrefIndicatorUseDynamicColor = false;
                }

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

                        //applicationWidgetOneRowLightnessT = GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_87; // lightness of text = white
                        if (applicationWidgetOneRowLightnessTChangeByNightMode) {
                            switch (applicationWidgetOneRowLightnessT) {
                                case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_0:
                                    applicationWidgetOneRowLightnessT = GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_100;
                                    break;
                                case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_12:
                                    applicationWidgetOneRowLightnessT = GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_87;
                                    break;
                                case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_25:
                                    applicationWidgetOneRowLightnessT = GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_75;
                                    break;
                                case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_37:
                                    applicationWidgetOneRowLightnessT = GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_62;
                                    break;
                            }
                            /*
                            SharedPreferences.Editor editor = ApplicationPreferences.getEditor(context.getApplicationContext());
                            editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_LIGHTNESS_T, applicationWidgetOneRowLightnessT);
                            editor.apply();
                            ApplicationPreferences.applicationWidgetOneRowLightnessT = applicationWidgetOneRowLightnessT;
                            */
                        } //else
                            //applicationWidgetOneRowLightnessT = GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_87; // lightness of text = white
                        //applicationWidgetOneRowLightnessBorder = GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_100;
                        if (applicationWidgetOneRowLightnessBorderChangeByNightMode) {
                            switch (applicationWidgetOneRowLightnessBorder) {
                                case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_0:
                                    applicationWidgetOneRowLightnessBorder = GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_100;
                                    break;
                                case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_12:
                                    applicationWidgetOneRowLightnessBorder = GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_87;
                                    break;
                                case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_25:
                                    applicationWidgetOneRowLightnessBorder = GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_75;
                                    break;
                                case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_37:
                                    applicationWidgetOneRowLightnessBorder = GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_62;
                                    break;
                            }
                            /*
                            SharedPreferences.Editor editor = ApplicationPreferences.getEditor(context.getApplicationContext());
                            editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_LIGHTNESS_BORDER, applicationWidgetOneRowLightnessBorder);
                            editor.apply();
                            ApplicationPreferences.applicationWidgetOneRowLightnessBorder = applicationWidgetOneRowLightnessBorder;
                            */
                        } //else
                            //applicationWidgetOneRowLightnessBorder = GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_100;
                        setRestartEventsLightness = -1;
                        //applicationWidgetOneRowIconColor = "0"; // icon type = colorful
                        //applicationWidgetOneRowIconLightness = GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_75;
                        //applicationWidgetOneRowPrefIndicatorLightness = GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_62; // lightness of preference indicators
                        //break;
                    } else {
                        //case Configuration.UI_MODE_NIGHT_NO:
                        //case Configuration.UI_MODE_NIGHT_UNDEFINED:

                        //applicationWidgetOneRowBackground = GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_100; // fully opaque
                        applicationWidgetOneRowBackgroundType = true; // background type = color
                        applicationWidgetOneRowBackgroundColor = String.valueOf(ColorChooserPreference.parseValue(applicationWidgetOneRowBackgroundColorNightModeOff)); // color of background
                        //applicationWidgetOneRowShowBorder = false; // do not show border

                        //applicationWidgetOneRowLightnessT = GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_12; // lightness of text = black
                        if (applicationWidgetOneRowLightnessTChangeByNightMode) {
                            switch (applicationWidgetOneRowLightnessT) {
                                case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_62:
                                    applicationWidgetOneRowLightnessT = GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_37;
                                    break;
                                case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_75:
                                    applicationWidgetOneRowLightnessT = GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_25;
                                    break;
                                case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_87:
                                    applicationWidgetOneRowLightnessT = GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_12;
                                    break;
                                case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_100:
                                    applicationWidgetOneRowLightnessT = GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_0;
                                    break;
                            }
                            /*
                            SharedPreferences.Editor editor = ApplicationPreferences.getEditor(context.getApplicationContext());
                            editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_LIGHTNESS_T, applicationWidgetOneRowLightnessT);
                            editor.apply();
                            ApplicationPreferences.applicationWidgetOneRowLightnessT = applicationWidgetOneRowLightnessT;
                            */
                        } //else
                            //applicationWidgetOneRowLightnessT = GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_12; // lightness of text = black
                        //applicationWidgetOneRowLightnessBorder = "0";
                        if (applicationWidgetOneRowLightnessBorderChangeByNightMode) {
                            switch (applicationWidgetOneRowLightnessBorder) {
                                case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_62:
                                    applicationWidgetOneRowLightnessBorder = GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_37;
                                    break;
                                case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_75:
                                    applicationWidgetOneRowLightnessBorder = GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_25;
                                    break;
                                case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_87:
                                    applicationWidgetOneRowLightnessBorder = GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_12;
                                    break;
                                case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_100:
                                    applicationWidgetOneRowLightnessBorder = GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_0;
                                    break;
                            }
                            /*
                            SharedPreferences.Editor editor = ApplicationPreferences.getEditor(context.getApplicationContext());
                            editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_LIGHTNESS_BORDER, applicationWidgetOneRowLightnessBorder);
                            editor.apply();
                            ApplicationPreferences.applicationWidgetOneRowLightnessBorder = applicationWidgetOneRowLightnessBorder;
                            */
                        } //else
                            //applicationWidgetOneRowLightnessBorder = GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_0;;

                        setRestartEventsLightness = 1;
                        //applicationWidgetOneRowIconColor = "0"; // icon type = colorful
                        //applicationWidgetOneRowIconLightness = GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_62;
                        //applicationWidgetOneRowPrefIndicatorLightness = GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_50; // lightness of preference indicators
                        //break;
                    }
                }
                if (applicationWidgetOneRowChangeColorsByNightMode) {
                    boolean nightModeOn = GlobalGUIRoutines.isNightModeEnabled(context.getApplicationContext());
                    if (nightModeOn) {
                        //applicationWidgetPanelIconLightness = GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_75;
                        if (applicationWidgetOneRowIconLightnessChangeByNightMode) {
                            switch (applicationWidgetOneRowIconLightness) {
                                case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_0:
                                    applicationWidgetOneRowIconLightness = GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_100;
                                    break;
                                case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_12:
                                    applicationWidgetOneRowIconLightness = GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_87;
                                    break;
                                case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_25:
                                    applicationWidgetOneRowIconLightness = GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_75;
                                    break;
                                case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_37:
                                    applicationWidgetOneRowIconLightness = GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_62;
                                    break;
                            }
                            /*
                            SharedPreferences.Editor editor = ApplicationPreferences.getEditor(context.getApplicationContext());
                            editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_ICON_LIGHTNESS, applicationWidgetOneRowIconLightness);
                            editor.apply();
                            ApplicationPreferences.applicationWidgetOneRowIconLightness = applicationWidgetOneRowIconLightness;
                            */
                        } //else
                            //applicationWidgetOneRowIconLightness = GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_75;
                    } else {
                        //applicationWidgetIconLightness = GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_62;
                        if (applicationWidgetOneRowIconLightnessChangeByNightMode) {
                            switch (applicationWidgetOneRowIconLightness) {
                                case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_62:
                                    applicationWidgetOneRowIconLightness = GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_37;
                                    break;
                                case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_75:
                                    applicationWidgetOneRowIconLightness = GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_25;
                                    break;
                                case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_87:
                                    applicationWidgetOneRowIconLightness = GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_12;
                                    break;
                                case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_100:
                                    applicationWidgetOneRowIconLightness = GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_0;
                                    break;
                            }
                            /*
                            SharedPreferences.Editor editor = ApplicationPreferences.getEditor(context.getApplicationContext());
                            editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_ICON_LIGHTNESS, applicationWidgetOneRowIconLightness);
                            editor.apply();
                            ApplicationPreferences.applicationWidgetOneRowIconLightness = applicationWidgetOneRowIconLightness;
                            */
                        } //else
                            //applicationWidgetOneRowIconLightness = GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_62;
                    }
                }
                if (applicationWidgetOneRowChangeColorsByNightMode &&
                        (!applicationWidgetOneRowUseDynamicColors) &&
                        (!applicationWidgetOneRowPrefIndicatorUseDynamicColor)) {
                    boolean nightModeOn = GlobalGUIRoutines.isNightModeEnabled(context.getApplicationContext());
                    if (nightModeOn) {
                        if (applicationWidgetOneRowPrefIndicatorLightnessChangeByNightMode) {
                            switch (applicationWidgetOneRowPrefIndicatorLightness) {
                                case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_0:
                                    applicationWidgetOneRowPrefIndicatorLightness = GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_100;
                                    break;
                                case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_12:
                                    applicationWidgetOneRowPrefIndicatorLightness = GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_87;
                                    break;
                                case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_25:
                                    applicationWidgetOneRowPrefIndicatorLightness = GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_75;
                                    break;
                                case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_37:
                                    applicationWidgetOneRowPrefIndicatorLightness = GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_62;
                                    break;
                            }
                            /*
                            SharedPreferences.Editor editor = ApplicationPreferences.getEditor(context.getApplicationContext());
                            editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_PREF_INDICATOR_LIGHTNESS, applicationWidgetOneRowPrefIndicatorLightness);
                            editor.apply();
                            ApplicationPreferences.applicationWidgetOneRowPrefIndicatorLightness = applicationWidgetOneRowPrefIndicatorLightness;
                            */
                        } //else
                            //applicationWidgetOneRowPrefIndicatorLightness = GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_50;
                    } else {
                        if (applicationWidgetOneRowPrefIndicatorLightnessChangeByNightMode) {
                            switch (applicationWidgetOneRowPrefIndicatorLightness) {
                                case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_62:
                                    applicationWidgetOneRowPrefIndicatorLightness = GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_37;
                                    break;
                                case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_75:
                                    applicationWidgetOneRowPrefIndicatorLightness = GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_25;
                                    break;
                                case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_87:
                                    applicationWidgetOneRowPrefIndicatorLightness = GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_12;
                                    break;
                                case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_100:
                                    applicationWidgetOneRowPrefIndicatorLightness = GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_0;
                                    break;
                            }
                            /*
                            SharedPreferences.Editor editor = ApplicationPreferences.getEditor(context.getApplicationContext());
                            editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_PREF_INDICATOR_LIGHTNESS, applicationWidgetOneRowPrefIndicatorLightness);
                            editor.apply();
                            ApplicationPreferences.applicationWidgetOneRowPrefIndicatorLightness = applicationWidgetOneRowPrefIndicatorLightness;
                            */
                        } //else
                            //applicationWidgetOneRowPrefIndicatorLightness = GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_50;
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
            if ((Build.VERSION.SDK_INT >= 31) &&
                    (applicationWidgetOneRowUseDynamicColors ||
                            applicationWidgetOneRowPrefIndicatorUseDynamicColor))
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
            if (setRestartEventsLightness == -1) {
                // nigthNodeOn = true
                // GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_87
                restartEventsLightness = 0xE0 - 0x1F;
                //if (restartEventsLightness < 0x00)
                //    restartEventsLightness = 0x00;
            }
            else
            if (setRestartEventsLightness == 1) {
                // nigthNodeOn = false
                // GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_12
                restartEventsLightness = 0x20 + 0x1F;
                //if (restartEventsLightness > 0xFF)
                //    restartEventsLightness = 0xFF;
            }

            boolean isIconResourceID;
            String iconIdentifier;
            Spannable profileName;
            if (profile != null) {
                isIconResourceID = profile.getIsIconResourceID();
                iconIdentifier = profile.getIconIdentifier();
                profileName = DataWrapperStatic.getProfileNameWithManualIndicator(profile, true, "", true, false, false, false, dataWrapper);
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
                            if (applicationWidgetOneRowFillBackground) {
                                if (PPApplicationStatic.isSmartLauncherDefault(context)) {
                                    int width = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH);
                                    int height = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT);
                                    if (width < height)
                                        remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_one_row_fill_no_indicator_sl_land);
                                    else
                                        remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_one_row_fill);
                                }
                                else
                                    remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_one_row_fill);
                            }
                            else {
                                if (PPApplicationStatic.isSmartLauncherDefault(context)) {
                                    int width = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH);
                                    int height = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT);
                                    if (width < height)
                                        remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_one_row_no_indicator_sl_land);
                                    else
                                        remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_one_row);
                                }
                                else
                                    remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_one_row);
                            }
                        }
                        else {
                            if (applicationWidgetOneRowFillBackground) {
                                if (PPApplicationStatic.isSmartLauncherDefault(context)) {
                                    int width = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH);
                                    int height = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT);
                                    if (width < height)
                                        remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_one_row_fill_no_indicator_sl_land);
                                    else
                                        remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_one_row_fill_no_indicator);
                                }
                                else
                                    remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_one_row_fill_no_indicator);
                            }
                            else {
                                if (PPApplicationStatic.isSmartLauncherDefault(context)) {
                                    int width = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH);
                                    int height = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT);
                                    if (width < height)
                                        remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_one_row_no_indicator_sl_land);
                                    else
                                        remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_one_row_no_indicator);
                                }
                                else
                                    remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_one_row_no_indicator);
                            }
                        }
                    } else if (applicationWidgetOneRowLayoutHeight.equals("1")) {
                        configuredHeight = context.getResources().getDimension(R.dimen.one_row_widget_height_higher);
//                        Log.e("OneRowWidgetProvider._onUpdate", "configuredHeight="+configuredHeight);
                        if (maxHeight < configuredHeight)
                            applicationWidgetOneRowFillBackground = true;
                        if (applicationWidgetOneRowPrefIndicator) {
                            if (applicationWidgetOneRowFillBackground) {
                                if (PPApplicationStatic.isSmartLauncherDefault(context)) {
                                    int width = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH);
                                    int height = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT);
                                    if (width < height)
                                        remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_one_row_higher_fill_no_indicator_sl_land);
                                    else
                                        remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_one_row_higher_fill);
                                }
                                else
                                    remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_one_row_higher_fill);
                            }
                            else {
                                if (PPApplicationStatic.isSmartLauncherDefault(context)) {
                                    int width = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH);
                                    int height = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT);
                                    if (width < height)
                                        remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_one_row_higher_no_indicator_sl_land);
                                    else
                                        remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_one_row_higher);
                                }
                                else
                                    remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_one_row_higher);
                            }
                        }
                        else {
                            if (applicationWidgetOneRowFillBackground) {
                                if (PPApplicationStatic.isSmartLauncherDefault(context)) {
                                    int width = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH);
                                    int height = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT);
                                    if (width < height)
                                        remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_one_row_higher_fill_no_indicator_sl_land);
                                    else
                                        remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_one_row_higher_fill_no_indicator);
                                }
                                else
                                    remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_one_row_higher_fill_no_indicator);
                            }
                            else {
                                if (PPApplicationStatic.isSmartLauncherDefault(context)) {
                                    int width = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH);
                                    int height = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT);
                                    if (width < height)
                                        remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_one_row_higher_no_indicator_sl_land);
                                    else
                                        remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_one_row_higher_no_indicator);
                                }
                                else
                                    remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_one_row_higher_no_indicator);
                            }
                        }
                    } else {
                        configuredHeight = context.getResources().getDimension(R.dimen.one_row_widget_height_highest);
//                        Log.e("OneRowWidgetProvider._onUpdate", "configuredHeight="+configuredHeight);
                        if (maxHeight < configuredHeight)
                            applicationWidgetOneRowFillBackground = true;
                        if (applicationWidgetOneRowPrefIndicator) {
                            if (applicationWidgetOneRowFillBackground) {
                                if (PPApplicationStatic.isSmartLauncherDefault(context)) {
                                    int width = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH);
                                    int height = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT);
                                    if (width < height)
                                        remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_one_row_highest_fill_no_indicator_sl_land);
                                    else
                                        remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_one_row_highest_fill);
                                }
                                else
                                    remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_one_row_highest_fill);
                            }
                            else {
                                if (PPApplicationStatic.isSmartLauncherDefault(context)) {
                                    int width = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH);
                                    int height = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT);
                                    if (width < height)
                                        remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_one_row_highest_no_indicator_sl_land);
                                    else
                                        remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_one_row_highest);
                                }
                                else
                                    remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_one_row_highest);
                            }
                        }
                        else {
                            if (applicationWidgetOneRowFillBackground) {
                                if (PPApplicationStatic.isSmartLauncherDefault(context)) {
                                    int width = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH);
                                    int height = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT);
                                    if (width < height)
                                        remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_one_row_highest_fill_no_indicator_sl_land);
                                    else
                                        remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_one_row_highest_fill_no_indicator);
                                }
                                else
                                    remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_one_row_highest_fill_no_indicator);
                            }
                            else {
                                if (PPApplicationStatic.isSmartLauncherDefault(context)) {
                                    int width = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH);
                                    int height = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT);
                                    if (width < height)
                                        remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_one_row_highest_no_indicator_sl_land);
                                    else
                                        remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_one_row_highest_no_indicator);
                                }
                                else
                                    remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_one_row_highest_no_indicator);
                            }
                        }
                    }
                } else {
                    if (applicationWidgetOneRowLayoutHeight.equals("0")) {
                        configuredHeight = context.getResources().getDimension(R.dimen.one_row_widget_height);
//                        Log.e("OneRowWidgetProvider._onUpdate", "configuredHeight="+configuredHeight);
                        if (maxHeight < configuredHeight)
                            applicationWidgetOneRowFillBackground = true;
                        if (applicationWidgetOneRowPrefIndicator) {
                            if (applicationWidgetOneRowFillBackground) {
                                if (PPApplicationStatic.isSmartLauncherDefault(context)) {
                                    int width = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH);
                                    int height = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT);
                                    if (width < height)
                                        remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_one_row_fill_no_indicator_dn_sl_land);
                                    else
                                        remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_one_row_fill_dn);
                                }
                                else
                                    remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_one_row_fill_dn);
                            }
                            else {
                                if (PPApplicationStatic.isSmartLauncherDefault(context)) {
                                    int width = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH);
                                    int height = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT);
                                    if (width < height)
                                        remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_one_row_no_indicator_dn_sl_land);
                                    else
                                        remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_one_row_dn);
                                }
                                else
                                    remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_one_row_dn);
                            }
                        }
                        else {
                            if (applicationWidgetOneRowFillBackground) {
                                if (PPApplicationStatic.isSmartLauncherDefault(context)) {
                                    int width = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH);
                                    int height = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT);
                                    if (width < height)
                                        remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_one_row_fill_no_indicator_dn_sl_land);
                                    else
                                        remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_one_row_fill_no_indicator_dn);
                                }
                                else
                                    remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_one_row_fill_no_indicator_dn);
                            }
                            else {
                                if (PPApplicationStatic.isSmartLauncherDefault(context)) {
                                    int width = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH);
                                    int height = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT);
                                    if (width < height)
                                        remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_one_row_no_indicator_dn_sl_land);
                                    else
                                        remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_one_row_no_indicator_dn);
                                }
                                else
                                    remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_one_row_no_indicator_dn);
                            }
                        }
                    } else if (applicationWidgetOneRowLayoutHeight.equals("1")) {
                        configuredHeight = context.getResources().getDimension(R.dimen.one_row_widget_height_higher);
//                        Log.e("OneRowWidgetProvider._onUpdate", "configuredHeight="+configuredHeight);
                        if (maxHeight < configuredHeight)
                            applicationWidgetOneRowFillBackground = true;
                        if (applicationWidgetOneRowPrefIndicator) {
                            if (applicationWidgetOneRowFillBackground) {
                                if (PPApplicationStatic.isSmartLauncherDefault(context)) {
                                    int width = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH);
                                    int height = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT);
                                    if (width < height)
                                        remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_one_row_higher_fill_no_indicator_dn_sl_land);
                                    else
                                        remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_one_row_higher_fill_dn);
                                }
                                else
                                    remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_one_row_higher_fill_dn);
                            }
                            else {
                                if (PPApplicationStatic.isSmartLauncherDefault(context)) {
                                    int width = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH);
                                    int height = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT);
                                    if (width < height)
                                        remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_one_row_higher_no_indicator_dn_sl_land);
                                    else
                                        remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_one_row_higher_dn);
                                }
                                else
                                    remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_one_row_higher_dn);
                            }
                        }
                        else {
                            if (applicationWidgetOneRowFillBackground) {
                                if (PPApplicationStatic.isSmartLauncherDefault(context)) {
                                    int width = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH);
                                    int height = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT);
                                    if (width < height)
                                        remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_one_row_higher_fill_no_indicator_dn_sl_land);
                                    else
                                        remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_one_row_higher_fill_no_indicator_dn);
                                }
                                else
                                    remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_one_row_higher_fill_no_indicator_dn);
                            }
                            else {
                                if (PPApplicationStatic.isSmartLauncherDefault(context)) {
                                    int width = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH);
                                    int height = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT);
                                    if (width < height)
                                        remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_one_row_higher_no_indicator_dn_sl_land);
                                    else
                                        remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_one_row_higher_no_indicator_dn);
                                }
                                else
                                    remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_one_row_higher_no_indicator_dn);
                            }
                        }
                    } else {
                        configuredHeight = context.getResources().getDimension(R.dimen.one_row_widget_height_highest);
//                        Log.e("OneRowWidgetProvider._onUpdate", "configuredHeight="+configuredHeight);
                        if (maxHeight < configuredHeight)
                            applicationWidgetOneRowFillBackground = true;
                        if (applicationWidgetOneRowPrefIndicator) {
                            if (applicationWidgetOneRowFillBackground) {
                                if (PPApplicationStatic.isSmartLauncherDefault(context)) {
                                    int width = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH);
                                    int height = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT);
                                    if (width < height)
                                        remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_one_row_highest_fill_no_indicator_dn_sl_land);
                                    else
                                        remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_one_row_highest_fill_dn);
                                }
                                else
                                    remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_one_row_highest_fill_dn);
                            }
                            else {
                                if (PPApplicationStatic.isSmartLauncherDefault(context)) {
                                    int width = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH);
                                    int height = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT);
                                    if (width < height)
                                        remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_one_row_highest_no_indicator_dn_sl_land);
                                    else
                                        remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_one_row_highest_dn);
                                }
                                else
                                    remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_one_row_highest_dn);
                            }
                        }
                        else {
                            if (applicationWidgetOneRowFillBackground) {
                                if (PPApplicationStatic.isSmartLauncherDefault(context)) {
                                    int width = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH);
                                    int height = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT);
                                    if (width < height)
                                        remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_one_row_highest_fill_no_indicator_dn_sl_land);
                                    else
                                        remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_one_row_highest_fill_no_indicator_dn);
                                }
                                else
                                    remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_one_row_highest_fill_no_indicator_dn);
                            }
                            else {
                                if (PPApplicationStatic.isSmartLauncherDefault(context)) {
                                    int width = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH);
                                    int height = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT);
                                    if (width < height)
                                        remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_one_row_highest_no_indicator_dn_sl_land);
                                    else
                                        remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_one_row_highest_no_indicator_dn);
                                }
                                else
                                    remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_one_row_highest_no_indicator_dn);
                            }
                        }
                    }
                }

                int roundedBackground;
                int roundedBorder;
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
                } /*else
                if ((Build.VERSION.SDK_INT >= 31) && PPApplicationStatic.isSmartLauncherDefault(context)) {
                    roundedBackground = R.drawable.rounded_widget_background_smart_launcher;
                    roundedBorder = R.drawable.rounded_widget_border_smart_launcher;
                }*/ else {
                    roundedBackground = getRoundedBackgroundDrawable(applicationWidgetOneRowRoundedCornersRadius);
                    roundedBorder = getRoundedBorderDrawable(applicationWidgetOneRowRoundedCornersRadius);
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
                    //if (!((Build.VERSION.SDK_INT >= 31) && applicationWidgetOneRowChangeColorsByNightMode &&
                    //        applicationWidgetOneRowIconColor.equals("0") && applicationWidgetOneRowUseDynamicColors))
                    if ((Build.VERSION.SDK_INT >= 31) && (applicationWidgetOneRowUseDynamicColors)) {
                        int dynamicColor = GlobalGUIRoutines.getDynamicColor(R.attr.colorSecondary, context);
                        if (dynamicColor != 0) {
                            dynamicColor = GlobalGUIRoutines.changeLigtnessOfColor(dynamicColor, redBorder);
                            remoteViews.setInt(R.id.widget_one_row_rounded_border, "setColorFilter", dynamicColor);
                        }
                        else
                            remoteViews.setInt(R.id.widget_one_row_rounded_border, "setColorFilter", Color.argb(0xFF, redBorder, greenBorder, blueBorder));
                    } else
                        remoteViews.setInt(R.id.widget_one_row_rounded_border, "setColorFilter", Color.argb(0xFF, redBorder, greenBorder, blueBorder));
                }

                Bitmap bitmap = null;
                if (applicationWidgetOneRowIconColor.equals("0")) {
                    if (isIconResourceID) {
                        if (applicationWidgetOneRowChangeColorsByNightMode ||
                                ((!applicationWidgetOneRowBackgroundType) &&
                                        (Integer.parseInt(applicationWidgetOneRowLightnessB) <= 25)) ||
                                (applicationWidgetOneRowBackgroundType &&
                                        (ColorUtils.calculateLuminance(Integer.parseInt(applicationWidgetOneRowBackgroundColor)) < 0.23)))
                            bitmap = profile.increaseProfileIconBrightnessForContext(context, profile._iconBitmap);
                    } else
                        bitmap = profile._iconBitmap;
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
                        applicationWidgetOneRowIconColor.equals("0") &&
                        (applicationWidgetOneRowUseDynamicColors ||
                         applicationWidgetOneRowPrefIndicatorUseDynamicColor))) {
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

                //Intent intent = GlobalGUIRoutines.getIntentForStartupSource(context, PPApplication.STARTUP_SOURCE_WIDGET);
                Intent intent;
                if (applicationWidgetOneRowLauncher.equals(StringConstants.EXTRA_ACTIVATOR))
                    intent = new Intent(context.getApplicationContext(), ActivatorActivity.class);
                else
                    intent = new Intent(context.getApplicationContext(), EditorActivity.class);
                // clear all opened activities
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra(PPApplication.EXTRA_STARTUP_SOURCE, PPApplication.STARTUP_SOURCE_WIDGET);
                PendingIntent pendingIntent = PendingIntent.getActivity(context, 200, intent, PendingIntent.FLAG_UPDATE_CURRENT);
//                remoteViews.setOnClickPendingIntent(R.id.widget_one_row_header_profile_root, pendingIntent);
                remoteViews.setOnClickPendingIntent(R.id.widget_one_row_header, pendingIntent);

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

    private static int getRoundedBackgroundDrawable(int applicationWidgetListRoundedCornersRadius) {
        switch (applicationWidgetListRoundedCornersRadius) {
            case 1:
                return R.drawable.rounded_widget_background_1;
            case 2:
                return  R.drawable.rounded_widget_background_2;
            case 3:
                return  R.drawable.rounded_widget_background_3;
            case 4:
                return  R.drawable.rounded_widget_background_4;
            case 5:
                //noinspection DuplicateBranchesInSwitch
                return  R.drawable.rounded_widget_background_5;
            case 6:
                return  R.drawable.rounded_widget_background_6;
            case 7:
                return  R.drawable.rounded_widget_background_7;
            case 8:
                return  R.drawable.rounded_widget_background_8;
            case 9:
                return  R.drawable.rounded_widget_background_9;
            case 10:
                return  R.drawable.rounded_widget_background_10;
            case 11:
                return  R.drawable.rounded_widget_background_11;
            case 12:
                return  R.drawable.rounded_widget_background_12;
            case 13:
                return  R.drawable.rounded_widget_background_13;
            case 14:
                return  R.drawable.rounded_widget_background_14;
            case 15:
                return  R.drawable.rounded_widget_background_15;
            case 16:
                return  R.drawable.rounded_widget_background_16;
            case 17:
                return  R.drawable.rounded_widget_background_17;
            case 18:
                return  R.drawable.rounded_widget_background_18;
            case 19:
                return  R.drawable.rounded_widget_background_19;
            case 20:
                return  R.drawable.rounded_widget_background_20;
            case 21:
                return  R.drawable.rounded_widget_background_21;
            case 22:
                return  R.drawable.rounded_widget_background_22;
            case 23:
                return  R.drawable.rounded_widget_background_23;
            case 24:
                return  R.drawable.rounded_widget_background_24;
            case 25:
                return  R.drawable.rounded_widget_background_25;
            case 26:
                return  R.drawable.rounded_widget_background_26;
            case 27:
                return  R.drawable.rounded_widget_background_27;
            case 28:
                return  R.drawable.rounded_widget_background_28;
            case 29:
                return  R.drawable.rounded_widget_background_29;
            case 30:
                return  R.drawable.rounded_widget_background_30;
            case 31:
                return  R.drawable.rounded_widget_background_31;
            case 32:
                return  R.drawable.rounded_widget_background_32;
            default:
                return  R.drawable.rounded_widget_background_5;
        }
    }
    private static int getRoundedBorderDrawable(int applicationWidgetListRoundedCornersRadius) {
        switch (applicationWidgetListRoundedCornersRadius) {
            case 1:
                return  R.drawable.rounded_widget_border_1;
            case 2:
                return  R.drawable.rounded_widget_border_2;
            case 3:
                return  R.drawable.rounded_widget_border_3;
            case 4:
                return  R.drawable.rounded_widget_border_4;
            case 5:
                //noinspection DuplicateBranchesInSwitch
                return  R.drawable.rounded_widget_border_5;
            case 6:
                return  R.drawable.rounded_widget_border_6;
            case 7:
                return  R.drawable.rounded_widget_border_7;
            case 8:
                return  R.drawable.rounded_widget_border_8;
            case 9:
                return  R.drawable.rounded_widget_border_9;
            case 10:
                return  R.drawable.rounded_widget_border_10;
            case 11:
                return  R.drawable.rounded_widget_border_11;
            case 12:
                return  R.drawable.rounded_widget_border_12;
            case 13:
                return  R.drawable.rounded_widget_border_13;
            case 14:
                return  R.drawable.rounded_widget_border_14;
            case 15:
                return  R.drawable.rounded_widget_border_15;
            case 16:
                return  R.drawable.rounded_widget_border_16;
            case 17:
                return  R.drawable.rounded_widget_border_17;
            case 18:
                return  R.drawable.rounded_widget_border_18;
            case 19:
                return  R.drawable.rounded_widget_border_19;
            case 20:
                return  R.drawable.rounded_widget_border_20;
            case 21:
                return  R.drawable.rounded_widget_border_21;
            case 22:
                return  R.drawable.rounded_widget_border_22;
            case 23:
                return  R.drawable.rounded_widget_border_23;
            case 24:
                return  R.drawable.rounded_widget_border_24;
            case 25:
                return  R.drawable.rounded_widget_border_25;
            case 26:
                return  R.drawable.rounded_widget_border_26;
            case 27:
                return  R.drawable.rounded_widget_border_27;
            case 28:
                return  R.drawable.rounded_widget_border_28;
            case 29:
                return  R.drawable.rounded_widget_border_29;
            case 30:
                return  R.drawable.rounded_widget_border_30;
            case 31:
                return  R.drawable.rounded_widget_border_31;
            case 32:
                return  R.drawable.rounded_widget_border_32;
            default:
                return  R.drawable.rounded_widget_border_5;
        }
    }

    @Override
    public void onReceive(Context context, final Intent intent) {
        final Context appContext = context.getApplicationContext();
        LocaleHelper.setApplicationLocale(appContext);

        super.onReceive(appContext, intent); // calls onUpdate, is required for widget

        String action = intent.getAction();
//        PPApplicationStatic.logE("[IN_BROADCAST] OneRowWidgetProvider.onReceive", "action="+action);
//        PPApplicationStatic.logE("[UPDATE_GUI] OneRowWidgetProvider.onReceive", "action="+action);

        if ((action != null) &&
                (action.equalsIgnoreCase(ACTION_REFRESH_ONEROWWIDGET))) {
            AppWidgetManager manager = AppWidgetManager.getInstance(appContext);
            if (manager != null) {
                final int[] appWidgetIds = manager.getAppWidgetIds(new ComponentName(appContext, OneRowWidgetProvider.class));
                if ((appWidgetIds != null) && (appWidgetIds.length > 0)) {
                    final WeakReference<AppWidgetManager> appWidgetManagerWeakRef = new WeakReference<>(manager);
                    Runnable runnable = () -> {
//                        PPApplicationStatic.logE("[IN_EXECUTOR] PPApplication.startHandlerThreadWidget", "START run - from=OneRowWidgetProvider.onReceive");

                        //Context appContext= appContextWeakRef.get();
                        AppWidgetManager appWidgetManager = appWidgetManagerWeakRef.get();

                        if (/*(appContext != null) &&*/ (appWidgetManager != null)) {
                            _onUpdate(appContext, appWidgetManager, appWidgetIds);
                        }
                    };
                    PPApplicationStatic.createDelayedGuiExecutor();
                    //PPApplication.delayedGuiExecutor.submit(runnable);
                    for (int appWidgetId : appWidgetIds) {
                        boolean found = false;
                        SheduledFutureWidgetData sheduledFutureWidgetData = null;
                        for (SheduledFutureWidgetData futureWidgetData : PPApplication.scheduledFutureOneRowWidgetExecutor) {
                            if (futureWidgetData.appWidgetId == appWidgetId) {
                                sheduledFutureWidgetData = futureWidgetData;
                                found = true;
                                break;
                            }
                        }
                        if (found)
                            sheduledFutureWidgetData.scheduledFutures.cancel(true);
                        else {
                            sheduledFutureWidgetData = new SheduledFutureWidgetData(appWidgetId, null);
                            PPApplication.scheduledFutureOneRowWidgetExecutor.add(sheduledFutureWidgetData);
                        }
                        sheduledFutureWidgetData.scheduledFutures =
                                PPApplication.delayedGuiExecutor.schedule(runnable, 5, TimeUnit.SECONDS);
                    }
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
//        PPApplicationStatic.logE("[UPDATE_GUI] OneRowWidgetProvider.onAppWidgetOptionsChanged", "xxxxxxxx");
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
