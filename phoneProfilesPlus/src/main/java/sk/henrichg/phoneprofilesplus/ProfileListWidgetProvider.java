package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.RemoteViews;

@SuppressLint("NewApi")
public class ProfileListWidgetProvider extends AppWidgetProvider {

    private DataWrapper dataWrapper;

    public static final String INTENT_REFRESH_LISTWIDGET = "sk.henrichg.phoneprofilesplus.REFRESH_LISTWIDGET";

    private boolean isLargeLayout;
    private boolean isKeyguard;

    private RemoteViews buildLayout(Context context, /*AppWidgetManager appWidgetManager,*/ int appWidgetId, boolean largeLayout)
    {
        Intent svcIntent=new Intent(context, ProfileListWidgetService.class);

        svcIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        svcIntent.setData(Uri.parse(svcIntent.toUri(Intent.URI_INTENT_SCHEME)));

        RemoteViews widget;

        if (largeLayout)
        {
            if (ApplicationPreferences.applicationWidgetListHeader(context))
            {
                if (!ApplicationPreferences.applicationWidgetListGridLayout(context))
                {
                    if (ApplicationPreferences.applicationWidgetListPrefIndicator(context))
                        widget=new RemoteViews(context.getPackageName(), R.layout.profile_list_widget);
                    else
                        widget=new RemoteViews(context.getPackageName(), R.layout.profile_list_widget_no_indicator);
                }
                else
                {
                    if (ApplicationPreferences.applicationWidgetListPrefIndicator(context))
                        widget=new RemoteViews(context.getPackageName(), R.layout.profile_grid_widget);
                    else
                        widget=new RemoteViews(context.getPackageName(), R.layout.profile_grid_widget_no_indicator);
                }
            }
            else
            {
                if (!ApplicationPreferences.applicationWidgetListGridLayout(context))
                    widget=new RemoteViews(context.getPackageName(), R.layout.profile_list_widget_no_header);
                else
                    widget=new RemoteViews(context.getPackageName(), R.layout.profile_grid_widget_no_header);
            }
        }
        else
        {
            if (isKeyguard)
            {
                if (ApplicationPreferences.applicationWidgetListPrefIndicator(context))
                    widget=new RemoteViews(context.getPackageName(), R.layout.profile_list_widget_small_keyguard);
                else
                    widget=new RemoteViews(context.getPackageName(), R.layout.profile_list_widget_small_no_indicator_keyguard);
            }
            else
            {
                if (ApplicationPreferences.applicationWidgetListPrefIndicator(context))
                    widget=new RemoteViews(context.getPackageName(), R.layout.profile_list_widget_small);
                else
                    widget=new RemoteViews(context.getPackageName(), R.layout.profile_list_widget_small_no_indicator);
            }
        }

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
            widget.setInt(R.id.widget_profile_list_root, "setBackgroundColor", 0x00000000);
            widget.setInt(R.id.widget_profile_list_background, "setColorFilter", Color.argb(alpha, red, green, blue));
            //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                widget.setInt(R.id.widget_profile_list_background, "setImageAlpha", alpha);
            //else
            //    widget.setInt(R.id.widget_profile_list_background, "setAlpha", alpha);
        }
        else {
            widget.setInt(R.id.widget_profile_list_root, "setBackgroundColor", Color.argb(alpha, red, green, blue));
            widget.setInt(R.id.widget_profile_list_background, "setColorFilter", 0x00000000);
            //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                widget.setInt(R.id.widget_profile_list_background, "setImageAlpha", 0);
            //else
            //    widget.setInt(R.id.widget_profile_list_background, "setAlpha", 0);
        }


        // header
        if (ApplicationPreferences.applicationWidgetListHeader(context) || (!largeLayout))
        {
            int monochromeValue = 0xFF;
            String applicationWidgetListIconLightness = ApplicationPreferences.applicationWidgetListIconLightness(context);
            if (applicationWidgetListIconLightness.equals("0")) monochromeValue = 0x00;
            if (applicationWidgetListIconLightness.equals("25")) monochromeValue = 0x40;
            if (applicationWidgetListIconLightness.equals("50")) monochromeValue = 0x80;
            if (applicationWidgetListIconLightness.equals("75")) monochromeValue = 0xC0;
            if (applicationWidgetListIconLightness.equals("100")) monochromeValue = 0xFF;

            Profile profile = DatabaseHandler.getInstance(dataWrapper.context).getActivatedProfile();

            boolean isIconResourceID;
            String iconIdentifier;
            String profileName;
            if (profile != null)
            {
                profile.generateIconBitmap(context,
                        ApplicationPreferences.applicationWidgetListIconColor(context).equals("1"),
                        monochromeValue);
                if (ApplicationPreferences.applicationWidgetListPrefIndicator(context))
                    profile.generatePreferencesIndicator(context,
                        ApplicationPreferences.applicationWidgetListIconColor(context).equals("1"),
                        monochromeValue);
                isIconResourceID = profile.getIsIconResourceID();
                iconIdentifier = profile.getIconIdentifier();
                profileName = DataWrapper.getProfileNameWithManualIndicator(profile, true, true, false, dataWrapper, false);
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
                if (ApplicationPreferences.applicationWidgetListPrefIndicator(context))
                    profile.generatePreferencesIndicator(context,
                        ApplicationPreferences.applicationWidgetListIconColor(context).equals("1"),
                        monochromeValue);
                isIconResourceID = profile.getIsIconResourceID();
                iconIdentifier = profile.getIconIdentifier();
                profileName = profile._name;
            }
            if (isIconResourceID)
            {
                if (profile._iconBitmap != null)
                    widget.setImageViewBitmap(R.id.widget_profile_list_header_profile_icon, profile._iconBitmap);
                else {
                    //int iconResource = context.getResources().getIdentifier(iconIdentifier, "drawable", context.getPackageName());
                    int iconResource = Profile.getIconResource(iconIdentifier);
                    widget.setImageViewResource(R.id.widget_profile_list_header_profile_icon, iconResource);
                }
            }
            else
            {
                widget.setImageViewBitmap(R.id.widget_profile_list_header_profile_icon, profile._iconBitmap);
            }

            red = 0xFF;
            String applicationWidgetListLightnessT = ApplicationPreferences.applicationWidgetListLightnessT(context);
            if (applicationWidgetListLightnessT.equals("0")) red = 0x00;
            if (applicationWidgetListLightnessT.equals("25")) red = 0x40;
            if (applicationWidgetListLightnessT.equals("50")) red = 0x80;
            if (applicationWidgetListLightnessT.equals("75")) red = 0xC0;
            if (applicationWidgetListLightnessT.equals("100")) red = 0xFF;
            green = red; blue = red;
            widget.setTextColor(R.id.widget_profile_list_header_profile_name, Color.argb(0xFF, red, green, blue));

            widget.setTextViewText(R.id.widget_profile_list_header_profile_name, profileName);
            if (ApplicationPreferences.applicationWidgetListPrefIndicator(context))
            {
                if (profile._preferencesIndicator != null)
                    widget.setImageViewBitmap(R.id.widget_profile_list_header_profile_pref_indicator, profile._preferencesIndicator);
                else
                    widget.setImageViewResource(R.id.widget_profile_list_header_profile_pref_indicator, R.drawable.ic_empty);
            }
            if (largeLayout)
            {
                red = 0xFF;
                if (applicationWidgetListLightnessT.equals("0")) red = 0x00;
                if (applicationWidgetListLightnessT.equals("25")) red = 0x40;
                if (applicationWidgetListLightnessT.equals("50")) red = 0x80;
                if (applicationWidgetListLightnessT.equals("75")) red = 0xC0;
                if (applicationWidgetListLightnessT.equals("100")) red = 0xFF;
                green = red; blue = red;
                widget.setInt(R.id.widget_profile_list_header_separator, "setBackgroundColor", Color.argb(0xFF, red, green, blue));
            }

            if (Event.getGlobalEventsRunning(context)) {
                if (ApplicationPreferences.applicationWidgetListIconColor(context).equals("1")) {
                    monochromeValue = 0xFF;
                    if (applicationWidgetListIconLightness.equals("0")) monochromeValue = 0x00;
                    if (applicationWidgetListIconLightness.equals("25")) monochromeValue = 0x40;
                    if (applicationWidgetListIconLightness.equals("50")) monochromeValue = 0x80;
                    if (applicationWidgetListIconLightness.equals("75")) monochromeValue = 0xC0;
                    if (applicationWidgetListIconLightness.equals("100")) monochromeValue = 0xFF;
                    Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_button_restart_events);
                    bitmap = BitmapManipulator.monochromeBitmap(bitmap, monochromeValue);
                    widget.setImageViewBitmap(R.id.widget_profile_list_header_restart_events, bitmap);
                }
            }

        }
        ////////////////////////////////////////////////

        // clicks
        if (largeLayout)
        {
            Intent intent = new Intent(context, EditorProfilesActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 1, intent,
                                                        PendingIntent.FLAG_UPDATE_CURRENT);
            widget.setOnClickPendingIntent(R.id.widget_profile_list_header_profile_root, pendingIntent);

            if (Event.getGlobalEventsRunning(context)) {
                widget.setViewVisibility(R.id.widget_profile_list_header_restart_events, View.VISIBLE);
                Intent intentRE = new Intent(context, RestartEventsFromNotificationActivity.class);
                PendingIntent pIntentRE = PendingIntent.getActivity(context, 2, intentRE, PendingIntent.FLAG_UPDATE_CURRENT);
                widget.setOnClickPendingIntent(R.id.widget_profile_list_header_restart_events, pIntentRE);
            }
            else
                widget.setViewVisibility(R.id.widget_profile_list_header_restart_events, View.GONE);

            if (!ApplicationPreferences.applicationWidgetListGridLayout(context))
                widget.setRemoteAdapter(R.id.widget_profile_list, svcIntent);
            else
                widget.setRemoteAdapter(R.id.widget_profile_grid, svcIntent);

            // The empty view is displayed when the collection has no items.
            // It should be in the same layout used to instantiate the RemoteViews
            // object above.
            if (!ApplicationPreferences.applicationWidgetListGridLayout(context))
                widget.setEmptyView(R.id.widget_profile_list, R.id.widget_profiles_list_empty);
            else
                widget.setEmptyView(R.id.widget_profile_grid, R.id.widget_profiles_list_empty);

            Intent clickIntent=new Intent(context, BackgroundActivateProfileActivity.class);
            clickIntent.putExtra(PPApplication.EXTRA_STARTUP_SOURCE, PPApplication.STARTUP_SOURCE_WIDGET);
            PendingIntent clickPI=PendingIntent.getActivity(context, 300,
                                                        clickIntent,
                                                        PendingIntent.FLAG_UPDATE_CURRENT);

            if (!ApplicationPreferences.applicationWidgetListGridLayout(context))
                widget.setPendingIntentTemplate(R.id.widget_profile_list, clickPI);
            else
                widget.setPendingIntentTemplate(R.id.widget_profile_grid, clickPI);
        }
        else
        {
            Intent intent = new Intent(context, LauncherActivity.class);
            // clear all opened activities
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(PPApplication.EXTRA_STARTUP_SOURCE, PPApplication.STARTUP_SOURCE_WIDGET);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 300, intent,
                                                        PendingIntent.FLAG_UPDATE_CURRENT);
            widget.setOnClickPendingIntent(R.id.widget_profile_list_header, pendingIntent);

            if (Event.getGlobalEventsRunning(context)) {
                widget.setViewVisibility(R.id.widget_profile_list_header_restart_events, View.VISIBLE);
                Intent intentRE = new Intent(context, RestartEventsFromNotificationActivity.class);
                PendingIntent pIntentRE = PendingIntent.getActivity(context, 2, intentRE, PendingIntent.FLAG_UPDATE_CURRENT);
                widget.setOnClickPendingIntent(R.id.widget_profile_list_header_restart_events, pIntentRE);
            }
            else
                widget.setViewVisibility(R.id.widget_profile_list_header_restart_events, View.GONE);
        }

        return widget;
    }

    private void createProfilesDataWrapper(Context context)
    {
        if (dataWrapper == null)
        {
            dataWrapper = new DataWrapper(context.getApplicationContext(), false, 0);
        }
    }

    private void doOnUpdate(Context context, AppWidgetManager appWidgetManager, int appWidgetId)
    {
        Bundle myOptions;
        myOptions = appWidgetManager.getAppWidgetOptions (appWidgetId);
        setLayoutParams(context, appWidgetManager, appWidgetId, myOptions);
        RemoteViews widget = buildLayout(context, appWidgetId, isLargeLayout);
        try {
            appWidgetManager.updateAppWidget(appWidgetId, widget);
        } catch (Exception ignored) {}
    }

    @Override
    public void onUpdate(final Context context, final AppWidgetManager appWidgetManager, final int[] appWidgetIds)
    {
        super.onUpdate(context, appWidgetManager, appWidgetIds);

        PPApplication.startHandlerThreadWidget();
        final Handler handler = new Handler(PPApplication.handlerThreadWidget.getLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                createProfilesDataWrapper(context);

                for (int appWidgetId : appWidgetIds) {
                    doOnUpdate(context, appWidgetManager, appWidgetId);
                }

                if (dataWrapper != null)
                    dataWrapper.invalidateDataWrapper();
                dataWrapper = null;
            }
        });

        /*
        if (PPApplication.widgetHandler != null) {
            PPApplication.widgetHandler.post(new Runnable() {
                public void run() {
                    createProfilesDataWrapper(context);

                    for (int appWidgetId : appWidgetIds) {
                        doOnUpdate(context, appWidgetManager, appWidgetId);
                    }

                    if (dataWrapper != null)
                        dataWrapper.invalidateDataWrapper();
                    dataWrapper = null;
                }
            });
        }
        */
    }

    @Override
    public void onReceive(final Context context, final Intent intent) {
        super.onReceive(context, intent);

        PPApplication.startHandlerThreadWidget();
        final Handler handler = new Handler(PPApplication.handlerThreadWidget.getLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                String action = intent.getAction();

                int appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                        AppWidgetManager.INVALID_APPWIDGET_ID);

                createProfilesDataWrapper(context);

                if ((action != null) &&
                        (action.equalsIgnoreCase("com.motorola.blur.home.ACTION_SET_WIDGET_SIZE")))
                {
                    int spanX = intent.getIntExtra("spanX", 1);
                    int spanY = intent.getIntExtra("spanY", 1);

                    AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
                    setLayoutParamsMotorola(context, spanX, spanY, appWidgetId);
                    RemoteViews layout;
                    layout = buildLayout(context, appWidgetId, isLargeLayout);
                    try {
                        appWidgetManager.updateAppWidget(appWidgetId, layout);
                    } catch (Exception ignored) {}
                }
                else
                if ((action != null) &&
                        (action.equalsIgnoreCase(INTENT_REFRESH_LISTWIDGET)))
                    updateWidgets(context);

                if (dataWrapper != null)
                    dataWrapper.invalidateDataWrapper();
                dataWrapper = null;
            }
        });

        /*
        if (PPApplication.widgetHandler != null) {
            PPApplication.widgetHandler.post(new Runnable() {
                public void run() {
                    String action = intent.getAction();

                    int appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                            AppWidgetManager.INVALID_APPWIDGET_ID);

                    createProfilesDataWrapper(context);

                    if ((action != null) &&
                            (action.equalsIgnoreCase("com.motorola.blur.home.ACTION_SET_WIDGET_SIZE")))
                    {
                        int spanX = intent.getIntExtra("spanX", 1);
                        int spanY = intent.getIntExtra("spanY", 1);

                        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
                        setLayoutParamsMotorola(context, spanX, spanY, appWidgetId);
                        RemoteViews layout;
                        layout = buildLayout(context, appWidgetId, isLargeLayout);
                        try {
                            appWidgetManager.updateAppWidget(appWidgetId, layout);
                        } catch (Exception ignored) {}
                    }
                    else
                    if ((action != null) &&
                            (action.equalsIgnoreCase(INTENT_REFRESH_LISTWIDGET)))
                        updateWidgets(context);

                    if (dataWrapper != null)
                        dataWrapper.invalidateDataWrapper();
                    dataWrapper = null;
                }
            });
        }
        */
    }

    private void setLayoutParams(Context context, AppWidgetManager appWidgetManager,
            int appWidgetId, Bundle newOptions)
    {
        String preferenceKey = "isLargeLayout_"+appWidgetId;
        ApplicationPreferences.getSharedPreferences(context);

        AppWidgetProviderInfo appWidgetProviderInfo = appWidgetManager.getAppWidgetInfo(appWidgetId);

        int minHeight;
        if (newOptions != null)
        {
            // Get the value of OPTION_APPWIDGET_HOST_CATEGORY
            int category = newOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_HOST_CATEGORY, -1);
            // If the value is WIDGET_CATEGORY_KEYGUARD, it's a lock screen widget
            isKeyguard = category == AppWidgetProviderInfo.WIDGET_CATEGORY_KEYGUARD;

            //int minWidth = newOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH);
            //int maxWidth = newOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH);
            minHeight = newOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT);
            //int maxHeight = newOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT);

            if ((minHeight == 0) && (appWidgetProviderInfo != null))
            {
                minHeight = appWidgetProviderInfo.minHeight;
            }

        }
        else
        {
            isKeyguard = false;
            if (appWidgetProviderInfo != null)
                minHeight = appWidgetProviderInfo.minHeight;
            else
                minHeight = 0;

            //if (minHeight == 0)
            //	return;
        }

        if (isKeyguard)
        {
            isLargeLayout = minHeight >= 250;
        }
        else
        {
            isLargeLayout = minHeight >= 110;
        }
        
        if (ApplicationPreferences.preferences.contains(preferenceKey))
            isLargeLayout = ApplicationPreferences.preferences.getBoolean(preferenceKey, true);
        else
        {
            Editor editor = ApplicationPreferences.preferences.edit();
            editor.putBoolean(preferenceKey, isLargeLayout);
            editor.apply();
        }
        
    }

    private void setLayoutParamsMotorola(Context context, @SuppressWarnings("unused") int spanX, int spanY, int appWidgetId)
    {
        isKeyguard = false;
        isLargeLayout = spanY != 1;
        
        String preferenceKey = "isLargeLayout_"+appWidgetId;
        ApplicationPreferences.getSharedPreferences(context);

        Editor editor = ApplicationPreferences.preferences.edit();
        editor.putBoolean(preferenceKey, isLargeLayout);
        editor.apply();
    }

    @Override
    public void onAppWidgetOptionsChanged(final Context context, final AppWidgetManager appWidgetManager,
            final int appWidgetId, final Bundle newOptions)
    {
        PPApplication.startHandlerThreadWidget();
        final Handler handler = new Handler(PPApplication.handlerThreadWidget.getLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                createProfilesDataWrapper(context);

                String preferenceKey = "isLargeLayout_"+appWidgetId;
                ApplicationPreferences.getSharedPreferences(context);

                // remove preference, will by reset in setLayoutParams
                Editor editor = ApplicationPreferences.preferences.edit();
                editor.remove(preferenceKey);
                editor.apply();


                updateWidget(context, appWidgetId);

                if (dataWrapper != null)
                    dataWrapper.invalidateDataWrapper();
                dataWrapper = null;
            }
        });

        /*
        if (PPApplication.widgetHandler != null) {
            PPApplication.widgetHandler.post(new Runnable() {
                public void run() {
                    createProfilesDataWrapper(context);

                    String preferenceKey = "isLargeLayout_"+appWidgetId;
                    ApplicationPreferences.getSharedPreferences(context);

                    // remove preference, will by reset in setLayoutParams
                    Editor editor = ApplicationPreferences.preferences.edit();
                    editor.remove(preferenceKey);
                    editor.apply();


                    updateWidget(context, appWidgetId);

                    if (dataWrapper != null)
                        dataWrapper.invalidateDataWrapper();
                    dataWrapper = null;
                }
            });
        }
        */
    }

    private void updateWidget(Context context, int appWidgetId) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);

        doOnUpdate(context, appWidgetManager, appWidgetId);
        if (isLargeLayout)
        {
            if (!ApplicationPreferences.applicationWidgetListGridLayout(context))
                appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.widget_profile_list);
            else
                appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.widget_profile_grid);
        }
    }

    private void updateWidgets(Context context) {
        try {
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            int appWidgetIds[] = appWidgetManager.getAppWidgetIds(new ComponentName(context, ProfileListWidgetProvider.class));

            for (int appWidgetId : appWidgetIds) {
                updateWidget(context, appWidgetId);
            }
        } catch (Exception ignored) {}
    }

}
