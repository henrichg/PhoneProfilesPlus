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
import android.os.PowerManager;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.View;
import android.widget.RemoteViews;

import androidx.core.graphics.ColorUtils;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.lang.ref.WeakReference;
import java.util.concurrent.TimeUnit;

/** @noinspection ExtractMethodRecommender*/
public class IconWidgetProvider extends AppWidgetProvider {

    static final String ACTION_REFRESH_ICONWIDGET = PPApplication.PACKAGE_NAME + ".ACTION_REFRESH_ICONWIDGET";
    private static final int PROFILE_ID_ACTIVATE_PROFILE_ID = 1000;

    public void onUpdate(Context context, AppWidgetManager _appWidgetManager, final int[] appWidgetIds)
    {
        //super.onUpdate(context, appWidgetManager, appWidgetIds);
//        PPApplicationStatic.logE("[IN_LISTENER] IconWidgetProvider.onUpdate", "xxx");
        if (appWidgetIds.length > 0) {
            final Context appContext = context.getApplicationContext();
            final WeakReference<AppWidgetManager> appWidgetManagerWeakRef = new WeakReference<>(_appWidgetManager);
            LocaleHelper.setApplicationLocale(appContext);
            Runnable runnable = () -> {
//                    PPApplicationStatic.logE("[IN_EXECUTOR] PPApplication.startHandlerThreadWidget", "START run - from=IconWidgetProvider.onUpdate");

                PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                PowerManager.WakeLock wakeLock = null;
                try {
                    if (powerManager != null) {
                        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WakelockTags.WAKELOCK_TAG_IconWidgetProvider_onUpdate);
                        wakeLock.acquire(10 * 60 * 1000);
                    }

                    AppWidgetManager appWidgetManager = appWidgetManagerWeakRef.get();

                    if (/*(appContext != null) &&*/ (appWidgetManager != null)) {
                        _onUpdate(appContext, appWidgetManager, appWidgetIds);
                    }

                } catch (Exception e) {
//                  PPApplicationStatic.logE("[IN_EXECUTOR] IconWidgetProvider.onUpdate", Log.getStackTraceString(e));
                    PPApplicationStatic.recordException(e);
                } finally {
                    if ((wakeLock != null) && wakeLock.isHeld()) {
                        try {
                            wakeLock.release();
                        } catch (Exception ignored) {
                        }
                    }
                    //worker.shutdown();
                }
            };
            PPApplicationStatic.createDelayedGuiExecutor();
//            PPApplication.delayedGuiExecutor.submit(runnable);
            if (PPApplication.scheduledFutureIconWidgetExecutor != null)
                PPApplication.scheduledFutureIconWidgetExecutor.cancel(true);
            PPApplication.scheduledFutureIconWidgetExecutor =
                    PPApplication.delayedGuiExecutor.schedule(runnable, 5, TimeUnit.SECONDS);
        }
    }

    /** @noinspection DataFlowIssue*/
    private static void _onUpdate(Context context, AppWidgetManager appWidgetManager,
                          /*Profile _profile, DataWrapper _dataWrapper,*/ int[] appWidgetIds) {

        String applicationWidgetIconLauncher;
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
        boolean applicationWidgetIconLightnessTChangeByNightMode;
        boolean applicationWidgetIconLightnessBorderChangeByNightMode;
        boolean applicationWidgetIconLightnessChangeByNightMode;

//        PPApplicationStatic.logE("[SYNCHRONIZED] IconWidgetProvider._onUpdate", "PPApplication.applicationPreferencesMutex");
        synchronized (PPApplication.applicationPreferencesMutex) {

            applicationWidgetIconLauncher = ApplicationPreferences.applicationWidgetIconLauncher;
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
            applicationWidgetIconLightnessTChangeByNightMode = ApplicationPreferences.applicationWidgetIconLightnessTChangeByNightMode;
            applicationWidgetIconShowProfileDuration = ApplicationPreferences.applicationWidgetIconShowProfileDuration;
            applicationWidgetIconRoundedCorners = ApplicationPreferences.applicationWidgetIconRoundedCorners;
            applicationWidgetIconRoundedCornersRadius = ApplicationPreferences.applicationWidgetIconRoundedCornersRadius;
            applicationWidgetIconLightnessBorderChangeByNightMode = ApplicationPreferences.applicationWidgetIconLightnessBorderChangeByNightMode;
            applicationWidgetIconLightnessChangeByNightMode = ApplicationPreferences.applicationWidgetIconLightnessChangeByNightMode;

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
                if (Build.VERSION.SDK_INT >= 31) {
                    if (PPApplicationStatic.isPixelLauncherDefault(context) ||
                            PPApplicationStatic.isOneUILauncherDefault(context) ||
                            PPApplicationStatic.isMIUILauncherDefault(context) /*||
                            PPApplicationStatic.isSmartLauncherDefault(context)*/) {
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

                        //applicationWidgetIconLightnessT = GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_87; // lightness of text = white
                        if (applicationWidgetIconLightnessTChangeByNightMode) {
                            switch (applicationWidgetIconLightnessT) {
                                case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_0:
                                    applicationWidgetIconLightnessT = GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_100;
                                    break;
                                case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_12:
                                    applicationWidgetIconLightnessT = GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_87;
                                    break;
                                case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_25:
                                    applicationWidgetIconLightnessT = GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_75;
                                    break;
                                case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_37:
                                    applicationWidgetIconLightnessT = GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_62;
                                    break;
                            }
                            /*
                            SharedPreferences.Editor editor = ApplicationPreferences.getEditor(context.getApplicationContext());
                            editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_LIGHTNESS_T, applicationWidgetIconLightnessT);
                            editor.apply();
                            ApplicationPreferences.applicationWidgetIconLightnessT = applicationWidgetIconLightnessT;
                            */
                        } //else
                            //applicationWidgetIconLightnessT = GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_87; // lightness of text = white
                        //applicationWidgetIconLightnessBorder = GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_100;
                        if (applicationWidgetIconLightnessBorderChangeByNightMode) {
                            switch (applicationWidgetIconLightnessBorder) {
                                case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_0:
                                    applicationWidgetIconLightnessBorder = GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_100;
                                    break;
                                case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_12:
                                    applicationWidgetIconLightnessBorder = GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_87;
                                    break;
                                case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_25:
                                    applicationWidgetIconLightnessBorder = GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_75;
                                    break;
                                case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_37:
                                    applicationWidgetIconLightnessBorder = GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_62;
                                    break;
                            }
                            /*
                            SharedPreferences.Editor editor = ApplicationPreferences.getEditor(context.getApplicationContext());
                            editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_LIGHTNESS_BORDER, applicationWidgetIconLightnessBorder);
                            editor.apply();
                            ApplicationPreferences.applicationWidgetIconLightnessBorder = applicationWidgetIconLightnessBorder;
                            */
                        }// else
                            //applicationWidgetIconLightnessBorder = GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_100;
                    } else {
                        //case Configuration.UI_MODE_NIGHT_NO:
                        //case Configuration.UI_MODE_NIGHT_UNDEFINED:

                        //applicationWidgetIconBackground = GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_100; // fully opaque
                        applicationWidgetIconBackgroundType = true; // background type = color
                        applicationWidgetIconBackgroundColor = String.valueOf(ColorChooserPreference.parseValue(applicationWidgetIconBackgroundColorNightModeOff)); // color of background
                        //applicationWidgetIconShowBorder = false; // do not show border

                        //applicationWidgetIconLightnessT = GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_12; // lightness of text = black
                        if (applicationWidgetIconLightnessTChangeByNightMode) {
                            switch (applicationWidgetIconLightnessT) {
                                case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_62:
                                    applicationWidgetIconLightnessT = GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_37;
                                    break;
                                case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_75:
                                    applicationWidgetIconLightnessT = GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_25;
                                    break;
                                case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_87:
                                    applicationWidgetIconLightnessT = GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_12;
                                    break;
                                case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_100:
                                    applicationWidgetIconLightnessT = GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_0;
                                    break;
                            }
                            /*
                            SharedPreferences.Editor editor = ApplicationPreferences.getEditor(context.getApplicationContext());
                            editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_LIGHTNESS_T, applicationWidgetIconLightnessT);
                            editor.apply();
                            ApplicationPreferences.applicationWidgetIconLightnessT = applicationWidgetIconLightnessT;
                            */
                        } //else
                            //applicationWidgetIconLightnessT = GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_12; // lightness of text = black
                        //applicationWidgetIconLightnessBorder = "0";
                        if (applicationWidgetIconLightnessBorderChangeByNightMode) {
                            switch (applicationWidgetIconLightnessBorder) {
                                case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_62:
                                    applicationWidgetIconLightnessBorder = GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_37;
                                    break;
                                case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_75:
                                    applicationWidgetIconLightnessBorder = GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_25;
                                    break;
                                case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_87:
                                    applicationWidgetIconLightnessBorder = GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_12;
                                    break;
                                case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_100:
                                    applicationWidgetIconLightnessBorder = GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_0;
                                    break;
                            }
                            /*
                            SharedPreferences.Editor editor = ApplicationPreferences.getEditor(context.getApplicationContext());
                            editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_LIGHTNESS_BORDER, applicationWidgetIconLightnessBorder);
                            editor.apply();
                            ApplicationPreferences.applicationWidgetIconLightnessBorder = applicationWidgetIconLightnessBorder;
                            */
                        }// else
                            //applicationWidgetIconLightnessBorder = GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_0;;
                    }
                }
                if ((/*PPApplication.isPixelLauncherDefault(context) ||*/
                        applicationWidgetIconChangeColorsByNightMode)) {
                    boolean nightModeOn = GlobalGUIRoutines.isNightModeEnabled(context.getApplicationContext());
                    if (nightModeOn) {
                        //applicationWidgetIconLightness = GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_75;
                        if (applicationWidgetIconLightnessChangeByNightMode) {
                            switch (applicationWidgetIconLightness) {
                                case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_0:
                                    applicationWidgetIconLightness = GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_100;
                                    break;
                                case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_12:
                                    applicationWidgetIconLightness = GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_87;
                                    break;
                                case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_25:
                                    applicationWidgetIconLightness = GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_75;
                                    break;
                                case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_37:
                                    applicationWidgetIconLightness = GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_62;
                                    break;
                            }
                            /*
                            SharedPreferences.Editor editor = ApplicationPreferences.getEditor(context.getApplicationContext());
                            editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_LIGHTNESS, applicationWidgetIconLightness);
                            editor.apply();
                            ApplicationPreferences.applicationWidgetIconLightness = applicationWidgetIconLightness;
                            */
                        } //else
                            //applicationWidgetIconLightness = GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_75;
                    } else {
                        //applicationWidgetIconLightness = GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_62;
                        if (applicationWidgetIconLightnessChangeByNightMode) {
                            switch (applicationWidgetIconLightness) {
                                case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_62:
                                    applicationWidgetIconLightness = GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_37;
                                    break;
                                case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_75:
                                    applicationWidgetIconLightness = GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_25;
                                    break;
                                case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_87:
                                    applicationWidgetIconLightness = GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_12;
                                    break;
                                case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_100:
                                    applicationWidgetIconLightness = GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_0;
                                    break;
                            }
                            /*
                            SharedPreferences.Editor editor = ApplicationPreferences.getEditor(context.getApplicationContext());
                            editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_LIGHTNESS, applicationWidgetIconLightness);
                            editor.apply();
                            ApplicationPreferences.applicationWidgetIconLightness = applicationWidgetIconLightness;
                            */
                        } //else
                            //applicationWidgetIconLightness = GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_62;
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
                    profileName = DataWrapperStatic.getProfileNameWithManualIndicator(profile, false, "", true, true, true, true, dataWrapper);
                else
                    profileName = DataWrapperStatic.getProfileNameWithManualIndicator(profile, false, "", false, true, false, true, dataWrapper);
            } else {
                // create empty profile and set icon resource
                profile = new Profile();
                profile._name = context.getString(R.string.profiles_header_profile_name_no_activated);
                profile._icon = StringConstants.PROFILE_ICON_DEFAULT + "|1|0|0";

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
                            if ((profile._endOfActivationType == Profile.AFTER_DURATION_DURATION_TYPE_DURATION) &&
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
                            } else if ((profile._endOfActivationType == Profile.AFTER_DURATION_DURATION_TYPE_EXACT_TIME) &&
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
                            if ((profile._endOfActivationType == Profile.AFTER_DURATION_DURATION_TYPE_DURATION) &&
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
                            } else if ((profile._endOfActivationType == Profile.AFTER_DURATION_DURATION_TYPE_EXACT_TIME) &&
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
                            if ((profile._endOfActivationType == Profile.AFTER_DURATION_DURATION_TYPE_DURATION) &&
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
                            } else if ((profile._endOfActivationType == Profile.AFTER_DURATION_DURATION_TYPE_EXACT_TIME) &&
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
                            if ((profile._endOfActivationType == Profile.AFTER_DURATION_DURATION_TYPE_DURATION) &&
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
                            } else if ((profile._endOfActivationType == Profile.AFTER_DURATION_DURATION_TYPE_EXACT_TIME) &&
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
                            if ((profile._endOfActivationType == Profile.AFTER_DURATION_DURATION_TYPE_DURATION) &&
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
                            } else if ((profile._endOfActivationType == Profile.AFTER_DURATION_DURATION_TYPE_EXACT_TIME) &&
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
                            if ((profile._endOfActivationType == Profile.AFTER_DURATION_DURATION_TYPE_DURATION) &&
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
                            } else if ((profile._endOfActivationType == Profile.AFTER_DURATION_DURATION_TYPE_EXACT_TIME) &&
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
                            if ((profile._endOfActivationType == Profile.AFTER_DURATION_DURATION_TYPE_DURATION) &&
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
                            } else if ((profile._endOfActivationType == Profile.AFTER_DURATION_DURATION_TYPE_EXACT_TIME) &&
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
                            if ((profile._endOfActivationType == Profile.AFTER_DURATION_DURATION_TYPE_DURATION) &&
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
                            } else if ((profile._endOfActivationType == Profile.AFTER_DURATION_DURATION_TYPE_EXACT_TIME) &&
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

                int roundedBackground;
                int roundedBorder;
                if ((Build.VERSION.SDK_INT >= 31) && PPApplicationStatic.isPixelLauncherDefault(context)) {
                    roundedBackground = R.drawable.rounded_widget_background_pixel_launcher;
                    roundedBorder = R.drawable.rounded_widget_border_pixel_launcher;
                }
                else
                if ((Build.VERSION.SDK_INT >= 31) && PPApplicationStatic.isOneUILauncherDefault(context)) {
                    roundedBackground = R.drawable.rounded_widget_background_oneui_launcher;
                    roundedBorder = R.drawable.rounded_widget_border_oneui_launcher;
                } else
                if ((Build.VERSION.SDK_INT >= 31) && PPApplicationStatic.isMIUILauncherDefault(context)) {
                    roundedBackground = R.drawable.rounded_widget_background_miui_launcher;
                    roundedBorder = R.drawable.rounded_widget_border_miui_launcher;
                }/* else
                if ((Build.VERSION.SDK_INT >= 31) && PPApplicationStatic.isSmartLauncherDefault(context)) {
                    roundedBackground = R.drawable.rounded_widget_background_smart_launcher;
                    roundedBorder = R.drawable.rounded_widget_border_smart_launcher;
                }*/ else {
                    roundedBackground = getRoundedBackgroundDrawable(applicationWidgetIconRoundedCornersRadius);
                    roundedBorder = getRoundedBorderDrawable(applicationWidgetIconRoundedCornersRadius);
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
                    //if (!((Build.VERSION.SDK_INT >= 31) && applicationWidgetIconChangeColorsByNightMode &&
                    //        applicationWidgetIconColor.equals("0") && applicationWidgetIconUseDynamicColors))
                    if ((Build.VERSION.SDK_INT >= 31) && (applicationWidgetIconUseDynamicColors)) {
                        int dynamicColor = GlobalGUIRoutines.getDynamicColor(R.attr.colorSecondary, context);
                        if (dynamicColor != 0) {
                            dynamicColor = GlobalGUIRoutines.changeLigtnessOfColor(dynamicColor, redBorder);
                            remoteViews.setInt(R.id.widget_icon_rounded_border, "setColorFilter", dynamicColor);
                        }
                        else
                            remoteViews.setInt(R.id.widget_icon_rounded_border, "setColorFilter", Color.argb(0xFF, redBorder, greenBorder, blueBorder));
                    } else
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
                    if (isIconResourceID) {
                        if (applicationWidgetIconChangeColorsByNightMode ||
                                ((!applicationWidgetIconBackgroundType) &&
                                        (Integer.parseInt(applicationWidgetIconLightnessB) <= 25)) ||
                                (applicationWidgetIconBackgroundType &&
                                        (ColorUtils.calculateLuminance(Integer.parseInt(applicationWidgetIconBackgroundColor)) < 0.23)))
                            bitmap = profile.increaseProfileIconBrightnessForContext(context, profile._iconBitmap);
                    } else
                        bitmap = profile._iconBitmap;
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

                dataWrapper.fillProfileList(false, false);
                boolean doSwitch = false;
                //Intent intent = GlobalGUIRoutines.getIntentForStartupSource(context, PPApplication.STARTUP_SOURCE_WIDGET);
                Intent intent;
                if (applicationWidgetIconLauncher.equals(StringConstants.EXTRA_SWITCH_PROFILES)) {
                    if (dataWrapper.profileList.size() == 2) {
                        doSwitch = true;
                        Profile profileToSwitch = null;
                        if (profile != null) {
                            for (Profile _profile : dataWrapper.profileList) {
                                if (profile._id != _profile._id) {
                                    profileToSwitch = _profile;
                                    break;
                                }
                            }
                        } else {
                            profileToSwitch = dataWrapper.profileList.get(0);
                        }
                        intent = new Intent(context.getApplicationContext(), BackgroundActivateProfileActivity.class);
                        intent.putExtra(PPApplication.EXTRA_PROFILE_ID, profileToSwitch._id);
                        PendingIntent pendingIntent = PendingIntent.getActivity(context, PROFILE_ID_ACTIVATE_PROFILE_ID + (int) profile._id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                        remoteViews.setOnClickPendingIntent(R.id.icon_widget_relLa1, pendingIntent);
                    }
                }
                if (!doSwitch) {
                    if (applicationWidgetIconLauncher.equals(StringConstants.EXTRA_ACTIVATOR) ||
                            applicationWidgetIconLauncher.equals(StringConstants.EXTRA_SWITCH_PROFILES))
                        intent = new Intent(context.getApplicationContext(), ActivatorActivity.class);
                    else
                        intent = new Intent(context.getApplicationContext(), EditorActivity.class);
                    // clear all opened activities
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra(PPApplication.EXTRA_STARTUP_SOURCE, PPApplication.STARTUP_SOURCE_WIDGET);
                    PendingIntent pendingIntent = PendingIntent.getActivity(context, 100, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                    //remoteViews.setOnClickPendingIntent(R.id.icon_widget_icon, pendingIntent);
                    //remoteViews.setOnClickPendingIntent(R.id.icon_widget_name, pendingIntent);
                    remoteViews.setOnClickPendingIntent(R.id.icon_widget_relLa1, pendingIntent);
                }

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
    public void onReceive(Context context, Intent intent) {
        final Context appContext = context.getApplicationContext();
        LocaleHelper.setApplicationLocale(appContext);

        super.onReceive(appContext, intent); // calls onUpdate, is required for widget
//        PPApplicationStatic.logE("[IN_BROADCAST] IconWidgetProvider.onReceive", "xxx");

        final String action = intent.getAction();

        if ((action != null) &&
                (action.equalsIgnoreCase(ACTION_REFRESH_ICONWIDGET))) {
            boolean drawImmediatelly = intent.getBooleanExtra(PPApplication.EXTRA_DRAW_IMMEDIATELY, false);
            AppWidgetManager manager = AppWidgetManager.getInstance(appContext);
            if (manager != null) {
                final int[] appWidgetIds = manager.getAppWidgetIds(new ComponentName(appContext, IconWidgetProvider.class));
                if ((appWidgetIds != null) && (appWidgetIds.length > 0)) {
                    final WeakReference<AppWidgetManager> appWidgetManagerWeakRef = new WeakReference<>(manager);
                    Runnable runnable = () -> {
//                            PPApplicationStatic.logE("[IN_EXECUTOR] PPApplication.startHandlerThreadWidget", "START run - from=IconWidgetProvider.onReceive");

                        PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                        PowerManager.WakeLock wakeLock = null;
                        try {
                            if (powerManager != null) {
                                wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WakelockTags.WAKELOCK_TAG_IconWidgetProvider_onReceive);
                                wakeLock.acquire(10 * 60 * 1000);
                            }

                            AppWidgetManager appWidgetManager = appWidgetManagerWeakRef.get();

                            if (/*(appContext != null) &&*/ (appWidgetManager != null)) {
                                _onUpdate(appContext, appWidgetManager, appWidgetIds);
    //                        This not working. This uses one row profie list provider. Why???
    //                        Intent updateIntent = new Intent();
    //                        updateIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
    //                        updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
    //                        context.sendBroadcast(updateIntent);
                            }

                        } catch (Exception e) {
//                          PPApplicationStatic.logE("[IN_EXECUTOR] IconWidgetProvider.onReceive", Log.getStackTraceString(e));
                            PPApplicationStatic.recordException(e);
                        } finally {
                            if ((wakeLock != null) && wakeLock.isHeld()) {
                                try {
                                    wakeLock.release();
                                } catch (Exception ignored) {
                                }
                            }
                            //worker.shutdown();
                        }
                    };
                    PPApplicationStatic.createDelayedGuiExecutor();
//                    PPApplication.delayedGuiExecutor.submit(runnable);
                    if (PPApplication.scheduledFutureIconWidgetExecutor != null)
                        PPApplication.scheduledFutureIconWidgetExecutor.cancel(true);
                    if (drawImmediatelly)
                        PPApplication.scheduledFutureIconWidgetExecutor =
                                PPApplication.delayedGuiExecutor.schedule(runnable, 200, TimeUnit.MILLISECONDS);
                    else
                        PPApplication.scheduledFutureIconWidgetExecutor =
                            PPApplication.delayedGuiExecutor.schedule(runnable, 5, TimeUnit.SECONDS);
                }
            }
        }
    }

    @Override
    public void onAppWidgetOptionsChanged (Context context,
                                           AppWidgetManager appWidgetManager,
                                           int appWidgetId,
                                           Bundle newOptions) {
//        PPApplicationStatic.logE("[LOCAL_BROADCAST_CALL] IconWidgetProvider.onAppWidgetOptionsChanged", "xxx");
        Intent intent3 = new Intent(ACTION_REFRESH_ICONWIDGET);
        intent3.putExtra(PPApplication.EXTRA_DRAW_IMMEDIATELY, true);
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
        intent3.putExtra(PPApplication.EXTRA_DRAW_IMMEDIATELY, true);
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
