package sk.henrichg.phoneprofilesplus;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.PowerManager;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.CharacterStyle;
import android.view.View;
import android.widget.RemoteViews;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;
import androidx.core.graphics.drawable.IconCompat;
import androidx.palette.graphics.Palette;

import java.util.concurrent.TimeUnit;

public class PPAppNotification {

    static final String ACTION_START_LAUNCHER_FROM_NOTIFICATION = PPApplication.PACKAGE_NAME + ".PhoneProfilesService.ACTION_START_LAUNCHER_FROM_NOTIFICATION";

    static private void _showNotification(final DataWrapper dataWrapper, boolean forFirstStart)
    {
        //synchronized (PPApplication.applicationPreferencesMutex) {
        //    if (PPApplication.doNotShowPPPAppNotification)
        //        return;
        //}

//        PPApplicationStatic.logE("[PPP_NOTIFICATION] PPAppNotification._showNotification", "start");

        final Context appContext = dataWrapper.context.getApplicationContext();
        LocaleHelper.setApplicationLocale(appContext);

        ActivityManager.RunningServiceInfo serviceInfo = GlobalUtils.getServiceInfo(appContext, PhoneProfilesService.class);
        if (serviceInfo == null) {
            // service is not running
            return;
        }

//        PPApplicationStatic.logE("[PPP_NOTIFICATION] PPAppNotification._showNotification", "call of createPPPAppNotificationChannel()");
        PPApplicationStatic.createPPPAppNotificationChannel(appContext, false);

        Intent launcherIntent;
        if (Build.VERSION.SDK_INT < 31) {
            launcherIntent = new Intent(ACTION_START_LAUNCHER_FROM_NOTIFICATION);
        } else {
            launcherIntent = GlobalGUIRoutines.getIntentForStartupSource(appContext, PPApplication.STARTUP_SOURCE_NOTIFICATION);
            // clear all opened activities
            launcherIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK/*|Intent.FLAG_ACTIVITY_NO_ANIMATION*/);
            // setup startupSource
            launcherIntent.putExtra(PPApplication.EXTRA_STARTUP_SOURCE, PPApplication.STARTUP_SOURCE_NOTIFICATION);
        }

        Profile profile = null;

        String notificationNotificationStyle;
        boolean notificationShowProfileIcon;
        String notificationProfileIconColor;
        String notificationProfileIconLightness;
        boolean notificationCustomProfileIconLightness;
        //boolean notificationShowInStatusBar;
        boolean notificationUseDecoration;
        boolean notificationPrefIndicator;
        String notificationPrefIndicatorLightness;
        //boolean notificationHideInLockScreen;
        String notificationStatusBarStyle;
        String notificationTextColor;
        String notificationBackgroundColor;
        int notificationBackgroundCustomColor;
        boolean notificationShowButtonExit;
        String notificationLayoutType;
        boolean notificationShowRestartEventsAsButton;

//        PPApplicationStatic.logE("[PPP_NOTIFICATION] PPAppNotification._showNotification", "forFirstStart="+forFirstStart);

        // !!! Use configured notification style, It is required for restart of PPP by system !!!
        if (forFirstStart) {
            PPApplicationStatic.logE("[SYNCHRONIZED] PPAppNotification._showNotification", "(1) PPApplication.applicationPreferencesMutex");
            synchronized (PPApplication.applicationPreferencesMutex) {
                // load style directly from shared preferences
                ApplicationPreferences.notificationNotificationStyle(appContext);
                notificationNotificationStyle = ApplicationPreferences.notificationNotificationStyle;
                ApplicationPreferences.notificationUseDecoration(appContext);
                notificationUseDecoration = ApplicationPreferences.notificationUseDecoration;
                notificationShowRestartEventsAsButton = ApplicationPreferences.notificationShowRestartEventsAsButton;

                //notificationShowInStatusBar = ApplicationPreferences.notificationShowInStatusBar;
                //notificationHideInLockScreen = ApplicationPreferences.notificationHideInLockScreen;
            }
            notificationShowProfileIcon = false; // for small notification at start
            notificationProfileIconColor = "0";
            notificationProfileIconLightness = GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_100;
            notificationCustomProfileIconLightness = false;

            notificationPrefIndicator = false;
            notificationPrefIndicatorLightness = GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_50;
            notificationStatusBarStyle = "1";

            // default value for Pixel (Android 12) -> 0 (native)
            notificationTextColor = "0";
            // default value for Pixel (Android 12) -> 0 (native)
            notificationBackgroundColor = "0";

            notificationBackgroundCustomColor = 0xFFFFFFFF;
            notificationShowButtonExit = false;
            notificationLayoutType = "2"; // only small layout
        }
        else {
            profile = dataWrapper.getActivatedProfileFromDB(false, false);

            PPApplicationStatic.logE("[SYNCHRONIZED] PPAppNotification._showNotification", "(2) PPApplication.applicationPreferencesMutex");
            synchronized (PPApplication.applicationPreferencesMutex) {

                // load style directly from shared preferences
                ApplicationPreferences.notificationNotificationStyle(appContext);
                notificationNotificationStyle = ApplicationPreferences.notificationNotificationStyle;
                ApplicationPreferences.notificationUseDecoration(appContext);
                notificationUseDecoration = ApplicationPreferences.notificationUseDecoration;
                notificationShowRestartEventsAsButton = ApplicationPreferences.notificationShowRestartEventsAsButton;

                notificationShowProfileIcon = ApplicationPreferences.notificationShowProfileIcon;
                notificationProfileIconColor = ApplicationPreferences.notificationProfileIconColor;
                notificationProfileIconLightness = ApplicationPreferences.notificationProfileIconLightness;
                notificationCustomProfileIconLightness = ApplicationPreferences.notificationCustomProfileIconLightness;

                //notificationShowInStatusBar = ApplicationPreferences.notificationShowInStatusBar;
                notificationPrefIndicator = ApplicationPreferences.notificationPrefIndicator;
                notificationPrefIndicatorLightness = ApplicationPreferences.notificationPrefIndicatorLightness;
                //notificationHideInLockScreen = ApplicationPreferences.notificationHideInLockScreen;
                notificationStatusBarStyle = ApplicationPreferences.notificationStatusBarStyle;
                notificationTextColor = ApplicationPreferences.notificationTextColor;
                notificationBackgroundColor = ApplicationPreferences.notificationBackgroundColor;
                notificationBackgroundCustomColor = ApplicationPreferences.notificationBackgroundCustomColor;
                notificationShowButtonExit = ApplicationPreferences.notificationShowButtonExit;
                notificationLayoutType = ApplicationPreferences.notificationLayoutType;
            }
        }

        int requestCode = 0;
        if (profile != null)
            requestCode = (int)profile._id;

        NotificationCompat.Builder notificationBuilder;

        RemoteViews contentView = null;
        RemoteViews contentViewLarge = null;

        boolean useDecorator;
        boolean useNightColor = GlobalGUIRoutines.isNightModeEnabled(appContext);
        //boolean profileIconExists = true;

        /*
        int nightModeFlags =
                appContext.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        switch (nightModeFlags) {
            case Configuration.UI_MODE_NIGHT_YES:
                useNightColor = 1;
                break;
            case Configuration.UI_MODE_NIGHT_NO:
                useNightColor = 2;
                break;
            case Configuration.UI_MODE_NIGHT_UNDEFINED:
                break;
        }
        */

        if (notificationNotificationStyle.equals("0")) {
            // ----- create content view

            useDecorator = notificationUseDecoration;

            switch (notificationBackgroundColor) {
                case "1":
                case "3":
                    notificationTextColor = "2";
                    break;
            }
            // is not possible to use decoration when notificication background is not "Native"
            useDecorator = useDecorator && notificationBackgroundColor.equals("0");

            boolean powerShadeInstalled = false;
            PackageManager pm = appContext.getPackageManager();
            try {
                pm.getPackageInfo("com.treydev.pns", PackageManager.GET_ACTIVITIES);
                powerShadeInstalled = true;
            } catch (Exception ignored) {}

            if (powerShadeInstalled) {
                if (!useDecorator) {
                    if (notificationPrefIndicator)
                        contentViewLarge = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.ppp_notification_no_decorator);
                    else
                        contentViewLarge = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.ppp_notification_no_decorator_no_indicators);
                }
                else {
                    if (notificationPrefIndicator)
                        contentViewLarge = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.ppp_notification);
                    else
                        contentViewLarge = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.ppp_notification_no_indicators);
                }
                if (!useDecorator) {
                    contentView = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.ppp_notification_compact_no_decorator);
                }
                else {
                    contentView = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.ppp_notification_compact);
                    //profileIconExists = false;
                }
            }
            else
            if (PPApplication.deviceIsXiaomi && PPApplication.romIsMIUI) {
                if (!useDecorator) {
                    if (notificationPrefIndicator)
                        contentViewLarge = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.ppp_notification_miui_no_decorator);
                    else
                        contentViewLarge = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.ppp_notification_miui_no_decorator_no_indicators);
                }
                else {
                    if (notificationPrefIndicator)
                        contentViewLarge = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.ppp_notification_miui);
                    else
                        contentViewLarge = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.ppp_notification_miui_no_indicators);
                }
                if (!useDecorator) {
                    contentView = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.ppp_notification_compact_miui_no_decorator);
                }
                else {
                    contentView = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.ppp_notification_compact_miui);
                    //profileIconExists = false;
                }
            } else if (PPApplication.deviceIsHuawei && PPApplication.romIsEMUI) {
                if (!useDecorator) {
                    if (notificationPrefIndicator)
                        contentViewLarge = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.ppp_notification_emui_no_decorator);
                    else
                        contentViewLarge = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.ppp_notification_emui_no_decorator_no_indicators);
                }
                else {
                    if (notificationPrefIndicator)
                        contentViewLarge = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.ppp_notification_emui);
                    else
                        contentViewLarge = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.ppp_notification_emui_no_indicators);
                }
                if (!useDecorator) {
                    contentView = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.ppp_notification_compact_emui_no_decorator);
                }
                else {
                    contentView = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.ppp_notification_compact);
                    //profileIconExists = false;
                }
            } else if (PPApplication.deviceIsSamsung && PPApplication.romIsGalaxy) {
                if (!useDecorator) {
                    if (notificationPrefIndicator)
                        contentViewLarge = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.ppp_notification_samsung_no_decorator);
                    else
                        contentViewLarge = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.ppp_notification_samsung_no_decorator_no_indicators);
                }
                else {
                    if (notificationPrefIndicator)
                        contentViewLarge = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.ppp_notification);
                    else
                        contentViewLarge = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.ppp_notification_no_indicators);
                }
                if (!useDecorator) {
                    contentView = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.ppp_notification_compact_samsung_no_decorator);
                }
                else {
                    contentView = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.ppp_notification_compact_samsung);
                    //profileIconExists = false;
                }
            } else if (PPApplication.deviceIsOnePlus) {
                if (!useDecorator) {
                    if (notificationPrefIndicator)
                        contentViewLarge = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.ppp_notification_no_decorator);
                    else
                        contentViewLarge = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.ppp_notification_no_decorator_no_indicators);
                }
                else {
                    if (notificationPrefIndicator)
                        contentViewLarge = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.ppp_notification);
                    else
                        contentViewLarge = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.ppp_notification_no_indicators);
                }
                if (!useDecorator) {
                    contentView = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.ppp_notification_compact_oneplus_no_decorator);
                }
                else {
                    contentView = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.ppp_notification_compact);
                    //profileIconExists = false;
                }
            } else {
                if (!useDecorator) {
                    if (notificationPrefIndicator)
                        contentViewLarge = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.ppp_notification_no_decorator);
                    else
                        contentViewLarge = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.ppp_notification_no_decorator_no_indicators);
                }
                else {
                    if (notificationPrefIndicator)
                        contentViewLarge = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.ppp_notification);
                    else
                        contentViewLarge = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.ppp_notification_no_indicators);
                }
                if (!useDecorator) {
                    contentView = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.ppp_notification_compact_no_decorator);
                }
                else {
                    contentView = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.ppp_notification_compact);
                    //profileIconExists = false;
                }
            }
        }
        else
            useDecorator = true; // for native style use decorator, when is supported by system

        boolean isIconResourceID;
        String iconIdentifier;
        String pName;
        Spannable profileName;
        Bitmap iconBitmap;
        Bitmap preferencesIndicatorBitmap;

        int monochromeValue = 0xFF;
        switch (notificationProfileIconLightness) {
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

//        PPApplicationStatic.logE("[PPP_NOTIFICATION] PPAppNotification._showNotification", "profile="+profile);

        // ----- get profile icon, preference indicators, profile name
        if (profile != null)
        {
            isIconResourceID = profile.getIsIconResourceID();
            iconIdentifier = profile.getIconIdentifier();
            profileName = DataWrapperStatic.getProfileNameWithManualIndicator(profile, true, "", true, false, false, dataWrapper);
            // get string from spannable
            Spannable sbt = new SpannableString(profileName);
            Object[] spansToRemove = sbt.getSpans(0, profileName.length(), Object.class);
            for (Object span : spansToRemove) {
                if (span instanceof CharacterStyle)
                    sbt.removeSpan(span);
            }

            //noinspection ConstantConditions
            if (!forFirstStart) {
                if (notificationProfileIconColor.equals("0")) {
                    profile.generateIconBitmap(appContext, false, 0, false);
                }
                else {
                    profile.generateIconBitmap(appContext, true, monochromeValue, notificationCustomProfileIconLightness);
                }

                if (notificationPrefIndicator && (notificationNotificationStyle.equals("0"))) {

                    float prefIndicatorLightnessValue = 0f;
                    int prefIndicatorMonochromeValue = 0x00;
                    switch (notificationPrefIndicatorLightness) {
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

                    int indicatorType = DataWrapper.IT_FOR_NOTIFICATION;
                    if ((Build.VERSION.SDK_INT >= 31) && notificationBackgroundColor.equals("0") &&
                            notificationProfileIconColor.equals("0"))
                        indicatorType = DataWrapper.IT_FOR_NOTIFICATION_DYNAMIC_COLORS;
                    else
                    if ((Build.VERSION.SDK_INT < 31) && notificationBackgroundColor.equals("0") &&
                            notificationProfileIconColor.equals("0"))
                        indicatorType = DataWrapper.IT_FOR_NOTIFICATION_NATIVE_BACKGROUND;
                    else
                    if ((!notificationBackgroundColor.equals("5")) &&
                            notificationProfileIconColor.equals("0"))
                        indicatorType = DataWrapper.IT_FOR_NOTIFICATION_DARK_BACKGROUND;
                    else
                    if (notificationBackgroundColor.equals("5") &&
                            notificationProfileIconColor.equals("0")) {
                        if (ColorUtils.calculateLuminance(notificationBackgroundCustomColor) < 0.23)
                            indicatorType = DataWrapper.IT_FOR_NOTIFICATION_DARK_BACKGROUND;
                        else
                            indicatorType = DataWrapper.IT_FOR_NOTIFICATION_LIGHT_BACKGROUND;
                    }

                    profile.generatePreferencesIndicator(appContext, notificationProfileIconColor.equals("1"),
                            prefIndicatorMonochromeValue,
                            indicatorType,
                            prefIndicatorLightnessValue);

                    preferencesIndicatorBitmap = profile._preferencesIndicator;
                }
                else
                    preferencesIndicatorBitmap = null;
                iconBitmap = profile._iconBitmap;
            }
            else {
                iconBitmap = null;
                preferencesIndicatorBitmap = null;
            }
        }
        else
        {
            isIconResourceID = true;
            iconIdentifier = StringConstants.PROFILE_ICON_DEFAULT;
            if (!forFirstStart)
                pName = appContext.getString(R.string.profiles_header_profile_name_no_activated);
            else
                pName = appContext.getString(R.string.ppp_app_name) + " " +
                        appContext.getString(R.string.application_is_starting_toast);
            profileName = new SpannableString(pName);
            iconBitmap = null;
            preferencesIndicatorBitmap = null;
        }

        PendingIntent pIntent;
        if (Build.VERSION.SDK_INT < 31)
            pIntent = PendingIntent.getBroadcast(appContext, requestCode, launcherIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        else
            pIntent = PendingIntent.getActivity(appContext, requestCode, launcherIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        // ----- create notificationBuilders
        notificationBuilder = new NotificationCompat.Builder(appContext, PPApplication.PROFILE_NOTIFICATION_CHANNEL);
        /*}
        else {
            notificationBuilder = new NotificationCompat.Builder(appContext, PPApplication.PROFILE_NOTIFICATION_CHANNEL);
            if (notificationShowInStatusBar) {
                KeyguardManager myKM = (KeyguardManager) appContext.getSystemService(Context.KEYGUARD_SERVICE);
                if (myKM != null) {
                    boolean screenUnlocked = !myKM.isKeyguardLocked();
                    if ((notificationHideInLockScreen && (!screenUnlocked)) ||
                            ((profile != null) && profile._hideStatusBarIcon))
                        notificationBuilder.setPriority(NotificationCompat.PRIORITY_MIN);
                    else
                        notificationBuilder.setPriority(NotificationCompat.PRIORITY_LOW);
                }
                else
                    notificationBuilder.setPriority(NotificationCompat.PRIORITY_LOW);
            }
            else {
                notificationBuilder.setPriority(NotificationCompat.PRIORITY_MIN);
            }
        }*/

        notificationBuilder.setContentIntent(pIntent);

        // Android 12:
        // The service provides a use case related to phone calls, navigation, or media playback,
        // as defined in the notification's category attribute.
        // Use CATEGORY_NAVIGATION to show notification in DND
        notificationBuilder.setCategory(NotificationCompat.CATEGORY_NAVIGATION);

        notificationBuilder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);

        // this disable timestamp in decorator
        notificationBuilder.setShowWhen(false);

        //notificationBuilder.setTicker(profileName);

        _addRestartEventsToNotification(forFirstStart,
                contentView, contentViewLarge,
                notificationBuilder,
                notificationNotificationStyle, notificationShowRestartEventsAsButton,
                notificationBackgroundColor, notificationBackgroundCustomColor,
                notificationProfileIconColor,
                useDecorator, useNightColor,
                appContext);
//        PPApplicationStatic.logE("[PPP_NOTIFICATION] PPAppNotification._showNotification", "after _addRestartEventsToNotification");

        // ----- set icons

        int decoratorColor = ContextCompat.getColor(appContext, R.color.notification_color);

        // decorator colot change by iocn is removed, becouse this cause problems with
        // custom icons.
        NotificationIconData notificationIconData =
                _addProfileIconToNotification(appContext, forFirstStart,
                        contentView, contentViewLarge,
                        notificationBuilder,
                        notificationNotificationStyle, notificationStatusBarStyle,

                        notificationBackgroundColor,
                        notificationBackgroundCustomColor,

                        notificationShowProfileIcon,
                        notificationProfileIconColor,
                        monochromeValue,
                        profile,
                        isIconResourceID, iconBitmap,
                        iconIdentifier,
                        //profileIconExists,
                        useDecorator,
                        decoratorColor,
                        appContext);
        decoratorColor = notificationIconData.decoratorColor;

//        PPApplicationStatic.logE("[PPP_NOTIFICATION] PPAppNotification._showNotification", "after _addProfileIconToNotification");

        if (notificationProfileIconColor.equals("0"))
            notificationBuilder.setColor(decoratorColor);

        // notification title
        if (notificationNotificationStyle.equals("0")) {
            contentViewLarge.setTextViewText(R.id.notification_activated_profile_name, profileName);
            if (contentView != null)
                contentView.setTextViewText(R.id.notification_activated_profile_name, profileName);
        }
        else {
            notificationBuilder.setContentTitle(profileName.toString());
        }

        // profile preferences indicator
        String indicators = null;
        try {
            if (notificationNotificationStyle.equals("0")) {
                if (notificationPrefIndicator) {
                    if (preferencesIndicatorBitmap != null) {
                        contentViewLarge.setImageViewBitmap(R.id.notification_activated_profile_pref_indicator, preferencesIndicatorBitmap);
                        contentViewLarge.setViewVisibility(R.id.notification_activated_profile_pref_indicator, View.VISIBLE);
                    } else {
                        contentViewLarge.setViewVisibility(R.id.notification_activated_profile_pref_indicator, View.GONE);
                    }
                }
                else {
                    contentViewLarge.setViewVisibility(R.id.notification_activated_profile_pref_indicator, View.GONE);
                }
            }
            else {
                if (notificationPrefIndicator) {
                    ProfilePreferencesIndicator _indicators = new ProfilePreferencesIndicator();
                    indicators = _indicators.getString(profile, /*0,*/ appContext);

                    // do not show indicators in collased notification ;-)
                    //notificationBuilder.setContentText(indicators);

                }
                else {
                    notificationBuilder.setContentText(null);
                }
            }
        } catch (Exception e) {
//            PPApplicationStatic.logE("[PPP_NOTIFICATION] PPAppNotification._showNotification", Log.getStackTraceString(e));
            PPApplicationStatic.recordException(e);
        }

        if (notificationNotificationStyle.equals("0")) {

            switch (notificationBackgroundColor) {
                case "3":
                    //if (!notificationNightMode || (useNightColor == 1)) {
                    int color = ContextCompat.getColor(appContext, R.color.notificationBlackBackgroundColor);
                    contentViewLarge.setInt(R.id.notification_activated_profile_root, "setBackgroundColor", color);
                    if (contentView != null)
                        contentView.setInt(R.id.notification_activated_profile_root, "setBackgroundColor", color);
                    break;
                case "1":
                    //if (!notificationNightMode || (useNightColor == 1)) {
                    color = ContextCompat.getColor(appContext, R.color.notificationDarkBackgroundColor);
                    contentViewLarge.setInt(R.id.notification_activated_profile_root, "setBackgroundColor", color);
                    if (contentView != null)
                        contentView.setInt(R.id.notification_activated_profile_root, "setBackgroundColor", color);
                    break;
                case "5":
                    //if (!notificationNightMode || (useNightColor == 1)) {
                    contentViewLarge.setInt(R.id.notification_activated_profile_root, "setBackgroundColor", notificationBackgroundCustomColor);
                    if (contentView != null)
                        contentView.setInt(R.id.notification_activated_profile_root, "setBackgroundColor", notificationBackgroundCustomColor);
                    break;
                default:
                    //int color = getResources().getColor(R.color.notificationBackground);
                    contentViewLarge.setInt(R.id.notification_activated_profile_root, "setBackgroundColor", Color.TRANSPARENT);
                    if (contentView != null)
                        contentView.setInt(R.id.notification_activated_profile_root, "setBackgroundColor", Color.TRANSPARENT);
                    break;
            }

            if (notificationTextColor.equals("1")) {
                contentViewLarge.setTextColor(R.id.notification_activated_profile_name, Color.BLACK);
                if (contentView != null)
                    contentView.setTextColor(R.id.notification_activated_profile_name, Color.BLACK);
            } else if (notificationTextColor.equals("2")) {
                contentViewLarge.setTextColor(R.id.notification_activated_profile_name, Color.WHITE);
                if (contentView != null)
                    contentView.setTextColor(R.id.notification_activated_profile_name, Color.WHITE);
            } else {
                if (Build.VERSION.SDK_INT == 28) {
                    // In 28 (Android 9) exists Dark theme, but not working in emulator,
                    // must be tested in device (for example Nexus 5x).

                    // In Android 9 is exception from normal functionality.
                    // Device theme do not change text color
                    // For this, must be changed programmatically
                    if (useNightColor/* == 1*/) {
                        contentViewLarge.setTextColor(R.id.notification_activated_profile_name, Color.WHITE);
                        if (contentView != null)
                            contentView.setTextColor(R.id.notification_activated_profile_name, Color.WHITE);
                    } else {
                        contentViewLarge.setTextColor(R.id.notification_activated_profile_name, Color.BLACK);
                        if (contentView != null)
                            contentView.setTextColor(R.id.notification_activated_profile_name, Color.BLACK);
                    }
                } else {
                    if (notificationProfileIconColor.equals("0")) {
                        int color = GlobalGUIRoutines.getDynamicColor(R.attr.colorOnBackground, appContext);
                        if (color != 0) {
                            // In One UI is not used Material You in motifications
                            //if (!(PPApplication.deviceIsSamsung && PPApplication.romIsGalaxy)) {
                                contentViewLarge.setTextColor(R.id.notification_activated_profile_name, color);
                                if (contentView != null)
                                    contentView.setTextColor(R.id.notification_activated_profile_name, color);
                            //}
                        }
                    }
                }
            }
        }

        if (notificationNotificationStyle.equals("0")) {

            //noinspection StatementWithEmptyBody
            if (useDecorator) {
                notificationBuilder.setStyle(new NotificationCompat.DecoratedCustomViewStyle());

                //notificationBuilder.setCustomContentView(contentView);
                //notificationBuilder.setCustomBigContentView(contentViewLarge);
            }
            else {
                //https://developer.android.com/develop/ui/views/notifications/custom-notification
                // If you don't want your notification decorated with the standard notification icon and header,
                // follow the steps above but do not call setStyle().
                // But long time is used this and notification working, then keep call this
                notificationBuilder.setStyle(null);

                /*
                switch (notificationLayoutType) {
                    case "1":
                        // only large layout
                        notificationBuilder.setCustomContentView(contentViewLarge);
                        break;
                    case "2":
                        // only small layout
                        notificationBuilder.setCustomContentView(contentView);
                        break;
                    default:
                        // expandable layout
                        notificationBuilder.setCustomContentView(contentView);
                        notificationBuilder.setCustomBigContentView(contentViewLarge);
                        break;
                }
                */
            }

            switch (notificationLayoutType) {
                case "1":
                    // only large layout
                    notificationBuilder.setCustomContentView(contentViewLarge);
                    break;
                case "2":
                    // only small layout
                    notificationBuilder.setCustomContentView(contentView);
                    break;
                default:
                    // expandable layout
                    notificationBuilder.setCustomContentView(contentView);
                    notificationBuilder.setCustomBigContentView(contentViewLarge);
                    break;
            }

        }
        else {
            if (Build.VERSION.SDK_INT >= 31) {
                // this style is used, because large icon and indicators are hidden in collapsed mode
                // but is reserved small space for bigPicture in expanded mode:-(
                if (notificationShowProfileIcon)
                    notificationBuilder.setStyle(new NotificationCompat.BigPictureStyle()
                                    .setBigContentTitle(profileName.toString())
                                    .setSummaryText(indicators)
                                    .bigLargeIcon(notificationIconData.imageBitmap)
                                    .bigPicture((Bitmap)null)
                    );
                else
                    notificationBuilder.setStyle(new NotificationCompat.BigPictureStyle()
                                    .setBigContentTitle(profileName.toString())
                                    .setSummaryText(indicators)
                                    .bigLargeIcon((Bitmap)null)
                                    .bigPicture((Bitmap)null)
                    );
            }
            else
                notificationBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(indicators));
        }

        if ((notificationShowButtonExit) && useDecorator) {
            // add action button to stop application

            Intent exitAppIntent = new Intent(appContext, ExitApplicationActivity.class);
            // clear all opened activities
            exitAppIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            PendingIntent pExitAppIntent = PendingIntent.getActivity(appContext, 0, exitAppIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            //int exitAppId = R.drawable.ic_action_exit_app;

            NotificationCompat.Action.Builder actionBuilder;
            actionBuilder = new NotificationCompat.Action.Builder(
                    R.drawable.ic_empty,
                    //exitAppId,
                    appContext.getString(R.string.menu_exit),
                    pExitAppIntent);
            notificationBuilder.addAction(actionBuilder.build());
        }

        notificationBuilder.setOnlyAlertOnce(true);

        notificationBuilder.setGroup(PPApplication.PROFILE_NOTIFICATION_GROUP);

        if (Build.VERSION.SDK_INT >= 33) {
            // required, because in API 33+ foreground serbice notification is dismissable
            notificationBuilder.setOngoing(true);
        }
        if (Build.VERSION.SDK_INT >= 33) {
//            Log.e("PPAppNotification._showNotification", "add delete intent");
            Intent deleteIntent = new Intent(PPAppNotificationDeletedReceiver.ACTION_PP_APP_NOTIFICATION_DELETED);
            PendingIntent deletePendingIntent = PendingIntent.getBroadcast(appContext, 0, deleteIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            notificationBuilder.setDeleteIntent(deletePendingIntent);
        }

        Notification phoneProfilesNotification;
        try {
            phoneProfilesNotification = notificationBuilder.build();
        } catch (Exception e) {
//            PPApplicationStatic.logE("[PPP_NOTIFICATION] PPAppNotification._showNotification", Log.getStackTraceString(e));
            phoneProfilesNotification = null;
        }

        if (phoneProfilesNotification != null) {

            // do not use Notification.FLAG_ONGOING_EVENT,
            // with this flag, is not possible to colapse this notification
            phoneProfilesNotification.flags |= Notification.FLAG_NO_CLEAR; //| Notification.FLAG_ONGOING_EVENT;

            if (PhoneProfilesService.getInstance() != null) {
                PhoneProfilesService.getInstance().startForeground(PPApplication.PROFILE_NOTIFICATION_ID, phoneProfilesNotification);
//                PPApplicationStatic.logE("[PPP_NOTIFICATION] PPAppNotification._showNotification", "after startForeground");
            }
        }

        /*
        if (profile != null) {
            profile.releaseIconBitmap();
            profile.releasePreferencesIndicator();
        }
        if (iconBitmap != null) {
            if (!iconBitmap.isRecycled())
                iconBitmap.recycle();
        }
        if (preferencesIndicatorBitmap != null) {
            if (!preferencesIndicatorBitmap.isRecycled())
                preferencesIndicatorBitmap.recycle();
        }
        */
    }

    static private void _addRestartEventsToNotification(boolean forFirstStart,
                                                               RemoteViews contentView, RemoteViews contentViewLarge,
                                                               NotificationCompat.Builder notificationBuilder,
                                                               String notificationNotificationStyle, boolean notificationShowRestartEventsAsButton,
                                                               String notificationBackgroundColor, int notificationBackgroundCustomColor,
                                                               String notificationProfileIconColor,
                                                               boolean useDecorator, boolean useNightColor,
                                                               Context appContext) {
        if (!forFirstStart) {
            PendingIntent pIntentRE; //= null;
            // intent for restart events
            Intent intentRE = new Intent(appContext, RestartEventsFromGUIActivity.class);
            intentRE.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            pIntentRE = PendingIntent.getActivity(appContext, 2, intentRE, PendingIntent.FLAG_UPDATE_CURRENT);

            if (notificationNotificationStyle.equals("0")) {

                if ((!useDecorator) || (!notificationShowRestartEventsAsButton)) {
                    Bitmap restartEventsBitmap = BitmapManipulator.getBitmapFromResource(R.drawable.ic_widget_restart_events, true, appContext);
                    if (notificationBackgroundColor.equals("1") || notificationBackgroundColor.equals("3")) {
                        // dark or black
                        restartEventsBitmap = BitmapManipulator.monochromeBitmap(restartEventsBitmap, 0xe0e0e0);
                        //restartEventsId = R.drawable.ic_widget_restart_events_dark;
                    } else if (notificationBackgroundColor.equals("5")) {
                        // custom color
                        if (ColorUtils.calculateLuminance(notificationBackgroundCustomColor) < 0.23)
                            restartEventsBitmap = BitmapManipulator.monochromeBitmap(restartEventsBitmap, 0xe0e0e0);
                            //restartEventsId = R.drawable.ic_widget_restart_events_dark;
                        else
                            restartEventsBitmap = BitmapManipulator.monochromeBitmap(restartEventsBitmap, 0x202020);
                        //restartEventsId = R.drawable.ic_widget_restart_events;
                    } else {
                        // native
                        if ((Build.VERSION.SDK_INT >= 31) && notificationProfileIconColor.equals("0")) {
                            int color = GlobalGUIRoutines.getDynamicColor(R.attr.colorSecondary, appContext);
                            if (color != 0) {
                                restartEventsBitmap = BitmapManipulator.recolorBitmap(restartEventsBitmap, color);
                            } else {
                                if (useNightColor/* == 1*/) {
                                    restartEventsBitmap = BitmapManipulator.monochromeBitmap(restartEventsBitmap, 0xe0e0e0);
                                    //restartEventsId = R.drawable.ic_widget_restart_events_dark;
                                } else {
                                    restartEventsBitmap = BitmapManipulator.monochromeBitmap(restartEventsBitmap, 0x202020);
                                    //restartEventsId = R.drawable.ic_widget_restart_events;
                                }
                            }
                        } else {
                            // Hm, dark mode is possible to change only from API 28 (from GUI).
                            //
                            // In 28 (Android 9) it is Device theme, but not working in emulator,
                            // must be tested in device (for example Nexus 5x).
                            if (useNightColor/* == 1*/) {
                                restartEventsBitmap = BitmapManipulator.monochromeBitmap(restartEventsBitmap, 0xe0e0e0);
                                //restartEventsId = R.drawable.ic_widget_restart_events_dark;
                            } else {
                                restartEventsBitmap = BitmapManipulator.monochromeBitmap(restartEventsBitmap, 0x202020);
                                //restartEventsId = R.drawable.ic_widget_restart_events;
                            }
                        }
                    }

                    try {
                        contentViewLarge.setViewVisibility(R.id.notification_activated_profile_restart_events, View.VISIBLE);
                        contentViewLarge.setImageViewBitmap(R.id.notification_activated_profile_restart_events, restartEventsBitmap);
                        //contentViewLarge.setImageViewResource(R.id.notification_activated_profile_restart_events, restartEventsId);
                        contentViewLarge.setOnClickPendingIntent(R.id.notification_activated_profile_restart_events, pIntentRE);

                        if (contentView != null) {
                            contentView.setViewVisibility(R.id.notification_activated_profile_restart_events, View.VISIBLE);
                            contentView.setImageViewBitmap(R.id.notification_activated_profile_restart_events, restartEventsBitmap);
                            //contentView.setImageViewResource(R.id.notification_activated_profile_restart_events, restartEventsId);
                            contentView.setOnClickPendingIntent(R.id.notification_activated_profile_restart_events, pIntentRE);
                        }
                    } catch (Exception e) {
                        PPApplicationStatic.recordException(e);
//                        PPApplicationStatic.logE("[PPP_NOTIFICATION] PPAppNotification._addRestartEventsToNotification", Log.getStackTraceString(e));
                    }
                }
                else {
                    try {
                        contentViewLarge.setViewVisibility(R.id.notification_activated_profile_restart_events, View.GONE);

                        if (contentView != null)
                            contentView.setViewVisibility(R.id.notification_activated_profile_restart_events, View.GONE);
                    } catch (Exception e) {
                        PPApplicationStatic.recordException(e);
//                        PPApplicationStatic.logE("[PPP_NOTIFICATION] PPAppNotification._addRestartEventsToNotification", Log.getStackTraceString(e));
                    }

                    /*
                    Bitmap restartEventsBitmap = BitmapManipulator.getBitmapFromResource(R.drawable.ic_widget_restart_events, true, appContext);
                    if (Build.VERSION.SDK_INT >= 29) {
                        if (useNightColor == 1) {
                            restartEventsBitmap = BitmapManipulator.monochromeBitmap(restartEventsBitmap, 0xe0e0e0);
                            //restartEventsId = R.drawable.ic_widget_restart_events_dark;
                        } else {
                            restartEventsBitmap = BitmapManipulator.monochromeBitmap(restartEventsBitmap, 0x202020);
                            //restartEventsId = R.drawable.ic_widget_restart_events;
                        }
                    } else {
                        if (notificationTextColor.equals("1"))
                            restartEventsBitmap = BitmapManipulator.monochromeBitmap(restartEventsBitmap, 0x202020);
                            //restartEventsId = R.drawable.ic_widget_restart_events;
                        else if (notificationTextColor.equals("2"))
                            restartEventsBitmap = BitmapManipulator.monochromeBitmap(restartEventsBitmap, 0xe0e0e0);
                            //restartEventsId = R.drawable.ic_widget_restart_events_dark;
                        else
                            restartEventsBitmap = BitmapManipulator.monochromeBitmap(restartEventsBitmap, 0x202020);
                            //restartEventsId = R.drawable.ic_widget_restart_events;
                    }
                    */

                    NotificationCompat.Action.Builder actionBuilder;
                    actionBuilder = new NotificationCompat.Action.Builder(
                            //IconCompat.createWithBitmap(restartEventsBitmap),
                            R.drawable.ic_empty,
                            //restartEventsId,
                            appContext.getString(R.string.menu_restart_events),
                            pIntentRE);
                    notificationBuilder.addAction(actionBuilder.build());
                }
            } else {
                NotificationCompat.Action.Builder actionBuilder;

                /*
                Bitmap restartEventsBitmap = BitmapManipulator.getBitmapFromResource(R.drawable.ic_widget_restart_events, true, appContext);
                if (Build.VERSION.SDK_INT >= 29) {
                    if (useNightColor == 1) {
                        restartEventsBitmap = BitmapManipulator.monochromeBitmap(restartEventsBitmap, 0xe0e0e0);
                        //restartEventsId = R.drawable.ic_widget_restart_events_dark;
                    } else {
                        restartEventsBitmap = BitmapManipulator.monochromeBitmap(restartEventsBitmap, 0x202020);
                        //restartEventsId = R.drawable.ic_widget_restart_events;
                    }
                } else {
                    if (notificationTextColor.equals("1"))
                        restartEventsBitmap = BitmapManipulator.monochromeBitmap(restartEventsBitmap, 0x202020);
                        //restartEventsId = R.drawable.ic_widget_restart_events;
                    else if (notificationTextColor.equals("2"))
                        restartEventsBitmap = BitmapManipulator.monochromeBitmap(restartEventsBitmap, 0xe0e0e0);
                        //restartEventsId = R.drawable.ic_widget_restart_events_dark;
                    else
                        restartEventsBitmap = BitmapManipulator.monochromeBitmap(restartEventsBitmap, 0x202020);
                        //restartEventsId = R.drawable.ic_widget_restart_events;
                }
                */

                actionBuilder = new NotificationCompat.Action.Builder(
                        //IconCompat.createWithBitmap(restartEventsBitmap),
                        R.drawable.ic_empty,
                        //restartEventsId,
                        appContext.getString(R.string.menu_restart_events),
                        pIntentRE);
                notificationBuilder.addAction(actionBuilder.build());
            }
        }
        else {
            if (notificationNotificationStyle.equals("0")) {
                try {
                    if (contentViewLarge != null)
                        contentViewLarge.setViewVisibility(R.id.notification_activated_profile_restart_events, View.GONE);
                    if (contentView != null)
                        contentView.setViewVisibility(R.id.notification_activated_profile_restart_events, View.GONE);
                } catch (Exception e) {
                    PPApplicationStatic.recordException(e);
//                    PPApplicationStatic.logE("[PPP_NOTIFICATION] PPAppNotification._addRestartEventsToNotification", Log.getStackTraceString(e));
                }
            }
        }
    }

    private static class NotificationIconData {
        int decoratorColor;
        Bitmap imageBitmap;
    }

    static private NotificationIconData _addProfileIconToNotification(Context context,
                                                                             boolean forFirstStart,
                                                                             RemoteViews contentView, RemoteViews contentViewLarge,
                                                                             NotificationCompat.Builder notificationBuilder,
                                                                             String notificationNotificationStyle, String notificationStatusBarStyle,

                                                                             String notificationBackgroundColor,
                                                                             int notificationBackgroundCustomColor,

                                                                             boolean notificationShowProfileIcon,
                                                                             String notificationProfileIconColor,
                                                                             int notificationProfileIconMonochromeValue,
                                                                             Profile profile,
                                                                             boolean isIconResourceID, Bitmap iconBitmap,
                                                                             String iconIdentifier,
                                                                             boolean useDecorator,
                                                                             int decoratorColor,
                                                                             Context appContext) {

        NotificationIconData notificationIconData = new NotificationIconData();

        if (!forFirstStart) {
            if (isIconResourceID) {
                int iconSmallResource;
                if (iconBitmap != null) {
                    if (notificationStatusBarStyle.equals("0")) {
                        // colorful icon

                        notificationBuilder.setSmallIcon(IconCompat.createWithBitmap(iconBitmap));
                    } else {
                        // native icon

                        iconSmallResource = R.drawable.ic_profile_default_notify;
                        try {
                            if ((iconIdentifier != null) && (!iconIdentifier.isEmpty())) {
                                Object obj = Profile.profileIconNotifyId.get(iconIdentifier);
                                if (obj != null)
                                    iconSmallResource = (int) obj;
                            }
                        } catch (Exception e) {
                            PPApplicationStatic.recordException(e);
//                            PPApplicationStatic.logE("[PPP_NOTIFICATION] PPAppNotification._addProfileIconToNotification", Log.getStackTraceString(e));
                        }
                        notificationBuilder.setSmallIcon(iconSmallResource);
                    }

                    if (notificationProfileIconColor.equals("0")) {
                        if ((!notificationBackgroundColor.equals("5")) ||
                                (ColorUtils.calculateLuminance(notificationBackgroundCustomColor) < 0.23)) {
                            if (profile != null) {
                                Bitmap bitmap = profile.increaseProfileIconBrightnessForContext(appContext, iconBitmap);
                                if (bitmap != null)
                                    iconBitmap = bitmap;
                            }
                        }
                    }

                    if (notificationNotificationStyle.equals("0")) {
                        try {
                            contentViewLarge.setImageViewBitmap(R.id.notification_activated_profile_icon, iconBitmap);
                            if ((!notificationShowProfileIcon) && useDecorator)
                                contentViewLarge.setViewVisibility(R.id.notification_activated_profile_icon, View.GONE);
                            //if (profileIconExists) {
                            if (contentView != null) {
                                contentView.setImageViewBitmap(R.id.notification_activated_profile_icon, iconBitmap);
                                if ((!notificationShowProfileIcon) && useDecorator)
                                    contentView.setViewVisibility(R.id.notification_activated_profile_icon, View.GONE);
                            }
                            //}
                        } catch (Exception e) {
                            PPApplicationStatic.recordException(e);
//                            PPApplicationStatic.logE("[PPP_NOTIFICATION] PPAppNotification._addProfileIconToNotification", Log.getStackTraceString(e));
                        }
                        notificationBuilder.setLargeIcon((Bitmap) null);
                    }
                    else {
                        // icon will be set in NotificationCompat.BigPictureStyle() for level 31+
                        if ((Build.VERSION.SDK_INT < 31) && notificationShowProfileIcon)
                            notificationBuilder.setLargeIcon(iconBitmap);
                    }
                } else {

                    if (notificationStatusBarStyle.equals("0")) {
                        iconSmallResource = R.drawable.ic_profile_default_notify_color;
                        try {
                            if ((iconIdentifier != null) && (!iconIdentifier.isEmpty())) {
                                Object idx = Profile.profileIconNotifyColorId.get(iconIdentifier);
                                if (idx != null)
                                    iconSmallResource = (int) idx;
                            }
                        } catch (Exception e) {
                            PPApplicationStatic.recordException(e);
//                            PPApplicationStatic.logE("[PPP_NOTIFICATION] PPAppNotification._addProfileIconToNotification", Log.getStackTraceString(e));
                        }
                    } else {
                        iconSmallResource = R.drawable.ic_profile_default_notify;
                        try {
                            if ((iconIdentifier != null) && (!iconIdentifier.isEmpty())) {
                                Object idx = Profile.profileIconNotifyId.get(iconIdentifier);
                                if (idx != null)
                                    iconSmallResource = (int) idx;
                            }
                        } catch (Exception e) {
                            PPApplicationStatic.recordException(e);
//                            PPApplicationStatic.logE("[PPP_NOTIFICATION] PPAppNotification._addProfileIconToNotification", Log.getStackTraceString(e));
                        }
                    }
                    notificationBuilder.setSmallIcon(iconSmallResource);

                    int iconLargeResource = ProfileStatic.getIconResource(iconIdentifier);
                    iconBitmap = BitmapManipulator.getBitmapFromResource(iconLargeResource, true, appContext);
                    if (iconBitmap != null) {
                        if (notificationProfileIconColor.equals("1"))
                            iconBitmap = BitmapManipulator.monochromeBitmap(iconBitmap, notificationProfileIconMonochromeValue);
                        else {
                            if (profile != null) {
                                Bitmap bitmap = profile.increaseProfileIconBrightnessForContext(appContext, iconBitmap);
                                if (bitmap != null)
                                    iconBitmap = bitmap;
                            }
                        }
                    }
                    if (iconBitmap == null) {
                        iconBitmap = BitmapManipulator.getBitmapFromResource(R.drawable.ic_profile_default, true, appContext);
                    }

                    if (notificationNotificationStyle.equals("0")) {
                        try {
                            contentViewLarge.setImageViewBitmap(R.id.notification_activated_profile_icon, iconBitmap);
                            if ((!notificationShowProfileIcon) && useDecorator)
                                contentViewLarge.setViewVisibility(R.id.notification_activated_profile_icon, View.GONE);
                            //if (profileIconExists) {
                            if (contentView != null) {
                                contentView.setImageViewBitmap(R.id.notification_activated_profile_icon, iconBitmap);
                                if ((!notificationShowProfileIcon) && useDecorator)
                                    contentView.setViewVisibility(R.id.notification_activated_profile_icon, View.GONE);
                            }
                            //}
                        } catch (Exception e) {
                            PPApplicationStatic.recordException(e);
//                            PPApplicationStatic.logE("[PPP_NOTIFICATION] PPAppNotification._addProfileIconToNotification", Log.getStackTraceString(e));
                        }
                        notificationBuilder.setLargeIcon((Bitmap) null);
                    } else {
                        // icon will be set in NotificationCompat.BigPictureStyle() for level 31+
                        if ((Build.VERSION.SDK_INT < 31) && notificationShowProfileIcon)
                            notificationBuilder.setLargeIcon(iconBitmap);
                    }
                }


                // do not use increaseNotificationDecorationBrightness(),
                // because icon will not be visible in AOD
                /* int color = 0;
                if (profile != null)
                    color = profile.increaseNotificationDecorationBrightness(appContext);
                if (color != 0)
                    decoratorColor = color;
                else*/ {
                    if ((profile != null) && (profile.getUseCustomColorForIcon()))
                        decoratorColor = profile.getIconCustomColor();
                    else {
                        if ((iconIdentifier != null) && (!iconIdentifier.isEmpty())) {
                            decoratorColor = ProfileStatic.getIconDefaultColor(iconIdentifier);
                        }
                    }
                }

            } else {

                if (iconBitmap != null) {
                    if (notificationStatusBarStyle.equals("2") /*||
                            PPApplication.deviceIsSamsung*/) {
                        Bitmap _iconBitmap = BitmapManipulator.monochromeBitmap(iconBitmap, 0xFF);
                        notificationBuilder.setSmallIcon(IconCompat.createWithBitmap(_iconBitmap));
                        //notificationBuilder.setSmallIcon(R.drawable.ic_profile_default_notify);
                    }
                    else
                        notificationBuilder.setSmallIcon(IconCompat.createWithBitmap(iconBitmap));
                } else {
                    int iconSmallResource;
                    if (notificationStatusBarStyle.equals("0"))
                        iconSmallResource = R.drawable.ic_profile_default;
                    else
                        iconSmallResource = R.drawable.ic_profile_default_notify;
                    notificationBuilder.setSmallIcon(iconSmallResource);
                }

                if (profile != null) {
                    Bitmap bitmap = profile.increaseProfileIconBrightnessForContext(appContext, iconBitmap);
                    if (bitmap != null)
                        iconBitmap = bitmap;
                }
                if (iconBitmap == null) {
                    iconBitmap = BitmapManipulator.getBitmapFromResource(R.drawable.ic_profile_default, true, appContext);
                }

                if (notificationNotificationStyle.equals("0")) {
                    try {
                        if (iconBitmap != null)
                            contentViewLarge.setImageViewBitmap(R.id.notification_activated_profile_icon, iconBitmap);
                        else {
                            if (notificationProfileIconColor.equals("0"))
                                contentViewLarge.setImageViewResource(R.id.notification_activated_profile_icon, R.drawable.ic_profile_default);
                            else {
                                iconBitmap = BitmapManipulator.getBitmapFromResource(R.drawable.ic_profile_default, true, appContext);
                                iconBitmap = BitmapManipulator.monochromeBitmap(iconBitmap, notificationProfileIconMonochromeValue);
                                contentViewLarge.setImageViewBitmap(R.id.notification_activated_profile_icon, iconBitmap);
                            }
                        }
                        if ((!notificationShowProfileIcon) && useDecorator)
                            contentViewLarge.setViewVisibility(R.id.notification_activated_profile_icon, View.GONE);
                        //if (profileIconExists) {
                        if (contentView != null) {
                            if (iconBitmap != null)
                                contentView.setImageViewBitmap(R.id.notification_activated_profile_icon, iconBitmap);
                            else {
                                if (notificationProfileIconColor.equals("0"))
                                    contentView.setImageViewResource(R.id.notification_activated_profile_icon, R.drawable.ic_profile_default);
                                else {
                                    iconBitmap = BitmapManipulator.getBitmapFromResource(R.drawable.ic_profile_default, true, appContext);
                                    iconBitmap = BitmapManipulator.monochromeBitmap(iconBitmap, notificationProfileIconMonochromeValue);
                                    contentViewLarge.setImageViewBitmap(R.id.notification_activated_profile_icon, iconBitmap);
                                }
                            }
                            if ((!notificationShowProfileIcon) && useDecorator)
                                contentView.setViewVisibility(R.id.notification_activated_profile_icon, View.GONE);
                        }
                        //}
                    } catch (Exception e) {
                        PPApplicationStatic.recordException(e);
//                        PPApplicationStatic.logE("[PPP_NOTIFICATION] PPAppNotification._addProfileIconToNotification", Log.getStackTraceString(e));
                    }
                    notificationBuilder.setLargeIcon((Bitmap) null);
                }
                else {
                    if (notificationShowProfileIcon) {
                        if (iconBitmap == null)
                            iconBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_profile_default);
                        if (notificationProfileIconColor.equals("1"))
                            iconBitmap = BitmapManipulator.monochromeBitmap(iconBitmap, notificationProfileIconMonochromeValue);
                        // icon will be set in NotificationCompat.BigPictureStyle() for level 31+
                        if (Build.VERSION.SDK_INT < 31)
                            notificationBuilder.setLargeIcon(iconBitmap);
                    }
                }

                if ((iconIdentifier != null) && (!iconIdentifier.isEmpty())) {
                    if (iconBitmap != null) {
                        // do not use increaseNotificationDecorationBrightness(),
                        // because icon will not be visible in AOD
                        /*int color = profile.increaseNotificationDecorationBrightness(appContext);
                        if (color != 0)
                            decoratorColor = color;
                        else*/ {
                            try {
                                Palette palette = Palette.from(iconBitmap).generate();
                                decoratorColor = palette.getDominantColor(ContextCompat.getColor(appContext, R.color.notification_color));
                            } catch (Exception ignored) {}
                        }
                    }
                }

            }
        }
        else {
            int iconSmallResource;
            if (notificationStatusBarStyle.equals("0"))
                iconSmallResource = R.drawable.ic_profile_default;
            else
                iconSmallResource = R.drawable.ic_profile_default_notify;
            notificationBuilder.setSmallIcon(iconSmallResource);
            if (notificationNotificationStyle.equals("0")) {
                try {
                    contentViewLarge.setImageViewResource(R.id.notification_activated_profile_icon, R.drawable.ic_empty);
                    if ((!notificationShowProfileIcon) && useDecorator)
                        contentViewLarge.setViewVisibility(R.id.notification_activated_profile_icon, View.GONE);
                    //if (profileIconExists) {
                    if (contentView != null) {
                        contentView.setImageViewResource(R.id.notification_activated_profile_icon, R.drawable.ic_empty);
                        if ((!notificationShowProfileIcon) && useDecorator)
                            contentView.setViewVisibility(R.id.notification_activated_profile_icon, View.GONE);
                    }
                    //}
                } catch (Exception e) {
                    PPApplicationStatic.recordException(e);
//                    PPApplicationStatic.logE("[PPP_NOTIFICATION] PPAppNotification._addProfileIconToNotification", Log.getStackTraceString(e));
                }
                notificationBuilder.setLargeIcon((Bitmap) null);
            }
            else {
                if (notificationShowProfileIcon) {
                    iconBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_empty);
                    // icon will be set in NotificationCompat.BigPictureStyle() for level 31+
                    if (Build.VERSION.SDK_INT < 31)
                        notificationBuilder.setLargeIcon(iconBitmap);
                }
            }
        }

        notificationIconData.decoratorColor = decoratorColor;
        notificationIconData.imageBitmap = iconBitmap;
        return notificationIconData;
    }

    static void clearOldNotification(Context context) {
        boolean clear = false;
        if (Build.MANUFACTURER.equals(PPApplication.MANUFACTURER_HMD_GLOBAL))
            // clear it for redraw icon in "Glance view" for "HMD Global" mobiles
            clear = true;
        if (PPApplication.deviceIsLG && (!Build.MODEL.contains(PPApplication.MODEL_NEXUS)) && (Build.VERSION.SDK_INT == 28))
            // clear it for redraw icon in "Glance view" for LG with Android 9
            clear = true;
        if (clear) {
            // next show will be with startForeground()
            //if (PhoneProfilesService.getInstance() != null) {
            clearNotification(context/*, true*/);
            GlobalUtils.sleep(100);
            //}
        }
    }

    static void forceDrawNotification(final Context appContext) {
        //boolean doNotShowNotification;
        //synchronized (PPApplication.applicationPreferencesMutex) {
        //    doNotShowNotification = PPApplication.doNotShowPPPAppNotification;
        //}

        //if (!doNotShowNotification) {
            //if (PhoneProfilesService.getInstance() != null) {

            clearOldNotification(appContext);

            //if (PhoneProfilesService.getInstance() != null) {
            PPApplicationStatic.logE("[SYNCHRONIZED] PPAppNotification.forceDrawNotification", "PPApplication.showPPPNotificationMutex");
            synchronized (PPApplication.showPPPNotificationMutex) {
                DataWrapper dataWrapper = new DataWrapper(appContext, false, 0, false, DataWrapper.IT_FOR_NOTIFICATION, 0, 0f);
//                PPApplicationStatic.logE("[PPP_NOTIFICATION] PPAppNotification.forceDrawNotification", "call of _showNotification");
                _showNotification(dataWrapper, false);
                dataWrapper.invalidateDataWrapper();
            }
            //}
            //}
        //}
    }

    static void forceDrawNotificationFromSettings(final Context appContext) {
        clearNotification(appContext);
        GlobalUtils.sleep(100);

        //if (PhoneProfilesService.getInstance() != null) {
        PPApplicationStatic.logE("[SYNCHRONIZED] PPAppNotification.forceDrawNotificationFromSettings", "PPApplication.showPPPNotificationMutex");
        synchronized (PPApplication.showPPPNotificationMutex) {
            DataWrapper dataWrapper = new DataWrapper(appContext, false, 0, false, DataWrapper.IT_FOR_NOTIFICATION, 0, 0f);
//                PPApplicationStatic.logE("[PPP_NOTIFICATION] PPAppNotification.forceDrawNotification", "call of _showNotification");
            _showNotification(dataWrapper, false);
            dataWrapper.invalidateDataWrapper();
        }
    }

    static void forceDrawNotificationWhenIsDeleted(final Context appContext) {
        //if (PhoneProfilesService.getInstance() != null) {
        PPApplicationStatic.logE("[SYNCHRONIZED] PPAppNotification.forceDrawNotificationWhenIsDeleted", "PPApplication.showPPPNotificationMutex");
        synchronized (PPApplication.showPPPNotificationMutex) {
            DataWrapper dataWrapper = new DataWrapper(appContext, false, 0, false, DataWrapper.IT_FOR_NOTIFICATION, 0, 0f);
//                PPApplicationStatic.logE("[PPP_NOTIFICATION] PPAppNotification.forceDrawNotification", "call of _showNotification");
            _showNotification(dataWrapper, false);
            dataWrapper.invalidateDataWrapper();
        }
    }

    static void drawNotification(boolean drawImmediatelly, Context context) {
//        PPApplicationStatic.logE("[EXECUTOR_CALL]  ***** PPAppNotification.drawNotification", "schedule");

        final Context appContext = context.getApplicationContext();
        //final ScheduledExecutorService worker = Executors.newSingleThreadScheduledExecutor();
        Runnable runnable = () -> {
//            long start = System.currentTimeMillis();
//            PPApplicationStatic.logE("[IN_EXECUTOR]  ***** PPAppNotification.drawNotification", "--------------- START");

            //Context appContext= appContextWeakRef.get();
            //if (appContext != null) {
            PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
            PowerManager.WakeLock wakeLock = null;
            try {
                if (powerManager != null) {
                    wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WakelockTags.WAKELOCK_TAG_PPPAppNotification_drawProfileNotification);
                    wakeLock.acquire(10 * 60 * 1000);
                }

//                PPApplicationStatic.logE("[PPP_NOTIFICATION] PPAppNotification.drawNotification", "call of forceDrawNotification");
//                Log.e("PPAppNotification.drawNotification", "xxx in runnable xxx");

                forceDrawNotification(appContext);

//                long finish = System.currentTimeMillis();
//                long timeElapsed = finish - start;
//                PPApplicationStatic.logE("[IN_EXECUTOR]  ***** PPAppNotification.drawNotification", "--------------- END - timeElapsed="+timeElapsed);
            } catch (Exception e) {
//                    PPApplicationStatic.logE("[IN_EXECUTOR] PPAppNotification.drawNotification", Log.getStackTraceString(e));
                PPApplicationStatic.recordException(e);
//                PPApplicationStatic.logE("[PPP_NOTIFICATION] PPAppNotification.drawNotification", Log.getStackTraceString(e));
            } finally {
                if ((wakeLock != null) && wakeLock.isHeld()) {
                    try {
                        wakeLock.release();
                    } catch (Exception ignored) {
                    }
                }
                //worker.shutdown();
            }
            //}
        };
        PPApplicationStatic.createDelayedAppNotificationExecutor();

//        Log.e("PPAppNotification.drawNotification", "xxx call of shedule xxx");
        if (PPApplication.scheduledFutureDelayedAppNotificationExecutor != null)
            PPApplication.scheduledFutureDelayedAppNotificationExecutor.cancel(false);
        if (drawImmediatelly)
            PPApplication.scheduledFutureDelayedAppNotificationExecutor =
                    PPApplication.delayedAppNotificationExecutor.schedule(runnable, 200, TimeUnit.MILLISECONDS);
        else {
            int delay = 5;
            if (PPApplication.isScreenOn)
                delay = 1;
            PPApplication.scheduledFutureDelayedAppNotificationExecutor =
                    PPApplication.delayedAppNotificationExecutor.schedule(runnable, delay, TimeUnit.SECONDS);
        }
    }

    static void showNotification(Context context, boolean drawEmpty, boolean drawActivatedProfle, boolean drawImmediatelly) {
        //if (DebugVersion.enabled)
        //    isServiceRunningInForeground(appContext, PhoneProfilesService.class);

        //if (!runningInForeground) {
        if (drawEmpty) {
            //if (!isServiceRunningInForeground(appContext, PhoneProfilesService.class)) {
            DataWrapper dataWrapper = new DataWrapper(context, false, 0, false, DataWrapper.IT_FOR_NOTIFICATION, 0, 0f);
//            PPApplicationStatic.logE("[PPP_NOTIFICATION] PPAppNotification.showNotification", "call of _showNotification");
            _showNotification(/*null,*/ dataWrapper, true/*, true*/);
            dataWrapper.invalidateDataWrapper();
            //return; // do not return, dusplay activated profile immediatelly
        }

        //if (DebugVersion.enabled)
        //    isServiceRunningInForeground(appContext, PhoneProfilesService.class);

        //synchronized (PPApplication.applicationPreferencesMutex) {
        //    if (PPApplication.doNotShowPPPAppNotification)
        //        return;
        //}

        if (!drawActivatedProfle)
            return;

/*        int delay;
        if (drawImmediatelly)
            delay = 200;
        else
            delay = 1000;*/
//        PPApplicationStatic.logE("[PPP_NOTIFICATION] PPAppNotification.showNotification", "call of drawNotification");
        drawNotification(drawImmediatelly, context);

        //PPApplication.lastRefreshOfPPPAppNotification = SystemClock.elapsedRealtime();
    }

    static void clearNotification(Context context/*, boolean onlyEmpty*/)
    {
        /*if (onlyEmpty) {
            final Context appContext = getApplicationContext();
            DataWrapper dataWrapper = new DataWrapper(appContext, false, 0, false);
            _showNotification(null, false, dataWrapper, true);
            dataWrapper.invalidateDataWrapper();
        }
        else {*/
        try {
            //startForegroundNotification = true;
            //isInForeground = false;
            if (PhoneProfilesService.getInstance() != null)
                PhoneProfilesService.getInstance().stopForeground(true);
            //PPApplicationStatic.cancelWork(ShowProfileNotificationWorker.WORK_TAG, true);
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                try {
                    PPApplicationStatic.logE("[SYNCHRONIZED] PPAppNotification.clearNotification", "(1) PPApplication.showPPPNotificationMutex");
                    synchronized (PPApplication.showPPPNotificationMutex) {
                        notificationManager.cancel(PPApplication.PROFILE_NOTIFICATION_ID);
                    }
                } catch (Exception ignored) {}
                try {
                    PPApplicationStatic.logE("[SYNCHRONIZED] PPAppNotification.clearNotification", "(2) PPApplication.showPPPNotificationMutex");
                    synchronized (PPApplication.showPPPNotificationMutex) {
                        notificationManager.cancel(PPApplication.PROFILE_NOTIFICATION_NATIVE_ID);
                    }
                } catch (Exception ignored) {}
            }
        } catch (Exception e) {
            //Log.e("PPAppNotification.clearNotification", Log.getStackTraceString(e));
            PPApplicationStatic.recordException(e);
//            PPApplicationStatic.logE("[PPP_NOTIFICATION] PPAppNotification.clearNotification", Log.getStackTraceString(e));
        }
        //runningInForeground = false;
        //}
    }

}
