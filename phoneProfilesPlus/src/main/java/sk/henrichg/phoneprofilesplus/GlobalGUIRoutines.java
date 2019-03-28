package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.preference.Preference;
import android.preference.PreferenceManager;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.appcompat.widget.AppCompatImageButton;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.CharacterStyle;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.util.TypedValue;

import java.lang.reflect.Method;
import java.text.Collator;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

class GlobalGUIRoutines {

    static Collator collator = null;

    // import/export
    static final String DB_FILEPATH = "/data/" + PPApplication.PACKAGE_NAME + "/databases";
    static final String REMOTE_EXPORT_PATH = "/PhoneProfiles";
    static final String EXPORT_APP_PREF_FILENAME = "ApplicationPreferences.backup";
    static final String EXPORT_DEF_PROFILE_PREF_FILENAME = "DefaultProfilePreferences.backup";

    // https://stackoverflow.com/questions/40221711/android-context-getresources-updateconfiguration-deprecated
    // but my solution working also in Android 8.1
    public static void setLanguage(Context context)//, boolean restart)
    {
        //if (android.os.Build.VERSION.SDK_INT < 24) {

            String lang = ApplicationPreferences.applicationLanguage(context);

            Locale appLocale;

            if (!lang.equals("system")) {
                String[] langSplit = lang.split("-");
                if (langSplit.length == 1)
                    appLocale = new Locale(lang);
                else
                    appLocale = new Locale(langSplit[0], langSplit[1]);
            } else {
                //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                //    appLocale = Resources.getSystem().getConfiguration().getLocales().get(0);
                //else
                    appLocale = Resources.getSystem().getConfiguration().locale;
            }

            Locale.setDefault(appLocale);
            Configuration appConfig = new Configuration();
            //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            //    appConfig.setLocale(appLocale);
            //else
                appConfig.locale = appLocale;

            //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            //    Context context  = context.createConfigurationContext(appConfig);
            //else
            context.getResources().updateConfiguration(appConfig, context.getResources().getDisplayMetrics());
        //}

        // collator for application locale sorting
        collator = getCollator(context);

        PPApplication.createNotificationChannels(context);
    }

    private static Collator getCollator(Context context)
    {
        //if (android.os.Build.VERSION.SDK_INT < 24) {
            // get application Locale
            String lang = ApplicationPreferences.applicationLanguage(context);
            Locale appLocale;
            if (!lang.equals("system")) {
                String[] langSplit = lang.split("-");
                if (langSplit.length == 1)
                    appLocale = new Locale(lang);
                else
                    appLocale = new Locale(langSplit[0], langSplit[1]);
            } else {
                //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                //    appLocale = Resources.getSystem().getConfiguration().getLocales().get(0);
                //} else {
                    appLocale = Resources.getSystem().getConfiguration().locale;
                //}
            }
            // get collator for application locale
            return Collator.getInstance(appLocale);
        /*}
        else {
            //Log.d("GlobalGUIRoutines.getCollator", java.util.Locale.getDefault().toString());
            return Collator.getInstance();
        }*/
    }

    public static void setTheme(Activity activity, boolean forPopup, boolean withToolbar, boolean withDrawerLayout)
    {
        int theme = getTheme(forPopup, withToolbar, withDrawerLayout, activity);
        if (theme != 0)
            activity.setTheme(theme);
    }

    static int getTheme(boolean forPopup, boolean withToolbar, boolean withDrawerLayout, Context context) {
        switch (ApplicationPreferences.applicationTheme(context, true)) {
            case "color":
                if (forPopup) {
                    if (withToolbar)
                        return R.style.PopupTheme_withToolbar_color;
                    else
                        return R.style.PopupTheme_color;
                } else {
                    if (withToolbar) {
                        if (withDrawerLayout)
                            return R.style.Theme_PhoneProfilesTheme_withToolbar_withDrawerLayout_color;
                        else
                            return R.style.Theme_PhoneProfilesTheme_withToolbar_color;
                    } else
                        return R.style.Theme_PhoneProfilesTheme_color;
                }
            case "white":
                if (forPopup) {
                    if (withToolbar)
                        return R.style.PopupTheme_withToolbar_white;
                    else
                        return R.style.PopupTheme_white;
                } else {
                    if (withToolbar) {
                        if (withDrawerLayout)
                            return R.style.Theme_PhoneProfilesTheme_withToolbar_withDrawerLayout_white;
                        else
                            return R.style.Theme_PhoneProfilesTheme_withToolbar_white;
                    } else
                        return R.style.Theme_PhoneProfilesTheme_white;
                }
            case "dark":
                if (forPopup) {
                    if (withToolbar)
                        return R.style.PopupTheme_withToolbar_dark;
                    else
                        return R.style.PopupTheme_dark;
                } else {
                    if (withToolbar) {
                        if (withDrawerLayout)
                            return R.style.Theme_PhoneProfilesTheme_withToolbar_withDrawerLayout_dark;
                        else
                            return R.style.Theme_PhoneProfilesTheme_withToolbar_dark;
                    } else
                        return R.style.Theme_PhoneProfilesTheme_dark;
                }
            case "dlight":
                if (forPopup) {
                    if (withToolbar)
                        return R.style.PopupTheme_withToolbar_dlight;
                    else
                        return R.style.PopupTheme_dlight;
                } else {
                    if (withToolbar) {
                        if (withDrawerLayout)
                            return R.style.Theme_PhoneProfilesTheme_withToolbar_withDrawerLayout_dlight;
                        else
                            return R.style.Theme_PhoneProfilesTheme_withToolbar_dlight;
                    } else
                        return R.style.Theme_PhoneProfilesTheme_dlight;
                }
            default:
                if (forPopup) {
                    if (withToolbar)
                        return R.style.PopupTheme_withToolbar_color;
                    else
                        return R.style.PopupTheme_color;
                } else {
                    if (withToolbar) {
                        if (withDrawerLayout)
                            return R.style.Theme_PhoneProfilesTheme_withToolbar_withDrawerLayout_color;
                        else
                            return R.style.Theme_PhoneProfilesTheme_withToolbar_color;
                    } else
                        return R.style.Theme_PhoneProfilesTheme_color;
                }
        }
    }

    static void reloadActivity(final Activity activity, boolean newIntent)
    {
        if (newIntent)
        {
            new Handler(activity.getMainLooper()).post(new Runnable() {

                @Override
                public void run() {
                    try {
                        Intent intent = activity.getIntent();
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        activity.finish();
                        activity.overridePendingTransition(0, 0);

                        activity.startActivity(intent);
                        activity.overridePendingTransition(0, 0);
                    } catch (Exception ignored) {}
                }
            });
        }
        else
            activity.recreate();
    }

    public static void setPreferenceTitleStyle(Preference preference, boolean enabled, boolean bold, boolean underline, boolean errorColor, boolean systemSettings)
    {
        if (preference != null) {
            CharSequence title = preference.getTitle();
            if (systemSettings) {
                String s = title.toString();
                if (!s.contains("(S)"))
                    title = TextUtils.concat("(S) ", title);
            }
            Spannable sbt = new SpannableString(title);
            Object spansToRemove[] = sbt.getSpans(0, title.length(), Object.class);
            for (Object span : spansToRemove) {
                if (span instanceof CharacterStyle)
                    sbt.removeSpan(span);
            }
            if (bold || underline) {
                if (bold)
                    sbt.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, sbt.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                if (underline)
                    sbt.setSpan(new UnderlineSpan(), 0, sbt.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                if (errorColor && enabled)
                    sbt.setSpan(new ForegroundColorSpan(Color.RED), 0, sbt.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                preference.setTitle(sbt);
            } else {
                preference.setTitle(sbt);
            }
        }
    }

    static void setPreferenceTitleStyleX(androidx.preference.Preference preference, boolean enabled, boolean bold, boolean underline, boolean errorColor, boolean systemSettings)
    {
        if (preference != null) {
            CharSequence title = preference.getTitle();
            if (systemSettings) {
                String s = title.toString();
                if (!s.contains("(S)"))
                    title = TextUtils.concat("(S) ", title);
            }
            Spannable sbt = new SpannableString(title);
            Object spansToRemove[] = sbt.getSpans(0, title.length(), Object.class);
            for (Object span : spansToRemove) {
                if (span instanceof CharacterStyle)
                    sbt.removeSpan(span);
            }
            if (bold || underline) {
                if (bold)
                    sbt.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, sbt.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                if (underline)
                    sbt.setSpan(new UnderlineSpan(), 0, sbt.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                if (errorColor && enabled)
                    sbt.setSpan(new ForegroundColorSpan(Color.RED), 0, sbt.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                preference.setTitle(sbt);
            } else {
                preference.setTitle(sbt);
            }
        }
    }

    /**
     * Sets the specified image button to the given state, while modifying or
     * "graying-out" the icon as well
     *
     * @param enabled The state of the menu item
     * @param item The menu item to modify
     * @param iconResId The icon ID
     */
    static void setImageButtonEnabled(boolean enabled, AppCompatImageButton item, int iconResId, Context context) {
        item.setEnabled(enabled);
        Drawable originalIcon = ContextCompat.getDrawable(context, iconResId);
        Drawable icon = enabled ? originalIcon : convertDrawableToGrayScale(originalIcon);
        item.setImageDrawable(icon);
    }

    /**
     * Mutates and applies a filter that converts the given drawable to a Gray
     * image. This method may be used to simulate the color of disable icons in
     * Honeycomb's ActionBar.
     *
     * @return a mutated version of the given drawable with a color filter
     *         applied.
     */
    private static Drawable convertDrawableToGrayScale(Drawable drawable) {
        if (drawable == null) {
            return null;
        }
        Drawable res = drawable.mutate();
        res.setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_IN);
        return res;
    }

    /*
    static float pixelsToSp(Context context, float px) {
        return px / context.getResources().getDisplayMetrics().scaledDensity;
    }
    */

    /*
    private static float spToPixels(Context context, float sp) {
        return sp * context.getResources().getDisplayMetrics().scaledDensity;
    }
    */

    @SuppressWarnings("SameParameterValue")
    static int dpToPx(int dp)
    {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    /*
    static int pxToDp(int px)
    {
        return (int) (px / Resources.getSystem().getDisplayMetrics().density);
    } */

    /**
     * Uses reflection to access divider private attribute and override its color
     * Use Color.Transparent if you wish to hide them
     */
    /*static void setSeparatorColorForNumberPicker(NumberPicker picker, int separatorColor) {
        Field[] pickerFields = NumberPicker.class.getDeclaredFields();
        for (Field pf : pickerFields) {
            if (pf.getName().equals("mSelectionDivider")) {
                pf.setAccessible(true);
                try {
                    pf.set(picker, new ColorDrawable(separatorColor));
                } catch (IllegalAccessException | IllegalArgumentException ignored) {
                }
                break;
            }
        }
    }*/
    /*
    static void updateTextAttributesForNumberPicker(NumberPicker picker, int textSizeSP) {
        for (int i = 0; i < picker.getChildCount(); i++){
            View child = picker.getChildAt(i);
            if (child instanceof EditText) {
                try {
                    Field selectorWheelPaintField = NumberPicker.class.getDeclaredField("mSelectorWheelPaint");
                    selectorWheelPaintField.setAccessible(true);

                    Paint wheelPaint = ((Paint)selectorWheelPaintField.get(picker));
                    //wheelPaint.setColor(textColor);
                    wheelPaint.setTextSize(spToPixels(picker.getContext(), textSizeSP));

                    EditText editText = ((EditText) child);
                    //editText.setTextColor(textColor);
                    editText.setTextSize(textSizeSP);

                    picker.invalidate();
                    break;
                }
                catch (NoSuchFieldException | IllegalAccessException | IllegalArgumentException ignored) {
                }
            }
        }
    }
    */

    static String formatDateTime(Context context, String timeToFormat) {

        String finalDateTime = "";

        @SuppressLint("SimpleDateFormat") SimpleDateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        Date date;
        if (timeToFormat != null) {
            try {
                date = iso8601Format.parse(timeToFormat);
            } catch (ParseException e) {
                date = null;
            }

            if (date != null) {
                long when = date.getTime();
                when += TimeZone.getDefault().getOffset(when);

                /*
                int flags = 0;
                flags |= DateUtils.FORMAT_SHOW_TIME;
                flags |= DateUtils.FORMAT_SHOW_DATE;
                flags |= DateUtils.FORMAT_NUMERIC_DATE;
                flags |= DateUtils.FORMAT_SHOW_YEAR;

                finalDateTime = android.text.format.DateUtils.formatDateTime(context,
                        when, flags);

                finalDateTime = DateFormat.getDateFormat(context).format(when) +
                        " " + DateFormat.getTimeFormat(context).format(when);
                */

                /*
                SimpleDateFormat sdf = new SimpleDateFormat("d.MM.yyyy HH:mm:ss");
                finalDateTime = sdf.format(when);
                */

                finalDateTime = timeDateStringFromTimestamp(context, when);
            }
        }
        return finalDateTime;
    }

    @SuppressLint("SimpleDateFormat")
    static String timeDateStringFromTimestamp(Context applicationContext, long timestamp){
        String timeDate;
        String androidDateTime=android.text.format.DateFormat.getDateFormat(applicationContext).format(new Date(timestamp))+" "+
                android.text.format.DateFormat.getTimeFormat(applicationContext).format(new Date(timestamp));
        String javaDateTime = DateFormat.getDateTimeInstance().format(new Date(timestamp));
        String AmPm="";
        if(!Character.isDigit(androidDateTime.charAt(androidDateTime.length()-1))) {
            if(androidDateTime.contains(new SimpleDateFormat().getDateFormatSymbols().getAmPmStrings()[Calendar.AM])){
                AmPm=" "+new SimpleDateFormat().getDateFormatSymbols().getAmPmStrings()[Calendar.AM];
            }else{
                AmPm=" "+new SimpleDateFormat().getDateFormatSymbols().getAmPmStrings()[Calendar.PM];
            }
            androidDateTime=androidDateTime.replace(AmPm, "");
        }
        if(!Character.isDigit(javaDateTime.charAt(javaDateTime.length()-1))){
            javaDateTime=javaDateTime.replace(" "+new SimpleDateFormat().getDateFormatSymbols().getAmPmStrings()[Calendar.AM], "");
            javaDateTime=javaDateTime.replace(" "+new SimpleDateFormat().getDateFormatSymbols().getAmPmStrings()[Calendar.PM], "");
        }
        javaDateTime=javaDateTime.substring(javaDateTime.length()-3);
        timeDate=androidDateTime.concat(javaDateTime);
        return timeDate.concat(AmPm);
    }

    public static Spanned fromHtml(String source) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Html.fromHtml(source, Html.FROM_HTML_MODE_LEGACY);
        } else {
            //noinspection deprecation
            return Html.fromHtml(source);
        }
    }

    @SuppressLint("DefaultLocale")
    static String getDurationString(int duration) {
        int hours = duration / 3600;
        int minutes = (duration % 3600) / 60;
        int seconds = duration % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    @SuppressLint("DefaultLocale")
    static String getEndsAtString(int duration) {
        if(duration == 0) {
            return "--";
        }

        Calendar ends = Calendar.getInstance();
        ends.add(Calendar.SECOND, duration);
        int hours = ends.get(Calendar.HOUR_OF_DAY);
        int minutes = ends.get(Calendar.MINUTE);
        int seconds = ends.get(Calendar.SECOND);
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    /*
    static Point getNavigationBarSize(Context context) {
        Point appUsableSize = getAppUsableScreenSize(context);
        Point realScreenSize = getRealScreenSize(context);

        if ((appUsableSize != null) && (realScreenSize != null)) {
            // navigation bar on the right
            if (appUsableSize.x < realScreenSize.x) {
                return new Point(realScreenSize.x - appUsableSize.x, appUsableSize.y);
            }

            // navigation bar at the bottom
            if (appUsableSize.y < realScreenSize.y) {
                return new Point(appUsableSize.x, realScreenSize.y - appUsableSize.y);
            }

            // navigation bar is not present
            return new Point();
        }
        else
            return null;
    }
    */

    /*
    private static Point getAppUsableScreenSize(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        if (windowManager != null) {
            Display display = windowManager.getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            PPApplication.logE("GlobalGUIRoutines.getAppUsableScreenSize", "size.y="+size.y);
            return size;
        }
        else
            return null;
    }

    static Point getRealScreenSize(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        if (windowManager != null) {
            Display display = windowManager.getDefaultDisplay();
            Point size = new Point();

            //if (Build.VERSION.SDK_INT >= 17) {
                display.getRealSize(size);
            //} else {
            //    try {
            //        size.x = (Integer) Display.class.getMethod("getRawWidth").invoke(display);
            //        size.y = (Integer) Display.class.getMethod("getRawHeight").invoke(display);
            //    } catch (Exception ignored) {
            //    }
            //}

            PPApplication.logE("GlobalGUIRoutines.getRealScreenSize", "size.y="+size.y);
            return size;
        }
        else
            return null;
    }
    static int getStatusBarHeight(Context context) {
        //int result = 0;
        //int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        //if (resourceId > 0) {
        //    result = context.getResources().getDimensionPixelSize(resourceId);
        //}
        //return result;
        final Resources resources = context.getResources();
        final int resourceId = resources.getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0)
            return resources.getDimensionPixelSize(resourceId);
        else
            return (int) Math.ceil((Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? 24 : 25) * resources.getDisplayMetrics().density);
    }
    static int getNavigationBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }
    */

    static int getThemeAccentColor(final Context context) {
        final TypedValue value = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.colorAccent, value, true);
        return value.data;
    }

    static int getThemeTextColor(final Context context) {
        final TypedValue value = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.activityTextColor, value, true);
        return value.data;
    }

    /*
    static int getThemeDisabledTextColor(final Context context) {
        final TypedValue value = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.activityDisabledTextColor, value, true);
        return value.data;
    }
    */

    static int getThemeCommandBackgroundColor(final Context context) {
        final TypedValue value = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.activityCommandBackgroundColor, value, true);
        return value.data;
    }

    static int getThemeColorControlHighlight(final Context context) {
        final TypedValue value = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.colorControlHighlight, value, true);
        return value.data;
    }

    /*
    static int getThemeEventPauseColor(final Context context) {
        final TypedValue value = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.eventPauseTextColor, value, true);
        return value.data;
    }
    */

    static int getThemeEventStopColor(final Context context) {
        final TypedValue value = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.eventStopTextColor, value, true);
        return value.data;
    }

    static int getThemeEventStopStatusIndicator(final Context context) {
        //final TypedValue value = new TypedValue();
        //context.getTheme().resolveAttribute(R.attr.eventStopStatusIndicator, value, true);
        //return value.data;
        int theme = GlobalGUIRoutines.getTheme(false, false, false, context);
        if (theme != 0) {
            TypedArray a = context.getTheme().obtainStyledAttributes(theme, new int[]{R.attr.eventStopStatusIndicator});
            return a.getResourceId(0, 0);
        }
        else
            return 0;
    }

    /*
    static int getThemeEventInDelayColor(final Context context) {
        final TypedValue value = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.eventInDelayTextColor, value, true);
        return value.data;
    }
    */

    static int getThemeSensorPassStatusColor(final int passStatus, final Context context) {
        final TypedValue value = new TypedValue();
        if (passStatus == EventPreferences.SENSOR_PASSED_PASSED)
            context.getTheme().resolveAttribute(R.attr.sensorPassStatusPassed, value, true);
        else
        if (passStatus == EventPreferences.SENSOR_PASSED_NOT_PASSED)
            context.getTheme().resolveAttribute(R.attr.sensorPassStatusNotPassed, value, true);
        else
            context.getTheme().resolveAttribute(R.attr.sensorPassStatusWaiting, value, true);
        return value.data;
    }

    static int getThemeActivatorGridDividerColor(final boolean show, final Context context) {
        final TypedValue value = new TypedValue();
        if (show)
            context.getTheme().resolveAttribute(android.R.attr.listDivider, value, false);
        else
            context.getTheme().resolveAttribute(R.attr.activityBackgroundColor, value, false);
        return value.data;
    }

    /*
    static int getThemeActivityLogTypeOtherColor(final Context context) {
        final TypedValue value = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.activityLogTypeOther, value, true);
        return value.data;
    }
    */

    /*
    static int getResourceId(String pVariableName, String pResourceName, Context context)
    {
        try {
            return context.getResources().getIdentifier(pVariableName, pResourceName, context.getPackageName());
        } catch (Exception e) {
            return -1;
        }
    }
    */

    static boolean activityActionExists(String action, Context context) {
        try {
            final Intent intent = new Intent(action);
            List<ResolveInfo> activities = context.getApplicationContext().getPackageManager().queryIntentActivities(intent, 0);
            return activities.size() > 0;
        } catch (Exception e) {
            return false;
        }
    }

    static boolean activityIntentExists(Intent intent, Context context) {
        try {
            List<ResolveInfo> activities = context.getApplicationContext().getPackageManager().queryIntentActivities(intent, 0);
            return activities.size() > 0;
        } catch (Exception e) {
            return false;
        }
    }

    static void registerOnActivityDestroyListener(@NonNull Preference preference, @NonNull PreferenceManager.OnActivityDestroyListener listener) {
        try {
            PreferenceManager pm = preference.getPreferenceManager();
            @SuppressLint("PrivateApi")
            Method method = pm.getClass().getDeclaredMethod(
                    "registerOnActivityDestroyListener",
                    PreferenceManager.OnActivityDestroyListener.class);
            method.setAccessible(true);
            method.invoke(pm, listener);
        } catch (Exception ignored) {
        }
    }

    static void unregisterOnActivityDestroyListener(@NonNull Preference preference, @NonNull PreferenceManager.OnActivityDestroyListener listener) {
        try {
            PreferenceManager pm = preference.getPreferenceManager();
            @SuppressLint("PrivateApi")
            Method method = pm.getClass().getDeclaredMethod(
                    "unregisterOnActivityDestroyListener",
                    PreferenceManager.OnActivityDestroyListener.class);
            method.setAccessible(true);
            method.invoke(pm, listener);
        } catch (Exception ignored) {
        }
    }

    static void lockScreenOrientation(Activity activity) {
        try {
            int currentOrientation = activity.getResources().getConfiguration().orientation;
            if (currentOrientation == Configuration.ORIENTATION_PORTRAIT) {
                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
            } else {
                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
            }
            // FC in tablets: java.lang.IllegalStateException: Only fullscreen activities can request orientation
        } catch (Exception ignored) {}
    }

    static void unlockScreenOrientation(Activity activity) {
        try {
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
            // FC in tablets: java.lang.IllegalStateException: Only fullscreen activities can request orientation
        } catch (Exception ignored) {}
    }

}
