package sk.henrichg.phoneprofilesplus;

import static android.view.View.VISIBLE;

import android.app.ActivityManager;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.PowerManager;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ProfileListNotification {

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

    static final String ACTION_REFRESH_PROFILELISTNOTIFICATION = PPApplication.PACKAGE_NAME + ".ACTION_REFRESH_PROFILELISTNOTIFICATION";
    private static final String ACTION_LEFT_ARROW_CLICK = PPApplication.PACKAGE_NAME + ".ACTION_LEFT_ARROW_CLICK";
    private static final String ACTION_RIGHT_ARROW_CLICK = PPApplication.PACKAGE_NAME + ".ACTION_RIGHT_ARROW_CLICK";
    private static final int PROFILE_ID_ACTIVATE_PROFILE_ID = 1000;


    static private void _showNotification(final Context context, boolean forFirstStart)
    {
//        PPApplication.logE("[PPP_NOTIFICATION] ProfileListNotification._showNotification", "start");

        final Context appContext = context.getApplicationContext();
        LocaleHelper.setApplicationLocale(appContext);

        ActivityManager.RunningServiceInfo serviceInfo = GlobalUtils.getServiceInfo(appContext, PhoneProfilesService.class);
        if (serviceInfo == null) {
            // service is not running
            return;
        }

//        PPApplication.logE("[PPP_NOTIFICATION] ProfileListNotification._showNotification", "call of createProfileListNotificationChannel()");
        PPApplication.createProfileListNotificationChannel(appContext);

        boolean notificationProfileListShowInStatusBar;
        boolean notificationProfileListHideInLockscreen;

        String notificationProfileListIconLightness;
        String notificationProfileListIconColor;
        boolean notificationProfileListCustomIconLightness;

        String notificationProfileListArrowsMarkLightness;

        String notificationProfileListBackgroundColor;
        int notificationProfileListBackgroundCustomColor;

        int notificationProfileListNumberOfProfilesPerPage;

        synchronized (PPApplication.applicationPreferencesMutex) {
            notificationProfileListShowInStatusBar = ApplicationPreferences.notificationProfileListShowInStatusBar;
            notificationProfileListHideInLockscreen = ApplicationPreferences.notificationProfileListHideInLockscreen;

            notificationProfileListIconLightness = ApplicationPreferences.notificationProfileListIconLightness;
            notificationProfileListIconColor = ApplicationPreferences.notificationProfileListIconColor;
            notificationProfileListCustomIconLightness = ApplicationPreferences.notificationProfileListCustomIconLightness;

            notificationProfileListArrowsMarkLightness = ApplicationPreferences.notificationProfileListArrowsMarkLightness;
            notificationProfileListNumberOfProfilesPerPage = ApplicationPreferences.notificationProfileListNumberOfProfilesPerPage;

            notificationProfileListBackgroundColor = ApplicationPreferences.notificationProfileListBackgroundColor;
            notificationProfileListBackgroundCustomColor = ApplicationPreferences.notificationProfileListBackgroundCustomColor;
        }

        NotificationCompat.Builder notificationBuilder;

        RemoteViews contentView = null;

        boolean useNightColor = GlobalGUIRoutines.isNightModeEnabled(appContext);

        contentView = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.ppp_notification_profile_list);

        int monochromeValue = 0xFF;
        switch (notificationProfileListIconLightness) {
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
                notificationProfileListIconColor.equals("1"), monochromeValue,
                notificationProfileListCustomIconLightness,
                DataWrapper.IT_FOR_NOTIFICATION, 0, 0);

        List<Profile> newProfileList = dataWrapper.getNewProfileList(true, false);

        // add activated profile, when has not enabled _showInActivator
        Profile activatedProfile = dataWrapper.getActivatedProfile(newProfileList);
        if ((activatedProfile != null) && (!activatedProfile._showInActivator))
        {
            activatedProfile._showInActivator = true;
            activatedProfile._porder = -1;
        }
        newProfileList.sort(new ProfileComparator());

        Profile restartEvents = null;
        if (Event.getGlobalEventsRunning()) {
            //restartEvents = DataWrapper.getNonInitializedProfile(context.getString(R.string.menu_restart_events), "ic_profile_restart_events|1|0|0", 0);
            restartEvents = DataWrapperStatic.getNonInitializedProfile(appContext.getString(R.string.menu_restart_events),
                    "ic_profile_restart_events|1|1|"+ApplicationPreferences.applicationRestartEventsIconColor, 0);
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

        int markRedColor = 0xFF;
        switch (notificationProfileListArrowsMarkLightness) {
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

        /*
        PendingIntent pIntent;
        if (Build.VERSION.SDK_INT < 31)
            pIntent = PendingIntent.getBroadcast(appContext, requestCode, launcherIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        else
            pIntent = PendingIntent.getActivity(appContext, requestCode, launcherIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        */

        // ----- create notificationBuilders
        if (Build.VERSION.SDK_INT >= 26) {
            notificationBuilder = new NotificationCompat.Builder(appContext, PPApplication.PROFILE_LIST_NOTIFICATION_CHANNEL);
        }
        else {
            notificationBuilder = new NotificationCompat.Builder(appContext, PPApplication.PROFILE_LIST_NOTIFICATION_CHANNEL);
            if (notificationProfileListShowInStatusBar) {
                KeyguardManager myKM = (KeyguardManager) appContext.getSystemService(Context.KEYGUARD_SERVICE);
                if (myKM != null) {
                    boolean screenUnlocked = !myKM.isKeyguardLocked();
                    if (notificationProfileListHideInLockscreen && (!screenUnlocked))
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
        }

        //notificationBuilder.setContentIntent(pIntent);

        // Android 12:
        // The service provides a use case related to phone calls, navigation, or media playback,
        // as defined in the notification's category attribute.
        // Use CATEGORY_NAVIGATION to show notification in DND
        notificationBuilder.setCategory(NotificationCompat.CATEGORY_NAVIGATION);

        notificationBuilder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);

        // this disable timestamp in decorator
        notificationBuilder.setShowWhen(false);

        switch (notificationProfileListBackgroundColor) {
            case "3":
                //if (!notificationNightMode || (useNightColor == 1)) {
                int color = ContextCompat.getColor(appContext, R.color.notificationBlackBackgroundColor);
                contentView.setInt(R.id.notification_profile_list_root, "setBackgroundColor", color);
                break;
            case "1":
                //if (!notificationNightMode || (useNightColor == 1)) {
                color = ContextCompat.getColor(appContext, R.color.notificationDarkBackgroundColor);
                contentView.setInt(R.id.notification_profile_list_root, "setBackgroundColor", color);
                break;
            case "5":
                //if (!notificationNightMode || (useNightColor == 1)) {
                contentView.setInt(R.id.notification_profile_list_root, "setBackgroundColor", notificationProfileListBackgroundCustomColor);
                break;
            default:
                //int color = getResources().getColor(R.color.notificationBackground);
                contentView.setInt(R.id.notification_profile_list_root, "setBackgroundColor", Color.TRANSPARENT);
                break;
        }

        int profileIdx = 0;
        int displayedProfileIdx = 0;
        int firstProfileIdxInPage = notificationProfileListNumberOfProfilesPerPage * displayedPage;
        //Log.e("ProfileListNotification._showNotification", "displayedPage="+displayedPage);
        //Log.e("ProfileListNotification._showNotification", "firstProfileInPage="+firstProfileInPage);
        for (Profile profile : dataWrapper.profileList) {
            if (profile._showInActivator) {
                if (profileIdx >= firstProfileIdxInPage) {
                    setProfileIcon(profile,
                            profileIconId[displayedProfileIdx], profileMarkId[displayedProfileIdx], profileRootId[displayedProfileIdx],
                            notificationProfileListIconColor,
                            monochromeValue,
                            notificationProfileListBackgroundColor,
                            notificationProfileListBackgroundCustomColor,
                            markRedColor, markGreenColor, markBlueColor,
                            contentView, appContext);
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
            contentView.setViewVisibility(profileRootId[i], View.GONE);
            //remoteViews.setViewVisibility(profileIconId[i], View.INVISIBLE);
            //remoteViews.setViewVisibility(profileMarkId[i], View.INVISIBLE);
            contentView.setOnClickPendingIntent(profileRootId[i], null);
        }

        if (!((Build.VERSION.SDK_INT >= 31) && notificationProfileListBackgroundColor.equals("0"))) {
            //if (Event.getGlobalEventsRunning() && PPApplication.getApplicationStarted(true)) {
            // left arrow
            Bitmap bitmap = BitmapManipulator.getBitmapFromResource(R.drawable.ic_widget_profile_list_scroll_left, true, appContext);
            bitmap = BitmapManipulator.monochromeBitmap(bitmap, arrowsLightness);
            contentView.setImageViewBitmap(R.id.notification_profile_list_scroll_left_arrow, bitmap);
            // right arrow
            bitmap = BitmapManipulator.getBitmapFromResource(R.drawable.ic_widget_profile_list_scroll_right, true, appContext);
            bitmap = BitmapManipulator.monochromeBitmap(bitmap, arrowsLightness);
            contentView.setImageViewBitmap(R.id.notification_profile_list_scroll_right_arrow, bitmap);
            //}
        } else {
            // good, color of this is as in notification ;-)
            // but must be removed android:tint in layout
            int color = GlobalGUIRoutines.getDynamicColor(R.attr.colorSecondary, appContext);
            if (color != 0) {
                // left arrow
                Bitmap bitmap = BitmapManipulator.getBitmapFromResource(R.drawable.ic_widget_profile_list_scroll_left, true, appContext);
                bitmap = BitmapManipulator.recolorBitmap(bitmap, color);
                contentView.setImageViewBitmap(R.id.notification_profile_list_scroll_left_arrow, bitmap);
                // right arrow
                bitmap = BitmapManipulator.getBitmapFromResource(R.drawable.ic_widget_profile_list_scroll_right, true, appContext);
                bitmap = BitmapManipulator.recolorBitmap(bitmap, color);
                contentView.setImageViewBitmap(R.id.notification_profile_list_scroll_right_arrow, bitmap);
            }
        }
        //if (Event.getGlobalEventsRunning() && PPApplication.getApplicationStarted(true)) {
        // left arrow
        if (displayedPage > 0)
            contentView.setViewVisibility(R.id.notification_profile_list_scroll_left_arrow, VISIBLE);
        else
            contentView.setViewVisibility(R.id.notification_profile_list_scroll_left_arrow, View.GONE);
        Intent intentLeftArrow = new Intent(appContext, ProfileListNotification.class);
        intentLeftArrow.setAction(ACTION_LEFT_ARROW_CLICK);
        PendingIntent pIntentLeftArrow = PendingIntent.getBroadcast(appContext, 2, intentLeftArrow, PendingIntent.FLAG_UPDATE_CURRENT);
        contentView.setOnClickPendingIntent(R.id.notification_profile_list_scroll_left_arrow, pIntentLeftArrow);
        // right arrow
        if ((displayedPage < profileCount / notificationProfileListNumberOfProfilesPerPage) &&
                (profileCount > notificationProfileListNumberOfProfilesPerPage))
            contentView.setViewVisibility(R.id.notification_profile_list_scroll_right_arrow, VISIBLE);
        else
            contentView.setViewVisibility(R.id.notification_profile_list_scroll_right_arrow, View.GONE);
        Intent intentRightArrow = new Intent(appContext, ProfileListNotification.class);
        intentRightArrow.setAction(ACTION_RIGHT_ARROW_CLICK);
        PendingIntent pIntentRightArrow = PendingIntent.getBroadcast(appContext, 3, intentRightArrow, PendingIntent.FLAG_UPDATE_CURRENT);
        contentView.setOnClickPendingIntent(R.id.notification_profile_list_scroll_right_arrow, pIntentRightArrow);

        notificationBuilder.setStyle(null);

        notificationBuilder.setCustomBigContentView(contentView);

        notificationBuilder.setOnlyAlertOnce(true);

        notificationBuilder.setGroup(PPApplication.PROFILE_NOTIFICATION_GROUP);

        /*
        if (Build.VERSION.SDK_INT >= 33) {
            // required, because in API 33+ foreground serbice notification is dismissable
            notificationBuilder.setOngoing(true);
        }
        */

        Notification profileListNotification;
        try {
            profileListNotification = notificationBuilder.build();
        } catch (Exception e) {
//            PPApplication.logE("[PPP_NOTIFICATION] ProfileListNotification._showNotification", Log.getStackTraceString(e));
            profileListNotification = null;
        }

        if (profileListNotification != null) {

            if (Build.VERSION.SDK_INT < 26) {
                profileListNotification.flags &= ~Notification.FLAG_SHOW_LIGHTS;
                profileListNotification.ledOnMS = 0;
                profileListNotification.ledOffMS = 0;
                profileListNotification.sound = null;
                profileListNotification.vibrate = null;
                profileListNotification.defaults &= ~NotificationCompat.DEFAULT_SOUND;
                profileListNotification.defaults &= ~NotificationCompat.DEFAULT_VIBRATE;
            }

            // do not use Notification.FLAG_ONGOING_EVENT,
            // with this flag, is not possible to colapse this notification
            profileListNotification.flags |= Notification.FLAG_NO_CLEAR; //| Notification.FLAG_ONGOING_EVENT;

            NotificationManagerCompat mNotificationManager = NotificationManagerCompat.from(appContext);
            try {
                mNotificationManager.notify(
                        PPApplication.PROFILE_LIST_NOTIFICATION_TAG,
                        PPApplication.PROFILE_LIST_NOTIFICATION_ID, notificationBuilder.build());
            } catch (SecurityException en) {
                Log.e("ProfileListNotification._showNotification", Log.getStackTraceString(en));
            } catch (Exception e) {
                //Log.e("ProfileListNotification._showNotification", Log.getStackTraceString(e));
                PPApplication.recordException(e);
            }

        }

        dataWrapper.invalidateDataWrapper();
    }

    static void clearOldNotification(Context context) {
        boolean clear = false;
        if (Build.MANUFACTURER.equals("HMD Global"))
            // clear it for redraw icon in "Glance view" for "HMD Global" mobiles
            clear = true;
        if (PPApplication.deviceIsLG && (!Build.MODEL.contains("Nexus")) && (Build.VERSION.SDK_INT == 28))
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
            synchronized (PPApplication.showPPPNotificationMutex) {
                //DataWrapper dataWrapper = new DataWrapper(appContext, false, 0, false, DataWrapper.IT_FOR_NOTIFICATION, 0, 0f);
//                PPApplication.logE("[PPP_NOTIFICATION] ProfileListNotification.forceDrawNotification", "call of _showNotification");
                _showNotification(appContext, false);
                //dataWrapper.invalidateDataWrapper();
            }
            //}
            //}
        //}
    }

    static void drawNotification(boolean drawImmediatelly, Context context) {
//        PPApplication.logE("[EXECUTOR_CALL]  ***** ProfileListNotification.drawNotification", "schedule");

        final Context appContext = context.getApplicationContext();
        //final ScheduledExecutorService worker = Executors.newSingleThreadScheduledExecutor();
        Runnable runnable = () -> {
//            long start = System.currentTimeMillis();
//            PPApplication.logE("[IN_EXECUTOR]  ***** ProfileListNotification.drawNotification", "--------------- START");

            //Context appContext= appContextWeakRef.get();
            //if (appContext != null) {
            PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
            PowerManager.WakeLock wakeLock = null;
            try {
                if (powerManager != null) {
                    wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":ProfileListNotification_drawNotification");
                    wakeLock.acquire(10 * 60 * 1000);
                }

//                PPApplication.logE("[PPP_NOTIFICATION] ProfileListNotification.drawNotification", "call of forceDrawNotification");
                forceDrawNotification(appContext);

//                long finish = System.currentTimeMillis();
//                long timeElapsed = finish - start;
//                PPApplication.logE("[IN_EXECUTOR]  ***** ProfileListNotification.drawNotification", "--------------- END - timeElapsed="+timeElapsed);
            } catch (Exception e) {
//                    PPApplication.logE("[IN_EXECUTOR] ProfileListNotification.drawNotification", Log.getStackTraceString(e));
                PPApplication.recordException(e);
//                PPApplication.logE("[PPP_NOTIFICATION] ProfileListNotification.drawNotification", Log.getStackTraceString(e));
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
        PPApplication.createDelayedShowNotificationExecutor();

//        PPApplication.delayedShowNotificationExecutor.shutdownNow(); // shutdown already scheduled
//        try {
//            PPApplication.delayedShowNotificationExecutor.awaitTermination(1, TimeUnit.SECONDS); // shutdown already scheduled
//        } catch (Exception ignored) {};

        if (drawImmediatelly)
            PPApplication.delayedShowNotificationExecutor.schedule(runnable, 200, TimeUnit.MILLISECONDS);
        else
            PPApplication.delayedShowNotificationExecutor.schedule(runnable, 1, TimeUnit.SECONDS);
    }

    static void showNotification(Context context, boolean drawEmpty, boolean drawActivatedProfle, boolean drawImmediatelly) {
        //if (Build.VERSION.SDK_INT >= 26) {
        //if (DebugVersion.enabled)
        //    isServiceRunningInForeground(appContext, PhoneProfilesService.class);

        //if (!runningInForeground) {
        if (drawEmpty) {
            //if (!isServiceRunningInForeground(appContext, PhoneProfilesService.class)) {
            //DataWrapper dataWrapper = new DataWrapper(context, false, 0, false, DataWrapper.IT_FOR_NOTIFICATION, 0, 0f);
//            PPApplication.logE("[PPP_NOTIFICATION] ProfileListNotification.showNotification", "call of _showNotification");
            _showNotification(context, true);
            //dataWrapper.invalidateDataWrapper();
            //return; // do not return, dusplay activated profile immediatelly
        }
        //}

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
//        PPApplication.logE("[PPP_NOTIFICATION] ProfileListNotification.showNotification", "call of drawNotification");
        drawNotification(drawImmediatelly, context);

        //PPApplication.lastRefreshOfPPPAppNotification = SystemClock.elapsedRealtime();
    }

    static void clearNotification(Context context)
    {
        try {
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                try {
                    synchronized (PPApplication.showPPPNotificationMutex) {
                        notificationManager.cancel(PPApplication.PROFILE_LIST_NOTIFICATION_ID);
                    }
                } catch (Exception ignored) {}
            }
        } catch (Exception e) {
            //Log.e("ProfileListNotification.clearNotification", Log.getStackTraceString(e));
            PPApplication.recordException(e);
//            PPApplication.logE("[PPP_NOTIFICATION] ProfileListNotification.clearNotification", Log.getStackTraceString(e));
        }
        //runningInForeground = false;
        //}
    }


    private static void setProfileIcon(Profile profile,
                                       int imageViewId, int markViewId, int rootId,
                                       String notificationProfileListIconColor,
                                       int notificationProfileListIconMonochromeValue,
                                       String notificationProfileListBackgroundColor,
                                       int notificationProfileListBackgroundCustomColor,
                                       int markRedColor, int markGreenColor, int markBlueColor,

                                       RemoteViews contentView,
                                       Context appContext) {

        contentView.setViewVisibility(rootId, VISIBLE);

        boolean isIconResourceID = profile.getIsIconResourceID();
        Bitmap  iconBitmap = profile._iconBitmap;
        String iconIdentifier = profile.getIconIdentifier();

        if (isIconResourceID) {
            int iconSmallResource;
            if (iconBitmap != null) {
                /*
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
                        PPApplication.recordException(e);
//                            PPApplication.logE("[PPP_NOTIFICATION] PPPAppNotification._addProfileIconToNotification", Log.getStackTraceString(e));
                    }
                    notificationBuilder.setSmallIcon(iconSmallResource);
                }
                */

                if (notificationProfileListIconColor.equals("0")) {
                    if ((!notificationProfileListBackgroundColor.equals("5")) ||
                            (ColorUtils.calculateLuminance(notificationProfileListBackgroundCustomColor) < 0.23)) {
                        //if (profile != null) {
                            Bitmap bitmap = profile.increaseProfileIconBrightnessForContext(appContext, iconBitmap);
                            if (bitmap != null)
                                iconBitmap = bitmap;
                        //}
                    }
                }

                try {
                    contentView.setImageViewBitmap(imageViewId, iconBitmap);
                } catch (Exception e) {
                    PPApplication.recordException(e);
//                            PPApplication.logE("[PPP_NOTIFICATION] ProfileListNotification.setProfileIcon", Log.getStackTraceString(e));
                }
            } else {
                /*
                iconSmallResource = R.drawable.ic_profile_default_notify_color;
                try {
                    if ((iconIdentifier != null) && (!iconIdentifier.isEmpty())) {
                        Object idx = Profile.profileIconNotifyColorId.get(iconIdentifier);
                        if (idx != null)
                            iconSmallResource = (int) idx;
                    }
                } catch (Exception e) {
                    PPApplication.recordException(e);
//                            PPApplication.logE("[PPP_NOTIFICATION] PPPAppNotification._addProfileIconToNotification", Log.getStackTraceString(e));
                }
                notificationBuilder.setSmallIcon(iconSmallResource);
                */

                int iconLargeResource = ProfileStatic.getIconResource(iconIdentifier);
                iconBitmap = BitmapManipulator.getBitmapFromResource(iconLargeResource, true, appContext);
                if (iconBitmap != null) {
                    if (notificationProfileListIconColor.equals("1"))
                        iconBitmap = BitmapManipulator.monochromeBitmap(iconBitmap, notificationProfileListIconMonochromeValue);
                    else {
                        //if (profile != null) {
                            Bitmap bitmap = profile.increaseProfileIconBrightnessForContext(appContext, iconBitmap);
                            if (bitmap != null)
                                iconBitmap = bitmap;
                        //}
                    }
                }
                if (iconBitmap == null) {
                    iconBitmap = BitmapManipulator.getBitmapFromResource(R.drawable.ic_profile_default, true, appContext);
                }

                try {
                    contentView.setImageViewBitmap(imageViewId, iconBitmap);
                } catch (Exception e) {
                    PPApplication.recordException(e);
//                            PPApplication.logE("[PPP_NOTIFICATION] PPPAppNotification._addProfileIconToNotification", Log.getStackTraceString(e));
                }
            }


            // do not use increaseNotificationDecorationBrightness(),
            // because icon will not be visible in AOD
            //    int color = 0;
            //    if (profile != null)
            //        color = profile.increaseNotificationDecorationBrightness(appContext);
            //    if (color != 0)
            //        decoratorColor = color;
            //    else
            /*
            {
                //if ((profile != null) && (profile.getUseCustomColorForIcon()))
                if (profile.getUseCustomColorForIcon())
                    decoratorColor = profile.getIconCustomColor();
                else {
                    if ((iconIdentifier != null) && (!iconIdentifier.isEmpty())) {
                        decoratorColor = ProfileStatic.getIconDefaultColor(iconIdentifier);
                    }
                }
            }
            */

        } else {

            /*
            if (iconBitmap != null) {
                if (notificationStatusBarStyle.equals("2")) {
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
            */

            //if (profile != null) {
                Bitmap bitmap = profile.increaseProfileIconBrightnessForContext(appContext, iconBitmap);
                if (bitmap != null)
                    iconBitmap = bitmap;
            //}
            if (iconBitmap == null) {
                iconBitmap = BitmapManipulator.getBitmapFromResource(R.drawable.ic_profile_default, true, appContext);
            }

            try {
                if (iconBitmap != null)
                    contentView.setImageViewBitmap(imageViewId, iconBitmap);
                else {
                    if (notificationProfileListIconColor.equals("0"))
                        contentView.setImageViewResource(imageViewId, R.drawable.ic_profile_default);
                    else {
                        iconBitmap = BitmapManipulator.getBitmapFromResource(R.drawable.ic_profile_default, true, appContext);
                        iconBitmap = BitmapManipulator.monochromeBitmap(iconBitmap, notificationProfileListIconMonochromeValue);
                        contentView.setImageViewBitmap(imageViewId, iconBitmap);
                    }
                }
                //if (contentView != null) {
                    if (iconBitmap != null)
                        contentView.setImageViewBitmap(imageViewId, iconBitmap);
                    else {
                        if (notificationProfileListIconColor.equals("0"))
                            contentView.setImageViewResource(imageViewId, R.drawable.ic_profile_default);
                        else {
                            iconBitmap = BitmapManipulator.getBitmapFromResource(R.drawable.ic_profile_default, true, appContext);
                            iconBitmap = BitmapManipulator.monochromeBitmap(iconBitmap, notificationProfileListIconMonochromeValue);
                            contentView.setImageViewBitmap(imageViewId, iconBitmap);
                        }
                    }
                //}
            } catch (Exception e) {
                PPApplication.recordException(e);
//                        PPApplication.logE("[PPP_NOTIFICATION] PPPAppNotification._addProfileIconToNotification", Log.getStackTraceString(e));
            }

            /*
            if ((iconIdentifier != null) && (!iconIdentifier.isEmpty())) {
                if (iconBitmap != null) {
                    // do not use increaseNotificationDecorationBrightness(),
                    // because icon will not be visible in AOD
                    //    int color = profile.increaseNotificationDecorationBrightness(appContext);
                    //    if (color != 0)
                    //        decoratorColor = color;
                    //    else
                    {
                        try {
                            Palette palette = Palette.from(iconBitmap).generate();
                            decoratorColor = palette.getDominantColor(ContextCompat.getColor(appContext, R.color.notification_color));
                        } catch (Exception ignored) {}
                    }
                }
            }
            */

        }

        if (profile._checked) {
            if (!((Build.VERSION.SDK_INT >= 31) && notificationProfileListBackgroundColor.equals("0")))
                contentView.setInt(markViewId, "setBackgroundColor", Color.argb(0xFF, markRedColor, markGreenColor, markBlueColor));
            else {
                int color = GlobalGUIRoutines.getDynamicColor(R.attr.colorSecondary, appContext);
                if (color != 0) {
                    //Bitmap bitmap = BitmapManipulator.getBitmapFromResource(R.drawable.ic_black, true, context);
                    //bitmap = BitmapManipulator.recolorBitmap(bitmap, color);
                    //contentView.setImageViewBitmap(markViewId, bitmap);
                    contentView.setInt(markViewId, "setBackgroundColor", color);
                }
            }
            contentView.setViewVisibility(markViewId, View.VISIBLE);
        } else {
            contentView.setViewVisibility(markViewId, View.INVISIBLE);
        }

        Intent clickIntent = new Intent(appContext, BackgroundActivateProfileActivity.class);
        if (Event.getGlobalEventsRunning() && (profile._id == Profile.RESTART_EVENTS_PROFILE_ID))
            clickIntent.putExtra(PPApplication.EXTRA_PROFILE_ID, Profile.RESTART_EVENTS_PROFILE_ID);
        else
            clickIntent.putExtra(PPApplication.EXTRA_PROFILE_ID, profile._id);
        clickIntent.putExtra(PPApplication.EXTRA_STARTUP_SOURCE, PPApplication.STARTUP_SOURCE_WIDGET);
        PendingIntent clickPI=PendingIntent.getActivity(appContext, PROFILE_ID_ACTIVATE_PROFILE_ID + (int) profile._id, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        contentView.setOnClickPendingIntent(rootId, clickPI);
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
