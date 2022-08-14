package sk.henrichg.phoneprofilesplus;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.View;
import android.widget.RemoteViews;

import androidx.core.graphics.ColorUtils;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class ProfileListWidgetProvider extends AppWidgetProvider {

    //private DataWrapper dataWrapper;

    static final String ACTION_REFRESH_LISTWIDGET = PPApplication.PACKAGE_NAME + ".ACTION_REFRESH_LISTWIDGET";

    //private boolean isLargeLayout;

    private static RemoteViews buildLayout(Context context, /*AppWidgetManager appWidgetManager,*/ int appWidgetId, /*boolean largeLayout,*/ DataWrapper dataWrapper)
    {
        boolean applicationWidgetListHeader;
        boolean applicationWidgetListGridLayout;
        boolean applicationWidgetListPrefIndicator;
        String applicationWidgetListPrefIndicatorLightness;
        boolean applicationWidgetListBackgroundType;
        String applicationWidgetListBackgroundColor;
        String applicationWidgetListLightnessB;
        String applicationWidgetListBackground;
        boolean applicationWidgetListShowBorder;
        String applicationWidgetListLightnessBorder;
        boolean applicationWidgetListRoundedCorners;
        String applicationWidgetListIconLightness;
        String applicationWidgetListIconColor;
        boolean applicationWidgetListCustomIconLightness;
        String applicationWidgetListLightnessT;
        int applicationWidgetListRoundedCornersRadius;
        boolean applicationWidgetListChangeColorsByNightMode;
        boolean applicationWidgetListUseDynamicColors;
        String applicationWidgetListBackgroundColorNightModeOff;
        String applicationWidgetListBackgroundColorNightModeOn;

        synchronized (PPApplication.applicationPreferencesMutex) {

            applicationWidgetListHeader = ApplicationPreferences.applicationWidgetListHeader;
            applicationWidgetListGridLayout = ApplicationPreferences.applicationWidgetListGridLayout;
            applicationWidgetListPrefIndicator = ApplicationPreferences.applicationWidgetListPrefIndicator;
            applicationWidgetListPrefIndicatorLightness = ApplicationPreferences.applicationWidgetListPrefIndicatorLightness;
            applicationWidgetListBackgroundType = ApplicationPreferences.applicationWidgetListBackgroundType;
            applicationWidgetListBackgroundColor = ApplicationPreferences.applicationWidgetListBackgroundColor;
            applicationWidgetListLightnessB = ApplicationPreferences.applicationWidgetListLightnessB;
            applicationWidgetListBackground = ApplicationPreferences.applicationWidgetListBackground;
            applicationWidgetListShowBorder = ApplicationPreferences.applicationWidgetListShowBorder;
            applicationWidgetListLightnessBorder = ApplicationPreferences.applicationWidgetListLightnessBorder;
            applicationWidgetListIconLightness = ApplicationPreferences.applicationWidgetListIconLightness;
            applicationWidgetListIconColor = ApplicationPreferences.applicationWidgetListIconColor;
            applicationWidgetListCustomIconLightness = ApplicationPreferences.applicationWidgetListCustomIconLightness;
            applicationWidgetListLightnessT = ApplicationPreferences.applicationWidgetListLightnessT;
            applicationWidgetListRoundedCorners = ApplicationPreferences.applicationWidgetListRoundedCorners;
            applicationWidgetListRoundedCornersRadius = ApplicationPreferences.applicationWidgetListRoundedCornersRadius;
            applicationWidgetListChangeColorsByNightMode = ApplicationPreferences.applicationWidgetListChangeColorsByNightMode;
            applicationWidgetListUseDynamicColors = ApplicationPreferences.applicationWidgetListUseDynamicColors;
            applicationWidgetListBackgroundColorNightModeOff = ApplicationPreferences.applicationWidgetListBackgroundColorNightModeOff;
            applicationWidgetListBackgroundColorNightModeOn = ApplicationPreferences.applicationWidgetListBackgroundColorNightModeOn;

            // "Rounded corners" parameter is removed, is forced to true
            if (!applicationWidgetListRoundedCorners) {
                //applicationWidgetListRoundedCorners = true;
                applicationWidgetListRoundedCornersRadius = 1;
            }

            if (Build.VERSION.SDK_INT >= 30) {
                if (PPApplication.isPixelLauncherDefault(context) ||
                        PPApplication.isOneUILauncherDefault(context)) {
                    ApplicationPreferences.applicationWidgetListRoundedCorners = true;
                    ApplicationPreferences.applicationWidgetListRoundedCornersRadius = 15;
                    //ApplicationPreferences.applicationWidgetChangeColorsByNightMode = true;
                    SharedPreferences.Editor editor = ApplicationPreferences.getEditor(context);
                    editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_ROUNDED_CORNERS,
                            ApplicationPreferences.applicationWidgetListRoundedCorners);
                    editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_ROUNDED_CORNERS_RADIUS,
                            String.valueOf(ApplicationPreferences.applicationWidgetListRoundedCornersRadius));
                    //editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_CHANGE_COLOR_BY_NIGHT_MODE,
                    //        ApplicationPreferences.applicationWidgetChangeColorsByNightMode);
                    editor.apply();
                    //applicationWidgetListRoundedCorners = ApplicationPreferences.applicationWidgetListRoundedCorners;
                    applicationWidgetListRoundedCornersRadius = ApplicationPreferences.applicationWidgetListRoundedCornersRadius;
                    //applicationWidgetChangeColorsByNightMode = ApplicationPreferences.applicationWidgetChangeColorsByNightMode;
                }
                if (Build.VERSION.SDK_INT < 31)
                    applicationWidgetListUseDynamicColors = false;
                if (//PPApplication.isPixelLauncherDefault(context) ||
                        (applicationWidgetListChangeColorsByNightMode &&
                        (!applicationWidgetListUseDynamicColors))) {
                    int nightModeFlags =
                            context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
                    switch (nightModeFlags) {
                        case Configuration.UI_MODE_NIGHT_YES:
                            //applicationWidgetListBackground = GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_100; // fully opaque
                            applicationWidgetListBackgroundType = true; // background type = color
                            applicationWidgetListBackgroundColor = String.valueOf(ColorChooserPreferenceX.parseValue(applicationWidgetListBackgroundColorNightModeOn)); // color of background
                            //applicationWidgetListShowBorder = false; // do not show border
                            applicationWidgetListLightnessBorder = GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_100;
                            applicationWidgetListLightnessT = GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_87; // lightness of text = white
                            //applicationWidgetListIconColor = "0"; // icon type = colorful
                            applicationWidgetListIconLightness = GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_75;
                            //applicationWidgetListPrefIndicatorLightness = GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_62; // lightness of preference indicators
                            break;
                        case Configuration.UI_MODE_NIGHT_NO:
                        case Configuration.UI_MODE_NIGHT_UNDEFINED:
                            //applicationWidgetListBackground = GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_100; // fully opaque
                            applicationWidgetListBackgroundType = true; // background type = color
                            applicationWidgetListBackgroundColor = String.valueOf(ColorChooserPreferenceX.parseValue(applicationWidgetListBackgroundColorNightModeOff)); // color of background
                            //applicationWidgetListShowBorder = false; // do not show border
                            applicationWidgetListLightnessBorder = "0";
                            applicationWidgetListLightnessT = GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_12; // lightness of text = black
                            //applicationWidgetListIconColor = "0"; // icon type = colorful
                            applicationWidgetListIconLightness = GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_62;
                            //applicationWidgetListPrefIndicatorLightness = GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_50; // lightness of preference indicators
                            break;
                    }
                }
            }
        }

        int monochromeValue = 0xFF;
        switch (applicationWidgetListIconLightness) {
            case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_0:
                monochromeValue = 0x00;
                break;
            case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_12:
                monochromeValue = 0x20;
                break;
            case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_25:
                monochromeValue = 0x40;
                break;
            case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_37:
                monochromeValue = 0x60;
                break;
            case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_50:
                monochromeValue = 0x80;
                break;
            case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_62:
                monochromeValue = 0xA0;
                break;
            case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_75:
                monochromeValue = 0xC0;
                break;
            case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_87:
                monochromeValue = 0xE0;
                break;
            case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_100:
                //noinspection ConstantConditions
                monochromeValue = 0xFF;
                break;
        }

        float prefIndicatorLightnessValue = 0f;
        int prefIndicatorMonochromeValue = 0x00;
        switch (applicationWidgetListPrefIndicatorLightness) {
            case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_0:
                prefIndicatorLightnessValue = -128f;
                //noinspection ConstantConditions
                prefIndicatorMonochromeValue = 0x00;
                break;
            case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_12:
                prefIndicatorLightnessValue = -96f;
                prefIndicatorMonochromeValue = 0x20;
                break;
            case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_25:
                prefIndicatorLightnessValue = -64f;
                prefIndicatorMonochromeValue = 0x40;
                break;
            case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_37:
                prefIndicatorLightnessValue = -32f;
                prefIndicatorMonochromeValue = 0x60;
                break;
            case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_50:
                prefIndicatorLightnessValue = 0f;
                prefIndicatorMonochromeValue = 0x80;
                break;
            case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_62:
                prefIndicatorLightnessValue = 32f;
                prefIndicatorMonochromeValue = 0xA0;
                break;
            case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_75:
                prefIndicatorLightnessValue = 64f;
                prefIndicatorMonochromeValue = 0xC0;
                break;
            case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_87:
                prefIndicatorLightnessValue = 96f;
                prefIndicatorMonochromeValue = 0xE0;
                break;
            case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_100:
                prefIndicatorLightnessValue = 128f;
                prefIndicatorMonochromeValue = 0xFF;
                break;
        }

        // set background
        int redBackground = 0x00;
        int greenBackground;
        int blueBackground;
        if (applicationWidgetListBackgroundType) {
            int bgColor = Integer.parseInt(applicationWidgetListBackgroundColor);
            redBackground = Color.red(bgColor);
            greenBackground = Color.green(bgColor);
            blueBackground = Color.blue(bgColor);
        }
        else {
            switch (applicationWidgetListLightnessB) {
                case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_0:
                    //noinspection ConstantConditions
                    redBackground = 0x00;
                    break;
                case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_12:
                    redBackground = 0x20;
                    break;
                case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_25:
                    redBackground = 0x40;
                    break;
                case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_37:
                    redBackground = 0x60;
                    break;
                case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_50:
                    redBackground = 0x80;
                    break;
                case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_62:
                    redBackground = 0xA0;
                    break;
                case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_75:
                    redBackground = 0xC0;
                    break;
                case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_87:
                    redBackground = 0xE0;
                    break;
                case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_100:
                    redBackground = 0xFF;
                    break;
            }
            greenBackground = redBackground;
            blueBackground = redBackground;
        }
        int alphaBackground = 0x40;
        switch (applicationWidgetListBackground) {
            case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_0:
                alphaBackground = 0x00;
                break;
            case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_12:
                alphaBackground = 0x20;
                break;
            case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_25:
                //noinspection ConstantConditions
                alphaBackground = 0x40;
                break;
            case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_37:
                alphaBackground = 0x60;
                break;
            case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_50:
                alphaBackground = 0x80;
                break;
            case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_62:
                alphaBackground = 0xA0;
                break;
            case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_75:
                alphaBackground = 0xC0;
                break;
            case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_87:
                alphaBackground = 0xE0;
                break;
            case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_100:
                alphaBackground = 0xFF;
                break;
        }
        int redBorder = 0xFF;
        int greenBorder;
        int blueBorder;
        if (applicationWidgetListShowBorder) {
            switch (applicationWidgetListLightnessBorder) {
                case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_0:
                    redBorder = 0x00;
                    break;
                case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_12:
                    redBorder = 0x20;
                    break;
                case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_25:
                    redBorder = 0x40;
                    break;
                case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_37:
                    redBorder = 0x60;
                    break;
                case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_50:
                    redBorder = 0x80;
                    break;
                case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_62:
                    redBorder = 0xA0;
                    break;
                case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_75:
                    redBorder = 0xC0;
                    break;
                case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_87:
                    redBorder = 0xE0;
                    break;
                case GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_100:
                    //noinspection ConstantConditions
                    redBorder = 0xFF;
                    break;
            }
        }
        greenBorder = redBorder;
        blueBorder = redBorder;

        int redText = 0xFF;
        switch (applicationWidgetListLightnessT) {
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
        int greenText = redText; int blueText = redText;

        int restartEventsLightness = redText;
        int separatorLightness = redText;

        //------------------

        Intent svcIntent=new Intent(context, ProfileListWidgetService.class);

        svcIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        svcIntent.setData(Uri.parse(svcIntent.toUri(Intent.URI_INTENT_SCHEME)));

        RemoteViews widget;

        //if (largeLayout)
        //{
        if (!((Build.VERSION.SDK_INT >= 31) && applicationWidgetListChangeColorsByNightMode &&
                applicationWidgetListIconColor.equals("0") && applicationWidgetListUseDynamicColors)) {
            if (applicationWidgetListHeader) {
                if (!applicationWidgetListGridLayout) {
                    if (applicationWidgetListPrefIndicator)
                        widget = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.profile_list_widget);
                    else
                        widget = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.profile_list_widget_no_indicator);
                } else {
                    if (applicationWidgetListPrefIndicator)
                        widget = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.profile_grid_widget);
                    else
                        widget = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.profile_grid_widget_no_indicator);
                }
            } else {
                if (!applicationWidgetListGridLayout)
                    widget = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.profile_list_widget_no_header);
                else
                    widget = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.profile_grid_widget_no_header);
            }
        } else {
            if (applicationWidgetListHeader) {
                if (!applicationWidgetListGridLayout) {
                    if (applicationWidgetListPrefIndicator)
                        widget = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.profile_list_widget_dn);
                    else
                        widget = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.profile_list_widget_no_indicator_dn);
                } else {
                    if (applicationWidgetListPrefIndicator)
                        widget = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.profile_grid_widget_dn);
                    else
                        widget = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.profile_grid_widget_no_indicator_dn);
                }
            } else {
                if (!applicationWidgetListGridLayout)
                    widget = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.profile_list_widget_no_header_dn);
                else
                    widget = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.profile_grid_widget_no_header_dn);
            }
        }

        int roundedBackground = 0;
        int roundedBorder = 0;
        if (PPApplication.isPixelLauncherDefault(context)) {
            roundedBackground = R.drawable.rounded_widget_background_pixel_launcher;
            roundedBorder = R.drawable.rounded_widget_border_pixel_launcher;
        }
        else
        if (PPApplication.isOneUILauncherDefault(context)) {
            roundedBackground = R.drawable.rounded_widget_background_oneui_launcher;
            roundedBorder = R.drawable.rounded_widget_border_oneui_launcher;
        }
        else {
            switch (applicationWidgetListRoundedCornersRadius) {
                case 1:
                    roundedBackground = R.drawable.rounded_widget_background_1;
                    roundedBorder = R.drawable.rounded_widget_border_1;
                    break;
                case 2:
                    roundedBackground = R.drawable.rounded_widget_background_2;
                    roundedBorder = R.drawable.rounded_widget_border_2;
                    break;
                case 3:
                    roundedBackground = R.drawable.rounded_widget_background_3;
                    roundedBorder = R.drawable.rounded_widget_border_3;
                    break;
                case 4:
                    roundedBackground = R.drawable.rounded_widget_background_4;
                    roundedBorder = R.drawable.rounded_widget_border_4;
                    break;
                case 5:
                    roundedBackground = R.drawable.rounded_widget_background_5;
                    roundedBorder = R.drawable.rounded_widget_border_5;
                    break;
                case 6:
                    roundedBackground = R.drawable.rounded_widget_background_6;
                    roundedBorder = R.drawable.rounded_widget_border_6;
                    break;
                case 7:
                    roundedBackground = R.drawable.rounded_widget_background_7;
                    roundedBorder = R.drawable.rounded_widget_border_7;
                    break;
                case 8:
                    roundedBackground = R.drawable.rounded_widget_background_8;
                    roundedBorder = R.drawable.rounded_widget_border_8;
                    break;
                case 9:
                    roundedBackground = R.drawable.rounded_widget_background_9;
                    roundedBorder = R.drawable.rounded_widget_border_9;
                    break;
                case 10:
                    roundedBackground = R.drawable.rounded_widget_background_10;
                    roundedBorder = R.drawable.rounded_widget_border_10;
                    break;
                case 11:
                    roundedBackground = R.drawable.rounded_widget_background_11;
                    roundedBorder = R.drawable.rounded_widget_border_11;
                    break;
                case 12:
                    roundedBackground = R.drawable.rounded_widget_background_12;
                    roundedBorder = R.drawable.rounded_widget_border_12;
                    break;
                case 13:
                    roundedBackground = R.drawable.rounded_widget_background_13;
                    roundedBorder = R.drawable.rounded_widget_border_13;
                    break;
                case 14:
                    roundedBackground = R.drawable.rounded_widget_background_14;
                    roundedBorder = R.drawable.rounded_widget_border_14;
                    break;
                case 15:
                    roundedBackground = R.drawable.rounded_widget_background_15;
                    roundedBorder = R.drawable.rounded_widget_border_15;
                    break;
            }
        }
        if (roundedBackground != 0)
            widget.setImageViewResource(R.id.widget_profile_list_background, roundedBackground);
        else
            widget.setImageViewResource(R.id.widget_profile_list_background, R.drawable.ic_empty);
        if (roundedBorder != 0)
            widget.setImageViewResource(R.id.widget_profile_list_rounded_border, roundedBorder);
        else
            widget.setImageViewResource(R.id.widget_profile_list_rounded_border, R.drawable.ic_empty);

        //if (applicationWidgetListRoundedCorners) {
            widget.setViewVisibility(R.id.widget_profile_list_background, View.VISIBLE);
            widget.setViewVisibility(R.id.widget_profile_list_not_rounded_border, View.GONE);
            if (applicationWidgetListShowBorder)
                widget.setViewVisibility(R.id.widget_profile_list_rounded_border, View.VISIBLE);
            else
                widget.setViewVisibility(R.id.widget_profile_list_rounded_border, View.GONE);
            widget.setInt(R.id.widget_profile_list_root, "setBackgroundColor", 0x00000000);

            if (!((Build.VERSION.SDK_INT >= 31) && applicationWidgetListChangeColorsByNightMode &&
                    applicationWidgetListIconColor.equals("0") && applicationWidgetListUseDynamicColors))
                widget.setInt(R.id.widget_profile_list_background, "setColorFilter", Color.argb(0xFF, redBackground, greenBackground, blueBackground));

            widget.setInt(R.id.widget_profile_list_background, "setImageAlpha", alphaBackground);

            if (applicationWidgetListShowBorder) {
                if (!((Build.VERSION.SDK_INT >= 31) && applicationWidgetListChangeColorsByNightMode &&
                        applicationWidgetListIconColor.equals("0") && applicationWidgetListUseDynamicColors))
                    widget.setInt(R.id.widget_profile_list_rounded_border, "setColorFilter", Color.argb(0xFF, redBorder, greenBorder, blueBorder));
                /*else {
                    // but must be removed android:tint in layout
                    int color = GlobalGUIRoutines.getDynamicColor(R.attr.colorOutline, context);
                    if (color != 0) {
                        widget.setInt(R.id.widget_profile_list_rounded_border, "setColorFilter", color);
                    }
                }*/
            }
        /*}
        else {
            widget.setViewVisibility(R.id.widget_profile_list_background, View.GONE);
            widget.setViewVisibility(R.id.widget_profile_list_rounded_border, View.GONE);
            if (applicationWidgetListShowBorder)
                widget.setViewVisibility(R.id.widget_profile_list_not_rounded_border, View.VISIBLE);
            else
                widget.setViewVisibility(R.id.widget_profile_list_not_rounded_border, View.GONE);
            widget.setInt(R.id.widget_profile_list_root, "setBackgroundColor", Color.argb(alphaBackground, redBackground, greenBackground, blueBackground));
            if (applicationWidgetListShowBorder)
                widget.setInt(R.id.widget_profile_list_not_rounded_border, "setColorFilter", Color.argb(0xFF, redBorder, greenBorder, blueBorder));
        }*/

        // header
        if (applicationWidgetListHeader/* || (!largeLayout)*/)
        {
            Profile profile;

            //boolean fullyStarted = false;
            //if (PhoneProfilesService.getInstance() != null)
            //    fullyStarted = PhoneProfilesService.getInstance().getApplicationFullyStarted();
            //boolean fullyStarted = PPApplication.applicationFullyStarted;
            //if ((!fullyStarted) /*|| applicationPackageReplaced*/)
            //    profile = null;
            //else
                profile = dataWrapper.getActivatedProfile(false, false);

            boolean isIconResourceID;
            String iconIdentifier;
            Spannable profileName;
            if (profile != null)
            {
                profile.generateIconBitmap(context.getApplicationContext(),
                        applicationWidgetListIconColor.equals("1"),
                        monochromeValue,
                        applicationWidgetListCustomIconLightness);
                if (applicationWidgetListPrefIndicator) {
                    int indicatorType;// = DataWrapper.IT_FOR_WIDGET;
                    if (applicationWidgetListChangeColorsByNightMode &&
                            applicationWidgetListIconColor.equals("0")) {
                        if ((Build.VERSION.SDK_INT >= 31) && applicationWidgetListUseDynamicColors)
                            indicatorType = DataWrapper.IT_FOR_WIDGET_DYNAMIC_COLORS;
                        else
                            indicatorType = DataWrapper.IT_FOR_WIDGET_NATIVE_BACKGROUND;
                    }
                    else
                    if (applicationWidgetListBackgroundType) {
                        if (ColorUtils.calculateLuminance(Integer.parseInt(applicationWidgetListBackgroundColor)) < 0.23)
                            indicatorType = DataWrapper.IT_FOR_WIDGET_DARK_BACKGROUND;
                        else
                            indicatorType = DataWrapper.IT_FOR_WIDGET_LIGHT_BACKGROUND;
                    } else {
                        if (Integer.parseInt(applicationWidgetListBackground) <= 37)
                            indicatorType = DataWrapper.IT_FOR_WIDGET_DARK_BACKGROUND;
                        else
                            indicatorType = DataWrapper.IT_FOR_WIDGET_LIGHT_BACKGROUND;
                    }

                    profile.generatePreferencesIndicator(context.getApplicationContext(),
                            applicationWidgetListIconColor.equals("1"),
                            prefIndicatorMonochromeValue,
                            indicatorType, prefIndicatorLightnessValue);
                }
                isIconResourceID = profile.getIsIconResourceID();
                iconIdentifier = profile.getIconIdentifier();
                profileName = DataWrapperStatic.getProfileNameWithManualIndicator(profile, true, "", true, false, false, dataWrapper);
            }
            else
            {
                // create empty profile and set icon resource
                profile = new Profile();
                profile._name = context.getString(R.string.profiles_header_profile_name_no_activated);
                profile._icon = Profile.PROFILE_ICON_DEFAULT+"|1|0|0";

                profile.generateIconBitmap(context.getApplicationContext(),
                        applicationWidgetListIconColor.equals("1"),
                        monochromeValue,
                        applicationWidgetListCustomIconLightness);
                /*if (applicationWidgetListPrefIndicator)
                    profile.generatePreferencesIndicator(context,
                        applicationWidgetListIconColor.equals("1"),
                        monochromeValue);*/
                isIconResourceID = profile.getIsIconResourceID();
                iconIdentifier = profile.getIconIdentifier();
                profileName = new SpannableString(profile._name);
            }

            Bitmap bitmap = null;
            if (applicationWidgetListIconColor.equals("0")) {
                if (applicationWidgetListChangeColorsByNightMode ||
                        ((!applicationWidgetListBackgroundType) &&
                    (Integer.parseInt(applicationWidgetListLightnessB) <= 25)) ||
                    (applicationWidgetListBackgroundType &&
                        (ColorUtils.calculateLuminance(Integer.parseInt(applicationWidgetListBackgroundColor)) < 0.23)))
                    bitmap = profile.increaseProfileIconBrightnessForContext(context, profile._iconBitmap);
            }
            if (isIconResourceID)
            {
                if (bitmap != null)
                    widget.setImageViewBitmap(R.id.widget_profile_list_header_profile_icon, bitmap);
                else {
                    if (profile._iconBitmap != null)
                        widget.setImageViewBitmap(R.id.widget_profile_list_header_profile_icon, profile._iconBitmap);
                    else {
                        //int iconResource = context.getResources().getIdentifier(iconIdentifier, "drawable", context.PPApplication.PACKAGE_NAME);
                        int iconResource = ProfileStatic.getIconResource(iconIdentifier);
                        widget.setImageViewResource(R.id.widget_profile_list_header_profile_icon, iconResource);
                    }
                }
            }
            else
            {
                if (bitmap != null)
                    widget.setImageViewBitmap(R.id.widget_profile_list_header_profile_icon, bitmap);
                else {
                    widget.setImageViewBitmap(R.id.widget_profile_list_header_profile_icon, profile._iconBitmap);
                }
            }

            if (!((Build.VERSION.SDK_INT >= 31) && applicationWidgetListChangeColorsByNightMode &&
                    applicationWidgetListIconColor.equals("0") && applicationWidgetListUseDynamicColors))
                widget.setTextColor(R.id.widget_profile_list_header_profile_name, Color.argb(0xFF, redText, greenText, blueText));
            else {
                // must be removed android:textColor in layout
                int color = GlobalGUIRoutines.getDynamicColor(R.attr.colorOnBackground, context);
                if (color != 0) {
                    widget.setTextColor(R.id.widget_profile_list_header_profile_name, color);
                }
            }

            widget.setTextViewText(R.id.widget_profile_list_header_profile_name, profileName);
            if (applicationWidgetListPrefIndicator)
            {
                if (profile._preferencesIndicator != null) {
                    widget.setImageViewBitmap(R.id.widget_profile_list_header_profile_pref_indicator, profile._preferencesIndicator);
                    widget.setViewVisibility(R.id.widget_profile_list_header_profile_pref_indicator, VISIBLE);
                }
                else
                    widget.setViewVisibility(R.id.widget_profile_list_header_profile_pref_indicator, GONE);
                    //widget.setImageViewResource(R.id.widget_profile_list_header_profile_pref_indicator, R.drawable.ic_empty);
            }

            if (!((Build.VERSION.SDK_INT >= 31) && applicationWidgetListChangeColorsByNightMode &&
                    applicationWidgetListIconColor.equals("0") && applicationWidgetListUseDynamicColors))
                widget.setInt(R.id.widget_profile_list_header_separator, "setBackgroundColor", Color.argb(0xFF, separatorLightness, separatorLightness, separatorLightness));
            /*else {
                // but must be removed android:tint in layout
                int color = GlobalGUIRoutines.getDynamicColor(R.attr.colorOutline, context);
                if (color != 0) {
                    Bitmap bitmap = BitmapManipulator.getBitmapFromResource(R.drawable.ic_black, false, context);
                    bitmap = BitmapManipulator.monochromeBitmap(bitmap, color);
                    widget.setImageViewBitmap(R.id.widget_profile_list_header_separator, bitmap);
                }
            }*/

            if (!((Build.VERSION.SDK_INT >= 31) && applicationWidgetListChangeColorsByNightMode &&
                    applicationWidgetListIconColor.equals("0") && applicationWidgetListUseDynamicColors)) {
                bitmap = BitmapManipulator.getBitmapFromResource(R.drawable.ic_widget_restart_events, true, context);
                bitmap = BitmapManipulator.monochromeBitmap(bitmap, restartEventsLightness);
                widget.setImageViewBitmap(R.id.widget_profile_list_header_restart_events, bitmap);
            } else {
                // good, color of this is as in notification ;-)
                // but must be removed android:tint in layout
                int color = GlobalGUIRoutines.getDynamicColor(R.attr.colorSecondary, context);
                if (color != 0) {
                    bitmap = BitmapManipulator.getBitmapFromResource(R.drawable.ic_widget_restart_events, true, context);
                    bitmap = BitmapManipulator.recolorBitmap(bitmap, color);
                    widget.setImageViewBitmap(R.id.widget_profile_list_header_restart_events, bitmap);
                }
            }

        }
        ////////////////////////////////////////////////

        // clicks
        //if (largeLayout)
        //{
            Intent intent = new Intent(context, EditorActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 1, intent,
                                                        PendingIntent.FLAG_UPDATE_CURRENT);
            widget.setOnClickPendingIntent(R.id.widget_profile_list_header_profile_root, pendingIntent);

            //if (Event.getGlobalEventsRunning() && PPApplication.getApplicationStarted(true)) {
                //widget.setViewVisibility(R.id.widget_profile_list_header_restart_events, View.VISIBLE);
                Intent intentRE = new Intent(context, RestartEventsFromGUIActivity.class);
                PendingIntent pIntentRE = PendingIntent.getActivity(context, 2, intentRE, PendingIntent.FLAG_UPDATE_CURRENT);
                widget.setOnClickPendingIntent(R.id.widget_profile_list_header_restart_events_click, pIntentRE);
            //}
            //else
            //    widget.setViewVisibility(R.id.widget_profile_list_header_restart_events, View.GONE);

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
        /*}
        else
        {
            Intent intent = new Intent(context, LauncherActivity.class);
            // clear all opened activities
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(PPApplication.EXTRA_STARTUP_SOURCE, PPApplication.STARTUP_SOURCE_WIDGET);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 300, intent,
                                                        PendingIntent.FLAG_UPDATE_CURRENT);
            widget.setOnClickPendingIntent(R.id.widget_profile_list_header, pendingIntent);

            //if (Event.getGlobalEventsRunning()) {
                //widget.setViewVisibility(R.id.widget_profile_list_header_restart_events, View.VISIBLE);
                Intent intentRE = new Intent(context, RestartEventsFromGUIActivity.class);
                PendingIntent pIntentRE = PendingIntent.getActivity(context, 2, intentRE, PendingIntent.FLAG_UPDATE_CURRENT);
                widget.setOnClickPendingIntent(R.id.widget_profile_list_header_restart_events, pIntentRE);
            //}
            //else
            //    widget.setViewVisibility(R.id.widget_profile_list_header_restart_events, View.GONE);
        }*/

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

    private static void doOnUpdate(Context context, AppWidgetManager appWidgetManager,
                                   int appWidgetId, boolean fromOnUpdate/*, boolean addWidgetType*/)
    {
        /*if (addWidgetType) {
            Bundle bundle = appWidgetManager.getAppWidgetOptions(appWidgetId);
            bundle.putInt(PPApplication.BUNDLE_WIDGET_TYPE, PPApplication.WIDGET_TYPE_LIST);
            appWidgetManager.updateAppWidgetOptions(appWidgetId, bundle);
        }*/

        //Bundle widgetIdOptions;
        //widgetIdOptions = appWidgetManager.getAppWidgetOptions(appWidgetId);
        //boolean isLargeLayout = setLayoutParams(context, appWidgetManager, appWidgetId, widgetIdOptions);
        DataWrapper dataWrapper = new DataWrapper(context.getApplicationContext(), false, 0, false, DataWrapper.IT_FOR_WIDGET, 0, 0f);
        RemoteViews widget = buildLayout(context, appWidgetId, /*isLargeLayout,*/ dataWrapper);
        dataWrapper.invalidateDataWrapper();
        try {
            appWidgetManager.updateAppWidget(appWidgetId, widget);
        } catch (Exception e) {
            PPApplication.recordException(e);
        }

        if (!fromOnUpdate) {
            //if (isLargeLayout) {
                if (!ApplicationPreferences.applicationWidgetListGridLayout)
                    appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.widget_profile_list);
                else
                    appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.widget_profile_grid);
            //}
        }
    }

    public void onUpdate(Context context, final AppWidgetManager appWidgetManager, final int[] appWidgetIds)
    {
        //super.onUpdate(context, appWidgetManager, appWidgetIds);
//        PPApplication.logE("[IN_LISTENER] ProfileListWidgetProvider.onUpdate", "xxx");
        if (appWidgetIds.length > 0) {

            final Context appContext = context;
            //PPApplication.startHandlerThreadWidget();
            //final Handler __handler = new Handler(PPApplication.handlerThreadWidget.getLooper());
            //__handler.post(new PPHandlerThreadRunnable(context, appWidgetManager) {
            //__handler.post(() -> {
            Runnable runnable = () -> {
//                    PPApplication.logE("[IN_EXECUTOR] PPApplication.startHandlerThreadWidget", "START run - from=ProfileListWidgetProvider.onUpdate");
                //createProfilesDataWrapper(_context);

                //Context appContext= appContextWeakRef.get();
                //AppWidgetManager appWidgetManager = appWidgetManagerWeakRef.get();

                //if ((appContext != null) && (appWidgetManager != null)) {

                    for (int appWidgetId : appWidgetIds) {
                        doOnUpdate(appContext, appWidgetManager, appWidgetId, true/*, true*/);
                    }

                    //if (dataWrapper != null)
                    //    dataWrapper.invalidateDataWrapper();
                    //dataWrapper = null;
                //}
            }; //);
            PPApplication.createDelayedGuiExecutor();
            PPApplication.delayedGuiExecutor.submit(runnable);
        }
    }

    @Override
    public void onReceive(Context context, final Intent intent) {
        super.onReceive(context, intent); // calls onUpdate, is required for widget
//        PPApplication.logE("[IN_BROADCAST] ProfileListWidgetProvider.onReceive", "xxx");

        final String action = intent.getAction();

        if (action != null) {
            if (action.equalsIgnoreCase("com.motorola.blur.home.ACTION_SET_WIDGET_SIZE")) {
                //final int spanX = intent.getIntExtra("spanX", 1);
                //final int spanY = intent.getIntExtra("spanY", 1);
                final AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
                final int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, ProfileListWidgetProvider.class));

                if ((appWidgetIds != null) && (appWidgetIds.length > 0)) {
                    final Context appContext = context;
                    //PPApplication.startHandlerThreadWidget();
                    //final Handler __handler = new Handler(PPApplication.handlerThreadWidget.getLooper());
                    //__handler.post(new PPHandlerThreadRunnable(context, appWidgetManager) {
                    //__handler.post(() -> {
                    Runnable runnable = () -> {
//                            PPApplication.logE("[IN_EXECUTOR] PPApplication.startHandlerThreadWidget", "START run - from=ProfileListWidgetProvider.onReceive (1)");

                        //Context appContext= appContextWeakRef.get();
                        //AppWidgetManager appWidgetManager = appWidgetManagerWeakRef.get();

                        //if ((appContext != null) && (appWidgetManager != null)) {
                            for (int appWidgetId : appWidgetIds) {
                                //boolean isLargeLayout = setLayoutParamsMotorola(context, spanX, spanY, appWidgetId);
                                RemoteViews layout;
                                DataWrapper dataWrapper = new DataWrapper(appContext.getApplicationContext(), false, 0, false, DataWrapper.IT_FOR_WIDGET, 0, 0f);
                                layout = buildLayout(appContext, appWidgetId, /*isLargeLayout,*/ dataWrapper);
                                try {
                                    appWidgetManager.updateAppWidget(appWidgetId, layout);
                                } catch (Exception e) {
                                    PPApplication.recordException(e);
                                }
                            }
                        //}
                    }; //);
                    PPApplication.createDelayedGuiExecutor();
                    PPApplication.delayedGuiExecutor.submit(runnable);
                }
            }
            else
            if (action.equalsIgnoreCase(ACTION_REFRESH_LISTWIDGET)) {
                final AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
                final int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, ProfileListWidgetProvider.class));

                if ((appWidgetIds != null) && (appWidgetIds.length > 0)) {
                    final Context appContext = context;
                    //PPApplication.startHandlerThreadWidget();
                    //final Handler __handler = new Handler(PPApplication.handlerThreadWidget.getLooper());
                    //__handler.post(new PPHandlerThreadRunnable(context, appWidgetManager) {
                    //__handler.post(() -> {
                    Runnable runnable = () -> {
//                            PPApplication.logE("[IN_EXECUTOR] PPApplication.startHandlerThreadWidget", "START run - from=ProfileListWidgetProvider.onReceive (2)");

                        //Context appContext= appContextWeakRef.get();
                        //AppWidgetManager appWidgetManager = appWidgetManagerWeakRef.get();

                        //if ((appContext != null) && (appWidgetManager != null)) {
                            for (int appWidgetId : appWidgetIds) {
                                doOnUpdate(appContext, appWidgetManager, appWidgetId, false/*, true*/);
                            }
                        //}
                    }; //);
                    PPApplication.createDelayedGuiExecutor();
                    PPApplication.delayedGuiExecutor.submit(runnable);
                }
            }
        }
    }

    /*
    private static boolean setLayoutParams(Context context, AppWidgetManager appWidgetManager,
            int appWidgetId, Bundle widgetIdOptions)
    {
        String preferenceKey = "isLargeLayout_"+appWidgetId;

        boolean isLargeLayout;

        SharedPreferences preferences = ApplicationPreferences.getSharedPreferences(context);
        if (preferences.contains(preferenceKey))
            // is already saved, use it
            isLargeLayout = preferences.getBoolean(preferenceKey, true);
        else
        {
            // is not saved, compute it

            AppWidgetProviderInfo appWidgetProviderInfo = appWidgetManager.getAppWidgetInfo(appWidgetId);

            int minHeight;
            if (widgetIdOptions != null)
            {
                //int minWidth = newOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH);
                //int maxWidth = newOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH);
                minHeight = widgetIdOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT);
                //int maxHeight = newOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT);

                if ((minHeight == 0) && (appWidgetProviderInfo != null))
                {
                    minHeight = appWidgetProviderInfo.minHeight;
                }
            }
            else
            {
                if (appWidgetProviderInfo != null)
                    minHeight = appWidgetProviderInfo.minHeight;
                else
                    minHeight = 0;

                //if (minHeight == 0)
                //	return;
            }

            isLargeLayout = minHeight >= 110;

            Editor editor = preferences.edit();
            editor.putBoolean(preferenceKey, isLargeLayout);
            editor.apply();
        }

        return isLargeLayout;
        //return true;
    }
    */
    /*
    private static boolean setLayoutParamsMotorola(Context context, int spanX, int spanY, int appWidgetId)
    {
        // for Motorola devices use spanY

        boolean isLargeLayout = spanY != 1;
        
        String preferenceKey = "isLargeLayout_"+appWidgetId;

        Editor editor = ApplicationPreferences.getEditor(context);
        editor.putBoolean(preferenceKey, isLargeLayout);
        editor.apply();

        return isLargeLayout;
        //return true;
    }
    */

    public void onAppWidgetOptionsChanged(Context context, final AppWidgetManager appWidgetManager,
            final int appWidgetId, final Bundle newOptions)
    {
//        PPApplication.logE("[IN_LISTENER] ProfileListWidgetProvider.onAppWidgetOptionsChanged", "xxx");

        final Context appContext = context;
        //PPApplication.startHandlerThreadWidget();
        //final Handler __handler = new Handler(PPApplication.handlerThreadWidget.getLooper());
        //__handler.post(new PPHandlerThreadRunnable(context, appWidgetManager) {
        //__handler.post(() -> {
        Runnable runnable = () -> {
//                PPApplication.logE("[IN_EXECUTOR] PPApplication.startHandlerThreadWidget", "START run - from=ProfileListWidgetProvider.onAppWidgetOptionsChanged");

            //Context appContext= appContextWeakRef.get();
            //AppWidgetManager appWidgetManager = appWidgetManagerWeakRef.get();

            //if ((appContext != null) && (appWidgetManager != null)) {
                //createProfilesDataWrapper(context);

                String preferenceKey = "isLargeLayout_" + appWidgetId;

                // remove preference, will by computed in setLayoutParams
                Editor editor = ApplicationPreferences.getEditor(appContext);
                editor.remove(preferenceKey);
                editor.apply();


                updateAfterWidgetOptionsChanged(appContext, appWidgetId);

                //if (dataWrapper != null)
                //    dataWrapper.invalidateDataWrapper();
                //dataWrapper = null;
            //}
        }; //);
        PPApplication.createDelayedGuiExecutor();
        PPApplication.delayedGuiExecutor.submit(runnable);

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

    private static void updateAfterWidgetOptionsChanged(Context context, int appWidgetId) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);

        doOnUpdate(context, appWidgetManager, appWidgetId, false/*, false*/);
    }

    /*
    private static void _updateWidgets(Context context) {
        try {
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, ProfileListWidgetProvider.class));

            if (appWidgetIds != null) {
                for (int appWidgetId : appWidgetIds) {
                    updateWidget(context, appWidgetId);
                }
            }
        } catch (Exception e) {
            PPApplication.recordException(e);
        }
    }
    */

    static void updateWidgets(final Context context/*, final boolean refresh*/) {
        //createProfilesDataWrapper(context);

        /*DataWrapper dataWrapper = new DataWrapper(context.getApplicationContext(), false, 0, false);
        Profile profile = dataWrapper.getActivatedProfile(false, false);
        //dataWrapper.getEventTimelineList(true);

        String pName;
        if (profile != null)
            pName = DataWrapper.getProfileNameWithManualIndicatorAsString(profile, true, "", true, false, false, dataWrapper);
        else
            pName = context.getString(R.string.profiles_header_profile_name_no_activated);

        if (!refresh) {
            String pNameWidget = PPApplication.prefWidgetProfileName3;

        }

        PPApplication.setWidgetProfileName(context, 3, pName);*/

//        PPApplication.logE("[LOCAL_BROADCAST_CALL] ProfileListWidgetProvider.updateWidgets", "xxx");
        Intent intent3 = new Intent(ACTION_REFRESH_LISTWIDGET);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent3);

        //Intent intent = new Intent(context, ProfileListWidgetProvider.class);
        //intent.setAction(ACTION_REFRESH_LISTWIDGET);
        //context.sendBroadcast(intent);
        //_updateWidgets(context);

        //if (dataWrapper != null)
        //    dataWrapper.invalidateDataWrapper();
        //dataWrapper = null;
    }

/*    private static abstract class PPHandlerThreadRunnable implements Runnable {

        final WeakReference<Context> appContextWeakRef;
        final WeakReference<AppWidgetManager> appWidgetManagerWeakRef;

        PPHandlerThreadRunnable(Context appContext,
                                       AppWidgetManager appWidgetManager) {
            this.appContextWeakRef = new WeakReference<>(appContext);
            this.appWidgetManagerWeakRef = new WeakReference<>(appWidgetManager);
        }

    }*/

}
