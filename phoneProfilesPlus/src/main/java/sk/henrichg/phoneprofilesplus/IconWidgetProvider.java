package sk.henrichg.phoneprofilesplus;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.View;
import android.widget.RemoteViews;

public class IconWidgetProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(final Context context, final AppWidgetManager appWidgetManager, final int[] appWidgetIds)
    {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        if (appWidgetIds.length > 0)
            _onUpdate(context, appWidgetManager, null, null, appWidgetIds);
    }

    public void _onUpdate(final Context context, final AppWidgetManager appWidgetManager,
                          final Profile _profile, final DataWrapper _dataWrapper, final int[] appWidgetIds) {
        PPApplication.startHandlerThreadWidget();
        final Handler handler = new Handler(PPApplication.handlerThreadWidget.getLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
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
                    applicationWidgetIconRoundedCorners = ApplicationPreferences.applicationWidgetIconRoundedCorners;
                    applicationWidgetIconLightnessT = ApplicationPreferences.applicationWidgetIconLightnessT;
                    applicationWidgetIconShowProfileDuration = ApplicationPreferences.applicationWidgetIconShowProfileDuration;
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

                DataWrapper dataWrapper = _dataWrapper;
                Profile profile = _profile;
                if (dataWrapper == null) {
                    //PPApplication.logE("IconWidgetProvider.onUpdate", "applicationWidgetIconColor="+applicationWidgetIconColor);
                    //PPApplication.logE("IconWidgetProvider.onUpdate", "applicationWidgetIconCustomIconLightness="+applicationWidgetIconCustomIconLightness);
                    dataWrapper = new DataWrapper(context.getApplicationContext(),
                            applicationWidgetIconColor.equals("1"),
                            monochromeValue,
                            applicationWidgetIconCustomIconLightness);

                    profile = dataWrapper.getActivatedProfile(true, false);
                }
                //PPApplication.logE("IconWidgetProvider.onUpdate", "profile="+profile);
                //if (profile != null)
                //    PPApplication.logE("IconWidgetProvider.onUpdate", "profile._name="+profile._name);

                //boolean fullyStarted = false;
                //if (PhoneProfilesService.getInstance() != null)
                //    fullyStarted = PhoneProfilesService.getInstance().getApplicationFullyStarted();
                boolean fullyStarted = PPApplication.applicationFullyStarted;
                //PPApplication.logE("IconWidgetProvider.onUpdate", "fullyStarted="+fullyStarted);

                //PPApplication.logE("IconWidgetProvider.onUpdate", "PPApplication.applicationPackageReplaced="+PPApplication.applicationPackageReplaced);
                boolean applicationPackageReplaced = PPApplication.applicationPackageReplaced;
                if ((!fullyStarted) || applicationPackageReplaced)
                    profile = null;

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

                    //PPApplication.logE("IconWidgetProvider.onUpdate", "applicationWidgetIconBackground="+ApplicationPreferencesapplicationWidgetIconBackground);
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
                        profile._name = context.getResources().getString(R.string.profiles_header_profile_name_no_activated);
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
                    //PPApplication.logE("IconWidgetProvider.onUpdate", "applicationWidgetIconHideProfileName="+applicationWidgetIconHideProfileName);
                    //PPApplication.logE("IconWidgetProvider.onUpdate", "applicationWidgetIconShowProfileDuration="+applicationWidgetIconShowProfileDuration);
                    RemoteViews remoteViews;
                    if (applicationWidgetIconHideProfileName) {
                        //PPApplication.logE("IconWidgetProvider.onUpdate", "R.layout.icon_widget_no_profile_name");
                        remoteViews = new RemoteViews(context.getPackageName(), R.layout.icon_widget_no_profile_name);
                    }
                    else {
                        if ((profile._duration > 0) && (applicationWidgetIconShowProfileDuration)) {
                            //PPApplication.logE("IconWidgetProvider.onUpdate", "R.layout.icon_widget");
                            remoteViews = new RemoteViews(context.getPackageName(), R.layout.icon_widget);
                        }
                        else {
                            //PPApplication.logE("IconWidgetProvider.onUpdate", "R.layout.icon_widget_one_line_text");
                            remoteViews = new RemoteViews(context.getPackageName(), R.layout.icon_widget_one_line_text);
                        }
                    }

                    //PPApplication.logE("IconWidgetProvider.onUpdate", "applicationWidgetIconRoundedCorners="+applicationWidgetIconRoundedCorners);
                    //PPApplication.logE("IconWidgetProvider.onUpdate", "applicationWidgetIconShowBorder="+applicationWidgetIconShowBorder);
                    if (applicationWidgetIconRoundedCorners) {
                        remoteViews.setViewVisibility(R.id.widget_icon_background, View.VISIBLE);
                        remoteViews.setViewVisibility(R.id.widget_icon_not_rounded_border, View.INVISIBLE);
                        if (applicationWidgetIconShowBorder)
                            remoteViews.setViewVisibility(R.id.widget_icon_rounded_border, View.VISIBLE);
                        else
                            remoteViews.setViewVisibility(R.id.widget_icon_rounded_border, View.INVISIBLE);
                        remoteViews.setInt(R.id.widget_icon_root, "setBackgroundColor", 0x00000000);
                        remoteViews.setInt(R.id.widget_icon_background, "setColorFilter", Color.argb(0xFF, redBackground, greenBackground, blueBackground));
                        //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                        remoteViews.setInt(R.id.widget_icon_background, "setImageAlpha", alphaBackground);
                        //else
                        //    remoteViews.setInt(R.id.widget_icon_background, "setAlpha", alpha);
                        if (applicationWidgetIconShowBorder)
                            remoteViews.setInt(R.id.widget_icon_rounded_border, "setColorFilter", Color.argb(0xFF, redBorder, greenBorder, blueBorder));
                    } else {
                        remoteViews.setViewVisibility(R.id.widget_icon_background, View.INVISIBLE);
                        remoteViews.setViewVisibility(R.id.widget_icon_rounded_border, View.INVISIBLE);
                        if (applicationWidgetIconShowBorder)
                            remoteViews.setViewVisibility(R.id.widget_icon_not_rounded_border, View.VISIBLE);
                        else
                            remoteViews.setViewVisibility(R.id.widget_icon_not_rounded_border, View.INVISIBLE);
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
                            //int iconResource = context.getResources().getIdentifier(iconIdentifier, "drawable", context.getPackageName());
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
                    PendingIntent pendingIntent = PendingIntent.getActivity(context, 100, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                    remoteViews.setOnClickPendingIntent(R.id.icon_widget_icon, pendingIntent);
                    remoteViews.setOnClickPendingIntent(R.id.icon_widget_name, pendingIntent);

                    // widget update
                    //PPApplication.logE("IconWidgetProvider.onUpdate", "appWidgetIds.length="+appWidgetIds.length);
                    for (int widgetId : appWidgetIds) {
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
        });
    }

    void updateWidgets(Context context, boolean refresh) {
        String applicationWidgetIconLightness;
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

        String pName;

        if (profile != null)
            pName = DataWrapper.getProfileNameWithManualIndicatorAsString(profile, false, "", true, false, false, dataWrapper);
        else
            pName = context.getResources().getString(R.string.profiles_header_profile_name_no_activated);

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

        PPApplication.setWidgetProfileName(context.getApplicationContext(), 1, pName);

        /*Intent intent = new Intent(context, IconWidgetProvider.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        int[] ids = AppWidgetManager.getInstance(context).getAppWidgetIds(new ComponentName(context, IconWidgetProvider.class));
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        context.sendBroadcast(intent);*/
        AppWidgetManager manager = AppWidgetManager.getInstance(context.getApplicationContext());
        if (manager != null) {
            int[] ids = manager.getAppWidgetIds(new ComponentName(context, IconWidgetProvider.class));
            if (ids.length > 0)
                _onUpdate(context, manager, profile, dataWrapper, ids);
        }
    }

}
