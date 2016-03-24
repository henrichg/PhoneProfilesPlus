package sk.henrichg.phoneprofilesplus;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatImageButton;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.CharacterStyle;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.view.View;
import android.widget.EditText;
import android.widget.NumberPicker;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.Collator;
import java.util.Locale;

public class GUIData {

    public static BrightnessView brightneesView = null;
    public static BrightnessView keepScreenOnView = null;
    public static Collator collator = null;

    // import/export
    public static final String DB_FILEPATH = "/data/" + GlobalData.PACKAGE_NAME + "/databases";
    public static final String REMOTE_EXPORT_PATH = "/PhoneProfiles";
    public static final String EXPORT_APP_PREF_FILENAME = "ApplicationPreferences.backup";
    public static final String EXPORT_DEF_PROFILE_PREF_FILENAME = "DefaultProfilePreferences.backup";

    // this string is from material-preferences linrary (https://github.com/ferrannp/material-preferences)
    public static final String MAIN_PREFERENCE_FRAGMENT_TAG = "com.fnp.materialpreferences.MainFragment";

    public static void setLanguage(Context context)//, boolean restart)
    {
        // jazyk na aky zmenit
        String lang = GlobalData.applicationLanguage;

        Locale appLocale;

        if (!lang.equals("system"))
        {
            String[] langSplit = lang.split("-");
            if (langSplit.length == 1)
                appLocale = new Locale(lang);
            else
                appLocale = new Locale(langSplit[0], langSplit[1]);
        }
        else
        {
            appLocale = Resources.getSystem().getConfiguration().locale;
        }

        Locale.setDefault(appLocale);
        Configuration appConfig = new Configuration();
        appConfig.locale = appLocale;
        /*  not working :-/
        if (android.os.Build.VERSION.SDK_INT == 17) {
            // workaround for Android 4.2 and wrong RTL layout
            int screenLayout = Resources.getSystem().getConfiguration().screenLayout;
            //if ((screenLayout & Configuration.SCREENLAYOUT_LAYOUTDIR_RTL) > 0)
            //    appConfig.screenLayout = screenLayout ^ Configuration.SCREENLAYOUT_LAYOUTDIR_MASK;
            screenLayout = screenLayout & (~Configuration.SCREENLAYOUT_LAYOUTDIR_MASK);
            screenLayout = screenLayout | Configuration.SCREENLAYOUT_LAYOUTDIR_LTR;
            appConfig.screenLayout = screenLayout;
        }
        */

        context.getResources().updateConfiguration(appConfig, context.getResources().getDisplayMetrics());

        // collator for application locale sorting
        collator = getCollator();

        //languageChanged = restart;
    }

    public static Collator getCollator()
    {
        // get application Locale
        String lang = GlobalData.applicationLanguage;
        Locale appLocale;
        if (!lang.equals("system"))
        {
            String[] langSplit = lang.split("-");
            if (langSplit.length == 1)
                appLocale = new Locale(lang);
            else
                appLocale = new Locale(langSplit[0], langSplit[1]);
        }
        else
        {
            appLocale = Resources.getSystem().getConfiguration().locale;
        }

        // get collator for application locale
        return Collator.getInstance(appLocale);
    }

    public static void setTheme(Activity activity, boolean forPopup, boolean withToolbar)
    {
        activity.setTheme(getTheme(forPopup, withToolbar));
    }

    public static int getTheme(boolean forPopup, boolean withToolbar) {
        if (GlobalData.applicationTheme.equals("material"))
        {
            if (forPopup)
            {
                if (withToolbar)
                    return R.style.PopupTheme_withToolbar_material;
                else
                    return R.style.PopupTheme_material;
            }
            else
            {
                if (withToolbar)
                    return R.style.Theme_Phoneprofilestheme_withToolbar_material;
                else
                    return R.style.Theme_Phoneprofilestheme_material;
            }
        }
        else
        if (GlobalData.applicationTheme.equals("dark"))
        {
            if (forPopup)
            {
                if (withToolbar)
                    return R.style.PopupTheme_withToolbar_dark;
                else
                    return R.style.PopupTheme_dark;
            }
            else
            {
                if (withToolbar)
                    return R.style.Theme_Phoneprofilestheme_withToolbar_dark;
                else
                    return R.style.Theme_Phoneprofilestheme_dark;
            }
        }
        else
        if (GlobalData.applicationTheme.equals("dlight"))
        {
            if (forPopup)
            {
                if (withToolbar)
                    return R.style.PopupTheme_withToolbar_dlight;
                else
                    return R.style.PopupTheme_dlight;
            }
            else
            {
                if (withToolbar)
                    return R.style.Theme_Phoneprofilestheme_withToolbar_dlight;
                else
                    return R.style.Theme_Phoneprofilestheme_dlight;
            }
        }
        return 0;
    }

    public static int getDialogTheme(boolean forAlert) {
        if (GlobalData.applicationTheme.equals("material"))
        {
            if (forAlert)
                return R.style.AlertDialogStyle;
            else
                return R.style.DialogStyle;
        }
        else
        if (GlobalData.applicationTheme.equals("dark"))
        {
            if (forAlert)
                return R.style.AlertDialogStyleDark;
            else
                return R.style.DialogStyleDark;
        }
        else
        if (GlobalData.applicationTheme.equals("dlight"))
        {
            if (forAlert)
                return R.style.AlertDialogStyle;
            else
                return R.style.DialogStyle;
        }
        return 0;
    }

    public static void reloadActivity(Activity activity, boolean newIntent)
    {
        if (newIntent)
        {
            final Activity _activity = activity;
            new Handler().post(new Runnable() {

                @Override
                public void run()
                {
                    Intent intent = _activity.getIntent();
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    _activity.overridePendingTransition(0, 0);
                    _activity.finish();

                    _activity.overridePendingTransition(0, 0);
                    _activity.startActivity(intent);
                }
            });
        }
        else
            activity.recreate();
    }

    public static void setPreferenceTitleStyle(Preference preference, boolean bold, boolean underline, boolean errorColor)
    {
        CharSequence title = preference.getTitle();
        Spannable sbt = new SpannableString(title);
        Object spansToRemove[] = sbt.getSpans(0, title.length(), Object.class);
        for(Object span: spansToRemove){
            if(span instanceof CharacterStyle)
                sbt.removeSpan(span);
        }
        if (bold || underline)
        {
            if (bold)
                sbt.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, title.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            if (underline)
                sbt.setSpan(new UnderlineSpan(), 0, title.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            if (errorColor)
                sbt.setSpan(new ForegroundColorSpan(Color.RED), 0, title.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            preference.setTitle(sbt);
        }
        else
        {
            preference.setTitle(sbt);
        }
    }

    public static void registerOnActivityDestroyListener(Preference preference, PreferenceManager.OnActivityDestroyListener listener) {
        try {
            PreferenceManager pm = preference.getPreferenceManager();
            Method method = pm.getClass().getDeclaredMethod(
                    "registerOnActivityDestroyListener",
                    PreferenceManager.OnActivityDestroyListener.class);
            method.setAccessible(true);
            method.invoke(pm, listener);
        } catch (Exception ignored) {
        }
    }

    public static void unregisterOnActivityDestroyListener(Preference preference, PreferenceManager.OnActivityDestroyListener listener) {
        try {
            PreferenceManager pm = preference.getPreferenceManager();
            Method method = pm.getClass().getDeclaredMethod(
                    "unregisterOnActivityDestroyListener",
                    PreferenceManager.OnActivityDestroyListener.class);
            method.setAccessible(true);
            method.invoke(pm, listener);
        } catch (Exception ignored) {
        }
    }

    /**
     * Sets the specified image buttonto the given state, while modifying or
     * "graying-out" the icon as well
     *
     * @param enabled The state of the menu item
     * @param item The menu item to modify
     * @param iconResId The icon ID
     */
    public static void setImageButtonEnabled(boolean enabled, AppCompatImageButton item, int iconResId, Context context) {
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
    public static Drawable convertDrawableToGrayScale(Drawable drawable) {
        if (drawable == null) {
            return null;
        }
        Drawable res = drawable.mutate();
        res.setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_IN);
        return res;
    }

    public static float pixelsToSp(Context context, float px) {
        return px / context.getResources().getDisplayMetrics().scaledDensity;
    }

    public static float spToPixels(Context context, float sp) {
        return sp * context.getResources().getDisplayMetrics().scaledDensity;
    }

    /**
     * Uses reflection to access divider private attribute and override its color
     * Use Color.Transparent if you wish to hide them
     */
    public static void setSeparatorColorForNumberPicker(NumberPicker picker, int separatorColor) {
        Field[] pickerFields = NumberPicker.class.getDeclaredFields();
        for (Field pf : pickerFields) {
            if (pf.getName().equals("mSelectionDivider")) {
                pf.setAccessible(true);
                try {
                    pf.set(picker, new ColorDrawable(separatorColor));
                } catch (IllegalAccessException | IllegalArgumentException e) {
                    e.printStackTrace();
                }
                break;
            }
        }
    }

    public static void updateTextAttributesForNumberPicker(NumberPicker picker, /*int textColor,*/ int textSizeSP) {
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
                catch (NoSuchFieldException | IllegalAccessException | IllegalArgumentException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
