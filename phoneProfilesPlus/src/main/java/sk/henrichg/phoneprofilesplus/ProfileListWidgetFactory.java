package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.util.TypedValue;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import androidx.core.graphics.ColorUtils;

import java.util.Comparator;
import java.util.List;

class ProfileListWidgetFactory implements RemoteViewsService.RemoteViewsFactory {

    private DataWrapper dataWrapper;

    private final Context context;

    boolean applicationWidgetListGridLayout;
    boolean applicationWidgetListCompactGrid;

    ProfileListWidgetFactory(Context context,
                             @SuppressWarnings("unused") Intent intent) {
        this.context = context;
        //Log.e("ProfileListWidgetFactory constuctor", "xxxxx");
    }

    public void onCreate() {
        //Log.e("ProfileListWidgetFactory.onCreate", "xxxx");
    }

    public void onDestroy() {
        /*if (dataWrapper != null)
            dataWrapper.invalidateDataWrapper();
        dataWrapper = null;*/
    }

    public int getCount() {
        synchronized (PPApplication.profileListWidgetDatasetChangedMutex) {
            int count = 0;
            if (dataWrapper != null) {
                //if (dataWrapper.profileList != null) {
                for (Profile profile : dataWrapper.profileList) {
                    if (profile._showInActivator)
                        ++count;
                }
                //}
            }
            //Log.e("ProfileListWidgetFactory.getCount", "count="+count);
            return count;
        }
    }

    public Profile getItem(int position) {
        if (getCount() == 0)
            return null;
        else {
            synchronized (PPApplication.profileListWidgetDatasetChangedMutex) {
                Profile _profile = null;
                if (dataWrapper != null) {
                    int pos = -1;
                    for (Profile profile : dataWrapper.profileList) {
                        if (profile._showInActivator)
                            ++pos;

                        if (pos == position) {
                            _profile = profile;
                            break;
                        }
                    }
                }
                /*if (_profile == null)
                    Log.e("ProfileListWidgetFactory.getItem", "_profile=NULL !!!");
                else
                    Log.e("ProfileListWidgetFactory.getItem", "_profile="+_profile._name);*/
                return _profile;
            }
        }
    }

    public RemoteViews getViewAt(int position) {
        synchronized (PPApplication.profileListWidgetDatasetChangedMutex) {
            //Log.e("ProfileListWidgetFactory.getViewAt", "applicationWidgetListGridLayout="+applicationWidgetListGridLayout);
            //Log.e("ProfileListWidgetFactory.getViewAt", "applicationWidgetListCompactGrid="+applicationWidgetListCompactGrid);

            Context appContext  = context;
            LocaleHelper.setApplicationLocale(appContext);

            RemoteViews row;

            String applicationWidgetListLightnessT;
            boolean applicationWidgetListHeader;
            boolean applicationWidgetListPrefIndicator;
            boolean applicationWidgetListChangeColorsByNightMode;
            String applicationWidgetListIconColor;
            boolean applicationWidgetListUseDynamicColors;
            boolean applicationWidgetListBackgroundType;
            String applicationWidgetListLightnessB;
            String applicationWidgetListBackgroundColor;
            String applicationWidgetListBackgroundColorNightModeOff;
            String applicationWidgetListBackgroundColorNightModeOn;

            synchronized (PPApplication.applicationPreferencesMutex) {
                applicationWidgetListLightnessT = ApplicationPreferences.applicationWidgetListLightnessT;
                applicationWidgetListHeader = ApplicationPreferences.applicationWidgetListHeader;
                applicationWidgetListPrefIndicator = ApplicationPreferences.applicationWidgetListPrefIndicator;

                if (Build.VERSION.SDK_INT < 30)
                    applicationWidgetListChangeColorsByNightMode = false;
                else
                    applicationWidgetListChangeColorsByNightMode = ApplicationPreferences.applicationWidgetListChangeColorsByNightMode;

                applicationWidgetListIconColor = ApplicationPreferences.applicationWidgetListIconColor;
                applicationWidgetListUseDynamicColors = ApplicationPreferences.applicationWidgetListUseDynamicColors;
                applicationWidgetListBackgroundType = ApplicationPreferences.applicationWidgetListBackgroundType;
                applicationWidgetListLightnessB = ApplicationPreferences.applicationWidgetListLightnessB;
                applicationWidgetListBackgroundColor = ApplicationPreferences.applicationWidgetListBackgroundColor;
                applicationWidgetListBackgroundColorNightModeOff = ApplicationPreferences.applicationWidgetListBackgroundColorNightModeOff;
                applicationWidgetListBackgroundColorNightModeOn = ApplicationPreferences.applicationWidgetListBackgroundColorNightModeOn;

                if (Build.VERSION.SDK_INT >= 30) {
                    if (Build.VERSION.SDK_INT < 31)
                        applicationWidgetListUseDynamicColors = false;
                    if (//PPApplication.isPixelLauncherDefault(context) ||
                            (applicationWidgetListChangeColorsByNightMode &&
                                    (!applicationWidgetListUseDynamicColors))) {
                        boolean nightModeOn = GlobalGUIRoutines.isNightModeEnabled(appContext);
                        //int nightModeFlags =
                        //        context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
                        //switch (nightModeFlags) {
                        if (nightModeOn) {
                            //case Configuration.UI_MODE_NIGHT_YES:

                            applicationWidgetListLightnessT = GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_87; // lightness of text = white
                            applicationWidgetListBackgroundType = true; // background type = color
                            applicationWidgetListBackgroundColor = String.valueOf(ColorChooserPreference.parseValue(applicationWidgetListBackgroundColorNightModeOn)); // color of background
                            //break;
                        } else {
                            //case Configuration.UI_MODE_NIGHT_NO:
                            //case Configuration.UI_MODE_NIGHT_UNDEFINED:

                            applicationWidgetListLightnessT = GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_12; // lightness of text = black
                            applicationWidgetListBackgroundType = true; // background type = color
                            applicationWidgetListBackgroundColor = String.valueOf(ColorChooserPreference.parseValue(applicationWidgetListBackgroundColorNightModeOff)); // color of background
                            //break;
                        }
                    }
                }
            }

            if (!((Build.VERSION.SDK_INT >= 31) && applicationWidgetListChangeColorsByNightMode &&
                    applicationWidgetListIconColor.equals("0") && applicationWidgetListUseDynamicColors)) {
                if (!applicationWidgetListGridLayout)
                    row = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_profile_list_listitem);
                else {
                    if (applicationWidgetListCompactGrid)
                        row = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_profile_grid_listitem_compact);
                    else
                        row = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_profile_grid_listitem);
                }
            } else {
                if (!applicationWidgetListGridLayout)
                    row = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_profile_list_listitem_dn);
                else {
                    if (applicationWidgetListCompactGrid)
                        row = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_profile_grid_listitem_compact_dn);
                    else
                        row = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.widget_profile_grid_listitem_dn);
                }
            }

            Profile profile = getItem(position);

            if (profile != null) {
                Bitmap bitmap = null;
                if (applicationWidgetListIconColor.equals("0")) {
                    if (applicationWidgetListChangeColorsByNightMode ||
                            ((!applicationWidgetListBackgroundType) &&
                                    (Integer.parseInt(applicationWidgetListLightnessB) <= 25)) ||
                            (applicationWidgetListBackgroundType &&
                                    (ColorUtils.calculateLuminance(Integer.parseInt(applicationWidgetListBackgroundColor)) < 0.23)))
                        bitmap = profile.increaseProfileIconBrightnessForContext(appContext, profile._iconBitmap);
                }
                if (profile.getIsIconResourceID()) {
                    if (bitmap != null)
                        row.setImageViewBitmap(R.id.widget_profile_list_item_profile_icon, bitmap);
                    else {
                        if (profile._iconBitmap != null)
                            row.setImageViewBitmap(R.id.widget_profile_list_item_profile_icon, profile._iconBitmap);
                        else
                            row.setImageViewResource(R.id.widget_profile_list_item_profile_icon,
                                    /*context.getResources().getIdentifier(profile.getIconIdentifier(), "drawable", context.PPApplication.PACKAGE_NAME)*/
                                    ProfileStatic.getIconResource(profile.getIconIdentifier()));
                    }
                } else {
                    if (bitmap != null)
                        row.setImageViewBitmap(R.id.widget_profile_list_item_profile_icon, bitmap);
                    else
                        row.setImageViewBitmap(R.id.widget_profile_list_item_profile_icon, profile._iconBitmap);
                }
                int red = 0xFF;
                int green;
                int blue;
                switch (applicationWidgetListLightnessT) {
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
                green = red;
                blue = red;

                int separatorLightness = red;

                if (!applicationWidgetListHeader) {
                    if (profile._checked) {
                        row.setTextViewTextSize(R.id.widget_profile_list_item_profile_name, TypedValue.COMPLEX_UNIT_DIP, 16);

                        if (!((Build.VERSION.SDK_INT >= 31) && applicationWidgetListChangeColorsByNightMode &&
                                applicationWidgetListIconColor.equals("0") && applicationWidgetListUseDynamicColors))
                            row.setTextColor(R.id.widget_profile_list_item_profile_name, Color.argb(0xFF, red, green, blue));
                        else {
                            // must be removed android:textColor in layout
                            int color = GlobalGUIRoutines.getDynamicColor(R.attr.colorOnBackground, appContext);
                            if (color != 0) {
                                row.setTextColor(R.id.widget_profile_list_item_profile_name, color);
                            }
                        }
                    } else {
                        row.setTextViewTextSize(R.id.widget_profile_list_item_profile_name, TypedValue.COMPLEX_UNIT_DIP, 15);

                        if (!((Build.VERSION.SDK_INT >= 31) && applicationWidgetListChangeColorsByNightMode &&
                                applicationWidgetListIconColor.equals("0") && applicationWidgetListUseDynamicColors))
                            row.setTextColor(R.id.widget_profile_list_item_profile_name, Color.argb(0xCC, red, green, blue));
                        else {
                            // must be removed android:textColor in layout
                            int color = GlobalGUIRoutines.getDynamicColor(R.attr.colorOnBackground, appContext);
                            if (color != 0) {
                                row.setTextColor(R.id.widget_profile_list_item_profile_name,
                                        Color.argb(0xCC, Color.red(color), Color.green(color), Color.blue(color)));
                            }
                        }
                    }
                } else {
                    if (applicationWidgetListGridLayout)
                        row.setTextViewTextSize(R.id.widget_profile_list_item_profile_name, TypedValue.COMPLEX_UNIT_DIP, 15);
                    else
                        row.setTextViewTextSize(R.id.widget_profile_list_item_profile_name, TypedValue.COMPLEX_UNIT_DIP, 16);

                    if (!((Build.VERSION.SDK_INT >= 31) && applicationWidgetListChangeColorsByNightMode &&
                            applicationWidgetListIconColor.equals("0") && applicationWidgetListUseDynamicColors))
                        row.setTextColor(R.id.widget_profile_list_item_profile_name, Color.argb(0xFF, red, green, blue));
                    else {
                        // must be removed android:textColor in layout
                        int color = GlobalGUIRoutines.getDynamicColor(R.attr.colorOnBackground, appContext);
                        if (color != 0) {
                            row.setTextColor(R.id.widget_profile_list_item_profile_name, color);
                        }
                    }
                }

                //if (profile._name.contains("Profile"))
                //    Log.e("ProfileListWidgetFactory.getViewAt", "position="+position+" pofile="+profile._name + " applicationWidgetListHeader="+applicationWidgetListHeader);

                if ((!applicationWidgetListHeader) && (profile._checked)) {
                    if (applicationWidgetListGridLayout && applicationWidgetListCompactGrid) {
                        if (!((Build.VERSION.SDK_INT >= 31) && applicationWidgetListChangeColorsByNightMode &&
                                applicationWidgetListIconColor.equals("0") && applicationWidgetListUseDynamicColors))
                            row.setInt(R.id.widget_profile_list_item_activated_profile_mark, "setBackgroundColor", Color.argb(0xFF, separatorLightness, separatorLightness, separatorLightness));
                            /*else {
                                // but must be removed android:tint in layout
                                int color = GlobalGUIRoutines.getDynamicColor(R.attr.colorOutline, context);
                                if (color != 0) {
                                    Bitmap bitmap = BitmapManipulator.getBitmapFromResource(R.drawable.ic_black, false, context);
                                    bitmap = BitmapManipulator.monochromeBitmap(bitmap, color);
                                    widget.setImageViewBitmap(R.id.widget_profile_list_header_separator, bitmap);
                                }
                            }*/

                        //if (profile._name.contains("Profile"))
                        //    Log.e("ProfileListWidgetFactory.getViewAt", "VISIBLE");
                        row.setViewVisibility(R.id.widget_profile_list_item_activated_profile_mark, View.VISIBLE);
                    } else {
                        // hm, interesting, how to set bold style for RemoteView text ;-)
                        Spannable profileName = DataWrapperStatic.getProfileNameWithManualIndicator(profile, !applicationWidgetListGridLayout,
                                "", true, true, applicationWidgetListGridLayout, dataWrapper);
                        Spannable sb = new SpannableString(profileName);
                        sb.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, profileName.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        row.setTextViewText(R.id.widget_profile_list_item_profile_name, sb);

                        //if (profile._name.contains("Profile"))
                        //    Log.e("ProfileListWidgetFactory.getViewAt", "** INVISIBLE (1)");
                        row.setViewVisibility(R.id.widget_profile_list_item_activated_profile_mark, View.INVISIBLE);
                    }
                } else {
                    Spannable profileName = profile.getProfileNameWithDuration("", "",
                            true/*applicationWidgetListGridLayout*/, applicationWidgetListGridLayout, appContext);
                    row.setTextViewText(R.id.widget_profile_list_item_profile_name, profileName);

                    //if (profile._name.contains("Profile"))
                    //    Log.e("ProfileListWidgetFactory.getViewAt", "** INVISIBLE (2)");
                    row.setViewVisibility(R.id.widget_profile_list_item_activated_profile_mark, View.INVISIBLE);
                }
                if (!applicationWidgetListGridLayout) {
                    if (applicationWidgetListPrefIndicator) {
                        if (profile._preferencesIndicator != null) {
                            row.setImageViewBitmap(R.id.widget_profile_list_profile_pref_indicator, profile._preferencesIndicator);
                            row.setViewVisibility(R.id.widget_profile_list_profile_pref_indicator, View.VISIBLE);
                        }
                        else {
                            row.setImageViewResource(R.id.widget_profile_list_profile_pref_indicator, R.drawable.ic_empty);
                            row.setViewVisibility(R.id.widget_profile_list_profile_pref_indicator, View.GONE);
                        }
                    } else
                        //row.setImageViewResource(R.id.widget_profile_list_profile_pref_indicator, R.drawable.ic_empty);
                        row.setViewVisibility(R.id.widget_profile_list_profile_pref_indicator, View.GONE);
                }

                Intent i = new Intent();
                Bundle extras = new Bundle();

                if ((!applicationWidgetListHeader) &&
                        EventStatic.getGlobalEventsRunning(appContext) && (position == 0))
                    extras.putLong(PPApplication.EXTRA_PROFILE_ID, Profile.RESTART_EVENTS_PROFILE_ID);
                else
                    extras.putLong(PPApplication.EXTRA_PROFILE_ID, profile._id);
                extras.putInt(PPApplication.EXTRA_STARTUP_SOURCE, PPApplication.STARTUP_SOURCE_SHORTCUT);
                i.putExtras(extras);
                row.setOnClickFillInIntent(R.id.widget_profile_list_item, i);

            }

            //Log.e("ProfileListWidgetFactory.getViewAt", "END");

            return (row);
        }
    }

    public RemoteViews getLoadingView() {
        return(null);
    }
  
    public int getViewTypeCount() {
        return 1;
    }

    public long getItemId(int position) {
        return position;
    }

    public boolean hasStableIds() {
        return false;
    }

    private DataWrapper createProfilesDataWrapper(boolean local,
                                                  String applicationWidgetListIconLightness,
                                                  String applicationWidgetListIconColor,
                                                  boolean applicationWidgetListCustomIconLightness,
                                                  String applicationWidgetListPrefIndicatorLightness,
                                                  boolean applicationWidgetListChangeColorsByNightMode,
                                                  boolean applicationWidgetListUseDynamicColors,
                                                  boolean applicationWidgetListBackgroundType,
                                                  String applicationWidgetListBackgroundColor,
                                                  String applicationWidgetListLightnessB)
    {
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
            if (Integer.parseInt(applicationWidgetListLightnessB) <= 37)
                indicatorType = DataWrapper.IT_FOR_WIDGET_DARK_BACKGROUND;
            else
                indicatorType = DataWrapper.IT_FOR_WIDGET_LIGHT_BACKGROUND;
        }

        if (local) {
            return new DataWrapper(context.getApplicationContext(), applicationWidgetListIconColor.equals("1"),
                    monochromeValue, applicationWidgetListCustomIconLightness,
                    indicatorType, prefIndicatorMonochromeValue, prefIndicatorLightnessValue);
        }
        else {
            if (dataWrapper == null) {
                dataWrapper = new DataWrapper(context.getApplicationContext(), applicationWidgetListIconColor.equals("1"),
                        monochromeValue, applicationWidgetListCustomIconLightness,
                        indicatorType, prefIndicatorMonochromeValue, prefIndicatorLightnessValue);
            } else {
                dataWrapper.setParameters(applicationWidgetListIconColor.equals("1"),
                        monochromeValue, applicationWidgetListCustomIconLightness,
                        indicatorType, prefIndicatorMonochromeValue, prefIndicatorLightnessValue);
            }
            return dataWrapper;
        }
    }

    /*
        Called when notifyDataSetChanged() is triggered on the remote adapter. This allows a RemoteViewsFactory to
        respond to data changes by updating any internal references.

        Note: expensive tasks can be safely performed synchronously within this method. In the interim,
        the old data will be displayed within the widget.
    */
    public void onDataSetChanged() {
//        PPApplicationStatic.logE("[IN_LISTENER] ProfileListWidgetFactory.onDataSetChanged", "START");

        String applicationWidgetListIconColor;
        String applicationWidgetListIconLightness;
        boolean applicationWidgetListCustomIconLightness;
        boolean applicationWidgetListPrefIndicator;
        String applicationWidgetListPrefIndicatorLightness;
        boolean applicationWidgetListHeader;
        boolean applicationWidgetListChangeColorsByNightMode;
        boolean applicationWidgetListUseDynamicColors;
        boolean applicationWidgetListBackgroundType;
        String applicationWidgetListBackgroundColor;
        String applicationWidgetListLightnessB;
        String applicationWidgetListBackgroundColorNightModeOff;
        String applicationWidgetListBackgroundColorNightModeOn;

        synchronized (PPApplication.applicationPreferencesMutex) {
            applicationWidgetListIconLightness = ApplicationPreferences.applicationWidgetListIconLightness;
            applicationWidgetListIconColor = ApplicationPreferences.applicationWidgetListIconColor;
            applicationWidgetListCustomIconLightness = ApplicationPreferences.applicationWidgetListCustomIconLightness;
            applicationWidgetListPrefIndicator = ApplicationPreferences.applicationWidgetListPrefIndicator;
            applicationWidgetListPrefIndicatorLightness = ApplicationPreferences.applicationWidgetListPrefIndicatorLightness;
            applicationWidgetListHeader = ApplicationPreferences.applicationWidgetListHeader;

            if (Build.VERSION.SDK_INT < 30)
                applicationWidgetListChangeColorsByNightMode = false;
            else
                applicationWidgetListChangeColorsByNightMode = ApplicationPreferences.applicationWidgetListChangeColorsByNightMode;

            applicationWidgetListUseDynamicColors = ApplicationPreferences.applicationWidgetListUseDynamicColors;
            applicationWidgetListBackgroundType = ApplicationPreferences.applicationWidgetListBackgroundType;
            applicationWidgetListBackgroundColor = ApplicationPreferences.applicationWidgetListBackgroundColor;
            applicationWidgetListLightnessB = ApplicationPreferences.applicationWidgetListLightnessB;
            applicationWidgetListBackgroundColorNightModeOff = ApplicationPreferences.applicationWidgetListBackgroundColorNightModeOff;
            applicationWidgetListBackgroundColorNightModeOn = ApplicationPreferences.applicationWidgetListBackgroundColorNightModeOn;

            if (Build.VERSION.SDK_INT >= 30) {
                if (Build.VERSION.SDK_INT < 31)
                    applicationWidgetListUseDynamicColors = false;
                if (applicationWidgetListChangeColorsByNightMode &&
                        (!applicationWidgetListUseDynamicColors)) {
                    boolean nightModeOn = GlobalGUIRoutines.isNightModeEnabled(context.getApplicationContext());
                    //int nightModeFlags =
                    //        context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
                    //switch (nightModeFlags) {
                    if (nightModeOn) {
                        //case Configuration.UI_MODE_NIGHT_YES:

                        //applicationWidgetListIconColor = "0"; // icon type = colorful
                        applicationWidgetListIconLightness = GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_75;
                        applicationWidgetListBackgroundColor = String.valueOf(ColorChooserPreference.parseValue(applicationWidgetListBackgroundColorNightModeOn)); // color of background
                        //applicationWidgetListPrefIndicatorLightness = GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_62; // lightness of preference indicators
                        //break;
                    } else {
                        //case Configuration.UI_MODE_NIGHT_NO:
                        //case Configuration.UI_MODE_NIGHT_UNDEFINED:

                        //applicationWidgetListIconColor = "0"; // icon type = colorful
                        applicationWidgetListIconLightness = GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_62;
                        applicationWidgetListBackgroundColor = String.valueOf(ColorChooserPreference.parseValue(applicationWidgetListBackgroundColorNightModeOff)); // color of background
                        //applicationWidgetListPrefIndicatorLightness = GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_50; // lightness of preference indicators
                        //break;
                    }
                }
            }
        }

        //Log.e("ProfileListWidgetFactory.onDataSetChanged", "applicationWidgetListHeader="+applicationWidgetListHeader);

        DataWrapper _dataWrapper = createProfilesDataWrapper(true,
                applicationWidgetListIconLightness,
                applicationWidgetListIconColor,
                applicationWidgetListCustomIconLightness,
                applicationWidgetListPrefIndicatorLightness,
                applicationWidgetListChangeColorsByNightMode,
                applicationWidgetListUseDynamicColors,
                applicationWidgetListBackgroundType,
                applicationWidgetListBackgroundColor,
                applicationWidgetListLightnessB);

        //List<Profile> newProfileList = _dataWrapper.getNewProfileList(true, applicationWidgetListPrefIndicator);
        List<Profile> newProfileList = _dataWrapper.getNewProfileList(false, false);

        _dataWrapper.getEventTimelineList(true);

        if (!applicationWidgetListHeader)
        {
            // show activated profile in list if is not showed in activator
            Profile profile = _dataWrapper.getActivatedProfile(newProfileList);
            if ((profile != null) && (!profile._showInActivator))
            {
                profile._showInActivator = true;
                profile._porder = -1;
            }
        }

        for (Profile profile : newProfileList) {
            if (profile._showInActivator) {
                _dataWrapper.generateProfileIcon(profile, true, applicationWidgetListPrefIndicator);
            }
        }

        newProfileList.sort(new ProfileComparator());

        Profile restartEvents = null;
        if ((!applicationWidgetListHeader) &&
                EventStatic.getGlobalEventsRunning(context)) {
            //restartEvents = DataWrapper.getNonInitializedProfile(context.getString(R.string.menu_restart_events), "ic_profile_restart_events|1|0|0", 0);
            restartEvents = DataWrapperStatic.getNonInitializedProfile(context.getString(R.string.menu_restart_events),
                    StringConstants.PROFILE_ICON_RESTART_EVENTS+"|1|1|" + ApplicationPreferences.applicationRestartEventsIconColor, 0);
            restartEvents._showInActivator = true;
            newProfileList.add(0, restartEvents);
        }
        _dataWrapper.invalidateDataWrapper();

        synchronized (PPApplication.profileListWidgetDatasetChangedMutex) {

            if (dataWrapper != null)
                dataWrapper.invalidateDataWrapper();
            createProfilesDataWrapper(false,
                    applicationWidgetListIconLightness,
                    applicationWidgetListIconColor,
                    applicationWidgetListCustomIconLightness,
                    applicationWidgetListPrefIndicatorLightness,
                    applicationWidgetListChangeColorsByNightMode,
                    applicationWidgetListUseDynamicColors,
                    applicationWidgetListBackgroundType,
                    applicationWidgetListBackgroundColor,
                    applicationWidgetListLightnessB);
            //if (dataWrapper != null) {
            //dataWrapper.invalidateProfileList();
            if (restartEvents != null)
                dataWrapper.generateProfileIcon(restartEvents, true, false);
            dataWrapper.setProfileList(newProfileList);
            //profileList = newProfileList;
            //}

        }

        //Log.e("ProfileListWidgetFactory.onDataSetChanged", "END");
    }

    private static class ProfileComparator implements Comparator<Profile> {

        public int compare(Profile lhs, Profile rhs) {
            int res = 0;
            if ((lhs != null) && (rhs != null))
                res = lhs._porder - rhs._porder;
            return res;
        }
    }

}
