package sk.henrichg.phoneprofilesplus;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.util.TypedValue;
import android.widget.RemoteViews;

import androidx.core.graphics.ColorUtils;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.lang.ref.WeakReference;
import java.util.concurrent.TimeUnit;

/** @noinspection ExtractMethodRecommender*/
public class PanelWidgetProvider extends AppWidgetProvider {

    //private DataWrapper dataWrapper;

    static final String ACTION_REFRESH_PANELWIDGET = PPApplication.PACKAGE_NAME + ".REFRESH_PANELWIDGET";

    private static RemoteViews buildLayout(Context context, /*AppWidgetManager appWidgetManager,*/ int appWidgetId, /*boolean largeLayout,*/ DataWrapper dataWrapper) {
        Intent svcIntent = new Intent(context, PanelWidgetService.class);
        svcIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        svcIntent.setData(Uri.parse(svcIntent.toUri(Intent.URI_INTENT_SCHEME)));

        RemoteViews widget;

        boolean applicationWidgetPanelHeader;
        boolean applicationWidgetPanelBackgroundType;
        String applicationWidgetPanelBackgroundColor;
        String applicationWidgetPanelLightnessB;
        String applicationWidgetPanelBackground;
        String applicationWidgetPanelIconLightness;
        String applicationWidgetPanelIconColor;
        boolean applicationWidgetPanelCustomIconLightness;
        String applicationWidgetPanelLightnessT;
        String applicationWidgetPanelVerticalPosition;
        boolean applicationWidgetPanelChangeColorsByNightMode;
        String applicationWidgetPanelBackgroundColorNightModeOff;
        String applicationWidgetPanelBackgroundColorNightModeOn;

        int setSettingsLightness = 0;

//        PPApplicationStatic.logE("[SYNCHRONIZED] PanelWidgetProvider.buildLayout", "PPApplication.applicationPreferencesMutex");
        synchronized (PPApplication.applicationPreferencesMutex) {
            applicationWidgetPanelHeader = ApplicationPreferences.applicationWidgetPanelHeader;
            applicationWidgetPanelBackgroundType = ApplicationPreferences.applicationWidgetPanelBackgroundType;
            applicationWidgetPanelBackgroundColor = ApplicationPreferences.applicationWidgetPanelBackgroundColor;
            applicationWidgetPanelLightnessB = ApplicationPreferences.applicationWidgetPanelLightnessB;
            applicationWidgetPanelBackground = ApplicationPreferences.applicationWidgetPanelBackground;
            applicationWidgetPanelIconLightness = ApplicationPreferences.applicationWidgetPanelIconLightness;
            applicationWidgetPanelIconColor = ApplicationPreferences.applicationWidgetPanelIconColor;
            applicationWidgetPanelCustomIconLightness = ApplicationPreferences.applicationWidgetPanelCustomIconLightness;
            applicationWidgetPanelLightnessT = ApplicationPreferences.applicationWidgetPanelLightnessT;
            applicationWidgetPanelVerticalPosition = ApplicationPreferences.applicationWidgetPanelVerticalPosition;

            if (Build.VERSION.SDK_INT < 30)
                applicationWidgetPanelChangeColorsByNightMode = false;
            else
                applicationWidgetPanelChangeColorsByNightMode = ApplicationPreferences.applicationWidgetPanelChangeColorsByNightMode;

            applicationWidgetPanelBackgroundColorNightModeOff = ApplicationPreferences.applicationWidgetPanelBackgroundColorNightModeOff;
            applicationWidgetPanelBackgroundColorNightModeOn = ApplicationPreferences.applicationWidgetPanelBackgroundColorNightModeOn;

            if (Build.VERSION.SDK_INT >= 30) {

                if (applicationWidgetPanelChangeColorsByNightMode) {
                    boolean nightModeOn = GlobalGUIRoutines.isNightModeEnabled(context.getApplicationContext());
                    //int nightModeFlags =
                    //        context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
                    //switch (nightModeFlags) {
                    applicationWidgetPanelBackgroundType = true; // background type = color
                    if (nightModeOn) {
                        //case Configuration.UI_MODE_NIGHT_YES:

                        //applicationWidgetPanelBackground = "75"; // opaque of backgroud = 75%
                        applicationWidgetPanelBackgroundColor = String.valueOf(ColorChooserPreference.parseValue(applicationWidgetPanelBackgroundColorNightModeOn)); // color of background
                        //applicationWidgetPanelLightnessT = GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_87; // lightness of text = white
                        //applicationWidgetPanelIconColor = "0"; // icon type = colorful
                        applicationWidgetPanelIconLightness = GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_75;
                        setSettingsLightness = -1;
                        //break;
                    } else {
                        //case Configuration.UI_MODE_NIGHT_NO:
                        //case Configuration.UI_MODE_NIGHT_UNDEFINED:

                        //applicationWidgetPanelBackground = "75"; // opaque of backgroud = 75%
                        applicationWidgetPanelBackgroundColor = String.valueOf(ColorChooserPreference.parseValue(applicationWidgetPanelBackgroundColorNightModeOff)); // color of background
                        //applicationWidgetPanelLightnessT = GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_12; // lightness of text = black
                        //applicationWidgetPanelIconColor = "0"; // icon type = colorful
                        applicationWidgetPanelIconLightness = GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_62;
                        setSettingsLightness = 1;
                        //break;
                    }
                }
            }
        }

        //Log.e("PanelWidgetProvider.buildLayout", "applicationWidgetPanelHeader=" + applicationWidgetPanelHeader);
        //Log.e("PanelWidgetProvider.buildLayout", "applicationWidgetPanelVerticalPosition=" + applicationWidgetPanelVerticalPosition);
        if (applicationWidgetPanelHeader) {
            switch (applicationWidgetPanelVerticalPosition) {
                case "1":
                    widget = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_panel_center);
                    break;
                case "2":
                    widget = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_panel_bottom);
                    break;
                default:
                    widget = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_panel_top);
                    break;
            }
        }
        else
        {
            switch (applicationWidgetPanelVerticalPosition) {
                case "1":
                    widget=new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_panel_center_no_header);
                    break;
                case "2":
                    widget=new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_panel_bottom_no_header);
                    break;
                default:
                    widget=new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_panel_top_no_header);
                    break;
            }
        }

        // set background
        int red = 0x00;
        int green;
        int blue;
        if (applicationWidgetPanelBackgroundType) {
            int bgColor = Integer.parseInt(applicationWidgetPanelBackgroundColor);
            red = Color.red(bgColor);
            green = Color.green(bgColor);
            blue = Color.blue(bgColor);
        }
        else {
            //if (applicationWidgetPanelLightnessB.equals(GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_0)) red = 0x00;
            if (applicationWidgetPanelLightnessB.equals(GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_12)) red = 0x20;
            if (applicationWidgetPanelLightnessB.equals(GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_25)) red = 0x40;
            if (applicationWidgetPanelLightnessB.equals(GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_37)) red = 0x60;
            if (applicationWidgetPanelLightnessB.equals(GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_50)) red = 0x80;
            if (applicationWidgetPanelLightnessB.equals(GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_62)) red = 0xA0;
            if (applicationWidgetPanelLightnessB.equals(GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_75)) red = 0xC0;
            if (applicationWidgetPanelLightnessB.equals(GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_87)) red = 0xE0;
            if (applicationWidgetPanelLightnessB.equals(GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_100)) red = 0xFF;
            green = red;
            blue = red;
        }
        int alpha = 0x80;
        if (applicationWidgetPanelBackground.equals(GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_0)) alpha = 0x00;
        if (applicationWidgetPanelBackground.equals(GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_12))
            alpha = 0x20;
        if (applicationWidgetPanelBackground.equals(GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_25))
            alpha = 0x40;
        if (applicationWidgetPanelBackground.equals(GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_37))
            alpha = 0x60;
        //if (applicationWidgetPanelBackground.equals(GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_50)) alpha = 0x80;
        if (applicationWidgetPanelBackground.equals(GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_62))
            alpha = 0xA0;
        if (applicationWidgetPanelBackground.equals(GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_75))
            alpha = 0xC0;
        if (applicationWidgetPanelBackground.equals(GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_87))
            alpha = 0xE0;
        if (applicationWidgetPanelBackground.equals(GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_100))
            alpha = 0xFF;
        widget.setInt(R.id.widget_panel_root, "setBackgroundColor", Color.argb(alpha, red, green, blue));

        int redText = 0xFF;
        switch (applicationWidgetPanelLightnessT) {
            case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_0:
                redText = 0x00;
                break;
            case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_12:
                redText = 0x20;
                break;
            case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_25:
                redText = 0x40;
                break;
            case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_37:
                redText = 0x60;
                break;
            case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_50:
                redText = 0x80;
                break;
            case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_62:
                redText = 0xA0;
                break;
            case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_75:
                redText = 0xC0;
                break;
            case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_87:
                redText = 0xE0;
                break;
            case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_100:
                //noinspection ConstantConditions
                redText = 0xFF;
                break;
        }
        //int greenText = redText;
        //int blueText = redText;

        int settingsLightness = redText;
        if (setSettingsLightness == -1) {
            // nigthNodeOn = true
            // GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_87
            settingsLightness = 0xE0 - 0x1F;
            //if (settingsLightness < 0x00)
            //    settingsLightness = 0x00;
        }
        else
        if (setSettingsLightness == 1) {
            // nigthNodeOn = false
            // GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_12
            settingsLightness = 0x20 + 0x1F;
            //if (settingsLightness > 0xFF)
            //    settingsLightness = 0xFF;
        }

        // header
        if (applicationWidgetPanelHeader) {
            int monochromeValue = 0xFF;
            if (applicationWidgetPanelIconLightness.equals(GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_0))
                monochromeValue = 0x00;
            if (applicationWidgetPanelIconLightness.equals(GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_12))
                monochromeValue = 0x20;
            if (applicationWidgetPanelIconLightness.equals(GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_25))
                monochromeValue = 0x40;
            if (applicationWidgetPanelIconLightness.equals(GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_37))
                monochromeValue = 0x60;
            if (applicationWidgetPanelIconLightness.equals(GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_50))
                monochromeValue = 0x80;
            if (applicationWidgetPanelIconLightness.equals(GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_62)) monochromeValue = 0xA0;
            if (applicationWidgetPanelIconLightness.equals(GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_75)) monochromeValue = 0xC0;
            if (applicationWidgetPanelIconLightness.equals(GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_87)) monochromeValue = 0xE0;
            //if (applicationWidgetPanelIconLightness.equals(GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_100)) monochromeValue = 0xFF;

            Profile profile = DatabaseHandler.getInstance(dataWrapper.context).getActivatedProfile();

            //boolean fullyStarted = false;
            //if (PhoneProfilesService.getInstance() != null)
            //    fullyStarted = PhoneProfilesService.getInstance().getApplicationFullyStarted();
            //boolean fullyStarted = PPApplication.applicationFullyStarted;
            //boolean applicationPackageReplaced = PPApplication.applicationPackageReplaced;
            //if ((!fullyStarted) /*|| applicationPackageReplaced*/)
            //    profile = null;

            boolean isIconResourceID;
            String iconIdentifier;
            Spannable profileName;
            if (profile != null)
            {
                profile.generateIconBitmap(context.getApplicationContext(),
                        applicationWidgetPanelIconColor.equals("1"),
                        monochromeValue,
                        applicationWidgetPanelCustomIconLightness);
                isIconResourceID = profile.getIsIconResourceID();
                iconIdentifier = profile.getIconIdentifier();
                profileName = DataWrapperStatic.getProfileNameWithManualIndicator(profile, true, "", true, true, true, dataWrapper);
            }
            else
            {
                // create empty profile and set icon resource
                profile = new Profile();
                profile._name = context.getString(R.string.profiles_header_profile_name_no_activated);
                profile._icon = StringConstants.PROFILE_ICON_DEFAULT+"|1|0|0";

                profile.generateIconBitmap(context.getApplicationContext(),
                        applicationWidgetPanelIconColor.equals("1"),
                        monochromeValue,
                        applicationWidgetPanelCustomIconLightness);
                /*profile.generatePreferencesIndicator(context,
                        applicationWidgetPanelIconColor.equals("1"),
                        monochromeValue);*/
                isIconResourceID = profile.getIsIconResourceID();
                iconIdentifier = profile.getIconIdentifier();
                profileName = new SpannableString(profile._name);
            }

            Bitmap bitmap = null;
            if (applicationWidgetPanelIconColor.equals("0")) {
                if (isIconResourceID) {
                    if (applicationWidgetPanelChangeColorsByNightMode ||
                            ((!applicationWidgetPanelBackgroundType) &&
                                    (Integer.parseInt(applicationWidgetPanelLightnessB) <= 25)) ||
                            (applicationWidgetPanelBackgroundType &&
                                    (ColorUtils.calculateLuminance(Integer.parseInt(applicationWidgetPanelBackgroundColor)) < 0.23)))
                        bitmap = profile.increaseProfileIconBrightnessForContext(context, profile._iconBitmap);
                } else
                    bitmap = profile._iconBitmap;
            }
            if (isIconResourceID)
            {
                if (bitmap != null)
                    widget.setImageViewBitmap(R.id.widget_panel_header_profile_icon, bitmap);
                else {
                    if (profile._iconBitmap != null)
                        widget.setImageViewBitmap(R.id.widget_panel_header_profile_icon, profile._iconBitmap);
                    else {
                        //int iconResource = context.getResources().getIdentifier(iconIdentifier, "drawable", context.PPApplication.PACKAGE_NAME);
                        int iconResource = ProfileStatic.getIconResource(iconIdentifier);
                        widget.setImageViewResource(R.id.widget_panel_header_profile_icon, iconResource);
                    }
                }
            }
            else
            {
                if (bitmap != null)
                    widget.setImageViewBitmap(R.id.widget_panel_header_profile_icon, bitmap);
                else
                    widget.setImageViewBitmap(R.id.widget_panel_header_profile_icon, profile._iconBitmap);
            }

            red = 0xFF;
            switch (applicationWidgetPanelLightnessT) {
                case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_0:
                    red = 0x00;
                    break;
                case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_12:
                    red = 0x20;
                    break;
                case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_25:
                    red = 0x40;
                    break;
                case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_37:
                    red = 0x60;
                    break;
                case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_50:
                    red = 0x80;
                    break;
                case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_62:
                    red = 0xA0;
                    break;
                case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_75:
                    red = 0xC0;
                    break;
                case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_87:
                    red = 0xE0;
                    break;
                case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_100:
                    //noinspection ConstantConditions
                    red = 0xFF;
                    break;
            }
            green = red; blue = red;
            widget.setTextColor(R.id.widget_panel_header_profile_name, Color.argb(0xFF, red, green, blue));
            widget.setTextViewTextSize(R.id.widget_panel_header_profile_name, TypedValue.COMPLEX_UNIT_DIP, 15);
            Spannable sb = new SpannableString(profileName);
            sb.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, profileName.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            widget.setTextViewText(R.id.widget_panel_header_profile_name, sb);
            /*if (applicationWidgetPanelPrefIndicator)
            {
                if (profile._preferencesIndicator != null)
                    widget.setImageViewBitmap(R.id.widget_panel_header_profile_pref_indicator, profile._preferencesIndicator);
                else
                    widget.setImageViewResource(R.id.widget_panel_header_profile_pref_indicator, R.drawable.ic_empty);
            }*/

            red = 0xFF;
            switch (applicationWidgetPanelLightnessT) {
                case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_0:
                    red = 0x00;
                    break;
                case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_12:
                    red = 0x20;
                    break;
                case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_25:
                    red = 0x40;
                    break;
                case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_37:
                    red = 0x60;
                    break;
                case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_50:
                    red = 0x80;
                    break;
                case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_62:
                    red = 0xA0;
                    break;
                case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_75:
                    red = 0xC0;
                    break;
                case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_87:
                    red = 0xE0;
                    break;
                case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_100:
                    //noinspection ConstantConditions
                    red = 0xFF;
                    break;
            }
            //noinspection ConstantConditions
            green = red;
            //noinspection ConstantConditions
            blue = red;
            widget.setInt(R.id.widget_panel_header_separator, "setBackgroundColor", Color.argb(0xFF, red, green, blue));

            /*
            if (Event.getGlobalEventsRunning(context)) {
                if (applicationWidgetPanelIconColor.equals("1")) {
                    monochromeValue = 0xFF;
                    if (applicationWidgetPanelIconLightness.equals(GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_0)) monochromeValue = 0x00;
                    if (applicationWidgetPanelIconLightness.equals(GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_25)) monochromeValue = 0x40;
                    if (applicationWidgetPanelIconLightness.equals(GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_50)) monochromeValue = 0x80;
                    if (applicationWidgetPanelIconLightness.equals(GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_75)) monochromeValue = 0xC0;
                    //if (applicationWidgetListIconLightness.equals(GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_100)) monochromeValue = 0xFF;
                    //Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_widget_restart_events);
                    Bitmap bitmap = BitmapManipulator.getBitmapFromResource(R.drawable.ic_widget_restart_events, context);
                    bitmap = BitmapManipulator.monochromeBitmap(bitmap, monochromeValue);
                    widget.setImageViewBitmap(R.id.widget_panel_header_restart_events, bitmap);
                }
                else
                    widget.setImageViewResource(R.id.widget_panel_header_restart_events, R.drawable.ic_widget_restart_events);
            }
            */

            dataWrapper.invalidateDataWrapper();
        }
        ////////////////////////////////////////////////

        Bitmap bitmap;
        /*if (!((Build.VERSION.SDK_INT >= 31) && applicationWidgetPanelChangeColorsByNightMode &&
                applicationWidgetPanelIconColor.equals("0")
                // && applicationWidgetOneRowUseDynamicColors
            )) {*/
        //if (Event.getGlobalEventsRunning() && PPApplicationStatic.getApplicationStarted(true)) {
        bitmap = BitmapManipulator.getBitmapFromResource(R.drawable.ic_widget_settings, true, context);
        bitmap = BitmapManipulator.monochromeBitmap(bitmap, settingsLightness);
        widget.setImageViewBitmap(R.id.widget_panel_settings, bitmap);
        //}
        /*} else {
            // good, color of this is as in notification ;-)
            // but must be removed android:tint in layout
            int color = GlobalGUIRoutines.getDynamicColor(R.attr.colorSecondary, context);
            if (color != 0) {
                bitmap = BitmapManipulator.getBitmapFromResource(R.drawable.ic_widget_settings, true, context);
                bitmap = BitmapManipulator.recolorBitmap(bitmap, color);
                widget.setImageViewBitmap(R.id.widget_panel_settings, bitmap);
            }
        }*/

        // clicks
        Intent intent = new Intent(context, EditorActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 1, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        widget.setOnClickPendingIntent(R.id.widget_panel_header, pendingIntent);

        /*
        if (Event.getGlobalEventsRunning(context)) {
            widget.setViewVisibility(R.id.widget_panel_header_restart_events, View.VISIBLE);
            Intent intentRE = new Intent(context, RestartEventsFromGUIActivity.class);
            PendingIntent pIntentRE = PendingIntent.getActivity(context, 2, intentRE, PendingIntent.FLAG_UPDATE_CURRENT);
            widget.setOnClickPendingIntent(R.id.widget_panel_header_restart_events, pIntentRE);
        }
        else
            widget.setViewVisibility(R.id.widget_panel_header_restart_events, View.GONE);
        */

        Intent intentSettings = new Intent(context, LaunchPanelWidgetConfigurationActivity.class);
        PendingIntent pIntentSettings = PendingIntent.getActivity(context, 3, intentSettings, PendingIntent.FLAG_UPDATE_CURRENT);
        widget.setOnClickPendingIntent(R.id.widget_panel_settings, pIntentSettings);

        /*if (!applicationWidgetPanelGridLayout)
            widget.setRemoteAdapter(R.id.widget_panel, svcIntent);
        else*/
        widget.setRemoteAdapter(R.id.widget_panel_grid, svcIntent);

        // The empty view is displayed when the collection has no items.
        // It should be in the same layout used to instantiate the RemoteViews
        // object above.
        /*if (!applicationWidgetPanelGridLayout)
            widget.setEmptyView(R.id.widget_panel, R.id.widget_panel_empty);
        else*/
            widget.setEmptyView(R.id.widget_panel_grid, R.id.widget_panel_empty);

        Intent clickIntent=new Intent(context, BackgroundActivateProfileActivity.class);
        clickIntent.putExtra(PPApplication.EXTRA_STARTUP_SOURCE, PPApplication.STARTUP_SOURCE_WIDGET);
        PendingIntent clickPI=PendingIntent.getActivity(context, 400,
                clickIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        /*if (!applicationWidgetPanelGridLayout)
            widget.setPendingIntentTemplate(R.id.widget_panel, clickPI);
        else*/
            widget.setPendingIntentTemplate(R.id.widget_panel_grid, clickPI);

        return widget;
    }

    /*
    private void createProfilesDataWrapper(Context context)
    {
        if (dataWrapper == null)
        {
            dataWrapper = new DataWrapper(context.getApplicationContext(), false, 0, false);
        }
    }
    */

    private static void doOnUpdate(Context context, AppWidgetManager _appWidgetManager, final int appWidgetId, boolean fromOnUpdate)
    {
        DataWrapper dataWrapper = new DataWrapper(context.getApplicationContext(), false, 0, false, DataWrapper.IT_FOR_WIDGET, 0, 0f);
        RemoteViews widget = buildLayout(context, appWidgetId, dataWrapper);
        dataWrapper.invalidateDataWrapper();
        try {
            _appWidgetManager.updateAppWidget(appWidgetId, widget);
        } catch (Exception e) {
            PPApplicationStatic.recordException(e);
        }

        if (!fromOnUpdate) {
            final WeakReference<AppWidgetManager> appWidgetManagerWeakRef = new WeakReference<>(_appWidgetManager);
            Runnable runnable = () -> {
                AppWidgetManager appWidgetManager = appWidgetManagerWeakRef.get();
                if (appWidgetManager != null) {
                /*if (!ApplicationPreferences.applicationWidgetPanelGridLayout(context))
                    appWidgetManager.notifyAppWidgetViewDataChanged(cocktailId, R.id.widget_panel);
                else*/
                    appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.widget_panel_grid);
                }
            };
            PPApplicationStatic.createDelayedGuiExecutor();
            //PPApplication.delayedGuiExecutor.submit(runnable);
            PPApplication.delayedGuiExecutor.schedule(runnable, 500, TimeUnit.MILLISECONDS);
        }
    }

    public void onUpdate(Context context, AppWidgetManager _appWidgetManager, final int[] appWidgetIds) {
        final Context appContext = context.getApplicationContext();
        LocaleHelper.setApplicationLocale(appContext);

//        PPApplicationStatic.logE("[IN_LISTENER] PanelWidgetProvider.onUpdate", "xxx");
        if (appWidgetIds.length > 0) {
            //final int[] _cocktailIds = cocktailIds;

            final WeakReference<AppWidgetManager> appWidgetManagerWeakRef = new WeakReference<>(_appWidgetManager);
            Runnable runnable = () -> {
//                    PPApplicationStatic.logE("[IN_EXECUTOR] PPApplication.startHandlerThreadWidget", "START run - from=PanelWidgetProvider.onUpdate");

                //Context appContext= appContextWeakRef.get();
                AppWidgetManager appWidgetManager = appWidgetManagerWeakRef.get();

                for (int appWidgetId : appWidgetIds) {
                    doOnUpdate(appContext, appWidgetManager, appWidgetId, true);
                }

            };
            PPApplicationStatic.createDelayedGuiExecutor();
            PPApplication.delayedGuiExecutor.submit(runnable);
        }
    }

    @Override
    public void onReceive(Context context, final Intent intent) {
        final Context appContext = context.getApplicationContext();
        LocaleHelper.setApplicationLocale(appContext);

        super.onReceive(appContext, intent); // calls onUpdate, is required for widget
//        PPApplicationStatic.logE("[IN_BROADCAST] PanelWidgetProvider.onReceive", "xxx");

        final String action = intent.getAction();

        if (action != null) {
            if (action.equalsIgnoreCase("com.motorola.blur.home.ACTION_SET_WIDGET_SIZE")) {
                //final int spanX = intent.getIntExtra("spanX", 1);
                //final int spanY = intent.getIntExtra("spanY", 1);
                AppWidgetManager manager = AppWidgetManager.getInstance(appContext);
                final int[] appWidgetIds = manager.getAppWidgetIds(new ComponentName(appContext, PanelWidgetProvider.class));

                if ((appWidgetIds != null) && (appWidgetIds.length > 0)) {
                    final WeakReference<AppWidgetManager> appWidgetManagerWeakRef = new WeakReference<>(manager);
                    Runnable runnable = () -> {
//                            PPApplicationStatic.logE("[IN_EXECUTOR] PPApplication.startHandlerThreadWidget", "START run - from=ProfileListWidgetProvider.onReceive (1)");

                        //Context appContext= appContextWeakRef.get();
                        AppWidgetManager appWidgetManager = appWidgetManagerWeakRef.get();

                        if (/*(appContext != null) &&*/ (appWidgetManager != null)) {
                            DataWrapper dataWrapper = new DataWrapper(appContext.getApplicationContext(), false, 0, false, DataWrapper.IT_FOR_WIDGET, 0, 0f);
                            for (int appWidgetId : appWidgetIds) {
                                //boolean isLargeLayout = setLayoutParamsMotorola(context, spanX, spanY, appWidgetId);
                                RemoteViews layout;
                                layout = buildLayout(appContext, appWidgetId, /*isLargeLayout,*/ dataWrapper);
                                try {
                                    appWidgetManager.updateAppWidget(appWidgetId, layout);
                                } catch (Exception e) {
                                    PPApplicationStatic.recordException(e);
                                }
                            }
                            dataWrapper.invalidateDataWrapper();
                        }
                    };
                    PPApplicationStatic.createDelayedGuiExecutor();
                    PPApplication.delayedGuiExecutor.submit(runnable);
                    //PPApplication.delayedGuiExecutor.schedule(runnable, 500, TimeUnit.MILLISECONDS);
                }
            }
            else
            if (action.equalsIgnoreCase(ACTION_REFRESH_PANELWIDGET)) {
                AppWidgetManager manager = AppWidgetManager.getInstance(appContext);
                final int[] appWidgetIds = manager.getAppWidgetIds(new ComponentName(appContext, PanelWidgetProvider.class));

                if ((appWidgetIds != null) && (appWidgetIds.length > 0)) {
                    final WeakReference<AppWidgetManager> appWidgetManagerWeakRef = new WeakReference<>(manager);
                    Runnable runnable = () -> {
    //                        PPApplicationStatic.logE("[IN_EXECUTOR] PPApplication.startHandlerThreadWidget", "START run - from=PanelWidgetProvider.onReceive");

                        //Context appContext= appContextWeakRef.get();
                        AppWidgetManager appWidgetManager = appWidgetManagerWeakRef.get();

                        if (/*(appContext != null) &&*/ (appWidgetManager != null)) {
                            for (int appWidgetId : appWidgetIds) {
                                doOnUpdate(appContext, appWidgetManager, appWidgetId, false);
                            }
                        }
                    };
                    PPApplicationStatic.createDelayedGuiExecutor();
                    PPApplication.delayedGuiExecutor.submit(runnable);
                }
            }
        }
    }

    static void updateWidgets(final Context context/*, final boolean refresh*/) {
        //createProfilesDataWrapper(context);

        /*DataWrapper dataWrapper = new DataWrapper(context.getApplicationContext(), false, 0, false);
        Profile profile = dataWrapper.getActivatedProfileFromDB(false, false);
        //dataWrapper.getEventTimelineList(true);

        String pName;
        if (profile != null)
            pName = DataWrapper.getProfileNameWithManualIndicatorAsString(profile, true, "", true, false, false, dataWrapper);
        else
            pName = context.getString(R.string.profiles_header_profile_name_no_activated);

        if (!refresh) {
            String pNameWidget = PPApplication.prefWidgetProfileName4;
        }

        PPApplication.setWidgetProfileName(context, 4, pName);*/

//        PPApplicationStatic.logE("[LOCAL_BROADCAST_CALL] PanelWidgetProvider.updateWidgets", "xxx");
        Intent intent3 = new Intent(ACTION_REFRESH_PANELWIDGET);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent3);

        //Intent intent = new Intent(context, PanelWidgetProvider.class);
        //intent.setAction(ACTION_REFRESH_WIDGETPANEL);
        //context.sendBroadcast(intent);
        //_updateWidgets(context);

        //if (dataWrapper != null)
        //    dataWrapper.invalidateDataWrapper();
        //dataWrapper = null;
    }

/*    private static abstract class PPHandlerThreadRunnable implements Runnable {

        final WeakReference<Context> appContextWeakRef;
        final WeakReference<SlookCocktailManager> cocktailManagerWeakRef;

        PPHandlerThreadRunnable(Context appContext,
                                       SlookCocktailManager cocktailManager) {
            this.appContextWeakRef = new WeakReference<>(appContext);
            this.cocktailManagerWeakRef = new WeakReference<>(cocktailManager);
        }

    }*/

}
