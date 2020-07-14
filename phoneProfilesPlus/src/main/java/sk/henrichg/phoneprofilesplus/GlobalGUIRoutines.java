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
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.provider.Settings;
import android.text.Editable;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.BulletSpan;
import android.text.style.CharacterStyle;
import android.text.style.ForegroundColorSpan;
import android.text.style.LeadingMarginSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.core.content.ContextCompat;

import org.xml.sax.XMLReader;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import mobi.upod.timedurationpicker.TimeDurationPicker;

import static android.os.Looper.getMainLooper;

class GlobalGUIRoutines {

    // import/export
    static final String DB_FILEPATH = "/data/" + PPApplication.PACKAGE_NAME + "/databases";
    //static final String REMOTE_EXPORT_PATH = "/PhoneProfiles";
    static final String EXPORT_APP_PREF_FILENAME = "ApplicationPreferences.backup";
    static final String EXPORT_DEF_PROFILE_PREF_FILENAME = "DefaultProfilePreferences.backup";

    static final int ICON_SIZE_DP = 50;

    /*
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
                else {
                    if ((langSplit[0].equals("sr")) && (langSplit[1].equals("Latn")))
                        appLocale = new Locale.Builder().setLanguage("sr").setScript("Latn").build();
                    else
                        appLocale = new Locale(langSplit[0], langSplit[1]);
                }
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                    appLocale = Resources.getSystem().getConfiguration().getLocales().get(0);
                else
                    appLocale = Resources.getSystem().getConfiguration().locale;
            }

            Locale.setDefault(appLocale);
            Configuration appConfig = new Configuration();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                appConfig.setLocale(appLocale);
            else
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
*/

    public static void setTheme(Activity activity, boolean forPopup, boolean withToolbar/*, boolean withDrawerLayout*/, boolean forActivator)
    {
        int theme = getTheme(forPopup, withToolbar, /*withDrawerLayout,*/ forActivator, activity);
        if (theme != 0)
            activity.setTheme(theme);
    }

    static int getTheme(boolean forPopup, boolean withToolbar, /*boolean withDrawerLayout,*/ boolean forActivator, Context context) {
        switch (ApplicationPreferences.applicationTheme(context, false)) {
            /*case "color":
                if (forPopup) {
                    if (withToolbar)
                        return R.style.PopupTheme_withToolbar_color;
                    else
                        return R.style.PopupTheme_color;
                } else {
                    if (withToolbar) {
                        //if (withDrawerLayout)
                        //    return R.style.Theme_PhoneProfilesTheme_withToolbar_withDrawerLayout_color;
                        //else
                            return R.style.Theme_PhoneProfilesTheme_withToolbar_color;
                    } else
                        return R.style.Theme_PhoneProfilesTheme_color;
                }*/
            case "white":
                //AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                /*if (forPopup) {
                    if (withToolbar)
                        return R.style.PopupTheme_withToolbar_white;
                    else
                        return R.style.PopupTheme_white;
                } else {
                    if (withToolbar) {
                        //if (withDrawerLayout)
                        //    return R.style.Theme_PhoneProfilesTheme_withToolbar_withDrawerLayout_white;
                        //else
                            return R.style.Theme_PhoneProfilesTheme_withToolbar_white;
                    } else
                        return R.style.Theme_PhoneProfilesTheme_white;
                }*/
                break;
            case "dark":
                //AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                /*if (forPopup) {
                    if (withToolbar)
                        return R.style.PopupTheme_withToolbar_dark;
                    else
                        return R.style.PopupTheme_dark;
                } else {
                    if (withToolbar) {
                        //if (withDrawerLayout)
                        //    return R.style.Theme_PhoneProfilesTheme_withToolbar_withDrawerLayout_dark;
                        //else
                            return R.style.Theme_PhoneProfilesTheme_withToolbar_dark;
                    } else
                        return R.style.Theme_PhoneProfilesTheme_dark;
                }
                */
                break;
            /*case "dlight":
                if (forPopup) {
                    if (withToolbar)
                        return R.style.PopupTheme_withToolbar_dlight;
                    else
                        return R.style.PopupTheme_dlight;
                } else {
                    if (withToolbar) {
                        //if (withDrawerLayout)
                        //    return R.style.Theme_PhoneProfilesTheme_withToolbar_withDrawerLayout_dlight;
                        //else
                            return R.style.Theme_PhoneProfilesTheme_withToolbar_dlight;
                    } else
                        return R.style.Theme_PhoneProfilesTheme_dlight;
                }*/
            case "night_mode":
                //AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                /*if (forPopup) {
                    if (withToolbar)
                        return R.style.PopupTheme_withToolbar_dayNight;
                    else
                        return R.style.PopupTheme_dayNight;
                } else {
                    if (withToolbar) {
                        //if (withDrawerLayout)
                        //    return R.style.Theme_PhoneProfilesTheme_withToolbar_withDrawerLayout_dark;
                        //else
                        return R.style.Theme_PhoneProfilesTheme_withToolbar_dayNight;
                    } else
                        return R.style.Theme_PhoneProfilesTheme_dayNight;
                }*/
                break;
            default:
                //AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                /*if (forPopup) {
                    if (withToolbar)
                        return R.style.PopupTheme_withToolbar_white;
                    else
                        return R.style.PopupTheme_white;
                } else {
                    if (withToolbar) {
                        //if (withDrawerLayout)
                        //    return R.style.Theme_PhoneProfilesTheme_withToolbar_withDrawerLayout_white;
                        //else
                        return R.style.Theme_PhoneProfilesTheme_withToolbar_white;
                    } else
                        return R.style.Theme_PhoneProfilesTheme_white;
                }*/
                /*if (forPopup) {
                    if (withToolbar)
                        return R.style.PopupTheme_withToolbar_color;
                    else
                        return R.style.PopupTheme_color;
                } else {
                    if (withToolbar) {
                        //if (withDrawerLayout)
                        //    return R.style.Theme_PhoneProfilesTheme_withToolbar_withDrawerLayout_color;
                        //else
                            return R.style.Theme_PhoneProfilesTheme_withToolbar_color;
                    } else
                        return R.style.Theme_PhoneProfilesTheme_color;
                }*/
        }
        if (forActivator) {
            return R.style.ActivatorTheme_dayNight;
        }
        else
        if (forPopup) {
            if (withToolbar)
                return R.style.PopupTheme_withToolbar_dayNight;
            else
                return R.style.PopupTheme_dayNight;
        } else {
            if (withToolbar) {
                //if (withDrawerLayout)
                //    return R.style.Theme_PhoneProfilesTheme_withToolbar_withDrawerLayout_dark;
                //else
                return R.style.Theme_PhoneProfilesTheme_withToolbar_dayNight;
            } else
                return R.style.Theme_PhoneProfilesTheme_dayNight;
        }
    }

    private static void switchNightMode(Context appContext) {
        switch (ApplicationPreferences.applicationTheme(appContext, false)) {
            case "white":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case "dark":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case "night_mode":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
        }
    }

    static void switchNightMode(final Context appContext, boolean useMainLooperHandler) {
        if (useMainLooperHandler) {
            new Handler(getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    try {
                        switchNightMode(appContext);
                    } catch (Exception e) {
                        PPApplication.recordException(e);
                    }
                }
            });
        }
        else
            switchNightMode(appContext);
    }

    static void reloadActivity(final Activity activity, @SuppressWarnings("SameParameterValue") boolean newIntent)
    {
        if (activity == null)
            return;

        if (newIntent)
        {
            new Handler(activity.getMainLooper()).post(new Runnable() {

                @Override
                public void run() {
                    try {
                        Context context = activity.getApplicationContext();

                        Intent intent = activity.getIntent();
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION);

                        activity.finish();
                        activity.overridePendingTransition(0, 0);

                        context.startActivity(intent);
                        //activity.overridePendingTransition(0, 0);
                    } catch (Exception e) {
                        PPApplication.recordException(e);
                    }
                }
            });
        }
        else
            activity.recreate();
    }

    /*
    public static void setPreferenceTitleStyle(Preference preference, boolean enabled,
                                               boolean bold, boolean addBullet,
                                               boolean underline, boolean errorColor, boolean systemSettings)
    {
        if (preference != null) {
            CharSequence title = preference.getTitle();
            if (systemSettings) {
                String s = title.toString();
                if (!s.contains("(S)")) {
                    if (bold && addBullet)
                        title = TextUtils.concat("• (S) ", title);
                    else
                        title = TextUtils.concat("(S) ", title);
                }
            }
            if (addBullet) {
                if (bold) {
                    String s = title.toString();
                    if (!s.startsWith("• "))
                        title = TextUtils.concat("• ", title);
                } else {
                    String s = title.toString();
                    if (s.startsWith("• "))
                        title = TextUtils.replace(title, new String[]{"• "}, new CharSequence[]{""});
                }
            }
            Spannable sbt = new SpannableString(title);
            Object[] spansToRemove = sbt.getSpans(0, title.length(), Object.class);
            for (Object span : spansToRemove) {
                if (span instanceof CharacterStyle)
                    sbt.removeSpan(span);
            }
            if (bold || underline) {
                if (bold) {
                    sbt.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, sbt.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    sbt.setSpan(new RelativeSizeSpan(1.05f), 0, sbt.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                if (underline) {
                    if (bold && addBullet)
                        sbt.setSpan(new UnderlineSpan(), 2, sbt.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    else
                        sbt.setSpan(new UnderlineSpan(), 0, sbt.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                if (errorColor && enabled)
                    sbt.setSpan(new ForegroundColorSpan(Color.RED), 0, sbt.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                preference.setTitle(sbt);
            } else {
                preference.setTitle(sbt);
            }
        }
    }
    */

    static void setPreferenceTitleStyleX(androidx.preference.Preference preference, boolean enabled,
                                         boolean bold, //boolean addBullet,
                                         boolean underline, boolean errorColor, boolean systemSettings)
    {
        if (preference != null) {
            CharSequence title = preference.getTitle();
            if (systemSettings) {
                String s = title.toString();
                if (!s.contains("(S)")) {
                    if (bold/* && addBullet*/)
                        title = TextUtils.concat("• (S) ", title);
                    else
                        title = TextUtils.concat("(S) ", title);
                }
            }
            //if (addBullet) {
                String s = title.toString();
                if (bold) {
                    if (!s.startsWith("• "))
                        title = TextUtils.concat("• ", title);
                } else {
                    if (s.startsWith("• "))
                        title = TextUtils.replace(title, new String[]{"• "}, new CharSequence[]{""});
                }
            //}
            Spannable sbt = new SpannableString(title);
            Object[] spansToRemove = sbt.getSpans(0, title.length(), Object.class);
            for (Object span : spansToRemove) {
                if (span instanceof CharacterStyle)
                    sbt.removeSpan(span);
            }
            if (bold || underline) {
                if (bold) {
                    sbt.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, sbt.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    //sbt.setSpan(new RelativeSizeSpan(1.05f), 0, sbt.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                if (underline) {
                    if (bold/* && addBullet*/)
                        sbt.setSpan(new UnderlineSpan(), 2, sbt.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    else
                        sbt.setSpan(new UnderlineSpan(), 0, sbt.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                if (errorColor && enabled)
                    sbt.setSpan(new ForegroundColorSpan(Color.RED), 0, sbt.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            preference.setTitle(sbt);
        }
    }

    static void setImageButtonEnabled(boolean enabled, AppCompatImageButton item, /*int iconResId,*/ Context context) {
        item.setEnabled(enabled);
        //Drawable originalIcon = ContextCompat.getDrawable(context, iconResId);
        //Drawable icon = enabled ? originalIcon : convertDrawableToGrayScale(originalIcon);
        //item.setImageDrawable(icon);
        if (enabled)
            item.setColorFilter(null);
        else
            item.setColorFilter(context.getColor(R.color.activityDisabledTextColor), PorterDuff.Mode.SRC_IN);
    }

/*    private static Drawable convertDrawableToGrayScale(Drawable drawable) {
        if (drawable == null) {
            return null;
        }
        Drawable res = drawable.mutate();
        res.setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_IN);
        return res;
    } */

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

    private static int dip(int dp) {
        return (int) (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, Resources.getSystem().getDisplayMetrics()));
    }

    private static int sip(int sp) {
        return (int) (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, Resources.getSystem().getDisplayMetrics()));
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

    static Spanned fromHtml(String source, boolean forBullets, boolean forNumbers, int numberFrom, int sp) {
        Spanned htmlSpanned;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (forNumbers)
                htmlSpanned = Html.fromHtml(source, Html.FROM_HTML_MODE_COMPACT, null, new LiTagHandler());
            else {
                htmlSpanned = Html.fromHtml(source, Html.FROM_HTML_MODE_COMPACT);
                //htmlSpanned = Html.fromHtml(source, Html.FROM_HTML_MODE_COMPACT, null, new LiTagHandler());
            }
        } else {
            if (forBullets || forNumbers)
                htmlSpanned = Html.fromHtml(source, null, new LiTagHandler());
            else
                htmlSpanned = Html.fromHtml(source);
        }

        if (forBullets)
            return addBullets(htmlSpanned);
        else
        if (forNumbers)
            return addNumbers(htmlSpanned, numberFrom, sp);
        else
            return  htmlSpanned;

    }

    private static SpannableStringBuilder addBullets(Spanned htmlSpanned) {
        SpannableStringBuilder spannableBuilder = new SpannableStringBuilder(htmlSpanned);
        BulletSpan[] spans = spannableBuilder.getSpans(0, spannableBuilder.length(), BulletSpan.class);
        if (spans != null) {
            for (BulletSpan span : spans) {
                int start = spannableBuilder.getSpanStart(span);
                int end  = spannableBuilder.getSpanEnd(span);
                spannableBuilder.removeSpan(span);
                spannableBuilder.setSpan(new ImprovedBulletSpan(dip(2), dip(8), 0), start, end, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            }
        }
        return spannableBuilder;
    }

    private static SpannableStringBuilder addNumbers(Spanned htmlSpanned, int numberFrom, int sp) {
        int listItemCount = numberFrom-1;
        SpannableStringBuilder spannableBuilder = new SpannableStringBuilder(htmlSpanned);
        BulletSpan[] spans = spannableBuilder.getSpans(0, spannableBuilder.length(), BulletSpan.class);
        if (spans != null) {
            for (BulletSpan span : spans) {
                int start = spannableBuilder.getSpanStart(span);
                int end  = spannableBuilder.getSpanEnd(span);
                spannableBuilder.removeSpan(span);
                ++listItemCount;
                spannableBuilder.insert(start, listItemCount + ". ");
                spannableBuilder.setSpan(new LeadingMarginSpan.Standard(0, sip(sp)), start, end, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            }
        }
        return spannableBuilder;
    }

    @SuppressLint("DefaultLocale")
    static String getDurationString(int duration) {
        int hours = duration / 3600;
        int minutes = (duration % 3600) / 60;
        int seconds = duration % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    @SuppressLint("DefaultLocale")
    static String getTimeString(int time) {
        int hours = time / 60;
        int minutes = (time % 60);
        return String.format("%02d:%02d", hours, minutes);
    }

    static String getListPreferenceString(String value, int arrayValuesRes,
                                          int arrayStringsRes, Context context) {
        String[] arrayValues = context.getResources().getStringArray(arrayValuesRes);
        String[] arrayStrings = context.getResources().getStringArray(arrayStringsRes);
        int index = 0;
        for (String arrayValue : arrayValues) {
            if (arrayValue.equals(value))
                break;
            ++index;
        }
        try {
            return arrayStrings[index];
        } catch (Exception e) {
            return context.getString(R.string.array_pref_no_change);
        }
    }

    static String getZenModePreferenceString(String value, Context context) {
        String[] arrayValues = context.getResources().getStringArray(R.array.zenModeValues);
        String[] arrayStrings = context.getResources().getStringArray(R.array.zenModeArray);
        String[] arraySummaryStrings = context.getResources().getStringArray(R.array.zenModeSummaryArray);
        int index = 0;
        for (String arrayValue : arrayValues) {
            if (arrayValue.equals(value))
                break;
            ++index;
        }
        try {
            return arrayStrings[index] + " - " + arraySummaryStrings[Integer.parseInt(value) - 1];
        } catch (Exception e) {
            return context.getString(R.string.array_pref_no_change);
        }
    }

    static void setRingtonePreferenceSummary(final String initSummary, final String ringtoneUri,
                                             final androidx.preference.Preference preference, final Context context) {
        new AsyncTask<Void, Integer, Void>() {

            private String ringtoneName;

            @Override
            protected Void doInBackground(Void... params) {
                if ((ringtoneUri == null) || ringtoneUri.isEmpty())
                    ringtoneName = context.getString(R.string.ringtone_preference_none);
                else {
                    Uri uri = Uri.parse(ringtoneUri);
                    Ringtone ringtone = RingtoneManager.getRingtone(context, uri);
                    try {
                        ringtoneName = ringtone.getTitle(context);
                    } catch (Exception e) {
                        ringtoneName = context.getString(R.string.ringtone_preference_not_set);
                    }
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                super.onPostExecute(result);
                String summary = TextUtils.replace(initSummary, new String[]{"<ringtone_name>"}, new String[]{ringtoneName}).toString();
                preference.setSummary(GlobalGUIRoutines.fromHtml(summary, false, false, 0, 0));
            }

        }.execute();
    }

    static void setProfileSoundsPreferenceSummary(final String initSummary,
                                             final String ringtoneUri, final String notificationUri, final String alarmUri,
                                             final androidx.preference.Preference preference, final Context context) {
        new AsyncTask<Void, Integer, Void>() {

            private String ringtoneName;
            private String notificationName;
            private String alarmName;

            @Override
            protected Void doInBackground(Void... params) {
                if ((ringtoneUri == null) || ringtoneUri.isEmpty())
                    ringtoneName = context.getString(R.string.ringtone_preference_none);
                else {
                    String[] splits = ringtoneUri.split("\\|");
                    Uri uri = Uri.parse(splits[0]);
                    Ringtone ringtone = RingtoneManager.getRingtone(context, uri);
                    try {
                        ringtoneName = ringtone.getTitle(context);
                    } catch (Exception e) {
                        ringtoneName = context.getString(R.string.ringtone_preference_not_set);
                    }
                }

                if ((notificationUri == null) || notificationUri.isEmpty())
                    notificationName = context.getString(R.string.ringtone_preference_none);
                else {
                    String[] splits = notificationUri.split("\\|");
                    Uri uri = Uri.parse(splits[0]);
                    Ringtone ringtone = RingtoneManager.getRingtone(context, uri);
                    try {
                        notificationName = ringtone.getTitle(context);
                    } catch (Exception e) {
                        notificationName = context.getString(R.string.ringtone_preference_not_set);
                    }
                }

                if ((alarmUri == null) || alarmUri.isEmpty())
                    alarmName = context.getString(R.string.ringtone_preference_none);
                else {
                    String[] splits = alarmUri.split("\\|");
                    Uri uri = Uri.parse(splits[0]);
                    Ringtone ringtone = RingtoneManager.getRingtone(context, uri);
                    try {
                        alarmName = ringtone.getTitle(context);
                    } catch (Exception e) {
                        alarmName = context.getString(R.string.ringtone_preference_not_set);
                    }
                }

                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                super.onPostExecute(result);
                String summary = TextUtils.replace(initSummary,
                        new String[]{"<ringtone_name>", "<notification_name>", "<alarm_name>"},
                        new String[]{ringtoneName, notificationName, alarmName}).toString();
                preference.setSummary(GlobalGUIRoutines.fromHtml(summary, false, false, 0, 0));
            }

        }.execute();
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

    static int getThemeWhiteTextColor(final Context context) {
        final TypedValue value = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.activityWhiteTextColor, value, true);
        return value.data;
    }
    static int getThemeNormalTextColor(final Context context) {
        final TypedValue value = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.activityNormalTextColor, value, true);
        return value.data;
    }

    static int getThemeDisabledTextColor(final Context context) {
        final TypedValue value = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.activityDisabledTextColor, value, true);
        return value.data;
    }

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
        int theme = GlobalGUIRoutines.getTheme(false, false, /*false,*/ false, context);
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

    /*
    static int getThemeActivatorGridDividerColor(final boolean show, final Context context) {
        final TypedValue value = new TypedValue();
        if (show)
            context.getTheme().resolveAttribute(android.R.attr.listDivider, value, false);
        else
            context.getTheme().resolveAttribute(R.attr.activityBackgroundColor, value, false);
        return value.data;
    }
    */

    /*
    static int getThemeActivityLogTypeOtherColor(final Context context) {
        final TypedValue value = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.activityLogTypeOther, value, true);
        return value.data;
    }
    */

    static private int getThemeEditorSpinnerDropDownTextColor(final Context context) {
        final TypedValue value = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.editorSpinnerDropDownTextColor, value, true);
        return value.data;
    }

    /*
    static int getThemeDialogDividerColor(final Context context) {
        final TypedValue value = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.dialogDivider, value, true);
        return value.data;
    }
    */

    static private int getThemeEditorFilterBackgroundColor(final Context context) {
        final TypedValue value = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.editorFilterBackgroundColor, value, true);
        return value.data;
    }

    static void setThemeTimeDurationPickerDisplay(TimeDurationPicker timeDurationPicker, final Activity activity) {
        if (ApplicationPreferences.applicationTheme(activity, true).equals("white")) {
            timeDurationPicker.setDisplayTextAppearance(R.style.TextAppearance_TimeDurationPicker_Display);
            timeDurationPicker.setUnitTextAppearance(R.style.TextAppearance_TimeDurationPicker_Unit);
            timeDurationPicker.setBackspaceIcon(ContextCompat.getDrawable(activity, R.drawable.ic_backspace_light));
            timeDurationPicker.setClearIcon(ContextCompat.getDrawable(activity, R.drawable.ic_clear_light));
        }
        else {
            timeDurationPicker.setDisplayTextAppearance(R.style.TextAppearance_TimeDurationPicker_Display_Dark);
            timeDurationPicker.setUnitTextAppearance(R.style.TextAppearance_TimeDurationPicker_Unit_Dark);
            timeDurationPicker.setBackspaceIcon(ContextCompat.getDrawable(activity, R.drawable.ic_backspace));
            timeDurationPicker.setClearIcon(ContextCompat.getDrawable(activity, R.drawable.ic_clear));
        }
        timeDurationPicker.setDurationDisplayBackgroundColor(getThemeEditorFilterBackgroundColor(activity));
        //timeDurationPicker.setSeparatorColor(GlobalGUIRoutines.getThemeDialogDividerColor(activity));
    }

    static int getThemeSecondaryTextColor(final Context context) {
        final TypedValue value = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.activitySecondaryTextColor, value, true);
        return value.data;
    }

    /*
    static int getResourceId(String pVariableName, String pResourceName, Context context)
    {
        try {
            return context.getResources().getIdentifier(pVariableName, pResourceName, context.PPApplication.PACKAGE_NAME);
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

    /*
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
    */

    @SuppressLint("SourceLockedOrientationActivity")
    static void lockScreenOrientation(Activity activity, boolean toDefault) {
        try {
            if ((Build.VERSION.SDK_INT != 26) && (!toDefault)) {
                int currentOrientation = activity.getResources().getConfiguration().orientation;
                if (currentOrientation == Configuration.ORIENTATION_PORTRAIT) {
                    activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
                } else {
                    activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
                }
            }
            else
                // this set device to default orientation (for mobile to portrait, for 10' tablets to landscape)
                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
        } catch (Exception e) {
            // FC in API 26 (A8) - Google bug: java.lang.IllegalStateException: Only fullscreen activities can request orientation
            PPApplication.recordException(e);
        }
    }

    static void unlockScreenOrientation(Activity activity) {
        try {
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
        } catch (Exception e) {
            // FC in API 26 (A8) - Google bug: java.lang.IllegalStateException: Only fullscreen activities can request orientation
            PPApplication.recordException(e);
        }
    }

    static class LiTagHandler implements Html.TagHandler {

        @Override
        public void handleTag(boolean opening, String tag, Editable output, XMLReader xmlReader) {

            class Bullet {}

            if (tag.equals("li") && opening) {
                output.setSpan(new Bullet(), output.length(), output.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
            }
            if (tag.equals("li") && !opening) {
                //output.append("\n\n");
                output.append("\n");
                Bullet[] spans = output.getSpans(0, output.length(), Bullet.class);
                if (spans != null) {
                    Bullet lastMark = spans[spans.length-1];
                    int start = output.getSpanStart(lastMark);
                    output.removeSpan(lastMark);
                    if (start != output.length()) {
                        output.setSpan(new BulletSpan(), start, output.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                    }
                }
            }
        }

    }

    static class HighlightedSpinnerAdapter extends ArrayAdapter<String> {

        private int mSelectedIndex = -1;
        private final Activity activity;

        @SuppressWarnings("SameParameterValue")
        HighlightedSpinnerAdapter(Activity activity, int textViewResourceId, String[] objects) {
            super(activity, textViewResourceId, objects);
            this.activity = activity;
        }

        @Override
        public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent){
            View itemView =  super.getDropDownView(position, convertView, parent);

            TextView itemText = itemView.findViewById(android.R.id.text1);
            if (itemText != null) {
                if (position == mSelectedIndex) {
                    itemText.setTextColor(GlobalGUIRoutines.getThemeAccentColor(activity));
                } else {
                    itemText.setTextColor(GlobalGUIRoutines.getThemeEditorSpinnerDropDownTextColor(activity));
                }
            }

            return itemView;
        }

        void setSelection(int position) {
            mSelectedIndex =  position;
            notifyDataSetChanged();
        }

    }

    static boolean areSystemAnimationsEnabled(Context context) {
        float duration, transition;
        //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            duration = Settings.Global.getFloat(
                    context.getContentResolver(),
                    Settings.Global.ANIMATOR_DURATION_SCALE, 1);
            transition = Settings.Global.getFloat(
                    context.getContentResolver(),
                    Settings.Global.TRANSITION_ANIMATION_SCALE, 1);
        /*} else {
            duration = Settings.System.getFloat(
                    context.getContentResolver(),
                    Settings.System.ANIMATOR_DURATION_SCALE, 1);
            transition = Settings.System.getFloat(
                    context.getContentResolver(),
                    Settings.System.TRANSITION_ANIMATION_SCALE, 1);
        }*/
        return (duration != 0 && transition != 0);
    }

}
