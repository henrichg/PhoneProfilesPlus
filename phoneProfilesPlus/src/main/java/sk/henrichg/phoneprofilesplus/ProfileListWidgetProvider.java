package sk.henrichg.phoneprofilesplus;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.View;
import android.widget.RemoteViews;

public class ProfileListWidgetProvider extends AppWidgetProvider {

    private DataWrapper dataWrapper;

    private static final String INTENT_REFRESH_LISTWIDGET = PPApplication.PACKAGE_NAME + ".REFRESH_LISTWIDGET";

    private boolean isLargeLayout;
    private boolean isKeyguard;

    private RemoteViews buildLayout(Context context, /*AppWidgetManager appWidgetManager,*/ int appWidgetId, boolean largeLayout)
    {
        boolean applicationWidgetListHeader = ApplicationPreferences.applicationWidgetListHeader(context);
        boolean applicationWidgetListGridLayout = ApplicationPreferences.applicationWidgetListGridLayout(context);
        boolean applicationWidgetListPrefIndicator = ApplicationPreferences.applicationWidgetListPrefIndicator(context);
        boolean applicationWidgetListBackgroundType = ApplicationPreferences.applicationWidgetListBackgroundType(context);
        String applicationWidgetListBackgroundColor = ApplicationPreferences.applicationWidgetListBackgroundColor(context);
        String applicationWidgetListLightnessB = ApplicationPreferences.applicationWidgetListLightnessB(context);
        String applicationWidgetListBackground = ApplicationPreferences.applicationWidgetListBackground(context);
        boolean applicationWidgetListShowBorder = ApplicationPreferences.applicationWidgetListShowBorder(context);
        String applicationWidgetListLightnessBorder = ApplicationPreferences.applicationWidgetListLightnessBorder(context);
        boolean applicationWidgetListRoundedCorners = ApplicationPreferences.applicationWidgetListRoundedCorners(context);
        String applicationWidgetListIconLightness = ApplicationPreferences.applicationWidgetListIconLightness(context);
        String applicationWidgetListIconColor = ApplicationPreferences.applicationWidgetListIconColor(context);
        boolean applicationWidgetListCustomIconLightness = ApplicationPreferences.applicationWidgetListCustomIconLightness(context);
        String applicationWidgetListLightnessT = ApplicationPreferences.applicationWidgetListLightnessT(context);

        Intent svcIntent=new Intent(context, ProfileListWidgetService.class);

        svcIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        svcIntent.setData(Uri.parse(svcIntent.toUri(Intent.URI_INTENT_SCHEME)));

        RemoteViews widget;

        if (largeLayout)
        {
            if (applicationWidgetListHeader)
            {
                if (!applicationWidgetListGridLayout)
                {
                    if (applicationWidgetListPrefIndicator)
                        widget=new RemoteViews(context.getPackageName(), R.layout.profile_list_widget);
                    else
                        widget=new RemoteViews(context.getPackageName(), R.layout.profile_list_widget_no_indicator);
                }
                else
                {
                    if (applicationWidgetListPrefIndicator)
                        widget=new RemoteViews(context.getPackageName(), R.layout.profile_grid_widget);
                    else
                        widget=new RemoteViews(context.getPackageName(), R.layout.profile_grid_widget_no_indicator);
                }
            }
            else
            {
                if (!applicationWidgetListGridLayout)
                    widget=new RemoteViews(context.getPackageName(), R.layout.profile_list_widget_no_header);
                else
                    widget=new RemoteViews(context.getPackageName(), R.layout.profile_grid_widget_no_header);
            }
        }
        else
        {
            if (isKeyguard)
            {
                if (applicationWidgetListPrefIndicator)
                    widget=new RemoteViews(context.getPackageName(), R.layout.profile_list_widget_small_keyguard);
                else
                    widget=new RemoteViews(context.getPackageName(), R.layout.profile_list_widget_small_no_indicator_keyguard);
            }
            else
            {
                if (applicationWidgetListPrefIndicator)
                    widget=new RemoteViews(context.getPackageName(), R.layout.profile_list_widget_small);
                else
                    widget=new RemoteViews(context.getPackageName(), R.layout.profile_list_widget_small_no_indicator);
            }
        }

        // set background
        int red = 0x00;
        int green;
        int blue;
        if (applicationWidgetListBackgroundType) {
            int bgColor = Integer.valueOf(applicationWidgetListBackgroundColor);
            red = Color.red(bgColor);
            green = Color.green(bgColor);
            blue = Color.blue(bgColor);
        }
        else {
            //if (applicationWidgetListLightnessB.equals("0")) red = 0x00;
            if (applicationWidgetListLightnessB.equals("25")) red = 0x40;
            if (applicationWidgetListLightnessB.equals("50")) red = 0x80;
            if (applicationWidgetListLightnessB.equals("75")) red = 0xC0;
            if (applicationWidgetListLightnessB.equals("100")) red = 0xFF;
            green = red;
            blue = red;
        }
        int alpha = 0x40;
        if (applicationWidgetListBackground.equals("0")) alpha = 0x00;
        //if (applicationWidgetListBackground.equals("25")) alpha = 0x40;
        if (applicationWidgetListBackground.equals("50")) alpha = 0x80;
        if (applicationWidgetListBackground.equals("75")) alpha = 0xC0;
        if (applicationWidgetListBackground.equals("100")) alpha = 0xFF;
        int redBorder = 0xFF;
        int greenBorder;
        int blueBorder;
        if (applicationWidgetListShowBorder) {
            if (applicationWidgetListLightnessBorder.equals("0")) redBorder = 0x00;
            if (applicationWidgetListLightnessBorder.equals("25")) redBorder = 0x40;
            if (applicationWidgetListLightnessBorder.equals("50")) redBorder = 0x80;
            if (applicationWidgetListLightnessBorder.equals("75")) redBorder = 0xC0;
            //if (applicationWidgetListLightnessBorder.equals("100")) redBorder = 0xFF;
        }
        greenBorder = redBorder;
        blueBorder = redBorder;
        if (applicationWidgetListRoundedCorners) {
            widget.setViewVisibility(R.id.widget_profile_list_background, View.VISIBLE);
            widget.setViewVisibility(R.id.widget_profile_list_not_rounded_border, View.INVISIBLE);
            if (applicationWidgetListShowBorder)
                widget.setViewVisibility(R.id.widget_profile_list_rounded_border, View.VISIBLE);
            else
                widget.setViewVisibility(R.id.widget_profile_list_rounded_border, View.INVISIBLE);
            widget.setInt(R.id.widget_profile_list_root, "setBackgroundColor", 0x00000000);
            widget.setInt(R.id.widget_profile_list_background, "setColorFilter", Color.argb(0xFF, red, green, blue));
            //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                widget.setInt(R.id.widget_profile_list_background, "setImageAlpha", alpha);
            //else
            //    widget.setInt(R.id.widget_profile_list_background, "setAlpha", alpha);
            if (applicationWidgetListShowBorder)
                widget.setInt(R.id.widget_profile_list_rounded_border, "setColorFilter", Color.argb(0xFF, redBorder, greenBorder, blueBorder));
        }
        else {
            widget.setViewVisibility(R.id.widget_profile_list_background, View.INVISIBLE);
            widget.setViewVisibility(R.id.widget_profile_list_rounded_border, View.INVISIBLE);
            if (applicationWidgetListShowBorder)
                widget.setViewVisibility(R.id.widget_profile_list_not_rounded_border, View.VISIBLE);
            else
                widget.setViewVisibility(R.id.widget_profile_list_not_rounded_border, View.INVISIBLE);
            widget.setInt(R.id.widget_profile_list_root, "setBackgroundColor", Color.argb(alpha, red, green, blue));
            /*widget.setInt(R.id.widget_profile_list_background, "setColorFilter", 0x00000000);
            //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                widget.setInt(R.id.widget_profile_list_background, "setImageAlpha", 0);
            //else
            //    widget.setInt(R.id.widget_profile_list_background, "setAlpha", 0);*/
            if (applicationWidgetListShowBorder)
                widget.setInt(R.id.widget_profile_list_not_rounded_border, "setColorFilter", Color.argb(0xFF, redBorder, greenBorder, blueBorder));
        }


        // header
        if (applicationWidgetListHeader || (!largeLayout))
        {
            int monochromeValue = 0xFF;
            if (applicationWidgetListIconLightness.equals("0")) monochromeValue = 0x00;
            if (applicationWidgetListIconLightness.equals("25")) monochromeValue = 0x40;
            if (applicationWidgetListIconLightness.equals("50")) monochromeValue = 0x80;
            if (applicationWidgetListIconLightness.equals("75")) monochromeValue = 0xC0;
            //if (applicationWidgetListIconLightness.equals("100")) monochromeValue = 0xFF;

            Profile profile = DatabaseHandler.getInstance(dataWrapper.context).getActivatedProfile();
            dataWrapper.getEventTimelineList(true);

            boolean isIconResourceID;
            String iconIdentifier;
            Spannable profileName;
            if (profile != null)
            {
                profile.generateIconBitmap(context,
                        applicationWidgetListIconColor.equals("1"),
                        monochromeValue,
                        applicationWidgetListCustomIconLightness);
                if (applicationWidgetListPrefIndicator)
                    profile.generatePreferencesIndicator(context,
                        applicationWidgetListIconColor.equals("1"),
                        monochromeValue);
                isIconResourceID = profile.getIsIconResourceID();
                iconIdentifier = profile.getIconIdentifier();
                profileName = DataWrapper.getProfileNameWithManualIndicator(profile, true, "", true, false, dataWrapper, false, dataWrapper.context);
            }
            else
            {
                // create empty profile and set icon resource
                profile = new Profile();
                profile._name = context.getResources().getString(R.string.profiles_header_profile_name_no_activated);
                profile._icon = Profile.PROFILE_ICON_DEFAULT+"|1|0|0";

                profile.generateIconBitmap(context,
                        applicationWidgetListIconColor.equals("1"),
                        monochromeValue,
                        applicationWidgetListCustomIconLightness);
                if (applicationWidgetListPrefIndicator)
                    profile.generatePreferencesIndicator(context,
                        applicationWidgetListIconColor.equals("1"),
                        monochromeValue);
                isIconResourceID = profile.getIsIconResourceID();
                iconIdentifier = profile.getIconIdentifier();
                profileName = new SpannableString(profile._name);
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
            if (applicationWidgetListLightnessT.equals("0")) red = 0x00;
            if (applicationWidgetListLightnessT.equals("25")) red = 0x40;
            if (applicationWidgetListLightnessT.equals("50")) red = 0x80;
            if (applicationWidgetListLightnessT.equals("75")) red = 0xC0;
            //if (applicationWidgetListLightnessT.equals("100")) red = 0xFF;
            green = red; blue = red;
            widget.setTextColor(R.id.widget_profile_list_header_profile_name, Color.argb(0xFF, red, green, blue));

            widget.setTextViewText(R.id.widget_profile_list_header_profile_name, profileName);
            if (applicationWidgetListPrefIndicator)
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
                //if (applicationWidgetListLightnessT.equals("100")) red = 0xFF;
                green = red; blue = red;
                widget.setInt(R.id.widget_profile_list_header_separator, "setBackgroundColor", Color.argb(0xFF, red, green, blue));
            }

            if (Event.getGlobalEventsRunning(context) && PPApplication.getApplicationStarted(context, true)) {
                if (applicationWidgetListIconColor.equals("1")) {
                    monochromeValue = 0xFF;
                    if (applicationWidgetListIconLightness.equals("0")) monochromeValue = 0x00;
                    if (applicationWidgetListIconLightness.equals("25")) monochromeValue = 0x40;
                    if (applicationWidgetListIconLightness.equals("50")) monochromeValue = 0x80;
                    if (applicationWidgetListIconLightness.equals("75")) monochromeValue = 0xC0;
                    //if (applicationWidgetListIconLightness.equals("100")) monochromeValue = 0xFF;
                    //Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_widget_restart_events);
                    Bitmap bitmap = BitmapManipulator.getBitmapFromResource(R.drawable.ic_widget_restart_events, context);
                    bitmap = BitmapManipulator.monochromeBitmap(bitmap, monochromeValue);
                    widget.setImageViewBitmap(R.id.widget_profile_list_header_restart_events, bitmap);
                }
                else
                    widget.setImageViewResource(R.id.widget_profile_list_header_restart_events, R.drawable.ic_widget_restart_events);
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

            if (Event.getGlobalEventsRunning(context) && PPApplication.getApplicationStarted(context, true)) {
                widget.setViewVisibility(R.id.widget_profile_list_header_restart_events, View.VISIBLE);
                Intent intentRE = new Intent(context, RestartEventsFromNotificationActivity.class);
                PendingIntent pIntentRE = PendingIntent.getActivity(context, 2, intentRE, PendingIntent.FLAG_UPDATE_CURRENT);
                widget.setOnClickPendingIntent(R.id.widget_profile_list_header_restart_events, pIntentRE);
            }
            else
                widget.setViewVisibility(R.id.widget_profile_list_header_restart_events, View.GONE);

            if (!applicationWidgetListGridLayout)
                widget.setRemoteAdapter(R.id.widget_profile_list, svcIntent);
            else
                widget.setRemoteAdapter(R.id.widget_profile_grid, svcIntent);

            // The empty view is displayed when the collection has no items.
            // It should be in the same layout used to instantiate the RemoteViews
            // object above.
            if (!applicationWidgetListGridLayout)
                widget.setEmptyView(R.id.widget_profile_list, R.id.widget_profiles_list_empty);
            else
                widget.setEmptyView(R.id.widget_profile_grid, R.id.widget_profiles_list_empty);

            Intent clickIntent=new Intent(context, BackgroundActivateProfileActivity.class);
            clickIntent.putExtra(PPApplication.EXTRA_STARTUP_SOURCE, PPApplication.STARTUP_SOURCE_WIDGET);
            PendingIntent clickPI=PendingIntent.getActivity(context, 300,
                                                        clickIntent,
                                                        PendingIntent.FLAG_UPDATE_CURRENT);

            if (!applicationWidgetListGridLayout)
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
            dataWrapper = new DataWrapper(context.getApplicationContext(), false, 0, false);
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
                    _updateWidgets(context);

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
                        _updateWidgets(context);

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
        //Log.e("ProfileListWidgetProvider.updateWidget","isLargeLayout="+isLargeLayout);
        if (isLargeLayout)
        {
            if (!ApplicationPreferences.applicationWidgetListGridLayout(context))
                appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.widget_profile_list);
            else
                appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.widget_profile_grid);
        }
    }

    private void _updateWidgets(Context context) {
        try {
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, ProfileListWidgetProvider.class));

            for (int appWidgetId : appWidgetIds) {
                updateWidget(context, appWidgetId);
            }
        } catch (Exception ignored) {}
    }

    void updateWidgets(final Context context, final boolean refresh) {
        PPApplication.startHandlerThreadWidget();
        final Handler handler = new Handler(PPApplication.handlerThreadWidget.getLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {

                createProfilesDataWrapper(context);

                Profile profile = dataWrapper.getActivatedProfileFromDB(false, false);
                dataWrapper.getEventTimelineList(true);

                String pName;
                if (profile != null)
                    pName = DataWrapper.getProfileNameWithManualIndicatorAsString(profile, true, "", true, false, dataWrapper, false, context);
                else
                    pName = context.getResources().getString(R.string.profiles_header_profile_name_no_activated);

                if (!refresh) {
                    String pNameWidget = PPApplication.getWidgetProfileName(context, 3);

                    if (!pNameWidget.isEmpty()) {
                        if (pName.equals(pNameWidget)) {
                            PPApplication.logE("ProfileListWidgetProvider.onUpdate", "activated profile NOT changed");
                            return;
                        }
                    }
                }

                PPApplication.setWidgetProfileName(context, 3, pName);

                _updateWidgets(context);

                if (dataWrapper != null)
                    dataWrapper.invalidateDataWrapper();
                dataWrapper = null;
            }
        });
    }

}
