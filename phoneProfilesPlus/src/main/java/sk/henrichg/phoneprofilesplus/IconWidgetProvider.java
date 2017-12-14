package sk.henrichg.phoneprofilesplus;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.widget.RemoteViews;

public class IconWidgetProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds)
    {
        int monochromeValue = 0xFF;
        String applicationWidgetIconLightness = ApplicationPreferences.applicationWidgetIconLightness(context);
        if (applicationWidgetIconLightness.equals("0")) monochromeValue = 0x00;
        if (applicationWidgetIconLightness.equals("25")) monochromeValue = 0x40;
        if (applicationWidgetIconLightness.equals("50")) monochromeValue = 0x80;
        if (applicationWidgetIconLightness.equals("75")) monochromeValue = 0xC0;
        if (applicationWidgetIconLightness.equals("100")) monochromeValue = 0xFF;

        DataWrapper dataWrapper = new DataWrapper(context, true,
                                                ApplicationPreferences.applicationWidgetIconColor(context).equals("1"),
                                                monochromeValue);

        Profile profile = dataWrapper.getActivatedProfile();

        // get all IconWidgetProvider widgets in launcher
        ComponentName thisWidget = new ComponentName(context, IconWidgetProvider.class);
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
                profileName = DataWrapper.getProfileNameWithManualIndicator(profile, false, true, true, dataWrapper);
            }
            else
            {
                // create empty profile and set icon resource
                profile = new Profile();
                profile._name = context.getResources().getString(R.string.profiles_header_profile_name_no_activated);
                profile._icon = Profile.PROFILE_ICON_DEFAULT+"|1|0|0";

                profile.generateIconBitmap(context,
                        ApplicationPreferences.applicationWidgetListIconColor(context).equals("1"), monochromeValue);
                isIconResourceID = profile.getIsIconResourceID();
                iconIdentifier = profile.getIconIdentifier();
                profileName = profile._name;
            }

            // prepare view for widget update
            RemoteViews remoteViews;
            if (ApplicationPreferences.applicationWidgetIconHideProfileName(context))
                remoteViews = new RemoteViews(context.getPackageName(), R.layout.icon_widget_no_profile_name);
            else {
                if (profile._duration > 0)
                    remoteViews = new RemoteViews(context.getPackageName(), R.layout.icon_widget);
                else
                    remoteViews = new RemoteViews(context.getPackageName(), R.layout.icon_widget_one_line_text);
            }

            // set background
            int red = 0;
            int green;
            int blue;
            String applicationWidgetIconLightnessB = ApplicationPreferences.applicationWidgetIconLightnessB(context);
            if (applicationWidgetIconLightnessB.equals("0")) red = 0x00;
            if (applicationWidgetIconLightnessB.equals("25")) red = 0x40;
            if (applicationWidgetIconLightnessB.equals("50")) red = 0x80;
            if (applicationWidgetIconLightnessB.equals("75")) red = 0xC0;
            if (applicationWidgetIconLightnessB.equals("100")) red = 0xFF;
            green = red; blue = red;
            int alpha = 0x40;
            String applicationWidgetIconBackground = ApplicationPreferences.applicationWidgetIconBackground(context);
            if (applicationWidgetIconBackground.equals("0")) alpha = 0x00;
            if (applicationWidgetIconBackground.equals("25")) alpha = 0x40;
            if (applicationWidgetIconBackground.equals("50")) alpha = 0x80;
            if (applicationWidgetIconBackground.equals("75")) alpha = 0xC0;
            if (applicationWidgetIconBackground.equals("100")) alpha = 0xFF;
            boolean roundedCorners = ApplicationPreferences.applicationWidgetIconRoundedCorners(context);
            if (roundedCorners) {
                remoteViews.setInt(R.id.widget_icon_root, "setBackgroundColor", 0x00000000);
                remoteViews.setInt(R.id.widget_icon_background, "setColorFilter", Color.argb(alpha, red, green, blue));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                    remoteViews.setInt(R.id.widget_icon_background, "setImageAlpha", alpha);
                else
                    remoteViews.setInt(R.id.widget_icon_background, "setAlpha", alpha);
            }
            else {
                remoteViews.setInt(R.id.widget_icon_root, "setBackgroundColor", Color.argb(alpha, red, green, blue));
                remoteViews.setInt(R.id.widget_icon_background, "setColorFilter", 0x00000000);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                    remoteViews.setInt(R.id.widget_icon_background, "setImageAlpha", 0);
                else
                    remoteViews.setInt(R.id.widget_icon_background, "setAlpha", 0);
            }

            if (isIconResourceID)
            {
                if (profile._iconBitmap != null)
                    remoteViews.setImageViewBitmap(R.id.icon_widget_icon, profile._iconBitmap);
                else {
                    int iconResource = context.getResources().getIdentifier(iconIdentifier, "drawable", context.getPackageName());
                    remoteViews.setImageViewResource(R.id.icon_widget_icon, iconResource);
                }
            }
            else
            {
                remoteViews.setImageViewBitmap(R.id.icon_widget_icon, profile._iconBitmap);
            }

            red = 0xFF;
            String applicationWidgetIconLightnessT = ApplicationPreferences.applicationWidgetIconLightnessT(context);
            if (applicationWidgetIconLightnessT.equals("0")) red = 0x00;
            if (applicationWidgetIconLightnessT.equals("25")) red = 0x40;
            if (applicationWidgetIconLightnessT.equals("50")) red = 0x80;
            if (applicationWidgetIconLightnessT.equals("75")) red = 0xC0;
            if (applicationWidgetIconLightnessT.equals("100")) red = 0xFF;
            green = red; blue = red;
            remoteViews.setTextColor(R.id.icon_widget_name, Color.argb(0xFF, red, green, blue));

            if (!ApplicationPreferences.applicationWidgetIconHideProfileName(context))
                remoteViews.setTextViewText(R.id.icon_widget_name, profileName);

            // intent for start LauncherActivity on widget click
            Intent intent = new Intent(context, LauncherActivity.class);
            intent.putExtra(PPApplication.EXTRA_STARTUP_SOURCE, PPApplication.STARTUP_SOURCE_WIDGET);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            remoteViews.setOnClickPendingIntent(R.id.icon_widget_icon, pendingIntent);
            remoteViews.setOnClickPendingIntent(R.id.icon_widget_name, pendingIntent);

            // widget update
            try {
                appWidgetManager.updateAppWidget(widgetId, remoteViews);
            } catch (Exception ignored) {}
        }

        dataWrapper.invalidateDataWrapper();
    }
}
