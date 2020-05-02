package sk.henrichg.phoneprofilesplus;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Handler;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.View;
import android.widget.RemoteViews;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class OneRowWidgetProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(final Context context, final AppWidgetManager appWidgetManager, final int[] appWidgetIds)
    {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        if (appWidgetIds.length > 0) {
            PPApplication.logE("##### OneRowWidgetProvider.onUpdate", "in broadcast");
            _onUpdate(context, appWidgetManager, null, null, appWidgetIds);
        }
    }

    private void _onUpdate(final Context context, final AppWidgetManager appWidgetManager,
                           final Profile _profile, final DataWrapper _dataWrapper, final int[] appWidgetIds)
    {
        //PPApplication.logE("OneRowWidgetProvider.onUpdate", "xxx");
        PPApplication.startHandlerThreadWidget();
        final Handler handler = new Handler(PPApplication.handlerThreadWidget.getLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                PPApplication.logE("##### OneRowWidgetProvider._onUpdate", "in handler");

                String applicationWidgetOneRowIconLightness;
                String applicationWidgetOneRowIconColor;
                boolean applicationWidgetOneRowCustomIconLightness;
                boolean applicationWidgetOneRowPrefIndicator;
                boolean applicationWidgetOneRowBackgroundType;
                String applicationWidgetOneRowBackgroundColor;
                String applicationWidgetOneRowLightnessB;
                String applicationWidgetOneRowBackground;
                boolean applicationWidgetOneRowShowBorder;
                String applicationWidgetOneRowLightnessBorder;
                boolean applicationWidgetOneRowRoundedCorners;
                String applicationWidgetOneRowLightnessT;
                synchronized (PPApplication.applicationPreferencesMutex) {
                    applicationWidgetOneRowIconLightness = ApplicationPreferences.applicationWidgetOneRowIconLightness;
                    applicationWidgetOneRowIconColor = ApplicationPreferences.applicationWidgetOneRowIconColor;
                    applicationWidgetOneRowCustomIconLightness = ApplicationPreferences.applicationWidgetOneRowCustomIconLightness;
                    applicationWidgetOneRowPrefIndicator = ApplicationPreferences.applicationWidgetOneRowPrefIndicator;
                    applicationWidgetOneRowBackgroundType = ApplicationPreferences.applicationWidgetOneRowBackgroundType;
                    applicationWidgetOneRowBackgroundColor = ApplicationPreferences.applicationWidgetOneRowBackgroundColor;
                    applicationWidgetOneRowLightnessB = ApplicationPreferences.applicationWidgetOneRowLightnessB;
                    applicationWidgetOneRowBackground = ApplicationPreferences.applicationWidgetOneRowBackground;
                    applicationWidgetOneRowShowBorder = ApplicationPreferences.applicationWidgetOneRowShowBorder;
                    applicationWidgetOneRowLightnessBorder = ApplicationPreferences.applicationWidgetOneRowLightnessBorder;
                    applicationWidgetOneRowRoundedCorners = ApplicationPreferences.applicationWidgetOneRowRoundedCorners;
                    applicationWidgetOneRowLightnessT = ApplicationPreferences.applicationWidgetOneRowLightnessT;
                }

                //PPApplication.logE("OneRowWidgetProvider.onUpdate", "applicationWidgetOneRowShowBorder="+applicationWidgetOneRowShowBorder);

                int monochromeValue = 0xFF;
                switch (applicationWidgetOneRowIconLightness) {
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
                    dataWrapper = new DataWrapper(context.getApplicationContext(),
                            applicationWidgetOneRowIconColor.equals("1"), monochromeValue,
                            applicationWidgetOneRowCustomIconLightness);

                    profile = dataWrapper.getActivatedProfile(true, applicationWidgetOneRowPrefIndicator);
                }

                //boolean fullyStarted = false;
                //if (PhoneProfilesService.getInstance() != null)
                //    fullyStarted = PhoneProfilesService.getInstance().getApplicationFullyStarted();
                boolean fullyStarted = PPApplication.applicationFullyStarted;
                boolean applicationPackageReplaced = PPApplication.applicationPackageReplaced;
                if ((!fullyStarted) || applicationPackageReplaced)
                    profile = null;

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

                    int alphaBackground = 0x40;
                    switch (applicationWidgetOneRowBackground) {
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

                    int redBorder = 0xFF;
                    int greenBorder;
                    int blueBorder;
                    if (applicationWidgetOneRowShowBorder) {
                        //PPApplication.logE("OneRowWidgetProvider.onUpdate", "");
                        switch (applicationWidgetOneRowLightnessBorder) {
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
                        //PPApplication.logE("OneRowWidgetProvider.onUpdate", "redBorder="+redBorder);
                    }
                    greenBorder = redBorder;
                    blueBorder = redBorder;

                    int redText = 0xFF;
                    switch (applicationWidgetOneRowLightnessT) {
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

                    int restartEventsLightness = redText;

                    boolean isIconResourceID;
                    String iconIdentifier;
                    Spannable profileName;
                    if (profile != null) {
                        isIconResourceID = profile.getIsIconResourceID();
                        iconIdentifier = profile.getIconIdentifier();
                        profileName = DataWrapper.getProfileNameWithManualIndicator(profile, true, "", true, false, false, dataWrapper);
                    } else {
                        // create empty profile and set icon resource
                        profile = new Profile();
                        profile._name = context.getResources().getString(R.string.profiles_header_profile_name_no_activated);
                        profile._icon = Profile.PROFILE_ICON_DEFAULT + "|1|0|0";

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

                    RemoteViews remoteViews;
                    if (applicationWidgetOneRowPrefIndicator)
                        remoteViews = new RemoteViews(context.getPackageName(), R.layout.one_row_widget);
                    else
                        remoteViews = new RemoteViews(context.getPackageName(), R.layout.one_row_widget_no_indicator);

                    if (applicationWidgetOneRowRoundedCorners) {
                        //PPApplication.logE("OneRowWidgetProvider.onUpdate", "rounded corners");
                        remoteViews.setViewVisibility(R.id.widget_one_row_background, VISIBLE);
                        remoteViews.setViewVisibility(R.id.widget_one_row_not_rounded_border, View.INVISIBLE);
                        if (applicationWidgetOneRowShowBorder) {
                            //PPApplication.logE("OneRowWidgetProvider.onUpdate", "VISIBLE border");
                            remoteViews.setViewVisibility(R.id.widget_one_row_rounded_border, VISIBLE);
                        }
                        else {
                            //PPApplication.logE("OneRowWidgetProvider.onUpdate", "GONE border");
                            remoteViews.setViewVisibility(R.id.widget_one_row_rounded_border, View.INVISIBLE);
                        }
                        remoteViews.setInt(R.id.widget_one_row_root, "setBackgroundColor", 0x00000000);
                        remoteViews.setInt(R.id.widget_one_row_background, "setColorFilter", Color.argb(0xFF, redBackground, greenBackground, blueBackground));
                        //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                        remoteViews.setInt(R.id.widget_one_row_background, "setImageAlpha", alphaBackground);
                        //else
                        //    remoteViews.setInt(R.id.widget_one_row_background, "setAlpha", alpha);
                        if (applicationWidgetOneRowShowBorder)
                            remoteViews.setInt(R.id.widget_one_row_rounded_border, "setColorFilter", Color.argb(0xFF, redBorder, greenBorder, blueBorder));
                    } else {
                        //PPApplication.logE("OneRowWidgetProvider.onUpdate", "NOT rounded corners");
                        remoteViews.setViewVisibility(R.id.widget_one_row_background, View.INVISIBLE);
                        remoteViews.setViewVisibility(R.id.widget_one_row_rounded_border, View.INVISIBLE);
                        if (applicationWidgetOneRowShowBorder) {
                            //PPApplication.logE("OneRowWidgetProvider.onUpdate", "VISIBLE border");
                            remoteViews.setViewVisibility(R.id.widget_one_row_not_rounded_border, VISIBLE);
                        }
                        else {
                            //PPApplication.logE("OneRowWidgetProvider.onUpdate", "GONE border");
                            remoteViews.setViewVisibility(R.id.widget_one_row_not_rounded_border, View.INVISIBLE);
                        }
                        remoteViews.setInt(R.id.widget_one_row_root, "setBackgroundColor", Color.argb(alphaBackground, redBackground, greenBackground, blueBackground));
                            /*remoteViews.setInt(R.id.widget_one_row_background, "setColorFilter", 0x00000000);
                            //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                            remoteViews.setInt(R.id.widget_one_row_background, "setImageAlpha", 0);
                            //else
                            //    remoteViews.setInt(R.id.widget_one_row_background, "setAlpha", 0);*/
                        if (applicationWidgetOneRowShowBorder)
                            remoteViews.setInt(R.id.widget_one_row_not_rounded_border, "setColorFilter", Color.argb(0xFF, redBorder, greenBorder, blueBorder));
                    }

                    if (isIconResourceID) {
                        if (profile._iconBitmap != null)
                            remoteViews.setImageViewBitmap(R.id.widget_one_row_header_profile_icon, profile._iconBitmap);
                        else {
                            //remoteViews.setImageViewResource(R.id.activate_profile_widget_icon, 0);
                            //int iconResource = context.getResources().getIdentifier(iconIdentifier, "drawable", context.getPackageName());
                            int iconResource = Profile.getIconResource(iconIdentifier);
                            remoteViews.setImageViewResource(R.id.widget_one_row_header_profile_icon, iconResource);
                        }
                    } else {
                        remoteViews.setImageViewBitmap(R.id.widget_one_row_header_profile_icon, profile._iconBitmap);
                    }

                    remoteViews.setTextColor(R.id.widget_one_row_header_profile_name, Color.argb(0xFF, redText, greenText, blueText));

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

                    //if (Event.getGlobalEventsRunning() && PPApplication.getApplicationStarted(true)) {
                    Bitmap bitmap = BitmapManipulator.getBitmapFromResource(R.drawable.ic_widget_restart_events, true, context);
                    bitmap = BitmapManipulator.monochromeBitmap(bitmap, restartEventsLightness);
                    remoteViews.setImageViewBitmap(R.id.widget_one_row_header_restart_events, bitmap);
                    //}

                        /*if (PPApplication.logEnabled()) {
                            PPApplication.logE("OneRowWidgetProvider.onUpdate", "events running=" + Event.getGlobalEventsRunning(context));
                            PPApplication.logE("OneRowWidgetProvider.onUpdate", "application started=" + PPApplication.getApplicationStarted(context, true));
                        }*/
                    //if (Event.getGlobalEventsRunning() && PPApplication.getApplicationStarted(true)) {
                    //remoteViews.setViewVisibility(R.id.widget_one_row_header_restart_events, VISIBLE);
                    Intent intentRE = new Intent(context, RestartEventsFromGUIActivity.class);
                    PendingIntent pIntentRE = PendingIntent.getActivity(context, 2, intentRE, PendingIntent.FLAG_UPDATE_CURRENT);
                    remoteViews.setOnClickPendingIntent(R.id.widget_one_row_header_restart_events, pIntentRE);
                    //} else
                    //    remoteViews.setViewVisibility(R.id.widget_one_row_header_restart_events, View.GONE);

                    // intent for start LauncherActivity on widget click
                    Intent intent = new Intent(context, LauncherActivity.class);
                    // clear all opened activities
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra(PPApplication.EXTRA_STARTUP_SOURCE, PPApplication.STARTUP_SOURCE_WIDGET);
                    PendingIntent pendingIntent = PendingIntent.getActivity(context, 200, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                    remoteViews.setOnClickPendingIntent(R.id.widget_one_row_header_profile_root, pendingIntent);

                    // widget update
                    for (int widgetId : appWidgetIds) {
                        try {
                            appWidgetManager.updateAppWidget(widgetId, remoteViews);
                            //ComponentName thisWidget = new ComponentName(context, OneRowWidgetProvider.class);
                            //appWidgetManager.updateAppWidget(thisWidget, remoteViews);
                        } catch (Exception e) {
                            PPApplication.recordException(e);
                        }
                    }
                //} catch (Exception ee) {
                //    PPApplication.recordException(ee);
                //}

                //dataWrapper.invalidateDataWrapper();
            }
        });
    }

    void updateWidgets(Context context, boolean refresh) {
        String applicationWidgetOneRowIconLightness;
        String applicationWidgetOneRowIconColor;
        boolean applicationWidgetOneRowCustomIconLightness;
        boolean applicationWidgetOneRowPrefIndicator;
        synchronized (PPApplication.applicationPreferencesMutex) {
            applicationWidgetOneRowIconLightness = ApplicationPreferences.applicationWidgetOneRowIconLightness;
            applicationWidgetOneRowIconColor = ApplicationPreferences.applicationWidgetOneRowIconColor;
            applicationWidgetOneRowCustomIconLightness = ApplicationPreferences.applicationWidgetOneRowCustomIconLightness;
            applicationWidgetOneRowPrefIndicator = ApplicationPreferences.applicationWidgetOneRowPrefIndicator;
        }

        //PPApplication.logE("OneRowWidgetProvider.onUpdate", "applicationWidgetOneRowShowBorder="+applicationWidgetOneRowShowBorder);

        int monochromeValue = 0xFF;
        switch (applicationWidgetOneRowIconLightness) {
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
                applicationWidgetOneRowIconColor.equals("1"), monochromeValue,
                applicationWidgetOneRowCustomIconLightness);

        Profile profile = dataWrapper.getActivatedProfile(true, applicationWidgetOneRowPrefIndicator);

        String pName;

        if (profile != null)
            pName = DataWrapper.getProfileNameWithManualIndicatorAsString(profile, true, "", true, false, false, dataWrapper);
        else
            pName = context.getResources().getString(R.string.profiles_header_profile_name_no_activated);

        if (!refresh) {
            String pNameWidget = PPApplication.prefWidgetProfileName2;

            if (!pNameWidget.isEmpty()) {
                if (pName.equals(pNameWidget)) {
                    //PPApplication.logE("OneRowWidgetProvider.onUpdate", "activated profile NOT changed");
                    return;
                }
            }
        }

        PPApplication.setWidgetProfileName(context, 2, pName);

        /*Intent intent = new Intent(context, OneRowWidgetProvider.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        int[] ids = AppWidgetManager.getInstance(context).getAppWidgetIds(new ComponentName(context, OneRowWidgetProvider.class));
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        context.sendBroadcast(intent);*/
        AppWidgetManager manager = AppWidgetManager.getInstance(context.getApplicationContext());
        if (manager != null) {
            int[] ids = manager.getAppWidgetIds(new ComponentName(context, OneRowWidgetProvider.class));
            if ((ids != null) && (ids.length > 0))
                _onUpdate(context.getApplicationContext(), manager, profile, dataWrapper, ids);
        }
    }

}
