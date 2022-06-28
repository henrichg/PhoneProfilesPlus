package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;

import androidx.core.os.ConfigurationCompat;
import androidx.core.os.LocaleListCompat;

import java.util.Locale;

public class LocaleHelper {

    private static final String SELECTED_LANGUAGE = "Locale.Helper.Selected.Language";

    public static Context onAttach(Context context) {
        LocaleListCompat systemLocales = ConfigurationCompat.getLocales(Resources.getSystem().getConfiguration());
        String lang = getPersistedData(context, systemLocales.get(0).getLanguage());
        if (lang.equals("[sys]")) {
            lang = systemLocales.get(0).getLanguage();
        }
        return setLocale(context, lang, false);
    }

    /*
    public static Context onAttach(Context context, String defaultLanguage) {
        String lang = getPersistedData(context, defaultLanguage);
        if (lang.equals("[sys]")) {
            LocaleListCompat systemLocales = ConfigurationCompat.getLocales(Resources.getSystem().getConfiguration());
            lang = systemLocales.get(0).getLanguage();
        }
        return setLocale(context, lang, false);
    }
    */

    public static String getLanguage(Context context) {
        return getPersistedData(context, Locale.getDefault().getLanguage());
    }

    public static Context setLocale(Context context, String language, boolean persist) {
        if (persist)
            persist(context, language);

        if (language.equals("[sys]")) {
            LocaleListCompat systemLocales = ConfigurationCompat.getLocales(Resources.getSystem().getConfiguration());
            language = systemLocales.get(0).getLanguage();
        }

        //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        return updateResources(context, language);
        //}

        //return updateResourcesLegacy(context, language);
    }

    private static String getPersistedData(Context context, String defaultLanguage) {
        //SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences preferences = context.getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getString(SELECTED_LANGUAGE, defaultLanguage);
    }

    private static void persist(Context context, String language) {
        //SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences preferences = context.getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(SELECTED_LANGUAGE, language);
        editor.apply();
    }

    //@TargetApi(Build.VERSION_CODES.N)
    private static Context updateResources(Context context, String language) {
        Locale locale = new Locale(language);
        Locale.setDefault(locale);

        Configuration configuration = context.getResources().getConfiguration();
        configuration.setLocale(locale);
        configuration.setLayoutDirection(locale);

        return context.createConfigurationContext(configuration);
    }

    /*
    @SuppressWarnings("deprecation")
    private static Context updateResourcesLegacy(Context context, String language) {
        Locale locale = new Locale(language);
        Locale.setDefault(locale);

        Resources resources = context.getResources();

        Configuration configuration = resources.getConfiguration();
        configuration.locale = locale;
        //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            configuration.setLayoutDirection(locale);
        //}

        resources.updateConfiguration(configuration, resources.getDisplayMetrics());

        return context;
    }
    */
}
