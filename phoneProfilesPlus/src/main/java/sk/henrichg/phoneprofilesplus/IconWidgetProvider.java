package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.View;
import android.widget.RemoteViews;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class IconWidgetProvider extends AppWidgetProvider {

    static final String ACTION_REFRESH_ICONWIDGET = PPApplication.PACKAGE_NAME + ".ACTION_REFRESH_ICONWIDGET";

    public void onUpdate(Context context, final AppWidgetManager appWidgetManager, final int[] appWidgetIds)
    {
        //super.onUpdate(context, appWidgetManager, appWidgetIds);
//        PPApplication.logE("[IN_LISTENER] IconWidgetProvider.onUpdate", "xxx");
        if (appWidgetIds.length > 0) {
            final Context appContext = context;
            PPApplication.startHandlerThreadWidget();
            final Handler __handler = new Handler(PPApplication.handlerThreadWidget.getLooper());
            //__handler.post(new PPHandlerThreadRunnable(context, appWidgetManager) {
            __handler.post(() -> {
//                    PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThreadWidget", "START run - from=IconWidgetProvider.onUpdate");

                //Context appContext= appContextWeakRef.get();
                //AppWidgetManager appWidgetManager = appWidgetManagerWeakRef.get();

                //if ((appContext != null) && (appWidgetManager != null)) {
                    _onUpdate(appContext, appWidgetManager, appWidgetIds);
                //}
            });
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
        boolean applicationWidgetChangeColorsByNightMode;
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
            applicationWidgetChangeColorsByNightMode = ApplicationPreferences.applicationWidgetChangeColorsByNightMode;

            if (Build.VERSION.SDK_INT >= 31) {
                if (PPApplication.isPixelLauncherDefault(context)) {
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
                    applicationWidgetIconRoundedCorners = ApplicationPreferences.applicationWidgetIconRoundedCorners;
                    applicationWidgetIconRoundedCornersRadius = ApplicationPreferences.applicationWidgetIconRoundedCornersRadius;
                    //applicationWidgetChangeColorsByNightMode = ApplicationPreferences.applicationWidgetChangeColorsByNightMode;
                }
                if (/*PPApplication.isPixelLauncherDefault(context) ||*/
                        applicationWidgetChangeColorsByNightMode) {
                    int nightModeFlags =
                            context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
                    switch (nightModeFlags) {
                        case Configuration.UI_MODE_NIGHT_YES:
                            //applicationWidgetIconBackground = "100"; // fully opaque
                            applicationWidgetIconBackgroundType = true; // background type = color
                            applicationWidgetIconBackgroundColor = String.valueOf(0x2f2f2f); // color of background
                            //applicationWidgetIconShowBorder = false; // do not show border
                            applicationWidgetIconLightnessBorder = "0";
                            applicationWidgetIconLightnessT = "100"; // lightness of text = white
                            applicationWidgetIconColor = "0"; // icon type = colorful
                            break;
                        case Configuration.UI_MODE_NIGHT_NO:
                        case Configuration.UI_MODE_NIGHT_UNDEFINED:
                            //applicationWidgetIconBackground = "100"; // fully opaque
                            applicationWidgetIconBackgroundType = true; // background type = color
                            applicationWidgetIconBackgroundColor = String.valueOf(0xf0f0f0); // color of background
                            //applicationWidgetIconShowBorder = false; // do not show border
                            applicationWidgetIconLightnessBorder = "100";
                            applicationWidgetIconLightnessT = "0"; // lightness of text = black
                            applicationWidgetIconColor = "0"; // icon type = colorful
                            break;
                    }
                }
            }
        }

        //PPApplication.logE("IconWidgetProvider.onUpdate", "ApplicationPreferences.applicationWidgetIconLightness="+ApplicationPreferences.applicationWidgetIconLightness);
        int monochromeValue = 0xFF;
        switch (applicationWidgetIconLightness) {
            case "0":
                monochromeValue = 0x00;
                break;
            case "12":
                monochromeValue = 0x20;
                break;
            case "25":
                monochromeValue = 0x40;
                break;
            case "37":
                monochromeValue = 0x60;
                break;
            case "50":
                monochromeValue = 0x80;
                break;
            case "62":
                monochromeValue = 0xA0;
                break;
            case "75":
                monochromeValue = 0xC0;
                break;
            case "87":
                monochromeValue = 0xE0;
                break;
            case "100":
                monochromeValue = 0xFF;
                break;
        }

        //DataWrapper dataWrapper = _dataWrapper;
        //Profile profile = _profile;
        //if (dataWrapper == null) {
            //PPApplication.logE("IconWidgetProvider.onUpdate", "applicationWidgetIconColor="+applicationWidgetIconColor);
            //PPApplication.logE("IconWidgetProvider.onUpdate", "applicationWidgetIconCustomIconLightness="+applicationWidgetIconCustomIconLightness);
        DataWrapper dataWrapper = new DataWrapper(context.getApplicationContext(),
                    applicationWidgetIconColor.equals("1"),
                    monochromeValue,
                    applicationWidgetIconCustomIconLightness,
                    DataWrapper.IT_FOR_WIDGET, 0f);

        Profile profile;
        //boolean fullyStarted = PPApplication.applicationFullyStarted;
        //if ((!fullyStarted) /*|| applicationPackageReplaced*/)
        //    profile = null;
        //else
            profile = dataWrapper.getActivatedProfile(true, false);

        //try {
            //PPApplication.logE("IconWidgetProvider.onUpdate", "refreshWidget="+refreshWidget);
            // set background
            //PPApplication.logE("IconWidgetProvider.onUpdate", "applicationWidgetIconBackgroundType="+applicationWidgetIconBackgroundType);
            //PPApplication.logE("IconWidgetProvider.onUpdate", "applicationWidgetIconBackgroundColor="+applicationWidgetIconBackgroundColor);
            //PPApplication.logE("IconWidgetProvider.onUpdate", "applicationWidgetIconLightnessB="+applicationWidgetIconLightnessB);
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
                    case "0":
                        redBackground = 0x00;
                        break;
                    case "12":
                        redBackground = 0x20;
                        break;
                    case "25":
                        redBackground = 0x40;
                        break;
                    case "37":
                        redBackground = 0x60;
                        break;
                    case "50":
                        redBackground = 0x80;
                        break;
                    case "62":
                        redBackground = 0xA0;
                        break;
                    case "75":
                        redBackground = 0xC0;
                        break;
                    case "87":
                        redBackground = 0xE0;
                        break;
                    case "100":
                        redBackground = 0xFF;
                        break;
                }
                greenBackground = redBackground;
                blueBackground = redBackground;
            }

            //PPApplication.logE("IconWidgetProvider.onUpdate", "applicationWidgetIconBackground="+ApplicationPreferences_applicationWidgetIconBackground);
            int alphaBackground = 0x40;
            switch (applicationWidgetIconBackground) {
                case "0":
                    alphaBackground = 0x00;
                    break;
                case "12":
                    alphaBackground = 0x20;
                    break;
                case "25":
                    alphaBackground = 0x40;
                    break;
                case "37":
                    alphaBackground = 0x60;
                    break;
                case "50":
                    alphaBackground = 0x80;
                    break;
                case "62":
                    alphaBackground = 0xA0;
                    break;
                case "75":
                    alphaBackground = 0xC0;
                    break;
                case "87":
                    alphaBackground = 0xE0;
                    break;
                case "100":
                    alphaBackground = 0xFF;
                    break;
            }

            //PPApplication.logE("IconWidgetProvider.onUpdate", "applicationWidgetIconShowBorder="+applicationWidgetIconShowBorder);
            //PPApplication.logE("IconWidgetProvider.onUpdate", "applicationWidgetIconLightnessBorder="+applicationWidgetIconLightnessBorder);
            int redBorder = 0xFF;
            int greenBorder;
            int blueBorder;
            if (applicationWidgetIconShowBorder) {
                switch (applicationWidgetIconLightnessBorder) {
                    case "0":
                        redBorder = 0x00;
                        break;
                    case "12":
                        redBorder = 0x20;
                        break;
                    case "25":
                        redBorder = 0x40;
                        break;
                    case "37":
                        redBorder = 0x60;
                        break;
                    case "50":
                        redBorder = 0x80;
                        break;
                    case "62":
                        redBorder = 0xA0;
                        break;
                    case "75":
                        redBorder = 0xC0;
                        break;
                    case "87":
                        redBorder = 0xE0;
                        break;
                    case "100":
                        redBorder = 0xFF;
                        break;
                }
            }
            greenBorder = redBorder;
            blueBorder = redBorder;

            //PPApplication.logE("IconWidgetProvider.onUpdate", "applicationWidgetIconLightnessT="+applicationWidgetIconLightnessT);
            int redText = 0xFF;
            switch (applicationWidgetIconLightnessT) {
                case "0":
                    redText = 0x00;
                    break;
                case "12":
                    redText = 0x20;
                    break;
                case "25":
                    redText = 0x40;
                    break;
                case "37":
                    redText = 0x60;
                    break;
                case "50":
                    redText = 0x80;
                    break;
                case "62":
                    redText = 0xA0;
                    break;
                case "75":
                    redText = 0xC0;
                    break;
                case "87":
                    redText = 0xE0;
                    break;
                case "100":
                    redText = 0xFF;
                    break;
            }
            int greenText = redText;
            int blueText = redText;

            //PPApplication.logE("IconWidgetProvider.onUpdate", "applicationWidgetIconShowProfileDuration="+applicationWidgetIconShowProfileDuration);
            //PPApplication.logE("IconWidgetProvider.onUpdate", "applicationWidgetIconColor="+applicationWidgetIconColor);
            //PPApplication.logE("IconWidgetProvider.onUpdate", "applicationWidgetIconCustomIconLightness="+applicationWidgetIconCustomIconLightness);
            boolean isIconResourceID;
            String iconIdentifier;
            Spannable profileName;
            if (profile != null) {
                isIconResourceID = profile.getIsIconResourceID();
                iconIdentifier = profile.getIconIdentifier();
                if (applicationWidgetIconShowProfileDuration)
                    profileName = DataWrapper.getProfileNameWithManualIndicator(profile, false, "", true, true, true, dataWrapper);
                else
                    profileName = DataWrapper.getProfileNameWithManualIndicator(profile, false, "", false, true, false, dataWrapper);
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
            //ComponentName thisWidget = new ComponentName(context, IconWidgetProvider.class);
            //int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
            //PPApplication.logE("IconWidgetProvider.onUpdate", "allWidgetIds="+allWidgetIds);
            //PPApplication.logE("IconWidgetProvider.onUpdate", "allWidgetIds.length="+allWidgetIds.length);

            // prepare view for widget update
            for (int widgetId : appWidgetIds) {
                //PPApplication.logE("IconWidgetProvider.onUpdate", "applicationWidgetIconHideProfileName="+applicationWidgetIconHideProfileName);
                //PPApplication.logE("IconWidgetProvider.onUpdate", "applicationWidgetIconShowProfileDuration="+applicationWidgetIconShowProfileDuration);
                RemoteViews remoteViews;
                if (applicationWidgetIconHideProfileName) {
                    //PPApplication.logE("IconWidgetProvider.onUpdate", "R.layout.icon_widget_no_profile_name");
                    remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.icon_widget_no_profile_name);
                }
                else {
                    if ((profile._endOfActivationType == 0) &&
                            (profile._duration > 0) &&
                            (applicationWidgetIconShowProfileDuration)) {
                        //PPApplication.logE("IconWidgetProvider.onUpdate", "R.layout.icon_widget");
                        remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.icon_widget);
                    }
                    else {
                        //PPApplication.logE("IconWidgetProvider.onUpdate", "R.layout.icon_widget_one_line_text");
                        remoteViews = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.icon_widget_one_line_text);
                    }
                }

//                PPApplication.logE("IconWidgetProvider.onUpdate", "applicationWidgetIconRoundedCornersRadius="+applicationWidgetIconRoundedCornersRadius);
                int roundedBackground = 0;
                int roundedBorder = 0;
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
                if (roundedBackground != 0)
                    remoteViews.setImageViewResource(R.id.widget_icon_background, roundedBackground);
                else
                    remoteViews.setImageViewResource(R.id.widget_icon_background, R.drawable.ic_empty);
                if (roundedBorder != 0)
                    remoteViews.setImageViewResource(R.id.widget_icon_rounded_border, roundedBorder);
                else
                    remoteViews.setImageViewResource(R.id.widget_icon_rounded_border, R.drawable.ic_empty);

                //PPApplication.logE("IconWidgetProvider.onUpdate", "applicationWidgetIconRoundedCorners="+applicationWidgetIconRoundedCorners);
                //PPApplication.logE("IconWidgetProvider.onUpdate", "applicationWidgetIconShowBorder="+applicationWidgetIconShowBorder);
                if (applicationWidgetIconRoundedCorners) {
                    remoteViews.setViewVisibility(R.id.widget_icon_background, View.VISIBLE);
                    remoteViews.setViewVisibility(R.id.widget_icon_not_rounded_border, View.GONE);
                    if (applicationWidgetIconShowBorder)
                        remoteViews.setViewVisibility(R.id.widget_icon_rounded_border, View.VISIBLE);
                    else
                        remoteViews.setViewVisibility(R.id.widget_icon_rounded_border, View.GONE);
                    remoteViews.setInt(R.id.widget_icon_root, "setBackgroundColor", 0x00000000);
                    remoteViews.setInt(R.id.widget_icon_background, "setColorFilter", Color.argb(0xFF, redBackground, greenBackground, blueBackground));
                    //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                    remoteViews.setInt(R.id.widget_icon_background, "setImageAlpha", alphaBackground);
                    //else
                    //    remoteViews.setInt(R.id.widget_icon_background, "setAlpha", alpha);
                    if (applicationWidgetIconShowBorder)
                        remoteViews.setInt(R.id.widget_icon_rounded_border, "setColorFilter", Color.argb(0xFF, redBorder, greenBorder, blueBorder));
                } else {
                    remoteViews.setViewVisibility(R.id.widget_icon_background, View.GONE);
                    remoteViews.setViewVisibility(R.id.widget_icon_rounded_border, View.GONE);
                    if (applicationWidgetIconShowBorder)
                        remoteViews.setViewVisibility(R.id.widget_icon_not_rounded_border, View.VISIBLE);
                    else
                        remoteViews.setViewVisibility(R.id.widget_icon_not_rounded_border, View.GONE);
                    remoteViews.setInt(R.id.widget_icon_root, "setBackgroundColor", Color.argb(alphaBackground, redBackground, greenBackground, blueBackground));
                        /*remoteViews.setInt(R.id.widget_icon_background, "setColorFilter", 0x00000000);
                        //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                        remoteViews.setInt(R.id.widget_icon_background, "setImageAlpha", 0);
                        //else
                        //    remoteViews.setInt(R.id.widget_icon_background, "setAlpha", 0);*/
                    if (applicationWidgetIconShowBorder)
                        remoteViews.setInt(R.id.widget_icon_not_rounded_border, "setColorFilter", Color.argb(0xFF, redBorder, greenBorder, blueBorder));
                }

                if (isIconResourceID) {
                    if (profile._iconBitmap != null)
                        remoteViews.setImageViewBitmap(R.id.icon_widget_icon, profile._iconBitmap);
                    else {
                        //int iconResource = context.getResources().getIdentifier(iconIdentifier, "drawable", context.PPApplication.PACKAGE_NAME);
                        int iconResource = Profile.getIconResource(iconIdentifier);
                        remoteViews.setImageViewResource(R.id.icon_widget_icon, iconResource);
                    }
                } else {
                    remoteViews.setImageViewBitmap(R.id.icon_widget_icon, profile._iconBitmap);
                }

                remoteViews.setTextColor(R.id.icon_widget_name, Color.argb(0xFF, redText, greenText, blueText));

                //PPApplication.logE("IconWidgetProvider.onUpdate", "applicationWidgetIconHideProfileName="+applicationWidgetIconHideProfileName);
                if (!applicationWidgetIconHideProfileName)
                    remoteViews.setTextViewText(R.id.icon_widget_name, profileName);

                // intent for start LauncherActivity on widget click
                Intent intent = new Intent(context, LauncherActivity.class);
                // clear all opened activities
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra(PPApplication.EXTRA_STARTUP_SOURCE, PPApplication.STARTUP_SOURCE_WIDGET);
                @SuppressLint("UnspecifiedImmutableFlag")
                PendingIntent pendingIntent = PendingIntent.getActivity(context, 100, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                //remoteViews.setOnClickPendingIntent(R.id.icon_widget_icon, pendingIntent);
                //remoteViews.setOnClickPendingIntent(R.id.icon_widget_name, pendingIntent);
                remoteViews.setOnClickPendingIntent(R.id.icon_widget_relLa1, pendingIntent);

                // widget update
                //PPApplication.logE("IconWidgetProvider.onUpdate", "appWidgetIds.length="+appWidgetIds.length);
                try {
                    appWidgetManager.updateAppWidget(widgetId, remoteViews);
                    //ComponentName thisWidget = new ComponentName(context, IconWidgetProvider.class);
                    //appWidgetManager.updateAppWidget(thisWidget, remoteViews);
                } catch (Exception e) {
                    PPApplication.recordException(e);
                }
            }
        //} catch (Exception e) {
            //PPApplication.logE("IconWidgetProvider.onUpdate", Log.getStackTraceString(e));
        //}

        //dataWrapper.invalidateDataWrapper();
    }

    @Override
    public void onReceive(Context context, final Intent intent) {
        super.onReceive(context, intent); // calls onUpdate, is required for widget
//        PPApplication.logE("[IN_BROADCAST] IconWidgetProvider.onReceive", "xxx");

        final String action = intent.getAction();

        if ((action != null) &&
                (action.equalsIgnoreCase(ACTION_REFRESH_ICONWIDGET))) {
            AppWidgetManager manager = AppWidgetManager.getInstance(context);
            if (manager != null) {
                final int[] ids = manager.getAppWidgetIds(new ComponentName(context, IconWidgetProvider.class));
                if ((ids != null) && (ids.length > 0)) {
                    final Context appContext = context;
                    final AppWidgetManager appWidgetManager = manager;
                    PPApplication.startHandlerThreadWidget();
                    final Handler __handler = new Handler(PPApplication.handlerThreadWidget.getLooper());
                    //__handler.post(new PPHandlerThreadRunnable(context, manager) {
                    __handler.post(() -> {
//                            PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThreadWidget", "START run - from=IconWidgetProvider.onReceive");

                        //Context appContext= appContextWeakRef.get();
                        //AppWidgetManager appWidgetManager = appWidgetManagerWeakRef.get();

                        //if ((appContext != null) && (appWidgetManager != null)) {
                            _onUpdate(appContext, appWidgetManager, ids);
                        //}
                    });
                }
            }
        }
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
        //PPApplication.logE("IconWidgetProvider.onUpdate", "ApplicationPreferences.applicationWidgetIconLightness="+ApplicationPreferences.applicationWidgetIconLightness);
        int monochromeValue = 0xFF;
        switch (applicationWidgetIconLightness) {
            case "0":
                monochromeValue = 0x00;
                break;
            case "12":
                monochromeValue = 0x20;
                break;
            case "25":
                monochromeValue = 0x40;
                break;
            case "37":
                monochromeValue = 0x60;
                break;
            case "50":
                monochromeValue = 0x80;
                break;
            case "62":
                monochromeValue = 0xA0;
                break;
            case "75":
                monochromeValue = 0xC0;
                break;
            case "87":
                monochromeValue = 0xE0;
                break;
            case "100":
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
            //PPApplication.logE("IconWidgetProvider.onUpdate", "PPApplication.prefWidgetProfileName1="+PPApplication.prefWidgetProfileName1);
            String pNameWidget = PPApplication.prefWidgetProfileName1;

            if (!pNameWidget.isEmpty()) {
                if (pName.equals(pNameWidget)) {
                    //PPApplication.logE("IconWidgetProvider.onUpdate", "activated profile NOT changed");
                    return;
                }
            }
        }

        PPApplication.setWidgetProfileName(context.getApplicationContext(), 1, pName);*/

//        PPApplication.logE("[LOCAL_BROADCAST_CALL] IconWidgetProvider.updateWidgets", "xxx");
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
