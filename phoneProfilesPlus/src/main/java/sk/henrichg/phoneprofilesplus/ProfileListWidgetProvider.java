package sk.henrichg.phoneprofilesplus;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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

//import com.crashlytics.android.Crashlytics;

import com.google.firebase.crashlytics.FirebaseCrashlytics;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class ProfileListWidgetProvider extends AppWidgetProvider {

    private DataWrapper dataWrapper;

    private static final String INTENT_REFRESH_LISTWIDGET = PPApplication.PACKAGE_NAME + ".REFRESH_LISTWIDGET";

    private boolean isLargeLayout;
    private boolean isKeyguard;

    private RemoteViews buildLayout(Context context, /*AppWidgetManager appWidgetManager,*/ int appWidgetId, boolean largeLayout)
    {
        /*boolean applicationWidgetListHeader = ApplicationPreferences.applicationWidgetListHeader;
        boolean applicationWidgetListGridLayout = ApplicationPreferences.applicationWidgetListGridLayout;
        boolean applicationWidgetListPrefIndicator = ApplicationPreferences.applicationWidgetListPrefIndicator;
        boolean applicationWidgetListBackgroundType = ApplicationPreferences.applicationWidgetListBackgroundType;
        String applicationWidgetListBackgroundColor = ApplicationPreferences.applicationWidgetListBackgroundColor;
        String applicationWidgetListLightnessB = ApplicationPreferences.applicationWidgetListLightnessB;
        String applicationWidgetListBackground = ApplicationPreferences.applicationWidgetListBackground;
        boolean applicationWidgetListShowBorder = ApplicationPreferences.applicationWidgetListShowBorder;
        String applicationWidgetListLightnessBorder = ApplicationPreferences.applicationWidgetListLightnessBorder;
        boolean applicationWidgetListRoundedCorners = ApplicationPreferences.applicationWidgetListRoundedCorners;
        String applicationWidgetListIconLightness = ApplicationPreferences.applicationWidgetListIconLightness;
        String applicationWidgetListIconColor = ApplicationPreferences.applicationWidgetListIconColor;
        boolean applicationWidgetListCustomIconLightness = ApplicationPreferences.applicationWidgetListCustomIconLightness;
        String applicationWidgetListLightnessT = ApplicationPreferences.applicationWidgetListLightnessT;*/

        Intent svcIntent=new Intent(context, ProfileListWidgetService.class);

        svcIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        svcIntent.setData(Uri.parse(svcIntent.toUri(Intent.URI_INTENT_SCHEME)));

        RemoteViews widget;

        if (largeLayout)
        {
            if (ApplicationPreferences.applicationWidgetListHeader)
            {
                if (!ApplicationPreferences.applicationWidgetListGridLayout)
                {
                    if (ApplicationPreferences.applicationWidgetListPrefIndicator)
                        widget=new RemoteViews(context.getPackageName(), R.layout.profile_list_widget);
                    else
                        widget=new RemoteViews(context.getPackageName(), R.layout.profile_list_widget_no_indicator);
                }
                else
                {
                    if (ApplicationPreferences.applicationWidgetListPrefIndicator)
                        widget=new RemoteViews(context.getPackageName(), R.layout.profile_grid_widget);
                    else
                        widget=new RemoteViews(context.getPackageName(), R.layout.profile_grid_widget_no_indicator);
                }
            }
            else
            {
                if (!ApplicationPreferences.applicationWidgetListGridLayout)
                    widget=new RemoteViews(context.getPackageName(), R.layout.profile_list_widget_no_header);
                else
                    widget=new RemoteViews(context.getPackageName(), R.layout.profile_grid_widget_no_header);
            }
        }
        else
        {
            if (isKeyguard)
            {
                if (ApplicationPreferences.applicationWidgetListPrefIndicator)
                    widget=new RemoteViews(context.getPackageName(), R.layout.profile_list_widget_small_keyguard);
                else
                    widget=new RemoteViews(context.getPackageName(), R.layout.profile_list_widget_small_no_indicator_keyguard);
            }
            else
            {
                if (ApplicationPreferences.applicationWidgetListPrefIndicator)
                    widget=new RemoteViews(context.getPackageName(), R.layout.profile_list_widget_small);
                else
                    widget=new RemoteViews(context.getPackageName(), R.layout.profile_list_widget_small_no_indicator);
            }
        }

        // set background
        int redBackground = 0x00;
        int greenBackground;
        int blueBackground;
        if (ApplicationPreferences.applicationWidgetListBackgroundType) {
            int bgColor = Integer.parseInt(ApplicationPreferences.applicationWidgetListBackgroundColor);
            redBackground = Color.red(bgColor);
            greenBackground = Color.green(bgColor);
            blueBackground = Color.blue(bgColor);
        }
        else {
            switch (ApplicationPreferences.applicationWidgetListLightnessB) {
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
        switch (ApplicationPreferences.applicationWidgetListBackground) {
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
        if (ApplicationPreferences.applicationWidgetListShowBorder) {
            switch (ApplicationPreferences.applicationWidgetListLightnessBorder) {
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
        if (ApplicationPreferences.applicationWidgetListRoundedCorners) {
            widget.setViewVisibility(R.id.widget_profile_list_background, View.VISIBLE);
            widget.setViewVisibility(R.id.widget_profile_list_not_rounded_border, View.INVISIBLE);
            if (ApplicationPreferences.applicationWidgetListShowBorder)
                widget.setViewVisibility(R.id.widget_profile_list_rounded_border, View.VISIBLE);
            else
                widget.setViewVisibility(R.id.widget_profile_list_rounded_border, View.INVISIBLE);
            widget.setInt(R.id.widget_profile_list_root, "setBackgroundColor", 0x00000000);
            widget.setInt(R.id.widget_profile_list_background, "setColorFilter", Color.argb(0xFF, redBackground, greenBackground, blueBackground));
            //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                widget.setInt(R.id.widget_profile_list_background, "setImageAlpha", alphaBackground);
            //else
            //    widget.setInt(R.id.widget_profile_list_background, "setAlpha", alpha);
            if (ApplicationPreferences.applicationWidgetListShowBorder)
                widget.setInt(R.id.widget_profile_list_rounded_border, "setColorFilter", Color.argb(0xFF, redBorder, greenBorder, blueBorder));
        }
        else {
            widget.setViewVisibility(R.id.widget_profile_list_background, View.INVISIBLE);
            widget.setViewVisibility(R.id.widget_profile_list_rounded_border, View.INVISIBLE);
            if (ApplicationPreferences.applicationWidgetListShowBorder)
                widget.setViewVisibility(R.id.widget_profile_list_not_rounded_border, View.VISIBLE);
            else
                widget.setViewVisibility(R.id.widget_profile_list_not_rounded_border, View.INVISIBLE);
            widget.setInt(R.id.widget_profile_list_root, "setBackgroundColor", Color.argb(alphaBackground, redBackground, greenBackground, blueBackground));
            /*widget.setInt(R.id.widget_profile_list_background, "setColorFilter", 0x00000000);
            //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                widget.setInt(R.id.widget_profile_list_background, "setImageAlpha", 0);
            //else
            //    widget.setInt(R.id.widget_profile_list_background, "setAlpha", 0);*/
            if (ApplicationPreferences.applicationWidgetListShowBorder)
                widget.setInt(R.id.widget_profile_list_not_rounded_border, "setColorFilter", Color.argb(0xFF, redBorder, greenBorder, blueBorder));
        }


        // header
        if (ApplicationPreferences.applicationWidgetListHeader || (!largeLayout))
        {
            int monochromeValue = 0xFF;
            switch (ApplicationPreferences.applicationWidgetListIconLightness) {
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

            Profile profile = DatabaseHandler.getInstance(dataWrapper.context).getActivatedProfile();

            boolean fullyStarted = false;
            if (PhoneProfilesService.getInstance() != null)
                fullyStarted = PhoneProfilesService.getInstance().getApplicationFullyStarted();
            boolean applicationPackageReplaced = PPApplication.applicationPackageReplaced;
            if ((!fullyStarted) || applicationPackageReplaced)
                profile = null;

            dataWrapper.getEventTimelineList(true);

            boolean isIconResourceID;
            String iconIdentifier;
            Spannable profileName;
            if (profile != null)
            {
                profile.generateIconBitmap(context.getApplicationContext(),
                        ApplicationPreferences.applicationWidgetListIconColor.equals("1"),
                        monochromeValue,
                        ApplicationPreferences.applicationWidgetListCustomIconLightness);
                if (ApplicationPreferences.applicationWidgetListPrefIndicator)
                    profile.generatePreferencesIndicator(context.getApplicationContext(),
                            ApplicationPreferences.applicationWidgetListIconColor.equals("1"),
                        monochromeValue);
                isIconResourceID = profile.getIsIconResourceID();
                iconIdentifier = profile.getIconIdentifier();
                profileName = DataWrapper.getProfileNameWithManualIndicator(profile, true, "", true, false, false, dataWrapper);
            }
            else
            {
                // create empty profile and set icon resource
                profile = new Profile();
                profile._name = context.getResources().getString(R.string.profiles_header_profile_name_no_activated);
                profile._icon = Profile.PROFILE_ICON_DEFAULT+"|1|0|0";

                profile.generateIconBitmap(context.getApplicationContext(),
                        ApplicationPreferences.applicationWidgetListIconColor.equals("1"),
                        monochromeValue,
                        ApplicationPreferences.applicationWidgetListCustomIconLightness);
                /*if (applicationWidgetListPrefIndicator)
                    profile.generatePreferencesIndicator(context,
                        applicationWidgetListIconColor.equals("1"),
                        monochromeValue);*/
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

            int redText = 0xFF;
            switch (ApplicationPreferences.applicationWidgetListLightnessT) {
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
            int greenText = redText; int blueText = redText;
            widget.setTextColor(R.id.widget_profile_list_header_profile_name, Color.argb(0xFF, redText, greenText, blueText));

            widget.setTextViewText(R.id.widget_profile_list_header_profile_name, profileName);
            if (ApplicationPreferences.applicationWidgetListPrefIndicator)
            {
                if (profile._preferencesIndicator != null) {
                    widget.setImageViewBitmap(R.id.widget_profile_list_header_profile_pref_indicator, profile._preferencesIndicator);
                    widget.setViewVisibility(R.id.widget_profile_list_header_profile_pref_indicator, VISIBLE);
                }
                else
                    widget.setViewVisibility(R.id.widget_profile_list_header_profile_pref_indicator, GONE);
                    //widget.setImageViewResource(R.id.widget_profile_list_header_profile_pref_indicator, R.drawable.ic_empty);
            }
            if (largeLayout)
            {
                redText = 0xFF;
                switch (ApplicationPreferences.applicationWidgetListLightnessT) {
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
                greenText = redText; blueText = redText;
                widget.setInt(R.id.widget_profile_list_header_separator, "setBackgroundColor", Color.argb(0xFF, redText, greenText, blueText));
            }

            //if (Event.getGlobalEventsRunning() && PPApplication.getApplicationStarted(true)) {
                monochromeValue = 0xFF;
            switch (ApplicationPreferences.applicationWidgetListLightnessT) {
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

                Bitmap bitmap = BitmapManipulator.getBitmapFromResource(R.drawable.ic_widget_restart_events, true, context);
                bitmap = BitmapManipulator.monochromeBitmap(bitmap, monochromeValue);
                widget.setImageViewBitmap(R.id.widget_profile_list_header_restart_events, bitmap);
            //}

        }
        ////////////////////////////////////////////////

        // clicks
        if (largeLayout)
        {
            Intent intent = new Intent(context, EditorProfilesActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 1, intent,
                                                        PendingIntent.FLAG_UPDATE_CURRENT);
            widget.setOnClickPendingIntent(R.id.widget_profile_list_header_profile_root, pendingIntent);

            //if (Event.getGlobalEventsRunning() && PPApplication.getApplicationStarted(true)) {
                //widget.setViewVisibility(R.id.widget_profile_list_header_restart_events, View.VISIBLE);
                Intent intentRE = new Intent(context, RestartEventsFromGUIActivity.class);
                PendingIntent pIntentRE = PendingIntent.getActivity(context, 2, intentRE, PendingIntent.FLAG_UPDATE_CURRENT);
                widget.setOnClickPendingIntent(R.id.widget_profile_list_header_restart_events, pIntentRE);
            //}
            //else
            //    widget.setViewVisibility(R.id.widget_profile_list_header_restart_events, View.GONE);

            if (!ApplicationPreferences.applicationWidgetListGridLayout)
                widget.setRemoteAdapter(R.id.widget_profile_list, svcIntent);
            else
                widget.setRemoteAdapter(R.id.widget_profile_grid, svcIntent);

            // The empty view is displayed when the collection has no items.
            // It should be in the same layout used to instantiate the RemoteViews
            // object above.
            if (!ApplicationPreferences.applicationWidgetListGridLayout)
                widget.setEmptyView(R.id.widget_profile_list, R.id.widget_profiles_list_empty);
            else
                widget.setEmptyView(R.id.widget_profile_grid, R.id.widget_profiles_list_empty);

            Intent clickIntent=new Intent(context, BackgroundActivateProfileActivity.class);
            clickIntent.putExtra(PPApplication.EXTRA_STARTUP_SOURCE, PPApplication.STARTUP_SOURCE_WIDGET);
            PendingIntent clickPI=PendingIntent.getActivity(context, 300,
                                                        clickIntent,
                                                        PendingIntent.FLAG_UPDATE_CURRENT);

            if (!ApplicationPreferences.applicationWidgetListGridLayout)
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

            if (Event.getGlobalEventsRunning()) {
                //widget.setViewVisibility(R.id.widget_profile_list_header_restart_events, View.VISIBLE);
                Intent intentRE = new Intent(context, RestartEventsFromGUIActivity.class);
                PendingIntent pIntentRE = PendingIntent.getActivity(context, 2, intentRE, PendingIntent.FLAG_UPDATE_CURRENT);
                widget.setOnClickPendingIntent(R.id.widget_profile_list_header_restart_events, pIntentRE);
            }
            //else
            //    widget.setViewVisibility(R.id.widget_profile_list_header_restart_events, View.GONE);
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
        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().recordException(e);
            //Crashlytics.logException(e);
        }
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

                //if (dataWrapper != null)
                //    dataWrapper.invalidateDataWrapper();
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
                    } catch (Exception e) {
                        FirebaseCrashlytics.getInstance().recordException(e);
                        //Crashlytics.logException(e);
                    }
                }
                else
                if ((action != null) &&
                        (action.equalsIgnoreCase(INTENT_REFRESH_LISTWIDGET)))
                    _updateWidgets(context);

                //if (dataWrapper != null)
                //    dataWrapper.invalidateDataWrapper();
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

        SharedPreferences preferences = ApplicationPreferences.getSharedPreferences(context);
        if (preferences.contains(preferenceKey))
            isLargeLayout = preferences.getBoolean(preferenceKey, true);
        else
        {
            Editor editor = preferences.edit();
            editor.putBoolean(preferenceKey, isLargeLayout);
            editor.apply();
        }
        
    }

    private void setLayoutParamsMotorola(Context context, @SuppressWarnings("unused") int spanX, int spanY, int appWidgetId)
    {
        isKeyguard = false;
        isLargeLayout = spanY != 1;
        
        String preferenceKey = "isLargeLayout_"+appWidgetId;

        Editor editor = ApplicationPreferences.getEditor(context);
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

                // remove preference, will by reset in setLayoutParams
                Editor editor = ApplicationPreferences.getEditor(context);
                editor.remove(preferenceKey);
                editor.apply();


                updateWidget(context, appWidgetId);

                //if (dataWrapper != null)
                //    dataWrapper.invalidateDataWrapper();
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
            if (!ApplicationPreferences.applicationWidgetListGridLayout)
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
        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().recordException(e);
            //Crashlytics.logException(e);
        }
    }

    void updateWidgets(final Context context, final boolean refresh) {
        PPApplication.startHandlerThreadWidget();
        final Handler handler = new Handler(PPApplication.handlerThreadWidget.getLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {

                createProfilesDataWrapper(context);

                Profile profile = dataWrapper.getActivatedProfileFromDB(false, false);
                //dataWrapper.getEventTimelineList(true);

                String pName;
                if (profile != null)
                    pName = DataWrapper.getProfileNameWithManualIndicatorAsString(profile, true, "", true, false, false, dataWrapper);
                else
                    pName = context.getResources().getString(R.string.profiles_header_profile_name_no_activated);

                if (!refresh) {
                    String pNameWidget = PPApplication.prefWidgetProfileName3;

                    if (!pNameWidget.isEmpty()) {
                        if (pName.equals(pNameWidget)) {
                            //PPApplication.logE("ProfileListWidgetProvider.onUpdate", "activated profile NOT changed");
                            return;
                        }
                    }
                }

                PPApplication.setWidgetProfileName(context, 3, pName);

                _updateWidgets(context);

                //if (dataWrapper != null)
                //    dataWrapper.invalidateDataWrapper();
                dataWrapper = null;
            }
        });
    }

}
