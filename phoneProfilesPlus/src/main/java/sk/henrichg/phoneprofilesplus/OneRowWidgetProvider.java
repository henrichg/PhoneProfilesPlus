package sk.henrichg.phoneprofilesplus;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Handler;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.View;
import android.widget.RemoteViews;

public class OneRowWidgetProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(final Context context, final AppWidgetManager appWidgetManager, final int[] appWidgetIds)
    {
        PPApplication.startHandlerThreadWidget();
        final Handler handler = new Handler(PPApplication.handlerThreadWidget.getLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                String applicationWidgetOneRowIconLightness = ApplicationPreferences.applicationWidgetOneRowIconLightness(context);
                String applicationWidgetOneRowIconColor = ApplicationPreferences.applicationWidgetOneRowIconColor(context);
                boolean applicationWidgetOneRowCustomIconLightness = ApplicationPreferences.applicationWidgetOneRowCustomIconLightness(context);
                boolean applicationWidgetOneRowPrefIndicator = ApplicationPreferences.applicationWidgetOneRowPrefIndicator(context);
                boolean applicationWidgetOneRowBackgroundType = ApplicationPreferences.applicationWidgetOneRowBackgroundType(context);
                String applicationWidgetOneRowBackgroundColor = ApplicationPreferences.applicationWidgetOneRowBackgroundColor(context);
                String applicationWidgetOneRowLightnessB = ApplicationPreferences.applicationWidgetOneRowLightnessB(context);
                String applicationWidgetOneRowBackground = ApplicationPreferences.applicationWidgetOneRowBackground(context);
                boolean applicationWidgetOneRowShowBorder = ApplicationPreferences.applicationWidgetOneRowShowBorder(context);
                String applicationWidgetOneRowLightnessBorder = ApplicationPreferences.applicationWidgetOneRowLightnessBorder(context);
                boolean applicationWidgetOneRowRoundedCorners = ApplicationPreferences.applicationWidgetOneRowRoundedCorners(context);
                String applicationWidgetOneRowLightnessT = ApplicationPreferences.applicationWidgetOneRowLightnessT(context);

                int monochromeValue = 0xFF;
                if (applicationWidgetOneRowIconLightness.equals("0")) monochromeValue = 0x00;
                if (applicationWidgetOneRowIconLightness.equals("25")) monochromeValue = 0x40;
                if (applicationWidgetOneRowIconLightness.equals("50")) monochromeValue = 0x80;
                if (applicationWidgetOneRowIconLightness.equals("75")) monochromeValue = 0xC0;
                //if (applicationWidgetOneRowIconLightness.equals("100")) monochromeValue = 0xFF;

                DataWrapper dataWrapper = new DataWrapper(context,
                        applicationWidgetOneRowIconColor.equals("1"), monochromeValue,
                        applicationWidgetOneRowCustomIconLightness);

                Profile profile = dataWrapper.getActivatedProfile(true,
                        applicationWidgetOneRowPrefIndicator);

                try {
                    // get all OneRowWidgetProvider widgets in launcher
                    ComponentName thisWidget = new ComponentName(context, OneRowWidgetProvider.class);
                    int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);

                    for (int widgetId : allWidgetIds) {
                        boolean isIconResourceID;
                        String iconIdentifier;
                        Spannable profileName;
                        if (profile != null) {
                            isIconResourceID = profile.getIsIconResourceID();
                            iconIdentifier = profile.getIconIdentifier();
                            profileName = DataWrapper.getProfileNameWithManualIndicator(profile, true, "", true, false, dataWrapper, false);
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
                            profileName = new SpannableString(profile._name);
                        }

                        RemoteViews remoteViews;
                        if (applicationWidgetOneRowPrefIndicator)
                            remoteViews = new RemoteViews(context.getPackageName(), R.layout.one_row_widget);
                        else
                            remoteViews = new RemoteViews(context.getPackageName(), R.layout.one_row_widget_no_indicator);


                        // set background
                        int red = 0x00;
                        int green;
                        int blue;
                        if (applicationWidgetOneRowBackgroundType) {
                            int bgColor = Integer.valueOf(applicationWidgetOneRowBackgroundColor);
                            red = Color.red(bgColor);
                            green = Color.green(bgColor);
                            blue = Color.blue(bgColor);
                        } else {
                            //if (applicationWidgetOneRowLightnessB.equals("0")) red = 0x00;
                            if (applicationWidgetOneRowLightnessB.equals("25")) red = 0x40;
                            if (applicationWidgetOneRowLightnessB.equals("50")) red = 0x80;
                            if (applicationWidgetOneRowLightnessB.equals("75")) red = 0xC0;
                            if (applicationWidgetOneRowLightnessB.equals("100")) red = 0xFF;
                            green = red;
                            blue = red;
                        }
                        int alpha = 0x40;
                        if (applicationWidgetOneRowBackground.equals("0")) alpha = 0x00;
                        //if (applicationWidgetOneRowBackground.equals("25")) alpha = 0x40;
                        if (applicationWidgetOneRowBackground.equals("50")) alpha = 0x80;
                        if (applicationWidgetOneRowBackground.equals("75")) alpha = 0xC0;
                        if (applicationWidgetOneRowBackground.equals("100")) alpha = 0xFF;
                        int redBorder = 0xFF;
                        int greenBorder;
                        int blueBorder;
                        if (applicationWidgetOneRowShowBorder) {
                            if (applicationWidgetOneRowLightnessBorder.equals("0")) redBorder = 0x00;
                            if (applicationWidgetOneRowLightnessBorder.equals("25")) redBorder = 0x40;
                            if (applicationWidgetOneRowLightnessBorder.equals("50")) redBorder = 0x80;
                            if (applicationWidgetOneRowLightnessBorder.equals("75")) redBorder = 0xC0;
                            //if (applicationWidgetOneRowLightnessBorder.equals("100")) redBorder = 0xFF;
                        }
                        greenBorder = redBorder;
                        blueBorder = redBorder;
                        if (applicationWidgetOneRowRoundedCorners) {
                            remoteViews.setViewVisibility(R.id.widget_one_row_background, View.VISIBLE);
                            remoteViews.setViewVisibility(R.id.widget_one_row_not_rounded_border, View.GONE);
                            if (applicationWidgetOneRowShowBorder)
                                remoteViews.setViewVisibility(R.id.widget_one_row_rounded_border, View.VISIBLE);
                            else
                                remoteViews.setViewVisibility(R.id.widget_one_row_rounded_border, View.GONE);
                            remoteViews.setInt(R.id.widget_one_row_root, "setBackgroundColor", 0x00000000);
                            remoteViews.setInt(R.id.widget_one_row_background, "setColorFilter", Color.argb(0xFF, red, green, blue));
                            //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                            remoteViews.setInt(R.id.widget_one_row_background, "setImageAlpha", alpha);
                            //else
                            //    remoteViews.setInt(R.id.widget_one_row_background, "setAlpha", alpha);
                            if (applicationWidgetOneRowShowBorder)
                                remoteViews.setInt(R.id.widget_one_row_rounded_border, "setColorFilter", Color.argb(0xFF, redBorder, greenBorder, blueBorder));
                        } else {
                            remoteViews.setViewVisibility(R.id.widget_one_row_background, View.GONE);
                            remoteViews.setViewVisibility(R.id.widget_one_row_rounded_border, View.GONE);
                            if (applicationWidgetOneRowShowBorder)
                                remoteViews.setViewVisibility(R.id.widget_one_row_not_rounded_border, View.VISIBLE);
                            else
                                remoteViews.setViewVisibility(R.id.widget_one_row_not_rounded_border, View.GONE);
                            remoteViews.setInt(R.id.widget_one_row_root, "setBackgroundColor", Color.argb(alpha, red, green, blue));
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

                        red = 0xFF;
                        if (applicationWidgetOneRowLightnessT.equals("0")) red = 0x00;
                        if (applicationWidgetOneRowLightnessT.equals("25")) red = 0x40;
                        if (applicationWidgetOneRowLightnessT.equals("50")) red = 0x80;
                        if (applicationWidgetOneRowLightnessT.equals("75")) red = 0xC0;
                        //if (applicationWidgetOneRowLightnessT.equals("100")) red = 0xFF;
                        green = red;
                        blue = red;
                        remoteViews.setTextColor(R.id.widget_one_row_header_profile_name, Color.argb(0xFF, red, green, blue));

                        remoteViews.setTextViewText(R.id.widget_one_row_header_profile_name, profileName);
                        if (applicationWidgetOneRowPrefIndicator) {
                            if (profile._preferencesIndicator == null)
                                remoteViews.setImageViewResource(R.id.widget_one_row_header_profile_pref_indicator, R.drawable.ic_empty);
                            else
                                remoteViews.setImageViewBitmap(R.id.widget_one_row_header_profile_pref_indicator, profile._preferencesIndicator);
                        }

                        if (Event.getGlobalEventsRunning(context) && PPApplication.getApplicationStarted(context, true)) {
                            if (applicationWidgetOneRowIconColor.equals("1")) {
                                monochromeValue = 0xFF;
                                if (applicationWidgetOneRowIconLightness.equals("0"))
                                    monochromeValue = 0x00;
                                if (applicationWidgetOneRowIconLightness.equals("25"))
                                    monochromeValue = 0x40;
                                if (applicationWidgetOneRowIconLightness.equals("50"))
                                    monochromeValue = 0x80;
                                if (applicationWidgetOneRowIconLightness.equals("75"))
                                    monochromeValue = 0xC0;
                                //if (applicationWidgetOneRowIconLightness.equals("100"))
                                //    monochromeValue = 0xFF;
                                Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_widget_restart_events);
                                bitmap = BitmapManipulator.monochromeBitmap(bitmap, monochromeValue);
                                remoteViews.setImageViewBitmap(R.id.widget_one_row_header_restart_events, bitmap);
                            }
                            else
                                remoteViews.setImageViewResource(R.id.widget_one_row_header_restart_events, R.drawable.ic_widget_restart_events);
                        }

                        if (Event.getGlobalEventsRunning(context) && PPApplication.getApplicationStarted(context, true)) {
                            remoteViews.setViewVisibility(R.id.widget_one_row_header_restart_events, View.VISIBLE);
                            Intent intentRE = new Intent(context, RestartEventsFromNotificationActivity.class);
                            PendingIntent pIntentRE = PendingIntent.getActivity(context, 2, intentRE, PendingIntent.FLAG_UPDATE_CURRENT);
                            remoteViews.setOnClickPendingIntent(R.id.widget_one_row_header_restart_events, pIntentRE);
                        } else
                            remoteViews.setViewVisibility(R.id.widget_one_row_header_restart_events, View.GONE);

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

                dataWrapper.invalidateDataWrapper();
            }
        });
    }

}
