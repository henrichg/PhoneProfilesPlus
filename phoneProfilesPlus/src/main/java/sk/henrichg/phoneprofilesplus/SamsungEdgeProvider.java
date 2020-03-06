package sk.henrichg.phoneprofilesplus;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Handler;
import android.text.Spannable;
import android.text.SpannableString;
import android.widget.RemoteViews;

import com.samsung.android.sdk.look.cocktailbar.SlookCocktailManager;
import com.samsung.android.sdk.look.cocktailbar.SlookCocktailProvider;

public class SamsungEdgeProvider extends SlookCocktailProvider {

    private DataWrapper dataWrapper;

    private static final String INTENT_REFRESH_EDGEPANEL = PPApplication.PACKAGE_NAME + ".REFRESH_EDGEPANEL";

    private RemoteViews buildLayout(Context context/*, SlookCocktailManager cocktailBarManager, int appWidgetId*/)
    {
        Intent svcIntent=new Intent(context, SamsungEdgeService.class);
        svcIntent.setData(Uri.parse(svcIntent.toUri(Intent.URI_INTENT_SCHEME)));

        RemoteViews widget;

        boolean applicationSamsungEdgeHeader = ApplicationPreferences.applicationSamsungEdgeHeader;
        boolean applicationSamsungEdgeBackgroundType = ApplicationPreferences.applicationSamsungEdgeBackgroundType;
        String applicationSamsungEdgeBackgroundColor = ApplicationPreferences.applicationSamsungEdgeBackgroundColor;
        String applicationWidgetListLightnessB = ApplicationPreferences.applicationSamsungEdgeLightnessB;
        String applicationWidgetListBackground = ApplicationPreferences.applicationSamsungEdgeBackground;
        String applicationWidgetListIconLightness = ApplicationPreferences.applicationSamsungEdgeIconLightness;
        String applicationSamsungEdgeIconColor = ApplicationPreferences.applicationSamsungEdgeIconColor;
        boolean applicationSamsungEdgeCustomIconLightness = ApplicationPreferences.applicationSamsungEdgeCustomIconLightness;
        String applicationWidgetListLightnessT = ApplicationPreferences.applicationSamsungEdgeLightnessT;
        String applicationSamsungEdgeVerticalPosition = ApplicationPreferences.applicationSamsungEdgeVerticalPosition;

        if (applicationSamsungEdgeHeader)
        {
            switch (applicationSamsungEdgeVerticalPosition) {
                case "1":
                    widget=new RemoteViews(context.getPackageName(), R.layout.samsung_edge_center);
                    break;
                case "2":
                    widget=new RemoteViews(context.getPackageName(), R.layout.samsung_edge_bottom);
                    break;
                default:
                    widget=new RemoteViews(context.getPackageName(), R.layout.samsung_edge_top);
                    break;
            }
        }
        else
        {
            switch (applicationSamsungEdgeVerticalPosition) {
                case "1":
                    widget=new RemoteViews(context.getPackageName(), R.layout.samsung_edge_center_no_header);
                    break;
                case "2":
                    widget=new RemoteViews(context.getPackageName(), R.layout.samsung_edge_bottom_no_header);
                    break;
                default:
                    widget=new RemoteViews(context.getPackageName(), R.layout.samsung_edge_top_no_header);
                    break;
            }
        }

        // set background
        int red = 0x00;
        int green;
        int blue;
        if (applicationSamsungEdgeBackgroundType) {
            int bgColor = Integer.parseInt(applicationSamsungEdgeBackgroundColor);
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
        widget.setInt(R.id.widget_profile_list_root, "setBackgroundColor", Color.argb(alpha, red, green, blue));


        // header
        if (applicationSamsungEdgeHeader)
        {
            int monochromeValue = 0xFF;
            if (applicationWidgetListIconLightness.equals("0")) monochromeValue = 0x00;
            if (applicationWidgetListIconLightness.equals("25")) monochromeValue = 0x40;
            if (applicationWidgetListIconLightness.equals("50")) monochromeValue = 0x80;
            if (applicationWidgetListIconLightness.equals("75")) monochromeValue = 0xC0;
            //if (applicationWidgetListIconLightness.equals("100")) monochromeValue = 0xFF;

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
                profile.generateIconBitmap(context,
                        applicationSamsungEdgeIconColor.equals("1"),
                        monochromeValue,
                        applicationSamsungEdgeCustomIconLightness);
                /*profile.generatePreferencesIndicator(context,
                        applicationSamsungEdgeIconColor.equals("1"),
                        monochromeValue);*/
                isIconResourceID = profile.getIsIconResourceID();
                iconIdentifier = profile.getIconIdentifier();
                profileName = DataWrapper.getProfileNameWithManualIndicator(profile, true, "", true, true, true, dataWrapper, false);
            }
            else
            {
                // create empty profile and set icon resource
                profile = new Profile();
                profile._name = context.getResources().getString(R.string.profiles_header_profile_name_no_activated);
                profile._icon = Profile.PROFILE_ICON_DEFAULT+"|1|0|0";

                profile.generateIconBitmap(context,
                        applicationSamsungEdgeIconColor.equals("1"),
                        monochromeValue,
                        applicationSamsungEdgeCustomIconLightness);
                /*profile.generatePreferencesIndicator(context,
                        applicationSamsungEdgeIconColor.equals("1"),
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

            red = 0xFF;
            if (applicationWidgetListLightnessT.equals("0")) red = 0x00;
            if (applicationWidgetListLightnessT.equals("25")) red = 0x40;
            if (applicationWidgetListLightnessT.equals("50")) red = 0x80;
            if (applicationWidgetListLightnessT.equals("75")) red = 0xC0;
            //if (applicationWidgetListLightnessT.equals("100")) red = 0xFF;
            green = red; blue = red;
            widget.setTextColor(R.id.widget_profile_list_header_profile_name, Color.argb(0xFF, red, green, blue));

            widget.setTextViewText(R.id.widget_profile_list_header_profile_name, profileName);
            /*if (applicationSamsungEdgePrefIndicator)
            {
                if (profile._preferencesIndicator != null)
                    widget.setImageViewBitmap(R.id.widget_profile_list_header_profile_pref_indicator, profile._preferencesIndicator);
                else
                    widget.setImageViewResource(R.id.widget_profile_list_header_profile_pref_indicator, R.drawable.ic_empty);
            }*/

            red = 0xFF;
            if (applicationWidgetListLightnessT.equals("0")) red = 0x00;
            if (applicationWidgetListLightnessT.equals("25")) red = 0x40;
            if (applicationWidgetListLightnessT.equals("50")) red = 0x80;
            if (applicationWidgetListLightnessT.equals("75")) red = 0xC0;
            //if (applicationWidgetListLightnessT.equals("100")) red = 0xFF;
            green = red; blue = red;
            widget.setInt(R.id.widget_profile_list_header_separator, "setBackgroundColor", Color.argb(0xFF, red, green, blue));

            /*
            if (Event.getGlobalEventsRunning(context)) {
                if (applicationSamsungEdgeIconColor.equals("1")) {
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
            */

        }
        ////////////////////////////////////////////////

        // clicks
        Intent intent = new Intent(context, EditorProfilesActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 1, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        widget.setOnClickPendingIntent(R.id.widget_profile_list_header, pendingIntent);

        /*
        if (Event.getGlobalEventsRunning(context)) {
            widget.setViewVisibility(R.id.widget_profile_list_header_restart_events, View.VISIBLE);
            Intent intentRE = new Intent(context, RestartEventsFromGUIActivity.class);
            PendingIntent pIntentRE = PendingIntent.getActivity(context, 2, intentRE, PendingIntent.FLAG_UPDATE_CURRENT);
            widget.setOnClickPendingIntent(R.id.widget_profile_list_header_restart_events, pIntentRE);
        }
        else
            widget.setViewVisibility(R.id.widget_profile_list_header_restart_events, View.GONE);
        */

        /*if (!applicationSamsungEdgeGridLayout)
            widget.setRemoteAdapter(R.id.widget_profile_list, svcIntent);
        else*/
            widget.setRemoteAdapter(R.id.widget_profile_grid, svcIntent);

        // The empty view is displayed when the collection has no items.
        // It should be in the same layout used to instantiate the RemoteViews
        // object above.
        /*if (!applicationSamsungEdgeGridLayout)
            widget.setEmptyView(R.id.widget_profile_list, R.id.widget_profiles_list_empty);
        else*/
            widget.setEmptyView(R.id.widget_profile_grid, R.id.widget_profiles_list_empty);

        Intent clickIntent=new Intent(context, BackgroundActivateProfileActivity.class);
        clickIntent.putExtra(PPApplication.EXTRA_STARTUP_SOURCE, PPApplication.STARTUP_SOURCE_WIDGET);
        PendingIntent clickPI=PendingIntent.getActivity(context, 400,
                clickIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        /*if (!applicationSamsungEdgeGridLayout)
            widget.setPendingIntentTemplate(R.id.widget_profile_list, clickPI);
        else*/
            widget.setPendingIntentTemplate(R.id.widget_profile_grid, clickPI);

        return widget;
    }

    private void createProfilesDataWrapper(Context context)
    {
        if (dataWrapper == null)
        {
            dataWrapper = new DataWrapper(context.getApplicationContext(), false, 0, false);
        }
    }

    private void doOnUpdate(Context context, SlookCocktailManager cocktailBarManager, int cocktailId)
    {
        RemoteViews widget = buildLayout(context);
        try {
            cocktailBarManager.updateCocktail(cocktailId, widget);
        } catch (Exception ignored) {}
    }

    @Override
    public void onUpdate(final Context context, final SlookCocktailManager cocktailBarManager, final int[] cocktailIds) {
        super.onUpdate(context, cocktailBarManager, cocktailIds);

        PPApplication.startHandlerThreadWidget();
        final Handler handler = new Handler(PPApplication.handlerThreadWidget.getLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                createProfilesDataWrapper(context);

                for (int cocktailId : cocktailIds) {
                    doOnUpdate(context, cocktailBarManager, cocktailId);
                }

                //if (dataWrapper != null)
                //    dataWrapper.invalidateDataWrapper();
                dataWrapper = null;
            }
        });
    }

    @Override
    public void onReceive(final Context context, final Intent intent) {
        super.onReceive(context, intent); // calling onUpdate!!!

        PPApplication.startHandlerThreadWidget();
        final Handler handler = new Handler(PPApplication.handlerThreadWidget.getLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (EditorProfilesActivity.doImport)
                    return;

                String action = intent.getAction();

                createProfilesDataWrapper(context);

                if ((action != null) &&
                        (action.equalsIgnoreCase(INTENT_REFRESH_EDGEPANEL))) {
                    _updateWidgets(context);
                }

                //if (dataWrapper != null)
                //    dataWrapper.invalidateDataWrapper();
                dataWrapper = null;
            }
        });
    }

    private void updateWidget(Context context, int cocktailId) {
        try {
            SlookCocktailManager cocktailManager = SlookCocktailManager.getInstance(context);

            doOnUpdate(context, cocktailManager, cocktailId);

            /*if (!ApplicationPreferences.applicationSamsungEdgeGridLayout(context))
                cocktailManager.notifyCocktailViewDataChanged(cocktailId, R.id.widget_profile_list);
            else*/
                cocktailManager.notifyCocktailViewDataChanged(cocktailId, R.id.widget_profile_grid);
        } catch (Exception ignored) {}
    }

    private void _updateWidgets(Context context) {
        try {
            SlookCocktailManager cocktailManager = SlookCocktailManager.getInstance(context);
            int[] cocktailIds = cocktailManager.getCocktailIds(new ComponentName(context, SamsungEdgeProvider.class));

            if (cocktailIds != null) {
                for (int cocktailId : cocktailIds) {
                    updateWidget(context, cocktailId);
                }
            }
        } catch (Exception ignored) {}
    }

    @Override
    public void onVisibilityChanged(Context context, int cocktailId, int visibility) {

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
                    pName = DataWrapper.getProfileNameWithManualIndicatorAsString(profile, true, "", true, false, false, dataWrapper, false);
                else
                    pName = context.getResources().getString(R.string.profiles_header_profile_name_no_activated);

                if (!refresh) {
                    String pNameWidget = PPApplication.prefWidgetProfileName4;

                    if (!pNameWidget.isEmpty()) {
                        if (pName.equals(pNameWidget)) {
                            //PPApplication.logE("SamsungEdgeProvider.onUpdate", "activated profile NOT changed");
                            return;
                        }
                    }
                }

                PPApplication.setWidgetProfileName(context, 4, pName);

                _updateWidgets(context);

                //if (dataWrapper != null)
                //    dataWrapper.invalidateDataWrapper();
                dataWrapper = null;
            }
        });
    }

}
