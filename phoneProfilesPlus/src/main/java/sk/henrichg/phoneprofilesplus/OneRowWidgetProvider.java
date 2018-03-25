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
import android.widget.RemoteViews;

public class OneRowWidgetProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds)
    {
        int monochromeValue = 0xFF;
        String applicationWidgetListIconLightness = ApplicationPreferences.applicationWidgetListIconLightness(context);
        if (applicationWidgetListIconLightness.equals("0")) monochromeValue = 0x00;
        if (applicationWidgetListIconLightness.equals("25")) monochromeValue = 0x40;
        if (applicationWidgetListIconLightness.equals("50")) monochromeValue = 0x80;
        if (applicationWidgetListIconLightness.equals("75")) monochromeValue = 0xC0;
        if (applicationWidgetListIconLightness.equals("100")) monochromeValue = 0xFF;

        DataWrapper dataWrapper = new DataWrapper(context,  ApplicationPreferences.applicationWidgetListIconColor(context).equals("1"), monochromeValue);

        Profile profile = dataWrapper.getActivatedProfile(true,
                                            ApplicationPreferences.applicationWidgetListPrefIndicator(context));

        // get all OneRowWidgetProvider widgets in launcher
        ComponentName thisWidget = new ComponentName(context, OneRowWidgetProvider.class);
        int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);

        for (int widgetId : allWidgetIds)
        {
            boolean isIconResourceID;
            String iconIdentifier;
            String profileName;
            if (profile != null)
            {
                isIconResourceID = profile.getIsIconResourceID();
                iconIdentifier = profile.getIconIdentifier();
                profileName = DataWrapper.getProfileNameWithManualIndicator(profile, true, true, false, dataWrapper);
            }
            else
            {
                // create empty profile and set icon resource
                profile = new Profile();
                profile._name = context.getResources().getString(R.string.profiles_header_profile_name_no_activated);
                profile._icon = Profile.PROFILE_ICON_DEFAULT+"|1|0|0";

                profile.generateIconBitmap(context,
                        ApplicationPreferences.applicationWidgetListIconColor(context).equals("1"),
                        monochromeValue);
                isIconResourceID = profile.getIsIconResourceID();
                iconIdentifier = profile.getIconIdentifier();
                profileName = profile._name;
            }

            RemoteViews remoteViews;
            if (ApplicationPreferences.applicationWidgetListPrefIndicator(context))
                remoteViews = new RemoteViews(context.getPackageName(), R.layout.one_row_widget);
            else
                remoteViews = new RemoteViews(context.getPackageName(), R.layout.one_row_widget_no_indicator);


            // set background
            int red = 0;
            int green;
            int blue;
            if (ApplicationPreferences.applicationWidgetListBackgroundType(context)) {
                int bgColor = Integer.valueOf(ApplicationPreferences.applicationWidgetListBackgroundColor(context));
                red = Color.red(bgColor);
                green = Color.green(bgColor);
                blue = Color.blue(bgColor);
            }
            else {
                String applicationWidgetListLightnessB = ApplicationPreferences.applicationWidgetListLightnessB(context);
                if (applicationWidgetListLightnessB.equals("0")) red = 0x00;
                if (applicationWidgetListLightnessB.equals("25")) red = 0x40;
                if (applicationWidgetListLightnessB.equals("50")) red = 0x80;
                if (applicationWidgetListLightnessB.equals("75")) red = 0xC0;
                if (applicationWidgetListLightnessB.equals("100")) red = 0xFF;
                green = red;
                blue = red;
            }
            int alpha = 0x40;
            String applicationWidgetListBackground = ApplicationPreferences.applicationWidgetListBackground(context);
            if (applicationWidgetListBackground.equals("0")) alpha = 0x00;
            if (applicationWidgetListBackground.equals("25")) alpha = 0x40;
            if (applicationWidgetListBackground.equals("50")) alpha = 0x80;
            if (applicationWidgetListBackground.equals("75")) alpha = 0xC0;
            if (applicationWidgetListBackground.equals("100")) alpha = 0xFF;
            boolean roundedCorners = ApplicationPreferences.applicationWidgetListRoundedCorners(context);
            if (roundedCorners) {
                remoteViews.setInt(R.id.widget_one_row_root, "setBackgroundColor", 0x00000000);
                remoteViews.setInt(R.id.widget_one_row_background, "setColorFilter", Color.argb(alpha, red, green, blue));
                //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                    remoteViews.setInt(R.id.widget_one_row_background, "setImageAlpha", alpha);
                //else
                //    remoteViews.setInt(R.id.widget_one_row_background, "setAlpha", alpha);
            }
            else {
                remoteViews.setInt(R.id.widget_one_row_root, "setBackgroundColor", Color.argb(alpha, red, green, blue));
                remoteViews.setInt(R.id.widget_one_row_background, "setColorFilter", 0x00000000);
                //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                    remoteViews.setInt(R.id.widget_one_row_background, "setImageAlpha", 0);
                //else
                //    remoteViews.setInt(R.id.widget_one_row_background, "setAlpha", 0);
            }

            if (isIconResourceID)
            {
                if (profile._iconBitmap != null)
                    remoteViews.setImageViewBitmap(R.id.widget_one_row_header_profile_icon, profile._iconBitmap);
                else {
                    //remoteViews.setImageViewResource(R.id.activate_profile_widget_icon, 0);
                    int iconResource = context.getResources().getIdentifier(iconIdentifier, "drawable", context.getPackageName());
                    remoteViews.setImageViewResource(R.id.widget_one_row_header_profile_icon, iconResource);
                }
            }
            else
            {
                remoteViews.setImageViewBitmap(R.id.widget_one_row_header_profile_icon, profile._iconBitmap);
            }

            red = 0xFF;
            String applicationWidgetListLightnessT = ApplicationPreferences.applicationWidgetListLightnessT(context);
            if (applicationWidgetListLightnessT.equals("0")) red = 0x00;
            if (applicationWidgetListLightnessT.equals("25")) red = 0x40;
            if (applicationWidgetListLightnessT.equals("50")) red = 0x80;
            if (applicationWidgetListLightnessT.equals("75")) red = 0xC0;
            if (applicationWidgetListLightnessT.equals("100")) red = 0xFF;
            green = red; blue = red;
            remoteViews.setTextColor(R.id.widget_one_row_header_profile_name, Color.argb(0xFF, red, green, blue));

            remoteViews.setTextViewText(R.id.widget_one_row_header_profile_name, profileName);
            if (ApplicationPreferences.applicationWidgetListPrefIndicator(context))
            {
                if (profile._preferencesIndicator == null)
                    remoteViews.setImageViewResource(R.id.widget_one_row_header_profile_pref_indicator, R.drawable.ic_empty);
                else
                    remoteViews.setImageViewBitmap(R.id.widget_one_row_header_profile_pref_indicator, profile._preferencesIndicator);
            }

            if (ApplicationPreferences.applicationWidgetListIconColor(context).equals("1")) {
                monochromeValue = 0xFF;
                if (applicationWidgetListIconLightness.equals("0")) monochromeValue = 0x00;
                if (applicationWidgetListIconLightness.equals("25")) monochromeValue = 0x40;
                if (applicationWidgetListIconLightness.equals("50")) monochromeValue = 0x80;
                if (applicationWidgetListIconLightness.equals("75")) monochromeValue = 0xC0;
                if (applicationWidgetListIconLightness.equals("100")) monochromeValue = 0xFF;
                Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_action_events_restart_notification);
                bitmap = BitmapManipulator.monochromeBitmap(bitmap, monochromeValue);
                remoteViews.setImageViewBitmap(R.id.widget_one_row_header_restart_events, bitmap);
            }

            Intent intentRE = new Intent(context, RestartEventsFromNotificationActivity.class);
            PendingIntent pIntentRE = PendingIntent.getActivity(context, 0, intentRE, PendingIntent.FLAG_CANCEL_CURRENT);
            remoteViews.setOnClickPendingIntent(R.id.widget_one_row_header_restart_events, pIntentRE);

            // intent for start LauncherActivity on widget click
            Intent intent = new Intent(context, LauncherActivity.class);
            intent.putExtra(PPApplication.EXTRA_STARTUP_SOURCE, PPApplication.STARTUP_SOURCE_WIDGET);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            remoteViews.setOnClickPendingIntent(R.id.widget_one_row_header, pendingIntent);

            // widget update
            try {
                appWidgetManager.updateAppWidget(widgetId, remoteViews);
            } catch (Exception ignored) {}
        }

        dataWrapper.invalidateDataWrapper();
    }

}
