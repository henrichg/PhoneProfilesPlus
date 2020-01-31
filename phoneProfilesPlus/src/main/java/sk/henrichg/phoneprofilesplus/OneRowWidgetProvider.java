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

    boolean refreshWidget = true;

    @Override
    public void onUpdate(final Context context, final AppWidgetManager appWidgetManager, final int[] appWidgetIds)
    {
        //PPApplication.logE("OneRowWidgetProvider.onUpdate", "xxx");
        PPApplication.startHandlerThreadWidget();
        final Handler handler = new Handler(PPApplication.handlerThreadWidget.getLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                //PPApplication.logE("OneRowWidgetProvider.onUpdate", "in handler");
                String applicationWidgetOneRowIconLightness = ApplicationPreferences.applicationWidgetOneRowIconLightness;
                String applicationWidgetOneRowIconColor = ApplicationPreferences.applicationWidgetOneRowIconColor;
                boolean applicationWidgetOneRowCustomIconLightness = ApplicationPreferences.applicationWidgetOneRowCustomIconLightness;
                //boolean applicationWidgetOneRowPrefIndicator = ApplicationPreferences.applicationWidgetOneRowPrefIndicator(context);
                boolean applicationWidgetOneRowPrefIndicator = ApplicationPreferences.applicationWidgetListPrefIndicator;
                boolean applicationWidgetOneRowBackgroundType = ApplicationPreferences.applicationWidgetOneRowBackgroundType;
                String applicationWidgetOneRowBackgroundColor = ApplicationPreferences.applicationWidgetOneRowBackgroundColor;
                String applicationWidgetOneRowLightnessB = ApplicationPreferences.applicationWidgetOneRowLightnessB;
                String applicationWidgetOneRowBackground = ApplicationPreferences.applicationWidgetOneRowBackground;
                boolean applicationWidgetOneRowShowBorder = ApplicationPreferences.applicationWidgetOneRowShowBorder;
                String applicationWidgetOneRowLightnessBorder = ApplicationPreferences.applicationWidgetOneRowLightnessBorder;
                boolean applicationWidgetOneRowRoundedCorners = ApplicationPreferences.applicationWidgetOneRowRoundedCorners;
                String applicationWidgetOneRowLightnessT = ApplicationPreferences.applicationWidgetOneRowLightnessT;

                //PPApplication.logE("OneRowWidgetProvider.onUpdate", "applicationWidgetOneRowShowBorder="+applicationWidgetOneRowShowBorder);

                int monochromeValue = 0xFF;
                if (applicationWidgetOneRowIconLightness.equals("0")) monochromeValue = 0x00;
                if (applicationWidgetOneRowIconLightness.equals("12")) monochromeValue = 0x20;
                if (applicationWidgetOneRowIconLightness.equals("25")) monochromeValue = 0x40;
                if (applicationWidgetOneRowIconLightness.equals("37")) monochromeValue = 0x60;
                if (applicationWidgetOneRowIconLightness.equals("50")) monochromeValue = 0x80;
                if (applicationWidgetOneRowIconLightness.equals("62")) monochromeValue = 0xA0;
                if (applicationWidgetOneRowIconLightness.equals("75")) monochromeValue = 0xC0;
                if (applicationWidgetOneRowIconLightness.equals("87")) monochromeValue = 0xE0;
                //if (applicationWidgetOneRowIconLightness.equals("100")) monochromeValue = 0xFF;

                DataWrapper dataWrapper = new DataWrapper(context,
                        applicationWidgetOneRowIconColor.equals("1"), monochromeValue,
                        applicationWidgetOneRowCustomIconLightness);

                Profile profile = dataWrapper.getActivatedProfile(true, applicationWidgetOneRowPrefIndicator);

                try {
                    if (!refreshWidget) {
                        String pNameWidget = PPApplication.prefWidgetProfileName2;

                        if (!pNameWidget.isEmpty()) {
                            String pName;
                            if (profile != null)
                                pName = DataWrapper.getProfileNameWithManualIndicatorAsString(profile, true, "", true, false, false, dataWrapper, true, context);
                            else
                                pName = context.getResources().getString(R.string.profiles_header_profile_name_no_activated);

                            if (pName.equals(pNameWidget)) {
                                //PPApplication.logE("OneRowWidgetProvider.onUpdate", "activated profile NOT changed");
                                return;
                            }
                        }
                    }


                    boolean isIconResourceID;
                    String iconIdentifier;
                    String pName;
                    Spannable profileName;
                    if (profile != null) {
                        isIconResourceID = profile.getIsIconResourceID();
                        iconIdentifier = profile.getIconIdentifier();
                        pName = DataWrapper.getProfileNameWithManualIndicatorAsString(profile, true, "", true, false, false, dataWrapper, false, context);
                        profileName = DataWrapper.getProfileNameWithManualIndicator(profile, true, "", true, false, false, dataWrapper, false, context);
                    } else {
                        // create empty profile and set icon resource
                        profile = new Profile();
                        profile._name = context.getResources().getString(R.string.profiles_header_profile_name_no_activated);
                        profile._icon = Profile.PROFILE_ICON_DEFAULT + "|1|0|0";

                        profile.generateIconBitmap(context,
                                applicationWidgetOneRowIconColor.equals("1"),
                                monochromeValue,
                                applicationWidgetOneRowCustomIconLightness);
                        isIconResourceID = profile.getIsIconResourceID();
                        iconIdentifier = profile.getIconIdentifier();
                        pName = profile._name;
                        profileName = new SpannableString(profile._name);
                    }

                    PPApplication.setWidgetProfileName(context, 2, pName);

                    // get all OneRowWidgetProvider widgets in launcher
                    ComponentName thisWidget = new ComponentName(context, OneRowWidgetProvider.class);
                    int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);

                    for (int widgetId : allWidgetIds) {

                        RemoteViews remoteViews;
                        if (applicationWidgetOneRowPrefIndicator)
                            remoteViews = new RemoteViews(context.getPackageName(), R.layout.one_row_widget);
                        else
                            remoteViews = new RemoteViews(context.getPackageName(), R.layout.one_row_widget_no_indicator);


                        // set background
                        int redBackground = 0x00;
                        int greenBackground;
                        int blueBackground;
                        if (applicationWidgetOneRowBackgroundType) {
                            int bgColor = Integer.valueOf(applicationWidgetOneRowBackgroundColor);
                            redBackground = Color.red(bgColor);
                            greenBackground = Color.green(bgColor);
                            blueBackground = Color.blue(bgColor);
                        } else {
                            //if (applicationWidgetOneRowLightnessB.equals("0")) redBackground = 0x00;
                            if (applicationWidgetOneRowLightnessB.equals("12")) redBackground = 0x20;
                            if (applicationWidgetOneRowLightnessB.equals("25")) redBackground = 0x40;
                            if (applicationWidgetOneRowLightnessB.equals("37")) redBackground = 0x60;
                            if (applicationWidgetOneRowLightnessB.equals("50")) redBackground = 0x80;
                            if (applicationWidgetOneRowLightnessB.equals("62")) redBackground = 0xA0;
                            if (applicationWidgetOneRowLightnessB.equals("75")) redBackground = 0xC0;
                            if (applicationWidgetOneRowLightnessB.equals("87")) redBackground = 0xE0;
                            if (applicationWidgetOneRowLightnessB.equals("100")) redBackground = 0xFF;
                            greenBackground = redBackground;
                            blueBackground = redBackground;
                        }
                        int alphaBackground = 0x40;
                        if (applicationWidgetOneRowBackground.equals("0")) alphaBackground = 0x00;
                        if (applicationWidgetOneRowBackground.equals("12")) alphaBackground = 0x20;
                        //if (applicationWidgetOneRowBackground.equals("25")) alphaBackground = 0x40;
                        if (applicationWidgetOneRowBackground.equals("37")) alphaBackground = 0x60;
                        if (applicationWidgetOneRowBackground.equals("50")) alphaBackground = 0x80;
                        if (applicationWidgetOneRowBackground.equals("62")) alphaBackground = 0xA0;
                        if (applicationWidgetOneRowBackground.equals("75")) alphaBackground = 0xC0;
                        if (applicationWidgetOneRowBackground.equals("87")) alphaBackground = 0xE0;
                        if (applicationWidgetOneRowBackground.equals("100")) alphaBackground = 0xFF;
                        int redBorder = 0xFF;
                        int greenBorder;
                        int blueBorder;
                        if (applicationWidgetOneRowShowBorder) {
                            //PPApplication.logE("OneRowWidgetProvider.onUpdate", "");
                            if (applicationWidgetOneRowLightnessBorder.equals("0")) redBorder = 0x00;
                            if (applicationWidgetOneRowLightnessBorder.equals("12")) redBorder = 0x20;
                            if (applicationWidgetOneRowLightnessBorder.equals("25")) redBorder = 0x40;
                            if (applicationWidgetOneRowLightnessBorder.equals("37")) redBorder = 0x60;
                            if (applicationWidgetOneRowLightnessBorder.equals("50")) redBorder = 0x80;
                            if (applicationWidgetOneRowLightnessBorder.equals("62")) redBorder = 0xA0;
                            if (applicationWidgetOneRowLightnessBorder.equals("75")) redBorder = 0xC0;
                            if (applicationWidgetOneRowLightnessBorder.equals("87")) redBorder = 0xE0;
                            //if (applicationWidgetOneRowLightnessBorder.equals("100")) redBorder = 0xFF;
                            //PPApplication.logE("OneRowWidgetProvider.onUpdate", "redBorder="+redBorder);
                        }
                        greenBorder = redBorder;
                        blueBorder = redBorder;
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

                        int redText = 0xFF;
                        if (applicationWidgetOneRowLightnessT.equals("0")) redText = 0x00;
                        if (applicationWidgetOneRowLightnessT.equals("12")) redText = 0x20;
                        if (applicationWidgetOneRowLightnessT.equals("25")) redText = 0x40;
                        if (applicationWidgetOneRowLightnessT.equals("37")) redText = 0x60;
                        if (applicationWidgetOneRowLightnessT.equals("50")) redText = 0x80;
                        if (applicationWidgetOneRowLightnessT.equals("62")) redText = 0xA0;
                        if (applicationWidgetOneRowLightnessT.equals("75")) redText = 0xC0;
                        if (applicationWidgetOneRowLightnessT.equals("87")) redText = 0xE0;
                        //if (applicationWidgetOneRowLightnessT.equals("100")) redText = 0xFF;
                        int greenText = redText;
                        int blueText = redText;
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
                            monochromeValue = 0xFF;
                            if (applicationWidgetOneRowLightnessT.equals("0")) monochromeValue = 0x00;
                            if (applicationWidgetOneRowLightnessT.equals("12")) monochromeValue = 0x20;
                            if (applicationWidgetOneRowLightnessT.equals("25")) monochromeValue = 0x40;
                            if (applicationWidgetOneRowLightnessT.equals("37")) monochromeValue = 0x60;
                            if (applicationWidgetOneRowLightnessT.equals("50")) monochromeValue = 0x80;
                            if (applicationWidgetOneRowLightnessT.equals("62")) monochromeValue = 0xA0;
                            if (applicationWidgetOneRowLightnessT.equals("75")) monochromeValue = 0xC0;
                            if (applicationWidgetOneRowLightnessT.equals("87")) monochromeValue = 0xE0;
                            //if (applicationWidgetOneRowLightnessT.equals("100")) monochromeValue = 0xFF;

                            Bitmap bitmap = BitmapManipulator.getBitmapFromResource(R.drawable.ic_widget_restart_events, true, context);
                            bitmap = BitmapManipulator.monochromeBitmap(bitmap, monochromeValue);
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
                        try {
                            appWidgetManager.updateAppWidget(widgetId, remoteViews);
                        } catch (Exception ignored) {}
                    }
                } catch (Exception ignored) {}

                //dataWrapper.invalidateDataWrapper();
            }
        });
    }

}
